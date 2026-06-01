package com.mandarly.boot.module.edu.controller.admin.pkg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 套餐 Response VO")
@Data
public class PackageRespVO {

    @Schema(description = "套餐 id", example = "1")
    private Long id;

    @Schema(description = "名称多语言 key", example = "package.standard")
    private String nameI18nCode;

    @Schema(description = "每周课次,免费体验课 NULL")
    private Integer weeklyCount;

    @Schema(description = "总课次")
    private Integer totalCount;

    @Schema(description = "有效期(天)")
    private Integer validityDays;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "是否免费体验")
    private Boolean isFreeTrial;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "描述多语言 key")
    private String descriptionI18nCode;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
