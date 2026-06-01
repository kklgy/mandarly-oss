package com.mandarly.boot.module.edu.controller.admin.support.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 客服联系方式分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SupportContactPageReqVO extends PageParam {

    @Schema(description = "市场")
    private String market;

    @Schema(description = "渠道类型")
    private String channelType;

    @Schema(description = "展示文案")
    private String displayText;

    @Schema(description = "是否启用")
    private Boolean isActive;
}
