package com.mandarly.boot.module.edu.dal.dataobject.booking;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * LCIC 课堂(与订单 1:1)
 *
 * <p>对应 docs/database/02-packages-orders.md §1.8 / DDL `meeting`
 *
 * <p>一期 booking 起步:lcic_class_id 用占位 "stub-{order_id}",
 *    M3 视频联调时调 LCIC API 替换为真实房间 id 与 join URL。
 */
@TableName("meeting")
@Data
@EqualsAndHashCode(callSuper = true)
public class MeetingDO extends TenantBaseDO {

    /**
     * 主键 = course_order.id(1:1)
     */
    @TableId
    private Long orderId;

    /**
     * LCIC 房间 id;一期占位 "stub-{order_id}",M3 LCIC 联调时替换
     */
    private String lcicClassId;

    private String lcicRoomUrl;

    private String studentJoinUrl;

    private String teacherJoinUrl;

    private LocalDateTime studentTokenExpiresAt;

    private LocalDateTime teacherTokenExpiresAt;

    /**
     * created / ongoing / ended / cancelled
     */
    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Boolean studentAttended;

    private Boolean teacherAttended;

}
