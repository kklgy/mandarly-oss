import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import type { EduWithdrawalApi } from '#/api/edu/withdrawal';

import { $t } from '#/locales';

/** 提现状态选项(全部 / 5 状态)— function 形式以保证 t() 在调用时取到当前 locale */
export function getWithdrawalStatusOptions() {
  return [
    { label: $t('edu.withdrawal.status.pending'), value: 'pending' },
    { label: $t('edu.withdrawal.status.approved'), value: 'approved' },
    { label: $t('edu.withdrawal.status.paid'), value: 'paid' },
    { label: $t('edu.withdrawal.status.rejected'), value: 'rejected' },
    { label: $t('edu.withdrawal.status.failed'), value: 'failed' },
  ];
}

/** 状态 → 文案 — function 返回新对象,保证 t() 切换 locale 时实时生效 */
export function getWithdrawalStatusLabel(): Record<string, string> {
  return {
    pending: $t('edu.withdrawal.status.pending'),
    approved: $t('edu.withdrawal.status.approved'),
    paid: $t('edu.withdrawal.status.paid'),
    rejected: $t('edu.withdrawal.status.rejected'),
    failed: $t('edu.withdrawal.status.failed'),
  };
}

/** 状态 → a-tag color(Ant Design Vue 内置语义色,不涉及 i18n) */
export const WITHDRAWAL_STATUS_COLOR: Record<string, string> = {
  pending: 'warning',
  approved: 'processing',
  paid: 'success',
  rejected: 'default',
  failed: 'error',
};

/** 状态 → 文案(zh-CN 常量,保留给下游模块如 teacher-income/BalanceCard 兼容用)
 *  withdrawal 模块自己优先用 getWithdrawalStatusLabel() 走 i18n */
export const WITHDRAWAL_STATUS_LABEL: Record<string, string> = {
  pending: '待审核',
  approved: '已通过待打款',
  paid: '已打款',
  rejected: '已驳回',
  failed: '打款失败',
};

/** 收款方式选项 */
export function getPayeeMethodOptions() {
  return [
    { label: $t('edu.withdrawal.payeeMethod.wechat'), value: 'wechat' },
    { label: $t('edu.withdrawal.payeeMethod.alipay'), value: 'alipay' },
    { label: $t('edu.withdrawal.payeeMethod.paypal'), value: 'paypal' },
    { label: $t('edu.withdrawal.payeeMethod.bankCard'), value: 'bank_card' },
    { label: $t('edu.withdrawal.payeeMethod.other'), value: 'other' },
  ];
}

/** 收款方式 → 文案 — function 返回新对象 */
export function getPayeeMethodLabel(): Record<string, string> {
  return {
    wechat: $t('edu.withdrawal.payeeMethod.wechat'),
    alipay: $t('edu.withdrawal.payeeMethod.alipay'),
    paypal: $t('edu.withdrawal.payeeMethod.paypal'),
    bank_card: $t('edu.withdrawal.payeeMethod.bankCard'),
    other: $t('edu.withdrawal.payeeMethod.other'),
  };
}

/** Tab 配置(5 tab 主列表) */
export function getTabList(): Array<{
  key: string;
  label: string;
  status?: EduWithdrawalApi.WithdrawalStatus;
}> {
  return [
    { key: 'pending', label: $t('edu.withdrawal.tabs.pending'), status: 'pending' },
    { key: 'approved', label: $t('edu.withdrawal.tabs.approved'), status: 'approved' },
    { key: 'paid', label: $t('edu.withdrawal.tabs.paid'), status: 'paid' },
    { key: 'rejected', label: $t('edu.withdrawal.tabs.rejected'), status: 'rejected' },
    { key: 'failed', label: $t('edu.withdrawal.tabs.failed'), status: 'failed' },
  ];
}

/** 搜索表单 schema(教师关键字 / 金额范围 / 申请时间范围) */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'teacherKeyword',
      label: $t('edu.withdrawal.filter.teacherKeyword'),
      component: 'Input',
      componentProps: {
        // placeholder 在 zh-TW key 树未覆盖,保留中文(辅助型文案,Phase E 评估补 key)
        placeholder: '昵称 / 手机号 / 邮箱',
        allowClear: true,
      },
    },
    {
      fieldName: 'teacherId',
      // 教师ID:zh-TW 未覆盖独立 key,沿用 column.teacher
      label: $t('edu.withdrawal.column.teacher'),
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
      fieldName: 'amountMin',
      label: $t('edu.withdrawal.filter.amountMin'),
      component: 'InputNumber',
      componentProps: {
        placeholder: '最小金额',
        allowClear: true,
        min: 0,
        precision: 2,
        step: 1,
        class: 'w-full',
      },
    },
    {
      fieldName: 'amountMax',
      label: $t('edu.withdrawal.filter.amountMax'),
      component: 'InputNumber',
      componentProps: {
        placeholder: '最大金额',
        allowClear: true,
        min: 0,
        precision: 2,
        step: 1,
        class: 'w-full',
      },
    },
    {
      fieldName: 'appliedAtRange',
      label: $t('edu.withdrawal.column.appliedAt'),
      component: 'RangePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DDTHH:mm:ss',
        allowClear: true,
        class: 'w-full',
      },
    },
  ];
}

/** 列定义(ID / 教师 / 金额 / 收款方式 / 状态 / 申请时间 / 操作) */
export function useGridColumns(): VxeTableGridOptions<EduWithdrawalApi.Withdrawal>['columns'] {
  return [
    {
      field: 'id',
      title: $t('edu.withdrawal.column.id'),
      minWidth: 100,
    },
    {
      field: 'teacherId',
      title: $t('edu.withdrawal.column.teacher'),
      minWidth: 100,
    },
    {
      field: 'amount',
      title: $t('edu.withdrawal.column.amount'),
      minWidth: 120,
      slots: { default: 'amount' },
    },
    {
      field: 'payeeMethod',
      title: $t('edu.withdrawal.column.payeeMethod'),
      minWidth: 110,
      slots: { default: 'payeeMethod' },
    },
    {
      field: 'payeeInfoMasked',
      title: $t('edu.withdrawal.column.payeeMasked'),
      minWidth: 200,
    },
    {
      field: 'status',
      title: $t('edu.withdrawal.column.status'),
      minWidth: 120,
      slots: { default: 'status' },
    },
    {
      field: 'appliedAt',
      title: $t('edu.withdrawal.column.appliedAt'),
      minWidth: 180,
      slots: { default: 'appliedAt' },
    },
    {
      title: $t('edu.withdrawal.column.action'),
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
