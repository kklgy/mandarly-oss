package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "App 端 - 登录 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthLoginRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "角色 student / teacher", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "访问令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expiresTime;

}
