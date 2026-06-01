package com.mandarly.boot.module.edu.enums.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学生持有套餐的来源
 *
 * <p>对应 docs/database/02-packages-orders.md §1.6 / PRD §S3
 */
@Getter
@AllArgsConstructor
public enum StudentPackageSourceEnum {

    PURCHASE("purchase"),
    FREE_TRIAL("free_trial"),
    ADMIN_GRANT("admin_grant"),
    REGISTER_GRANT("register_grant"),
    REFERRAL_REWARD("referral_reward");

    private final String code;

}
