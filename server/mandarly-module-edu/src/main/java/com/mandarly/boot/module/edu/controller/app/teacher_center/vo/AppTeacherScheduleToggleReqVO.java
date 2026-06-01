package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户端 - 教师中心:周模板单格子切换 Request VO
 *
 * <p>对应 spec §5.1 数据流 {@code POST /toggle { dayOfWeek, hh, mm, available }}。
 *
 * <p>语义:
 * <ul>
 *   <li>available=true → 在 (dayOfWeek, hh:mm) 上新增一段 30-min 可约区间(若已存在覆盖区间则幂等)</li>
 *   <li>available=false → 从 (dayOfWeek, hh:mm) 撤销可约(精确删除该 30-min 区间;
 *       若仅有覆盖该格子的更大区间,Service 选择不拆分,直接 404 — 由前端发详细编辑请求)</li>
 * </ul>
 */
@Schema(description = "用户端 - 教师中心 - 周模板格子切换 Request VO")
@Data
public class AppTeacherScheduleToggleReqVO {

    @Schema(description = "周几 0=周日..6=周六", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "dayOfWeek 不能为空")
    @Min(value = 0, message = "dayOfWeek 范围 0-6")
    @Max(value = 6, message = "dayOfWeek 范围 0-6")
    private Integer dayOfWeek;

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

    @Schema(description = "是否可约 true=新增 / false=撤销", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "available 不能为空")
    private Boolean available;

    @Schema(description = "时区(IANA),available=true 新增时必填(Service 层条件校验);available=false 撤销不需要",
            example = "Asia/Hong_Kong")
    @Size(max = 64)
    private String timezone;
}
