import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import type { EduRefundApi } from '#/api/edu/refund';

/** 退款状态选项 */
export const REFUND_STATUS_OPTIONS = [
  { label: '待审核', value: 'pending' },
  { label: '已批准', value: 'approved' },
  { label: '已退款', value: 'refunded' },
  { label: '已拒绝', value: 'rejected' },
  { label: '退款失败', value: 'failed' },
];

/** 状态 → 文案 */
export const REFUND_STATUS_LABEL: Record<string, string> = {
  pending: '待审核',
  approved: '已批准',
  refunded: '已退款',
  rejected: '已拒绝',
  failed: '退款失败',
};

/** 状态 → a-tag color */
export const REFUND_STATUS_COLOR: Record<string, string> = {
  pending: 'warning',
  approved: 'processing',
  refunded: 'success',
  rejected: 'default',
  failed: 'error',
};

/** 搜索表单 schema */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'userId',
      label: '学生ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '输入学生 user.id',
        allowClear: true,
        min: 1,
        controls: false,
        class: 'w-full',
      },
    },
    {
      fieldName: 'paymentId',
      label: '支付单ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '输入支付单 ID',
        allowClear: true,
        min: 1,
        controls: false,
        class: 'w-full',
      },
    },
    {
      fieldName: 'status',
      label: '退款状态',
      component: 'Select',
      componentProps: {
        options: REFUND_STATUS_OPTIONS,
        placeholder: '请选择退款状态',
        allowClear: true,
      },
    },
  ];
}

/** 列定义 */
export function useGridColumns(): VxeTableGridOptions<EduRefundApi.Refund>['columns'] {
  return [
    {
      field: 'id',
      title: '工单ID',
      minWidth: 90,
    },
    {
      field: 'paymentId',
      title: '支付单ID',
      minWidth: 100,
    },
    {
      field: 'userId',
      title: '学生ID',
      minWidth: 90,
    },
    {
      field: 'suggestedAmountUsd',
      title: '建议金额(USD)',
      minWidth: 140,
      slots: { default: 'suggestedAmountUsd' },
    },
    {
      field: 'finalAmountUsd',
      title: '最终金额(USD)',
      minWidth: 140,
      slots: { default: 'finalAmountUsd' },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      slots: { default: 'status' },
    },
    {
      field: 'applyReason',
      title: '申请原因',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '申请时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
