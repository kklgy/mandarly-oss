package com.mandarly.boot.module.edu.dal.dataobject.schedule;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 教师每周可约时段
 *
 * <p>对应 docs/database/01-users-auth.md §1.5;DDL 见 server/sql/mysql/mandarly.sql §01
 *
 * <p>时区策略:start_time / end_time 为教师本地时区下的时刻,timezone 字段记录该条记录设置时使用的时区。
 * 学生预约时由 Service 层按 学生时区 + 教师时区 计算出真实 UTC 落点。
 */
@TableName("teacher_schedule")
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherScheduleDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教师 user.id
     */
    private Long teacherId;

    /**
     * 周几;0=周日 6=周六
     */
    private Integer weekday;

    /**
     * 起始时刻(教师本地时区)
     */
    private LocalTime startTime;

    /**
     * 结束时刻;同日内,end > start
     */
    private LocalTime endTime;

    /**
     * 设置时使用的时区(IANA),如 Asia/Hong_Kong / Africa/Accra
     */
    private String timezone;

}
