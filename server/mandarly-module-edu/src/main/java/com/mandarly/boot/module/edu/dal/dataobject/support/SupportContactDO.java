package com.mandarly.boot.module.edu.dal.dataobject.support;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 私域客服联系方式。
 */
@TableName("support_contact")
@Data
@EqualsAndHashCode(callSuper = true)
public class SupportContactDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String market;

    private String channelType;

    private String displayText;

    private String linkUrl;

    private String imageUrl;

    private Integer sort;

    private Boolean isActive;
}
