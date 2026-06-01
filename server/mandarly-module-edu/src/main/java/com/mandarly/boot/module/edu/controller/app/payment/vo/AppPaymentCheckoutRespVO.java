package com.mandarly.boot.module.edu.controller.app.payment.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 学生发起套餐购买响应 VO
 */
@Data
@Builder
public class AppPaymentCheckoutRespVO {

    /** 支付单 ID */
    private Long paymentId;

    /** Stripe Checkout Session URL,前端直接跳转 */
    private String checkoutUrl;

    /** 优惠金额(USD),0 表示无优惠 */
    private java.math.BigDecimal discountAmountUsd;
}
