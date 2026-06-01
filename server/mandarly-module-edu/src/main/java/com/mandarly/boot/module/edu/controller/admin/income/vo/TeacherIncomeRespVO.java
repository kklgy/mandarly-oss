package com.mandarly.boot.module.edu.controller.admin.income.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 教师收入流水 Response VO(含 JOIN 字段)")
@Data
public class TeacherIncomeRespVO {

    @Schema(description = "流水 ID")
    private Long id;

    @Schema(description = "教师 user.id")
    private Long teacherId;

    @Schema(description = "教师邮箱(JOIN system_users)")
    private String teacherEmail;

    @Schema(description = "教师昵称(JOIN system_users)")
    private String teacherName;

    @Schema(description = "关联课程订单 ID")
    private Long courseOrderId;

    @Schema(description = "关联退款工单 ID(refund_deduct 类型时有值)")
    private Long refundId;

    @Schema(description = "套餐名称(JOIN course_order → package)")
    private String packageName;

    @Schema(description = "收入金额(USD),负数表示扣回")
    private BigDecimal amountUsd;

    @Schema(description = "收入类型:normal/free_trial/no_show_student/refund_deduct")
    private String type;

    @Schema(description = "结算时间(UTC)")
    private LocalDateTime settledAt;

    @Schema(description = "创建时间(UTC)")
    private LocalDateTime createTime;
}
