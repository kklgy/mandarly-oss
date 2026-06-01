package com.mandarly.boot.module.edu.dal.dataobject.user;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Mandarly C 端用户主表(学生 + 教师)
 *
 * <p>与若依 {@code system_users} 分离:system_users 是后台 admin,本表是 C 端业务用户。
 * 见 docs/database/01-users-auth.md §1.1。
 *
 * <p>租户拦截器一期关闭(yaml mandarly.tenant.enable=false),tenant_id 字段保留只是
 * 兼容 TenantBaseDO 继承,业务侧无意义。
 */
@TableName("user")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色:student / teacher 互斥
     *
     * 枚举:{@link com.mandarly.boot.module.edu.enums.user.UserRoleEnum}
     */
    private String role;

    /**
     * 邮箱(可空,与 phone 至少一个非空)
     */
    private String email;

    /**
     * 邮箱验证时间;NULL 即未验证
     */
    private LocalDateTime emailVerifiedAt;

    /**
     * E.164 格式手机号(可空,与 email 至少一个非空)
     */
    private String phone;

    /**
     * 手机号验证时间
     */
    private LocalDateTime phoneVerifiedAt;

    /**
     * BCrypt 密码哈希(第三方登录可空)
     */
    private String passwordHash;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL(COS)
     */
    private String avatarUrl;

    /**
     * 语言偏好:en / zh-CN / zh-TW / ar
     */
    private String locale;

    /**
     * IANA 时区名,如 Asia/Hong_Kong
     */
    private String timezone;

    /**
     * 状态
     *
     * 枚举:{@link com.mandarly.boot.module.edu.enums.user.UserStatusEnum}
     */
    private String status;

    /**
     * 自己的推荐码(注册时生成,8 字符 MAND + 4 位)
     */
    private String referralCode;

    /**
     * 被谁推荐 user.id
     */
    private Long referredBy;

    /**
     * 学生学习目标(自填,选填)
     */
    private String learningGoal;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最近登录 IP
     */
    private String lastLoginIp;

}
