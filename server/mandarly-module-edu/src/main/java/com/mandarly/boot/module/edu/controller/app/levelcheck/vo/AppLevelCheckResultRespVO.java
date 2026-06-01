package com.mandarly.boot.module.edu.controller.app.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "App - 水平测试推荐结果 Response VO")
@Data
public class AppLevelCheckResultRespVO {

    @Schema(description = "答卷 id(用于 getResult 重看)")
    private Long submissionId;

    @Schema(description = "推断等级", example = "beginner")
    private String inferredLevel;

    @Schema(description = "推荐教师列表(Top 3,简化字段)")
    private List<RecommendedTeacher> recommendedTeachers;

    @Schema(description = "推荐套餐(可空)")
    private RecommendedPackage recommendedPackage;

    @Data
    @Schema(description = "推荐教师卡片字段")
    public static class RecommendedTeacher {
        private Long userId;
        private String nickname;
        private String avatar;
        private String intro;
        private List<String> expertise;
        private String accent;
        private List<String> languages;
        private Integer yearsExperience;
        private String introVideoUrl;
    }

    @Data
    @Schema(description = "推荐套餐字段")
    public static class RecommendedPackage {
        private Long id;
        private String nameI18nCode;
        private String name;
        private Integer weeklyCount;
        private Integer totalCount;
        private Integer validityDays;
        private BigDecimal price;
        private String currency;
        private Boolean priceVisible;
        private Boolean isFreeTrial;
    }
}
