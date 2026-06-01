package com.mandarly.boot.module.edu.service.income;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherIncomeDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import com.mandarly.boot.module.edu.enums.booking.OrderStatusEnum;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.INCOME_ALREADY_REVERTED;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.INCOME_LOCK_TIMEOUT;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.INCOME_NOT_FOUND;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.INCOME_STATUS_INVALID;

/**
 * 教师收入结算服务实现(Flow 3 + M6 退款扣回)
 *
 * <p>5 个分支(settle):
 * <ul>
 *   <li>normal:status=finished + !freeTrial → amount=teacherAmount</li>
 *   <li>free_trial:isFreeTrial=true → amount=0</li>
 *   <li>no_show_teacher:abnormalResolution='no_show_teacher' → amount=0</li>
 *   <li>no_show_student:abnormalResolution='no_show_student' → 全额 teacherAmount</li>
 *   <li>duplicate:DuplicateKeyException → 静默 skip(unique constraint 兜底)</li>
 * </ul>
 *
 * <p>M6:settle 时写 status=frozen + frozen_until=settledAt+7d,事务内调 balance.freeze(...)。
 */
@Slf4j
@Service
public class TeacherIncomeServiceImpl implements TeacherIncomeService {

    private static final String TEACHER_LOCK_PREFIX = "teacher:lock:";
    private static final long LOCK_WAIT_SECONDS = 5;
    private static final long LOCK_LEASE_SECONDS = 30;

    @Resource
    private TeacherIncomeMapper teacherIncomeMapper;

    @Resource
    private CourseOrderMapper courseOrderMapper;

