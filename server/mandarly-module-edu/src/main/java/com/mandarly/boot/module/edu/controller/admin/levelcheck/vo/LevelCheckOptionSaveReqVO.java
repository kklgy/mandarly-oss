package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 选项 创建/更新 Request VO")
@Data
public class LevelCheckOptionSaveReqVO {

    @Schema(description = "id;创建为空,更新必填")
    private Long id;

    @Schema(description = "题目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "questionId 不能为空")
    private Long questionId;

    @Schema(description = "选项编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "beginner")
    @NotBlank(message = "optionCode 不能为空")
    @Size(max = 32)
    private String optionCode;

    @Schema(description = "选项文案多语言 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "level_check.q1.opt.beginner")
    @NotBlank(message = "optionI18nCode 不能为空")
    @Size(max = 64)
    private String optionI18nCode;

    @Schema(description = "硬约束:必须命中的教师 expertise(数组),可空")
    private List<String> matchExpertise;

    @Schema(description = "软约束打分规则,可空", example = "[{\"expertise\":\"business\",\"score\":10}]")
    private List<Map<String, Object>> scoreRules;

    @Schema(description = "Q1 用,推断等级 beginner/intermediate/advanced", example = "beginner")
    private String inferredLevel;

    @Schema(description = "Q3 用,推荐套餐每周课次", example = "1")
    private Integer recommendedWeeklyCount;

    @Schema(description = "排序", example = "10")
    private Integer sort;
}
