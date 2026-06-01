package com.mandarly.boot.module.edu.controller.app.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "App - 提交水平测试答卷 Request VO")
@Data
public class AppLevelCheckSubmitReqVO {

    @Schema(description = "session id(浏览器 localStorage)", requiredMode = Schema.RequiredMode.REQUIRED, example = "uuid-xxx")
    @NotBlank(message = "sessionId 不能为空")
    @Size(max = 64)
    private String sessionId;

    @Schema(description = "用户 locale", requiredMode = Schema.RequiredMode.REQUIRED, example = "en")
    @NotBlank(message = "locale 不能为空")
    @Size(max = 16)
    private String locale;

    @Schema(description = "邮箱(选填,后续召回用)", example = "user@example.com")
    @Email(message = "email 格式不合法")
    @Size(max = 128)
    private String email;

    @Schema(description = "答案数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "answers 不能为空")
    @Valid
    private List<Answer> answers;

    @Data
    @Schema(description = "单题答案")
    public static class Answer {
        @Schema(description = "题目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "q1_level")
        @NotBlank
        @Size(max = 32)
        private String questionCode;

        @Schema(description = "选项编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "complete_beginner")
        @NotBlank
        @Size(max = 32)
        private String optionCode;
    }
}
