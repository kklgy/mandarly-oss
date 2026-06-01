package com.mandarly.boot.module.edu.service.booking;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCancelReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCreateReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.CourseOrderPageReqVO;
import com.mandarly.boot.module.edu.controller.app.booking.vo.AppBookingCountsRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.MeetingDO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.booking.MeetingMapper;
import com.mandarly.boot.module.edu.dal.mysql.booking.StudentPackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.review.ReviewMapper;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.booking.OrderStatusEnum;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import com.mandarly.boot.module.edu.enums.user.UserRoleEnum;
import com.mandarly.boot.module.edu.enums.user.UserStatusEnum;
import com.mandarly.boot.module.edu.service.classroom.ClassroomService;
import com.mandarly.boot.module.edu.service.income.event.OrderFinishedEvent;
import com.mandarly.boot.module.edu.service.notification.NotificationService;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.BOOKING_SLOT_LOCK_TIMEOUT;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.TEACHER_NOT_APPROVED;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    /** 取消返还课次的阈值:课前 24h */
    private static final Duration CANCEL_REFUND_THRESHOLD = Duration.ofHours(24);

    /** 一期固定 30 分钟课时 */
    private static final int CLASS_DURATION_MINUTES = 30;

    /** infra_config key:普通课教师课时费(USD)。值由运营配置,seed 默认 3。 */
    private static final String CONFIG_KEY_TEACHER_LESSON_FEE = "mandarly.income.teacher_lesson_fee_usd";

    /** 一期教师结算固定 USD,不要跟随学生购买套餐币种。 */
    private static final String TEACHER_SETTLEMENT_CURRENCY = "USD";

    /** D28 sweep:课程结束后预留多少 buffer 才判定为"过期未结束"。 */
    private static final Duration OVERDUE_BUFFER = Duration.ofMinutes(15);

    /** D28 sweep:单次最多扫多少条,防爆。 */
    private static final int SWEEP_BATCH_LIMIT = 1000;

    /** 预约同一教师同一 UTC 时段的分布式锁前缀。 */
    private static final String BOOKING_SLOT_LOCK_PREFIX = "booking:teacher-slot:";

    private static final int BOOKING_LOCK_WAIT_SECONDS = 5;

    private static final int BOOKING_LOCK_LEASE_SECONDS = 30;

    @Resource
    private CourseOrderMapper courseOrderMapper;

    @Resource
    private StudentPackageMapper studentPackageMapper;

    @Resource
    private MeetingMapper meetingMapper;

    @Resource
    private PackageMapper packageMapper;

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private ClassroomService classroomService;

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private TeacherProfileMapper teacherProfileMapper;

    @Resource
    private ConfigApi configApi;

    @Resource
    private NotificationService notificationService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public PageResult<CourseOrderDO> getOrderPage(CourseOrderPageReqVO reqVO) {
        return courseOrderMapper.selectPage(reqVO);
    }

    @Override
    public PageResult<CourseOrderDO> getMyOrdersPage(Long studentId, Collection<String> statuses, PageParam pageParam) {
        return courseOrderMapper.selectMyOrdersPage(studentId, statuses, pageParam);
    }

    @Override
    public PageResult<CourseOrderDO> getTeacherOrdersPage(Long teacherId, Collection<String> statuses, PageParam pageParam) {
        return courseOrderMapper.selectTeacherOrdersPage(teacherId, statuses, pageParam);
    }

    @Override
    public CourseOrderDO getOrder(Long id) {
        return courseOrderMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(BookingCreateReqVO reqVO) {
        // 1. 校验学生 + 教师身份
        UserDO student = eduUserMapper.selectById(reqVO.getStudentId());
        if (student == null
                || !UserRoleEnum.STUDENT.getCode().equals(student.getRole())
                || !UserStatusEnum.ACTIVE.getCode().equals(student.getStatus())) {
            throw ServiceExceptionUtil.exception0(400, "学生不存在或非 active 状态");
        }
        UserDO teacher = eduUserMapper.selectById(reqVO.getTeacherId());
        if (teacher == null
                || !UserRoleEnum.TEACHER.getCode().equals(teacher.getRole())
                || !UserStatusEnum.ACTIVE.getCode().equals(teacher.getStatus())) {
            throw ServiceExceptionUtil.exception0(400, "教师不存在或非 active 状态");
        }
        // 1.1 闭合 backdoor:教师档案必须 audit_status=approved 才允许接单
        TeacherProfileDO teacherProfile = teacherProfileMapper.selectById(reqVO.getTeacherId());
        if (teacherProfile == null
                || !TeacherAuditStatusEnum.APPROVED.getCode().equals(teacherProfile.getAuditStatus())) {
            throw exception(TEACHER_NOT_APPROVED);
        }

        // 2. 校验上课时间不能在过去
        if (reqVO.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw ServiceExceptionUtil.exception0(400, "上课时间不能早于当前时间");
        }

        String lockKey = BOOKING_SLOT_LOCK_PREFIX + reqVO.getTeacherId() + ":" + reqVO.getScheduledAt();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(BOOKING_LOCK_WAIT_SECONDS, BOOKING_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw exception(BOOKING_SLOT_LOCK_TIMEOUT);
            }
            return createOrderUnderSlotLock(reqVO);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(BOOKING_SLOT_LOCK_TIMEOUT);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Long createOrderUnderSlotLock(BookingCreateReqVO reqVO) {
        // 3. 教师同一时间冲突检测(只看 upcoming 状态)。必须在 slot 锁内重查,防并发双插。
        List<CourseOrderDO> conflicts = courseOrderMapper.selectByTeacherAndScheduledAt(
                reqVO.getTeacherId(), reqVO.getScheduledAt(), OrderStatusEnum.UPCOMING.getCode());
        if (!conflicts.isEmpty()) {
            throw ServiceExceptionUtil.exception0(409, "教师该时段已有预约");
        }

        // 4. 选 student_package + 扣次
        StudentPackageDO chosen = chooseStudentPackage(reqVO.getStudentId(), reqVO.getStudentPackageId());
        chosen.setRemaining(chosen.getRemaining() - 1);
        studentPackageMapper.updateById(chosen);

        // 5. 取套餐定义,带出价格快照与免费体验课标记
        PackageDO pkg = packageMapper.selectById(chosen.getPackageId());
        if (pkg == null) {
            throw ServiceExceptionUtil.exception0(500, "套餐定义已被删除,数据异常 packageId=" + chosen.getPackageId());
        }
        BigDecimal unitPrice = pkg.getTotalCount() != null && pkg.getTotalCount() > 0
                ? pkg.getPrice().divide(BigDecimal.valueOf(pkg.getTotalCount()), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 6. 创建订单(upcoming)
        CourseOrderDO order = new CourseOrderDO();
        order.setStudentId(reqVO.getStudentId());
        order.setTeacherId(reqVO.getTeacherId());
        order.setStudentPackageId(chosen.getId());
        order.setScheduledAt(reqVO.getScheduledAt());
        order.setDuration(CLASS_DURATION_MINUTES);
        order.setPriceDisplay(unitPrice);
        order.setCurrency(pkg.getCurrency());
        order.setStatus(OrderStatusEnum.UPCOMING.getCode());
        order.setIsRefundedClass(false);
        order.setIsFreeTrial(Boolean.TRUE.equals(pkg.getIsFreeTrial()));
        // teacher_amount 下单时 snapshot:free_trial = 0,正式课读 infra_config 课时费。
        // 配置后续改动不追溯历史订单(PRD-v4 §4.5 V1 触发 Job 时算 → 当前简化为下单时算并 freeze)
        order.setTeacherAmount(resolveTeacherAmount(pkg));
        order.setTeacherAmountCurrency(TEACHER_SETTLEMENT_CURRENCY);
        order.setTeacherSettleStatus("pending");
        // @PermitAll 上下文兜底:无登录用户时 MetaObjectHandler 会留 null,触发 NOT NULL 约束
        order.setCreator("anonymous");
        order.setUpdater("anonymous");
        try {
            courseOrderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            log.warn("[createOrder] duplicate booking slot teacherId={} scheduledAt={}",
                    reqVO.getTeacherId(), reqVO.getScheduledAt());
            throw ServiceExceptionUtil.exception0(409, "教师该时段已有预约");
        }

        // 7. 调 ClassroomService 开 LCIC 课堂房间;LCIC 失败时内部留 stub-{orderId} 占位 + 异步重试,
        //    不反噬下单事务(决策位 b)
        classroomService.openRoomForOrder(order.getId());

        try {
            notificationService.sendForBookingCreated(order.getId());
        } catch (Exception e) {
            log.error("[createOrder] booking notification failed orderId={}", order.getId(), e);
        }

        log.info("[createOrder] orderId={} student={} teacher={} package={} scheduledAt={} freeTrial={}",
                order.getId(), order.getStudentId(), order.getTeacherId(),
                chosen.getId(), order.getScheduledAt(), order.getIsFreeTrial());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(BookingCancelReqVO reqVO) {
        CourseOrderDO order = courseOrderMapper.selectById(reqVO.getOrderId());
        if (order == null) {
            throw ServiceExceptionUtil.exception0(404, "订单不存在");
        }
        if (!OrderStatusEnum.UPCOMING.getCode().equals(order.getStatus())) {
            throw ServiceExceptionUtil.exception0(409,
                    "订单当前状态 " + order.getStatus() + ",仅 upcoming 可取消");
        }

        // 24h 规则
        Duration toClass = Duration.between(LocalDateTime.now(), order.getScheduledAt());
        boolean refundClass = toClass.compareTo(CANCEL_REFUND_THRESHOLD) >= 0;

        // 返还课次(仅 24h+)
        if (refundClass) {
            StudentPackageDO sp = studentPackageMapper.selectById(order.getStudentPackageId());
            if (sp != null) {
                sp.setRemaining(sp.getRemaining() + 1);
                studentPackageMapper.updateById(sp);
            } else {
                log.warn("[cancelOrder] student_package 已不存在,跳过返还 orderId={} spId={}",
                        order.getId(), order.getStudentPackageId());
            }
        }

        // 更新订单
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(reqVO.getReason());
        order.setCancelledBy(reqVO.getCancelledBy());
        order.setCancelledAt(LocalDateTime.now());
        order.setIsRefundedClass(refundClass);
        courseOrderMapper.updateById(order);

        // 同步 meeting 状态(避免后续 LCIC 误开房)
        MeetingDO meeting = meetingMapper.selectById(order.getId());
        if (meeting != null) {
            meeting.setStatus("cancelled");
            meetingMapper.updateById(meeting);
        }

        log.info("[cancelOrder] orderId={} by={} refundClass={} hoursToClass={}",
                order.getId(), reqVO.getCancelledBy(), refundClass, toClass.toHours());
    }

    /**
     * D28 过期 upcoming 兜底扫描 — 见 {@link BookingService#sweepOverdueUpcoming()} 接口注释。
     *
     * <p>分流规则(按 meeting 实际状态):
     * <ul>
     *   <li>meeting == null → status=abnormal, reason=meeting_missing(理论不该出现,留观测口)</li>
     *   <li>meeting.status=ended → status=finished + publish OrderFinishedEvent
     *       (LCIC room_end webhook 丢包补救路径,自动接结算链)</li>
     *   <li>meeting.status=ongoing → status=abnormal, reason=meeting_ongoing_overdue
     *       (room_start 发了但 room_end 没发,至少一方进过)</li>
     *   <li>meeting.status=created + lcic_class_id LIKE 'stub-%' → reason=lcic_init_failed
     *       (createOrder 时 LCIC 开房失败,占位符未替换)</li>
     *   <li>meeting.status=created + 真实 lcic_class_id → reason=lcic_no_attendance
     *       (房间创出来了,但双方都没进)</li>
     *   <li>meeting.status=cancelled → reason=meeting_cancelled_orphan
     *       (理论上 cancelOrder 已同步,留兜底)</li>
     * </ul>
     *
     * <p>单条 try/catch,失败 log error 后跳到下一条;不引入分布式锁(单实例 prod + Quartz misfire 足够)。
     */
    @Override
    public String sweepOverdueUpcoming() {
        LocalDateTime cutoff = LocalDateTime.now().minus(OVERDUE_BUFFER);
        List<CourseOrderDO> overdue = courseOrderMapper.selectOverdueUpcoming(cutoff, SWEEP_BATCH_LIMIT);
        if (overdue.isEmpty()) {
            log.info("[sweepOverdueUpcoming] scanned=0 cutoff={}", cutoff);
            return "scanned=0";
        }

        int finished = 0;
        int abnormal = 0;
        Map<String, Integer> breakdown = new LinkedHashMap<>();

        for (CourseOrderDO order : overdue) {
            try {
                String reason = resolveAndApplySweepBranch(order);
                if ("finished".equals(reason)) {
                    finished++;
                } else {
                    abnormal++;
                    breakdown.merge(reason, 1, Integer::sum);
                }
            } catch (Exception e) {
                log.error("[sweepOverdueUpcoming] orderId={} 处理失败,跳过", order.getId(), e);
            }
        }

        StringBuilder summary = new StringBuilder()
                .append("scanned=").append(overdue.size())
                .append(" finished=").append(finished)
                .append(" abnormal=").append(abnormal);
        if (!breakdown.isEmpty()) {
            summary.append(" (");
            boolean first = true;
            for (Map.Entry<String, Integer> e : breakdown.entrySet()) {
                if (!first) summary.append(" ");
                summary.append(e.getKey()).append("=").append(e.getValue());
                first = false;
            }
            summary.append(")");
        }
        log.info("[sweepOverdueUpcoming] {}", summary);
        return summary.toString();
    }

    /**
     * 单条订单走完整分流并落库;返回 "finished" 或具体 abnormal_reason。
     * 独立成方法是为了让单测可针对每个分支精确断言,且便于调用方累加统计。
     */
    @Transactional(rollbackFor = Exception.class)
    protected String resolveAndApplySweepBranch(CourseOrderDO order) {
        MeetingDO meeting = meetingMapper.selectById(order.getId());

        if (meeting == null) {
            return markAbnormal(order, "meeting_missing");
        }
        String meetingStatus = meeting.getStatus();

        if ("ended".equals(meetingStatus)) {
            // LCIC room_end webhook 丢包补救:补 finished + publish event 接结算链
            order.setStatus(OrderStatusEnum.FINISHED.getCode());
            order.setFinishedAt(meeting.getEndedAt() != null ? meeting.getEndedAt() : LocalDateTime.now());
            courseOrderMapper.updateById(order);
            applicationEventPublisher.publishEvent(new OrderFinishedEvent(order.getId()));
            log.info("[sweepOverdueUpcoming] orderId={} meeting=ended → finished, event published", order.getId());
            return "finished";
        }
        if ("ongoing".equals(meetingStatus)) {
            return markAbnormal(order, "meeting_ongoing_overdue");
        }
        if ("created".equals(meetingStatus)) {
            String lcicClassId = meeting.getLcicClassId();
            if (lcicClassId != null && lcicClassId.startsWith("stub-")) {
                return markAbnormal(order, "lcic_init_failed");
            }
            return markAbnormal(order, "lcic_no_attendance");
        }
        if ("cancelled".equals(meetingStatus)) {
            return markAbnormal(order, "meeting_cancelled_orphan");
        }
        // 兜底:未知 meeting.status,也标 abnormal 但 reason 留可观测痕迹
        log.warn("[sweepOverdueUpcoming] orderId={} 未知 meeting.status={}", order.getId(), meetingStatus);
        return markAbnormal(order, "meeting_unknown_status");
    }

    private String markAbnormal(CourseOrderDO order, String reason) {
        order.setStatus(OrderStatusEnum.ABNORMAL.getCode());
        order.setAbnormalReason(reason);
        courseOrderMapper.updateById(order);
        log.info("[sweepOverdueUpcoming] orderId={} → abnormal reason={}", order.getId(), reason);
        return reason;
    }

    /**
     * 扣次优先级(PRD §1.5):
     * <ol>
     *   <li>若 reqVO 指定 student_package,直接用(校验属于该学生 + 仍可用)</li>
     *   <li>否则:候选池为该学生所有"剩余 > 0 且未过期"套餐</li>
     *   <li>优先扣**免费体验课**(is_free_trial=1)</li>
     *   <li>再选**剩余有效期最短**的套餐(避免到期作废)</li>
     * </ol>
     */
    private StudentPackageDO chooseStudentPackage(Long studentId, Long requestedPackageId) {
        if (requestedPackageId != null) {
            StudentPackageDO sp = studentPackageMapper.selectById(requestedPackageId);
            if (sp == null || !sp.getStudentId().equals(studentId)) {
                throw ServiceExceptionUtil.exception0(400, "指定的 student_package 不存在或不属于该学生");
            }
            if (sp.getRemaining() == null || sp.getRemaining() <= 0) {
                throw ServiceExceptionUtil.exception0(409, "指定套餐剩余课次不足");
            }
            if (sp.getExpireAt() == null || sp.getExpireAt().isBefore(LocalDateTime.now())) {
                throw ServiceExceptionUtil.exception0(409, "指定套餐已过期");
            }
            return sp;
        }

        List<StudentPackageDO> candidates = studentPackageMapper.selectActiveByStudent(
                studentId, LocalDateTime.now());
        if (candidates.isEmpty()) {
            throw ServiceExceptionUtil.exception0(409, "学生无可用套餐,请先购买或发放");
        }

        // 拉取每个 candidate 对应的 package 是否 is_free_trial
        return candidates.stream()
                .min(Comparator
                        .comparing((StudentPackageDO sp) -> isFreeTrial(sp) ? 0 : 1)
                        .thenComparing(StudentPackageDO::getExpireAt))
                .orElseThrow(() -> ServiceExceptionUtil.exception0(500, "扣次算法异常"));
    }

    private boolean isFreeTrial(StudentPackageDO sp) {
        PackageDO pkg = packageMapper.selectById(sp.getPackageId());
        return pkg != null && Boolean.TRUE.equals(pkg.getIsFreeTrial());
    }

    @Override
    public AppBookingCountsRespVO getMyOrderCounts(Long studentId) {
        AppBookingCountsRespVO vo = new AppBookingCountsRespVO();
        if (studentId == null) {
            vo.setUpcomingCount(0);
            vo.setToReviewCount(0);
            vo.setRefundingCount(0);
            return vo;
        }

        LocalDateTime now = LocalDateTime.now();

        // upcoming = status='upcoming' AND scheduled_at > now
        // 注意:M5 阶段 BookingService 状态机仅落 upcoming/cancelled/finished;
        // scheduled_at < now 的 upcoming 应视为"待结算"(LCIC webhook 未推回 finished),
        // 这里口径上仍排除掉,避免与 OrderCard "等待结算" 灰态重复计入待上课。
        Long upcoming = courseOrderMapper.selectCount(Wrappers.<CourseOrderDO>lambdaQuery()
                .eq(CourseOrderDO::getStudentId, studentId)
                .eq(CourseOrderDO::getStatus, OrderStatusEnum.UPCOMING.getCode())
                .gt(CourseOrderDO::getScheduledAt, now));

        // toReview = status='finished' AND review.order_id IS NULL
        // 复用 ReviewMapper 已定义的 selectPendingReviewOrderIds(M5 之前留作 future,本次启用)
        List<Long> pending = reviewMapper.selectPendingReviewOrderIds(studentId);

        // refunding = course_order.status IN ('refunding','refunded')
        // M4 Stripe webhook 推动状态机;M5 阶段一般为 0
        Long refunding = courseOrderMapper.selectCount(Wrappers.<CourseOrderDO>lambdaQuery()
                .eq(CourseOrderDO::getStudentId, studentId)
                .in(CourseOrderDO::getStatus,
                        OrderStatusEnum.REFUNDING.getCode(), OrderStatusEnum.REFUNDED.getCode()));

        vo.setUpcomingCount(upcoming == null ? 0 : upcoming.intValue());
        vo.setToReviewCount(pending == null ? 0 : pending.size());
        vo.setRefundingCount(refunding == null ? 0 : refunding.intValue());
        return vo;
    }

    /**
     * 决议教师课时费:免费体验课 → 0,普通课读 {@code mandarly.income.teacher_lesson_fee_usd}。
     *
     * <p>配置缺失或非法 → fallback 0(伴随 log.warn,Phase E ops 监控会告警)。
     * 该值会写入 {@code course_order.teacher_amount},结算时直接引用订单快照。
     */
    private BigDecimal resolveTeacherAmount(PackageDO pkg) {
        if (Boolean.TRUE.equals(pkg.getIsFreeTrial())) {
            return BigDecimal.ZERO;
        }
        String raw = configApi.getConfigValueByKey(CONFIG_KEY_TEACHER_LESSON_FEE);
        if (raw == null || raw.isBlank()) {
            log.warn("[resolveTeacherAmount] config {} 未配置,fallback 0", CONFIG_KEY_TEACHER_LESSON_FEE);
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("[resolveTeacherAmount] config {}={} 非法数字,fallback 0",
                    CONFIG_KEY_TEACHER_LESSON_FEE, raw);
            return BigDecimal.ZERO;
        }
    }

}
