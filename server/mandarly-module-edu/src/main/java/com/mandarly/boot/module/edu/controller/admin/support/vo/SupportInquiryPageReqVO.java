package com.mandarly.boot.module.edu.controller.admin.support.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 客服咨询日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SupportInquiryPageReqVO extends PageParam {

    @Schema(description = "用户 id")
    private Long userId;

    @Schema(description = "会话 id")
    private String sessionId;

    @Schema(description = "语言")
    private String locale;

    @Schema(description = "市场")
    private String market;

    @Schema(description = "问题关键字")
    private String questionKeyword;

    @Schema(description = "是否命中 FAQ")
    private Boolean matched;

    @Schema(description = "是否点击真人客服")
    private Boolean clickedToHuman;
}
