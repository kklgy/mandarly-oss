package com.mandarly.boot.module.edu.controller.app.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "用户端 - 客服提问 Request VO")
@Data
public class AppSupportAskReqVO {

    @Schema(description = "浏览器会话 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "sessionId 不能为空")
    @Size(max = 64)
    private String sessionId;

    @Schema(description = "locale", requiredMode = Schema.RequiredMode.REQUIRED, example = "zh-CN")
    @NotBlank(message = "locale 不能为空")
    @Size(max = 16)
    private String locale;

    @Schema(description = "market", example = "HK")
    @Size(max = 16)
    private String market;

    @Schema(description = "用户问题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "question 不能为空")
    @Size(max = 1024)
    private String question;
}
