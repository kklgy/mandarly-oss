package com.mandarly.boot.module.edu.dal.mysql.withdrawal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.withdrawal.vo.WithdrawalPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Mapper
public interface TeacherWithdrawalMapper extends BaseMapperX<TeacherWithdrawalDO> {

    /**
     * 锁内重复申请校验:同教师是否已存在 pending / approved 行
     */
    default List<TeacherWithdrawalDO> selectByTeacherIdAndStatusIn(Long teacherId, Collection<String> statuses) {
        return selectList(new LambdaQueryWrapper<TeacherWithdrawalDO>()
                .eq(TeacherWithdrawalDO::getTeacherId, teacherId)
                .in(TeacherWithdrawalDO::getStatus, statuses));
    }

    /**
     * 教师视角分页(可选 status 过滤,按 appliedAt 倒序)
     */
    default PageResult<TeacherWithdrawalDO> selectPageForTeacher(Long teacherId, String status, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<TeacherWithdrawalDO>()
                .eq(TeacherWithdrawalDO::getTeacherId, teacherId)
                .eqIfPresent(TeacherWithdrawalDO::getStatus, status)
                .orderByDesc(TeacherWithdrawalDO::getAppliedAt));
    }

    /**
     * Admin 视角分页(支持 status / teacherId / 金额区间 / 时间区间过滤)
     *
     * <p>注:`teacherKeyword`(教师昵称 / 手机模糊)需 JOIN system_users,A7 Controller 阶段
     * 如需该过滤再加 XML JOIN 查询;A4 此处先支持 Wrapper 能力范围内的字段。
     */
    default PageResult<TeacherWithdrawalDO> selectPageForAdmin(WithdrawalPageReqVO req) {
        return selectPage(req, new LambdaQueryWrapperX<TeacherWithdrawalDO>()
                .eqIfPresent(TeacherWithdrawalDO::getStatus, req.getStatus())
                .eqIfPresent(TeacherWithdrawalDO::getTeacherId, req.getTeacherId())
                .geIfPresent(TeacherWithdrawalDO::getAmount, req.getAmountMin())
                .leIfPresent(TeacherWithdrawalDO::getAmount, req.getAmountMax())
                .geIfPresent(TeacherWithdrawalDO::getAppliedAt, req.getAppliedAtFrom())
                .leIfPresent(TeacherWithdrawalDO::getAppliedAt, req.getAppliedAtTo())
                .orderByDesc(TeacherWithdrawalDO::getAppliedAt));
    }

    /**
     * 对账告警 Job 用:聚合 SUM(amount) FROM teacher_withdrawal
     * WHERE teacher_id=? AND status IN ('pending', 'approved')
     * — 期望 = balance.pending_withdraw(spec §4.3 / db-design v1.2 §4.3 query 3)
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM teacher_withdrawal " +
            "WHERE teacher_id = #{teacherId} AND status IN ('pending', 'approved') AND deleted = 0")
    BigDecimal sumInflightAmount(@Param("teacherId") Long teacherId);
}
