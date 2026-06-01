package com.mandarly.boot.module.edu.dal.dataobject.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * C 端用户第三方登录绑定。
 */
@TableName("user_oauth")
@Data
@EqualsAndHashCode(callSuper = true)
public class EduUserOauthDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String provider;

    private String oauthUid;

    private String oauthEmail;

    private LocalDateTime boundAt;

    private LocalDateTime unboundAt;

}
