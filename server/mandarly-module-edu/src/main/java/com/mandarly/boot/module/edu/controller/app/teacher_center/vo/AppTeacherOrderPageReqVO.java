package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端 - 教师中心:订单分页 Request VO
 *
 * <p>对应 spec §5.2 OrdersView 三 tab(待上课 / 已上课 / 已取消)。
 */
@Schema(description = "用户端 - 教师中心 - 订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppTeacherOrderPageReqVO extends PageParam {

    @Schema(description = "状态过滤,逗号分隔(如 upcoming,finished);留空表示全部", example = "upcoming")
    private String status;
}
