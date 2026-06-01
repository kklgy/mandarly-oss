package com.mandarly.boot.module.edu.controller.app.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "App 端 - 支付订单 Response VO(不暴露 Stripe 内部字段)")
@Data
public class AppPaymentRespVO {

    @Schema(description = "支付单 ID")
    private Long id;

    @Schema(description = "套餐 ID")
    private Long packageId;

    @Schema(description = "套餐名称(含 i18n,英文 fallback)")
    private String packageName;

    @Schema(description = "请求金额(USD)")
    private BigDecimal amountRequest;

    @Schema(description = "实际付款金额(原支付币种)")
    private BigDecimal amountPaid;

    @Schema(description = "付款币种,如 HKD/USD")
    private String currencyPaid;

    @Schema(description = "优惠金额(USD),0 表示无优惠")
    private BigDecimal discountAmountUsd;

    @Schema(description = "状态:pending/paid/failed/expired/refunded/partial_refunded")
    private String status;

    @Schema(description = "支付时间(UTC,pending 时为 null)")
    private LocalDateTime paidAt;

    @Schema(description = "创建时间(UTC)")
    private LocalDateTime createTime;
}
