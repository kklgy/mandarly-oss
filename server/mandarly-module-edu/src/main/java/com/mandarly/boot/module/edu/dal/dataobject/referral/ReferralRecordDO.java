package com.mandarly.boot.module.edu.dal.dataobject.referral;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("referral_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReferralRecordDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long referrerUserId;
    private Long refereeUserId;
    private String referralCode;
    private Long paymentId;
    private BigDecimal refereeDiscountAmountUsd;
    private Long referrerRewardPackageId;
    private String status;
    private LocalDateTime boundAt;
    private LocalDateTime rewardedAt;
}
