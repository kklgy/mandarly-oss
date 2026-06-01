package com.mandarly.boot.module.edu.dal.dataobject.payment;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Stripe webhook 事件去重 + 审计
 *
 * <p>主键 = Stripe evt_xxx,INSERT IGNORE 即去重
 * <p>非 TenantBaseDO(全局共享,无租户)
 */
@TableName("stripe_event")
@Data
public class StripeEventDO {

    @TableId
    private String id;

    private String type;
    private String payload;
    private LocalDateTime processedAt;
    private String result;
    private String errorMsg;
    private LocalDateTime createdAt;
}
