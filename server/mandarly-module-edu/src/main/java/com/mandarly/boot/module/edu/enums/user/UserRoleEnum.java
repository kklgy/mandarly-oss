package com.mandarly.boot.module.edu.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * C 端用户角色枚举(对应 user.role)
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    STUDENT("student", "学生"),
    TEACHER("teacher", "教师");

    private final String code;
    private final String name;

    public static UserRoleEnum of(String code) {
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
