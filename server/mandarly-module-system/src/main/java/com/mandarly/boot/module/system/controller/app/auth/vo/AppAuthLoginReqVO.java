package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "App 端 - 邮箱密码登录 Request VO")
@Data
public class AppAuthLoginReqVO {

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "alice@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "Pass1234")
    @NotBlank(message = "密码不能为空")
    private String password;

}
