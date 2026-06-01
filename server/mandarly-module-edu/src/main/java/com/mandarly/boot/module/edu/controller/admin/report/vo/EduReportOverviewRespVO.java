package com.mandarly.boot.module.edu.controller.admin.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 运营分析概览 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EduReportOverviewRespVO {

    @Schema(description = "注册学员总数")
    private Long totalStudents;

    @Schema(description = "教师总数")
    private Long totalTeachers;

    @Schema(description = "已通过教师数")
    private Long approvedTeachers;

    @Schema(description = "待审核教师数")
    private Long pendingTeacherAudits;

    @Schema(description = "课程订单总数")
    private Long totalCourseOrders;

    @Schema(description = "待上课订单数")
    private Long upcomingCourseOrders;

    @Schema(description = "异常订单数")
    private Long abnormalCourseOrders;

    @Schema(description = "成功支付订单数")
    private Long paidPaymentOrders;

    @Schema(description = "成功支付结算金额(USD)")
    private BigDecimal paidAmountUsd;

    @Schema(description = "待处理退款数")
    private Long pendingRefunds;

    @Schema(description = "已退款金额(USD)")
    private BigDecimal refundedAmountUsd;

    @Schema(description = "统计更新时间(UTC)")
    private LocalDateTime lastUpdatedAt;

    @Schema(description = "日 / 周 / 月区间指标")
    private List<PeriodStat> periods;

    @Schema(description = "运营分析区间指标")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodStat {

        @Schema(description = "区间标签")
        private String label;

        @Schema(description = "区间起始时间(UTC)")
        private LocalDateTime beginAt;

        @Schema(description = "新增学员数")
        private Long newStudents;

        @Schema(description = "新增教师数")
        private Long newTeachers;

        @Schema(description = "课程订单数")
        private Long courseOrders;

        @Schema(description = "成功支付订单数")
        private Long paidPaymentOrders;

        @Schema(description = "成功支付结算金额(USD)")
        private BigDecimal paidAmountUsd;

        @Schema(description = "已退款单数")
        private Long refundedOrders;

        @Schema(description = "已退款金额(USD)")
        private BigDecimal refundedAmountUsd;
    }
}
