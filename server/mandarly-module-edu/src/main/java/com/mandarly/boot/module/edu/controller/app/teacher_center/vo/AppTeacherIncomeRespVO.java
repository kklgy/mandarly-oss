package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户端 - 教师中心:收入流水单行 Response VO
 *
 * <p>对应 spec §5.3 IncomeView 底部明细表(课程时间 / 学生 / 类型 / 课时费 / 状态 / 解冻时间)。
 */
@Schema(description = "用户端 - 教师中心 - 收入流水单行 Response VO")
@Data
public class AppTeacherIncomeRespVO {

    @Schema(description = "流水 id", example = "20001")
    private Long id;

    @Schema(description = "关联课程订单 id", example = "10001")
    private Long courseOrderId;

    @Schema(description = "收入类型 normal/free_trial/no_show_teacher/no_show_student/refund_deduct/manual_adjust", example = "normal")
    private String type;

    @Schema(description = "金额(USD,refund_deduct 为负)", example = "3.00")
    private BigDecimal amountUsd;

    @Schema(description = "状态 frozen/available/reverted", example = "frozen")
    private String status;

    @Schema(description = "结算时间(UTC)", example = "2026-05-20T09:30:00")
    private LocalDateTime settledAt;

    @Schema(description = "解冻时间(UTC,= settledAt+7d)", example = "2026-05-27T09:30:00")
    private LocalDateTime frozenUntil;

    @Schema(description = "币种(一期固定 USD)", example = "USD")
    private String currency;

    @Schema(description = "备注(manual_adjust 必填)")
    private String description;
}
