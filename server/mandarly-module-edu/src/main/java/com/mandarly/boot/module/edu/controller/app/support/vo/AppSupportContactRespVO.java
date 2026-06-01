package com.mandarly.boot.module.edu.controller.app.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户端 - 客服联系方式 Response VO")
@Data
public class AppSupportContactRespVO {

    @Schema(description = "联系方式 id", example = "1")
    private Long id;

    @Schema(description = "market", example = "DEFAULT")
    private String market;

    @Schema(description = "渠道类型", example = "whatsapp")
    private String channelType;

    @Schema(description = "展示文案", example = "Contact us")
    private String displayText;

    @Schema(description = "跳转链接")
    private String linkUrl;

    @Schema(description = "二维码图片 URL")
    private String imageUrl;
}
