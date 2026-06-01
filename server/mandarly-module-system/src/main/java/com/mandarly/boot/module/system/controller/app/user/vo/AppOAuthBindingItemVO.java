package com.mandarly.boot.module.system.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "App 端 - 三方绑定项 VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppOAuthBindingItemVO {

    @Schema(description = "第三方平台", example = "google")
    private String provider;

    @Schema(description = "第三方绑定邮箱")
    private String oauthEmail;

    @Schema(description = "绑定时间")
    private LocalDateTime boundAt;
}
