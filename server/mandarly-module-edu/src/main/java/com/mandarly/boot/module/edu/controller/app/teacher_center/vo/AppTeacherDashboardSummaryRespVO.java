package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 教师 - 工作台 Dashboard 聚合 Response VO(D16 P2 / PRD-v4 §4.2 T0)
 *
 * <p>对应 GET /app-api/edu/teacher-center/dashboard/summary
 *
 * <p>口径:
 * <ul>
 *   <li>weeklyClassCount — 教师时区本周(周一 00:00 ~ 下周一 00:00)status=upcoming 的课次</li>
 *   <li>monthlyIncomeUsd — 教师时区当月 finished 订单的 teacherAmount 求和</li>
 *   <li>pendingSettleUsd — teacher_balance.frozen_t7_usd(T+7 在途)</li>
 *   <li>totalEarnedUsd / availableUsd — teacher_balance 同名字段</li>
 *   <li>ratingAvg / ratingCount — ReviewService.getTeacherStat 聚合</li>
 * </ul>
 */
@Schema(description = "教师 - 工作台 Dashboard 聚合 Response VO")
@Data
public class AppTeacherDashboardSummaryRespVO {

    @Schema(description = "本周课次(教师时区,周一-周日,status=upcoming)", example = "3")
    private Integer weeklyClassCount;

    @Schema(description = "本月收入 USD(教师时区当月 finished 订单 teacherAmount 求和)", example = "120.50")
    private BigDecimal monthlyIncomeUsd;

    @Schema(description = "待结算 USD(在途 T+7 冻结)", example = "30.00")
    private BigDecimal pendingSettleUsd;

    @Schema(description = "累计收入 USD(历史 total)", example = "1250.00")
    private BigDecimal totalEarnedUsd;

    @Schema(description = "可提现余额 USD", example = "90.50")
    private BigDecimal availableUsd;

    @Schema(description = "评价均分(无评价返 null)", example = "4.70")
    private BigDecimal ratingAvg;

    @Schema(description = "评价条数", example = "15")
    private Integer ratingCount;
}
