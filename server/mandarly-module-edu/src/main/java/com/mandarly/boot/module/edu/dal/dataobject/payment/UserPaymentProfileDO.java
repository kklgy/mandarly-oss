package com.mandarly.boot.module.edu.dal.dataobject.payment;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户支付通道 customer 映射(Stripe Customer lazy create)
 *
 * <p>M4 仅创建不更新 default_payment_method_id(二期保存卡片用)
 */
@TableName("user_payment_profile")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPaymentProfileDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String channel;
    private String channelCustomerId;
    private String defaultPaymentMethodId;
}
