package com.mandarly.boot.module.edu.dal.dataobject.teacher;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 教师资质材料表(一个教师可上传多份独立审核)
 *
 * <p>对应 docs/database/01-users-auth.md §4.4 + DDL teacher_qualification
 */
@TableName("teacher_qualification")
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherQualificationDO extends TenantBaseDO {

    /**
     * 主键(自增)
     *
     * <p>必须显式 IdType.AUTO,避免 prod 容器全局智能模式 id 不回填(memory feedback_tableid_must_be_explicit)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联 user.id
     */
    private Long userId;

    /**
     * 文件类型
     *
     * 枚举:{@link com.mandarly.boot.module.edu.enums.teacher.TeacherQualificationDocTypeEnum}
     */
    private String docType;

    /**
     * 文件 URL(COS)
     */
    private String docUrl;

    /**
     * 原始文件名
     */
    private String docFilename;

    /**
     * 审核状态(DDL 默认 'pending')
     *
     * 枚举:{@link com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum}
     */
    private String auditStatus;

    /**
     * 驳回原因(audit_status=rejected 时必填)
     */
    private String rejectReason;

    /**
     * 审核时间
     */
    private LocalDateTime auditedAt;

    /**
     * 审核管理员 system_users.id
     */
    private Long auditedBy;

}
