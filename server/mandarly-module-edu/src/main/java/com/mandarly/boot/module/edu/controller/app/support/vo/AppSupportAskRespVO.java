package com.mandarly.boot.module.edu.controller.app.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "用户端 - 客服提问 Response VO")
@Data
public class AppSupportAskRespVO {

    @Schema(description = "咨询日志 id")
    private Long logId;

    @Schema(description = "是否命中 FAQ")
    private Boolean matched;

    @Schema(description = "命中的 FAQ id")
    private Long faqId;

    @Schema(description = "FAQ 问题")
    private String question;

    @Schema(description = "FAQ 答案 Markdown")
    private String answer;

    @Schema(description = "匹配分")
    private BigDecimal score;

    @Schema(description = "未命中提示")
    private String fallbackMessage;

    @Schema(description = "真人客服联系方式")
    private List<AppSupportContactRespVO> contacts;
}
