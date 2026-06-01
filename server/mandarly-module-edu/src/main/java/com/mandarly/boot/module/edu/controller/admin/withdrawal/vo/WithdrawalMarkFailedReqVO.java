package com.mandarly.boot.module.edu.controller.admin.withdrawal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理后台 - 标记打款失败(approved → failed) Request VO
 *
 * <p>对应 spec §6.4 admin withdrawal markFailed 动作;失败原因强制必填(后续余额回退依赖该原因审计)。
 */
@Schema(description = "管理后台 - 标记打款失败 Request VO")
@Data
public class WithdrawalMarkFailedReqVO {

    @Schema(description = "失败原因(必填)", requiredMode = Schema.RequiredMode.REQUIRED, example = "对方账号错误")
    @NotBlank(message = "failReason 不能为空")
    @Size(max = 500, message = "failReason 长度不能超过 500")
    private String failReason;
}
