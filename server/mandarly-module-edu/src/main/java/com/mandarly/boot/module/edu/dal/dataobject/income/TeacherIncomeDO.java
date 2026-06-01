package com.mandarly.boot.module.edu.dal.dataobject.income;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师收入流水(M6 v1.2)
 *
 * <p>对应 patch 20260512_170500 schema:
 * <ul>
 *   <li>status — frozen / available / reverted</li>
 *   <li>currency — USD(一期固定)</li>
 *   <li>frozenUntil — 解冻时刻 = settledAt + 7d(refund_deduct / manual_adjust 取 settledAt 立即生效)</li>
 *   <li>relatedIncomeId — refund_deduct 时关联原 normal 行 id</li>
 *   <li>description — 备注(manual_adjust 必填)</li>
 * </ul>
 */
@TableName("teacher_income")
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherIncomeDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;
    private Long courseOrderId;
    private Long refundId;
    private BigDecimal amountUsd;
    private String type;
    private LocalDateTime settledAt;

    /** 状态:frozen / available / reverted */
    private String status;

    /** 币种(一期固定 USD) */
    private String currency;

    /** 解冻时刻 = settledAt + 7d(refund_deduct / manual_adjust 取 settledAt 立即生效) */
    private LocalDateTime frozenUntil;

    /** refund_deduct 时关联原 normal 行 id */
    private Long relatedIncomeId;

    /** 备注 */
    private String description;
}
