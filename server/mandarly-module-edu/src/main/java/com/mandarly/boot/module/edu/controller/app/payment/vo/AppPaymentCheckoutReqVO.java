package com.mandarly.boot.module.edu.controller.app.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "App 端 - 发起套餐购买 Request VO")
@Data
public class AppPaymentCheckoutReqVO {

    @Schema(description = "套餐 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "套餐 ID 不能为空")
    private Long packageId;

    @Schema(description = "前端展示币种(可空;必须与套餐币种一致)", example = "HKD")
    private String currency;

    @Schema(description = "推荐码(可空,空则无折扣)", example = "ABC123")
    private String referralCode;
}
