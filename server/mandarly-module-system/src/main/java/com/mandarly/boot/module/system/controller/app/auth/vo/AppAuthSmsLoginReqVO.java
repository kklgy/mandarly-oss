package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "App 端 - 手机验证码登录 Request VO")
@Data
public class AppAuthSmsLoginReqVO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "+85298765432")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\+?[0-9]{8,16}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度为 4-8 位")
    private String code;

    @Schema(description = "语言偏好(可选,需与发码时一致)", example = "zh-HK")
    private String locale;

}
