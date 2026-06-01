package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户端 - 教师中心:提现申请 Response VO
 *
 * <p>对应 spec §5.5 WithdrawalView 列表 / 详情。
 *
 * <p><strong>安全</strong>:payeeInfoMasked 字段仅展示后 4 位(spec §6.5 / §5.4 教师视角),
 * 教师端不返回明文。Controller 调用 {@link com.mandarly.boot.module.edu.controller.app.teacher_center.util.PayeeInfoMasker}
 * 完成脱敏。
 */
@Schema(description = "用户端 - 教师中心 - 提现申请 Response VO(payee 脱敏)")
@Data
public class AppWithdrawalRespVO {

    @Schema(description = "提现申请 id", example = "30001")
    private Long id;

    @Schema(description = "提现金额(USD)", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "币种", example = "USD")
    private String currency;

    @Schema(description = "收款方式 wechat/alipay/paypal/bank_card/other", example = "paypal")
    private String payeeMethod;

    @Schema(description = "收款信息脱敏后展示(后 4 位),如 \"paypal·****.com\"", example = "paypal·****.com")
    private String payeeInfoMasked;

    @Schema(description = "状态 pending/approved/paid/rejected/failed", example = "pending")
    private String status;

    @Schema(description = "申请时间(UTC)", example = "2026-05-15T10:00:00")
    private LocalDateTime appliedAt;

    @Schema(description = "审核时间(UTC)", example = "2026-05-15T12:00:00")
    private LocalDateTime auditedAt;

    @Schema(description = "驳回原因(rejected 时填)")
    private String rejectReason;

    @Schema(description = "打款时间(UTC)", example = "2026-05-16T08:00:00")
    private LocalDateTime paidAt;

    @Schema(description = "打款备注(paid/failed 时填,失败时复用为 failReason)")
    private String paidRemark;
}
