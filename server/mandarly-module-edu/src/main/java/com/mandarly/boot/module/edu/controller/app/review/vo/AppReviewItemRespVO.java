package com.mandarly.boot.module.edu.controller.app.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "App - 评价单条")
@Data
public class AppReviewItemRespVO {

    @Schema(description = "课程订单 id(也是评价主键)")
    private Long orderId;

    @Schema(description = "学生 user.id")
    private Long studentId;

    @Schema(description = "学生昵称(脱敏:首字母 + ***)")
    private String studentDisplayName;

    @Schema(description = "教师 user.id")
    private Long teacherId;

    @Schema(description = "1-5")
    private Integer rating;

    @Schema(description = "评价正文")
    private String content;

    @Schema(description = "标签数组(白名单 key,前端用 i18n 解 review.tag.*)")
    private List<String> tags;

    @Schema(description = "首次提交时间")
    private LocalDateTime createTime;

    @Schema(description = "最后编辑时间")
    private LocalDateTime lastEditedAt;

    @Schema(description = "1=显示 / 0=隐藏(公开列表只返 1)")
    private Boolean isVisible;

    @Schema(description = "匿名评价(Wave 5);公开列表 true 时 studentDisplayName 已被脱敏成'匿名学员'")
    private Boolean isAnonymous;

    @Schema(description = "编辑窗口截止时间(create + 72h),前端 EditWindowProgress 读")
    private LocalDateTime editableUntilAt;

    @Schema(description = "用户自定义标签(最多 3 条 × 8 字符)")
    private List<String> customTags;

}
