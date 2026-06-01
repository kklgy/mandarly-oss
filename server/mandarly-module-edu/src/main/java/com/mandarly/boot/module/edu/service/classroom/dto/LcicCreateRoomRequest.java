package com.mandarly.boot.module.edu.service.classroom.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建 LCIC 课堂房间入参
 */
@Data
@Builder
public class LcicCreateRoomRequest {

    private Long orderId;

    private Long teacherId;

    private Long studentId;

    private String teacherName;

    private String studentName;

    /** 课程开始时间 */
    private LocalDateTime scheduledAt;

    /** 课时分钟数 */
    private Integer durationMinutes;

}
