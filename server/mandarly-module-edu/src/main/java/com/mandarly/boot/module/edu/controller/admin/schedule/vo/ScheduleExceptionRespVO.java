package com.mandarly.boot.module.edu.controller.admin.schedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "排课例外 Response VO")
@Data
public class ScheduleExceptionRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "教师 user.id")
    private Long teacherId;

    @Schema(description = "例外日期")
    private LocalDate exceptionDate;

    @Schema(description = "类型 closed/extra")
    private String type;

    @Schema(description = "起始时刻")
    private LocalTime startTime;

    @Schema(description = "结束时刻")
    private LocalTime endTime;

    @Schema(description = "时区")
    private String timezone;

    @Schema(description = "备注")
    private String reason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
