package com.mandarly.boot.module.edu.controller.admin.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 发放套餐给学生 Request VO(admin_grant 来源)")
@Data
public class StudentPackageGrantReqVO {

    @Schema(description = "学生 user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "studentId 不能为空")
    private Long studentId;

    @Schema(description = "套餐定义 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "packageId 不能为空")
    private Long packageId;

    @Schema(description = "覆盖剩余课次(选填,默认取 package.totalCount)", example = "10")
    @Min(value = 1)
    private Integer remainingOverride;

    @Schema(description = "覆盖有效期(选填,默认 now + package.validityDays);单位天", example = "180")
    @Min(value = 1)
    private Integer validityDaysOverride;

    @Schema(description = "备注", example = "客诉补偿")
    @Size(max = 256)
    private String grantedReason;
}
