package com.mandarly.boot.module.edu.controller.admin.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 客服联系方式保存 Request VO")
@Data
public class SupportContactSaveReqVO {

    @Schema(description = "联系方式 id,更新时必填")
    private Long id;

    @Schema(description = "市场")
    @NotBlank(message = "市场不能为空")
    @Size(max = 16, message = "市场长度不能超过 16")
    private String market;

    @Schema(description = "渠道类型")
    @NotBlank(message = "渠道类型不能为空")
    @Size(max = 16, message = "渠道类型长度不能超过 16")
    private String channelType;

    @Schema(description = "展示文案")
    @NotBlank(message = "展示文案不能为空")
    @Size(max = 128, message = "展示文案长度不能超过 128")
    private String displayText;

    @Schema(description = "外链")
    @Size(max = 512, message = "外链长度不能超过 512")
    private String linkUrl;

    @Schema(description = "二维码图片 URL")
    @Size(max = 512, message = "二维码图片 URL 长度不能超过 512")
    private String imageUrl;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用")
    private Boolean isActive;
}
