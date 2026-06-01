package com.mandarly.boot.module.edu.service.refund;

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
import com.mandarly.boot.module.edu.enums.payment.PaymentStatusEnum;
import com.mandarly.boot.module.edu.enums.payment.RefundStatusEnum;
import com.mandarly.boot.module.edu.framework.stripe.client.CreateRefundRequest;
import com.mandarly.boot.module.edu.framework.stripe.client.StripeClient;
import com.mandarly.boot.module.edu.service.income.TeacherIncomeService;
import com.mandarly.boot.module.edu.service.mail.PaymentMailTemplateHelper;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import com.mandarly.boot.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import com.stripe.model.Refund;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.*;

/**
 * 退款服务实现(Flow 2 + Flow 2b)
 *
 * <p>关键算法:
 * <ul>
 *   <li>apply: ratio = remaining/totalCount(scale=4 HALF_UP),suggested = settledUsd×ratio(scale=2 HALF_UP)</li>
 *   <li>approve: finalPaidCurrency = amountPaid × (final/settled)(scale=2 HALF_UP) — 原支付币种!</li>
 *   <li>Flow 2b: deductTeacherIncomes 同事务扣回 income(负数 amount_usd,DuplicateKeyException 视为 success)</li>
 *   <li>approve 本地 DB 步骤同事务;Stripe Refund 是外部幂等副作用,保留 orphan job 兜底写库失败窗口</li>
 * </ul>
 */
@Slf4j
@Service
public class RefundServiceImpl implements RefundService {

    @Resource
    private RefundMapper refundMapper;

    @Resource
    private PaymentMapper paymentMapper;

    @Resource
    private PackageMapper packageMapper;

    @Resource
    private StudentPackageMapper studentPackageMapper;

    @Resource
    private TeacherIncomeMapper teacherIncomeMapper;

    @Resource
    private StripeClient stripeClient;

    @Resource
    private MailSendApi mailSendApi;

