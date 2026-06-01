package com.mandarly.boot.module.edu.enums.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 教师资质材料类型(对应 teacher_qualification.doc_type)
 *
 * <p>DDL: doc_type VARCHAR(32) NOT NULL COMMENT 'id_card/passport/degree_cert/teaching_cert/english_cert/experience_proof'
 */
@Getter
@AllArgsConstructor
public enum TeacherQualificationDocTypeEnum {

    ID_CARD("id_card", "身份证"),
    PASSPORT("passport", "护照"),
    DEGREE_CERT("degree_cert", "学历证书"),
    TEACHING_CERT("teaching_cert", "教学证书"),
    ENGLISH_CERT("english_cert", "英语四/六级证书"),
    EXPERIENCE_PROOF("experience_proof", "教学经验证明");

    private final String code;
    private final String name;
}
