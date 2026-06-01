package com.mandarly.boot.module.edu.controller.app.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "App 端 - 申请退款 Request VO")
@Data
public class AppRefundApplyReqVO {

    @Schema(description = "支付单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "支付单 ID 不能为空")
    private Long paymentId;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "课程不符合预期")
    @NotBlank(message = "申请原因不能为空")
    @Size(max = 500, message = "申请原因不超过 500 字")
    private String reason;
}
