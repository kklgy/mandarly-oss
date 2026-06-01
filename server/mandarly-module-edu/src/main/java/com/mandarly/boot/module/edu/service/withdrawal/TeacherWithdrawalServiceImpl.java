package com.mandarly.boot.module.edu.service.withdrawal;

import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.withdrawal.vo.WithdrawalPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;
import com.mandarly.boot.module.edu.dal.mysql.withdrawal.TeacherWithdrawalMapper;
import com.mandarly.boot.module.edu.enums.LogRecordConstants;
import com.mandarly.boot.module.edu.enums.withdrawal.WithdrawalStatusEnum;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import com.mandarly.boot.module.edu.service.notification.NotificationService;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.WITHDRAWAL_BELOW_MIN;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.WITHDRAWAL_FAIL_REASON_REQUIRED;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.WITHDRAWAL_INVALID_STATUS;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.WITHDRAWAL_LOCK_TIMEOUT;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.WITHDRAWAL_NOT_FOUND;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.WITHDRAWAL_PENDING_EXISTS;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.WITHDRAWAL_REJECT_REASON_REQUIRED;

/**
 * 教师提现 Service 实现(M6 §3.1 / §3.2 / §3.3)
 *
 * <p>统一锁前缀:{@code teacher:lock:{teacherId}}(spec §3.3),5s wait / 30s lease。
 *
 * <p>Lock-then-tx 模式(参考 TeacherIncomeServiceImpl#deductForRefund):
 * Redisson 锁外层 + TransactionTemplate 内层,避免 @Transactional 自调用失效。
 */
@Slf4j
@Service
public class TeacherWithdrawalServiceImpl implements TeacherWithdrawalService {

    private static final String TEACHER_LOCK_PREFIX = "teacher:lock:";
    private static final long LOCK_WAIT_SECONDS = 5;
    private static final long LOCK_LEASE_SECONDS = 30;

    /**
     * spec §7.5:platform_config.withdrawal.min_amount,默认 100 USD。
     *
     * <p>A7 起改走 {@link ConfigApi#getConfigValueByKey(String)} 读 {@code infra_config} 表的
     * {@code mandarly.withdrawal.min_amount},支持热更新无需重启。读取失败 / key 未配置时回落 100。
     */
    private static final String CONFIG_KEY_MIN_AMOUNT = "mandarly.withdrawal.min_amount";
    private static final BigDecimal DEFAULT_MIN_AMOUNT = new BigDecimal("100");

    @Resource
    private TeacherWithdrawalMapper teacherWithdrawalMapper;

    @Resource
    private TeacherBalanceService teacherBalanceService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ConfigApi configApi;

    @Resource
    private NotificationService notificationService;

    /**
     * 读取最低提现金额配置(热更新)。
     *
     * <p>读 {@code infra_config.mandarly.withdrawal.min_amount} → 解析为 BigDecimal;
     * 解析失败 / key 未配置 / 抛异常 → fallback 到 {@link #DEFAULT_MIN_AMOUNT}(100 USD)。
     */
    private BigDecimal resolveMinAmount() {
        try {
            String raw = configApi.getConfigValueByKey(CONFIG_KEY_MIN_AMOUNT);
            if (raw == null || raw.isBlank()) {
                return DEFAULT_MIN_AMOUNT;
            }
            return new BigDecimal(raw.trim());
        } catch (Exception e) {
            log.warn("[withdrawal] resolveMinAmount fallback to default {} — reason: {}",
                    DEFAULT_MIN_AMOUNT, e.getMessage());
            return DEFAULT_MIN_AMOUNT;
        }
    }

    // ======================== applyWithdrawal ========================

    @Override
    public Long applyWithdrawal(Long teacherId, BigDecimal amount, String payeeInfo, String payeeMethod) {
        // 入口校验 — 锁外预检 + 锁内复检(防热更后的边缘 case),双重 resolve 容忍 1ms 期间的配置变更
        BigDecimal minAmount = resolveMinAmount();
        if (amount == null || amount.compareTo(minAmount) < 0) {
            throw exception(WITHDRAWAL_BELOW_MIN, minAmount);
        }

        Long[] resultId = new Long[1];

        runUnderTeacherLock(teacherId, () -> {
            // (a) 已有进行中的(pending / approved)提现 → 拒
            List<TeacherWithdrawalDO> existing = teacherWithdrawalMapper.selectByTeacherIdAndStatusIn(
                    teacherId,
                    List.of(WithdrawalStatusEnum.PENDING.getValue(), WithdrawalStatusEnum.APPROVED.getValue()));
            if (existing != null && !existing.isEmpty()) {
                throw exception(WITHDRAWAL_PENDING_EXISTS);
            }

            // (b) 金额下限(锁内复检,防 minAmount 配置热更后的边缘 case)
            BigDecimal currentMinAmount = resolveMinAmount();
            if (amount.compareTo(currentMinAmount) < 0) {
                throw exception(WITHDRAWAL_BELOW_MIN, currentMinAmount);
            }

            // (c) 余额冻结 + 写 withdrawal,全在事务内
            transactionTemplate.executeWithoutResult(s -> {
                // balance.available -= amount / pending_withdraw += amount
                // 余额不够 / 乐观锁冲突由 balance 内部抛 ServiceException(BALANCE_VERSION_CONFLICT)
                teacherBalanceService.holdForWithdrawal(teacherId, amount);

                TeacherWithdrawalDO row = new TeacherWithdrawalDO();
                row.setTeacherId(teacherId);
                row.setAmount(amount);
                row.setCurrency("USD");
                row.setPayeeInfo(payeeInfo); // TypeHandler 透明加密
                row.setPayeeMethod(payeeMethod);
                row.setStatus(WithdrawalStatusEnum.PENDING.getValue());
                row.setAppliedAt(LocalDateTime.now());
                teacherWithdrawalMapper.insert(row);
                resultId[0] = row.getId();
            });
        });

        // 主事务 + 锁释放后触发异步通知(失败不影响主流程,见 NotificationServiceImpl)
        notifyWithdrawalEvent("applied", resultId[0]);

        return resultId[0];
    }

