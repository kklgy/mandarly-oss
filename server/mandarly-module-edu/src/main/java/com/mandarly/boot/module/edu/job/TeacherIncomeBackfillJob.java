package com.mandarly.boot.module.edu.job;

import com.mandarly.boot.framework.quartz.core.handler.JobHandler;
import com.mandarly.boot.framework.tenant.core.job.TenantJob;
import com.mandarly.boot.module.edu.service.income.TeacherIncomeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 教师收入兜底建账 Job(daily)
 *
 * <p>Cron: {@code 0 0 4 * * ?} = 每天 UTC 04:00 = HKT 12:00
 * <p>功能:扫描 finished 但无 income 的 course_order,补录收入流水
 * <p>场景:LCIC RoomEnd 回调丢失、服务重启等造成的漏记
 *
 * <p><b>infra_job handler_name = teacherIncomeBackfillJob(Spring Bean 名)</b>
 */
@Slf4j
@Component
public class TeacherIncomeBackfillJob implements JobHandler {

    @Resource
    private TeacherIncomeService incomeService;

    /**
     * 每天 UTC 04:00 兜底补录 teacher_income。
     *
     * @param param 参数(不使用)
     * @return 执行摘要
     */
    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        int backfilled = incomeService.backfillFinishedOrders();
        log.info("[execute] backfilled {} orders", backfilled);
        return "backfilled=" + backfilled;
    }
}
