package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户端 - 教师中心:订单 Response VO(教师视角)
 *
 * <p>对应 PRD M6 §5.2 OrdersView。
 *
 * <p>字段裁剪原则:教师视角只看学生身份 + 上课时间 + 教师结算状态。
 * <strong>不暴露</strong>学生敏感信息(电话 / 邮箱 / 推荐码),不暴露 priceDisplay
 *(避免泄漏定价策略)。
 */
@Schema(description = "用户端 - 教师中心 - 订单 Response VO(教师视角)")
@Data
public class AppTeacherOrderRespVO {

    @Schema(description = "订单 id", example = "10001")
    private Long id;

    @Schema(description = "学生 user.id", example = "1024")
    private Long studentId;

    @Schema(description = "学生昵称(便于前端直出)", example = "Alice")
    private String studentNickname;

    @Schema(description = "学生头像 URL", example = "https://cdn.mandarly.com/avatars/1024.png")
    private String studentAvatar;

    @Schema(description = "上课时间(UTC ISO),前端按教师时区展示", example = "2026-05-20T09:00:00")
    private LocalDateTime scheduledAt;

    @Schema(description = "课时长度(分钟)", example = "30")
    private Integer duration;

    @Schema(description = "状态 upcoming/finished/cancelled/...", example = "upcoming")
    private String status;

    @Schema(description = "教师课时费(USD,settled 时填)", example = "3.00")
    private BigDecimal teacherAmount;

    @Schema(description = "教师结算状态 frozen/available/reverted/null(未结算)", example = "frozen")
    private String teacherSettleStatus;

    @Schema(description = "是否免费体验课订单")
    private Boolean isFreeTrial;

    @Schema(description = "结课时间(UTC)", example = "2026-05-20T09:30:00")
    private LocalDateTime finishedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
