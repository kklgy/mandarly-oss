package com.mandarly.boot.module.edu.dal.mysql.booking;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.StudentPackagePageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StudentPackageMapper extends BaseMapperX<StudentPackageDO> {

    default PageResult<StudentPackageDO> selectPage(StudentPackagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentPackageDO>()
                .eqIfPresent(StudentPackageDO::getStudentId, reqVO.getStudentId())
                .eqIfPresent(StudentPackageDO::getPackageId, reqVO.getPackageId())
                .eqIfPresent(StudentPackageDO::getSource, reqVO.getSource())
                .orderByDesc(StudentPackageDO::getCreateTime));
    }

    /**
     * 取学生**仍可用**(remaining > 0 AND expire_at > now)的所有套餐
     */
    default List<StudentPackageDO> selectActiveByStudent(Long studentId, LocalDateTime now) {
        return selectList(Wrappers.<StudentPackageDO>lambdaQuery()
                .eq(StudentPackageDO::getStudentId, studentId)
                .gt(StudentPackageDO::getRemaining, 0)
                .gt(StudentPackageDO::getExpireAt, now)
                .orderByAsc(StudentPackageDO::getExpireAt));
    }

    /**
     * 取学生**全部**套餐(active + expired + exhausted),按购买时间倒序;
     * S8 我的套餐分页用。
     */
    default List<StudentPackageDO> selectAllByStudent(Long studentId) {
        return selectList(Wrappers.<StudentPackageDO>lambdaQuery()
                .eq(StudentPackageDO::getStudentId, studentId)
                .orderByDesc(StudentPackageDO::getCreateTime));
    }

}
