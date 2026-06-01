package com.mandarly.boot.module.edu.enums.levelcheck;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 水平测试推断的学习者等级
 *
 * <p>对应 docs/product/level-check-recommendation-v1.md §4.1
 */
@Getter
@AllArgsConstructor
public enum InferredLevelEnum {

    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    private final String code;

}
