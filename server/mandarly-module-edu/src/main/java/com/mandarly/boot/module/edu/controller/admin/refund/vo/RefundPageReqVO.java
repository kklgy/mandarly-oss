package com.mandarly.boot.module.edu.controller.admin.refund.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 退款工单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundPageReqVO extends PageParam {

    @Schema(description = "学生 user.id", example = "1024")
    private Long userId;

    @Schema(description = "关联支付单 ID", example = "1")
    private Long paymentId;

    @Schema(description = "状态:pending/approved/rejected/refunded", example = "pending")
    private String status;
}
