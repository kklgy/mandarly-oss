package com.mandarly.boot.module.edu.controller.app.booking.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端 - "我的订单"分页 Request VO
 *
 * <p>对应 PRD-v4 §4.3 S5 我的订单。M2.5 已切真 token,
 * studentId 由后端从 SecurityFrameworkUtils.getLoginUserId() 取,无需前端传入。
 */
@Schema(description = "用户端 - 我的订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppMyOrderPageReqVO extends PageParam {

    @Schema(description = "状态过滤,逗号分隔(如 upcoming,cancelled);留空表示全部",
            example = "upcoming")
    private String status;
}
