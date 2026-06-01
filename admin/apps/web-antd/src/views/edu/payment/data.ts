import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import type { EduPaymentApi } from '#/api/edu/payment';

/** 支付状态选项 */
export const PAYMENT_STATUS_OPTIONS = [
  { label: '待支付', value: 'pending' },
  { label: '已支付', value: 'paid' },
  { label: '已失败', value: 'failed' },
  { label: '已过期', value: 'expired' },
  { label: '已退款', value: 'refunded' },
  { label: '部分退款', value: 'partial_refunded' },
  { label: '已取消', value: 'cancelled' },
];

/** 状态 → 文案 */
export const PAYMENT_STATUS_LABEL: Record<string, string> = {
  pending: '待支付',
  paid: '已支付',
  failed: '已失败',
  expired: '已过期',
  refunded: '已退款',
  partial_refunded: '部分退款',
  cancelled: '已取消',
};

/** 状态 → a-tag color */
export const PAYMENT_STATUS_COLOR: Record<string, string> = {
  pending: 'warning',
  paid: 'success',
  failed: 'error',
  expired: 'default',
  refunded: 'default',
  partial_refunded: 'orange',
  cancelled: 'default',
};

const CURRENCY_PREFIX: Record<string, string> = {
  CNY: 'CNY ',
  HKD: 'HKD ',
  USD: '$',
};

export function formatCurrencyAmount(
  val?: null | number | string,
  currency?: null | string,
) {
  if (val == null || val === '') return '-';
  const num = Number(val);
  if (!Number.isFinite(num)) return '-';
  const normalized = currency?.toUpperCase();
  const amount = num.toFixed(2);
  if (!normalized) return amount;
  const prefix = CURRENCY_PREFIX[normalized] ?? `${normalized} `;
  return `${prefix}${amount}`;
}

export function formatUsdAmount(val?: null | number | string) {
  return formatCurrencyAmount(val, 'USD');
}

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
      fieldName: 'packageId',
      label: '套餐ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '输入套餐 ID',
        allowClear: true,
        min: 1,
        controls: false,
        class: 'w-full',
      },
    },
    {
      fieldName: 'status',
      label: '支付状态',
      component: 'Select',
      componentProps: {
        options: PAYMENT_STATUS_OPTIONS,
        placeholder: '请选择支付状态',
        allowClear: true,
      },
    },
  ];
}

/** 列定义 */
export function useGridColumns(): VxeTableGridOptions<EduPaymentApi.Payment>['columns'] {
  return [
    {
      field: 'id',
      title: '支付单ID',
      minWidth: 100,
    },
    {
      field: 'userId',
      title: '学生ID',
      minWidth: 100,
    },
    {
      field: 'packageId',
      title: '套餐ID',
      minWidth: 100,
    },
    {
      field: 'amountRequest',
      title: '请求金额',
      minWidth: 140,
      slots: { default: 'amountRequest' },
    },
    {
      field: 'amountPaid',
      title: '实付金额',
      minWidth: 140,
      slots: { default: 'amountPaid' },
    },
    {
      field: 'amountSettledUsd',
      title: '结算金额(USD)',
      minWidth: 140,
      slots: { default: 'amountSettledUsd' },
    },
    {
      field: 'discountAmountUsd',
      title: '折扣(USD)',
      minWidth: 110,
      slots: { default: 'discountAmountUsd' },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
      slots: { default: 'status' },
    },
    {
      field: 'paymentMethodType',
      title: '支付方式',
      minWidth: 110,
    },
    {
      field: 'paidAt',
      title: '支付时间',
      minWidth: 180,
      slots: { default: 'paidAt' },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
