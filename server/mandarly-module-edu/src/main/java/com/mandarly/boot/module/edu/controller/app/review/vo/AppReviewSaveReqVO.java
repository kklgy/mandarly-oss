package com.mandarly.boot.module.edu.controller.app.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "App - 提交评价请求")
@Data
public class AppReviewSaveReqVO {

    @Schema(description = "课程订单 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "orderId 不能为空")
    private Long orderId;

    @Schema(description = "1-5 星", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "rating 不能为空")
    @Min(value = 1, message = "rating 最小 1")
    @Max(value = 5, message = "rating 最大 5")
    private Integer rating;

    @Schema(description = "评价正文,可空")
    @Size(max = 1024, message = "content 最长 1024")
    private String content;

    @Schema(description = "标签数组,白名单 0-5 个;白名单见 platform_config.review.tag_dict")
    @Size(max = 5, message = "最多选 5 个预设标签")
    private List<String> tags;

    @Schema(description = "匿名评价(Wave 5),true 时公开列表展示'匿名学员'")
    private Boolean isAnonymous;

    @Schema(description = "用户自定义标签,最多 3 条 × 8 字符")
    @Size(max = 3, message = "最多 3 条自定义标签")
    private List<String> customTags;

}
