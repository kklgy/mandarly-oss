package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "水平测试选项 Response VO")
@Data
public class LevelCheckOptionRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "题目 id")
    private Long questionId;

    @Schema(description = "选项编码")
    private String optionCode;

    @Schema(description = "选项文案多语言 key")
    private String optionI18nCode;

    @Schema(description = "硬约束教师 expertise 数组")
    private List<String> matchExpertise;

    @Schema(description = "软约束打分规则")
    private List<Map<String, Object>> scoreRules;

    @Schema(description = "Q1 时推断的等级")
    private String inferredLevel;

    @Schema(description = "Q3 时推荐套餐每周课次")
    private Integer recommendedWeeklyCount;

    @Schema(description = "排序")
    private Integer sort;
}
