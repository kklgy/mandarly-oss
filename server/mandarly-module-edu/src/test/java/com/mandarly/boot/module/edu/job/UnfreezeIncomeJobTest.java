package com.mandarly.boot.module.edu.job;

import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherIncomeDO;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UnfreezeIncomeJob TDD 单元测试(spec §8.2 case 19-21)
 *
 * <p>case 19: scans_frozen_due_and_unfreezes
 * <p>case 20: runs_under_redisson_lock_per_teacher
 * <p>case 21: batch_size_capped_at_1000
 */
class UnfreezeIncomeJobTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UnfreezeIncomeJob job;

    @Mock
    private TeacherIncomeMapper teacherIncomeMapper;

    @Mock
    private TeacherBalanceService teacherBalanceService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private TransactionTemplate transactionTemplate;

    // ======================== helpers ========================

    private TeacherIncomeDO frozenIncome(Long id, Long teacherId, BigDecimal amount) {
        TeacherIncomeDO i = new TeacherIncomeDO();
        i.setId(id);
        i.setTeacherId(teacherId);
        i.setAmountUsd(amount);
        i.setType("normal");
        i.setStatus("frozen");
        i.setCurrency("USD");
        i.setSettledAt(LocalDateTime.now().minusDays(8));
        i.setFrozenUntil(LocalDateTime.now().minusHours(1)); // 已到期
        return i;
    }

    /** 模拟 Redisson 锁:tryLock 返回 true */
    private RLock stubLock() {
        RLock lock = mock(RLock.class);
        try {
            lenient().when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lenient().when(lock.isHeldByCurrentThread()).thenReturn(true);
        return lock;
    }

    /** 模拟 TransactionTemplate.executeWithoutResult — 直接执行回调 */
    private void stubTxTemplate() {
        lenient().doAnswer(inv -> {
            java.util.function.Consumer<Object> callback = inv.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    // ======================== case 19 ========================

    /**
     * case 19: 2 条同教师 income 同时到期 → 解冻后两条都 status='available',
     *           balance unfreeze 一次,总额 30+40=70
     */
    @Test
    void scans_frozen_due_and_unfreezes() throws Exception {
        Long teacherId = 100L;
        TeacherIncomeDO a = frozenIncome(1001L, teacherId, new BigDecimal("30.00"));
        TeacherIncomeDO b = frozenIncome(1002L, teacherId, new BigDecimal("40.00"));

        when(teacherIncomeMapper.selectFrozenDue(any(LocalDateTime.class), eq(1000)))
                .thenReturn(List.of(a, b));

        RLock lock = stubLock();
        when(redissonClient.getLock("teacher:lock:" + teacherId)).thenReturn(lock);
        stubTxTemplate();
        when(teacherIncomeMapper.updateById(any(TeacherIncomeDO.class))).thenReturn(1);

        String result = job.execute(null);

        // 两个 income 各 update 一次 status=available
        verify(teacherIncomeMapper).updateById(org.mockito.ArgumentMatchers.argThat((TeacherIncomeDO upd) ->
                upd.getId().equals(1001L) && "available".equals(upd.getStatus())));
        verify(teacherIncomeMapper).updateById(org.mockito.ArgumentMatchers.argThat((TeacherIncomeDO upd) ->
                upd.getId().equals(1002L) && "available".equals(upd.getStatus())));

        // balance.unfreeze 调用 1 次,金额=70
        verify(teacherBalanceService, times(1)).unfreeze(eq(teacherId), eq(new BigDecimal("70.00")));

        assertThat(result).contains("rows=2").contains("teachers=1");
    }

    // ======================== case 20 ========================

    /**
     * case 20: 验证每个教师都获取了 Redisson 锁 `teacher:lock:{teacherId}`,
     *           且在 balance 写操作之前 tryLock。
     */
    @Test
    void runs_under_redisson_lock_per_teacher() throws Exception {
        TeacherIncomeDO a = frozenIncome(2001L, 200L, new BigDecimal("20.00"));
        TeacherIncomeDO b = frozenIncome(2002L, 201L, new BigDecimal("25.00"));

        when(teacherIncomeMapper.selectFrozenDue(any(LocalDateTime.class), eq(1000)))
                .thenReturn(List.of(a, b));

        RLock lock200 = stubLock();
        RLock lock201 = stubLock();
        when(redissonClient.getLock("teacher:lock:200")).thenReturn(lock200);
        when(redissonClient.getLock("teacher:lock:201")).thenReturn(lock201);
        stubTxTemplate();
        when(teacherIncomeMapper.updateById(any(TeacherIncomeDO.class))).thenReturn(1);

        job.execute(null);

        // 每个教师都 getLock + tryLock + unlock
        verify(redissonClient).getLock("teacher:lock:200");
        verify(redissonClient).getLock("teacher:lock:201");
        verify(lock200, atLeastOnce()).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
        verify(lock201, atLeastOnce()).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
        verify(lock200).unlock();
        verify(lock201).unlock();

        // 余额 unfreeze 按教师各调一次
        verify(teacherBalanceService).unfreeze(eq(200L), eq(new BigDecimal("20.00")));
        verify(teacherBalanceService).unfreeze(eq(201L), eq(new BigDecimal("25.00")));
    }

    // ======================== case 21 ========================

    /**
     * case 21: 扫描器最多返回 1000 行(批处理上限),Job 不能传超过 1000 给 mapper。
     */
    @Test
    void batch_size_capped_at_1000() throws Exception {
        // 模拟 1000 行(同一教师以简化 mock)
        Long teacherId = 300L;
        List<TeacherIncomeDO> rows = new ArrayList<>(1000);
        for (long i = 0; i < 1000; i++) {
            rows.add(frozenIncome(3000L + i, teacherId, new BigDecimal("1.00")));
        }
        when(teacherIncomeMapper.selectFrozenDue(any(LocalDateTime.class), eq(1000)))
                .thenReturn(rows);

        RLock lock = stubLock();
        when(redissonClient.getLock("teacher:lock:" + teacherId)).thenReturn(lock);
        stubTxTemplate();
        when(teacherIncomeMapper.updateById(any(TeacherIncomeDO.class))).thenReturn(1);

        String result = job.execute(null);

        // Mapper 被调用时 limit 必须传 1000(不超过)
        verify(teacherIncomeMapper).selectFrozenDue(any(LocalDateTime.class), eq(1000));
        // 只处理 1000 行(没有第 1001 行)
        verify(teacherIncomeMapper, times(1000)).updateById(any(TeacherIncomeDO.class));
        // balance.unfreeze 调一次:同一教师所有金额合并
        verify(teacherBalanceService, times(1)).unfreeze(eq(teacherId), eq(new BigDecimal("1000.00")));
        assertThat(result).contains("rows=1000").contains("teachers=1");
    }

    /**
     * 没有到期 income → 直接返回,不走锁也不调 balance
     */
    @Test
    void no_due_income_returns_zero() throws Exception {
        when(teacherIncomeMapper.selectFrozenDue(any(LocalDateTime.class), eq(1000)))
                .thenReturn(List.of());

        String result = job.execute(null);

        verify(redissonClient, never()).getLock(any(String.class));
        verify(teacherBalanceService, never()).unfreeze(any(), any());
        assertThat(result).contains("rows=0").contains("teachers=0");
    }
}
