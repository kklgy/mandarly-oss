package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端 - 教师中心:余额 Response VO
 *
 * <p>对应 spec §5.3 IncomeView 顶部 4 卡片(累计入账 / 可提现 / 在途 T+7 / 申请中冻结)。
 *
 * <p>5 字段(spec §3.7 / §5.3 / A6 plan):
 * <ul>
 *   <li>availableUsd — 可提现金额(结算超 7 天)</li>
 *   <li>frozenT7Usd — T+7 未解冻(结算 7 天内)</li>
 *   <li>pendingWithdrawUsd — 提现 in-flight 冻结(pending/approved)</li>
 *   <li>totalEarnedUsd — 累计已赚</li>
 *   <li>totalWithdrawnUsd — 累计已提现</li>
 * </ul>
 *
 * <p>不变量(对齐 db-design v1.2 §4.3):
 * availableUsd + frozenT7Usd + pendingWithdrawUsd = totalEarnedUsd - totalWithdrawnUsd
 */
@Schema(description = "用户端 - 教师中心 - 余额 Response VO(5 字段)")
@Data
public class AppTeacherBalanceRespVO {

    @Schema(description = "可提现金额(USD)", example = "580.00")
    private BigDecimal availableUsd;

    @Schema(description = "T+7 未解冻金额(USD)", example = "120.00")
    private BigDecimal frozenT7Usd;

    @Schema(description = "提现 in-flight 冻结金额(USD,pending/approved)", example = "100.00")
    private BigDecimal pendingWithdrawUsd;

    @Schema(description = "累计已赚金额(USD)", example = "1200.00")
    private BigDecimal totalEarnedUsd;

    @Schema(description = "累计已提现金额(USD)", example = "400.00")
    private BigDecimal totalWithdrawnUsd;

    @Schema(description = "币种(一期固定 USD)", example = "USD")
    private String currency;
}
