package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherDashboardSummaryRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.booking.OrderStatusEnum;
import com.mandarly.boot.module.edu.enums.user.UserRoleEnum;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import com.mandarly.boot.module.edu.service.review.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TeacherDashboardServiceImpl TDD 单元测试(D16 P2.2)
 *
 * <p>覆盖:
 * <ol>
 *   <li>normal case — 7 字段全装好</li>
 *   <li>empty teacher — booking/balance/review 全空时返 0 / null</li>
 *   <li>nonTeacher — role=student 抛 403 业务异常</li>
 *   <li>week window timezone — 教师时区 Asia/Hong_Kong 周一边界正确换算到 UTC LocalDateTime</li>
 *   <li>month window timezone — 当月 1 号边界 timezone 切换</li>
 * </ol>
 */
class TeacherDashboardServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherDashboardServiceImpl teacherDashboardService;

    @Mock
    private EduUserMapper eduUserMapper;

    @Mock
    private CourseOrderMapper courseOrderMapper;

    @Mock
    private TeacherBalanceService teacherBalanceService;

    @Mock
    private ReviewService reviewService;

    private static final Long TEACHER_ID = 100L;

    // ==================== helpers ====================

    private UserDO teacherUser(String timezone) {
        UserDO u = new UserDO();
        u.setId(TEACHER_ID);
        u.setRole(UserRoleEnum.TEACHER.getCode());
        u.setTimezone(timezone);
        return u;
    }

    private TeacherBalanceDO balance(BigDecimal available, BigDecimal frozen, BigDecimal total) {
        TeacherBalanceDO b = new TeacherBalanceDO();
        b.setTeacherId(TEACHER_ID);
        b.setAvailableUsd(available);
        b.setFrozenT7Usd(frozen);
        b.setTotalEarnedUsd(total);
        b.setCurrency("USD");
        return b;
    }

    private Map<String, Object> reviewStat(String avg, long count) {
        Map<String, Object> m = new HashMap<>();
        m.put("avgRating", new BigDecimal(avg));
        m.put("reviewCount", count);
        m.put("finishedOrderCount", 0L);
        return m;
    }

    // ==================== tests ====================

    /** case 1: normal — 7 字段全装好 */
    @Test
    void getSummary_normalCase_returnsAllFields() {
        when(eduUserMapper.selectById(TEACHER_ID)).thenReturn(teacherUser("Asia/Hong_Kong"));
        when(courseOrderMapper.countByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.UPCOMING.getCode()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);
        when(courseOrderMapper.sumTeacherAmountByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.FINISHED.getCode()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("256.40"));
        when(teacherBalanceService.getBalance(TEACHER_ID))
                .thenReturn(balance(new BigDecimal("90.50"), new BigDecimal("30.00"), new BigDecimal("120.50")));
        when(reviewService.getTeacherStat(TEACHER_ID)).thenReturn(reviewStat("4.70", 15L));

        AppTeacherDashboardSummaryRespVO vo = teacherDashboardService.getSummary(TEACHER_ID);

        assertThat(vo.getWeeklyClassCount()).isEqualTo(3);
        assertThat(vo.getMonthlyIncomeUsd()).isEqualByComparingTo("256.40");
        assertThat(vo.getPendingSettleUsd()).isEqualByComparingTo("30.00");
        assertThat(vo.getTotalEarnedUsd()).isEqualByComparingTo("120.50");
        assertThat(vo.getAvailableUsd()).isEqualByComparingTo("90.50");
        assertThat(vo.getRatingAvg()).isEqualByComparingTo("4.70");
        assertThat(vo.getRatingCount()).isEqualTo(15);
    }

    /** case 2: empty — 新教师无 booking/balance/review,字段返 0 / null */
    @Test
    void getSummary_emptyTeacher_returnsZeros() {
        when(eduUserMapper.selectById(TEACHER_ID)).thenReturn(teacherUser("UTC"));
        when(courseOrderMapper.countByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.UPCOMING.getCode()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(courseOrderMapper.sumTeacherAmountByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.FINISHED.getCode()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);
        when(teacherBalanceService.getBalance(TEACHER_ID)).thenReturn(null);
        when(reviewService.getTeacherStat(TEACHER_ID)).thenReturn(reviewStat("0.00", 0L));

        AppTeacherDashboardSummaryRespVO vo = teacherDashboardService.getSummary(TEACHER_ID);

        assertThat(vo.getWeeklyClassCount()).isEqualTo(0);
        assertThat(vo.getMonthlyIncomeUsd()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vo.getPendingSettleUsd()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vo.getTotalEarnedUsd()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vo.getAvailableUsd()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vo.getRatingCount()).isEqualTo(0);
    }

    /** case 3: nonTeacher — role=student 调本接口抛业务异常,且不查 balance/booking */
    @Test
    void getSummary_nonTeacher_throws() {
        UserDO student = new UserDO();
        student.setId(TEACHER_ID);
        student.setRole(UserRoleEnum.STUDENT.getCode());
        when(eduUserMapper.selectById(TEACHER_ID)).thenReturn(student);

        assertThatThrownBy(() -> teacherDashboardService.getSummary(TEACHER_ID))
                .isInstanceOf(ServiceException.class);

        verify(courseOrderMapper, never()).countByTeacherInWindow(
                any(), any(), any(), any());
        verify(teacherBalanceService, never()).getBalance(any());
    }

    /**
     * case 4: week window timezone — Asia/Hong_Kong (UTC+8) 的本周边界,
     * 落到 mapper 的 UTC LocalDateTime 必须 = HKT 周一 00:00 - 8h。
     */
    @Test
    void getSummary_weekWindow_respectsTimezone() {
        ZoneId tz = ZoneId.of("Asia/Hong_Kong");
        when(eduUserMapper.selectById(TEACHER_ID)).thenReturn(teacherUser("Asia/Hong_Kong"));
        when(courseOrderMapper.countByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.UPCOMING.getCode()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(courseOrderMapper.sumTeacherAmountByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.FINISHED.getCode()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(teacherBalanceService.getBalance(TEACHER_ID)).thenReturn(null);
        when(reviewService.getTeacherStat(TEACHER_ID)).thenReturn(reviewStat("0", 0L));

        teacherDashboardService.getSummary(TEACHER_ID);

        ArgumentCaptor<LocalDateTime> startCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCap = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(courseOrderMapper).countByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.UPCOMING.getCode()),
                startCap.capture(), endCap.capture());

        // 期望:start = HKT 本周一 00:00 转 UTC,end = 下周一 00:00 转 UTC
        ZonedDateTime expectedStartZ = ZonedDateTime.now(tz)
                .with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(tz);
        ZonedDateTime expectedEndZ = expectedStartZ.plusWeeks(1);
        LocalDateTime expectedStartUtc = expectedStartZ.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime expectedEndUtc = expectedEndZ.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        assertThat(startCap.getValue()).isEqualTo(expectedStartUtc);
        assertThat(endCap.getValue()).isEqualTo(expectedEndUtc);
        // 跨时区核验:HKT 周一 00:00 == UTC 周日(前一天)16:00
        assertThat(startCap.getValue().getHour()).isEqualTo(16);
    }

    /**
     * case 5: month window timezone — Asia/Hong_Kong (UTC+8) 当月 1 号 00:00 转 UTC 应是上月最后一天 16:00。
     */
    @Test
    void getSummary_monthWindow_respectsTimezone() {
        ZoneId tz = ZoneId.of("Asia/Hong_Kong");
        when(eduUserMapper.selectById(TEACHER_ID)).thenReturn(teacherUser("Asia/Hong_Kong"));
        when(courseOrderMapper.countByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.UPCOMING.getCode()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(courseOrderMapper.sumTeacherAmountByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.FINISHED.getCode()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(teacherBalanceService.getBalance(TEACHER_ID)).thenReturn(null);
        when(reviewService.getTeacherStat(TEACHER_ID)).thenReturn(reviewStat("0", 0L));

        teacherDashboardService.getSummary(TEACHER_ID);

        ArgumentCaptor<LocalDateTime> startCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCap = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(courseOrderMapper).sumTeacherAmountByTeacherInWindow(
                eq(TEACHER_ID), eq(OrderStatusEnum.FINISHED.getCode()),
                startCap.capture(), endCap.capture());

        LocalDate today = LocalDate.now(tz);
        ZonedDateTime expectedStartZ = today.withDayOfMonth(1).atStartOfDay(tz);
        ZonedDateTime expectedEndZ = expectedStartZ.plusMonths(1);
        LocalDateTime expectedStartUtc = expectedStartZ.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime expectedEndUtc = expectedEndZ.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        assertThat(startCap.getValue()).isEqualTo(expectedStartUtc);
        assertThat(endCap.getValue()).isEqualTo(expectedEndUtc);
        // 跨时区核验:HKT 1 号 00:00 == UTC 上月末 16:00
        assertThat(startCap.getValue().getHour()).isEqualTo(16);
    }

}
