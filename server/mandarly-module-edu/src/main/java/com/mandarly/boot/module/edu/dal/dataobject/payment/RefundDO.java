package com.mandarly.boot.module.edu.dal.dataobject.payment;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("refund")
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paymentId;
    private Long userId;
    private Long studentPackageId;
    private String applyReason;
    private BigDecimal suggestedAmountUsd;
    private BigDecimal finalAmountUsd;
    private String adjustReason;
    private String channelRefundId;
    private String status;
    private Long auditBy;
    private LocalDateTime auditAt;
    private String auditNote;
    private LocalDateTime refundedAt;
    private Boolean teacherIncomeDeducted;
    /**
     * GENERATED ALWAYS AS (CASE WHEN status IN ('pending','approved') THEN 1 ELSE NULL END) STORED
     * MySQL 拒绝 INSERT/UPDATE 此列;FieldStrategy.NEVER 让 MyBatis-Plus 永远不写它,SELECT 仍读
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Byte isActive;
}
