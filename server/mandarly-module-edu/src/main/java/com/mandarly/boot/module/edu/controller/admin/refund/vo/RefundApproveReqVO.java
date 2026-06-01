package com.mandarly.boot.module.edu.controller.admin.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 审批退款(approve) Request VO")
@Data
public class RefundApproveReqVO {

    @Schema(description = "最终退款金额(USD)", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.00")
    @NotNull(message = "最终退款金额不能为空")
    @DecimalMin(value = "0.00", message = "最终退款金额不能为负数")
    private BigDecimal finalAmountUsd;

    @Schema(description = "调整原因(final ≠ suggested 时必填)", example = "扣除已消费课次")
    private String adjustReason;

    @Schema(description = "审核备注", example = "已确认学生申请原因,批准全额退款")
    private String auditNote;
}
