package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "App 端 - 发送邮箱验证码 Request VO")
@Data
public class AppAuthSendEmailCodeReqVO {

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "alice@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "场景 register / login / reset / bind", requiredMode = Schema.RequiredMode.REQUIRED, example = "register")
    @NotBlank(message = "场景不能为空")
    private String scene;

    @Schema(description = "语言(zh* → 中文模板,其它 → 英文模板)", example = "zh-CN")
    private String locale;

}
