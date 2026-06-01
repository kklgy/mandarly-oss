package com.mandarly.boot.module.edu.service.payment;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.controller.app.payment.vo.AppPaymentCheckoutRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.StudentPackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.payment.PaymentMapper;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.framework.stripe.client.CreateSessionRequest;
import com.mandarly.boot.module.edu.framework.stripe.client.StripeClient;
import com.mandarly.boot.module.edu.framework.stripe.config.StripeProperties;
import com.mandarly.boot.module.edu.service.referral.ReferralService;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentServiceImpl TDD 单元测试(12 case)
 *
 * <p>覆盖:
 * <ol>
 *   <li>首单无推荐码 → 正常创建 session</li>
 *   <li>有效推荐码 → 应用 discount</li>
 *   <li>自引用 → 抛 REFERRAL_CODE_SELF_USE</li>
 *   <li>已绑过 → skipDiscount 但仍创建 session</li>
 *   <li>套餐下架 → 抛 PACKAGE_NOT_FOUND</li>
 *   <li>90s 复用 pending session</li>
 *   <li>Stripe API 失败 → payment 保持 pending</li>
 *   <li>getMyPayment 越权 → 抛 PAYMENT_NOT_FOUND</li>
 *   <li>handleChargeUpdated:payment 不存在 → skip,不调 updateById</li>
 *   <li>handleChargeUpdated:amountSettledUsd 已有值 → 幂等 skip</li>
 *   <li>handleChargeUpdated:balance_transaction 未 ready → skip(等下次事件)</li>
 *   <li>handleChargeUpdated:balance_transaction ready → 回填 amountSettledUsd</li>
 * </ol>
 */
class PaymentServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private StripeClient stripeClient;

    @Mock
    private ReferralService referralService;

    @Mock
    private PackageMapper packageMapper;

    @Mock
    private StudentPackageMapper studentPackageMapper;

    @Mock
    private ConfigApi configApi;

    @Mock
    private StripeProperties stripeProperties;

    @Mock
    private MailSendApi mailSendApi;  // Phase 9.2:邮件接入 mock(InjectMocks 自动注入)

    // ======================== helpers ========================

    private PackageDO activePackage() {
        PackageDO p = new PackageDO();
        p.setId(10L);
        p.setNameI18nCode("pkg.name.4lessons");
        p.setPrice(new BigDecimal("199.00"));
        p.setCurrency("usd");
        p.setTotalCount(4);
        p.setValidityDays(30);
        p.setIsActive(true);
        p.setIsFreeTrial(false);
        return p;
    }

    private Session stubSession(String id, String url) {
        Session s = new Session();
        s.setId(id);
        s.setUrl(url);
        return s;
    }

    private void mockPaymentInsertId(Long id) {
        when(paymentMapper.insert(any(PaymentDO.class))).thenAnswer(inv -> {
            PaymentDO p = inv.getArgument(0);
            p.setId(id);
            return 1;
        });
    }

    // ======================== tests ========================

    /** case 1: 首单 + 无推荐码 → 正常创建 session,discount=0 */
    @Test
    void createCheckout_firstTime_noReferralCode_works() {
        // given
        Long userId = 1L;
        Long packageId = 10L;
        PackageDO pkg = activePackage();

        when(packageMapper.selectById(packageId)).thenReturn(pkg);
        when(stripeProperties.getPendingReuseWindowSeconds()).thenReturn(90);
        when(paymentMapper.selectRecentPendingByUserAndPackage(eq(userId), eq(packageId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(referralService.bindAndCalculateDiscount(userId, null)).thenReturn(BigDecimal.ZERO);
        mockPaymentInsertId(1001L);

        Session session = stubSession("cs_test_001", "https://checkout.stripe.com/001");
        when(stripeClient.createCheckoutSession(any(CreateSessionRequest.class))).thenReturn(session);

        when(stripeProperties.getAppBaseUrl()).thenReturn("http://localhost:3001");
        when(stripeProperties.getSessionExpiresMinutes()).thenReturn(30);

        // when
        AppPaymentCheckoutRespVO resp = paymentService.createCheckout(userId, packageId, null, null);

        // then
        assertThat(resp.getCheckoutUrl()).isEqualTo("https://checkout.stripe.com/001");
        assertThat(resp.getDiscountAmountUsd()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(paymentMapper).insert(argThat((PaymentDO p) ->
                p.getSuccessUrl().startsWith("http://localhost:3001/payment/success")
                        && !p.getSuccessUrl().contains("userId=")
        ));
        verify(stripeClient).createCheckoutSession(argThat(req ->
                "http://localhost:3001/payment/success?paymentId=1001".equals(req.getSuccessUrl())
        ));
    }

    /** regression:HKD 套餐必须以 HKD 创建 payment 和 Stripe Session,不能被当成 USD */
    @Test
    void createCheckout_hkdPackage_usesPackageCurrency() {
        // given
        Long userId = 11L;
        Long packageId = 10L;
        PackageDO pkg = activePackage();
        pkg.setPrice(new BigDecimal("60.00"));
        pkg.setCurrency("HKD");

        when(packageMapper.selectById(packageId)).thenReturn(pkg);
        when(stripeProperties.getPendingReuseWindowSeconds()).thenReturn(90);
        when(paymentMapper.selectRecentPendingByUserAndPackage(eq(userId), eq(packageId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(referralService.bindAndCalculateDiscount(userId, null)).thenReturn(BigDecimal.ZERO);
        mockPaymentInsertId(1100L);
        when(stripeProperties.getAppBaseUrl()).thenReturn("http://localhost:3001");
        when(stripeProperties.getSessionExpiresMinutes()).thenReturn(30);

        Session session = stubSession("cs_test_hkd", "https://checkout.stripe.com/hkd");
        when(stripeClient.createCheckoutSession(any(CreateSessionRequest.class))).thenReturn(session);

        // when
        paymentService.createCheckout(userId, packageId, "HKD", null);

        // then
        verify(paymentMapper).insert(argThat((PaymentDO p) ->
                p.getAmountRequest().compareTo(new BigDecimal("60.00")) == 0
                        && "HKD".equals(p.getCurrencyRequest())
        ));
        verify(stripeClient).createCheckoutSession(argThat(req ->
                req.getUnitAmount().compareTo(new BigDecimal("60.00")) == 0
                        && "HKD".equals(req.getCurrency())
        ));
    }

    /** case 2: 有效推荐码 → 含 discount */
    @Test
    void createCheckout_validReferralCode_appliesDiscount() {
        // given
        Long userId = 2L;
        Long packageId = 10L;
        BigDecimal discount = new BigDecimal("20.00");
        PackageDO pkg = activePackage();

        when(packageMapper.selectById(packageId)).thenReturn(pkg);
        when(stripeProperties.getPendingReuseWindowSeconds()).thenReturn(90);
        when(paymentMapper.selectRecentPendingByUserAndPackage(eq(userId), eq(packageId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(referralService.bindAndCalculateDiscount(userId, "ABCD1234")).thenReturn(discount);
        mockPaymentInsertId(1002L);

        Session session = stubSession("cs_test_002", "https://checkout.stripe.com/002");
        when(stripeClient.createCheckoutSession(any(CreateSessionRequest.class))).thenReturn(session);
        when(stripeProperties.getAppBaseUrl()).thenReturn("http://localhost:3001");
        when(stripeProperties.getSessionExpiresMinutes()).thenReturn(30);

        // when
        AppPaymentCheckoutRespVO resp = paymentService.createCheckout(userId, packageId, null, "ABCD1234");

        // then
        assertThat(resp.getDiscountAmountUsd()).isEqualByComparingTo(discount);
        // 验证 StripeClient 收到的 unitAmount = price - discount
        verify(stripeClient).createCheckoutSession(argThat(req ->
                req.getUnitAmount().compareTo(pkg.getPrice().subtract(discount)) == 0
        ));
    }

    /** case 3: 自引用 → 抛 REFERRAL_CODE_SELF_USE(1_004_002) */
    @Test
    void createCheckout_selfReferral_throws_REFERRAL_CODE_SELF_USE() {
        // given
        Long userId = 3L;
        Long packageId = 10L;
        PackageDO pkg = activePackage();

        when(packageMapper.selectById(packageId)).thenReturn(pkg);
        when(paymentMapper.selectRecentPendingByUserAndPackage(eq(userId), eq(packageId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(referralService.bindAndCalculateDiscount(userId, "SELF9999"))
                .thenThrow(new ServiceException(1_004_002, "推荐码不能引用自己"));
        // getPendingReuseWindowSeconds 在 selectRecentPendingByUserAndPackage 前被调用
        when(stripeProperties.getPendingReuseWindowSeconds()).thenReturn(90);

        // when & then
        assertThatThrownBy(() -> paymentService.createCheckout(userId, packageId, null, "SELF9999"))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode()).isEqualTo(1_004_002));
    }

    /** case 4: 已绑过推荐码 → ReferralService 返回 0,仍正常创建 session */
    @Test
    void createCheckout_alreadyBound_skipDiscount_butStillCreateSession() {
        // given
        Long userId = 4L;
        Long packageId = 10L;
        PackageDO pkg = activePackage();

        when(packageMapper.selectById(packageId)).thenReturn(pkg);
        when(stripeProperties.getPendingReuseWindowSeconds()).thenReturn(90);
        when(paymentMapper.selectRecentPendingByUserAndPackage(eq(userId), eq(packageId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        // 已绑过 → 降级返回 0
        when(referralService.bindAndCalculateDiscount(userId, "OLDCODE")).thenReturn(BigDecimal.ZERO);
        mockPaymentInsertId(1004L);

        Session session = stubSession("cs_test_004", "https://checkout.stripe.com/004");
        when(stripeClient.createCheckoutSession(any(CreateSessionRequest.class))).thenReturn(session);
        when(stripeProperties.getAppBaseUrl()).thenReturn("http://localhost:3001");
        when(stripeProperties.getSessionExpiresMinutes()).thenReturn(30);

        // when
        AppPaymentCheckoutRespVO resp = paymentService.createCheckout(userId, packageId, null, "OLDCODE");

        // then: discount=0, session 仍创建
        assertThat(resp.getDiscountAmountUsd()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resp.getCheckoutUrl()).isNotBlank();
        verify(stripeClient).createCheckoutSession(any());
    }

    /** case 5: 套餐下架 → 抛 PACKAGE_NOT_FOUND(1_004_010) */
    @Test
    void createCheckout_packageOffShelf_throws_PACKAGE_NOT_FOUND() {
        // given: 套餐 isActive=false
        Long userId = 5L;
        Long packageId = 99L;
        PackageDO pkg = activePackage();
        pkg.setIsActive(false);

        when(packageMapper.selectById(packageId)).thenReturn(pkg);

        // when & then
        assertThatThrownBy(() -> paymentService.createCheckout(userId, packageId, null, null))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode()).isEqualTo(1_004_010));
    }

    /** case 6: 90s 内已有 pending session → 复用,不再创建新记录 */
    @Test
    void createCheckout_duplicatePending_within90s_reusesSession() {
        // given
        Long userId = 6L;
        Long packageId = 10L;
        PackageDO pkg = activePackage();
        when(packageMapper.selectById(packageId)).thenReturn(pkg);

        PaymentDO existing = new PaymentDO();
        existing.setId(888L);
        existing.setChannelSessionId("cs_existing");
        // 注意: session url 在 payment DO 里可能不存,这里 checkout url 来自 session
        // 实现中会重新生成 checkout url 或直接返回 pending payment id
        when(paymentMapper.selectRecentPendingByUserAndPackage(eq(userId), eq(packageId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existing));
        when(stripeProperties.getPendingReuseWindowSeconds()).thenReturn(90);
        when(stripeProperties.getAppBaseUrl()).thenReturn("http://localhost:3001");
        // getSessionExpiresMinutes 在复用早返回路径不会被调用,不 stub

        // when
        AppPaymentCheckoutRespVO resp = paymentService.createCheckout(userId, packageId, null, null);

        // then: 复用了旧 payment
        assertThat(resp.getPaymentId()).isEqualTo(888L);
        // 不再调 Stripe 创建新 session
        verify(stripeClient, never()).createCheckoutSession(any());
    }

    /** case 7: Stripe API 失败 → payment 保持 pending(不回滚,仅 log) */
    @Test
    void createCheckout_stripeApiFails_paymentRemainsPending() {
        // given
        Long userId = 7L;
        Long packageId = 10L;
        PackageDO pkg = activePackage();

        when(packageMapper.selectById(packageId)).thenReturn(pkg);
        when(stripeProperties.getPendingReuseWindowSeconds()).thenReturn(90);
        when(paymentMapper.selectRecentPendingByUserAndPackage(eq(userId), eq(packageId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(referralService.bindAndCalculateDiscount(userId, null)).thenReturn(BigDecimal.ZERO);
        mockPaymentInsertId(1007L);
        when(stripeProperties.getAppBaseUrl()).thenReturn("http://localhost:3001");
        when(stripeProperties.getSessionExpiresMinutes()).thenReturn(30);

        // Stripe 抛错 → ServiceException STRIPE_CHANNEL_ERROR
        when(stripeClient.createCheckoutSession(any()))
                .thenThrow(new ServiceException(1_004_400, "支付通道异常,请稍后重试"));

        // when & then: PaymentService 应将 Stripe 异常向上抛
        assertThatThrownBy(() -> paymentService.createCheckout(userId, packageId, null, null))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode()).isEqualTo(1_004_400));

        // payment record 已 insert(pending),Stripe 失败不回滚
        verify(paymentMapper).insert(any(PaymentDO.class));
    }

    /** case 8: getMyPayment 越权 → 抛 PAYMENT_NOT_FOUND(1_004_020) */
    @Test
    void getMyPayment_otherUser_throws_PAYMENT_NOT_FOUND() {
        // given: payment 属于 userId=1,但查询 userId=99
        Long paymentId = 1000L;
        PaymentDO p = new PaymentDO();
        p.setId(paymentId);
        p.setUserId(1L);

        when(paymentMapper.selectById(paymentId)).thenReturn(p);

        // when & then
        assertThatThrownBy(() -> paymentService.getMyPayment(99L, paymentId))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode()).isEqualTo(1_004_020));
    }

    // ======================== case 9-12: handleChargeUpdated ========================

    /** case 9: payment 不存在 → skip,不调 stripeClient/updateById */
    @Test
    void handleChargeUpdated_paymentNotFound_skip() {
        com.stripe.model.Charge charge = new com.stripe.model.Charge();
        charge.setId("ch_unknown");

        when(paymentMapper.selectByChargeId("ch_unknown")).thenReturn(Optional.empty());

        paymentService.handleChargeUpdated(charge);

        verify(stripeClient, never()).retrieveCharge(anyString());
        verify(paymentMapper, never()).updateById(any(PaymentDO.class));
    }

    /** case 10: amountSettledUsd 已有值 → 幂等 skip,不调 stripeClient/updateById */
    @Test
    void handleChargeUpdated_alreadySettled_skip() {
        com.stripe.model.Charge charge = new com.stripe.model.Charge();
        charge.setId("ch_settled");

        PaymentDO p = new PaymentDO();
        p.setId(1L);
        p.setAmountSettledUsd(new BigDecimal("58.50"));
        when(paymentMapper.selectByChargeId("ch_settled")).thenReturn(Optional.of(p));

        paymentService.handleChargeUpdated(charge);

        verify(stripeClient, never()).retrieveCharge(anyString());
        verify(paymentMapper, never()).updateById(any(PaymentDO.class));
    }

    /** case 11: balance_transaction 未 ready(null amount)→ skip,不 updateById */
    @Test
    void handleChargeUpdated_balanceTxNotReady_skip() {
        com.stripe.model.Charge eventCharge = new com.stripe.model.Charge();
        eventCharge.setId("ch_pending_bt");

        PaymentDO p = new PaymentDO();
        p.setId(2L);
        p.setAmountSettledUsd(null);
        when(paymentMapper.selectByChargeId("ch_pending_bt")).thenReturn(Optional.of(p));

        // retrieve 返回的 Charge balance_transaction 为 null
        com.stripe.model.Charge full = new com.stripe.model.Charge();
        full.setId("ch_pending_bt");
        // balanceTransactionObject 默认 null
        when(stripeClient.retrieveCharge("ch_pending_bt")).thenReturn(full);

        paymentService.handleChargeUpdated(eventCharge);

        verify(paymentMapper, never()).updateById(any(PaymentDO.class));
    }

    /** case 12: balance_transaction ready → 回填 amountSettledUsd(cents → BigDecimal /100) */
    @Test
    void handleChargeUpdated_balanceTxReady_backfill() {
        com.stripe.model.Charge eventCharge = new com.stripe.model.Charge();
        eventCharge.setId("ch_ready");

        PaymentDO p = new PaymentDO();
        p.setId(3L);
        p.setAmountSettledUsd(null);
        when(paymentMapper.selectByChargeId("ch_ready")).thenReturn(Optional.of(p));

        com.stripe.model.Charge full = new com.stripe.model.Charge();
        full.setId("ch_ready");
        com.stripe.model.BalanceTransaction bt = new com.stripe.model.BalanceTransaction();
        bt.setId("txn_test");
        bt.setAmount(5836L); // 58.36 USD,cents
        full.setBalanceTransactionObject(bt);
        when(stripeClient.retrieveCharge("ch_ready")).thenReturn(full);

        paymentService.handleChargeUpdated(eventCharge);

        org.mockito.ArgumentCaptor<PaymentDO> captor = org.mockito.ArgumentCaptor.forClass(PaymentDO.class);
        verify(paymentMapper).updateById(captor.capture());
        PaymentDO upd = captor.getValue();
        assertThat(upd.getId()).isEqualTo(3L);
        assertThat(upd.getAmountSettledUsd()).isEqualByComparingTo(new BigDecimal("58.36"));
        assertThat(upd.getUpdater()).isEqualTo("stripe-webhook");
    }

    /**
     * B1 防御:settled > paid(USD 支付场景)→ 仍写入 + 走 sanity 路径(不阻断)
     *
     * <p>用例:amountPaid=8.00 USD 但 balance_transaction.amount=10000(100 USD)— 异常,
     * 通常说明 Stripe API 单位或账户币种漂移。期望:仍 updateById(避免阻断 settle),
     * 但 amountSettledUsd 字段被写入实际解析值(便于人工对账)。
     */
    @Test
    void handleChargeUpdated_settledExceedsPaid_stillWritesButFlagged() {
        com.stripe.model.Charge eventCharge = new com.stripe.model.Charge();
        eventCharge.setId("ch_anomaly");

        PaymentDO p = new PaymentDO();
        p.setId(4L);
        p.setAmountSettledUsd(null);
        p.setAmountPaid(new BigDecimal("8.00"));
        p.setCurrencyPaid("USD");
        when(paymentMapper.selectByChargeId("ch_anomaly")).thenReturn(Optional.of(p));

        com.stripe.model.Charge full = new com.stripe.model.Charge();
        full.setId("ch_anomaly");
        com.stripe.model.BalanceTransaction bt = new com.stripe.model.BalanceTransaction();
        bt.setId("txn_anomaly");
        bt.setAmount(10000L); // 100.00 — 远大于 paid 8.00
        full.setBalanceTransactionObject(bt);
        when(stripeClient.retrieveCharge("ch_anomaly")).thenReturn(full);

        paymentService.handleChargeUpdated(eventCharge);

        // 仍写入(不阻断 settle)
        org.mockito.ArgumentCaptor<PaymentDO> captor = org.mockito.ArgumentCaptor.forClass(PaymentDO.class);
        verify(paymentMapper).updateById(captor.capture());
        assertThat(captor.getValue().getAmountSettledUsd()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
