package com.mandarly.boot.module.edu.service.referral;

import java.math.BigDecimal;

/**
 * 推荐码服务接口(Flow 4)
 *
 * <p>职责:推荐码绑定 + 折扣计算 + 奖励发放
 */
public interface ReferralService {

    /**
     * 创建支付时绑定推荐关系,返回 discount 金额(0=不生效)
     *
     * <p>降级语义:空码/无效码/已 paid/已 bound → 返回 0,不抛错
     * <p>唯一抛错路径:自引用 → REFERRAL_CODE_SELF_USE
     *
     * @param refereeUserId 被推荐人 userId
     * @param referralCode  推荐码(可空)
     * @return discount 金额(USD)
     */
    BigDecimal bindAndCalculateDiscount(Long refereeUserId, String referralCode);

    /**
     * 支付单创建后回填 referral_record.payment_id。
     *
     * <p>bindAndCalculateDiscount 执行时 payment 还未插入,因此 paymentId 只能在支付单落库后补写。
     *
     * @param refereeUserId 被推荐人 userId
     * @param paymentId 支付单 ID
     */
    void attachPayment(Long refereeUserId, Long paymentId);

    /**
     * webhook 支付成功后触发奖励发放(idempotent)
     *
     * @param paymentId 支付单 ID
     */
    void triggerReward(Long paymentId);

    /**
     * 学生 ProfileView 查推荐战绩
     *
     * @param userId 学生 userId
     * @return 推荐统计 VO
     */
    com.mandarly.boot.module.edu.controller.app.referral.vo.AppReferralStatsRespVO getMyStats(Long userId);
}
