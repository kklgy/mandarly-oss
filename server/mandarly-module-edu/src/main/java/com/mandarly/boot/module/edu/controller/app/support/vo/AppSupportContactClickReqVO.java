package com.mandarly.boot.module.edu.controller.app.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "用户端 - 客服转人工点击 Request VO")
@Data
public class AppSupportContactClickReqVO {

    @Schema(description = "咨询日志 id;Phase 1 直接渠道点击时可空,二期接 chat UI 后由 ask 接口返回")
    private Long logId;

    @Schema(description = "联系方式 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "contactId 不能为空")
    private Long contactId;

    @Schema(description = "浏览器会话 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "sessionId 不能为空")
    @Size(max = 64)
    private String sessionId;

    @Schema(description = "locale", example = "zh-CN")
    @Size(max = 16)
    private String locale;

    @Schema(description = "market", example = "HK")
    @Size(max = 16)
    private String market;
}
