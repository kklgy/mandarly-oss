package com.mandarly.boot.module.edu.dal.dataobject.schedule;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 教师时段例外(临时不可约 / 新增可约)
 *
 * <p>对应 docs/database/01-users-auth.md §1.5;DDL 见 server/sql/mysql/mandarly.sql §01
 *
 * <p>type=closed:整天/部分时段不可约,start/end 可空表示整天;
 *    type=extra :在常规 weekly schedule 之外新增可约时段,start/end 必填。
 */
@TableName("schedule_exception")
@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleExceptionDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教师 user.id
     */
    private Long teacherId;

    /**
     * 日期(教师本地时区下)
     */
    private LocalDate exceptionDate;

    /**
     * 类型:closed / extra
     */
    private String type;

    /**
     * 起始时刻;closed 可空(整天)
     */
    private LocalTime startTime;

    /**
     * 结束时刻;closed 可空(整天)
     */
    private LocalTime endTime;

    /**
     * 时区(IANA)
     */
    private String timezone;

    /**
     * 备注(可空)
     */
    private String reason;

}
