package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "App 端 - 重置密码 Request VO")
@Data
public class AppAuthResetPasswordReqVO {

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "alice@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度为 4-8 位")
    private String code;

    @Schema(description = "新密码(8-64位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "NewPass1234!")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度为 8-64 位")
    private String newPassword;

}
