package com.mandarly.boot.module.edu.dal.mysql.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TeacherScheduleMapper extends BaseMapperX<TeacherScheduleDO> {

    default List<TeacherScheduleDO> selectListByTeacherId(Long teacherId) {
        return selectList(Wrappers.<TeacherScheduleDO>lambdaQuery()
                .eq(TeacherScheduleDO::getTeacherId, teacherId)
                .orderByAsc(TeacherScheduleDO::getWeekday)
                .orderByAsc(TeacherScheduleDO::getStartTime));
    }

}
