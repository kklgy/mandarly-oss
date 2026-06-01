package com.mandarly.boot.module.edu.dal.dataobject.income;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师余额汇总(M6 v1.2)
 *
 * <p>不变量(对齐 db-design v1.2 §4.3):
 * available + frozenT7 + pendingWithdraw = totalEarned - totalWithdrawn
 *
 * <p>对应 patch 20260512_170500 schema:
 * <ul>
 *   <li>frozenUsd → frozenT7Usd(rename,T+7 未解冻)</li>
 *   <li>pendingWithdrawUsd — 提现 in-flight 冻结</li>
 *   <li>currency — USD(一期固定)</li>
 *   <li>version — 乐观锁</li>
 * </ul>
 */
@TableName("teacher_balance")
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherBalanceDO extends TenantBaseDO {

    @TableId
    private Long teacherId;

    /** T+7 未解冻余额(原 frozenUsd) */
    private BigDecimal frozenT7Usd;

    /** 可提现余额 */
    private BigDecimal availableUsd;

    /** 累计已赚 */
    private BigDecimal totalEarnedUsd;

    /** 累计已提现 */
    private BigDecimal totalWithdrawnUsd;

    /** 提现 in-flight 冻结(pending/approved 的提现申请金额) */
    private BigDecimal pendingWithdrawUsd;

    /** 币种(一期固定 USD) */
    private String currency;

    /** 乐观锁版本号 */
    private Integer version;

    private LocalDateTime lastRebuildAt;
}
