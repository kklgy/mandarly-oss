import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import type { EduSupportApi } from '#/api/edu/support';

import { z } from '#/adapter/form';

export const FAQ_CATEGORY_OPTIONS = [
  { label: '账号', value: 'account' },
  { label: '套餐', value: 'package' },
  { label: '上课', value: 'class' },
  { label: '退款', value: 'refund' },
  { label: '教师', value: 'teacher' },
  { label: '其他', value: 'other' },
];

export const LOCALE_OPTIONS = [
  { label: '简体中文', value: 'zh-CN' },
  { label: '繁體中文', value: 'zh-TW' },
  { label: 'English', value: 'en' },
  { label: 'Arabic', value: 'ar' },
];

export const FAQ_STATUS_OPTIONS = [
  { label: '启用', value: 'active' },
  { label: '停用', value: 'disabled' },
];

export const CONTACT_CHANNEL_OPTIONS = [
  { label: 'WhatsApp', value: 'whatsapp' },
  { label: '微信', value: 'wechat' },
  { label: '企业微信', value: 'wecom' },
  { label: '邮箱/其他', value: 'other' },
];

export const MARKET_OPTIONS = [
  { label: 'DEFAULT 兜底', value: 'DEFAULT' },
  { label: 'HK 香港', value: 'HK' },
  { label: 'AE 阿联酋', value: 'AE' },
  { label: 'GH 加纳', value: 'GH' },
  { label: 'US 美国', value: 'US' },
  { label: 'CN 大陆', value: 'CN' },
];

export const BOOL_OPTIONS = [
  { label: '是', value: true },
  { label: '否', value: false },
];

export const MATCHED_OPTIONS = [
  { label: '已命中 FAQ', value: true },
  { label: '未命中', value: false },
];

export function useFaqFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'locale',
      label: '语言',
      component: 'Select',
      componentProps: { options: LOCALE_OPTIONS, allowClear: true },
      rules: 'required',
    },
    {
      fieldName: 'category',
      label: '分类',
      component: 'Select',
      componentProps: { options: FAQ_CATEGORY_OPTIONS, allowClear: true },
      rules: 'required',
    },
    {
      fieldName: 'question',
      label: '问题',
      component: 'Input',
      componentProps: { maxlength: 512, allowClear: true },
      rules: 'required',
    },
    {
      fieldName: 'answer',
      label: '答案',
      component: 'Textarea',
      componentProps: {
        rows: 8,
        maxlength: 4000,
        placeholder: '支持简单 Markdown,不要内嵌图片',
      },
      rules: 'required',
    },
    {
      fieldName: 'keywords',
      label: '关键字',
      component: 'Select',
      componentProps: {
        mode: 'tags',
        allowClear: true,
        placeholder: '输入关键字后按 Enter 添加',
        tokenSeparators: [',', '，'],
      },
      help: '可空。用户问题命中任一关键字会提高 FAQ 分数。',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: { options: FAQ_STATUS_OPTIONS },
      rules: z.string().default('active'),
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

export function useContactFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'market',
      label: '市场',
      component: 'Select',
      componentProps: { options: MARKET_OPTIONS, allowClear: true },
      rules: 'required',
    },
    {
      fieldName: 'channelType',
      label: '渠道',
      component: 'Select',
      componentProps: { options: CONTACT_CHANNEL_OPTIONS, allowClear: true },
      rules: 'required',
    },
    {
      fieldName: 'displayText',
      label: '展示文案',
      component: 'Input',
      componentProps: { maxlength: 128, allowClear: true },
      rules: 'required',
    },
    {
      fieldName: 'linkUrl',
      label: '外链',
      component: 'Input',
      componentProps: {
        maxlength: 512,
        allowClear: true,
        placeholder: '例如 https://wa.me/... 或 mailto:hello.com',
      },
    },
    {
      fieldName: 'imageUrl',
      label: '二维码图片',
      component: 'Input',
      componentProps: { maxlength: 512, allowClear: true },
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

export function useFaqGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'question',
      label: '问题',
      component: 'Input',
      componentProps: { placeholder: '支持模糊匹配', allowClear: true },
    },
    {
      fieldName: 'locale',
      label: '语言',
      component: 'Select',
      componentProps: { options: LOCALE_OPTIONS, allowClear: true },
    },
    {
      fieldName: 'category',
      label: '分类',
      component: 'Select',
      componentProps: { options: FAQ_CATEGORY_OPTIONS, allowClear: true },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: { options: FAQ_STATUS_OPTIONS, allowClear: true },
    },
  ];
}

