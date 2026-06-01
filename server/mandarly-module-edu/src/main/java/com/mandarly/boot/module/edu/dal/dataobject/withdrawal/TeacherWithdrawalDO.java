package com.mandarly.boot.module.edu.dal.dataobject.withdrawal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mandarly.boot.framework.security.typehandler.AesEncryptTypeHandler;
import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师提现申请(M6 §2.1 c)
 *
 * <p>对应 patch 20260512_170500 schema:
 * <ul>
 *   <li>payeeInfo — AES-256-GCM 加密存储,经 {@link AesEncryptTypeHandler} 透明加解密</li>
 *   <li>status — pending / approved / paid / rejected / failed</li>
 *   <li>payeeMethod — wechat / alipay / paypal / bank_card / other</li>
 *   <li>currency — USD(一期固定)</li>
 * </ul>
 *
 * <p>状态机详见 docs/database/04-teacher-income-withdrawal.md §3.2。
 */
@TableName(value = "teacher_withdrawal", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherWithdrawalDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private BigDecimal amount;

    /** 币种(一期固定 USD) */
    private String currency;

    /**
     * 收款信息(JSON 字符串或单串),AES-256-GCM 透明加解密,落库密文格式 {@code v1:{iv}:{ct}}。
     * <p>必经 {@link AesEncryptTypeHandler} 才能正确读写。
     */
    @TableField(typeHandler = AesEncryptTypeHandler.class)
    private String payeeInfo;

    /** 收款方式:wechat / alipay / paypal / bank_card / other */
    private String payeeMethod;

    /** 状态:pending / approved / paid / rejected / failed */
    private String status;

    private LocalDateTime appliedAt;

    /** 审核人(管理员 user.id) */
    private Long auditedBy;
    private LocalDateTime auditedAt;
    private String rejectReason;

    /** 打款人(管理员 user.id) */
    private Long paidBy;
    private LocalDateTime paidAt;
    /** 打款凭证截图 URL(COS) */
    private String paidProof;
    private String paidRemark;
}
