package com.mandarly.boot.module.edu.service.income;

import java.math.BigDecimal;

/**
 * 教师收入结算服务接口(Flow 3)
 */
public interface TeacherIncomeService {

    /**
     * 结算单节课 income(LCIC RoomEnd 触发)
     *
     * <p>5 个分支:normal / free_trial(amount=0) / no_show_teacher(amount=0) /
     * no_show_student(全额) / duplicate(DuplicateKeyException 视为 success)
     *
     * <p>M6:status=frozen,frozen_until=settledAt+7d,同事务调 balance.freeze(...)。
     *
     * @param courseOrderId 课程订单 ID
     */
    void settle(Long courseOrderId);

    /**
     * 批量兜底结算:扫描 finished 但无 income 的 order(定时任务兜底)
     *
     * @return 补录条数
     */
    int backfillFinishedOrders();

    /**
     * 退款扣回教师收入(spec §3.4 / db-design v1.2 §1.5.2,P0-4 ratio 扣方案)
     *
     * <p>扣回金额 = {@code origin.amount × ratio}(scale=2 HALF_UP)。
     * 例:学生买 5 节 25 USD,上了 2 节(教师已结算 10 USD),退 15 USD(只退未上的 3 节)→
     * ratio = 15/25 = 0.6,教师每节扣 5×0.6 = 3 USD,扣后教师剩 (5-3)×2 = 4 USD —
     * 体现教师上 2 节课的实际付出。
     *
     * <p>4 分支(按原 normal income.status):
     * <ul>
     *   <li>available → balance.deductFromAvailable(可能允许负数 + 告警)</li>
     *   <li>frozen → balance.deductFromFrozen</li>
     *   <li>reverted → throw INCOME_ALREADY_REVERTED(幂等防御)</li>
     *   <li>已被提现导致 available 不够 → 仍调 deductFromAvailable(允许负数 + log.error)</li>
     * </ul>
     *
     * <p>事务内:写 refund_deduct 负数流水(amount=-origin.amount×ratio,status=available,
     * frozen_until=settledAt)+ 原行标 reverted。全程加 Redisson 锁 `teacher:lock:{teacherId}`。
     *
     * @param courseOrderId 原结算订单 id
     * @param refundId      退款工单 id(关联 refund 表)
     * @param ratio         扣回比例,= refund.finalUsd / payment.settledBase,scale=4 HALF_UP。
     *                      由 caller(RefundServiceImpl.approve)统一计算后传入。
     *                      ratio &lt;= 0 视为不扣(免费试听 / 0 元订单短路返回 reverted)。
     */
    void deductForRefund(Long courseOrderId, Long refundId, BigDecimal ratio);
}
