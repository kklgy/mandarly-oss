package com.mandarly.boot.module.edu.service.refund;

import com.mandarly.boot.module.edu.dal.dataobject.payment.RefundDO;

import java.math.BigDecimal;

/**
 * 退款服务接口(Flow 2 + 2b)
 */
public interface RefundService {

    /**
     * 学生申请退款
     *
     * @param userId    学生 user.id
     * @param paymentId 支付单 ID
     * @param reason    申请原因
     * @return 创建的退款工单
     */
    RefundDO apply(Long userId, Long paymentId, String reason);

    /**
     * 管理员审批退款
     *
     * @param adminId       管理员 user.id
     * @param refundId      退款工单 ID
     * @param finalUsd      最终退款金额(USD)
     * @param adjustReason  调整原因(final ≠ suggested 时必填)
     * @param auditNote     审核备注
     */
    void approve(Long adminId, Long refundId, BigDecimal finalUsd, String adjustReason, String auditNote);

    /**
     * 管理员拒绝退款申请
     *
     * <p>校验 status=pending → UPDATE status='rejected' + audit_by + audit_at + audit_note。
     * 不调 Stripe,仅标记数据库状态。
     *
     * @param adminId   管理员 user.id
     * @param refundId  退款工单 ID
     * @param auditNote 拒绝理由(必填,≥ 5 字符,便于学生申诉)
     */
    void reject(Long adminId, Long refundId, String auditNote);

    /**
     * webhook: charge.refunded → 更新退款工单状态
     *
     * @param chargeId Stripe Charge ID
     */
    void handleChargeRefunded(String chargeId);

    /**
     * 孤儿退款对账(RefundOrphanCheckJob 调用)
     *
     * <p>扫描 status=approved & channel_refund_id IS NULL & create_time < now()-1h 的退款,
     * 逐条 call Stripe Refund.list(metadata.refund_id={refundId}) 反查,
     * 匹配到则回填 channel_refund_id。
     *
     * @return 成功回填的条数
     */
    int checkAndReconcileOrphans();
}
