import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { z } from '#/adapter/form';

/** 货币选项(一期主用 HKD,后续按需扩展) */
const CURRENCY_OPTIONS = [
  { label: 'HKD 港币', value: 'HKD' },
  { label: 'CNY 人民币', value: 'CNY' },
  { label: 'USD 美元', value: 'USD' },
];

/** 是/否选项(用于 isFreeTrial / isActive 搜索) */
const BOOL_OPTIONS = [
  { label: '是', value: true },
  { label: '否', value: false },
];

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'nameI18nCode',
      label: '名称 i18n Code',
      component: 'Input',
      componentProps: {
        placeholder: '例如:package.standard',
        maxlength: 64,
        allowClear: true,
      },
      help: '这是 i18n_message 表的 code,例如 package.standard',
      rules: 'required',
    },
    {
      fieldName: 'totalCount',
      label: '总课时数',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        precision: 0,
        placeholder: '请输入总课时数',
        class: 'w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'weeklyCount',
      label: '每周课时数',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        precision: 0,
        placeholder: '免费体验课可不填',
        class: 'w-full',
      },
    },
    {
      fieldName: 'validityDays',
      label: '有效期(天)',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        precision: 0,
        placeholder: '请输入套餐有效天数',
        class: 'w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'price',
      label: '价格',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        step: 0.01,
        precision: 2,
        placeholder: '免费体验课请填 0',
        class: 'w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'currency',
      label: '货币',
      component: 'Select',
      componentProps: {
        options: CURRENCY_OPTIONS,
        placeholder: '请选择货币',
        allowClear: true,
      },
      rules: z.string().default('HKD'),
    },
    {
      fieldName: 'isFreeTrial',
      label: '免费体验',
      component: 'Switch',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
      },
      rules: z.boolean().default(false),
    },
    {
      fieldName: 'isActive',
      label: '是否启用',
      component: 'Switch',
      componentProps: {
        checkedChildren: '启用',
        unCheckedChildren: '停用',
      },
      rules: z.boolean().default(true),
    },
    {
      fieldName: 'sort',
      label: '显示顺序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 0,
        placeholder: '数字越小越靠前',
        class: 'w-full',
      },
      rules: z.number().default(0),
    },
    {
      fieldName: 'descriptionI18nCode',
      label: '描述 i18n Code',
      component: 'Input',
      componentProps: {
        placeholder: '例如:package.standard.desc',
        maxlength: 64,
        allowClear: true,
      },
      help: '这是 i18n_message 表的 code,例如 package.standard.desc',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'nameI18nCode',
      label: '名称 Code',
      component: 'Input',
      componentProps: {
        placeholder: '支持模糊匹配',
        allowClear: true,
      },
    },
    {
      fieldName: 'currency',
      label: '货币',
      component: 'Select',
      componentProps: {
        options: CURRENCY_OPTIONS,
        placeholder: '请选择货币',
        allowClear: true,
      },
    },
    {
      fieldName: 'isFreeTrial',
      label: '免费体验',
      component: 'Select',
      componentProps: {
        options: BOOL_OPTIONS,
        placeholder: '请选择',
        allowClear: true,
      },
    },
    {
      fieldName: 'isActive',
      label: '是否启用',
      component: 'Select',
      componentProps: {
        options: BOOL_OPTIONS,
        placeholder: '请选择',
        allowClear: true,
      },
    },
  ];
}

/** 千分符 + 两位小数,带货币前缀 */
function formatPrice(price: number | string | null | undefined, currency?: string) {
  if (price === null || price === undefined || price === '') {
    return '-';
  }
  const num = typeof price === 'number' ? price : Number.parseFloat(String(price));
  if (Number.isNaN(num)) {
    return String(price);
  }
  const formatted = num.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  return currency ? `${currency} ${formatted}` : formatted;
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 80,
    },
    {
      field: 'nameI18nCode',
      title: '名称 Code',
      minWidth: 200,
    },
    {
      field: 'totalCount',
      title: '总课时',
      minWidth: 90,
      align: 'center',
    },
    {
      field: 'weeklyCount',
      title: '每周课时',
      minWidth: 90,
      align: 'center',
      formatter: ({ cellValue }) =>
        cellValue === null || cellValue === undefined ? '-' : String(cellValue),
    },
    {
      field: 'validityDays',
      title: '有效期(天)',
      minWidth: 110,
      align: 'center',
    },
    {
      field: 'price',
      title: '价格',
      minWidth: 140,
      align: 'right',
      formatter: ({ row }) => formatPrice(row.price, row.currency),
    },
    {
      field: 'isFreeTrial',
      title: '免费体验',
      minWidth: 100,
      align: 'center',
      slots: { default: 'isFreeTrial' },
    },
    {
      field: 'isActive',
      title: '是否启用',
      minWidth: 100,
      align: 'center',
      slots: { default: 'isActive' },
    },
    {
      field: 'sort',
      title: '排序',
      minWidth: 80,
      align: 'center',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
