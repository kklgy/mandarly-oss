package com.mandarly.boot.module.edu.dal.mysql.income;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.util.MyBatisUtils;
import com.mandarly.boot.module.edu.controller.admin.income.vo.TeacherIncomePageReqVO;
import com.mandarly.boot.module.edu.controller.admin.income.vo.TeacherIncomeRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherIncomeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherIncomeMapper extends BaseMapperX<TeacherIncomeDO> {

    /**
     * 重建 balance 用:按 teacher_id 聚合 frozen / available / total
     * <p>frozenThreshold = now() - frozen_days(7d),settled_at <= threshold 的为 available
     * <p>实现在 TeacherIncomeMapper.xml(MyBatis-Plus annotation 不支持 foreach)
     */
    List<Map<String, Object>> aggregateByTeachers(@Param("teacherIds") List<Long> teacherIds,
                                                   @Param("frozenThreshold") LocalDateTime frozenThreshold);

    /**
     * 找出有 income 变动的 teacher_id(daily Job 增量重建用,M5 提现可优化)
     */
    @Select("SELECT DISTINCT teacher_id FROM teacher_income " +
            "WHERE deleted = 0 AND create_time >= #{since}")
    List<Long> selectChangedTeacherIds(@Param("since") LocalDateTime since);

    /**
     * 教师 app 端"我的收入"分页(M6 §3.7 A6):按 teacherId 固定过滤 + 可选 from/to/type/status,按 settledAt 倒序。
     */
    default PageResult<TeacherIncomeDO> selectMyIncomePage(Long teacherId,
                                                           LocalDateTime from, LocalDateTime to,
                                                           String type, String status,
                                                           PageParam pageParam) {
        return selectPage(pageParam, new com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX<TeacherIncomeDO>()
                .eq(TeacherIncomeDO::getTeacherId, teacherId)
                .geIfPresent(TeacherIncomeDO::getSettledAt, from)
                .leIfPresent(TeacherIncomeDO::getSettledAt, to)
                .eqIfPresent(TeacherIncomeDO::getType, type)
                .eqIfPresent(TeacherIncomeDO::getStatus, status)
                .orderByDesc(TeacherIncomeDO::getSettledAt));
    }

    /**
     * 分页查询收入流水(JOIN system_users + course_order + package),供 admin TeacherIncomeController 使用
     */
    List<TeacherIncomeRespVO> selectPageWithJoin(IPage<TeacherIncomeRespVO> page, @Param("req") TeacherIncomePageReqVO req);

    default PageResult<TeacherIncomeRespVO> selectIncomePageWithJoin(TeacherIncomePageReqVO req) {
        IPage<TeacherIncomeRespVO> iPage = MyBatisUtils.buildPage(req);
        List<TeacherIncomeRespVO> list = selectPageWithJoin(iPage, req);
        return new PageResult<>(list, iPage.getTotal());
    }

    /**
     * 找该 student_package 已经结算的 normal/free_trial income(扣回时用)
     */
    @Select("SELECT ti.* FROM teacher_income ti " +
            "INNER JOIN course_order co ON ti.course_order_id = co.id " +
            "WHERE co.student_package_id = #{studentPackageId} " +
            "  AND ti.type IN ('normal','free_trial') " +
            "  AND ti.deleted = 0 AND co.deleted = 0")
    List<TeacherIncomeDO> selectByStudentPackage(@Param("studentPackageId") Long studentPackageId);

    /**
     * 退款扣回时用:按 courseOrderId + type 查唯一原始 income 行
     */
    default TeacherIncomeDO selectByOrderIdAndType(Long courseOrderId, String type) {
        return selectOne(new LambdaQueryWrapper<TeacherIncomeDO>()
                .eq(TeacherIncomeDO::getCourseOrderId, courseOrderId)
                .eq(TeacherIncomeDO::getType, type));
    }

    /**
     * UnfreezeIncomeJob 用:扫到期的 frozen 流水(frozen_until <= cutoff)
     *
     * <p>按 teacher_id 排序,便于 Job 按 teacher 分组聚合解冻金额。
     * <p>limit 由 Job 传入(spec §3.5 单批最大 1000)。
     */
    @Select("SELECT * FROM teacher_income " +
            "WHERE status = 'frozen' AND frozen_until <= #{cutoff} AND deleted = 0 " +
            "ORDER BY teacher_id, id " +
            "LIMIT #{limit}")
    List<TeacherIncomeDO> selectFrozenDue(@Param("cutoff") LocalDateTime cutoff,
                                          @Param("limit") int limit);

    /**
     * 对账告警 Job 用:聚合 SUM(amount) FROM teacher_income
     * WHERE teacher_id=? AND deleted=0 AND status != 'reverted'
     * — 期望 = total_earned + total_withdrawn(spec §4.3 / db-design v1.2 §4.3 query 1)
     */
    @Select("SELECT COALESCE(SUM(amount_usd), 0) FROM teacher_income " +
            "WHERE teacher_id = #{teacherId} AND deleted = 0 AND status != 'reverted'")
    BigDecimal sumNonRevertedAmount(@Param("teacherId") Long teacherId);

    /**
     * 对账告警 Job 用:聚合 SUM(amount) FROM teacher_income
     * WHERE teacher_id=? AND status='frozen' AND frozen_until > NOW()
     * — 期望 = balance.frozen_t7(spec §4.3 query 2)
     */
    @Select("SELECT COALESCE(SUM(amount_usd), 0) FROM teacher_income " +
            "WHERE teacher_id = #{teacherId} AND status = 'frozen' " +
            "  AND frozen_until > #{now} AND deleted = 0")
    BigDecimal sumStillFrozenAmount(@Param("teacherId") Long teacherId,
                                    @Param("now") LocalDateTime now);
}