    @Resource
    private TeacherBalanceService teacherBalanceService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 自注入 proxy 用于 backfill 循环调 settle — 避免 self-invocation 绕过 {@code @Transactional}。
     * {@code @Lazy} 防字段循环依赖启动失败。
     */
    @Resource
    @Lazy
    private TeacherIncomeService self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settle(Long courseOrderId) {
        CourseOrderDO co = courseOrderMapper.selectById(courseOrderId);
        if (co == null) {
            log.warn("[settle] courseOrder not found: {}", courseOrderId);
            return;
        }

        // 计算 income amount + type
        BigDecimal amount;
        String type;

        if (Boolean.TRUE.equals(co.getIsFreeTrial())) {
            // free_trial:amount=0
            amount = BigDecimal.ZERO;
            type = "free_trial";
        } else if ("no_show_teacher".equals(co.getAbnormalResolution())) {
            // 教师缺席:0
            amount = BigDecimal.ZERO;
            type = "normal";
        } else if ("no_show_student".equals(co.getAbnormalResolution())) {
            // 学生缺席:全额
            amount = co.getTeacherAmount() != null ? co.getTeacherAmount() : BigDecimal.ZERO;
            type = "normal";
        } else {
            // normal
            amount = co.getTeacherAmount() != null ? co.getTeacherAmount() : BigDecimal.ZERO;
            type = "normal";
        }

        // INSERT teacher_income(catch DuplicateKeyException 当 success)
        boolean inserted = false;
        LocalDateTime settledAt = LocalDateTime.now();
        try {
            TeacherIncomeDO income = new TeacherIncomeDO();
            income.setTeacherId(co.getTeacherId());
            income.setCourseOrderId(courseOrderId);
            income.setAmountUsd(amount);
            income.setType(type);
            income.setSettledAt(settledAt);
            // M6:status=frozen,frozen_until = settledAt + 7d(normal / free_trial 同处理,spec §3.4)
            income.setStatus("frozen");
            income.setCurrency("USD");
            income.setFrozenUntil(settledAt.plusDays(TeacherBalanceServiceImpl.FROZEN_DAYS));
            // settle 走 @Async listener,无 SecurityContext → MetaObjectHandler 填不上 creator/updater
            // 显式兜底,避免 NOT NULL 约束失败(memory: c34a6e0 D19 同根子)
            income.setCreator("system-settle");
            income.setUpdater("system-settle");
            teacherIncomeMapper.insert(income);
            inserted = true;
        } catch (DuplicateKeyException e) {
            log.info("[settle] courseOrderId={} already settled(DuplicateKey), skip insert", courseOrderId);
        }

        // UPDATE course_order.teacher_settle_status='settled'
        CourseOrderDO upd = new CourseOrderDO();
        upd.setId(courseOrderId);
        upd.setTeacherSettleStatus("settled");
        // settle 异步无 SecurityContext,显式塞 updater 避免 NOT NULL
        upd.setUpdater("system-settle");
        courseOrderMapper.updateById(upd);

        // M6:联动 balance.freeze(amount)(仅 amount > 0 时,避免 free_trial / no_show_teacher 0 元写无意义流水)
        // spec §3.3 严格单锁不变量:同教师所有余额相关写操作必须持 teacher:lock:{teacherId},否则与 UnfreezeIncomeJob /
        // applyWithdrawal / deductForRefund 等其他余额写路径产生竞态。teacher_income INSERT 留在锁外 — 它 append-only
        // + 有 unique constraint 兜底,本身无竞态。
        if (inserted && amount.compareTo(BigDecimal.ZERO) > 0) {
            Long teacherId = co.getTeacherId();
            RLock lock = redissonClient.getLock(TEACHER_LOCK_PREFIX + teacherId);
            boolean locked = false;
            try {
                locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
                if (!locked) {
                    throw exception(INCOME_LOCK_TIMEOUT);
                }
                teacherBalanceService.freeze(teacherId, amount, "USD");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw exception(INCOME_LOCK_TIMEOUT);
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * 兜底扫描 {@code status='finished' AND teacher_settle_status='pending'} 的 course_order 补录结算。
     *
     * <p>触发场景:LCIC RoomEnd webhook 丢失 / IncomeSettleListener 异步异常 / 服务重启。
     * 单批最多 1000 单,失败单 log.error 但继续下一条,返回成功结算条数。
     */
    @Override
    public int backfillFinishedOrders() {
        List<CourseOrderDO> orders = courseOrderMapper.selectList(
                Wrappers.<CourseOrderDO>lambdaQuery()
                        .eq(CourseOrderDO::getStatus, OrderStatusEnum.FINISHED.getCode())
                        .eq(CourseOrderDO::getTeacherSettleStatus, "pending")
                        .orderByAsc(CourseOrderDO::getId)
                        .last("LIMIT 1000"));
        if (orders.isEmpty()) {
            log.info("[backfillFinishedOrders] no pending settle orders");
            return 0;
        }
        int settled = 0;
        for (CourseOrderDO co : orders) {
            try {
                self.settle(co.getId());
                settled++;
            } catch (Exception e) {
                log.error("[backfillFinishedOrders] orderId={} settle failed,继续下一条", co.getId(), e);
            }
        }
        log.info("[backfillFinishedOrders] scanned={} settled={}", orders.size(), settled);
        return settled;
    }

    /**
     * 退款扣回(spec §3.4)。
     *
     * <p>4 分支(按原 normal income.status):
     * <ol>
     *   <li>available → balance.deductFromAvailable(可能允许负数 + 告警)</li>
     *   <li>frozen → balance.deductFromFrozen</li>
     *   <li>reverted → throw INCOME_ALREADY_REVERTED(幂等防御)</li>
     *   <li>else(未知) → throw INCOME_STATUS_INVALID 兜底</li>
     * </ol>
     *
     * <p>事务内:写 refund_deduct 负数行 + 原行 status=reverted。
     * Redisson 锁 `teacher:lock:{teacherId}`(spec §3.3)。
     */
    @Override
    public void deductForRefund(Long courseOrderId, Long refundId, BigDecimal ratio) {
        if (ratio == null) {
            ratio = BigDecimal.ZERO;
        }
        // 先查原 income(锁前查,不需要事务)
        TeacherIncomeDO origin = teacherIncomeMapper.selectByOrderIdAndType(courseOrderId, "normal");
        if (origin == null) {
            throw exception(INCOME_NOT_FOUND);
        }
        Long teacherId = origin.getTeacherId();

        RLock lock = redissonClient.getLock(TEACHER_LOCK_PREFIX + teacherId);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw exception(INCOME_LOCK_TIMEOUT); // 锁等不到 — 通常说明并发处理中
            }
            // 锁内启事务(用 TransactionTemplate 避免同类 @Transactional 自调用失效)
            final BigDecimal finalRatio = ratio;
            transactionTemplate.executeWithoutResult(s -> doDeductForRefund(origin, refundId, finalRatio));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("[deductForRefund] interrupted", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 锁内事务:按 ratio 扣 balance + 写 refund_deduct 行 + 原行 reverted。
     *
     * <p>扣回金额 = {@code origin.amount × ratio}(scale=2 HALF_UP)。
     * 例:origin.amount=5,ratio=0.6 → deductAmount=3.00,refund_deduct 行 amount=-3.00,
     * teacher 实际从该订单留 5-3=2 USD。
     */
    private void doDeductForRefund(TeacherIncomeDO origin, Long refundId, BigDecimal ratio) {
        String status = origin.getStatus();
        BigDecimal originAmount = origin.getAmountUsd() != null ? origin.getAmountUsd() : BigDecimal.ZERO;
        Long teacherId = origin.getTeacherId();
        LocalDateTime now = LocalDateTime.now();

        if ("reverted".equals(status)) {
            // 幂等防御:已被扣过
            throw exception(INCOME_ALREADY_REVERTED);
        }

        // 计算实际扣回金额:origin × ratio,scale=2 HALF_UP
        BigDecimal deductAmount = originAmount.multiply(ratio)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // 边界:原 amount=0(免费试听 / no_show_teacher)或 ratio<=0 → 标 reverted 防重复但不动 balance
        if (deductAmount.compareTo(BigDecimal.ZERO) <= 0) {
            TeacherIncomeDO upd = new TeacherIncomeDO();
            upd.setId(origin.getId());
            upd.setStatus("reverted");
            upd.setUpdater("system-refund-deduct");
            teacherIncomeMapper.updateById(upd);
            return;
        }

        // 1) 扣 balance(分支 — 按 deductAmount)
        if ("available".equals(status)) {
            teacherBalanceService.deductFromAvailable(teacherId, deductAmount);
        } else if ("frozen".equals(status)) {
            teacherBalanceService.deductFromFrozen(teacherId, deductAmount);
        } else {
            // 未知 status — 防御性 throw,语义独立于 reverted(避免静默吞掉异常状态)
            throw exception(INCOME_STATUS_INVALID);
        }

        // 2) 写 refund_deduct 负数流水(立即生效 — frozen_until=settledAt=now,status=available)
        TeacherIncomeDO deduct = new TeacherIncomeDO();
        deduct.setTeacherId(teacherId);
        deduct.setCourseOrderId(origin.getCourseOrderId());
        deduct.setRefundId(refundId);
        deduct.setAmountUsd(deductAmount.negate());
        deduct.setType("refund_deduct");
        deduct.setSettledAt(now);
        deduct.setStatus("available");
        deduct.setCurrency("USD");
        deduct.setFrozenUntil(now);
        deduct.setRelatedIncomeId(origin.getId());
        deduct.setCreator("system-refund-deduct");
        deduct.setUpdater("system-refund-deduct");
        teacherIncomeMapper.insert(deduct);

        // 3) 原行标 reverted(注意:原行 amount 不动,语义是"已发生退款扣回"而非"金额清零")
        TeacherIncomeDO upd = new TeacherIncomeDO();
        upd.setId(origin.getId());
        upd.setStatus("reverted");
        upd.setUpdater("system-refund-deduct");
        teacherIncomeMapper.updateById(upd);
    }
}
