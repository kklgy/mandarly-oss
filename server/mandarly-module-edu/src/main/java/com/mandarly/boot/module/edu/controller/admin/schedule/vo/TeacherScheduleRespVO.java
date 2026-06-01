package com.mandarly.boot.module.edu.controller.admin.schedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "教师周排课 Response VO")
@Data
public class TeacherScheduleRespVO {

    @Schema(description = "id", example = "1")
    private Long id;

    @Schema(description = "教师 user.id", example = "1024")
    private Long teacherId;

    @Schema(description = "周几;0=周日 6=周六")
    private Integer weekday;

    @Schema(description = "起始时刻")
    private LocalTime startTime;

    @Schema(description = "结束时刻")
    private LocalTime endTime;

    @Schema(description = "时区")
    private String timezone;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
