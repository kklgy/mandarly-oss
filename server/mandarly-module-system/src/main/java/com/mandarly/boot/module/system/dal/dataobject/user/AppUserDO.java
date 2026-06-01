package com.mandarly.boot.module.system.dal.dataobject.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mandarly.boot.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import java.time.LocalDateTime;

@TableName(value = "user", autoResultMap = true)
@KeySequence("user_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserDO extends BaseDO {

    // 必须显式 AUTO:省略时 prod 容器内全局智能模式生效不稳,user.insert 后 id 不回填,
    // 导致 createOrLoginBySocial → bind(user.getId(), ...) 拿到 null → user_oauth.user_id 缺失(2026-05-11 实证)
    @TableId(type = IdType.AUTO)
    private Long id;
    private String role;                  // student / teacher
    private String email;
    private LocalDateTime emailVerifiedAt;
    private String phone;
    private LocalDateTime phoneVerifiedAt;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private String locale;
    private String timezone;
    private String status;                // pending_verification / active / frozen
    private String referralCode;
    private Long referredBy;
    private String learningGoal;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
}
