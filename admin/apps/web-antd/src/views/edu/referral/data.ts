import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import type { EduReferralApi } from '#/api/edu/referral';

/** 推荐状态选项 */
export const REFERRAL_STATUS_OPTIONS = [
  { label: '已绑定', value: 'bound' },
  { label: '已奖励', value: 'rewarded' },
  { label: '已作废', value: 'voided' },
];

/** 状态 → 文案 */
export const REFERRAL_STATUS_LABEL: Record<string, string> = {
  bound: '已绑定',
  rewarded: '已奖励',
  voided: '已作废',
};

/** 状态 → a-tag color */
export const REFERRAL_STATUS_COLOR: Record<string, string> = {
  bound: 'warning',
  rewarded: 'success',
  voided: 'default',
};

/** 搜索表单 schema */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'referrerUserId',
      label: '推荐人ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '输入推荐人 user.id',
        allowClear: true,
        min: 1,
        controls: false,
        class: 'w-full',
      },
    },
    {
      fieldName: 'refereeUserId',
      label: '被推荐人ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '输入被推荐人 user.id',
        allowClear: true,
        min: 1,
        controls: false,
        class: 'w-full',
      },
    },
    {
      fieldName: 'status',
      label: '推荐状态',
      component: 'Select',
      componentProps: {
        options: REFERRAL_STATUS_OPTIONS,
        placeholder: '请选择推荐状态',
        allowClear: true,
      },
    },
  ];
}

/** 列定义 */
export function useGridColumns(): VxeTableGridOptions<EduReferralApi.ReferralRecord>['columns'] {
  return [
    {
      field: 'id',
      title: '记录ID',
      minWidth: 90,
    },
    {
      field: 'referrerEmail',
      title: '推荐人邮箱',
      minWidth: 200,
    },
    {
      field: 'refereeEmail',
      title: '被推荐人邮箱',
      minWidth: 200,
    },
    {
      field: 'referralCode',
      title: '推荐码',
      minWidth: 130,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      slots: { default: 'status' },
    },
    {
      field: 'refereeDiscountAmountUsd',
      title: '被推荐人折扣(USD)',
      minWidth: 160,
      slots: { default: 'refereeDiscountAmountUsd' },
    },
    {
      field: 'referrerRewardPackageId',
      title: '推荐人奖励套餐ID',
      minWidth: 150,
    },
    {
      field: 'boundAt',
      title: '绑定时间',
      minWidth: 180,
      slots: { default: 'boundAt' },
    },
    {
      field: 'rewardedAt',
      title: '奖励发放时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
  ];
}
