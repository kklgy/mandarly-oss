package com.mandarly.boot.module.edu.controller.admin.withdrawal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理后台 - 提现审核(approve/reject) Request VO
 *
 * <p>对应 spec §6.4 admin withdrawal audit 动作。
 *
 * <p>校验:
 * <ul>
 *   <li>approved 不能为空</li>
 *   <li>rejectReason 非空校验在 Service({@code WithdrawalServiceImpl.audit}),
 *       原因:当 approved=true 时,rejectReason 无意义,不能强制 @NotBlank</li>
 * </ul>
 */
@Schema(description = "管理后台 - 提现审核 Request VO")
@Data
public class WithdrawalAuditReqVO {

    @Schema(description = "是否通过:true=approved,false=rejected", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "approved 不能为空")
    private Boolean approved;

    @Schema(description = "驳回原因(approved=false 时必填,Service 内校验)", example = "材料不全")
    @Size(max = 500, message = "rejectReason 长度不能超过 500")
    private String rejectReason;
}