    // ======================== audit ========================

    @Override
    public void audit(Long id, boolean approved, String rejectReason, Long auditorId) {
        TeacherWithdrawalDO row = teacherWithdrawalMapper.selectById(id);
        if (row == null) {
            throw exception(WITHDRAWAL_NOT_FOUND);
        }
        // 状态机:audit 只允许 pending
        if (!WithdrawalStatusEnum.PENDING.getValue().equals(row.getStatus())) {
            throw exception(WITHDRAWAL_INVALID_STATUS);
        }
        // rejected 必须填原因(锁外早抛减少锁竞争)
        if (!approved && (rejectReason == null || rejectReason.isBlank())) {
            throw exception(WITHDRAWAL_REJECT_REASON_REQUIRED);
        }

        Long teacherId = row.getTeacherId();
        BigDecimal amount = row.getAmount();

        runUnderTeacherLock(teacherId, () -> transactionTemplate.executeWithoutResult(s -> {
            TeacherWithdrawalDO upd = new TeacherWithdrawalDO();
            upd.setId(id);
            upd.setAuditedBy(auditorId);
            upd.setAuditedAt(LocalDateTime.now());

            if (approved) {
                upd.setStatus(WithdrawalStatusEnum.APPROVED.getValue());
                teacherWithdrawalMapper.updateById(upd);
                // approved 不动余额(仍在 pending_withdraw 中,等 markPaid / markFailed 二次操作)
            } else {
                upd.setStatus(WithdrawalStatusEnum.REJECTED.getValue());
                upd.setRejectReason(rejectReason);
                teacherWithdrawalMapper.updateById(upd);
                // 余额回退:pending_withdraw → available
                teacherBalanceService.releaseFromWithdrawal(teacherId, amount);
            }
        }));

        // 主事务 + 锁释放后触发异步通知(approved → "approved";rejected → "rejected")
        notifyWithdrawalEvent(approved ? "approved" : "rejected", id);
    }

    // ======================== markPaid ========================

    @Override
    public void markPaid(Long id, String paidProof, String paidRemark, Long operatorId) {
        TeacherWithdrawalDO row = teacherWithdrawalMapper.selectById(id);
        if (row == null) {
            throw exception(WITHDRAWAL_NOT_FOUND);
        }
        // 状态机:markPaid 只允许 approved
        if (!WithdrawalStatusEnum.APPROVED.getValue().equals(row.getStatus())) {
            throw exception(WITHDRAWAL_INVALID_STATUS);
        }

        Long teacherId = row.getTeacherId();
        BigDecimal amount = row.getAmount();

        runUnderTeacherLock(teacherId, () -> transactionTemplate.executeWithoutResult(s -> {
            TeacherWithdrawalDO upd = new TeacherWithdrawalDO();
            upd.setId(id);
            upd.setStatus(WithdrawalStatusEnum.PAID.getValue());
            upd.setPaidBy(operatorId);
            upd.setPaidAt(LocalDateTime.now());
            upd.setPaidProof(paidProof);
            upd.setPaidRemark(paidRemark);
            teacherWithdrawalMapper.updateById(upd);
            // 余额清算:pending_withdraw -= amount / total_withdrawn += amount
            teacherBalanceService.confirmWithdrawal(teacherId, amount);
        }));

        // 主事务 + 锁释放后触发异步通知
        notifyWithdrawalEvent("paid", id);
    }

    // ======================== markFailed ========================

