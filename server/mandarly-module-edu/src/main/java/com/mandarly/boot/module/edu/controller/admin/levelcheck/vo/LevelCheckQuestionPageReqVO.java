package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 水平测试题目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class LevelCheckQuestionPageReqVO extends PageParam {

    @Schema(description = "题目编码", example = "q1_level")
    private String questionCode;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive;
}
