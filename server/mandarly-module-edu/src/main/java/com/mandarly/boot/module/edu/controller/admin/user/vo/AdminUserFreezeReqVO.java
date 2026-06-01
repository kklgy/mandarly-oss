package com.mandarly.boot.module.edu.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - C 端用户冻结 / 解冻 Request VO")
@Data
public class AdminUserFreezeReqVO {

    @Schema(description = "C 端 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2001")
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    @Schema(description = "动作:freeze / unfreeze", requiredMode = Schema.RequiredMode.REQUIRED, example = "freeze")
    @NotBlank(message = "动作不能为空")
    private String action;

    @Schema(description = "操作原因", example = "风控冻结")
    private String reason;

}
