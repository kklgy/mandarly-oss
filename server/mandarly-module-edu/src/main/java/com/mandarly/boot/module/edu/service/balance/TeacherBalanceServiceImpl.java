package com.mandarly.boot.module.edu.service.balance;

import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherBalanceMapper;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import com.mandarly.boot.module.edu.dal.mysql.withdrawal.TeacherWithdrawalMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.BALANCE_VERSION_CONFLICT;

/**
 * 教师余额服务实现
 *
 * <p>rebuildBalances(M4 遗留):仍保留用于初始化 / 手动 rebuild;Job 主入口 {@link #rebuildAll()} 已改为对账告警。
 *
 * <p>{@link #rebuildAll()}(A5 重写):每日 Job 调用 — 按 spec §4.3 三处查询比对 balance 行,
 * 不一致 → log.error(可告警),不自动改余额,由 admin 人工介入。
 *
 * <p>M6 §3.6 新增 7 个事务方法,全部走乐观锁。调用方负责持有 Redisson `teacher:lock:{teacherId}` 锁。
 */
@Slf4j
@Service
public class TeacherBalanceServiceImpl implements TeacherBalanceService {

    public static final int FROZEN_DAYS = 7;

    @Resource
    private TeacherIncomeMapper teacherIncomeMapper;

    @Resource
    private TeacherBalanceMapper teacherBalanceMapper;

    @Resource
    private TeacherWithdrawalMapper teacherWithdrawalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rebuildBalances(List<Long> teacherIds) {
        if (teacherIds == null || teacherIds.isEmpty()) return 0;

        LocalDateTime frozenThreshold = LocalDateTime.now().minusDays(FROZEN_DAYS);
        List<Map<String, Object>> rows = teacherIncomeMapper.aggregateByTeachers(teacherIds, frozenThreshold);

        int count = 0;
        for (Map<String, Object> row : rows) {
            Long teacherId = toLong(row.get("teacher_id"));
            BigDecimal available = toBigDecimal(row.get("available_usd"));
            BigDecimal frozen = toBigDecimal(row.get("frozen_t7_usd"));
            BigDecimal total = toBigDecimal(row.get("total_earned_usd"));

            TeacherBalanceDO balance = new TeacherBalanceDO();
            balance.setTeacherId(teacherId);
            balance.setAvailableUsd(available);
            balance.setFrozenT7Usd(frozen);
            balance.setTotalEarnedUsd(total);
            balance.setLastRebuildAt(LocalDateTime.now());

            // UPSERT:先尝试 update,0 rows 说明不存在则 insert
            int updated = teacherBalanceMapper.updateById(balance);
            if (updated == 0) {
                teacherBalanceMapper.insert(balance);
            }
            count++;
        }
        return count;
    }

    /**
     * 对账告警(spec §3.6 + §4.3 / db-design v1.2 §4.3)。
     *
     * <p>不再自动 rebuild balance — 改为按三处查询比对 balance 行,有偏差 → log.error 触发告警,
     * 等 admin 人工 investigate(避免 race 期间自动覆盖正确的余额状态)。
     *
     * <p>遍历过去 1 天有 income 变动的教师 + 所有有 balance 行的教师并集即可(M6 一期数据量小,
     * 直接拿 "近 1 天有变动" 一组就够;二期再做全量分页)。
     *
     * @return 已检查的教师数(无论是否一致)
     */
    @Override
    public int rebuildAll() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<Long> changedTeacherIds = teacherIncomeMapper.selectChangedTeacherIds(since);
        if (changedTeacherIds == null || changedTeacherIds.isEmpty()) {
            log.info("[rebuildAll] no changed teachers in last 24h, skip reconciliation");
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        int checked = 0;
        for (Long teacherId : changedTeacherIds) {
            reconcileBalance(teacherId, now);
            checked++;
        }
        log.info("[rebuildAll] reconciled {} teachers", checked);
        return checked;
    }

