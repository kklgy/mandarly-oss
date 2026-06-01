package com.mandarly.boot.module.edu.controller.admin.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 退款工单 Response VO(含完整审核字段)")
@Data
public class RefundRespVO {

    @Schema(description = "退款工单 ID")
    private Long id;

    @Schema(description = "关联支付单 ID")
    private Long paymentId;

    @Schema(description = "学生 user.id")
    private Long userId;

    @Schema(description = "关联学生套餐 ID")
    private Long studentPackageId;

    @Schema(description = "申请原因")
    private String applyReason;

    @Schema(description = "建议退款金额(USD),系统按剩余课次比例计算")
    private BigDecimal suggestedAmountUsd;

    @Schema(description = "最终退款金额(USD),管理员填写")
    private BigDecimal finalAmountUsd;

    @Schema(description = "调整原因(final ≠ suggested 时必填)")
    private String adjustReason;

    @Schema(description = "Stripe Refund ID(approve 后回填)")
    private String channelRefundId;

    @Schema(description = "状态:pending/approved/rejected/refunded")
    private String status;

    @Schema(description = "审核人 user.id")
    private Long auditBy;

    @Schema(description = "审核时间(UTC)")
    private LocalDateTime auditAt;

    @Schema(description = "审核备注(rejected 时为拒绝理由)")
    private String auditNote;

    @Schema(description = "实际退款到账时间(UTC,Stripe charge.refunded 触发后更新)")
    private LocalDateTime refundedAt;

    @Schema(description = "是否已扣回教师收入")
    private Boolean teacherIncomeDeducted;

    @Schema(description = "申请创建时间(UTC)")
    private LocalDateTime createTime;

    @Schema(description = "更新时间(UTC)")
    private LocalDateTime updateTime;
}
