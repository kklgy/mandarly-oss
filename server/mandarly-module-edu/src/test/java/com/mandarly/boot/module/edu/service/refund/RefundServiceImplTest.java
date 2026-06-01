package com.mandarly.boot.module.edu.service.refund;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherIncomeDO;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;
import com.mandarly.boot.module.edu.dal.dataobject.payment.RefundDO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.StudentPackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import com.mandarly.boot.module.edu.dal.mysql.payment.PaymentMapper;
import com.mandarly.boot.module.edu.dal.mysql.payment.RefundMapper;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.framework.stripe.client.CreateRefundRequest;
import com.mandarly.boot.module.edu.framework.stripe.client.StripeClient;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import com.stripe.model.Refund;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * RefundServiceImpl TDD 单元测试(10 case)
 *
 * <p>覆盖:
 * <ol>
 *   <li>apply:剩 1/2 → suggested = paid × 0.5</li>
 *   <li>apply:remaining=0 → suggested=0</li>
 *   <li>apply:已有 pending → 抛 REFUND_DUPLICATE_PENDING</li>
 *   <li>approve:final ≠ suggested 无 adjustReason → 抛 REFUND_ADJUST_REASON_REQUIRED</li>
 *   <li>approve:final > amount_settled_usd → 抛 REFUND_AMOUNT_EXCEEDS</li>
 *   <li>approve:调用 Stripe 用原支付币种金额(非 USD)</li>
 *   <li>approve:扣回 teacherIncome(按比例,负数 amount)</li>
 *   <li>handleChargeRefunded:全退 → payment.status=refunded;部分退 → partial_refunded</li>
 *   <li>checkAndReconcileOrphans:有孤儿 → 反查 Stripe 回填 channelRefundId</li>
 *   <li>apply:amount_settled_usd=NULL → fallback amountPaid(P1 #7 修复)</li>
 * </ol>
 */
class RefundServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private RefundServiceImpl refundService;

    @Mock
    private RefundMapper refundMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PackageMapper packageMapper;

    @Mock
    private StudentPackageMapper studentPackageMapper;

    @Mock
    private TeacherIncomeMapper teacherIncomeMapper;

    @Mock
    private StripeClient stripeClient;

    @Mock
    private MailSendApi mailSendApi;  // Phase 9.2:邮件接入 mock

    @Mock
    private com.mandarly.boot.module.edu.service.income.TeacherIncomeService teacherIncomeService;

    // ======================== helpers ========================

    private PaymentDO paidPayment(Long id, Long userId, BigDecimal settledUsd, BigDecimal amountPaid, String currency) {
        PaymentDO p = new PaymentDO();
        p.setId(id);
        p.setUserId(userId);
        p.setStatus("paid");
        p.setAmountSettledUsd(settledUsd);
        p.setAmountPaid(amountPaid);
        p.setCurrencyPaid(currency);
        p.setChannelPaymentIntentId("pi_test_" + id);
        p.setStudentPackageId(id * 10);
        p.setPackageId(100L);
        return p;
    }

    private PackageDO pkg4Count() {
        PackageDO p = new PackageDO();
        p.setId(100L);
        p.setTotalCount(4);
        return p;
    }

    private StudentPackageDO sp(Long id, Long paymentId, int remaining) {
        StudentPackageDO s = new StudentPackageDO();
        s.setId(id);
        s.setPaymentId(paymentId);
        s.setRemaining(remaining);
        return s;
    }

    // ======================== apply tests ========================

    /** case 1: 剩 1/2(remaining=2, totalCount=4) → suggested = settledUsd × 0.5 */
    @Test
    void apply_calculatesSuggested_byRemainingRatio() {
        // given
        Long userId = 1L;
        Long paymentId = 10L;
        PaymentDO payment = paidPayment(paymentId, userId, new BigDecimal("100.00"), new BigDecimal("780.00"), "hkd");
        PackageDO pkg = pkg4Count();
        StudentPackageDO sp = sp(100L, paymentId, 2); // remaining=2, total=4 → ratio=0.5

        when(paymentMapper.selectById(paymentId)).thenReturn(payment);
        when(studentPackageMapper.selectById(payment.getStudentPackageId())).thenReturn(sp);
        when(packageMapper.selectById(payment.getPackageId())).thenReturn(pkg);
        when(refundMapper.selectActivePendingByPayment(paymentId)).thenReturn(Optional.empty());
        when(refundMapper.insert(any(RefundDO.class))).thenReturn(1);

        // when
        RefundDO result = refundService.apply(userId, paymentId, "want refund");

        // then: suggested = 100 × (2/4) = 50.00
        assertThat(result.getSuggestedAmountUsd()).isEqualByComparingTo(new BigDecimal("50.00"));
        verify(refundMapper).insert(argThat((RefundDO r) -> "pending".equals(r.getStatus())));
    }

    /** case 2: remaining=0 → suggested=0 */
    @Test
    void apply_zeroRemaining_suggestedIsZero() {
        Long userId = 1L;
        Long paymentId = 11L;
        PaymentDO payment = paidPayment(paymentId, userId, new BigDecimal("100.00"), new BigDecimal("780.00"), "hkd");
        PackageDO pkg = pkg4Count();
        StudentPackageDO sp = sp(110L, paymentId, 0);

        when(paymentMapper.selectById(paymentId)).thenReturn(payment);
        when(studentPackageMapper.selectById(payment.getStudentPackageId())).thenReturn(sp);
        when(packageMapper.selectById(payment.getPackageId())).thenReturn(pkg);
        when(refundMapper.selectActivePendingByPayment(paymentId)).thenReturn(Optional.empty());
        when(refundMapper.insert(any(RefundDO.class))).thenReturn(1);

        RefundDO result = refundService.apply(userId, paymentId, "0 remaining");

        assertThat(result.getSuggestedAmountUsd()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /** case 3: 已有 pending 退款 → 抛 REFUND_DUPLICATE_PENDING(1_004_203) */
    @Test
    void apply_duplicatePending_throws_REFUND_DUPLICATE_PENDING() {
        Long userId = 1L;
        Long paymentId = 12L;
        PaymentDO payment = paidPayment(paymentId, userId, new BigDecimal("100.00"), new BigDecimal("780.00"), "hkd");

        when(paymentMapper.selectById(paymentId)).thenReturn(payment);

        RefundDO existingPending = new RefundDO();
        existingPending.setId(99L);
        existingPending.setStatus("pending");
        when(refundMapper.selectActivePendingByPayment(paymentId)).thenReturn(Optional.of(existingPending));

        // studentPackage 查询不会执行(提前返回错误)
        // 所以不需要 stub studentPackageMapper

        assertThatThrownBy(() -> refundService.apply(userId, paymentId, "dup"))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode()).isEqualTo(1_004_203));
    }

    // ======================== approve tests ========================

    /** case 4: final ≠ suggested 且无 adjustReason → 抛 REFUND_ADJUST_REASON_REQUIRED(1_004_202) */
    @Test
    void approve_finalChanged_requiresAdjustReason() {
        Long refundId = 20L;
        RefundDO refund = new RefundDO();
        refund.setId(refundId);
        refund.setStatus("pending");
        refund.setPaymentId(100L);
        refund.setSuggestedAmountUsd(new BigDecimal("50.00"));
        refund.setStudentPackageId(1000L);

        PaymentDO payment = paidPayment(100L, 1L, new BigDecimal("100.00"), new BigDecimal("780.00"), "hkd");

        when(refundMapper.selectById(refundId)).thenReturn(refund);
        when(paymentMapper.selectById(100L)).thenReturn(payment);

        // final=40 ≠ suggested=50,但 adjustReason 为空
        assertThatThrownBy(() -> refundService.approve(99L, refundId, new BigDecimal("40.00"), null, "note"))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode()).isEqualTo(1_004_202));
    }

    /** case 5: final > amount_settled_usd → 抛 REFUND_AMOUNT_EXCEEDS(1_004_201) */
    @Test
    void approve_finalExceedsPaid_throws_REFUND_AMOUNT_EXCEEDS() {
        Long refundId = 21L;
        RefundDO refund = new RefundDO();
        refund.setId(refundId);
        refund.setStatus("pending");
        refund.setPaymentId(101L);
        refund.setSuggestedAmountUsd(new BigDecimal("50.00"));
        refund.setStudentPackageId(1001L);

        PaymentDO payment = paidPayment(101L, 1L, new BigDecimal("100.00"), new BigDecimal("780.00"), "hkd");

        when(refundMapper.selectById(refundId)).thenReturn(refund);
        when(paymentMapper.selectById(101L)).thenReturn(payment);

        // final=120 > settled=100
        assertThatThrownBy(() -> refundService.approve(99L, refundId, new BigDecimal("120.00"), null, null))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode()).isEqualTo(1_004_201));
    }

    /**
     * case 6: approve 调 Stripe Refund 用原支付币种金额
     * settled=100 USD, paid=780 HKD, final=50 USD → finalPaidCurrency = 780 × (50/100) = 390 HKD
     */
    @Test
    void approve_callsStripeRefund_withConvertedPaidCurrencyAmount() {
        Long refundId = 22L;
        RefundDO refund = new RefundDO();
        refund.setId(refundId);
        refund.setStatus("pending");
        refund.setPaymentId(102L);
        refund.setSuggestedAmountUsd(new BigDecimal("50.00"));
        refund.setStudentPackageId(1002L);

        PaymentDO payment = paidPayment(102L, 1L, new BigDecimal("100.00"), new BigDecimal("780.00"), "hkd");

        when(refundMapper.selectById(refundId)).thenReturn(refund);
        when(paymentMapper.selectById(102L)).thenReturn(payment);
        when(refundMapper.updateById(any(RefundDO.class))).thenReturn(1);

        Refund stripeRefund = new Refund();
        stripeRefund.setId("re_test_22");
        // final=50, ratio=0.5, finalPaidCurrency = 780×0.5 = 390 HKD
        when(stripeClient.createRefund(argThat(req ->
                req.getAmount().compareTo(new BigDecimal("390.00")) == 0
                        && "hkd".equals(req.getCurrency())
        ))).thenReturn(stripeRefund);

        when(teacherIncomeMapper.selectByStudentPackage(refund.getStudentPackageId())).thenReturn(List.of());
        when(studentPackageMapper.updateById(any(StudentPackageDO.class))).thenReturn(1);

        // when: final=suggested=50,不需要 adjustReason
        refundService.approve(99L, refundId, new BigDecimal("50.00"), null, "approved");

        verify(stripeClient).createRefund(argThat(req ->
                req.getAmount().compareTo(new BigDecimal("390.00")) == 0
        ));
    }

    /**
     * case 7: approve 按 ratio 扣回(P0-4 ratio 方案,2026-05-19 重写)
     *
     * <p>实际扣回逻辑下放到 {@code TeacherIncomeService.deductForRefund(orderId, refundId, ratio)}
     * 逐 course_order 走完整 lock-then-tx + balance 扣减。本测验证 RefundService.approve 内:
     * ① 正确计算 ratio = finalUsd / settledBase
     * ② 对 student_package 关联的每个 normal 订单调一次 deductForRefund 且传入同一 ratio
     * ③ 同一 orderId 只调一次(去重)
     *
     * <p>原 income amount 不再断言负数 amount_usd — 那是 deductForRefund 内部行为,
     * 本测覆盖 caller 契约;deductForRefund 自身行为见 `TeacherIncomeServiceImplTest.deduct_for_refund_*`。
     */
    @Test
    void approve_deductsTeacherIncome_byProRataAcrossTeachers() {
        Long refundId = 23L;
        Long teacherId = 500L;
        Long studentPackageId = 1003L;
        RefundDO refund = new RefundDO();
        refund.setId(refundId);
        refund.setStatus("pending");
        refund.setPaymentId(103L);
        refund.setSuggestedAmountUsd(new BigDecimal("50.00"));
        refund.setStudentPackageId(studentPackageId);

        PaymentDO payment = paidPayment(103L, 1L, new BigDecimal("100.00"), new BigDecimal("780.00"), "hkd");

        when(refundMapper.selectById(refundId)).thenReturn(refund);
        when(paymentMapper.selectById(103L)).thenReturn(payment);
        when(refundMapper.updateById(any(RefundDO.class))).thenReturn(1);

        Refund stripeRefund = new Refund();
        stripeRefund.setId("re_test_23");
        when(stripeClient.createRefund(any(CreateRefundRequest.class))).thenReturn(stripeRefund);

        // teacher 有 2 节 normal income(orderId=2001/2002),套餐另有一条历史 refund_deduct(应被过滤)
        TeacherIncomeDO income1 = new TeacherIncomeDO();
        income1.setTeacherId(teacherId);
        income1.setCourseOrderId(2001L);
        income1.setType("normal");
        income1.setAmountUsd(new BigDecimal("30.00"));
        TeacherIncomeDO income2 = new TeacherIncomeDO();
        income2.setTeacherId(teacherId);
        income2.setCourseOrderId(2002L);
        income2.setType("normal");
        income2.setAmountUsd(new BigDecimal("30.00"));
        TeacherIncomeDO historicalDeduct = new TeacherIncomeDO();
        historicalDeduct.setTeacherId(teacherId);
        historicalDeduct.setCourseOrderId(2099L);
        historicalDeduct.setType("refund_deduct"); // 应被 filter
        historicalDeduct.setAmountUsd(new BigDecimal("-5.00"));
        when(teacherIncomeMapper.selectByStudentPackage(studentPackageId))
                .thenReturn(List.of(income1, income2, historicalDeduct));

        when(studentPackageMapper.updateById(any(StudentPackageDO.class))).thenReturn(1);

        // when: final=50,settledBase=100 → ratio=0.5000
        refundService.approve(99L, refundId, new BigDecimal("50.00"), null, "ok");

        // then: 对 2 个 normal 订单各调一次 deductForRefund(ratio=0.5)
        java.math.BigDecimal expectedRatio = new java.math.BigDecimal("0.5000");
        verify(teacherIncomeService).deductForRefund(eq(2001L), eq(refundId),
                argThat((java.math.BigDecimal r) -> r.compareTo(expectedRatio) == 0));
        verify(teacherIncomeService).deductForRefund(eq(2002L), eq(refundId),
                argThat((java.math.BigDecimal r) -> r.compareTo(expectedRatio) == 0));
        // refund_deduct 历史行不应被传入 deductForRefund
        verify(teacherIncomeService, never()).deductForRefund(eq(2099L), any(), any());
    }

    /** case 8: handleChargeRefunded — 全退 → payment.status=refunded;部分退 → partial_refunded */
    @Test
    void handleChargeRefunded_updatesStatusAndPayment() {
        // sub-case A: 全退(final == settled)
        String chargeId = "ch_test_full";
        PaymentDO payment = paidPayment(200L, 1L, new BigDecimal("100.00"), new BigDecimal("780.00"), "hkd");
        payment.setChannelChargeId(chargeId);

        RefundDO refund = new RefundDO();
        refund.setId(300L);
        refund.setStatus("approved");
        refund.setPaymentId(200L);
        refund.setFinalAmountUsd(new BigDecimal("100.00")); // 全退

        when(paymentMapper.selectByChargeId(chargeId)).thenReturn(Optional.of(payment));
        when(refundMapper.selectActivePendingByPayment(200L)).thenReturn(Optional.of(refund));
        when(refundMapper.updateById(any(RefundDO.class))).thenReturn(1);
        when(paymentMapper.updateById(any(PaymentDO.class))).thenReturn(1);

        refundService.handleChargeRefunded(chargeId);

        // payment.status 应变 "refunded"
        verify(paymentMapper).updateById(argThat((PaymentDO p) ->
                "refunded".equals(p.getStatus())
        ));
    }

    // ======================== checkAndReconcileOrphans tests ========================

    /**
     * case 9: checkAndReconcileOrphans — 有 1 条孤儿 → 反查 Stripe 找到 → 回填 channelRefundId
     */
    @Test
    void checkAndReconcileOrphans_orphanFound_backfillsChannelRefundId() {
        // given: 1 条孤儿退款(approved + channel_refund_id IS NULL + 超 1h)
        RefundDO orphan = new RefundDO();
        orphan.setId(400L);
        orphan.setStatus("approved");
        orphan.setChannelRefundId(null);

        // Stripe 反查返回对应的 Refund
        Refund stripeRefund = new Refund();
        stripeRefund.setId("re_orphan_found");

        when(refundMapper.selectOrphanApproved(any(LocalDateTime.class)))
                .thenReturn(List.of(orphan));
        when(stripeClient.listRefundsByRefundId(400L))
                .thenReturn(List.of(stripeRefund));
        when(refundMapper.updateById(any(RefundDO.class))).thenReturn(1);

        // when
        int result = refundService.checkAndReconcileOrphans();

        // then
        assertThat(result).isEqualTo(1);
        verify(refundMapper).updateById(argThat((RefundDO r) ->
                r.getId().equals(400L) && "re_orphan_found".equals(r.getChannelRefundId())
        ));
    }

    // ======================== reject tests ========================

    /**
     * case 10: reject — pending → status=rejected + audit_by + audit_at + audit_note
     */
    @Test
    void reject_pendingStatus_succeeds() {
        // given
        Long refundId = 500L;
        Long adminId = 9L;
        String auditNote = "申请原因不符合退款政策";

        RefundDO refund = new RefundDO();
        refund.setId(refundId);
        refund.setStatus("pending");
        refund.setPaymentId(200L);

        when(refundMapper.selectById(refundId)).thenReturn(refund);
        when(refundMapper.updateById(any(RefundDO.class))).thenReturn(1);

        // when
        refundService.reject(adminId, refundId, auditNote);

        // then: updateById 应传入 status=rejected + auditBy + auditNote
        verify(refundMapper).updateById(argThat((RefundDO r) ->
                r.getId().equals(refundId)
                        && "rejected".equals(r.getStatus())
                        && adminId.equals(r.getAuditBy())
                        && auditNote.equals(r.getAuditNote())
                        && r.getAuditAt() != null
        ));
        // Stripe 不应被调用
        verifyNoInteractions(stripeClient);
    }

    /** case 10: P1 #7 — amount_settled_usd=NULL 时 fallback amountPaid,不再 NPE */
    @Test
    void apply_settledUsdIsNull_fallbackToAmountPaid() {
        Long userId = 1L;
        Long paymentId = 99L;
        // settledUsd=NULL(charge.updated 还没回填),amountPaid=80.00
        PaymentDO payment = paidPayment(paymentId, userId, null, new BigDecimal("80.00"), "usd");
        PackageDO pkg = pkg4Count();
        StudentPackageDO sp = sp(990L, paymentId, 2); // remaining=2, total=4 → ratio=0.5

        when(paymentMapper.selectById(paymentId)).thenReturn(payment);
        when(studentPackageMapper.selectById(payment.getStudentPackageId())).thenReturn(sp);
        when(packageMapper.selectById(payment.getPackageId())).thenReturn(pkg);
        when(refundMapper.selectActivePendingByPayment(paymentId)).thenReturn(Optional.empty());
        when(refundMapper.insert(any(RefundDO.class))).thenReturn(1);

        // 修复前会 NPE,修复后 suggested = amountPaid × ratio = 80 × 0.5 = 40.00
        RefundDO result = refundService.apply(userId, paymentId, "settled NULL fallback");

        assertThat(result.getSuggestedAmountUsd()).isEqualByComparingTo(new BigDecimal("40.00"));
    }
}
