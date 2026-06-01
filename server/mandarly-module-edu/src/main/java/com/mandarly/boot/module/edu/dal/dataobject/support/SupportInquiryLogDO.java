package com.mandarly.boot.module.edu.dal.dataobject.support;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 客服咨询日志。
 */
@TableName("support_inquiry_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class SupportInquiryLogDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String sessionId;

    private String locale;

    private String market;

    private String questionText;

    private Long matchedFaqId;

    private BigDecimal score;

    private Boolean clickedToHuman;

    private Long clickedContactId;

    private String ip;

    private String userAgent;
}
