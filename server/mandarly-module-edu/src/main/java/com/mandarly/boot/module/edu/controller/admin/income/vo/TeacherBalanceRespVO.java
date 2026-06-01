package com.mandarly.boot.module.edu.controller.admin.income.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 教师余额详情 Response VO")
@Data
public class TeacherBalanceRespVO {

    @Schema(description = "教师 user.id")
    private Long teacherId;

    @Schema(description = "T+7 未解冻金额(USD,结算 7 天内)")
    private BigDecimal frozenT7Usd;

    @Schema(description = "提现 in-flight 冻结金额(USD,pending/approved 的提现)")
    private BigDecimal pendingWithdrawUsd;

    @Schema(description = "可提现金额(USD,结算超 7 天)")
    private BigDecimal availableUsd;

    @Schema(description = "累计已赚金额(USD)")
    private BigDecimal totalEarnedUsd;

    @Schema(description = "累计已提现金额(USD)")
    private BigDecimal totalWithdrawnUsd;

    @Schema(description = "最后余额重建时间(UTC)")
    private LocalDateTime lastRebuildAt;
}
