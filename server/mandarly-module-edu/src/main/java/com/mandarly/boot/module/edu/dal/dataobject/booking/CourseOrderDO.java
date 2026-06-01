package com.mandarly.boot.module.edu.dal.dataobject.booking;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程订单(单次预约)
 *
 * <p>对应 docs/database/02-packages-orders.md §3.1 / DDL `course_order`
 * <p>状态机枚举:{@link com.mandarly.boot.module.edu.enums.booking.OrderStatusEnum}
 */
@TableName("course_order")
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseOrderDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long teacherId;

    /**
     * 扣次来源 student_package.id
     */
    private Long studentPackageId;

    /**
     * 上课时间(UTC)
     */
    private LocalDateTime scheduledAt;

    /**
     * 课时长度(分钟),一期固定 30
     */
    private Integer duration;

    /**
     * 展示价(冗余 student_package 当时的单价快照)
     */
    private BigDecimal priceDisplay;

    private String currency;

    private String status;

    private String cancelReason;

    /**
     * status=abnormal 时由 {@code OverdueUpcomingSweepJob} 自动写入的诊断子类
     * (meeting_missing / meeting_ongoing_overdue / lcic_init_failed / lcic_no_attendance / meeting_cancelled_orphan)。
     * 与 {@link #abnormalResolution}(admin 人工录入)语义不同。
     */
    private String abnormalReason;

    /**
     * student / teacher / admin / system
     */
    private String cancelledBy;

    private LocalDateTime cancelledAt;

    /**
     * 取消是否返还课次:1=已返还(24h+)/0=未返还(24h 内 / 缺席)
     */
    private Boolean isRefundedClass;

    private String abnormalResolution;

    private Long abnormalProcessedBy;

    private LocalDateTime abnormalProcessedAt;

    private LocalDateTime finishedAt;

    private BigDecimal teacherAmount;

    private String teacherAmountCurrency;

    private String teacherSettleStatus;

    private Boolean isFreeTrial;

}
