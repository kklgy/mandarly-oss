package com.mandarly.boot.module.edu.service.payment;

import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.app.payment.vo.AppPaymentCheckoutRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.StudentPackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.payment.PaymentMapper;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.enums.payment.PaymentStatusEnum;
import com.mandarly.boot.module.edu.framework.stripe.client.CreateSessionRequest;
import com.mandarly.boot.module.edu.framework.stripe.client.StripeClient;
import com.mandarly.boot.module.edu.framework.stripe.config.StripeProperties;
import com.mandarly.boot.module.edu.service.mail.PaymentMailTemplateHelper;
import com.mandarly.boot.module.edu.service.referral.ReferralService;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import com.mandarly.boot.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.PACKAGE_NOT_FOUND;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.PAYMENT_NOT_FOUND;

/**
 * 支付服务实现(套餐购买 Flow 1)
 *
 * <p>关键设计:
 * <ul>
 *   <li>90s 复用窗口:同 user+package+pending 内不重复创建 payment 记录</li>
 *   <li>推荐码:委托给 ReferralService.bindAndCalculateDiscount(降级语义)</li>
 *   <li>handleCheckoutCompleted:主事务 → commit 后另起 try-catch 调 triggerReward + 邮件</li>
 *   <li>creator 兜底:webhook 上下文手动 set "stripe-webhook"</li>
 * </ul>
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentMapper paymentMapper;

    @Resource
    private PackageMapper packageMapper;

    @Resource
    private StudentPackageMapper studentPackageMapper;

    @Resource
    private StripeClient stripeClient;

    @Resource
    private ReferralService referralService;

    @Resource
    private ConfigApi configApi;

    @Resource
    private StripeProperties stripeProperties;

    @Resource
    private MailSendApi mailSendApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPaymentCheckoutRespVO createCheckout(Long userId, Long packageId, String requestedCurrency, String referralCode) {
        // 1. 校验套餐
        PackageDO pkg = packageMapper.selectById(packageId);
        if (pkg == null || Boolean.FALSE.equals(pkg.getIsActive())) {
            throw exception(PACKAGE_NOT_FOUND);
        }
        String packageCurrency = normalizeCurrency(pkg.getCurrency());
        if (requestedCurrency != null && !requestedCurrency.isBlank()
                && !packageCurrency.equals(normalizeCurrency(requestedCurrency))) {
            throw exception0(400, "所选币种与套餐币种不一致,请刷新套餐后重试");
        }

        // 2. 90s 复用窗口检查
        LocalDateTime since = LocalDateTime.now().minusSeconds(stripeProperties.getPendingReuseWindowSeconds());
        Optional<PaymentDO> existing = paymentMapper.selectRecentPendingByUserAndPackage(userId, packageId, since);
        if (existing.isPresent()) {
            PaymentDO p = existing.get();
            log.info("[createCheckout] reuse pending payment={} for user={} package={}", p.getId(), userId, packageId);
            return AppPaymentCheckoutRespVO.builder()
                    .paymentId(p.getId())
                    .checkoutUrl(buildCheckoutReturnUrl(p))
                    .discountAmountUsd(BigDecimal.ZERO)
                    .build();
        }

        // 3. 推荐码处理(降级语义:自引用会抛,其他降级 0)
        BigDecimal discount = referralService.bindAndCalculateDiscount(userId, referralCode);

        // 4. 计算最终价
        BigDecimal unitAmount = pkg.getPrice().subtract(discount);
        if (unitAmount.compareTo(BigDecimal.ZERO) < 0) {
            unitAmount = BigDecimal.ZERO;
        }

        // 5. INSERT payment(pending)
        PaymentDO payment = new PaymentDO();
        payment.setUserId(userId);
        payment.setPackageId(packageId);
        payment.setChannel("stripe");
        payment.setAmountRequest(unitAmount);
        payment.setCurrencyRequest(packageCurrency);
        payment.setDiscountAmountUsd(discount);
        payment.setStatus(PaymentStatusEnum.PENDING.getCode());
        payment.setSuccessUrl(buildSuccessUrl(null));
        payment.setCancelUrl(buildCancelUrl(null));
        // creator 在 TenantBaseDO 由框架 auto-fill(登录上下文)
        paymentMapper.insert(payment);
        String successUrl = buildSuccessUrl(payment.getId());
        String cancelUrl = buildCancelUrl(payment.getId());
        PaymentDO urlUpdate = new PaymentDO();
        urlUpdate.setId(payment.getId());
        urlUpdate.setSuccessUrl(successUrl);
        urlUpdate.setCancelUrl(cancelUrl);
        paymentMapper.updateById(urlUpdate);
        payment.setSuccessUrl(successUrl);
        payment.setCancelUrl(cancelUrl);
        referralService.attachPayment(userId, payment.getId());

        // 6. 调 Stripe(失败时 payment 保持 pending,异常上抛给前端)
        Session session = stripeClient.createCheckoutSession(CreateSessionRequest.builder()
                .paymentId(payment.getId())
                .userId(userId)
                .packageId(packageId)
                .packageNameI18n(pkg.getNameI18nCode())
                .unitAmount(unitAmount)
                .currency(packageCurrency)
                .successUrl(payment.getSuccessUrl())
                .cancelUrl(payment.getCancelUrl())
                .expiresInMinutes(stripeProperties.getSessionExpiresMinutes())
                .build());

        // 7. 更新 session ID
        PaymentDO update = new PaymentDO();
        update.setId(payment.getId());
        update.setChannelSessionId(session.getId());
        paymentMapper.updateById(update);

        return AppPaymentCheckoutRespVO.builder()
                .paymentId(payment.getId())
                .checkoutUrl(session.getUrl())
                .discountAmountUsd(discount)
                .build();
    }

    @Override
    public PaymentDO getMyPayment(Long userId, Long paymentId) {
        PaymentDO p = paymentMapper.selectById(paymentId);
        if (p == null || !p.getUserId().equals(userId)) {
            throw exception(PAYMENT_NOT_FOUND);
        }
        return p;
    }

    @Override
    public PageResult<PaymentDO> getMyPaymentPage(Long userId, PageParam pageParam) {
        return paymentMapper.selectPage(pageParam,
                new com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX<PaymentDO>()
                        .eq(PaymentDO::getUserId, userId)
                        .orderByDesc(PaymentDO::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCheckoutCompleted(Session session) {
        String sessionId = session.getId();
        Optional<PaymentDO> opt = paymentMapper.selectBySessionId(sessionId);
        if (opt.isEmpty()) {
            log.warn("[handleCheckoutCompleted] payment not found for session={}", sessionId);
            return;
        }
        PaymentDO payment = opt.get();
        if (!PaymentStatusEnum.PENDING.getCode().equals(payment.getStatus())) {
            log.info("[handleCheckoutCompleted] payment={} already in status={}, skip", payment.getId(), payment.getStatus());
            return;
        }
        PackageDO pkg = packageMapper.selectById(payment.getPackageId());

        // A-pre-3 回填 Stripe 关键字段:retrievePaymentIntent expand latest_charge.balance_transaction
        // 拿到 amountReceived(实付,可能含 PromotionCode 优惠)+ balance_transaction.amount(账户结算 USD,扣手续费前)
        BigDecimal amountPaid = null;
        String currencyPaid = null;
        BigDecimal amountSettledUsd = null;
        String paymentIntentId = session.getPaymentIntent();
        String chargeId = null;
        String paymentMethodType = null;
        if (paymentIntentId != null) {
            try {
                PaymentIntent pi = stripeClient.retrievePaymentIntent(paymentIntentId);
                if (pi.getAmountReceived() != null) {
                    // Stripe 用最小货币单位(USD = cents),除 100 转 BigDecimal
                    amountPaid = BigDecimal.valueOf(pi.getAmountReceived()).movePointLeft(2);
                }
                if (pi.getCurrency() != null) {
                    currencyPaid = pi.getCurrency().toUpperCase();
                }
                Charge charge = pi.getLatestChargeObject();
                if (charge != null) {
                    chargeId = charge.getId();
                    if (charge.getPaymentMethodDetails() != null && charge.getPaymentMethodDetails().getType() != null) {
                        paymentMethodType = charge.getPaymentMethodDetails().getType();
                    }
                    if (charge.getBalanceTransactionObject() != null && charge.getBalanceTransactionObject().getAmount() != null) {
                        // balance_transaction.amount 是账户币种(USD),扣 Stripe 手续费**前**金额 — 教师 income 计算基数
                        amountSettledUsd = BigDecimal.valueOf(charge.getBalanceTransactionObject().getAmount()).movePointLeft(2);
                    }
                }
            } catch (Exception e) {
                // Stripe 回填失败不阻断主路径(本地 paid 状态 + student_package 仍要落库,运营可后续手动对账)
                log.error("[handleCheckoutCompleted] retrievePaymentIntent failed paymentId={} pi={}",
                        payment.getId(), paymentIntentId, e);
            }
        }

        // 创建 student_package
        StudentPackageDO sp = new StudentPackageDO();
        sp.setStudentId(payment.getUserId());
        sp.setPackageId(payment.getPackageId());
        sp.setRemaining(pkg != null ? pkg.getTotalCount() : 0);
        sp.setExpireAt(LocalDateTime.now().plusDays(pkg != null ? pkg.getValidityDays() : 30));
        sp.setSource("purchase");
        sp.setPaymentId(payment.getId());
        // webhook 上下文 creator 兜底
        sp.setCreator("stripe-webhook");
        sp.setUpdater("stripe-webhook");
        studentPackageMapper.insert(sp);

        // 更新 payment → paid + 回填 Stripe 5 字段
        PaymentDO upd = new PaymentDO();
        upd.setId(payment.getId());
        upd.setStudentPackageId(sp.getId());
        upd.setStatus(PaymentStatusEnum.PAID.getCode());
        upd.setPaidAt(LocalDateTime.now());
        upd.setChannelPaymentIntentId(paymentIntentId);
        upd.setChannelChargeId(chargeId);
        upd.setPaymentMethodType(paymentMethodType);
        upd.setAmountPaid(amountPaid);
        upd.setCurrencyPaid(currencyPaid);
        upd.setAmountSettledUsd(amountSettledUsd);
        upd.setUpdater("stripe-webhook");
        paymentMapper.updateById(upd);

        Long paymentId = payment.getId();

        // 主事务 commit 后另起 try-catch 触发奖励 + 邮件(失败不回滚)
        try {
            referralService.triggerReward(paymentId);
        } catch (Exception e) {
            log.error("[handleCheckoutCompleted] triggerReward failed, paymentId={}", paymentId, e);
        }
        // TODO M5 短信通道
        // TODO M5 站内信通道

        // Phase 9.2 邮件模板 #1:套餐购买成功通知 — 用 amountPaid 真实值,fallback amountRequest
        BigDecimal mailAmount = amountPaid != null ? amountPaid : payment.getAmountRequest();
        String mailCurrency = currencyPaid != null ? currencyPaid : "USD";
        try {
            String userLocale = getUserLocale(payment.getUserId());
            String templateCode = PaymentMailTemplateHelper.pickTemplateCode("mandarly_payment_success", userLocale);
            Map<String, Object> vars = Map.of(
                    "packageName", pkg != null ? pkg.getNameI18nCode() : "—",
                    "amountPaid",  mailAmount.toPlainString(),
                    "currency",    mailCurrency,
                    PaymentMailTemplateHelper.PARAM_LOCALE, PaymentMailTemplateHelper.normalizeLocale(userLocale)
            );
            String email = getUserEmail(payment.getUserId());
            if (email == null) {
                log.warn("[handleCheckoutCompleted] user email not found, skip mail. paymentId={} userId={}",
                        paymentId, payment.getUserId());
                return;
            }
            MailSendSingleToUserReqDTO req = new MailSendSingleToUserReqDTO();
            req.setToMails(java.util.List.of(email));
            req.setTemplateCode(templateCode);
            req.setTemplateParams(vars);
            mailSendApi.sendSingleMailToMember(req);
        } catch (Exception e) {
            log.error("[handleCheckoutCompleted] mail send failed for payment {}", paymentId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleSessionExpired(String sessionId) {
        paymentMapper.selectBySessionId(sessionId).ifPresent(p -> {
            if (PaymentStatusEnum.PENDING.getCode().equals(p.getStatus())) {
                PaymentDO upd = new PaymentDO();
                upd.setId(p.getId());
                upd.setStatus(PaymentStatusEnum.EXPIRED.getCode());
                upd.setExpiredAt(LocalDateTime.now());
                upd.setUpdater("stripe-webhook");
                paymentMapper.updateById(upd);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleChargeUpdated(Charge charge) {
        String chargeId = charge.getId();
        Optional<PaymentDO> opt = paymentMapper.selectByChargeId(chargeId);
        if (opt.isEmpty()) {
            log.info("[handleChargeUpdated] payment not found for charge={}, skip", chargeId);
            return;
        }
        PaymentDO payment = opt.get();
        if (payment.getAmountSettledUsd() != null) {
            log.info("[handleChargeUpdated] payment={} amountSettledUsd already set, skip", payment.getId());
            return;
        }
        // 二次拉取 charge 拿 balance_transaction(webhook payload 不一定 expand)
        BigDecimal amountSettledUsd;
        try {
            Charge full = stripeClient.retrieveCharge(chargeId);
            if (full.getBalanceTransactionObject() == null
                    || full.getBalanceTransactionObject().getAmount() == null) {
                log.info("[handleChargeUpdated] balance_transaction not ready for charge={}, will retry on next event", chargeId);
                return;
            }
            amountSettledUsd = BigDecimal.valueOf(full.getBalanceTransactionObject().getAmount()).movePointLeft(2);
        } catch (Exception e) {
            log.error("[handleChargeUpdated] retrieveCharge failed chargeId={}", chargeId, e);
            return;
        }
        // B1 sanity check:USD 支付下 settled 应 ≤ paid(Stripe 扣手续费后入账);> paid 通常说明
        // Stripe API 单位或账户币种漂移,不阻断写入,但 log.error 触发告警让运维介入对账。
        if (payment.getAmountPaid() != null
                && "USD".equalsIgnoreCase(payment.getCurrencyPaid())
                && amountSettledUsd.compareTo(payment.getAmountPaid()) > 0) {
            log.error("[handleChargeUpdated] anomaly: settled={} > paid={} for payment={} charge={} — possible Stripe unit/currency mismatch, please investigate",
                    amountSettledUsd, payment.getAmountPaid(), payment.getId(), chargeId);
        }
        PaymentDO upd = new PaymentDO();
        upd.setId(payment.getId());
        upd.setAmountSettledUsd(amountSettledUsd);
        upd.setUpdater("stripe-webhook");
        paymentMapper.updateById(upd);
        log.info("[handleChargeUpdated] payment={} amountSettledUsd backfilled to {}", payment.getId(), amountSettledUsd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentFailed(String paymentIntentId) {
        paymentMapper.selectByPaymentIntentId(paymentIntentId).ifPresent(p -> {
            if (PaymentStatusEnum.PENDING.getCode().equals(p.getStatus())) {
                PaymentDO upd = new PaymentDO();
                upd.setId(p.getId());
                upd.setStatus(PaymentStatusEnum.FAILED.getCode());
                upd.setUpdater("stripe-webhook");
                paymentMapper.updateById(upd);
            }
        });
    }

    // ======================== private helpers ========================

    /**
     * 反射取用户 locale(沿用 M2.5 反射模式,避免循环依赖)。
     * 失败时降级 "en"。
     */
    private String getUserLocale(Long userId) {
        Object user = reflectGetUser(userId);
        if (user == null) return "en";
        try {
            String locale = (String) user.getClass().getMethod("getLocale").invoke(user);
            return locale != null ? locale : "en";
        } catch (Exception e) {
            log.warn("[getUserLocale] reflection fail for userId={}, fallback to en", userId, e);
            return "en";
        }
    }

    /**
     * 反射取用户 email — Mandarly 删除了 ruoyi 默认 member 模块,MailSendApi 默认走
     * MemberUserApi 反查 email 时报 ClassNotFoundException;改用本地反射查 user.email
     * 后直接 setToMails 绕过 member 反查。
     */
    private String getUserEmail(Long userId) {
        Object user = reflectGetUser(userId);
        if (user == null) return null;
        try {
            return (String) user.getClass().getMethod("getEmail").invoke(user);
        } catch (Exception e) {
            log.warn("[getUserEmail] reflection fail for userId={}", userId, e);
            return null;
        }
    }

    private Object reflectGetUser(Long userId) {
        try {
            // Mandarly app 用户走 UserService(查 user 表 → AppUserDO),
            // 不是 ruoyi 默认的 AdminUserService(查 system_users 表 → AdminUserDO)
            Class<?> svc = Class.forName("com.mandarly.boot.module.system.service.user.UserService");
            Object bean = cn.hutool.extra.spring.SpringUtil.getBean(svc);
            return svc.getMethod("getById", Long.class).invoke(bean, userId);
        } catch (Exception e) {
            log.warn("[reflectGetUser] fail userId={}", userId, e);
            return null;
        }
    }

    private String buildSuccessUrl(Long paymentId) {
        String url = stripeProperties.getAppBaseUrl() + "/payment/success";
        return paymentId == null ? url : url + "?paymentId=" + paymentId;
    }

    private String buildCancelUrl(Long paymentId) {
        String url = stripeProperties.getAppBaseUrl() + "/payment/cancel";
        return paymentId == null ? url : url + "?paymentId=" + paymentId;
    }

    private String buildCheckoutReturnUrl(PaymentDO p) {
        // 复用场景:返回 success page URL(前端可轮询支付状态)
        return stripeProperties.getAppBaseUrl() + "/payment/pending?paymentId=" + p.getId();
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "HKD";
        }
        return currency.trim().toUpperCase();
    }
}
