package com.mandarly.boot.module.edu.service.income;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherIncomeDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TeacherIncomeServiceImpl TDD 单元测试
 *
 * <p>覆盖:
 * <ol>
 *   <li>原 M4 settle 6 case</li>
 *   <li>M6 settle 写 status=frozen + frozen_until=settledAt+7d + 联动 balance.freeze</li>
 *   <li>M6 deductForRefund 4 分支:available / frozen / reverted(throw)/ already-withdrawn</li>
 * </ol>
 */
class TeacherIncomeServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherIncomeServiceImpl teacherIncomeService;

    @Mock
    private TeacherIncomeMapper teacherIncomeMapper;

    @Mock
    private CourseOrderMapper courseOrderMapper;

    @Mock
    private TeacherBalanceService teacherBalanceService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private TransactionTemplate transactionTemplate;

    // ======================== helpers ========================

    private CourseOrderDO order(Long id, Long teacherId, String status, boolean freeTrial,
                                 BigDecimal teacherAmount, String settleStatus, String cancelledBy) {
        CourseOrderDO o = new CourseOrderDO();
        o.setId(id);
        o.setTeacherId(teacherId);
        o.setStatus(status);
        o.setIsFreeTrial(freeTrial);
        o.setTeacherAmount(teacherAmount);
        o.setTeacherAmountCurrency("usd");
        o.setTeacherSettleStatus(settleStatus);
        o.setCancelledBy(cancelledBy);
        o.setFinishedAt(LocalDateTime.now());
        o.setStudentPackageId(100L);
        return o;
    }

    private TeacherIncomeDO income(Long id, Long teacherId, Long courseOrderId,
                                    BigDecimal amount, String type, String status) {
        TeacherIncomeDO i = new TeacherIncomeDO();
        i.setId(id);
        i.setTeacherId(teacherId);
        i.setCourseOrderId(courseOrderId);
        i.setAmountUsd(amount);
        i.setType(type);
        i.setStatus(status);
        i.setSettledAt(LocalDateTime.now());
        i.setCurrency("USD");
        return i;
    }

    /** 模拟 Redisson 锁:tryLock 永远返回 true,isHeldByCurrentThread 返回 true(lenient — finally 内才用到) */
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

    // ======================== M4 原测试(保持不变,验证 settle 行为)========================

    /** case 1: normal 课 → 写正数 income,M6 加 status=frozen 断言 */
    @Test
    void settle_normalCourse_writesPositiveIncome() {
        Long courseOrderId = 1L;
        CourseOrderDO co = order(courseOrderId, 10L, "finished", false,
                new BigDecimal("30.00"), "pending", null);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:10"))).thenReturn(lock);
        when(courseOrderMapper.selectById(courseOrderId)).thenReturn(co);
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(courseOrderMapper.updateById(any(CourseOrderDO.class))).thenReturn(1);

        teacherIncomeService.settle(courseOrderId);

        verify(teacherIncomeMapper).insert(argThat((TeacherIncomeDO ti) ->
                ti.getTeacherId().equals(10L)
                        && ti.getAmountUsd().compareTo(new BigDecimal("30.00")) == 0
                        && "normal".equals(ti.getType())
                        && "frozen".equals(ti.getStatus())
        ));
        verify(teacherBalanceService).freeze(eq(10L), eq(new BigDecimal("30.00")), eq("USD"));
    }

    /** case 2: free_trial 课 → amount=0 + status=frozen */
    @Test
    void settle_freeTrial_amountZero() {
        Long courseOrderId = 2L;
        CourseOrderDO co = order(courseOrderId, 20L, "finished", true,
                new BigDecimal("0.00"), "pending", null);

        when(courseOrderMapper.selectById(courseOrderId)).thenReturn(co);
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(courseOrderMapper.updateById(any(CourseOrderDO.class))).thenReturn(1);

        teacherIncomeService.settle(courseOrderId);

        verify(teacherIncomeMapper).insert(argThat((TeacherIncomeDO ti) ->
                ti.getAmountUsd().compareTo(BigDecimal.ZERO) == 0
                        && "free_trial".equals(ti.getType())
                        && "frozen".equals(ti.getStatus())
        ));
        // amount=0 不联动 balance.freeze
        verify(teacherBalanceService, never()).freeze(any(), any(), any());
    }

    /** case 3: no_show_teacher → amount=0 */
    @Test
    void settle_noShowTeacher_amountZero() {
        Long courseOrderId = 3L;
        CourseOrderDO co = order(courseOrderId, 30L, "cancelled", false,
                new BigDecimal("30.00"), "pending", "teacher");
        co.setAbnormalResolution("no_show_teacher");

        when(courseOrderMapper.selectById(courseOrderId)).thenReturn(co);
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(courseOrderMapper.updateById(any(CourseOrderDO.class))).thenReturn(1);

        teacherIncomeService.settle(courseOrderId);

        verify(teacherIncomeMapper).insert(argThat((TeacherIncomeDO ti) ->
                ti.getAmountUsd().compareTo(BigDecimal.ZERO) == 0
                        && "frozen".equals(ti.getStatus())
        ));
    }

    /** case 4: no_show_student → 全额 income + 联动 balance.freeze */
    @Test
    void settle_noShowStudent_fullPayment() {
        Long courseOrderId = 4L;
        CourseOrderDO co = order(courseOrderId, 40L, "cancelled", false,
                new BigDecimal("30.00"), "pending", "student");
        co.setAbnormalResolution("no_show_student");

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:40"))).thenReturn(lock);
        when(courseOrderMapper.selectById(courseOrderId)).thenReturn(co);
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(courseOrderMapper.updateById(any(CourseOrderDO.class))).thenReturn(1);

        teacherIncomeService.settle(courseOrderId);

        verify(teacherIncomeMapper).insert(argThat((TeacherIncomeDO ti) ->
                ti.getAmountUsd().compareTo(new BigDecimal("30.00")) == 0
        ));
        verify(teacherBalanceService).freeze(eq(40L), eq(new BigDecimal("30.00")), eq("USD"));
    }

    /** case 5: duplicate — DuplicateKeyException 视为 success(静默忽略),不联动 balance */
    @Test
    void settle_duplicateOrder_silentlyIgnored_viaUniqueConstraint() {
        Long courseOrderId = 5L;
        CourseOrderDO co = order(courseOrderId, 50L, "finished", false,
                new BigDecimal("30.00"), "pending", null);

        when(courseOrderMapper.selectById(courseOrderId)).thenReturn(co);
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class)))
                .thenThrow(new DuplicateKeyException("unique constraint income_course_order_id"));
        when(courseOrderMapper.updateById(any(CourseOrderDO.class))).thenReturn(1);

        teacherIncomeService.settle(courseOrderId);
        verify(teacherIncomeMapper).insert(any(TeacherIncomeDO.class));
        // DuplicateKeyException 后不联动 balance.freeze(防重复入账)
        verify(teacherBalanceService, never()).freeze(any(), any(), any());
    }

    /** case 6: settle 后 UPDATE course_order.teacher_settle_status='settled' */
    @Test
    void settle_updatesCourseOrderTeacherSettleStatus() {
        Long courseOrderId = 6L;
        CourseOrderDO co = order(courseOrderId, 60L, "finished", false,
                new BigDecimal("25.00"), "pending", null);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:60"))).thenReturn(lock);
        when(courseOrderMapper.selectById(courseOrderId)).thenReturn(co);
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(courseOrderMapper.updateById(any(CourseOrderDO.class))).thenReturn(1);

        teacherIncomeService.settle(courseOrderId);

        verify(courseOrderMapper).updateById(argThat((CourseOrderDO upd) ->
                "settled".equals(upd.getTeacherSettleStatus())
        ));
    }

    // ======================== M6 新增 ========================

    /**
     * settle 写 frozen_until = settledAt + 7d(spec §3.4)
     */
    @Test
    void settle_writes_status_frozen_with_frozen_until() {
        Long courseOrderId = 100L;
        CourseOrderDO co = order(courseOrderId, 110L, "finished", false,
                new BigDecimal("40.00"), "pending", null);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:110"))).thenReturn(lock);
        when(courseOrderMapper.selectById(courseOrderId)).thenReturn(co);
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(courseOrderMapper.updateById(any(CourseOrderDO.class))).thenReturn(1);

        LocalDateTime before = LocalDateTime.now();
        teacherIncomeService.settle(courseOrderId);
        LocalDateTime after = LocalDateTime.now();

        verify(teacherIncomeMapper).insert(argThat((TeacherIncomeDO ti) -> {
            if (!"frozen".equals(ti.getStatus())) return false;
            if (ti.getFrozenUntil() == null || ti.getSettledAt() == null) return false;
            long days = ChronoUnit.DAYS.between(ti.getSettledAt(), ti.getFrozenUntil());
            if (days != 7) return false;
            // settledAt should be in [before, after]
            return !ti.getSettledAt().isBefore(before) && !ti.getSettledAt().isAfter(after);
        }));
    }

    /**
     * deductForRefund 原 income status=available → 调 deductFromAvailable + 写 refund_deduct + 原行 reverted
     */
    @Test
    void deduct_for_refund_original_available_branch() {
        Long courseOrderId = 200L;
        Long refundId = 9001L;
        TeacherIncomeDO origin = income(2001L, 210L, courseOrderId,
                new BigDecimal("30.00"), "normal", "available");

        RLock lock = stubLock();
        when(teacherIncomeMapper.selectByOrderIdAndType(courseOrderId, "normal")).thenReturn(origin);
        when(redissonClient.getLock(eq("teacher:lock:210"))).thenReturn(lock);
        stubTxTemplate();
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(teacherIncomeMapper.updateById(any(TeacherIncomeDO.class))).thenReturn(1);

        teacherIncomeService.deductForRefund(courseOrderId, refundId, BigDecimal.ONE);

        verify(teacherBalanceService).deductFromAvailable(eq(210L), eq(new BigDecimal("30.00")));
        verify(teacherBalanceService, never()).deductFromFrozen(any(), any());
        verify(teacherIncomeMapper).insert(argThat((TeacherIncomeDO ti) ->
                "refund_deduct".equals(ti.getType())
                        && ti.getAmountUsd().compareTo(new BigDecimal("-30.00")) == 0
                        && "available".equals(ti.getStatus())
                        && ti.getRelatedIncomeId().equals(2001L)
                        && ti.getRefundId().equals(refundId)
        ));
        verify(teacherIncomeMapper).updateById(argThat((TeacherIncomeDO upd) ->
                upd.getId().equals(2001L) && "reverted".equals(upd.getStatus())
        ));
    }

    /**
     * deductForRefund 原 income status=frozen → 调 deductFromFrozen
     */
    @Test
    void deduct_for_refund_original_frozen_branch() {
        Long courseOrderId = 201L;
        Long refundId = 9002L;
        TeacherIncomeDO origin = income(2002L, 211L, courseOrderId,
                new BigDecimal("25.00"), "normal", "frozen");

        RLock lock = stubLock();
        when(teacherIncomeMapper.selectByOrderIdAndType(courseOrderId, "normal")).thenReturn(origin);
        when(redissonClient.getLock(eq("teacher:lock:211"))).thenReturn(lock);
        stubTxTemplate();
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(teacherIncomeMapper.updateById(any(TeacherIncomeDO.class))).thenReturn(1);

        teacherIncomeService.deductForRefund(courseOrderId, refundId, BigDecimal.ONE);

        verify(teacherBalanceService).deductFromFrozen(eq(211L), eq(new BigDecimal("25.00")));
        verify(teacherBalanceService, never()).deductFromAvailable(any(), any());
        verify(teacherIncomeMapper).updateById(argThat((TeacherIncomeDO upd) ->
                upd.getId().equals(2002L) && "reverted".equals(upd.getStatus())
        ));
    }

    /**
     * deductForRefund 原 income status=reverted → throw 幂等异常,不写流水 / 不动 balance
     */
    @Test
    void deduct_for_refund_original_reverted_throws() {
        Long courseOrderId = 202L;
        Long refundId = 9003L;
        TeacherIncomeDO origin = income(2003L, 212L, courseOrderId,
                new BigDecimal("20.00"), "normal", "reverted");

        RLock lock = stubLock();
        when(teacherIncomeMapper.selectByOrderIdAndType(courseOrderId, "normal")).thenReturn(origin);
        when(redissonClient.getLock(eq("teacher:lock:212"))).thenReturn(lock);
        stubTxTemplate();

        assertThatThrownBy(() -> teacherIncomeService.deductForRefund(courseOrderId, refundId, BigDecimal.ONE))
                .isInstanceOf(ServiceException.class);

        verify(teacherBalanceService, never()).deductFromAvailable(any(), any());
        verify(teacherBalanceService, never()).deductFromFrozen(any(), any());
        verify(teacherIncomeMapper, never()).insert(any(TeacherIncomeDO.class));
    }

    /**
     * deductForRefund 已被提走分支:原 status=available 但 balance.available 不够;
     * 仍走 deductFromAvailable(允许负数,balance 内部 log.error);不抛
     */
    @Test
    void deduct_for_refund_original_already_withdrawn_logs_negative() {
        Long courseOrderId = 203L;
        Long refundId = 9004L;
        TeacherIncomeDO origin = income(2004L, 213L, courseOrderId,
                new BigDecimal("50.00"), "normal", "available");

        RLock lock = stubLock();
        when(teacherIncomeMapper.selectByOrderIdAndType(courseOrderId, "normal")).thenReturn(origin);
        when(redissonClient.getLock(eq("teacher:lock:213"))).thenReturn(lock);
        stubTxTemplate();
        // mock balance.deductFromAvailable 不抛(教师 balance 内部允许负数,只 log.error)
        doNothing().when(teacherBalanceService).deductFromAvailable(eq(213L), eq(new BigDecimal("50.00")));
        when(teacherIncomeMapper.insert(any(TeacherIncomeDO.class))).thenReturn(1);
        when(teacherIncomeMapper.updateById(any(TeacherIncomeDO.class))).thenReturn(1);

        // 不应抛
        teacherIncomeService.deductForRefund(courseOrderId, refundId, BigDecimal.ONE);

        verify(teacherBalanceService).deductFromAvailable(eq(213L), eq(new BigDecimal("50.00")));
        verify(teacherIncomeMapper).insert(argThat((TeacherIncomeDO ti) ->
                "refund_deduct".equals(ti.getType())
        ));
    }
}
