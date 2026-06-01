package com.mandarly.boot.module.edu.controller.admin.support.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 客服 FAQ 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SupportFaqPageReqVO extends PageParam {

    @Schema(description = "分类")
    private String category;

    @Schema(description = "语言")
    private String locale;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "问题关键字")
    private String question;
}
