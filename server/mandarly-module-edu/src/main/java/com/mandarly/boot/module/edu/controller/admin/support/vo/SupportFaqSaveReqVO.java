package com.mandarly.boot.module.edu.controller.admin.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 客服 FAQ 保存 Request VO")
@Data
public class SupportFaqSaveReqVO {

    @Schema(description = "FAQ id,更新时必填")
    private Long id;

    @Schema(description = "分类")
    @NotBlank(message = "分类不能为空")
    @Size(max = 32, message = "分类长度不能超过 32")
    private String category;

    @Schema(description = "语言")
    @NotBlank(message = "语言不能为空")
    @Size(max = 16, message = "语言长度不能超过 16")
    private String locale;

    @Schema(description = "问题")
    @NotBlank(message = "问题不能为空")
    @Size(max = 512, message = "问题长度不能超过 512")
    private String question;

    @Schema(description = "答案")
    @NotBlank(message = "答案不能为空")
    private String answer;

    @Schema(description = "关键字数组")
    private List<String> keywords;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态 active/disabled")
    @NotBlank(message = "状态不能为空")
    @Size(max = 16, message = "状态长度不能超过 16")
    private String status;
}
