package com.mandarly.boot.module.edu.controller.admin.withdrawal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理后台 - 标记打款成功(approved → paid) Request VO
 *
 * <p>对应 spec §6.4 admin withdrawal markPaid 动作。
 * 凭证 / 备注均为可选(部分通道只需备注)。
 */
@Schema(description = "管理后台 - 标记打款成功 Request VO")
@Data
public class WithdrawalMarkPaidReqVO {

    @Schema(description = "打款凭证截图 URL(COS),可空", example = "https://cos.example.com/proof/8001.png")
    @Size(max = 512, message = "paidProof 长度不能超过 512")
    private String paidProof;

    @Schema(description = "打款备注(可空,例如对方银行流水号)", example = "对方流水 TX20260512001")
    @Size(max = 500, message = "paidRemark 长度不能超过 500")
    private String paidRemark;
}
