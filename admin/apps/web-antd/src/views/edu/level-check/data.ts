import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { z } from '#/adapter/form';

export const BOOL_OPTIONS = [
  { label: '是', value: true },
  { label: '否', value: false },
];

export const ACTIVE_OPTIONS = [
  { label: '启用', value: true },
  { label: '停用', value: false },
];

export const LOCALE_OPTIONS = [
  { label: '简体中文', value: 'zh-CN' },
  { label: '繁體中文', value: 'zh-TW' },
  { label: 'English', value: 'en' },
  { label: 'Arabic', value: 'ar' },
];

export const LEVEL_OPTIONS = [
  { label: 'Beginner', value: 'beginner' },
  { label: 'Intermediate', value: 'intermediate' },
  { label: 'Advanced', value: 'advanced' },
];

export const EXPERTISE_OPTIONS = [
  { label: 'Business', value: 'business' },
  { label: 'Daily', value: 'daily' },
  { label: 'HSK', value: 'HSK' },
  { label: 'Kids', value: 'kids' },
];

export function useQuestionFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'questionCode',
      label: '题目编码',
      component: 'Input',
      componentProps: {
        allowClear: true,
        maxlength: 32,
        placeholder: '例如 q1_level',
      },
      rules: 'required',
    },
    {
      fieldName: 'questionI18nCode',
      label: '题干 i18n Code',
      component: 'Input',
      componentProps: {
        allowClear: true,
        maxlength: 64,
        placeholder: '例如 level_check.q1.text',
      },
      rules: 'required',
    },
    {
      fieldName: 'questionType',
      label: '题型',
      component: 'Select',
      componentProps: {
        options: [{ label: '单选', value: 'single_choice' }],
      },
      rules: z.string().default('single_choice'),
    },
    {
      fieldName: 'isActive',
      label: '启用',
      component: 'Switch',
      componentProps: { checkedChildren: '启用', unCheckedChildren: '停用' },
      rules: z.boolean().default(true),
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: { min: 0, precision: 0, class: 'w-full' },
      rules: z.number().default(0),
    },
  ];
}

export function useOptionFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'questionId',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'optionCode',
      label: '选项编码',
      component: 'Input',
      componentProps: {
        allowClear: true,
        maxlength: 32,
        placeholder: '例如 complete_beginner',
      },
      rules: 'required',
    },
    {
      fieldName: 'optionI18nCode',
      label: '选项 i18n Code',
      component: 'Input',
      componentProps: {
        allowClear: true,
        maxlength: 64,
        placeholder: '例如 level_check.q1.opt.complete_beginner',
      },
      rules: 'required',
    },
    {
      fieldName: 'inferredLevel',
      label: '推断等级',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: LEVEL_OPTIONS,
        placeholder: '仅 Q1 通常需要',
      },
    },
    {
      fieldName: 'recommendedWeeklyCount',
      label: '推荐周课次',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        precision: 0,
        class: 'w-full',
        placeholder: '仅 Q3 通常需要',
      },
    },
    {
      fieldName: 'matchExpertise',
      label: '硬约束标签',
      component: 'Select',
      componentProps: {
        allowClear: true,
        mode: 'tags',
        options: EXPERTISE_OPTIONS,
        placeholder: '例如 kids / HSK',
      },
    },
    {
      fieldName: 'scoreRulesText',
      label: '软约束 JSON',
      component: 'Textarea',
      componentProps: {
        rows: 4,
        placeholder: '[{"expertise":"business","score":10}]',
      },
      help: '可空。提交前会校验 JSON 数组。',
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: { min: 0, precision: 0, class: 'w-full' },
      rules: z.number().default(0),
    },
  ];
}

export function useQuestionGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'questionCode',
      label: '题目编码',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '精准查询' },
    },
    {
      fieldName: 'isActive',
      label: '状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: ACTIVE_OPTIONS,
        placeholder: '请选择',
      },
    },
  ];
}

export function useSubmissionGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'locale',
      label: '语言',
      component: 'Select',
      componentProps: { allowClear: true, options: LOCALE_OPTIONS },
    },
    {
      fieldName: 'inferredLevel',
      label: '等级',
      component: 'Select',
      componentProps: { allowClear: true, options: LEVEL_OPTIONS },
    },
    {
      fieldName: 'isConverted',
      label: '已下单',
      component: 'Select',
      componentProps: { allowClear: true, options: BOOL_OPTIONS },
    },
  ];
}

export function useQuestionColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', width: 80 },
    { field: 'questionCode', title: '题目编码', minWidth: 150 },
    { field: 'questionI18nCode', title: '题干 i18n Code', minWidth: 220 },
    { field: 'questionType', title: '题型', minWidth: 120 },
    { field: 'isActive', title: '状态', minWidth: 100, slots: { default: 'active' } },
    { field: 'sort', title: '排序', width: 90 },
    {
      field: 'actions',
      fixed: 'right',
      slots: { default: 'questionActions' },
      title: '操作',
      width: 230,
    },
  ];
}

export function useOptionColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', width: 80 },
    { field: 'optionCode', title: '选项编码', minWidth: 160 },
    { field: 'optionI18nCode', title: '文案 i18n Code', minWidth: 230 },
    { field: 'inferredLevel', title: '推断等级', minWidth: 120, slots: { default: 'level' } },
    { field: 'recommendedWeeklyCount', title: '周课次', minWidth: 100 },
    {
      field: 'matchExpertise',
      title: '硬约束',
      minWidth: 180,
      slots: { default: 'expertise' },
    },
    { field: 'scoreRules', title: '软约束', minWidth: 240, slots: { default: 'scoreRules' } },
    { field: 'sort', title: '排序', width: 90 },
    {
      field: 'actions',
      fixed: 'right',
      slots: { default: 'optionActions' },
      title: '操作',
      width: 170,
    },
  ];
}

export function useSubmissionColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', width: 90 },
    { field: 'createTime', title: '答卷时间', minWidth: 170 },
    { field: 'locale', title: '语言', width: 110 },
    { field: 'market', title: '市场', width: 90 },
    { field: 'email', title: '邮箱', minWidth: 200 },
    { field: 'inferredLevel', title: '等级', minWidth: 130, slots: { default: 'submissionLevel' } },
    {
      field: 'recommendedTeacherIds',
      title: '推荐教师',
      minWidth: 160,
      slots: { default: 'teacherIds' },
    },
    { field: 'recommendedPackageId', title: '套餐 ID', minWidth: 100 },
    { field: 'isConverted', title: '已下单', minWidth: 100, slots: { default: 'converted' } },
    { field: 'answers', title: '答案', minWidth: 260, slots: { default: 'answers' } },
  ];
}
