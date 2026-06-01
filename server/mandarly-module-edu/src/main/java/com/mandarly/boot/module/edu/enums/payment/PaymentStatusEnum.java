package com.mandarly.boot.module.edu.enums.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatusEnum {
    PENDING("pending"),
    PAID("paid"),
    FAILED("failed"),
    EXPIRED("expired"),
    REFUNDED("refunded"),
    PARTIAL_REFUNDED("partial_refunded");

    private final String code;
}
