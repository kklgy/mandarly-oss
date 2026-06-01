package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端 - 教师中心:申请提现 Request VO
 *
 * <p>对应 spec §5.4 WithdrawalApplyView 提交表单。
 *
 * <p>校验:
 * <ul>
 *   <li>amount ≥ 0.01(后端再校验 ≥ {@code mandarly.withdrawal.min-amount})</li>
 *   <li>payeeInfo 明文,后端透明加密落库(AES-256-GCM)</li>
 *   <li>payeeMethod ∈ wechat/alipay/paypal/bank_card/other</li>
 * </ul>
 */
@Schema(description = "用户端 - 教师中心 - 申请提现 Request VO")
@Data
public class AppWithdrawalApplyReqVO {

    @Schema(description = "提现金额(USD,> 0)", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "amount 不能为空")
    @DecimalMin(value = "0.01", message = "amount 必须大于 0")
    private BigDecimal amount;

    @Schema(description = "收款信息明文(JSON 或单串),后端透明加密", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotBlank(message = "payeeInfo 不能为空")
    @Size(max = 1024, message = "payeeInfo 长度不能超过 1024")
    private String payeeInfo;

    @Schema(description = "收款方式 wechat/alipay/paypal/bank_card/other", requiredMode = Schema.RequiredMode.REQUIRED, example = "paypal")
    @NotBlank(message = "payeeMethod 不能为空")
    @Pattern(regexp = "wechat|alipay|paypal|bank_card|other", message = "payeeMethod 仅支持 wechat/alipay/paypal/bank_card/other")
    private String payeeMethod;
}
