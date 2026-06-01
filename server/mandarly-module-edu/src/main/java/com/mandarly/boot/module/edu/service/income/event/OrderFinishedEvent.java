package com.mandarly.boot.module.edu.service.income.event;

/**
 * 课程订单 finished 事件。
 *
 * <p>由 {@code ClassroomServiceImpl#handleRoomEnd} 在事务内 publish,
 * {@code IncomeSettleListener} 在 AFTER_COMMIT 阶段异步消费触发结算。
 *
 * <p>事件解耦保证:settle 失败不会反噬课堂 ended/订单 finished 状态,
 * 由 {@code TeacherIncomeBackfillJob} 定时兜底补结算漏单。
 */
public record OrderFinishedEvent(Long orderId) {
}
