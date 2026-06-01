package com.mandarly.boot.module.edu.framework.stripe.client;

import com.mandarly.boot.module.edu.framework.stripe.config.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.RefundCollection;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.ChargeRetrieveParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.RefundListParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.STRIPE_CHANNEL_ERROR;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.STRIPE_WEBHOOK_SIG_INVALID;

/**
 * Stripe API 封装实现
 *
 * <p>所有对 Stripe 的 HTTP 调用收口到此类:
 * <ul>
 *   <li>createCheckoutSession:idempotency-key = "payment-{paymentId}"</li>
 *   <li>retrievePaymentIntent:expand latest_charge.balance_transaction</li>
 *   <li>createRefund:idempotency-key = "refund-{refundId}",原支付币种金额</li>
 *   <li>verifyWebhookSignature:签名验证失败抛 STRIPE_WEBHOOK_SIG_INVALID</li>
 * </ul>
 */
@Slf4j
@Component
public class StripeClientImpl implements StripeClient {

    private final StripeProperties props;

    public StripeClientImpl(StripeProperties props) {
        this.props = props;
    }

    @Override
    public Session createCheckoutSession(CreateSessionRequest req) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(req.getCurrency().toLowerCase())
                                    .setUnitAmount(req.getUnitAmount().movePointRight(2).longValueExact())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(req.getPackageNameI18n())
                                            .build())
                                    .build())
                            .build())
                    .setSuccessUrl(req.getSuccessUrl())
                    .setCancelUrl(req.getCancelUrl())
                    .setExpiresAt(Instant.now().plusSeconds(req.getExpiresInMinutes() * 60L).getEpochSecond())
                    .putMetadata("payment_id", String.valueOf(req.getPaymentId()))
                    .putMetadata("user_id", String.valueOf(req.getUserId()))
                    .putMetadata("package_id", String.valueOf(req.getPackageId()))
                    // Stripe 26.x 不含 AutomaticPaymentMethods 参数类,使用 addPaymentMethodTypes 兜底
                    // 留空则 Stripe Dashboard 默认配置自动生效
                    .build();

            RequestOptions.RequestOptionsBuilder optsBuilder = RequestOptions.builder()
                    .setIdempotencyKey("payment-" + req.getPaymentId());
            RequestOptions.RequestOptionsBuilder.unsafeSetStripeVersionOverride(optsBuilder, props.getApiVersion());
            RequestOptions opts = optsBuilder.build();

            return Session.create(params, opts);

        } catch (StripeException e) {
            log.error("[createCheckoutSession][paymentId={}] Stripe API failed", req.getPaymentId(), e);
            throw exception(STRIPE_CHANNEL_ERROR);
        }
    }

    @Override
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) {
        try {
            PaymentIntentRetrieveParams params = PaymentIntentRetrieveParams.builder()
                    .addExpand("latest_charge.balance_transaction")
                    .build();
            return PaymentIntent.retrieve(paymentIntentId, params, null);
        } catch (StripeException e) {
            log.error("[retrievePaymentIntent][{}] Stripe API failed", paymentIntentId, e);
            throw exception(STRIPE_CHANNEL_ERROR);
        }
    }

    @Override
    public Charge retrieveCharge(String chargeId) {
        try {
            ChargeRetrieveParams params = ChargeRetrieveParams.builder()
                    .addExpand("balance_transaction")
                    .build();
            return Charge.retrieve(chargeId, params, null);
        } catch (StripeException e) {
            log.error("[retrieveCharge][{}] Stripe API failed", chargeId, e);
            throw exception(STRIPE_CHANNEL_ERROR);
        }
    }

    @Override
    public Refund createRefund(CreateRefundRequest req) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(req.getPaymentIntentId())
                    .setAmount(req.getAmount().movePointRight(2).longValueExact())
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .putMetadata("refund_id", String.valueOf(req.getRefundId()))
                    .build();

            RequestOptions.RequestOptionsBuilder refundOptsBuilder = RequestOptions.builder()
                    .setIdempotencyKey("refund-" + req.getRefundId());
            RequestOptions.RequestOptionsBuilder.unsafeSetStripeVersionOverride(refundOptsBuilder, props.getApiVersion());
            RequestOptions opts = refundOptsBuilder.build();

            return Refund.create(params, opts);
        } catch (StripeException e) {
            log.error("[createRefund][refundId={}] Stripe API failed", req.getRefundId(), e);
            throw exception(STRIPE_CHANNEL_ERROR);
        }
    }

    @Override
    public List<Refund> listRefundsByRefundId(Long refundId) {
        try {
            RefundListParams params = RefundListParams.builder()
                    .putExtraParam("metadata[refund_id]", String.valueOf(refundId))
                    .setLimit(10L)
                    .build();
            RefundCollection collection = Refund.list(params);
            return collection.getData();
        } catch (StripeException e) {
            log.error("[listRefundsByRefundId][refundId={}] Stripe API failed", refundId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Event verifyWebhookSignature(String payload, String sigHeader, String webhookSecret) {
        // A-pre-5 安全 fail-fast:webhookSecret 必须非空 — 空 secret 时 Stripe SDK 仍会算 HMAC
        // 但永不匹配(算出来的 sig 跟请求头不一致)→ 所有请求拒绝,但日志含糊;
        // 这里显式校验 + 独立日志,运维能直接看到"未配置"而不是"sig invalid"
        if (cn.hutool.core.util.StrUtil.isBlank(webhookSecret)) {
            log.error("[verifyWebhookSignature] STRIPE_WEBHOOK_SECRET 未配置,拒绝 webhook 请求");
            throw exception(STRIPE_WEBHOOK_SIG_INVALID);
        }
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("[verifyWebhookSignature] sig invalid: {}", e.getMessage());
            throw exception(STRIPE_WEBHOOK_SIG_INVALID);
        } catch (Exception e) {
            log.error("[verifyWebhookSignature] unexpected error", e);
            throw exception(STRIPE_WEBHOOK_SIG_INVALID);
        }
    }
}
