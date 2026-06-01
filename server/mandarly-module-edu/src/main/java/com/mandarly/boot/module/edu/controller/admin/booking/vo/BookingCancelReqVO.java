package com.mandarly.boot.module.edu.controller.admin.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "取消订单 Request VO")
@Data
public class BookingCancelReqVO {

    @Schema(description = "订单 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "orderId 不能为空")
    private Long orderId;

    @Schema(description = "取消方 student/teacher/admin/system", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @Pattern(regexp = "student|teacher|admin|system", message = "cancelledBy 取值不合法")
    private String cancelledBy;

    @Schema(description = "取消原因", example = "学生临时有事")
    @Size(max = 256)
    private String reason;
}