    @Override
    public void markFailed(Long id, String failReason, Long operatorId) {
        TeacherWithdrawalDO row = teacherWithdrawalMapper.selectById(id);
        if (row == null) {
            throw exception(WITHDRAWAL_NOT_FOUND);
        }
        if (!WithdrawalStatusEnum.APPROVED.getValue().equals(row.getStatus())) {
            throw exception(WITHDRAWAL_INVALID_STATUS);
        }
        if (failReason == null || failReason.isBlank()) {
            throw exception(WITHDRAWAL_FAIL_REASON_REQUIRED);
        }

        Long teacherId = row.getTeacherId();
        BigDecimal amount = row.getAmount();

        runUnderTeacherLock(teacherId, () -> transactionTemplate.executeWithoutResult(s -> {
            TeacherWithdrawalDO upd = new TeacherWithdrawalDO();
            upd.setId(id);
            upd.setStatus(WithdrawalStatusEnum.FAILED.getValue());
            upd.setPaidBy(operatorId);
            upd.setPaidAt(LocalDateTime.now());
            // 失败原因记入 paidRemark 字段(spec §4.2 表无独立 failReason 列,复用 paidRemark)
            upd.setPaidRemark(failReason);
            teacherWithdrawalMapper.updateById(upd);
            // 余额回退:pending_withdraw → available
            teacherBalanceService.releaseFromWithdrawal(teacherId, amount);
        }));

        // 主事务 + 锁释放后触发异步通知
        notifyWithdrawalEvent("failed", id);
    }

    // ======================== 查询 ========================

    @Override
    public PageResult<TeacherWithdrawalDO> getPageForTeacher(Long teacherId, String status, PageParam pageParam) {
        return teacherWithdrawalMapper.selectPageForTeacher(teacherId, status, pageParam);
    }

    @Override
    public PageResult<TeacherWithdrawalDO> getPageForAdmin(WithdrawalPageReqVO req) {
        return teacherWithdrawalMapper.selectPageForAdmin(req);
    }

    @Override
    public TeacherWithdrawalDO getDetail(Long id) {
        return teacherWithdrawalMapper.selectById(id);
    }

    @Override
    @LogRecord(type = LogRecordConstants.EDU_WITHDRAWAL_TYPE,
            subType = LogRecordConstants.EDU_WITHDRAWAL_REVEAL_PAYEE_SUB_TYPE,
            bizNo = "{{#id}}",
            success = LogRecordConstants.EDU_WITHDRAWAL_REVEAL_PAYEE_SUCCESS)
    public String getUnmaskedPayeeInfo(Long id, Long adminId) {
        TeacherWithdrawalDO row = teacherWithdrawalMapper.selectById(id);
        if (row == null) {
            throw exception(WITHDRAWAL_NOT_FOUND);
        }

        // 一次性审计日志 — 走 @LogRecord(spec §6.5 ⭐),由 mzt-biz-log AOP 拦截,
        // 落到 system_operate_log 表;userId / userIp / userAgent 由 LogRecordServiceImpl 兜底补全。
        // teacherId 放入上下文供 success 模板 {{#teacherId}} 渲染。
        LogRecordContext.putVariable("teacherId", row.getTeacherId());

        // payeeInfo 经 AesEncryptTypeHandler 透明解密,直接返回
        return row.getPayeeInfo();
    }

    // ======================== 通知 hook(D3)========================

    /**
     * 主事务 commit 后触发提现状态通知。NotificationService.sendForWithdrawalEvent
     * 已 {@code @Async} 注解 + 内部 try-catch,任何失败都不影响主流程。
     *
     * <p>此处仅做防御性 try-catch + 重新拉 DO(确保拿到 commit 后的最新字段,如 paidRemark / rejectReason)。
     *
     * @param event 状态事件 applied / approved / paid / rejected / failed
     * @param id    withdrawal id
     */
    private void notifyWithdrawalEvent(String event, Long id) {
        if (id == null) return;
        try {
            TeacherWithdrawalDO row = teacherWithdrawalMapper.selectById(id);
            if (row == null) {
                log.warn("[withdrawal-notify] row not found after commit, event={} id={}", event, id);
                return;
            }
            notificationService.sendForWithdrawalEvent(event, row);
        } catch (Exception e) {
            // 通知触发本身不应导致主事务回滚,Notification 内部已 @Async + try-catch 兜底,
            // 此处仅记日志防止异常逃逸到 controller(若 @Async 代理失败 fallback 到同步执行)
            log.error("[withdrawal-notify] trigger fail event={} id={}", event, id, e);
        }
    }

    // ======================== 锁工具 ========================

    /**
     * 持 Redisson 教师锁执行任务(lock-then-tx 外层)。
     *
     * @param teacherId 教师 id(单一锁前缀 spec §3.3)
     * @param work      要执行的任务(内部应自行启 TransactionTemplate)
     */
    private void runUnderTeacherLock(Long teacherId, Runnable work) {
        RLock lock = redissonClient.getLock(TEACHER_LOCK_PREFIX + teacherId);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw exception(WITHDRAWAL_LOCK_TIMEOUT);
            }
            work.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(WITHDRAWAL_LOCK_TIMEOUT);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
