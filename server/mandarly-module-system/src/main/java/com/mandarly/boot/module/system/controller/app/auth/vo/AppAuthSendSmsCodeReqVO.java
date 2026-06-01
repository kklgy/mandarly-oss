package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "App 端 - 发送手机验证码 Request VO")
@Data
public class AppAuthSendSmsCodeReqVO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "+85298765432")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\+?[0-9]{8,16}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "场景 register / login / reset", requiredMode = Schema.RequiredMode.REQUIRED, example = "login")
    @NotBlank(message = "场景不能为空")
    private String scene;

    @Schema(description = "语言偏好(可选,zh-* 走中文模板,其它走英文模板)", example = "zh-HK")
    private String locale;

}
