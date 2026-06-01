package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 题目 创建/更新 Request VO")
@Data
public class LevelCheckQuestionSaveReqVO {

    @Schema(description = "id;创建为空,更新必填", example = "1")
    private Long id;

    @Schema(description = "题目编码,如 q1_level", requiredMode = Schema.RequiredMode.REQUIRED, example = "q1_level")
    @NotBlank(message = "questionCode 不能为空")
    @Size(max = 32)
    private String questionCode;

    @Schema(description = "题干多语言 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "level_check.q1.text")
    @NotBlank(message = "questionI18nCode 不能为空")
    @Size(max = 64)
    private String questionI18nCode;

    @Schema(description = "题型,一期固定 single_choice", example = "single_choice")
    private String questionType;

    @Schema(description = "排序", example = "10")
    private Integer sort;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive;
}
