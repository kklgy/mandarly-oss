package com.mandarly.boot.module.edu.controller.admin.payment.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 支付订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentPageReqVO extends PageParam {

    @Schema(description = "学生 user.id", example = "1024")
    private Long userId;

    @Schema(description = "套餐 ID", example = "1")
    private Long packageId;

    @Schema(description = "状态:pending/paid/failed/expired/refunded/partial_refunded", example = "paid")
    private String status;

    @Schema(description = "创建时间起点(UTC)", example = "2026-05-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createTimeFrom;

    @Schema(description = "创建时间终点(UTC)", example = "2026-06-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createTimeTo;
}
