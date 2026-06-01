package com.mandarly.boot.module.edu.controller.admin.teacher.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 教师档案分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherProfilePageReqVO extends PageParam {

    @Schema(description = "教师 user.id", example = "1024")
    private Long userId;

    @Schema(description = "审核状态:draft/pending/approved/rejected", example = "pending")
    private String auditStatus;

    @Schema(description = "自我介绍关键字模糊匹配", example = "母语")
    private String introKeyword;

    @Schema(description = "口音:mainland/taiwan/hk/mixed", example = "mainland")
    private String accent;
}
