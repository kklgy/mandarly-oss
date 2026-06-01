package com.mandarly.boot.module.edu.controller.admin.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "课程订单 Response VO")
@Data
public class CourseOrderRespVO {

    @Schema(description = "订单 id")
    private Long id;

    @Schema(description = "学生 user.id")
    private Long studentId;

    @Schema(description = "教师 user.id")
    private Long teacherId;

    @Schema(description = "扣次来源 student_package.id")
    private Long studentPackageId;

    @Schema(description = "上课时间(UTC)")
    private LocalDateTime scheduledAt;

    @Schema(description = "课时长度(分钟)")
    private Integer duration;

    @Schema(description = "展示价")
    private BigDecimal priceDisplay;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "状态 upcoming/cancelled/finished/...")
    private String status;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "异常子类(自动写入,仅 status=abnormal 时有值):meeting_missing / meeting_ongoing_overdue / lcic_init_failed / lcic_no_attendance / meeting_cancelled_orphan / meeting_unknown_status")
    private String abnormalReason;

    @Schema(description = "客服处置结论备注(admin 人工录入,仅 status=abnormal 时有值)")
    private String abnormalResolution;

    @Schema(description = "处置 abnormal 的管理员 id")
    private Long abnormalProcessedBy;

    @Schema(description = "处置 abnormal 的时间")
    private LocalDateTime abnormalProcessedAt;

    @Schema(description = "完课时间(LCIC room_end webhook 或 D28 兜底 Job 写入)")
    private LocalDateTime finishedAt;

    @Schema(description = "取消方 student/teacher/admin/system")
    private String cancelledBy;

    @Schema(description = "取消时间")
    private LocalDateTime cancelledAt;

    @Schema(description = "取消是否返还课次")
    private Boolean isRefundedClass;

    @Schema(description = "是否免费体验课订单")
    private Boolean isFreeTrial;

    @Schema(description = "教师结算金额")
    private BigDecimal teacherAmount;

    @Schema(description = "教师结算状态")
    private String teacherSettleStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