    /**
     * 单 teacher 三处对账;不一致 → log.error(可告警)。
     */
    private void reconcileBalance(Long teacherId, LocalDateTime now) {
        TeacherBalanceDO balance = teacherBalanceMapper.selectById(teacherId);
        if (balance == null) {
            // 有 income 变动但 balance 行不存在 — 说明 freeze() 未走通,告警
            log.error("[reconcile] teacherId={} has income changes but teacher_balance row missing — investigate",
                    teacherId);
            return;
        }

        BigDecimal sumNonReverted = nullToZero(teacherIncomeMapper.sumNonRevertedAmount(teacherId));
        BigDecimal sumStillFrozen = nullToZero(teacherIncomeMapper.sumStillFrozenAmount(teacherId, now));
        BigDecimal sumInflightWithdraw = nullToZero(teacherWithdrawalMapper.sumInflightAmount(teacherId));

        BigDecimal expectedTotalNet = sumNonReverted;
        BigDecimal actualTotalNet = nullToZero(balance.getTotalEarnedUsd())
                .subtract(nullToZero(balance.getTotalWithdrawnUsd()));

        BigDecimal expectedFrozen = sumStillFrozen;
        BigDecimal actualFrozen = nullToZero(balance.getFrozenT7Usd());

        BigDecimal expectedPending = sumInflightWithdraw;
        BigDecimal actualPending = nullToZero(balance.getPendingWithdrawUsd());

        boolean mismatch = expectedTotalNet.compareTo(actualTotalNet) != 0
                || expectedFrozen.compareTo(actualFrozen) != 0
                || expectedPending.compareTo(actualPending) != 0;

        if (mismatch) {
            log.error("[reconcile] teacherId={} BALANCE DRIFT detected — "
                            + "totalNet expected={} actual(total_earned-total_withdrawn)={} | "
                            + "frozen_t7 expected={} actual={} | "
                            + "pending_withdraw expected={} actual={}",
                    teacherId,
                    expectedTotalNet, actualTotalNet,
                    expectedFrozen, actualFrozen,
                    expectedPending, actualPending);
        }
    }

    private BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @Override
    public TeacherBalanceDO getBalance(Long teacherId) {
        return teacherBalanceMapper.selectById(teacherId);
    }

