package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "App 端 - 三方登录跳转 URL Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppAuthSocialRedirectRespVO {

    @Schema(description = "跳转 URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

}
