package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "App 端 - 解绑三方账号 Request VO")
@Data
public class AppAuthUnbindSocialReqVO {

    @Schema(description = "三方平台类型 google / apple", requiredMode = Schema.RequiredMode.REQUIRED, example = "google")
    @NotBlank(message = "三方平台类型不能为空")
    @Pattern(regexp = "^(google|apple)$", message = "三方平台类型必须为 google 或 apple")
    private String provider;

}
