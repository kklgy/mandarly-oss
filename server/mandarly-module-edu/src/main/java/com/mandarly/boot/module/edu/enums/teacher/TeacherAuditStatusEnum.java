package com.mandarly.boot.module.edu.enums.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 教师档案审核状态(对应 teacher_profile.audit_status / teacher_qualification.audit_status)
 */
@Getter
@AllArgsConstructor
public enum TeacherAuditStatusEnum {

    DRAFT("draft", "未提交"),
    PENDING("pending", "待审核"),
    APPROVED("approved", "审核通过"),
    REJECTED("rejected", "驳回");

    private final String code;
    private final String name;
}
