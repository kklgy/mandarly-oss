package com.mandarly.boot.module.edu.controller.admin.teacher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 教师档案审核 Request VO")
@Data
public class TeacherProfileAuditReqVO {

    @Schema(description = "教师 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "userId 不能为空")
    private Long userId;

    @Schema(description = "审核动作:approve / reject", requiredMode = Schema.RequiredMode.REQUIRED, example = "approve")
    @NotNull(message = "action 不能为空")
    private String action;

    @Schema(description = "驳回原因(action=reject 时必填)", example = "资质材料模糊不清")
    private String rejectReason;
}
