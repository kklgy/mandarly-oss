package com.mandarly.boot.module.edu.dal.mysql.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.ScheduleExceptionDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleExceptionMapper extends BaseMapperX<ScheduleExceptionDO> {

    default List<ScheduleExceptionDO> selectListByTeacherIdAndDateRange(Long teacherId, LocalDate from, LocalDate to) {
        return selectList(Wrappers.<ScheduleExceptionDO>lambdaQuery()
                .eq(ScheduleExceptionDO::getTeacherId, teacherId)
                .ge(from != null, ScheduleExceptionDO::getExceptionDate, from)
                .le(to != null, ScheduleExceptionDO::getExceptionDate, to)
                .orderByAsc(ScheduleExceptionDO::getExceptionDate)
                .orderByAsc(ScheduleExceptionDO::getStartTime));
    }

}
