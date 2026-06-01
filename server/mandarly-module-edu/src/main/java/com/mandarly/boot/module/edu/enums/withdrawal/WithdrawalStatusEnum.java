package com.mandarly.boot.module.edu.enums.withdrawal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提现申请状态机
 *
 * <p>对应 docs/database/04-teacher-income-withdrawal.md §3.2 / spec §3.2
 *
 * <p>合法跃迁:
 * <ul>
 *   <li>pending → approved (audit approved)</li>
 *   <li>pending → rejected (audit rejected,余额回退)</li>
 *   <li>approved → paid (markPaid,余额清算)</li>
 *   <li>approved → failed (markFailed,余额回退)</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum WithdrawalStatusEnum {

    PENDING("pending"),
    APPROVED("approved"),
    PAID("paid"),
    REJECTED("rejected"),
    FAILED("failed");

    private final String value;

    public static WithdrawalStatusEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (WithdrawalStatusEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
