package com.mandarly.boot.module.edu.framework.stripe.client;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建 Stripe Checkout Session 请求 VO
 */
@Data
@Builder
public class CreateSessionRequest {

    /** 支付单 ID,用于生成 idempotency-key = "payment-{paymentId}" */
    Long paymentId;

    Long userId;

    Long packageId;

    /** 套餐名称多语言 key 或展示文案 */
    String packageNameI18n;

    /** 最终金额(套餐币种,已扣 discount) */
    BigDecimal unitAmount;

    /** Stripe 付款币种,使用套餐记录自己的 currency */
    String currency;

    String successUrl;

    String cancelUrl;

    /** Session 有效期(分钟) */
    Integer expiresInMinutes;
}
