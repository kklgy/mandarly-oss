package com.mandarly.boot.module.edu.service.balance;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherBalanceMapper;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TeacherBalanceServiceImpl TDD 单元测试
 *
 * <p>覆盖:
 * <ol>
 *   <li>原 M4 rebuild 4 case(field rename frozen_usd → frozen_t7_usd 后保留)</li>
 *   <li>M6 §8.2 case 11-14:freeze/unfreeze 不变量 / hold/release 不变量 /
 *       乐观锁冲突 throw / refund_deduct 两分支(available + frozen,允许负数)</li>
 * </ol>
 */
class TeacherBalanceServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherBalanceServiceImpl teacherBalanceService;

    @Mock
    private TeacherIncomeMapper teacherIncomeMapper;

    @Mock
    private TeacherBalanceMapper teacherBalanceMapper;

    // ======================== helpers ========================

    private Map<String, Object> aggregateRow(Long teacherId, BigDecimal available, BigDecimal frozen, BigDecimal total) {
        Map<String, Object> row = new HashMap<>();
        row.put("teacher_id", teacherId);
        row.put("available_usd", available);
        row.put("frozen_t7_usd", frozen);
        row.put("total_earned_usd", total);
        return row;
    }

    private TeacherBalanceDO existingBalance(Long teacherId,
                                              BigDecimal frozenT7, BigDecimal available,
                                              BigDecimal pending, BigDecimal totalEarned,
                                              BigDecimal totalWithdrawn, Integer version) {
        TeacherBalanceDO b = new TeacherBalanceDO();
        b.setTeacherId(teacherId);
        b.setFrozenT7Usd(frozenT7);
        b.setAvailableUsd(available);
        b.setPendingWithdrawUsd(pending);
        b.setTotalEarnedUsd(totalEarned);
        b.setTotalWithdrawnUsd(totalWithdrawn);
        b.setCurrency("USD");
        b.setVersion(version);
        return b;
    }

    // ======================== M4 rebuild tests(field rename 保留行为)========================

    /** case 1: settledAt <= now-7d → available */
    @Test
    void rebuild_frozenBoundary_settledAt7DaysAgo_isAvailable() {
        Long teacherId = 100L;
        Map<String, Object> row = aggregateRow(teacherId, new BigDecimal("50.00"), BigDecimal.ZERO, new BigDecimal("50.00"));
        when(teacherIncomeMapper.aggregateByTeachers(eq(List.of(teacherId)), any(LocalDateTime.class)))
                .thenReturn(List.of(row));
        when(teacherBalanceMapper.updateById(any(TeacherBalanceDO.class))).thenReturn(0);
        when(teacherBalanceMapper.insert(any(TeacherBalanceDO.class))).thenReturn(1);

        int count = teacherBalanceService.rebuildBalances(List.of(teacherId));

        assertThat(count).isEqualTo(1);
        verify(teacherBalanceMapper, atLeastOnce()).insert(argThat((TeacherBalanceDO b) ->
                b.getAvailableUsd().compareTo(new BigDecimal("50.00")) == 0
                        && b.getFrozenT7Usd().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    /** case 2: settledAt > now-7d → frozen */
    @Test
    void rebuild_frozenBoundary_settledAt6DaysAgo_isFrozen() {
        Long teacherId = 101L;
        Map<String, Object> row = aggregateRow(teacherId, BigDecimal.ZERO, new BigDecimal("80.00"), new BigDecimal("80.00"));
        when(teacherIncomeMapper.aggregateByTeachers(eq(List.of(teacherId)), any(LocalDateTime.class)))
                .thenReturn(List.of(row));
        when(teacherBalanceMapper.updateById(any(TeacherBalanceDO.class))).thenReturn(0);
        when(teacherBalanceMapper.insert(any(TeacherBalanceDO.class))).thenReturn(1);

        teacherBalanceService.rebuildBalances(List.of(teacherId));

        verify(teacherBalanceMapper, atLeastOnce()).insert(argThat((TeacherBalanceDO b) ->
                b.getFrozenT7Usd().compareTo(new BigDecimal("80.00")) == 0
                        && b.getAvailableUsd().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    /** case 3: refund_deduct 负数 → 减 available */
    @Test
    void rebuild_negativeRefundDeduct_subtractsFromAvailable() {
        Long teacherId = 102L;
        Map<String, Object> row = aggregateRow(teacherId, new BigDecimal("30.00"), BigDecimal.ZERO, new BigDecimal("50.00"));
        when(teacherIncomeMapper.aggregateByTeachers(eq(List.of(teacherId)), any(LocalDateTime.class)))
                .thenReturn(List.of(row));
        when(teacherBalanceMapper.updateById(any(TeacherBalanceDO.class))).thenReturn(0);
        when(teacherBalanceMapper.insert(any(TeacherBalanceDO.class))).thenReturn(1);

        teacherBalanceService.rebuildBalances(List.of(teacherId));

        verify(teacherBalanceMapper, atLeastOnce()).insert(argThat((TeacherBalanceDO b) ->
                b.getAvailableUsd().compareTo(new BigDecimal("30.00")) == 0
        ));
    }

    /** case 4: 幂等 — 连续 rebuild 两次,结果一致 */
    @Test
    void rebuild_idempotent_runTwiceSameResult() {
        Long teacherId = 103L;
        Map<String, Object> row = aggregateRow(teacherId, new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("100.00"));
        when(teacherIncomeMapper.aggregateByTeachers(eq(List.of(teacherId)), any(LocalDateTime.class)))
                .thenReturn(List.of(row));
        when(teacherBalanceMapper.updateById(any(TeacherBalanceDO.class)))
                .thenReturn(0)
                .thenReturn(1);
        when(teacherBalanceMapper.insert(any(TeacherBalanceDO.class))).thenReturn(1);

        int count1 = teacherBalanceService.rebuildBalances(List.of(teacherId));
        int count2 = teacherBalanceService.rebuildBalances(List.of(teacherId));

        assertThat(count1).isEqualTo(1);
        assertThat(count2).isEqualTo(1);
        verify(teacherBalanceMapper, times(1)).insert(any(TeacherBalanceDO.class));
    }

    // ======================== M6 §8.2 case 11-14 ========================

    /**
     * case 11: freeze + unfreeze 不变量
     * - freeze($50): frozen_t7 += 50, total_earned += 50
     * - unfreeze($50): frozen_t7 -= 50, available += 50
     */
    @Test
    void freeze_then_unfreeze_invariant() {
        Long teacherId = 200L;
        TeacherBalanceDO existing = existingBalance(teacherId,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(teacherBalanceMapper.selectById(teacherId)).thenReturn(existing);
        when(teacherBalanceMapper.updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                any(), any(), any(), any(), any())).thenReturn(1);

        // freeze
        teacherBalanceService.freeze(teacherId, new BigDecimal("50.00"), "USD");
        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                eq(new BigDecimal("50.00")),                              // deltaFrozenT7 = +50
                eq(BigDecimal.ZERO),                                       // deltaAvailable = 0
                eq(BigDecimal.ZERO),                                       // deltaPending = 0
                eq(new BigDecimal("50.00")),                              // deltaTotalEarned = +50
                eq(BigDecimal.ZERO));                                      // deltaTotalWithdrawn = 0

        // unfreeze
        teacherBalanceService.unfreeze(teacherId, new BigDecimal("50.00"));
        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                eq(new BigDecimal("-50.00")),                             // deltaFrozenT7 = -50
                eq(new BigDecimal("50.00")),                              // deltaAvailable = +50
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO));
    }

    /**
     * case 12: hold + release 不变量
     * - holdForWithdrawal($100): available -= 100, pending_withdraw += 100
     * - releaseFromWithdrawal($100): pending_withdraw -= 100, available += 100
     */
    @Test
    void hold_then_release_invariant() {
        Long teacherId = 201L;
        TeacherBalanceDO existing = existingBalance(teacherId,
                BigDecimal.ZERO, new BigDecimal("200.00"), BigDecimal.ZERO,
                new BigDecimal("200.00"), BigDecimal.ZERO, 3);
        when(teacherBalanceMapper.selectById(teacherId)).thenReturn(existing);
        when(teacherBalanceMapper.updateBalanceWithOptimisticLock(
                eq(teacherId), eq(3),
                any(), any(), any(), any(), any())).thenReturn(1);

        teacherBalanceService.holdForWithdrawal(teacherId, new BigDecimal("100.00"));
        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(3),
                eq(BigDecimal.ZERO),
                eq(new BigDecimal("-100.00")),                            // available -= 100
                eq(new BigDecimal("100.00")),                             // pending += 100
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO));

        teacherBalanceService.releaseFromWithdrawal(teacherId, new BigDecimal("100.00"));
        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(3),
                eq(BigDecimal.ZERO),
                eq(new BigDecimal("100.00")),                             // available += 100
                eq(new BigDecimal("-100.00")),                            // pending -= 100
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO));
    }

    /**
     * case 13: 乐观锁冲突 throw
     * - updateBalanceWithOptimisticLock 返回 0 → throw BALANCE_VERSION_CONFLICT
     */
    @Test
    void optimistic_lock_conflict_throws() {
        Long teacherId = 202L;
        TeacherBalanceDO existing = existingBalance(teacherId,
                BigDecimal.ZERO, new BigDecimal("100.00"), BigDecimal.ZERO,
                new BigDecimal("100.00"), BigDecimal.ZERO, 5);
        when(teacherBalanceMapper.selectById(teacherId)).thenReturn(existing);
        when(teacherBalanceMapper.updateBalanceWithOptimisticLock(
                eq(teacherId), eq(5),
                any(), any(), any(), any(), any())).thenReturn(0); // 并发冲突

        assertThatThrownBy(() -> teacherBalanceService.holdForWithdrawal(teacherId, new BigDecimal("50.00")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("并发冲突");
    }

    /**
     * case 14a: deductFromAvailable 正常分支(available 够)
     * - available=100, deduct=30 → available -= 30(total_earned 不动 — 退款扣回不该减「累计赚总额」)
     */
    @Test
    void deductFromAvailable_normal_subtracts() {
        Long teacherId = 203L;
        TeacherBalanceDO existing = existingBalance(teacherId,
                BigDecimal.ZERO, new BigDecimal("100.00"), BigDecimal.ZERO,
                new BigDecimal("100.00"), BigDecimal.ZERO, 1);
        when(teacherBalanceMapper.selectById(teacherId)).thenReturn(existing);
        when(teacherBalanceMapper.updateBalanceWithOptimisticLock(
                eq(teacherId), eq(1),
                any(), any(), any(), any(), any())).thenReturn(1);

        teacherBalanceService.deductFromAvailable(teacherId, new BigDecimal("30.00"));

        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(1),
                eq(BigDecimal.ZERO),
                eq(new BigDecimal("-30.00")),                             // available -= 30
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),                                       // B2 fix:total_earned 不动
                eq(BigDecimal.ZERO));
    }

    /**
     * case 14b: deductFromAvailable 余额不够 → 仍 proceed(允许负数)+ 不抛
     * - available=10, deduct=30 → 仍调 update;不抛异常(允许 balance.available 变 -20)
     */
    @Test
    void deductFromAvailable_insufficient_allowsNegative_noThrow() {
        Long teacherId = 204L;
        TeacherBalanceDO existing = existingBalance(teacherId,
                BigDecimal.ZERO, new BigDecimal("10.00"), BigDecimal.ZERO,
                new BigDecimal("10.00"), BigDecimal.ZERO, 0);
        when(teacherBalanceMapper.selectById(teacherId)).thenReturn(existing);
        when(teacherBalanceMapper.updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                any(), any(), any(), any(), any())).thenReturn(1);

        // 不应抛异常
        teacherBalanceService.deductFromAvailable(teacherId, new BigDecimal("30.00"));

        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                eq(BigDecimal.ZERO),
                eq(new BigDecimal("-30.00")),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),                                       // B2 fix:total_earned 不动
                eq(BigDecimal.ZERO));
    }

    /**
     * case 14c: deductFromFrozen 正常 — frozen_t7 -= amount(total_earned 不动)
     */
    @Test
    void deductFromFrozen_normal_subtracts() {
        Long teacherId = 205L;
        TeacherBalanceDO existing = existingBalance(teacherId,
                new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("50.00"), BigDecimal.ZERO, 2);
        when(teacherBalanceMapper.selectById(teacherId)).thenReturn(existing);
        when(teacherBalanceMapper.updateBalanceWithOptimisticLock(
                eq(teacherId), eq(2),
                any(), any(), any(), any(), any())).thenReturn(1);

        teacherBalanceService.deductFromFrozen(teacherId, new BigDecimal("20.00"));

        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(2),
                eq(new BigDecimal("-20.00")),                             // frozen_t7 -= 20
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),                                       // B2 fix:total_earned 不动
                eq(BigDecimal.ZERO));
    }

    /**
     * A3 follow-up:ensureBalance race 修复
     *
     * <p>场景:两条并发 freeze 同时遇到 balance 不存在,各自尝试 insert,其中第二条命中 unique key
     * 拿到 DuplicateKeyException;按 spec(M6-plan Step 4.5)应捕获后 re-select 返回另一线程已写入的行。
     */
    @Test
    void ensureBalance_duplicateKey_reSelects() {
        Long teacherId = 207L;
        TeacherBalanceDO otherThreadInserted = existingBalance(teacherId,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0);

        // 第一次 selectById 返回 null(未建);第二次返回另一线程已 insert 的行
        when(teacherBalanceMapper.selectById(teacherId))
                .thenReturn(null)
                .thenReturn(otherThreadInserted);
        // insert 抛 DuplicateKey(另一线程先一步插入)
        when(teacherBalanceMapper.insert(any(TeacherBalanceDO.class)))
                .thenThrow(new DuplicateKeyException("uk_teacher_balance"));
        // optimistic lock update 应基于另一线程插入的 version=0 行成功
        when(teacherBalanceMapper.updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                any(), any(), any(), any(), any())).thenReturn(1);

        // 应不抛 DuplicateKeyException — 已经被 catch + re-select
        teacherBalanceService.freeze(teacherId, new BigDecimal("15.00"), "USD");

        // 验证:selectById 至少 2 次(一次 null,一次 re-select),insert 1 次,update 1 次
        verify(teacherBalanceMapper, atLeast(2)).selectById(teacherId);
        verify(teacherBalanceMapper).insert(any(TeacherBalanceDO.class));
        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                eq(new BigDecimal("15.00")),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(new BigDecimal("15.00")),
                eq(BigDecimal.ZERO));
    }

    /**
     * 首次 freeze:no existing balance → 惰性 insert(version=0)+ optimistic update
     */
    @Test
    void freeze_firstTime_lazyCreate() {
        Long teacherId = 206L;
        // selectById 第一次返回 null(不存在)→ ensureBalance insert + 返回新建对象 version=0
        when(teacherBalanceMapper.selectById(teacherId)).thenReturn(null);
        when(teacherBalanceMapper.insert(any(TeacherBalanceDO.class))).thenReturn(1);
        when(teacherBalanceMapper.updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                any(), any(), any(), any(), any())).thenReturn(1);

        teacherBalanceService.freeze(teacherId, new BigDecimal("25.00"), "USD");

        // verify lazy insert was called with version=0 + currency=USD
        verify(teacherBalanceMapper).insert(argThat((TeacherBalanceDO b) ->
                b.getTeacherId().equals(teacherId)
                        && b.getVersion() != null && b.getVersion() == 0
                        && "USD".equals(b.getCurrency())));
        verify(teacherBalanceMapper).updateBalanceWithOptimisticLock(
                eq(teacherId), eq(0),
                eq(new BigDecimal("25.00")),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(new BigDecimal("25.00")),
                eq(BigDecimal.ZERO));
    }
}
