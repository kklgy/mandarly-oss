package com.mandarly.boot.module.edu.job;

import com.mandarly.boot.framework.quartz.core.handler.JobHandler;
import com.mandarly.boot.framework.tenant.core.job.TenantJob;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 教师余额对账告警 Job(daily,A5 改造前称为 BalanceRebuildJob)
 *
 * <p>Cron: {@code 0 0 3 * * ?} = 每天 UTC 03:00 = HKT 11:00
 * <p>功能:按 spec §4.3 三处查询比对 teacher_balance,不一致 → log.error(可告警),不自动 rebuild。
 *
 * <p>A5 前(M4)语义:全量重建 teacher_balance(frozen 7d 到期转 available)— 已废弃,
 * frozen 解冻由 hourly UnfreezeIncomeJob 接管,本 Job 仅做对账校验。
 *
 * <p><b>infra_job handler_name = teacherBalanceFrozenToAvailableJob(Spring Bean 名)</b>
 */
@Slf4j
@Component
public class TeacherBalanceFrozenToAvailableJob implements JobHandler {

    @Resource
    private TeacherBalanceService balanceService;

    /**
     * 每天 UTC 03:00 跑对账(spec §4.3)。
     *
     * @param param 参数(不使用)
     * @return 执行摘要
     */
    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        int checked = balanceService.rebuildAll();
        log.info("[execute] reconciled {} teacher_balance rows", checked);
        return "checked=" + checked;
    }
}