export function useContactGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'market',
      label: '市场',
      component: 'Select',
      componentProps: { options: MARKET_OPTIONS, allowClear: true },
    },
    {
      fieldName: 'channelType',
      label: '渠道',
      component: 'Select',
      componentProps: { options: CONTACT_CHANNEL_OPTIONS, allowClear: true },
    },
    {
      fieldName: 'displayText',
      label: '展示文案',
      component: 'Input',
      componentProps: { placeholder: '支持模糊匹配', allowClear: true },
    },
    {
      fieldName: 'isActive',
      label: '启用',
      component: 'Select',
      componentProps: { options: BOOL_OPTIONS, allowClear: true },
    },
  ];
}

export function useInquiryGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'questionKeyword',
      label: '问题',
      component: 'Input',
      componentProps: { placeholder: '支持模糊匹配', allowClear: true },
    },
    {
      fieldName: 'matched',
      label: '命中状态',
      component: 'Select',
      componentProps: { options: MATCHED_OPTIONS, allowClear: true },
    },
    {
      fieldName: 'clickedToHuman',
      label: '转人工',
      component: 'Select',
      componentProps: { options: BOOL_OPTIONS, allowClear: true },
    },
    {
      fieldName: 'locale',
      label: '语言',
      component: 'Select',
      componentProps: { options: LOCALE_OPTIONS, allowClear: true },
    },
    {
      fieldName: 'market',
      label: '市场',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'userId',
      label: '用户 ID',
      component: 'InputNumber',
      componentProps: { min: 1, controls: false, class: 'w-full' },
    },
  ];
}

export function useFaqColumns(): VxeTableGridOptions<EduSupportApi.Faq>['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'locale', title: '语言', minWidth: 100 },
    { field: 'category', title: '分类', minWidth: 100 },
    { field: 'question', title: '问题', minWidth: 260 },
    { field: 'keywords', title: '关键字', minWidth: 220, slots: { default: 'keywords' } },
    { field: 'status', title: '状态', minWidth: 100, slots: { default: 'faqStatus' } },
    { field: 'matchCount', title: '命中次数', minWidth: 100, align: 'right' },
    { field: 'sort', title: '排序', minWidth: 80, align: 'center' },
    { field: 'updateTime', title: '更新时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 170, fixed: 'right', slots: { default: 'faqActions' } },
  ];
}

export function useContactColumns(): VxeTableGridOptions<EduSupportApi.Contact>['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'market', title: '市场', minWidth: 120 },
    { field: 'channelType', title: '渠道', minWidth: 120 },
    { field: 'displayText', title: '展示文案', minWidth: 180 },
    { field: 'linkUrl', title: '外链', minWidth: 260 },
    { field: 'imageUrl', title: '二维码', minWidth: 220 },
    { field: 'isActive', title: '状态', minWidth: 100, slots: { default: 'contactStatus' } },
    { field: 'sort', title: '排序', minWidth: 80, align: 'center' },
    { field: 'updateTime', title: '更新时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 170, fixed: 'right', slots: { default: 'contactActions' } },
  ];
}

export function useInquiryColumns(): VxeTableGridOptions<EduSupportApi.Inquiry>['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    {
      field: 'questionText',
      title: '用户问题',
      minWidth: 300,
      formatter: ({ cellValue }) => cellValue || '(直接点击客服联系方式)',
    },
    { field: 'matchedFaqId', title: '命中', minWidth: 120, slots: { default: 'matchedFaq' } },
    { field: 'score', title: '分数', minWidth: 90, align: 'right' },
    { field: 'clickedToHuman', title: '转人工', minWidth: 100, slots: { default: 'clickedToHuman' } },
    { field: 'locale', title: '语言', minWidth: 100 },
    { field: 'market', title: '市场', minWidth: 100 },
    { field: 'userId', title: '用户 ID', minWidth: 100 },
    { field: 'sessionId', title: 'Session', minWidth: 190 },
    { field: 'ip', title: 'IP', minWidth: 140 },
    { field: 'createTime', title: '提问时间', minWidth: 180, formatter: 'formatDateTime' },
  ];
}
