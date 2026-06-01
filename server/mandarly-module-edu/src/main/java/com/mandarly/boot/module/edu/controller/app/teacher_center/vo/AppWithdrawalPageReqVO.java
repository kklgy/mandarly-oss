package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端 - 教师中心:提现历史分页 Request VO
 *
 * <p>对应 spec §5.5 WithdrawalView 列表。
 */
@Schema(description = "用户端 - 教师中心 - 提现历史分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppWithdrawalPageReqVO extends PageParam {

    @Schema(description = "状态 pending/approved/paid/rejected/failed;留空表示全部", example = "pending")
    private String status;
}
