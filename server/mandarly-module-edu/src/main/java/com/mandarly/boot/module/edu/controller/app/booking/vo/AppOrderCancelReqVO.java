package com.mandarly.boot.module.edu.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户端 - 取消订单 Request VO(学生发起)
 *
 * <p>对应 PRD-v4 §4.3 S5 取消规则。一期 @PermitAll,cancelledBy 后端固定填 student。
 */
@Schema(description = "用户端 - 取消订单 Request VO")
@Data
public class AppOrderCancelReqVO {

    @Schema(description = "取消原因(选填)", example = "临时有事")
    @Size(max = 256, message = "取消原因不能超过 256 字")
    private String reason;
}
