package com.mandarly.boot.module.edu.controller.admin.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 支付订单 Response VO(含完整 Stripe 字段)")
@Data
public class PaymentRespVO {

    @Schema(description = "支付单 ID")
    private Long id;

    @Schema(description = "学生 user.id")
    private Long userId;

    @Schema(description = "套餐 ID")
    private Long packageId;

    @Schema(description = "学生套餐 ID(paid 后关联)")
    private Long studentPackageId;

    @Schema(description = "支付渠道:stripe")
    private String channel;

    @Schema(description = "Stripe Checkout Session ID")
    private String channelSessionId;

    @Schema(description = "Stripe PaymentIntent ID")
    private String channelPaymentIntentId;

    @Schema(description = "Stripe Charge ID")
    private String channelChargeId;

    @Schema(description = "支付方式:card/wechat_pay/alipay/apple_pay")
    private String paymentMethodType;

    @Schema(description = "请求金额(USD)")
    private BigDecimal amountRequest;

    @Schema(description = "请求币种")
    private String currencyRequest;

    @Schema(description = "实际付款金额(原支付币种)")
    private BigDecimal amountPaid;

    @Schema(description = "付款币种,如 HKD/USD")
    private String currencyPaid;

    @Schema(description = "Stripe 结算金额(USD,来自 balance_transaction,教师收入基数)")
    private BigDecimal amountSettledUsd;

    @Schema(description = "折扣金额(USD)")
    private BigDecimal discountAmountUsd;

    @Schema(description = "推荐人 user.id")
    private Long referrerUserId;

    @Schema(description = "状态:pending/paid/failed/expired/refunded/partial_refunded")
    private String status;

    @Schema(description = "支付时间(UTC)")
    private LocalDateTime paidAt;

    @Schema(description = "过期时间(UTC,expired 时填写)")
    private LocalDateTime expiredAt;

    @Schema(description = "创建时间(UTC)")
    private LocalDateTime createTime;

    @Schema(description = "更新时间(UTC)")
    private LocalDateTime updateTime;
}
