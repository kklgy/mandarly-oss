package com.mandarly.boot.module.edu.enums.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 课程订单状态机
 *
 * <p>对应 docs/database/02-packages-orders.md §3.1
 * <p>一期 booking 实现 upcoming / cancelled 两态;finished / no_show* / refunding 等
 *    分支依赖 LCIC Webhook,在 M3 视频联调阶段补全。
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    UPCOMING("upcoming"),
    CANCELLED("cancelled"),
    FINISHED("finished"),
    NO_SHOW_STUDENT("no_show_student"),
    NO_SHOW_TEACHER("no_show_teacher"),
    REFUNDING("refunding"),
    REFUNDED("refunded"),
    ABNORMAL("abnormal");

    private final String code;

}
