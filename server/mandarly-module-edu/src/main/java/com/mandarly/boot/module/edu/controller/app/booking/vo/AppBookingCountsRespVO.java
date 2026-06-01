package com.mandarly.boot.module.edu.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端 - 我的订单角标计数 Response VO
 *
 * <p>对应 <code>GET /app-api/edu/booking/counts</code>,数据消费方:
 * <ul>
 *   <li>app/src/stores/bookingCounts.js — 30s 缓存 + visibilitychange 强刷</li>
 *   <li>AppTabBar 我的课红点(upcomingCount &gt; 0)</li>
 *   <li>MyOrders OrderTabs 各 tab 数字徽章</li>
 * </ul>
 *
 * <p>口径(M5 Wave 3 落地):
 * <ul>
 *   <li>upcomingCount = course_order.status='upcoming' AND scheduled_at &gt; now()</li>
 *   <li>toReviewCount = course_order.status='finished' AND review.order_id IS NULL
 *       (复用 ReviewMapper#selectPendingReviewOrderIds)</li>
 *   <li>refundingCount = course_order.status IN ('refunding','refunded')
 *       依赖 M4 Stripe webhook 推动状态机,M5 阶段 0 不影响展示</li>
 * </ul>
 */
@Schema(description = "用户端 - 我的订单角标计数 Response VO")
@Data
public class AppBookingCountsRespVO {

    @Schema(description = "待上课数(course_order.status=upcoming AND scheduled_at>now)", example = "3")
    private Integer upcomingCount;

    @Schema(description = "待评价数(status=finished AND review IS NULL)", example = "1")
    private Integer toReviewCount;

    @Schema(description = "退款中数(status IN refunding/refunded;M4 后端推动)", example = "0")
    private Integer refundingCount;

}
