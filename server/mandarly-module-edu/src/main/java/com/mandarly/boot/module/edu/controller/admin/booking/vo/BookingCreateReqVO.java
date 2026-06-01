package com.mandarly.boot.module.edu.controller.admin.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "下单 Request VO(管理员代下 / 学生本人)")
@Data
public class BookingCreateReqVO {

    @Schema(description = "学生 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "studentId 不能为空")
    private Long studentId;

    @Schema(description = "教师 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "teacherId 不能为空")
    private Long teacherId;

    @Schema(description = "上课时间(UTC)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05-20T10:00:00")
    @NotNull(message = "scheduledAt 不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledAt;

    @Schema(description = "指定使用某个 student_package(选填,缺省按扣次优先级算法自动选);免费体验课传 null", example = "12")
    private Long studentPackageId;
}
