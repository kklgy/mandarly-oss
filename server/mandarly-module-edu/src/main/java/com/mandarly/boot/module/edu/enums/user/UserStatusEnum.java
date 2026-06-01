package com.mandarly.boot.module.edu.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    PENDING_VERIFICATION("pending_verification", "未验证"),
    ACTIVE("active", "活跃"),
    FROZEN("frozen", "冻结");

    private final String code;
    private final String name;
}
