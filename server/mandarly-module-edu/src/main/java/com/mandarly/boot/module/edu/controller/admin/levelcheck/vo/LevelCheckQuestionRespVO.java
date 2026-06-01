package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "水平测试题目 Response VO(含选项嵌套)")
@Data
public class LevelCheckQuestionRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "题目编码")
    private String questionCode;

    @Schema(description = "题干多语言 key")
    private String questionI18nCode;

    @Schema(description = "题型")
    private String questionType;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "选项列表(按 sort 升序)")
    private List<LevelCheckOptionRespVO> options;
}
