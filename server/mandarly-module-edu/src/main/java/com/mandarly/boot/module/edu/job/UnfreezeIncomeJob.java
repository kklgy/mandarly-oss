package com.mandarly.boot.module.edu.job;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.quartz.core.handler.JobHandler;
import com.mandarly.boot.framework.tenant.core.job.TenantJob;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherIncomeDO;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.INCOME_LOCK_TIMEOUT;

/**
 * 教师收入解冻 Job(hourly,spec §3.5)
 *
 * <p>Cron: {@code 0 5 * * * ?} = 每小时第 5 分钟
 * <p>功能:扫 status='frozen' 且 frozen_until <= NOW 的 teacher_income,逐 teacher 加 Redisson 锁后
 *  status 改 'available' + balance.unfreeze(amount)。
 * <p>批处理上限 1000 行/Job 防雪崩(spec §3.5 第 4 点)。
 *
 * <p><b>infra_job handler_name = unfreezeIncomeJob(Spring Bean 名)</b>
 */
@Slf4j
@Component
public class UnfreezeIncomeJob implements JobHandler {

    private static final String TEACHER_LOCK_PREFIX = "teacher:lock:";
    private static final long LOCK_WAIT_SECONDS = 5;
    private static final long LOCK_LEASE_SECONDS = 30;
    /** 单 Job 最大扫描行数(spec §3.5)*/
    public static final int BATCH_LIMIT = 1000;

    @Resource
    private TeacherIncomeMapper teacherIncomeMapper;

    @Resource
    private TeacherBalanceService teacherBalanceService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<TeacherIncomeDO> dueRows = teacherIncomeMapper.selectFrozenDue(now, BATCH_LIMIT);

        if (dueRows == null || dueRows.isEmpty()) {
            log.info("[execute] no frozen income due, skip");
            return "rows=0,teachers=0";
        }

        // 按 teacher 分组(保持顺序便于调试)
        Map<Long, List<TeacherIncomeDO>> byTeacher = new LinkedHashMap<>();
        for (TeacherIncomeDO row : dueRows) {
            byTeacher.computeIfAbsent(row.getTeacherId(), k -> new ArrayList<>()).add(row);
        }

        int totalRows = 0;
        int teacherCount = 0;
        for (Map.Entry<Long, List<TeacherIncomeDO>> entry : byTeacher.entrySet()) {
            Long teacherId = entry.getKey();
            List<TeacherIncomeDO> rows = entry.getValue();
            try {
                int processed = unfreezeForTeacher(teacherId, rows);
                totalRows += processed;
                teacherCount++;
            } catch (Exception e) {
                // 单教师失败不阻塞其他教师(Job 整体仍 success);锁等待超时是预期并发路径,降级为 warn。
                if (isIncomeLockTimeout(e)) {
                    log.warn("[execute] teacherId={} unfreeze skipped by lock timeout, rows={}",
                            teacherId, rows.size());
                } else {
                    log.error("[execute] teacherId={} unfreeze failed, rows={}", teacherId, rows.size(), e);
                }
            }
        }

        log.info("[execute] unfroze rows={} teachers={}", totalRows, teacherCount);
        return "rows=" + totalRows + ",teachers=" + teacherCount;
    }

    private boolean isIncomeLockTimeout(Exception e) {
        return e instanceof ServiceException serviceException
                && INCOME_LOCK_TIMEOUT.getCode().equals(serviceException.getCode());
    }

    /**
     * 单 teacher 解冻 — Redisson 锁内事务:逐行 UPDATE status='available' + 一次性 balance.unfreeze(总额)
     */
    private int unfreezeForTeacher(Long teacherId, List<TeacherIncomeDO> rows) throws InterruptedException {
        RLock lock = redissonClient.getLock(TEACHER_LOCK_PREFIX + teacherId);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw exception(INCOME_LOCK_TIMEOUT);
            }
            int[] processed = new int[]{0};
            transactionTemplate.executeWithoutResult(s -> processed[0] = doUnfreeze(teacherId, rows));
            return processed[0];
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 锁内事务:更新 income 行 + 联动 balance.unfreeze。
     * <p>总额 = SUM(amount_usd);amount_usd=0 的行仍标 available 但不计入解冻总额(避免 0 元 unfreeze)。
     */
    private int doUnfreeze(Long teacherId, List<TeacherIncomeDO> rows) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int count = 0;
        for (TeacherIncomeDO row : rows) {
            TeacherIncomeDO upd = new TeacherIncomeDO();
            upd.setId(row.getId());
            upd.setStatus("available");
            // 异步 Job 无 SecurityContext,显式塞 updater 避免 NOT NULL
            upd.setUpdater("system-unfreeze");
            teacherIncomeMapper.updateById(upd);

            BigDecimal amt = row.getAmountUsd() != null ? row.getAmountUsd() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(amt);
            count++;
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            teacherBalanceService.unfreeze(teacherId, totalAmount);
        }
        return count;
    }
}