    // ======================== M6 新增方法(spec §3.6)========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freeze(Long teacherId, BigDecimal amount, String currency) {
        TeacherBalanceDO balance = ensureBalance(teacherId, currency);
        applyDelta(balance, amount, BigDecimal.ZERO, BigDecimal.ZERO, amount, BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(Long teacherId, BigDecimal amount) {
        TeacherBalanceDO balance = ensureBalance(teacherId, "USD");
        applyDelta(balance, amount.negate(), amount, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void holdForWithdrawal(Long teacherId, BigDecimal amount) {
        TeacherBalanceDO balance = ensureBalance(teacherId, "USD");
        applyDelta(balance, BigDecimal.ZERO, amount.negate(), amount, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseFromWithdrawal(Long teacherId, BigDecimal amount) {
        TeacherBalanceDO balance = ensureBalance(teacherId, "USD");
        applyDelta(balance, BigDecimal.ZERO, amount, amount.negate(), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmWithdrawal(Long teacherId, BigDecimal amount) {
        TeacherBalanceDO balance = ensureBalance(teacherId, "USD");
        applyDelta(balance, BigDecimal.ZERO, BigDecimal.ZERO, amount.negate(), BigDecimal.ZERO, amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductFromAvailable(Long teacherId, BigDecimal amount) {
        TeacherBalanceDO balance = ensureBalance(teacherId, "USD");
        // B2 fix:退款扣回只动 available,不减 total_earned(累计入账总额语义保留,退款用 refund 流水维度独立呈现)
        // 允许 available 转负(spec §3.4 / db-design §1.5.2 case 3:教师已提走 → 报警 + 人工介入)
        BigDecimal current = balance.getAvailableUsd() != null ? balance.getAvailableUsd() : BigDecimal.ZERO;
        if (current.compareTo(amount) < 0) {
            log.error("[deductFromAvailable] teacherId={} balance.available={} < deduct={} — 允许负数,需人工介入",
                    teacherId, current, amount);
        }
        applyDelta(balance, BigDecimal.ZERO, amount.negate(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductFromFrozen(Long teacherId, BigDecimal amount) {
        TeacherBalanceDO balance = ensureBalance(teacherId, "USD");
        BigDecimal current = balance.getFrozenT7Usd() != null ? balance.getFrozenT7Usd() : BigDecimal.ZERO;
        if (current.compareTo(amount) < 0) {
            log.error("[deductFromFrozen] teacherId={} balance.frozen_t7={} < deduct={} — 允许负数,需人工介入",
                    teacherId, current, amount);
        }
        // B2 fix:退款扣回只动 frozen_t7,不减 total_earned(同 deductFromAvailable 语义)
        applyDelta(balance, amount.negate(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // ======================== private helpers ========================

    /**
     * 取或惰性创建 balance 行(version=0)。
     *
     * <p>A5 fix:并发 freeze 同教师都见 null + 都 insert → 第二条 DuplicateKeyException;
     * catch 后 re-select 拿另一线程已写入的行,不再向上抛(spec §3.3 callers 已持 teacher:lock,
     * 本地 DuplicateKey 仅是多进程 / pod 间 race 的兜底)。
     */
    private TeacherBalanceDO ensureBalance(Long teacherId, String currency) {
        TeacherBalanceDO balance = teacherBalanceMapper.selectById(teacherId);
        if (balance != null) {
            return balance;
        }
        balance = new TeacherBalanceDO();
        balance.setTeacherId(teacherId);
        balance.setFrozenT7Usd(BigDecimal.ZERO);
        balance.setAvailableUsd(BigDecimal.ZERO);
        balance.setPendingWithdrawUsd(BigDecimal.ZERO);
        balance.setTotalEarnedUsd(BigDecimal.ZERO);
        balance.setTotalWithdrawnUsd(BigDecimal.ZERO);
        balance.setCurrency(currency != null ? currency : "USD");
        balance.setVersion(0);
        // last_rebuild_at NOT NULL schema 无 default,惰性创建必须显式塞值
        // (M4 引入字段,M6 e2e 实证补)
        balance.setLastRebuildAt(LocalDateTime.now());
        // settle 异步路径无 SecurityContext → 显式兜底 creator/updater(MVP e2e 实证)
        balance.setCreator("system-settle");
        balance.setUpdater("system-settle");
        try {
            teacherBalanceMapper.insert(balance);
            return balance;
        } catch (DuplicateKeyException e) {
            // 另一线程 / pod 抢先 insert 成功 — re-select 拿胜出方的行
            TeacherBalanceDO existing = teacherBalanceMapper.selectById(teacherId);
            if (existing == null) {
                // 极端:DuplicateKey 抛了但 selectById 又返回 null(通常是测试 mock 写错或 DB 异常)
                throw e;
            }
            return existing;
        }
    }

    /**
     * 应用 delta + 乐观锁 update;失败抛 BALANCE_VERSION_CONFLICT
     */
    private void applyDelta(TeacherBalanceDO balance,
                            BigDecimal deltaFrozenT7,
                            BigDecimal deltaAvailable,
                            BigDecimal deltaPending,
                            BigDecimal deltaTotalEarned,
                            BigDecimal deltaTotalWithdrawn) {
        Integer version = balance.getVersion() != null ? balance.getVersion() : 0;
        int updated = teacherBalanceMapper.updateBalanceWithOptimisticLock(
                balance.getTeacherId(), version,
                deltaFrozenT7, deltaAvailable, deltaPending,
                deltaTotalEarned, deltaTotalWithdrawn);
        if (updated == 0) {
            throw exception(BALANCE_VERSION_CONFLICT);
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return new BigDecimal(val.toString());
        return new BigDecimal(val.toString());
    }
}
