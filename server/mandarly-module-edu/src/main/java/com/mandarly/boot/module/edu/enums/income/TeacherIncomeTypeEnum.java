package com.mandarly.boot.module.edu.enums.income;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TeacherIncomeTypeEnum {
    NORMAL("normal"),
    FREE_TRIAL("free_trial"),
    REFUND_DEDUCT("refund_deduct");

    private final String code;
}
