package com.mandarly.boot.module.edu.controller.app.teacher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户端 - 教师列表项 VO(脱敏)")
@Data
public class AppTeacherSimpleRespVO {

    @Schema(description = "教师 user.id", example = "1024")
    private Long userId;

    @Schema(description = "教师昵称")
    private String nickname;

    @Schema(description = "教师头像 URL")
    private String avatar;

    @Schema(description = "教师 IANA 时区", example = "Asia/Hong_Kong")
    private String teacherTimezone;

    @Schema(description = "自我介绍")
    private String intro;

    @Schema(description = "教学方向")
    private List<String> expertise;

    @Schema(description = "口音(M5 patch 后扩 List<String>;过渡期前端兼容 String / List)")
    private String accent;

    @Schema(description = "会的语言")
    private List<String> languages;

    @Schema(description = "教学年限")
    private Integer yearsExperience;

    @Schema(description = "自我介绍视频 URL")
    private String introVideoUrl;

    @Schema(description = "是否有自我介绍视频")
    private Boolean hasIntroVideo;

    // ---- M5 Wave 4 字段(SQL patch 落地后由 service 填充)----

    @Schema(description = "教师标签(M5 patch teacher_profile.tags JSON)")
    private List<String> tags;

    @Schema(description = "推荐排序权重(M5 patch teacher_profile.recommend_weight)")
    private Integer recommendWeight;

    @Schema(description = "平均评分(0-5)")
    private java.math.BigDecimal avgRating;

    @Schema(description = "评价数")
    private Integer reviewCount;

    @Schema(description = "已完成订单数(用于排序)")
    private Integer finishedOrderCount;

    @Schema(description = "今日可约时段数;> 0 时前端浮签 todayAvailable")
    private Integer todayAvailableCount;

    @Schema(description = "徽章(todayAvailable / newTeacher),前端 TeacherCard 用")
    private List<String> badges;

    @Schema(description = "审核通过时间(用于 newTeacher 徽章判断)")
    private java.time.LocalDateTime auditedAt;

    @Schema(description = "老师真人档案是否对当前请求可见;未登录游客为 false")
    private Boolean profileVisible;
}
