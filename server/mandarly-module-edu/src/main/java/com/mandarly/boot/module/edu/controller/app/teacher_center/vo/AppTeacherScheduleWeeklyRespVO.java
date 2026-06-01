package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * 用户端 - 教师中心:周排课模板 Response VO
 *
 * <p>对应 PRD M6 §3.7 / §5.1 ScheduleView 顶部 33×7 网格。
 * 后端返回原始区间数据(每条 = (weekday, startTime, endTime, timezone)),
 * 前端按教师时区渲染 30-min 格子。
 *
 * <p>结构:
 * <pre>
 * {
 *   "teacherId": 2048,
 *   "timezone": "Asia/Hong_Kong",
 *   "slots": [
 *     { "weekday": 1, "startTime": "09:00:00", "endTime": "12:00:00" },
 *     ...
 *   ]
 * }
 * </pre>
 */
@Schema(description = "用户端 - 教师中心 - 周排课模板 Response VO")
@Data
public class AppTeacherScheduleWeeklyRespVO {

    @Schema(description = "教师 user.id", example = "2048")
    private Long teacherId;

    @Schema(description = "教师本地时区(IANA),前端按此渲染", example = "Asia/Hong_Kong")
    private String timezone;

    @Schema(description = "周排课区间列表(weekday 0=Sun..6=Sat)")
    private List<Slot> slots;

    @Schema(description = "周排课单个区间")
    @Data
    public static class Slot {
        @Schema(description = "区间 id", example = "10001")
        private Long id;

        @Schema(description = "周几 0=Sun..6=Sat", example = "1")
        private Integer weekday;

        @Schema(description = "起始时刻(教师本地时区)", example = "09:00:00")
        private LocalTime startTime;

        @Schema(description = "结束时刻(教师本地时区)", example = "12:00:00")
        private LocalTime endTime;
    }
}
