package com.mandarly.boot.module.system.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "App 端 - 绑定手机 Request VO")
@Data
public class AppUserBindPhoneReqVO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\+?[0-9]{8,16}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8)
    private String code;
}
