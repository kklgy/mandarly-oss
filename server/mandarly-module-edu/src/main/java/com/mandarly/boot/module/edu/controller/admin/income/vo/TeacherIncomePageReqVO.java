package com.mandarly.boot.module.edu.controller.admin.income.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 教师收入流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherIncomePageReqVO extends PageParam {

    @Schema(description = "教师 user.id", example = "2048")
    private Long teacherId;

    @Schema(description = "收入类型:normal/free_trial/no_show_student/refund_deduct", example = "normal")
    private String type;

    @Schema(description = "结算时间起点(UTC)", example = "2026-05-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime settledAtFrom;

    @Schema(description = "结算时间终点(UTC)", example = "2026-06-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime settledAtTo;
}
