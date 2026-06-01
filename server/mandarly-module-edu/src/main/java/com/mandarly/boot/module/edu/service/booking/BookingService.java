package com.mandarly.boot.module.edu.service.booking;

import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCancelReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCreateReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.CourseOrderPageReqVO;
import com.mandarly.boot.module.edu.controller.app.booking.vo.AppBookingCountsRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;

import java.util.Collection;

/**
 * 课程预约 Service(订单状态机入口)
 *
 * <p>对应 docs/database/02-packages-orders.md §1.5 扣次优先级 + §3.1 状态机
 *
 * <p>一期 booking 实现 upcoming + cancelled 两态;LCIC 真实开房用占位字符串,
 *    M3 视频联调阶段调 LCIC API 替换 meeting.lcic_class_id / join_url;
 *    finished / no_show / abnormal 等状态依赖 LCIC Webhook,M3 实现。
 */
public interface BookingService {

    PageResult<CourseOrderDO> getOrderPage(CourseOrderPageReqVO reqVO);

    /**
     * 用户端"我的订单"分页(按 studentId 固定过滤 + 可选 status IN)。
     *
     * @param studentId 学生 user.id
     * @param statuses  null/空 表示不过滤;非空走 IN
     * @param pageParam pageNo / pageSize
     */
    PageResult<CourseOrderDO> getMyOrdersPage(Long studentId, Collection<String> statuses, PageParam pageParam);

    /**
     * 教师视角"我的订单"分页(按 teacherId 固定过滤 + 可选 status IN)。
     *
     * <p>M6 §3.7:服务于 {@code /app-api/edu/teacher-center/orders/list}。
     *
     * @param teacherId 教师 user.id
     * @param statuses  null/空 表示不过滤;非空走 IN
     * @param pageParam pageNo / pageSize
     */
    PageResult<CourseOrderDO> getTeacherOrdersPage(Long teacherId, Collection<String> statuses, PageParam pageParam);

    CourseOrderDO getOrder(Long id);

    /**
     * 创建预约订单(状态机起点 → upcoming)。
     *
     * <p>处理流程:
     * <ol>
     *   <li>校验学生 / 教师身份与状态</li>
     *   <li>选 student_package(reqVO 指定 / 按 §1.5 扣次优先级自动选)</li>
     *   <li>校验教师该 UTC 时间无冲突订单</li>
     *   <li>事务内:扣 1 次 student_package.remaining + 创建 course_order(upcoming) + 创建 meeting(占位)</li>
     * </ol>
     *
     * @return 订单 id
     */
    Long createOrder(BookingCreateReqVO reqVO);

    /**
     * 取消订单(upcoming → cancelled)。
     *
     * <p>规则(PRD §10):
     * <ul>
     *   <li>课前 24h+:返还 1 课次到原 student_package(remaining +1),is_refunded_class=1</li>
     *   <li>课前 24h 内:不返还课次(逻辑保留 student_package_id),is_refunded_class=0</li>
     * </ul>
     */
    void cancelOrder(BookingCancelReqVO reqVO);

    /**
     * D28 过期 upcoming 兜底扫描:
     * 扫 status='upcoming' AND scheduledAt + duration + 15min &lt; now() 的孤儿订单,
     * 按 meeting 实际状态分流到 finished(LCIC webhook 丢包补救)或 abnormal(进客诉队列)。
     *
     * <p>由 {@code OverdueUpcomingSweepJob} 每 15 min 调用;首次跑会清历史污染数据。
     *
     * <p>分流规则见 {@code BookingServiceImpl#sweepOverdueUpcoming} 实现;6 个分支映射到
     * abnormal_reason 子类(由 admin 客服在订单管理后台查实后再录 abnormal_resolution)。
     *
     * @return 摘要文本,形如 "scanned=10 finished=2 abnormal=8 (lcic_init_failed=5 lcic_no_attendance=3)"
     */
    String sweepOverdueUpcoming();

    /**
     * 我的订单角标计数(Wave 3 全局徽章数据源)。
     *
     * <p>对应 <code>GET /app-api/edu/booking/counts</code>,前端
     * {@code app/src/stores/bookingCounts.js} 30s 缓存 + visibilitychange / 取消 / 评价后强刷。
     *
     * <p>口径(M5 Wave 3 落地):
     * <ul>
     *   <li>upcomingCount = course_order.status='upcoming' AND scheduled_at &gt; now()</li>
     *   <li>toReviewCount = course_order.status='finished' AND review.order_id IS NULL
     *       (复用 ReviewMapper#selectPendingReviewOrderIds)</li>
     *   <li>refundingCount = course_order.status IN ('refunding','refunded')
     *       (M4 Stripe webhook 推动状态机,M5 阶段一般为 0)</li>
     * </ul>
     *
     * @param studentId 学生 user.id(由 SecurityFrameworkUtils.getLoginUserId() 取)
     */
    AppBookingCountsRespVO getMyOrderCounts(Long studentId);

}
