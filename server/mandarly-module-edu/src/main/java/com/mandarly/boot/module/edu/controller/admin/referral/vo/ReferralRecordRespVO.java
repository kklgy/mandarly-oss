package com.mandarly.boot.module.edu.controller.admin.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 推荐记录 Response VO(含 JOIN 邮箱字段)")
@Data
public class ReferralRecordRespVO {

    @Schema(description = "推荐记录 ID")
    private Long id;

    @Schema(description = "推荐人 user.id")
    private Long referrerUserId;

    @Schema(description = "推荐人邮箱(JOIN user)")
    private String referrerEmail;

    @Schema(description = "被推荐人 user.id")
    private Long refereeUserId;

    @Schema(description = "被推荐人邮箱(JOIN user)")
    private String refereeEmail;

    @Schema(description = "使用的推荐码")
    private String referralCode;

    @Schema(description = "关联支付单 ID(首付成功后关联)")
    private Long paymentId;

    @Schema(description = "被推荐人折扣金额(USD)")
    private BigDecimal refereeDiscountAmountUsd;

    @Schema(description = "推荐人奖励套餐 ID")
    private Long referrerRewardPackageId;

    @Schema(description = "状态:bound/rewarded")
    private String status;

    @Schema(description = "绑定时间(UTC)")
    private LocalDateTime boundAt;

    @Schema(description = "奖励发放时间(UTC)")
    private LocalDateTime rewardedAt;

    @Schema(description = "创建时间(UTC)")
    private LocalDateTime createTime;
}
