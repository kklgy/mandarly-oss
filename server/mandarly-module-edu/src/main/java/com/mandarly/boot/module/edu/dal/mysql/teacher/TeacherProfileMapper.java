package com.mandarly.boot.module.edu.dal.mysql.teacher;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfilePageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeacherProfileMapper extends BaseMapperX<TeacherProfileDO> {

    /**
     * Admin 后台 — 教师档案分页查询(按审核状态 / 关键字)
     */
    default PageResult<TeacherProfileDO> selectPage(TeacherProfilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TeacherProfileDO>()
                .eqIfPresent(TeacherProfileDO::getUserId, reqVO.getUserId())
                .eqIfPresent(TeacherProfileDO::getAuditStatus, reqVO.getAuditStatus())
                .likeIfPresent(TeacherProfileDO::getIntro, reqVO.getIntroKeyword())
                .eqIfPresent(TeacherProfileDO::getAccent, reqVO.getAccent())
                .orderByDesc(TeacherProfileDO::getCreateTime));
    }

}
