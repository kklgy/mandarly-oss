package com.mandarly.boot.module.edu.dal.dataobject.support;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * FAQ 库。
 */
@TableName(value = "faq", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class FaqDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String category;

    private String locale;

    private String question;

    private String answer;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> keywords;

    private Integer sort;

    private String status;

    private Long viewCount;

    private Long matchCount;
}
