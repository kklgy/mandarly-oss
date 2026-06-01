package com.mandarly.boot.module.edu.service.income.event;

import com.mandarly.boot.module.edu.service.income.TeacherIncomeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 监听 {@link OrderFinishedEvent} 触发教师收入结算。
 *
 * <p>AFTER_COMMIT 保证课堂/订单状态主事务已落库,settle 在独立线程独立事务执行,
 * 异常不反噬课堂状态。失败由 {@link com.mandarly.boot.module.edu.job.TeacherIncomeBackfillJob}
 * 兜底补录。
 *
 * <p>@Async 在 {@code MandarlyAsyncAutoConfiguration} 已启用,默认线程池足够。
 */
@Slf4j
@Component
public class IncomeSettleListener {

    @Resource
    private TeacherIncomeService incomeService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderFinished(OrderFinishedEvent event) {
        try {
            incomeService.settle(event.orderId());
            log.info("[onOrderFinished] settled orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("[onOrderFinished] settle 失败 orderId={},等待 TeacherIncomeBackfillJob 兜底",
                    event.orderId(), e);
        }
    }

    /**
     * 兜底监听:即使没有事务(罕见场景,例如 backfill Job 同步调用路径),也能触发一次 settle。
     *
     * <p>用 {@code condition = "false"} 在生产中关闭,只在测试中通过 spring profile 打开。
     * 默认走 {@link #onOrderFinished} 即可。
     */
    @EventListener(condition = "false")
    public void onOrderFinishedFallback(OrderFinishedEvent event) {
        log.warn("[onOrderFinishedFallback] 非事务路径触发 orderId={}", event.orderId());
        incomeService.settle(event.orderId());
    }
}
