package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "App 端 - 邮箱注册 Request VO")
@Data
public class AppAuthRegisterReqVO {

    @Schema(description = "角色 student / teacher", requiredMode = Schema.RequiredMode.REQUIRED, example = "student")
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(student|teacher)$", message = "角色必须为 student 或 teacher")
    private String role;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "alice@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "密码(8-64位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "Pass1234!")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度为 8-64 位")
    private String password;

    @Schema(description = "邮箱验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "654321")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度为 4-8 位")
    private String code;

    @Schema(description = "昵称(可选)", example = "Alice")
    private String nickname;

    @Schema(description = "语言偏好(可选)", example = "zh-HK")
    private String locale;

    @Schema(description = "时区(可选)", example = "Asia/Hong_Kong")
    private String tz;

    @Schema(description = "推荐码(可选)", example = "ALICE123")
    private String refCode;

}
