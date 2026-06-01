package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 4 渠道凭证就位状态(M2.5 §6.5 toggle 探测)
 *
 * 前端 LoginView/RegisterView/TeacherRegisterView 据此 disable 不可用的 tab/按钮。
 */
@Schema(description = "App 端 - 登录/注册 4 渠道可用状态 Resp VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthChannelsRespVO {

    @Schema(description = "邮箱密码登录/邮箱注册(SMTP 凭证就位)", example = "true")
    private Boolean email;

    @Schema(description = "手机短信登录/手机注册(任一 sms_channel status=0 + api_key 非 PENDING)", example = "true")
    private Boolean sms;

    @Schema(description = "Google OAuth(client-id/secret 就位)", example = "false")
    private Boolean google;

    @Schema(description = "Apple Sign In(team-id / service-id / key-id / private-key-path 就位)", example = "false")
    private Boolean apple;

}
