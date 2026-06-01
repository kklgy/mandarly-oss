package com.mandarly.boot.module.edu.service.webhook;

import com.mandarly.boot.framework.tenant.core.context.TenantContextHolder;
import com.mandarly.boot.module.edu.framework.stripe.client.StripeClient;
import com.mandarly.boot.module.edu.framework.stripe.config.StripeProperties;
import com.mandarly.boot.module.edu.service.payment.PaymentService;
import com.mandarly.boot.module.edu.service.refund.RefundService;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Stripe Webhook 处理器 — 两段式事务核心
 *
 * <p><b>事务边界规范(PRD-v4 §4.2 锁定,禁止修改):</b>
 * <ol>
 *   <li>段一(REQUIRES_NEW):INSERT IGNORE 去重 + 即时 commit;返回 false → skip</li>
 *   <li>段二(无包裹):业务 Service 各自 @Transactional;webhook 上下文无租户,先 setIgnore(true)</li>
 *   <li>段三(REQUIRES_NEW):段二 throw 时 markEventFailed — 独立事务,不被段二回滚</li>
 * </ol>
 *
 * <p><b>事务实际边界由 {@link StripeEventTxHelper} 持有</b>:Spring AOP 同类内部
 * 调用不走代理,REQUIRES_NEW 会退化为同事务。把段一/段三方法抽到独立 Bean,
 * 通过 @Resource 注入再调用才生效(self-invocation 修复 — A-pre-4)。
 *
 * <p><b>Controller 始终 200</b>:签名失败 401 由 Controller 处理,业务失败由 stripe_event.result=failed 监控。
 */
@Slf4j
@Service
public class StripeWebhookHandler {

    @Resource
    private StripeClient stripeClient;

    @Resource
    private StripeProperties props;

    @Resource
    private StripeEventTxHelper txHelper;

    @Resource
    private PaymentService paymentService;

    @Resource
    private RefundService refundService;

    /**
     * Webhook 入口 — 不带 @Transactional,事务边界在 {@link StripeEventTxHelper}。
     *
     * @param payload   请求体原始字符串
     * @param sigHeader Stripe-Signature 请求头
     */
    public void process(String payload, String sigHeader) {
        // 1. 验签(失败抛 STRIPE_WEBHOOK_SIG_INVALID,由 Controller 转 401)
        Event event = stripeClient.verifyWebhookSignature(payload, sigHeader, props.getWebhookSecret());

        // 2. 段一(REQUIRES_NEW 独立事务):INSERT IGNORE 去重 + 即时 commit
        boolean isNewEvent = txHelper.persistEventIgnoreDuplicate(event, payload);
        if (!isNewEvent) {
            log.info("[process] duplicate event {}, skip business", event.getId());
            return;
        }

        // 3. 段二(业务 Service 内部各自 @Transactional):webhook 无租户上下文,先 setIgnore
        TenantContextHolder.setIgnore(true);
        try {
            dispatch(event);
            // 业务成功:stripe_event.result 已在段一 INSERT 时设为 'success'
        } catch (Exception e) {
            log.error("[process] business handler failed for event {}, marking failed", event.getId(), e);
            // 段三(REQUIRES_NEW 独立事务):标 failed + error_msg,不影响段一已 commit 的去重行
            txHelper.markEventFailed(event.getId(), e);
            // 不再 rethrow,Controller 返回 200 防 Stripe 重发(业务失败监控 stripe_event.result=failed)
        } finally {
            TenantContextHolder.clear();
        }
    }

    // ==================== 段二:dispatch 路由 ====================

    private void dispatch(Event event) {
        switch (event.getType()) {
            case "checkout.session.completed":
                paymentService.handleCheckoutCompleted(extractSession(event));
                break;
            case "checkout.session.expired":
                paymentService.handleSessionExpired(extractSessionId(event));
                break;
            case "charge.refunded":
                refundService.handleChargeRefunded(extractChargeId(event));
                break;
            case "charge.updated":
                paymentService.handleChargeUpdated(extractCharge(event));
                break;
            case "payment_intent.payment_failed":
                paymentService.handlePaymentFailed(extractPaymentIntentId(event));
                break;
            default:
                log.info("[dispatch] unhandled event type={} eventId={}", event.getType(), event.getId());
                txHelper.markEventIgnored(event.getId());
        }
    }

    // ==================== helper: 从 Event 提取对象 / ID ====================

    private String extractSessionId(Event event) {
        return extractSession(event).getId();
    }

    private Session extractSession(Event event) {
        return (Session) deserializeStripeObject(event);
    }

    private String extractChargeId(Event event) {
        return ((Charge) deserializeStripeObject(event)).getId();
    }

    private Charge extractCharge(Event event) {
        return (Charge) deserializeStripeObject(event);
    }

    private String extractPaymentIntentId(Event event) {
        return ((PaymentIntent) deserializeStripeObject(event)).getId();
    }

    /**
     * 反序列化 Event.data.object 为 StripeObject。
     * <p>先 getObject() 严格匹配,失败用 deserializeUnsafe() 兜底
     * (Stripe SDK 文档推荐做法 — API 版本与 SDK 编译版本不一致时,严格反序列化会丢字段)。
     * <p>这条路径在两种情况下触发:① stripe trigger 模拟事件用最新 API 版本;
     * ② Stripe 服务端推送时账户 API 版本升级早于 SDK 升级。
     */
    private StripeObject deserializeStripeObject(Event event) {
        Optional<StripeObject> strict = event.getDataObjectDeserializer().getObject();
        if (strict.isPresent()) {
            return strict.get();
        }
        try {
            return event.getDataObjectDeserializer().deserializeUnsafe();
        } catch (com.stripe.exception.EventDataObjectDeserializationException e) {
            throw new IllegalStateException(
                    "cannot deserialize event " + event.getId() + " type=" + event.getType(), e);
        }
    }
}
