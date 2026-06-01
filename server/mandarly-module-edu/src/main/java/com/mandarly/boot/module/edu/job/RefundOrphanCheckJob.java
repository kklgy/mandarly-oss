package com.mandarly.boot.module.edu.job;

import com.mandarly.boot.framework.quartz.core.handler.JobHandler;
import com.mandarly.boot.framework.tenant.core.aop.TenantIgnore;
import com.mandarly.boot.module.edu.service.refund.RefundService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 退款孤儿对账 Job(weekly, Monday)
 *
 * <p>Cron: {@code 0 0 5 ? * MON} = 每周一 UTC 05:00 = HKT 13:00
 * <p>功能:扫描 status=approved & channel_refund_id IS NULL & 超过 1h 的退款,
 * 反查 Stripe Refund.list 补填 channel_refund_id。
 * <p>孤儿成因:approve 调 Stripe createRefund 成功但写库前进程崩溃。
 *
 * <p><b>infra_job handler_name = refundOrphanCheckJob(Spring Bean 名)</b>
 *
 * <p>注:使用 @TenantIgnore 而非 @TenantJob — refund 操作是全局的(Stripe 全局),
 * 不需要按租户逐个执行,直接忽略租户过滤即可。
 */
@Slf4j
@Component
public class RefundOrphanCheckJob implements JobHandler {

    @Resource
    private RefundService refundService;

    /**
     * 每周一 UTC 05:00 对账退款孤儿。
     *
     * @param param 参数(不使用)
     * @return 执行摘要
     */
    @Override
    @TenantIgnore
    public String execute(String param) throws Exception {
        int checked = refundService.checkAndReconcileOrphans();
        log.info("[execute] reconciled {} orphan refunds", checked);
        return "checked=" + checked;
    }
}
