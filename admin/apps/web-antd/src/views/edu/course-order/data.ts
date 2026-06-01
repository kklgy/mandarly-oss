import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { getRangePickerDefaultProps } from '#/utils';

/** 订单状态选项(与后端 OrderStatusEnum 对齐) */
export const COURSE_ORDER_STATUS_OPTIONS = [
  { label: '待上课', value: 'upcoming' },
  { label: '已完成', value: 'finished' },
  { label: '待评价', value: 'to_review' },
  { label: '退款中', value: 'refunding' },
  { label: '已退款', value: 'refunded' },
  { label: '已取消', value: 'cancelled' },
  { label: '异常(待人工处理)', value: 'abnormal' },
  { label: '学生未到课', value: 'no_show_student' },
  { label: '教师未到课', value: 'no_show_teacher' },
];

/** 状态 → 文案 */
export const COURSE_ORDER_STATUS_LABEL: Record<string, string> = {
  upcoming: '待上课',
  finished: '已完成',
  to_review: '待评价',
  refunding: '退款中',
  refunded: '已退款',
  cancelled: '已取消',
  abnormal: '异常',
  no_show_student: '学生未到课',
  no_show_teacher: '教师未到课',
};

/** 状态 → a-tag 内置 color */
export const COURSE_ORDER_STATUS_COLOR: Record<string, string> = {
  upcoming: 'blue',
  finished: 'green',
  to_review: 'purple',
  refunding: 'orange',
  refunded: 'default',
  cancelled: 'default',
  abnormal: 'red',
  no_show_student: 'orange',
  no_show_teacher: 'red',
};

/** D28 abnormal_reason 自动诊断子类 → 文案(只在客服 admin 端用) */
export const ABNORMAL_REASON_LABEL: Record<string, string> = {
  meeting_missing: '课堂记录缺失',
  meeting_ongoing_overdue: '课堂未正常结束(超时)',
  lcic_init_failed: 'LCIC 房间未创建',
  lcic_no_attendance: '双方未进入课堂',
  meeting_cancelled_orphan: '课堂已取消但订单未同步',
  meeting_unknown_status: '课堂状态异常',
};

/** 取消发起方 → 文案 */
export const CANCELLED_BY_LABEL: Record<string, string> = {
  student: '学生取消',
  teacher: '教师取消',
  admin: '管理员取消',
  system: '系统取消',
};

/** 教师结算状态 → 文案 */
export const TEACHER_SETTLE_STATUS_LABEL: Record<string, string> = {
  pending: '待结算',
  settled: '已结算',
  voided: '已作废',
};

/** 教师结算状态 → a-tag color */
export const TEACHER_SETTLE_STATUS_COLOR: Record<string, string> = {
  pending: 'orange',
  settled: 'green',
  voided: 'default',
};

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'studentId',
      label: '学生ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '输入用户 ID(后续 P2 改为下拉选择器)',
        allowClear: true,
        min: 1,
        controls: false,
        class: 'w-full',
      },
    },
    {
      fieldName: 'teacherId',
      label: '教师ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '输入用户 ID(后续 P2 改为下拉选择器)',
        allowClear: true,
        min: 1,
        controls: false,
        class: 'w-full',
      },
    },
    {
      fieldName: 'status',
      label: '订单状态',
      component: 'Select',
      componentProps: {
        options: COURSE_ORDER_STATUS_OPTIONS,
        placeholder: '请选择订单状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'scheduledRange',
      label: '上课时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '订单ID',
      minWidth: 100,
    },
    {
      field: 'studentId',
      title: '学生ID',
      minWidth: 100,
    },
    {
      field: 'teacherId',
      title: '教师ID',
      minWidth: 100,
    },
    {
      field: 'scheduledAt',
      title: '上课时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'duration',
      title: '时长(分钟)',
      minWidth: 100,
    },
    {
      field: 'priceDisplay',
      title: '订单金额',
      minWidth: 130,
      slots: { default: 'priceDisplay' },
    },
    {
      field: 'status',
      title: '订单状态',
      minWidth: 110,
      slots: { default: 'status' },
    },
    {
      field: 'abnormalReason',
      title: '异常原因',
      minWidth: 160,
      slots: { default: 'abnormalReason' },
    },
    {
      field: 'isFreeTrial',
      title: '免费体验',
      minWidth: 100,
      slots: { default: 'isFreeTrial' },
    },
    {
      field: 'cancelledBy',
      title: '取消方',
      minWidth: 110,
      slots: { default: 'cancelledBy' },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 120,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
