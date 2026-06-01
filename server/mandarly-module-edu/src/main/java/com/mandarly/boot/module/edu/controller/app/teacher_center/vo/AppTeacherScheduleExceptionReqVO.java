package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户端 - 教师中心:单次例外操作 Request VO
 *
 * <p>对应 spec §5.1 数据流 {@code POST /exceptions { date, hh, mm, action }}。
 *
 * <p>action 取值:
 * <ul>
 *   <li>{@code closed} — 新增"该日 30-min 时段不可约"例外</li>
 *   <li>{@code extra} — 新增"该日 30-min 时段额外可约"例外</li>
 *   <li>{@code remove} — 移除该 30-min 时段对应的例外(按 (date, hh:mm) 精确匹配)</li>
 * </ul>
 */
@Schema(description = "用户端 - 教师中心 - 例外操作 Request VO")
@Data
public class AppTeacherScheduleExceptionReqVO {

    @Schema(description = "例外日期(教师本地时区下)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05-20")
    @NotNull(message = "date 不能为空")
    private LocalDate date;

    @Schema(description = "小时 0-23", requiredMode = Schema.RequiredMode.REQUIRED, example = "9")
    @NotNull(message = "hh 不能为空")
    @Min(value = 0, message = "hh 范围 0-23")
    @Max(value = 23, message = "hh 范围 0-23")
    private Integer hh;

    @Schema(description = "分钟 0 或 30(一期固定 30-min 网格)", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "mm 不能为空")
    @Min(value = 0, message = "mm 范围 0/30")
    @Max(value = 30, message = "mm 范围 0/30")
    private Integer mm;

    @Schema(description = "操作 closed/extra/remove", requiredMode = Schema.RequiredMode.REQUIRED, example = "closed")
    @NotBlank(message = "action 不能为空")
    @Pattern(regexp = "closed|extra|remove", message = "action 仅支持 closed/extra/remove")
    private String action;

    @Schema(description = "时区(IANA),closed/extra 时必填", example = "Asia/Hong_Kong")
    @Size(max = 64)
    private String timezone;

    @Schema(description = "备注(closed/extra 可选)", example = "临时请假")
    @Size(max = 256)
    private String reason;
}
