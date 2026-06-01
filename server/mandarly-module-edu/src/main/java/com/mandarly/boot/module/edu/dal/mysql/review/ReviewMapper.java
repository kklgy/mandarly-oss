package com.mandarly.boot.module.edu.dal.mysql.review;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.review.ReviewDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapperX<ReviewDO> {

    /**
     * 教师评价列表(公开端,只查 is_visible=1),按 update_time 倒序。
     */
    default PageResult<ReviewDO> selectVisiblePageByTeacher(Long teacherId, PageParam pageParam) {
        return selectPage(pageParam, Wrappers.<ReviewDO>lambdaQuery()
                .eq(ReviewDO::getTeacherId, teacherId)
                .eq(ReviewDO::getIsVisible, true)
                .orderByDesc(ReviewDO::getUpdateTime));
    }

    /**
     * 学生写过的评价分页(自己看),按 update_time 倒序。
     */
    default PageResult<ReviewDO> selectPageByStudent(Long studentId, PageParam pageParam) {
        return selectPage(pageParam, Wrappers.<ReviewDO>lambdaQuery()
                .eq(ReviewDO::getStudentId, studentId)
                .orderByDesc(ReviewDO::getUpdateTime));
    }

    /**
     * 教师评分聚合(只算 is_visible=1)。返回 [avgRating, reviewCount]。
     * MyBatis 自动映射到 Map,这里走原生 SQL 取双值。
     */
    @Select("SELECT IFNULL(AVG(rating), 0) AS avg_rating, COUNT(*) AS review_count "
            + "FROM review "
            + "WHERE teacher_id = #{teacherId} AND is_visible = 1 AND deleted = 0")
    java.util.Map<String, Object> selectStatByTeacher(@Param("teacherId") Long teacherId);

    /**
     * 学生待评价的 finished 订单 id 列表(用于 MyOrders 待评价 tab)。
     * 一期未走该接口,留作后续扩展。
     */
    @Select("SELECT co.id FROM course_order co "
            + "LEFT JOIN review r ON r.order_id = co.id AND r.deleted = 0 "
            + "WHERE co.student_id = #{studentId} AND co.status = 'finished' "
            + "AND co.deleted = 0 AND r.order_id IS NULL "
            + "ORDER BY co.scheduled_at DESC")
    List<Long> selectPendingReviewOrderIds(@Param("studentId") Long studentId);

}
