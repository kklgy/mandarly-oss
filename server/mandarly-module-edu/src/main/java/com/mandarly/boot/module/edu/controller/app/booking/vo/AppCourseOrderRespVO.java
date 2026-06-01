package com.mandarly.boot.module.edu.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户端 - 课程订单 Response VO(成功页 / 我的订单)")
@Data
public class AppCourseOrderRespVO {

    @Schema(description = "订单 id")
    private Long id;

    @Schema(description = "学生 user.id")
    private Long studentId;

    @Schema(description = "教师 user.id")
    private Long teacherId;

    @Schema(description = "教师昵称(便于前端单接口直出)")
    private String teacherNickname;

    @Schema(description = "扣次来源 student_package.id")
    private Long studentPackageId;

    @Schema(description = "套餐显示名(经 i18n 翻译)")
    private String packageName;

    @Schema(description = "上课时间(UTC ISO),前端按 user 时区展示")
    private LocalDateTime scheduledAt;

    @Schema(description = "课时长度(分钟)")
    private Integer duration;

    @Schema(description = "展示价")
    private BigDecimal priceDisplay;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "状态 upcoming/cancelled/finished")
    private String status;

    @Schema(description = "是否免费体验课订单")
    private Boolean isFreeTrial;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
