package com.mandarly.boot.module.edu.enums.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefundStatusEnum {
    PENDING("pending"),
    APPROVED("approved"),
    REFUNDED("refunded"),
    REJECTED("rejected"),
    FAILED("failed");

    private final String code;
}
