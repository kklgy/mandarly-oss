package com.mandarly.boot.module.edu.dal.dataobject.payment;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单(套餐购买)
 *
 * <p>状态机:pending → paid / failed / expired,paid → refunded / partial_refunded
 * <p>amount_settled_usd 取自 Stripe balance_transaction,是教师 income 计算基数
 */
@TableName("payment")
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String type;
    private Long packageId;
    private Long studentPackageId;
    private String channel;
    private String channelSessionId;
    private String channelPaymentIntentId;
    private String channelChargeId;
    private String paymentMethodType;
    private BigDecimal amountRequest;
    private String currencyRequest;
    private BigDecimal amountPaid;
    private String currencyPaid;
    private BigDecimal amountSettledUsd;
    private BigDecimal discountAmountUsd;
    private Long referrerUserId;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;
    private String successUrl;
    private String cancelUrl;
}
