package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户端 - 教师中心:收入流水分页 Request VO
 *
 * <p>对应 spec §5.3 IncomeView 底部明细 + 筛选,服务 {@code GET /income/list}。
 */
@Schema(description = "用户端 - 教师中心 - 收入流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppTeacherIncomePageReqVO extends PageParam {

    @Schema(description = "结算时间起点(UTC)", example = "2026-05-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @Schema(description = "结算时间终点(UTC)", example = "2026-06-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    @Schema(description = "收入类型 normal/free_trial/no_show_teacher/no_show_student/refund_deduct/manual_adjust", example = "normal")
    private String type;

    @Schema(description = "状态 frozen/available/reverted", example = "frozen")
    private String status;
}
