package com.mandarly.boot.module.edu.controller.admin.pkg.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 套餐分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PackagePageReqVO extends PageParam {

    @Schema(description = "名称多语言 key 模糊匹配", example = "package.standard")
    private String nameI18nCode;

    @Schema(description = "币种", example = "HKD")
    private String currency;

    @Schema(description = "是否免费体验", example = "false")
    private Boolean isFreeTrial;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive;
}
