package com.mandarly.boot.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "App 端 - 三方账号登录 Request VO")
@Data
public class AppAuthSocialLoginReqVO {

    @Schema(description = "三方平台类型 google / apple", requiredMode = Schema.RequiredMode.REQUIRED, example = "google")
    @NotBlank(message = "三方平台类型不能为空")
    @Pattern(regexp = "^(google|apple)$", message = "三方平台类型必须为 google 或 apple")
    private String type;

    @Schema(description = "授权码", requiredMode = Schema.RequiredMode.REQUIRED, example = "4/0AX4...")
    @NotBlank(message = "授权码不能为空")
    private String code;

    @Schema(description = "state 参数", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc123")
    @NotBlank(message = "state 不能为空")
    private String state;

    @Schema(description = "角色 student / teacher,默认 student", example = "student")
    @Pattern(regexp = "^(student|teacher)$", message = "角色必须为 student 或 teacher")
    private String role = "student";

}
