package com.mandarly.boot.module.edu.controller.app.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "App 端 - 推荐战绩 Response VO")
@Data
@Builder
public class AppReferralStatsRespVO {

    @Schema(description = "我的推荐码")
    private String referralCode;

    @Schema(description = "已推荐人数(bound 状态,含未完成首付)")
    private int refereeCount;

    @Schema(description = "已奖励次数(referrer 收到免费试课套餐的次数)")
    private int rewardedCount;

    @Schema(description = "累计获得奖励套餐数(= rewardedCount,保留以备前端差异展示)")
    private int totalRewardPackages;
}
