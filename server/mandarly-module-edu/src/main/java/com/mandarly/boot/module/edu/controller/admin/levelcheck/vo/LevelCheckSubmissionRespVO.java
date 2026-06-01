package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "答卷 Response VO")
@Data
public class LevelCheckSubmissionRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户 id(可空)")
    private Long userId;

    @Schema(description = "session id")
    private String sessionId;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "语言")
    private String locale;

    @Schema(description = "市场")
    private String market;

    @Schema(description = "答案数组")
    private List<Map<String, String>> answers;

    @Schema(description = "推断等级")
    private String inferredLevel;

    @Schema(description = "推荐教师 ids")
    private List<Long> recommendedTeacherIds;

    @Schema(description = "推荐套餐 id")
    private Long recommendedPackageId;

    @Schema(description = "是否已转化下单")
    private Boolean isConverted;

    @Schema(description = "转化的订单 id")
    private Long convertedOrderId;

    @Schema(description = "答卷时间")
    private LocalDateTime createTime;
}
