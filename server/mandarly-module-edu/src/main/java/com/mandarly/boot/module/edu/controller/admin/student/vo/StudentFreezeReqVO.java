package com.mandarly.boot.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 学生冻结/解冻 Request VO")
@Data
public class StudentFreezeReqVO {

    @Schema(description = "学生 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2001")
    @NotNull(message = "userId 不能为空")
    private Long userId;

    @Schema(description = "动作:freeze / unfreeze", requiredMode = Schema.RequiredMode.REQUIRED, example = "freeze")
    @NotNull(message = "action 不能为空")
    private String action;

    @Schema(description = "原因(freeze 时建议填,审计用)", example = "违反社区规则,屡次发广告")
    private String reason;
}
