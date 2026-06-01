package com.mandarly.boot.module.edu.controller.admin.withdrawal.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理后台 - 提现申请分页 Request VO
 *
 * <p>注:此 VO 服务 Service / Mapper 公用,在 A4 阶段创建;
 * Controller(A7 实现)会复用同一份。
 */
@Schema(description = "管理后台 - 提现申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class WithdrawalPageReqVO extends PageParam {

    @Schema(description = "状态:pending/approved/paid/rejected/failed", example = "pending")
    private String status;

    @Schema(description = "教师 user.id", example = "2048")
    private Long teacherId;

    @Schema(description = "教师关键字(昵称 / 手机号 / 邮箱模糊匹配)", example = "Alice")
    private String teacherKeyword;

    @Schema(description = "提现金额下限", example = "100")
    private BigDecimal amountMin;

    @Schema(description = "提现金额上限", example = "10000")
    private BigDecimal amountMax;

    @Schema(description = "申请时间起点(UTC)", example = "2026-05-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime appliedAtFrom;

    @Schema(description = "申请时间终点(UTC)", example = "2026-06-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime appliedAtTo;
}
