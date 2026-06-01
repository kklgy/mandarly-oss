package com.mandarly.boot.module.edu.controller.admin.pkg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 套餐 创建/更新 Request VO")
@Data
public class PackageSaveReqVO {

    @Schema(description = "套餐 id;创建时为空,更新时必填", example = "1")
    private Long id;

    @Schema(description = "名称多语言 key,关联 i18n_message.code", requiredMode = Schema.RequiredMode.REQUIRED, example = "package.standard")
    @NotBlank(message = "nameI18nCode 不能为空")
    @Size(max = 64)
    private String nameI18nCode;

    @Schema(description = "每周课次,免费体验课传 null", example = "2")
    @Min(value = 1, message = "weeklyCount 至少 1")
    private Integer weeklyCount;

    @Schema(description = "总课次", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    @NotNull(message = "totalCount 不能为空")
    @Min(value = 1, message = "totalCount 至少 1")
    private Integer totalCount;

    @Schema(description = "有效期(天)", requiredMode = Schema.RequiredMode.REQUIRED, example = "90")
    @NotNull(message = "validityDays 不能为空")
    @Min(value = 1, message = "validityDays 至少 1")
    private Integer validityDays;

    @Schema(description = "价格,免费体验课传 0", requiredMode = Schema.RequiredMode.REQUIRED, example = "1280.00")
    @NotNull(message = "price 不能为空")
    @DecimalMin(value = "0.00", message = "price 不能为负")
    private BigDecimal price;

    @Schema(description = "币种,默认 HKD", example = "HKD")
    @Size(max = 8)
    private String currency;

    @Schema(description = "是否免费体验,默认 false", example = "false")
    private Boolean isFreeTrial;

    @Schema(description = "是否启用,默认 true", example = "true")
    private Boolean isActive;

    @Schema(description = "排序,正序小在前;默认 0", example = "10")
    private Integer sort;

    @Schema(description = "描述多语言 key", example = "package.standard.desc")
    @Size(max = 64)
    private String descriptionI18nCode;
}
