package com.mandarly.boot.module.edu.dal.mysql.booking;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.CourseOrderPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CourseOrderMapper extends BaseMapperX<CourseOrderDO> {

    default PageResult<CourseOrderDO> selectPage(CourseOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CourseOrderDO>()
                .eqIfPresent(CourseOrderDO::getStudentId, reqVO.getStudentId())
                .eqIfPresent(CourseOrderDO::getTeacherId, reqVO.getTeacherId())
                .eqIfPresent(CourseOrderDO::getStatus, reqVO.getStatus())
                .geIfPresent(CourseOrderDO::getScheduledAt, reqVO.getScheduledFrom())
                .leIfPresent(CourseOrderDO::getScheduledAt, reqVO.getScheduledTo())
                .orderByDesc(CourseOrderDO::getScheduledAt));
    }

    /**
     * 用户端"我的订单"分页:固定按 studentId 过滤,可选多状态 IN 过滤,按上课时间倒序。
     */
    default PageResult<CourseOrderDO> selectMyOrdersPage(Long studentId,
                                                        java.util.Collection<String> statuses,
                                                        com.mandarly.boot.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CourseOrderDO>()
                .eq(CourseOrderDO::getStudentId, studentId)
                .inIfPresent(CourseOrderDO::getStatus, statuses)
                .orderByDesc(CourseOrderDO::getScheduledAt));
    }

    /**
     * 教师视角"我的订单"分页:固定按 teacherId 过滤,可选多状态 IN 过滤,按上课时间倒序。
     *
     * <p>M6 §3.7 / A6:服务于 {@code /app-api/edu/teacher-center/orders/list}。
     */
    default PageResult<CourseOrderDO> selectTeacherOrdersPage(Long teacherId,
                                                              java.util.Collection<String> statuses,
                                                              com.mandarly.boot.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CourseOrderDO>()
                .eq(CourseOrderDO::getTeacherId, teacherId)
                .inIfPresent(CourseOrderDO::getStatus, statuses)
                .orderByDesc(CourseOrderDO::getScheduledAt));
    }

    /**
     * 检查教师在某 UTC 时间是否已有 upcoming 订单(冲突检测)
     */
    default List<CourseOrderDO> selectByTeacherAndScheduledAt(Long teacherId, LocalDateTime scheduledAt, String status) {
        return selectList(Wrappers.<CourseOrderDO>lambdaQuery()
                .eq(CourseOrderDO::getTeacherId, teacherId)
                .eq(CourseOrderDO::getScheduledAt, scheduledAt)
                .eq(CourseOrderDO::getStatus, status));
    }

    /**
     * 统计教师在 [start, end) 窗口内某状态的订单数。
     *
     * <p>D16 P2 教师 Dashboard 聚合用 — 时间字段 scheduledAt(UTC LocalDateTime)。
     *
     * @param teacherId 教师 user.id
     * @param status    订单状态(如 upcoming / finished)
     * @param startUtc  起始 UTC(含,LocalDateTime)
     * @param endUtc    截止 UTC(不含,LocalDateTime)
     * @return 命中订单数(可能为 0,不会 null)
     */
    default Long countByTeacherInWindow(Long teacherId, String status,
                                        LocalDateTime startUtc, LocalDateTime endUtc) {
        return selectCount(Wrappers.<CourseOrderDO>lambdaQuery()
                .eq(CourseOrderDO::getTeacherId, teacherId)
                .eq(CourseOrderDO::getStatus, status)
                .ge(CourseOrderDO::getScheduledAt, startUtc)
                .lt(CourseOrderDO::getScheduledAt, endUtc));
    }

    /**
     * 求和教师在 [start, end) 窗口内某状态订单的 teacher_amount。
     *
     * <p>D16 P2 教师 Dashboard 聚合(本月收入)用 — finished 订单按 scheduledAt 落窗口。
     *
     * @param teacherId 教师 user.id
     * @param status    订单状态(通常 finished)
     * @param startUtc  起始 UTC(含)
     * @param endUtc    截止 UTC(不含)
     * @return 累加 teacher_amount;无命中订单返 null
     */
    default BigDecimal sumTeacherAmountByTeacherInWindow(Long teacherId, String status,
                                                          LocalDateTime startUtc, LocalDateTime endUtc) {
        List<CourseOrderDO> rows = selectList(Wrappers.<CourseOrderDO>lambdaQuery()
                .select(CourseOrderDO::getTeacherAmount)
                .eq(CourseOrderDO::getTeacherId, teacherId)
                .eq(CourseOrderDO::getStatus, status)
                .ge(CourseOrderDO::getScheduledAt, startUtc)
                .lt(CourseOrderDO::getScheduledAt, endUtc));
        if (rows.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (CourseOrderDO o : rows) {
            if (o.getTeacherAmount() != null) {
                sum = sum.add(o.getTeacherAmount());
            }
        }
        return sum;
    }

    /**
     * D28 OverdueUpcomingSweepJob 扫描入口:
     * status='upcoming' AND DATE_ADD(scheduled_at, INTERVAL duration MINUTE) < cutoffUtc,
     * 单次 LIMIT {@code limit} 防爆。按 scheduled_at 升序扫,先处理最早的孤儿。
     *
     * <p>cutoffUtc 由 Service 算出 = now() - buffer(默认 15min);显式由 Service 传 LocalDateTime,
     * 避免 MySQL NOW() 与 JVM 时区漂移。
     *
     * @param cutoffUtc 截止 UTC(scheduled_at + duration < cutoffUtc 即视为过期)
     * @param limit     本次最多扫多少条(防爆,默认 1000)
     * @return 过期 upcoming 单列表
     */
    default List<CourseOrderDO> selectOverdueUpcoming(LocalDateTime cutoffUtc, int limit) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseOrderDO>()
                .eq("status", "upcoming")
                .apply("DATE_ADD(scheduled_at, INTERVAL duration MINUTE) < {0}", cutoffUtc)
                .orderByAsc("scheduled_at")
                .last("LIMIT " + Math.max(1, limit)));
    }

}
