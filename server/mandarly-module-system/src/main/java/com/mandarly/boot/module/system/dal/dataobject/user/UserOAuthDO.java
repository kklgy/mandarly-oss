package com.mandarly.boot.module.system.dal.dataobject.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mandarly.boot.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@TableName(value = "user_oauth", autoResultMap = true)
@KeySequence("user_oauth_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOAuthDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String provider;          // google / apple
    private String oauthUid;
    private String oauthEmail;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> oauthRaw;

    private LocalDateTime boundAt;
    private LocalDateTime unboundAt;
}
