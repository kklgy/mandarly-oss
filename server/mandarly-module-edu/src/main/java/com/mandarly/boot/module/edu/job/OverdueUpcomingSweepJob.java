package com.mandarly.boot.module.edu.job;

import com.mandarly.boot.framework.quartz.core.handler.JobHandler;
import com.mandarly.boot.framework.tenant.core.job.TenantJob;
import com.mandarly.boot.module.edu.service.booking.BookingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * D28 过期 upcoming 订单兜底 Job(每 15 min)
 *
 * <p>Cron: {@code 0 *}{@code /15 * * * ?} = 每 15 min(UTC)
 * <p>场景:LCIC room_end webhook 丢包 / stub-{orderId} 占位 / 双方都没进 等情况下,
 * 课程已超过 (scheduledAt + duration + 15min) 但 status 仍是 'upcoming' 的孤儿单,
 * 由 {@link BookingService#sweepOverdueUpcoming()} 按 meeting 实际状态分流到
 * finished(补 webhook 丢包)或 abnormal(进 admin 客诉队列)。
 *
 * <p><b>infra_job handler_name = overdueUpcomingSweepJob(Spring Bean 名)</b>
 */
@Slf4j
@Component
public class OverdueUpcomingSweepJob implements JobHandler {

    @Resource
    private BookingService bookingService;

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        String summary = bookingService.sweepOverdueUpcoming();
        log.info("[execute] {}", summary);
        return summary;
    }
}
