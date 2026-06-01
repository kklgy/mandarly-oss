package com.mandarly.boot.module.edu.controller.admin.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 拒绝退款(reject) Request VO")
@Data
public class RefundRejectReqVO {

    @Schema(description = "拒绝理由(≥ 5 字符,便于学生申诉)", requiredMode = Schema.RequiredMode.REQUIRED, example = "申请原因不符合退款政策")
    @NotBlank(message = "拒绝理由不能为空")
    @Size(min = 5, message = "拒绝理由不少于 5 个字符")
    private String auditNote;
}
