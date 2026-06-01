package com.mandarly.boot.module.edu.dal.mysql.teacher;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TeacherQualificationMapper extends BaseMapperX<TeacherQualificationDO> {

    /**
     * 按 user.id 查教师的所有资质文件(按上传时间倒序)
     */
    default List<TeacherQualificationDO> selectListByUserId(Long userId) {
        return selectList(Wrappers.<TeacherQualificationDO>lambdaQuery()
                .eq(TeacherQualificationDO::getUserId, userId)
                .orderByDesc(TeacherQualificationDO::getCreateTime));
    }

}
