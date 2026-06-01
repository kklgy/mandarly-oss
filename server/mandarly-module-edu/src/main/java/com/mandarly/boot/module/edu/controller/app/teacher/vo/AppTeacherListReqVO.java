package com.mandarly.boot.module.edu.controller.app.teacher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端 - 教师列表查询参数(Wave 4 Subagent E)
 *
 * <p>来源:docs/frontend/visual-reference/DESIGN-mandarly-v1.md § C / § D
 * (TeacherListView 改造 第 6 轮)+ docs/progress/M5-用户端UX大改造-plan.md § 6
 *
 * <p>各字段 nullable;前端逗号分隔多选(如 accent="mandarin_cn,cantonese")。
 */
@Schema(description = "用户端 - 教师列表查询参数")
@Data
public class AppTeacherListReqVO {

    @Schema(description = "关键词,模糊匹配 nickname / intro", example = "张老师")
    private String keyword;

    @Schema(description = "口音,逗号分隔多选(枚举:mandarin_cn / mandarin_tw / cantonese)",
            example = "mandarin_cn,cantonese")
    private String accent;

    @Schema(description = "价格区间,逗号分隔多选(枚举:lt200 / 200-500 / 500-1000 / gt1000)",
            example = "lt200,200-500")
    private String priceBuckets;

    @Schema(description = "今日可约;true 仅返今天有可约时段的教师", example = "true")
    private Boolean available;

    @Schema(description = "最低评分(0-5,step 0.5)", example = "4.5")
    private BigDecimal minRating;

    @Schema(description = "教学方向,逗号分隔多选(枚举:business / daily / kids / HSK / speaking)",
            example = "business,HSK")
    private String expertise;

    @Schema(description = "教师标签,逗号分隔多选(枚举:beginner / kids / hasVideo)",
            example = "beginner,hasVideo")
    private String tags;

    @Schema(description = "排序(recommend / rating_desc / price_asc / price_desc / review_count_desc)",
            example = "recommend")
    private String sort;

    @Schema(description = "页码,默认 1", example = "1")
    private Integer pageNo;

    @Schema(description = "每页大小,默认 24", example = "24")
    private Integer pageSize;

}
