import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduCourseOrderApi {
  /** 课程订单状态(与后端 OrderStatusEnum 对齐) */
  export type CourseOrderStatus =
    | 'abnormal'
    | 'cancelled'
    | 'finished'
    | 'no_show_student'
    | 'no_show_teacher'
    | 'refunded'
    | 'refunding'
    | 'to_review'
    | 'upcoming';

  /** 取消发起方 */
  export type CancelledBy = 'admin' | 'student' | 'system' | 'teacher';

  /** 教师结算状态 */
  export type TeacherSettleStatus = 'pending' | 'settled' | 'voided';

  /** 课程订单(只读 RespVO) */
  export interface CourseOrder {
    id: number;
    studentId: number;
    teacherId: number;
    studentPackageId?: number;
    /** 上课时间(UTC ISO 字符串) */
    scheduledAt: string;
    /** 时长(分钟) */
    duration: number;
    /** 显示价格 BigDecimal */
    priceDisplay: number | string;
    currency: string;
    status: CourseOrderStatus;
    cancelReason?: string;
    /** 异常子类(D28 OverdueUpcomingSweepJob 自动写入,仅 status=abnormal 时有值) */
    abnormalReason?: string;
    /** 客服处置结论(admin 人工录入,仅 status=abnormal 时有值) */
    abnormalResolution?: string;
    abnormalProcessedBy?: number;
    abnormalProcessedAt?: string;
    /** 完课时间(LCIC room_end 或 D28 sweep 写入) */
    finishedAt?: string;
    cancelledBy?: CancelledBy;
    /** 取消时间(UTC ISO 字符串) */
    cancelledAt?: string;
    isRefundedClass?: boolean;
    isFreeTrial?: boolean;
    teacherAmount?: number | string;
    teacherSettleStatus?: TeacherSettleStatus;
    /** 创建时间(UTC ISO 字符串) */
    createTime?: string;
  }

  /** 列表查询参数 */
  export interface PageReq extends PageParam {
    studentId?: number;
    teacherId?: number;
    status?: CourseOrderStatus;
    /** UTC ISO 起始时间 */
    scheduledFrom?: string;
    /** UTC ISO 结束时间 */
    scheduledTo?: string;
  }
}

/** 查询课程订单分页 */
export function getCourseOrderPage(params: EduCourseOrderApi.PageReq) {
  return requestClient.get<PageResult<EduCourseOrderApi.CourseOrder>>(
    '/edu/course-order/page',
    { params },
  );
}

/** 查询课程订单详情 */
export function getCourseOrder(id: number) {
  return requestClient.get<EduCourseOrderApi.CourseOrder>(
    `/edu/course-order/get?id=${id}`,
  );
}
