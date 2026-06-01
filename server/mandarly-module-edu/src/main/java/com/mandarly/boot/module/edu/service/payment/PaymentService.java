package com.mandarly.boot.module.edu.service.payment;

import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.app.payment.vo.AppPaymentCheckoutRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;

/**
 * 支付服务接口(套餐购买 Flow 1)
 *
 * <p>职责:
 * <ul>
 *   <li>createCheckout:学生购买套餐 → 推荐码 → StripeClient → 返回 checkout_url</li>
 *   <li>handleCheckoutCompleted / handleSessionExpired / handlePaymentFailed:webhook 回调</li>
 *   <li>getMyPayment / getMyPaymentPage:学生查自己的支付记录</li>
 * </ul>
 */
public interface PaymentService {

    /**
     * 学生发起套餐购买
     *
     * @param userId      学生 user.id
     * @param packageId   套餐 ID
     * @param referralCode 推荐码(可空)
     * @return 含 checkout_url 的 VO
     */
    AppPaymentCheckoutRespVO createCheckout(Long userId, Long packageId, String requestedCurrency, String referralCode);

    /**
     * 查自己的单笔支付(success_url 轮询用)
     *
     * @param userId    学生 user.id
     * @param paymentId 支付单 ID
     * @return PaymentDO
     * @throws com.mandarly.boot.framework.common.exception.ServiceException PAYMENT_NOT_FOUND
     */
    PaymentDO getMyPayment(Long userId, Long paymentId);

    /**
     * 分页查我的支付历史
     */
    PageResult<PaymentDO> getMyPaymentPage(Long userId, PageParam pageParam);

    /**
     * webhook: checkout.session.completed → 标记 paid + 回填 Stripe 字段 + 创建 student_package
     *
     * @param session Stripe Checkout Session 对象(从 webhook event 反序列化得到,
     *                需用 session.getPaymentIntent() 拿 paymentIntentId 二次拉取 charge + balance_transaction)
     */
    void handleCheckoutCompleted(com.stripe.model.checkout.Session session);

    /**
     * webhook: checkout.session.expired → 标记 expired
     */
    void handleSessionExpired(String sessionId);

    /**
     * webhook: payment_intent.payment_failed → 标记 failed
     */
    void handlePaymentFailed(String paymentIntentId);

    /**
     * webhook: charge.updated → 异步回填 amountSettledUsd
     *
     * <p>session.completed 时 Stripe balance_transaction 通常尚未 ready
     * (异步生成,几秒到几分钟),所以 handleCheckoutCompleted 落地的 amountSettledUsd 经常为 NULL。
     * 本方法是兜底:监听 charge.updated 事件,balance_transaction ready 后回填。
     *
     * <p>幂等:如果 amountSettledUsd 已有值则 skip;balance_transaction 还没 ready 则 skip
     * (下一次 charge.updated 会再触发)。
     *
     * @param charge Stripe Charge 对象(从 webhook 反序列化得到)
     */
    void handleChargeUpdated(com.stripe.model.Charge charge);
}