    @Resource
    private TeacherIncomeService teacherIncomeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundDO apply(Long userId, Long paymentId, String reason) {
        PaymentDO p = paymentMapper.selectById(paymentId);
        if (p == null || !p.getUserId().equals(userId)) throw exception(PAYMENT_NOT_FOUND);
        if (!PaymentStatusEnum.PAID.getCode().equals(p.getStatus())) throw exception(PAYMENT_STATUS_INVALID);
        if (refundMapper.selectActivePendingByPayment(paymentId).isPresent()) throw exception(REFUND_DUPLICATE_PENDING);

        StudentPackageDO sp = studentPackageMapper.selectById(p.getStudentPackageId());
        PackageDO pkg = packageMapper.selectById(p.getPackageId());

        // ⭐ ratio 用 BigDecimal scale=4 中间精度,setScale(2, HALF_UP) 落库
        BigDecimal ratio = new BigDecimal(sp.getRemaining())
                .divide(new BigDecimal(pkg.getTotalCount()), 4, RoundingMode.HALF_UP);
        // Stripe charge.updated 异步 settle 时间窗内 amountSettledUsd 可能 NULL,fallback amountPaid
        // 业务影响:fallback 值略高于真实 settled(差 Stripe 手续费 ~3%),但不阻塞用户申请退款
        BigDecimal base = p.getAmountSettledUsd() != null ? p.getAmountSettledUsd() : p.getAmountPaid();
        BigDecimal suggested = base.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        RefundDO r = new RefundDO();
        r.setPaymentId(paymentId);
        r.setUserId(userId);
        r.setStudentPackageId(sp.getId());
        r.setApplyReason(reason);
        r.setSuggestedAmountUsd(suggested);
        r.setStatus(RefundStatusEnum.PENDING.getCode());
        r.setTeacherIncomeDeducted(false);
        refundMapper.insert(r);
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long adminId, Long refundId, BigDecimal finalUsd, String adjustReason, String auditNote) {
        RefundDO r = refundMapper.selectById(refundId);
        if (!RefundStatusEnum.PENDING.getCode().equals(r.getStatus())) throw exception(REFUND_STATUS_INVALID);
        PaymentDO p = paymentMapper.selectById(r.getPaymentId());

        // Stripe charge.updated 异步 settle 时间窗内 amountSettledUsd 可能 NULL(或 Stripe webhook 未订阅 charge.updated)。
        // 与 apply() line 92 fallback 对齐:settled 缺失则用 amountPaid(等价于"假设无手续费",退款比例与 Stripe 实退金额一致)。
        BigDecimal settledBase = p.getAmountSettledUsd() != null ? p.getAmountSettledUsd() : p.getAmountPaid();

        // ⭐ 校验 final 合理:0 ≤ final ≤ settledBase
        if (finalUsd.compareTo(BigDecimal.ZERO) < 0 || finalUsd.compareTo(settledBase) > 0)
            throw exception(REFUND_AMOUNT_EXCEEDS);

        // ⭐ 改了金额必须填 adjust_reason
        if (finalUsd.compareTo(r.getSuggestedAmountUsd()) != 0
                && (adjustReason == null || adjustReason.isBlank()))
            throw exception(REFUND_ADJUST_REASON_REQUIRED);

        // ⭐ Stripe Refund 用原支付币种金额(不是 USD!)
        // finalPaidCurrency = amount_paid × (final / settledBase),scale=4 中间,scale=2 落库
        BigDecimal ratio = finalUsd.divide(settledBase, 4, RoundingMode.HALF_UP);
        BigDecimal finalPaidCurrency = p.getAmountPaid().multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        RefundDO approveUpd = new RefundDO();
        approveUpd.setId(refundId);
        approveUpd.setStatus(RefundStatusEnum.APPROVED.getCode());
        approveUpd.setFinalAmountUsd(finalUsd);
        approveUpd.setAdjustReason(adjustReason);
        approveUpd.setAuditBy(adminId);
        approveUpd.setAuditAt(LocalDateTime.now());
        approveUpd.setAuditNote(auditNote);
        refundMapper.updateById(approveUpd);

        // 调 Stripe(idempotency-key='refund-{id}',原支付币种)。若远端成功后本地写库失败,RefundOrphanCheckJob 负责补偿。
        Refund stripeRefund = stripeClient.createRefund(CreateRefundRequest.builder()
                .refundId(refundId)
                .paymentIntentId(p.getChannelPaymentIntentId())
                .amount(finalPaidCurrency)
                .currency(p.getCurrencyPaid())
                .build());
        RefundDO channelUpd = new RefundDO();
        channelUpd.setId(refundId);
        channelUpd.setChannelRefundId(stripeRefund.getId());
        refundMapper.updateById(channelUpd);

        // Flow 2b 同事务扣回 income
        deductTeacherIncomes(r, p, finalUsd);

        // 作废 sp(整套作废)
        StudentPackageDO spUpd = new StudentPackageDO();
        spUpd.setId(r.getStudentPackageId());
        spUpd.setRemaining(0);
        spUpd.setExpireAt(LocalDateTime.now());
        studentPackageMapper.updateById(spUpd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long adminId, Long refundId, String auditNote) {
        RefundDO r = refundMapper.selectById(refundId);
        if (r == null) throw exception(REFUND_NOT_FOUND);
        if (!RefundStatusEnum.PENDING.getCode().equals(r.getStatus())) throw exception(REFUND_STATUS_INVALID);

        RefundDO upd = new RefundDO();
        upd.setId(refundId);
        upd.setStatus(RefundStatusEnum.REJECTED.getCode());
        upd.setAuditBy(adminId);
        upd.setAuditAt(LocalDateTime.now());
        upd.setAuditNote(auditNote);
        refundMapper.updateById(upd);
        log.info("[reject] refundId={} rejected by adminId={}", refundId, adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleChargeRefunded(String chargeId) {
        PaymentDO p = paymentMapper.selectByChargeId(chargeId)
                .orElseThrow(() -> exception(PAYMENT_NOT_FOUND));
        RefundDO r = refundMapper.selectActivePendingByPayment(p.getId())
                .orElseThrow(() -> exception(REFUND_NOT_FOUND));

        RefundDO refundUpd = new RefundDO();
        refundUpd.setId(r.getId());
        refundUpd.setStatus(RefundStatusEnum.REFUNDED.getCode());
        refundUpd.setRefundedAt(LocalDateTime.now());
        refundUpd.setUpdater("stripe-webhook");
        refundMapper.updateById(refundUpd);

        // 全退 → refunded,部分退 → partial_refunded
        String newPaymentStatus = r.getFinalAmountUsd().compareTo(p.getAmountSettledUsd()) >= 0
                ? PaymentStatusEnum.REFUNDED.getCode() : PaymentStatusEnum.PARTIAL_REFUNDED.getCode();
        PaymentDO paymentUpd = new PaymentDO();
        paymentUpd.setId(p.getId());
        paymentUpd.setStatus(newPaymentStatus);
        paymentUpd.setUpdater("stripe-webhook");
        paymentMapper.updateById(paymentUpd);

        // Phase 9.2 邮件模板 #2:退款到账通知学生
        try {
            String userLocale = getUserLocale(r.getUserId());
            String templateCode = PaymentMailTemplateHelper.pickTemplateCode("mandarly_payment_refund", userLocale);
            PackageDO pkg2 = packageMapper.selectById(p.getPackageId());
            Map<String, Object> vars = Map.of(
                    "finalAmount", r.getFinalAmountUsd().toPlainString(),
                    "currency",    p.getCurrencyPaid() != null ? p.getCurrencyPaid() : "USD",
                    "packageName", pkg2 != null ? pkg2.getNameI18nCode() : "—",
                    PaymentMailTemplateHelper.PARAM_LOCALE, PaymentMailTemplateHelper.normalizeLocale(userLocale)
            );
            String email = getUserEmail(r.getUserId());
            if (email == null || email.isEmpty()) {
                log.warn("[handleChargeRefunded] user email not found, skip mail. refundId={} userId={}",
                        r.getId(), r.getUserId());
            } else {
                MailSendSingleToUserReqDTO req = new MailSendSingleToUserReqDTO();
                req.setToMails(java.util.List.of(email));
                req.setTemplateCode(templateCode);
                req.setTemplateParams(vars);
                mailSendApi.sendSingleMailToMember(req);
            }
        } catch (Exception e) {
            log.error("[handleChargeRefunded] mail send failed for refund {}", r.getId(), e);
        }
        // TODO M5 短信通道
    }

    @Override
    public int checkAndReconcileOrphans() {
        // 扫描 1h 前 approved 但 channel_refund_id 为空的退款
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        List<RefundDO> orphans = refundMapper.selectOrphanApproved(cutoff);
        if (orphans.isEmpty()) {
            log.info("[checkAndReconcileOrphans] no orphan refunds found");
            return 0;
        }

        AtomicInteger reconciled = new AtomicInteger(0);
        for (RefundDO orphan : orphans) {
            try {
                // 反查 Stripe Refund.list(metadata.refund_id={refundId})
                List<Refund> stripeRefunds = stripeClient.listRefundsByRefundId(orphan.getId());
                if (stripeRefunds.isEmpty()) {
                    log.warn("[checkAndReconcileOrphans] refundId={} not found in Stripe, skip", orphan.getId());
                    continue;
                }
                // 取第一条(正常只有 1 条)
                Refund stripeRefund = stripeRefunds.get(0);
                RefundDO update = new RefundDO();
                update.setId(orphan.getId());
                update.setChannelRefundId(stripeRefund.getId());
                refundMapper.updateById(update);
                reconciled.incrementAndGet();
                log.info("[checkAndReconcileOrphans] refundId={} reconciled channelRefundId={}", orphan.getId(), stripeRefund.getId());
            } catch (Exception e) {
                log.error("[checkAndReconcileOrphans] refundId={} reconcile failed", orphan.getId(), e);
            }
        }
        log.info("[checkAndReconcileOrphans] total orphans={} reconciled={}", orphans.size(), reconciled.get());
        return reconciled.get();
    }

    // ======================== private ========================

    /**
     * 反射取用户 locale,失败降级 "en"。
     */
    private String getUserLocale(Long userId) {
        try {
            Class<?> cls = Class.forName("com.mandarly.boot.module.system.service.user.UserService");
            Object bean = cn.hutool.extra.spring.SpringUtil.getBean(cls);
            java.lang.reflect.Method m = cls.getMethod("getById", Long.class);
            Object user = m.invoke(bean, userId);
            if (user == null) return "en";
            java.lang.reflect.Method getLocale = user.getClass().getMethod("getLocale");
            String locale = (String) getLocale.invoke(user);
            return locale != null ? locale : "en";
        } catch (Exception e) {
            log.warn("[getUserLocale] reflection fail for userId={}, fallback en", userId, e);
            return "en";
        }
    }

    /**
     * 反射取用户邮箱,失败降级空字符串。
     */
    private String getUserEmail(Long userId) {
        try {
            Class<?> cls = Class.forName("com.mandarly.boot.module.system.service.user.UserService");
            Object bean = cn.hutool.extra.spring.SpringUtil.getBean(cls);
            java.lang.reflect.Method m = cls.getMethod("getById", Long.class);
            Object user = m.invoke(bean, userId);
            if (user == null) return "";
            java.lang.reflect.Method getEmail = user.getClass().getMethod("getEmail");
            Object email = getEmail.invoke(user);
            return email != null ? email.toString() : "";
        } catch (Exception e) {
            log.warn("[getUserEmail] reflection fail for userId={}", userId, e);
            return "";
        }
    }

    /**
     * 退款扣回(P0-4 ratio 扣方案,2026-05-19 重写)。
     *
     * <p>原 M4 实现自己写负数 income 行但**不扣 teacher_balance + 无 Redisson 锁 + 不标原 income reverted**,
     * 教师退款后照样能提走全额钱(¥ 漏洞)。本次替换为循环调 M6 spec §3.4 的
     * {@link TeacherIncomeService#deductForRefund(Long, Long, BigDecimal)}—
     * 每个 course_order 单独扣 origin.amount × ratio,完整扣 balance + 标 reverted + Redisson 锁。
     *
     * <p>ratio = {@code finalUsd / settledBase}(scale=4 HALF_UP),settledBase 与 {@link #approve}
     * 用同一个公式(优先 amountSettledUsd,fallback amountPaid)。
     *
     * <p>邮件通知按 teacher 聚合 — 扣完后从 teacher_income 反查刚写入的 refund_deduct 行求和。
     */
    private void deductTeacherIncomes(RefundDO r, PaymentDO p, BigDecimal finalUsd) {
        BigDecimal settledBase = p.getAmountSettledUsd() != null ? p.getAmountSettledUsd() : p.getAmountPaid();
        BigDecimal ratio = finalUsd.divide(settledBase, 4, RoundingMode.HALF_UP);
        List<TeacherIncomeDO> incomes = teacherIncomeMapper.selectByStudentPackage(r.getStudentPackageId());

        // 逐订单调 deductForRefund(每节课独立扣 origin.amount × ratio)
        java.util.Set<Long> processedOrderIds = new java.util.HashSet<>();
        for (TeacherIncomeDO ti : incomes) {
            if (!"normal".equals(ti.getType())) continue; // 跳过历史 refund_deduct 负数行
            if (!processedOrderIds.add(ti.getCourseOrderId())) continue; // 同 order 仅扣一次
            try {
                teacherIncomeService.deductForRefund(ti.getCourseOrderId(), r.getId(), ratio);
            } catch (com.mandarly.boot.framework.common.exception.ServiceException dup) {
                // INCOME_ALREADY_REVERTED 幂等防御:同一 refund 重复审核(理论上 PENDING 状态机已防,留兜底)
                log.info("[deductTeacherIncomes] orderId={} 已 reverted,幂等跳过 code={}",
                        ti.getCourseOrderId(), dup.getCode());
            }
        }

        // 邮件按 teacher 聚合 — 从 teacher_income 反查刚写入的 refund_deduct 负数行求和
        List<TeacherIncomeDO> deductRows = teacherIncomeMapper.selectByStudentPackage(r.getStudentPackageId()).stream()
                .filter(ti -> "refund_deduct".equals(ti.getType()) && r.getId().equals(ti.getRefundId()))
                .collect(Collectors.toList());
        Map<Long, BigDecimal> deductSumByTeacher = deductRows.stream()
                .collect(Collectors.groupingBy(TeacherIncomeDO::getTeacherId,
                        Collectors.reducing(BigDecimal.ZERO,
                                ti -> ti.getAmountUsd().abs(), BigDecimal::add)));

        for (Map.Entry<Long, BigDecimal> e : deductSumByTeacher.entrySet()) {
            try {
                String teacherLocale = getUserLocale(e.getKey());
                String tmplCode = PaymentMailTemplateHelper.pickTemplateCode("mandarly_payment_teacher_deduct", teacherLocale);
                Map<String, Object> tvars = Map.of(
                        "teacherName",       String.valueOf(e.getKey()),
                        "deductedAmountUsd", e.getValue().toPlainString(),
                        "studentEmail",      getUserEmail(r.getUserId()),
                        "reason",            r.getApplyReason() != null ? r.getApplyReason() : "—",
                        PaymentMailTemplateHelper.PARAM_LOCALE, PaymentMailTemplateHelper.normalizeLocale(teacherLocale)
                );
                String teacherEmail = getUserEmail(e.getKey());
                if (teacherEmail != null && !teacherEmail.isEmpty()) {
                    MailSendSingleToUserReqDTO treq = new MailSendSingleToUserReqDTO();
                    treq.setToMails(java.util.List.of(teacherEmail));
                    treq.setTemplateCode(tmplCode);
                    treq.setTemplateParams(tvars);
                    mailSendApi.sendSingleMailToMember(treq);
                }
            } catch (Exception mailEx) {
                log.error("[deductTeacherIncomes] mail send failed for teacher {}", e.getKey(), mailEx);
            }
        }

        RefundDO deductedUpd = new RefundDO();
        deductedUpd.setId(r.getId());
        deductedUpd.setTeacherIncomeDeducted(true);
        refundMapper.updateById(deductedUpd);
    }
}
