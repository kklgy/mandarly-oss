import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import type { EduTeacherIncomeApi } from '#/api/edu/teacher-income';

/** 收入类型选项 */
export const INCOME_TYPE_OPTIONS = [
  { label: '普通课', value: 'normal' },
  { label: '免费体验', value: 'free_trial' },
  { label: '学生缺席', value: 'no_show_student' },
  { label: '退款扣回', value: 'refund_deduct' },
];

/** 类型 → 文案 */
export const INCOME_TYPE_LABEL: Record<string, string> = {
  normal: '普通课',
  free_trial: '免费体验',
  no_show_student: '学生缺席',
  refund_deduct: '退款扣回',
};

/** 类型 → a-tag color */
export const INCOME_TYPE_COLOR: Record<string, string> = {
  normal: 'blue',
  free_trial: 'purple',
  no_show_student: 'warning',
  refund_deduct: 'error',
};

/** 搜索表单 schema */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'teacherId',
      label: '教师ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '输入教师 user.id',
        allowClear: true,
        min: 1,
        controls: false,
        class: 'w-full',
      },
    },
    {
      fieldName: 'type',
      label: '收入类型',
      component: 'Select',
      componentProps: {
        options: INCOME_TYPE_OPTIONS,
        placeholder: '请选择收入类型',
        allowClear: true,
      },
    },
  ];
}

/** 列定义 */
export function useGridColumns(): VxeTableGridOptions<EduTeacherIncomeApi.TeacherIncome>['columns'] {
  return [
    {
      field: 'id',
      title: '流水ID',
      minWidth: 90,
    },
    {
      field: 'teacherEmail',
      title: '教师邮箱',
      minWidth: 200,
    },
    {
      field: 'teacherName',
      title: '教师昵称',
      minWidth: 120,
    },
    {
      field: 'packageName',
      title: '套餐名称',
      minWidth: 150,
    },
    {
      field: 'amountUsd',
      title: '金额(USD)',
      minWidth: 120,
      slots: { default: 'amountUsd' },
    },
    {
      field: 'type',
      title: '收入类型',
      minWidth: 110,
      slots: { default: 'type' },
    },
    {
      field: 'courseOrderId',
      title: '关联课程单',
      minWidth: 110,
    },
    {
      field: 'refundId',
      title: '关联退款单',
      minWidth: 110,
    },
    {
      field: 'settledAt',
      title: '结算时间',
      minWidth: 180,
      slots: { default: 'settledAt' },
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
