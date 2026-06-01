package com.mandarly.boot.module.edu.controller.admin.schedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Schema(description = "管理后台 - 教师周排课 创建/更新 Request VO")
@Data
public class TeacherScheduleSaveReqVO {

    @Schema(description = "排课 id;创建时为空,更新时必填", example = "1")
    private Long id;

    @Schema(description = "教师 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "teacherId 不能为空")
    private Long teacherId;

    @Schema(description = "周几;0=周日 6=周六", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "weekday 不能为空")
    @Min(value = 0, message = "weekday 范围 0-6")
    @Max(value = 6, message = "weekday 范围 0-6")
    private Integer weekday;

    @Schema(description = "起始时刻(教师本地时区);格式 HH:mm:ss", requiredMode = Schema.RequiredMode.REQUIRED, example = "09:00:00")
    @NotNull(message = "startTime 不能为空")
    private LocalTime startTime;

    @Schema(description = "结束时刻;同日内,end > start", requiredMode = Schema.RequiredMode.REQUIRED, example = "18:00:00")
    @NotNull(message = "endTime 不能为空")
    private LocalTime endTime;

    @Schema(description = "时区(IANA)", requiredMode = Schema.RequiredMode.REQUIRED, example = "Asia/Hong_Kong")
    @NotBlank(message = "timezone 不能为空")
    @Size(max = 64)
    private String timezone;
}
