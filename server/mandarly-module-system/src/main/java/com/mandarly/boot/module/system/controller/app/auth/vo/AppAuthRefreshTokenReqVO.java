package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "App 端 - 刷新 Token Request VO")
@Data
public class AppAuthRefreshTokenReqVO {

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "refresh-token-abc")
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

}
