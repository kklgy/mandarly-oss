package com.mandarly.boot.module.edu.framework.stripe.client;

import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;

/**
 * Stripe API 封装接口
 *
 * <p>所有 Stripe 调用统一走此接口,便于测试 mock 和后续多渠道扩展。
 */
public interface StripeClient {

    /**
     * 创建 Checkout Session
     *
     * @param req 请求参数(含 paymentId → idempotency-key = "payment-{paymentId}")
     * @return Stripe Session 对象
     */
    Session createCheckoutSession(CreateSessionRequest req);

    /**
     * 查询 PaymentIntent,同时 expand latest_charge.balance_transaction
     *
     * @param paymentIntentId Stripe PaymentIntent ID
     * @return PaymentIntent(含 charge + balance_transaction 子对象)
     */
    PaymentIntent retrievePaymentIntent(String paymentIntentId);

    /**
     * 查询 Charge,expand balance_transaction(charge.updated 异步 settle 兜底用)
     *
     * @param chargeId Stripe Charge ID
     * @return Charge(含 balance_transaction 子对象,balance_transaction 未 ready 时 amount=null)
     */
    Charge retrieveCharge(String chargeId);

    /**
     * 创建退款
     *
     * @param req 请求参数(含 refundId → idempotency-key = "refund-{refundId}")
     * @return Stripe Refund 对象
     */
    Refund createRefund(CreateRefundRequest req);

    /**
     * 验证 Webhook 签名
     *
     * @param payload       请求体原始字符串
     * @param sigHeader     Stripe-Signature 请求头
     * @param webhookSecret Webhook 签名密钥
     * @return 解析后的 Stripe Event
     * @throws com.mandarly.boot.framework.common.exception.ServiceException STRIPE_WEBHOOK_SIG_INVALID
     */
    Event verifyWebhookSignature(String payload, String sigHeader, String webhookSecret);

    /**
     * 通过 metadata.refund_id 反查 Stripe Refund 列表(用于孤儿对账)
     *
     * @param refundId 本地 refund.id(存储于 Stripe Refund metadata.refund_id)
     * @return 匹配的 Refund 列表(正常只有 0 或 1 条)
     */
    java.util.List<Refund> listRefundsByRefundId(Long refundId);
}
