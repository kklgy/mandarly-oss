package com.mandarly.boot.module.edu.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户端 - 学生持有套餐(联表 package 翻译后)Response VO")
@Data
public class AppStudentPackageRespVO {

    @Schema(description = "student_package.id")
    private Long id;

    @Schema(description = "套餐定义 id")
    private Long packageId;

    @Schema(description = "套餐显示名(经 i18n 翻译,可空 → 前端按 packageDefaultName fallback)")
    private String name;

    @Schema(description = "剩余课次")
    private Integer remaining;

    @Schema(description = "总课次")
    private Integer totalCount;

    @Schema(description = "每周课次(免费体验课为空)")
    private Integer weeklyCount;

    @Schema(description = "有效期截止(UTC ISO)")
    private LocalDateTime expireAt;

    @Schema(description = "是否免费体验课")
    private Boolean isFreeTrial;

    @Schema(description = "套餐展示价(冗余)")
    private BigDecimal price;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "套餐来源:purchase / free_trial / admin_grant / register_grant / referral_reward",
            allowableValues = {"purchase", "free_trial", "admin_grant", "register_grant", "referral_reward"})
    private String source;

    @Schema(description = "前端展示状态:active(可用) / expired(过期) / exhausted(用完)",
            allowableValues = {"active", "expired", "exhausted"})
    private String status;

    @Schema(description = "购买/获取时间(一期用 create_time;M4 接 payment 后切 paid_at)")
    private LocalDateTime purchasedAt;

    @Schema(description = "关联的支付订单 id(source=purchase 才有值;前端退款按钮按此判断显示)")
    private Long paymentId;
}
