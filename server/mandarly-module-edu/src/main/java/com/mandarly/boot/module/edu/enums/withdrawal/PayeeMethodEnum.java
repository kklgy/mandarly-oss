package com.mandarly.boot.module.edu.enums.withdrawal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提现收款方式
 *
 * <p>对应 docs/database/04-teacher-income-withdrawal.md §4.2:
 * teacher_withdrawal.payee_method 字段取值。
 */
@Getter
@AllArgsConstructor
public enum PayeeMethodEnum {

    WECHAT("wechat"),
    ALIPAY("alipay"),
    PAYPAL("paypal"),
    BANK_CARD("bank_card"),
    OTHER("other");

    private final String value;

    public static PayeeMethodEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (PayeeMethodEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
