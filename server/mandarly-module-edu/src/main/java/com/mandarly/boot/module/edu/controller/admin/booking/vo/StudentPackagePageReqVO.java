package com.mandarly.boot.module.edu.controller.admin.booking.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 学生持有套餐分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentPackagePageReqVO extends PageParam {

    @Schema(description = "学生 user.id", example = "1024")
    private Long studentId;

    @Schema(description = "套餐定义 id", example = "1")
    private Long packageId;

    @Schema(description = "来源 purchase / free_trial / admin_grant", example = "admin_grant")
    private String source;
}
