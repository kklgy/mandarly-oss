package com.mandarly.boot.module.edu.dal.dataobject.i18n;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 多语言文案
 *
 * <p>对应 DDL `i18n_message`,后端 MessageSource / 套餐名 / 邮件模板 / 通知文案统一查这里
 */
@TableName("i18n_message")
@Data
@EqualsAndHashCode(callSuper = true)
public class I18nMessageDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String locale;

    private String text;

    private String category;

    private String description;

}
