package com.mandarly.boot.module.edu.controller.admin.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 客服咨询指标 Response VO")
@Data
public class SupportInquiryStatsRespVO {

    @Schema(description = "全部交互日志数(提问 + 纯渠道点击)")
    private Long totalCount;

    @Schema(description = "真实提问数(question_text 非空)")
    private Long askCount;

    @Schema(description = "命中 FAQ 数(分子取自 askCount)")
    private Long matchedCount;

    @Schema(description = "未命中数(askCount - matchedCount)")
    private Long unmatchedCount;

    @Schema(description = "转人工点击数(包含纯点击 + 问完后再点击)")
    private Long clickedToHumanCount;

    @Schema(description = "直接点客服联系方式数(question_text 为空)")
    private Long directClickCount;

    @Schema(description = "FAQ 命中率 = matchedCount / askCount")
    private BigDecimal matchRate;

    @Schema(description = "转人工点击率 = clickedToHumanCount / totalCount")
    private BigDecimal clickRate;
}
