package com.mandarly.boot.module.edu.dal.dataobject.teacher;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师档案表(与 user 1:1)
 *
 * <p>对应 docs/database/01-users-auth.md §4.3
 */
@TableName(value = "teacher_profile", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherProfileDO extends TenantBaseDO {

    /**
     * 主键 = user.id(1:1)
     */
    @TableId
    private Long userId;

    /**
     * 文字自我介绍
     */
    private String intro;

    /**
     * 审核状态
     *
     * 枚举:{@link com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum}
     */
    private String auditStatus;

    /**
     * 驳回原因(audit_status=rejected 时必填)
     */
    private String rejectReason;

    /**
     * 最近一次审核时间
     */
    private LocalDateTime auditedAt;

    /**
     * 审核管理员 system_users.id
     */
    private Long auditedBy;

    /**
     * 教师等级,一期 NULL,二期预留 Level1/2/3 差异化课时费
     */
    private String level;

    /**
     * 教学方向数组,如 ["business","kids","HSK"]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> expertise;

    /**
     * 口音:mainland / taiwan / hk / mixed
     */
    private String accent;

    /**
     * 教师会的语言列表,如 ["zh","en","ar"]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> languages;

    /**
     * 教学年限
     */
    private Integer yearsExperience;

    /**
     * 教师自我介绍视频(COS 路径,选填)
     */
    private String introVideoUrl;

    /**
     * 视频文件大小 byte
     */
    private Long introVideoSize;

    /**
     * 视频上传时间
     */
    private LocalDateTime introVideoUploadedAt;

}
