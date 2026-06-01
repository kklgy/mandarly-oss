package com.mandarly.boot.module.edu.controller.admin.report;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.module.edu.controller.admin.report.vo.EduReportOverviewRespVO;
import com.mandarly.boot.module.edu.dal.mysql.report.EduReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 运营分析")
@RestController
@RequestMapping("/edu/report")
@Validated
public class EduReportController {

    @Resource
    private EduReportMapper reportMapper;

    @GetMapping("/overview")
    @Operation(summary = "运营分析概览(日 / 周 / 月真实数据)")
    @PreAuthorize("@ss.hasAnyPermissions('edu:user:query', 'edu:payment:query', 'edu:course-order:query', 'edu:teacher-profile:query')")
    public CommonResult<EduReportOverviewRespVO> getOverview() {
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = today.withDayOfMonth(1);

        List<EduReportOverviewRespVO.PeriodStat> periods = List.of(
                buildPeriod("今日", today.atStartOfDay(), now),
                buildPeriod("本周", weekStart.atStartOfDay(), now),
                buildPeriod("本月", monthStart.atStartOfDay(), now)
        );

        return success(EduReportOverviewRespVO.builder()
                .totalStudents(value(reportMapper.countUsers("student", null, null)))
                .totalTeachers(value(reportMapper.countUsers("teacher", null, null)))
                .approvedTeachers(value(reportMapper.countTeacherProfiles("approved", null, null)))
                .pendingTeacherAudits(value(reportMapper.countTeacherProfiles("pending", null, null)))
                .totalCourseOrders(value(reportMapper.countCourseOrders(null, null, null)))
                .upcomingCourseOrders(value(reportMapper.countCourseOrders("upcoming", null, null)))
                .abnormalCourseOrders(value(reportMapper.countCourseOrders("abnormal", null, null)))
                .paidPaymentOrders(value(reportMapper.countPaidPayments(null, null)))
                .paidAmountUsd(reportMapper.sumPaidAmountUsd(null, null))
                .pendingRefunds(value(reportMapper.countRefunds("pending", null, null)))
                .refundedAmountUsd(reportMapper.sumRefundedAmountUsd(null, null))
                .lastUpdatedAt(now)
                .periods(periods)
                .build());
    }

    private EduReportOverviewRespVO.PeriodStat buildPeriod(String label,
                                                           LocalDateTime beginAt,
                                                           LocalDateTime endAt) {
        return EduReportOverviewRespVO.PeriodStat.builder()
                .label(label)
                .beginAt(beginAt)
                .newStudents(value(reportMapper.countUsers("student", beginAt, endAt)))
                .newTeachers(value(reportMapper.countUsers("teacher", beginAt, endAt)))
                .courseOrders(value(reportMapper.countCourseOrders(null, beginAt, endAt)))
                .paidPaymentOrders(value(reportMapper.countPaidPayments(beginAt, endAt)))
                .paidAmountUsd(reportMapper.sumPaidAmountUsd(beginAt, endAt))
                .refundedOrders(value(reportMapper.countRefunds("refunded", beginAt, endAt)))
                .refundedAmountUsd(reportMapper.sumRefundedAmountUsd(beginAt, endAt))
                .build();
    }

    private Long value(Long val) {
        return val == null ? 0L : val;
    }
}
