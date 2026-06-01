package com.mandarly.boot.module.edu.controller.admin.referral.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 推荐记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReferralRecordPageReqVO extends PageParam {

    @Schema(description = "推荐人 user.id", example = "1024")
    private Long referrerUserId;

    @Schema(description = "被推荐人 user.id", example = "2048")
    private Long refereeUserId;

    @Schema(description = "状态:bound/rewarded", example = "rewarded")
    private String status;
}
