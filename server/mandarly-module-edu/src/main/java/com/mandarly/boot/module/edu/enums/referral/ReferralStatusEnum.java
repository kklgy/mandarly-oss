package com.mandarly.boot.module.edu.enums.referral;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReferralStatusEnum {
    BOUND("bound"),
    REWARDED("rewarded"),
    VOIDED("voided");

    private final String code;
}
