package com.mandarly.boot.module.edu.framework.stripe.client;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建 Stripe Refund 请求 VO
 */
@Data
@Builder
public class CreateRefundRequest {

    /** 退款工单 ID,用于生成 idempotency-key = "refund-{refundId}" */
    Long refundId;

    String paymentIntentId;

    /** 退款金额(原支付币种,非 USD!) */
    BigDecimal amount;

    /** 原支付币种(如 hkd / usd) */
    String currency;
}
