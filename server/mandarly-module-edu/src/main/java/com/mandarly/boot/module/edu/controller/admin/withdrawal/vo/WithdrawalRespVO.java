package com.mandarly.boot.module.edu.controller.admin.withdrawal.vo;

import com.mandarly.boot.module.edu.controller.app.teacher_center.util.PayeeInfoMasker;
import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理后台 - 提现申请 Response VO(payee 脱敏)
 *
 * <p>对应 spec §6.4 admin 列表 / 详情;payee_info 经
 * {@link PayeeInfoMasker#mask(String, String)} 脱敏(后 4 位)。
 * 明文仅通过 {@code GET /reveal-payee} + 权限 {@code edu:withdrawal:reveal-payee} 二次确认获取。
 *
 * <p>跨包引用说明:复用 app/teacher_center/util/PayeeInfoMasker。该工具类于 A6 落地于教师端;
 * A7 admin 列表也需要相同脱敏规则,二者共享同一函数避免漂移。若后续 admin /
 * 其他模块继续扩展使用,可上移至 framework 公共 util。
 */
@Schema(description = "管理后台 - 提现申请 Response VO(payee 脱敏)")
@Data
public class WithdrawalRespVO {

    @Schema(description = "提现申请 id", example = "30001")
    private Long id;

    @Schema(description = "教师 user.id", example = "2048")
    private Long teacherId;

    @Schema(description = "提现金额(USD)", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "币种", example = "USD")
    private String currency;

    @Schema(description = "收款方式 wechat/alipay/paypal/bank_card/other", example = "paypal")
    private String payeeMethod;

    @Schema(description = "收款信息脱敏后展示(后 4 位)", example = "paypal·****.com")
    private String payeeInfoMasked;

    @Schema(description = "状态 pending/approved/paid/rejected/failed", example = "pending")
    private String status;

    @Schema(description = "申请时间(UTC)", example = "2026-05-15T10:00:00")
    private LocalDateTime appliedAt;

    @Schema(description = "审核人 admin user.id", example = "999")
    private Long auditedBy;

    @Schema(description = "审核时间(UTC)", example = "2026-05-15T12:00:00")
    private LocalDateTime auditedAt;

    @Schema(description = "驳回原因(rejected 时填)")
    private String rejectReason;

    @Schema(description = "打款人 admin user.id", example = "999")
    private Long paidBy;

    @Schema(description = "打款时间(UTC)", example = "2026-05-16T08:00:00")
    private LocalDateTime paidAt;

    @Schema(description = "打款凭证截图 URL(COS)")
    private String paidProof;

    @Schema(description = "打款备注(paid/failed 时填,失败时复用为 failReason)")
    private String paidRemark;

    /**
     * DO → masked Resp VO(payee_info 经 PayeeInfoMasker 脱敏)
     */
    public static WithdrawalRespVO fromDO(TeacherWithdrawalDO row) {
        if (row == null) {
            return null;
        }
        WithdrawalRespVO vo = new WithdrawalRespVO();
        vo.setId(row.getId());
        vo.setTeacherId(row.getTeacherId());
        vo.setAmount(row.getAmount());
        vo.setCurrency(row.getCurrency());
        vo.setPayeeMethod(row.getPayeeMethod());
        vo.setPayeeInfoMasked(PayeeInfoMasker.mask(row.getPayeeInfo(), row.getPayeeMethod()));
        vo.setStatus(row.getStatus());
        vo.setAppliedAt(row.getAppliedAt());
        vo.setAuditedBy(row.getAuditedBy());
        vo.setAuditedAt(row.getAuditedAt());
        vo.setRejectReason(row.getRejectReason());
        vo.setPaidBy(row.getPaidBy());
        vo.setPaidAt(row.getPaidAt());
        vo.setPaidProof(row.getPaidProof());
        vo.setPaidRemark(row.getPaidRemark());
        return vo;
    }
}
