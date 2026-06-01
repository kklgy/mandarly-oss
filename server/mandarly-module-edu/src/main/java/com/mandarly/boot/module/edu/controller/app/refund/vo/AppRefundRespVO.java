package com.mandarly.boot.module.edu.controller.app.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "App 端 - 退款工单 Response VO")
@Data
public class AppRefundRespVO {

    @Schema(description = "退款工单 ID")
    private Long id;

    @Schema(description = "关联支付单 ID")
    private Long paymentId;

    @Schema(description = "申请原因")
    private String applyReason;

    @Schema(description = "建议退款金额(USD),系统按剩余课次比例计算")
    private BigDecimal suggestedAmountUsd;

    @Schema(description = "最终退款金额(USD),管理员审核后确定")
    private BigDecimal finalAmountUsd;

    @Schema(description = "状态:pending/approved/rejected/refunded")
    private String status;

    @Schema(description = "审核备注(管理员填写,rejected 时为拒绝理由)")
    private String auditNote;

    @Schema(description = "实际退款到账时间(UTC,Stripe charge.refunded 触发后更新)")
    private LocalDateTime refundedAt;

    @Schema(description = "申请创建时间(UTC)")
    private LocalDateTime createTime;
}
