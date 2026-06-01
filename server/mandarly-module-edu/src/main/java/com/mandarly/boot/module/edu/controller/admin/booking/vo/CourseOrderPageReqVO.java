package com.mandarly.boot.module.edu.controller.admin.booking.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 课程订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseOrderPageReqVO extends PageParam {

    @Schema(description = "学生 user.id", example = "1024")
    private Long studentId;

    @Schema(description = "教师 user.id", example = "2048")
    private Long teacherId;

    @Schema(description = "订单状态", example = "upcoming")
    private String status;

    @Schema(description = "上课时间起点(UTC)", example = "2026-05-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledFrom;

    @Schema(description = "上课时间终点(UTC)", example = "2026-06-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledTo;
}
