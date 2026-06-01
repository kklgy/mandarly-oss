package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherDashboardSummaryRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.booking.OrderStatusEnum;
import com.mandarly.boot.module.edu.enums.user.UserRoleEnum;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import com.mandarly.boot.module.edu.service.review.ReviewService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * 教师 - 工作台 Dashboard 聚合实现(D16 P2.3 / PRD-v4 §4.2 T0)
 *
 * <p>依赖:
 * <ul>
 *   <li>{@link EduUserMapper} — 取 user.role(校验 teacher)+ user.timezone</li>
 *   <li>{@link CourseOrderMapper} — 本周 upcoming 计数 + 本月 finished 金额求和(scheduledAt UTC 窗口)</li>
 *   <li>{@link TeacherBalanceService} — pending(frozenT7)+ available + totalEarned</li>
 *   <li>{@link ReviewService#getTeacherStat} — avgRating + reviewCount</li>
 * </ul>
 *
 * <p>窗口边界对齐前端 dayjs:周窗口 = 教师时区周一 00:00 ~ 下周一 00:00;
 * 月窗口 = 教师时区当月 1 号 00:00 ~ 下月 1 号 00:00。
 *
 * <p>时区落库统一 UTC LocalDateTime(与 CourseOrderDO.scheduledAt 一致),
 * 通过 {@code ZonedDateTime.withZoneSameInstant(UTC).toLocalDateTime()} 转换。
 */
@Service
@Slf4j
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private CourseOrderMapper courseOrderMapper;

    @Resource
    private TeacherBalanceService teacherBalanceService;

    @Resource
    private ReviewService reviewService;

    @Override
    public AppTeacherDashboardSummaryRespVO getSummary(Long userId) {
        // 1. 校验登录用户 = teacher(@PreAuthorize 桥接外再防御一道,避免 role 切换的脏数据)
        UserDO user = eduUserMapper.selectById(userId);
        if (user == null || !UserRoleEnum.TEACHER.getCode().equals(user.getRole())) {
            throw ServiceExceptionUtil.exception0(403, "仅教师可访问教师工作台");
        }

        // 2. 时区:user.timezone 优先,UTC 兜底
        ZoneId tz = parseTimezone(user.getTimezone());

        // 3. 周窗口(教师时区周一 00:00 ~ 下周一 00:00 → UTC LocalDateTime)
        ZonedDateTime nowZ = ZonedDateTime.now(tz);
        ZonedDateTime weekStartZ = nowZ.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(tz);
        ZonedDateTime weekEndZ = weekStartZ.plusWeeks(1);
        LocalDateTime weekStartUtc = toUtcLocal(weekStartZ);
        LocalDateTime weekEndUtc = toUtcLocal(weekEndZ);

        // 4. 月窗口
        LocalDate today = LocalDate.now(tz);
        ZonedDateTime monthStartZ = today.withDayOfMonth(1).atStartOfDay(tz);
        ZonedDateTime monthEndZ = monthStartZ.plusMonths(1);
        LocalDateTime monthStartUtc = toUtcLocal(monthStartZ);
        LocalDateTime monthEndUtc = toUtcLocal(monthEndZ);

        // 5. 聚合 booking — 本周 upcoming 课次 + 本月 finished 金额
        Long weeklyCount = courseOrderMapper.countByTeacherInWindow(
                userId, OrderStatusEnum.UPCOMING.getCode(), weekStartUtc, weekEndUtc);
        BigDecimal monthlyIncome = courseOrderMapper.sumTeacherAmountByTeacherInWindow(
                userId, OrderStatusEnum.FINISHED.getCode(), monthStartUtc, monthEndUtc);

        // 6. 余额(M6 既有 Service,null 时全 0)
        TeacherBalanceDO balance = teacherBalanceService.getBalance(userId);

        // 7. 评价(M3 既有 ReviewService,返 Map)
        Map<String, Object> stat = reviewService.getTeacherStat(userId);

        // 8. 装 VO
        AppTeacherDashboardSummaryRespVO vo = new AppTeacherDashboardSummaryRespVO();
        vo.setWeeklyClassCount(weeklyCount == null ? 0 : weeklyCount.intValue());
        vo.setMonthlyIncomeUsd(monthlyIncome == null ? BigDecimal.ZERO : monthlyIncome);
        vo.setPendingSettleUsd(nullToZero(balance == null ? null : balance.getFrozenT7Usd()));
        vo.setTotalEarnedUsd(nullToZero(balance == null ? null : balance.getTotalEarnedUsd()));
        vo.setAvailableUsd(nullToZero(balance == null ? null : balance.getAvailableUsd()));
        vo.setRatingAvg(extractAvgRating(stat));
        vo.setRatingCount(extractReviewCount(stat));
        return vo;
    }

    private ZoneId parseTimezone(String tz) {
        if (tz == null || tz.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(tz);
        } catch (Exception e) {
            log.warn("[parseTimezone] invalid user timezone={}, fallback UTC", tz);
            return ZoneOffset.UTC;
        }
    }

    private LocalDateTime toUtcLocal(ZonedDateTime z) {
        return z.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal extractAvgRating(Map<String, Object> stat) {
        if (stat == null) {
            return null;
        }
        Object v = stat.get("avgRating");
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer extractReviewCount(Map<String, Object> stat) {
        if (stat == null) {
            return 0;
        }
        Object v = stat.get("reviewCount");
        if (v == null) {
            return 0;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
