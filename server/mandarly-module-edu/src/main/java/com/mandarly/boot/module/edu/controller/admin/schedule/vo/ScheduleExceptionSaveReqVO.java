package com.mandarly.boot.module.edu.controller.admin.schedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "管理后台 - 排课例外 创建 Request VO")
@Data
public class ScheduleExceptionSaveReqVO {

    @Schema(description = "教师 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "teacherId 不能为空")
    private Long teacherId;

    @Schema(description = "例外日期(教师本地时区下)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05-20")
    @NotNull(message = "exceptionDate 不能为空")
    private LocalDate exceptionDate;

    @Schema(description = "类型:closed=临时不可约 / extra=额外可约时段", requiredMode = Schema.RequiredMode.REQUIRED, example = "closed")
    @NotBlank(message = "type 不能为空")
    @Pattern(regexp = "closed|extra", message = "type 仅支持 closed / extra")
    private String type;

    @Schema(description = "起始时刻;closed 可空(整天)/ extra 必填", example = "09:00:00")
    private LocalTime startTime;

    @Schema(description = "结束时刻;closed 可空(整天)/ extra 必填", example = "12:00:00")
    private LocalTime endTime;

    @Schema(description = "时区(IANA)", requiredMode = Schema.RequiredMode.REQUIRED, example = "Asia/Hong_Kong")
    @NotBlank(message = "timezone 不能为空")
    @Size(max = 64)
    private String timezone;

    @Schema(description = "备注", example = "临时请假")
    @Size(max = 256)
    private String reason;
}
