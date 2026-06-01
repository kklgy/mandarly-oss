package com.mandarly.boot.module.edu.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "用户端 - 预约创建 Request VO(M2.5:studentId 由后端从 token 取)")
@Data
public class AppBookingCreateReqVO {

    @Schema(description = "教师 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2001")
    @NotNull(message = "teacherId 不能为空")
    private Long teacherId;

    @Schema(description = "上课时间(UTC ISO),如 2026-05-20T10:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05-20T10:00:00")
    @NotNull(message = "scheduledAt 不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledAt;

    @Schema(description = "指定使用某个 student_package(选填,缺省按扣次优先级算法自动选)",
            example = "1")
    private Long studentPackageId;
}
