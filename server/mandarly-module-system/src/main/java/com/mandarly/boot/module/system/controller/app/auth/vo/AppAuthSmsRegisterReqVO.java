package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "App 端 - 手机注册 Request VO")
@Data
public class AppAuthSmsRegisterReqVO {

    @Schema(description = "角色 student / teacher", requiredMode = Schema.RequiredMode.REQUIRED, example = "student")
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(student|teacher)$", message = "角色必须为 student 或 teacher")
    private String role;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "+85298765432")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\+?[0-9]{8,16}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "短信验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "654321")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度为 4-8 位")
    private String code;

    @Schema(description = "昵称(可选)", example = "Bob")
    private String nickname;

    @Schema(description = "语言偏好(可选)", example = "zh-HK")
    private String locale;

    @Schema(description = "时区(可选)", example = "Asia/Hong_Kong")
    private String tz;

    @Schema(description = "推荐码(可选)", example = "BOB456")
    private String refCode;

}
