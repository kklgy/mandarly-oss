import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduUserApi } from '#/api/edu/user';

export const ROLE_OPTIONS = [
  { label: '学生', value: 'student' },
  { label: '教师', value: 'teacher' },
];

export const ROLE_LABEL: Record<string, string> = {
  student: '学生',
  teacher: '教师',
};

export const ROLE_COLOR: Record<string, string> = {
  student: 'blue',
  teacher: 'purple',
};

export const STATUS_OPTIONS = [
  { label: '活跃', value: 'active' },
  { label: '待验证', value: 'pending_verification' },
  { label: '已冻结', value: 'frozen' },
];

export const STATUS_LABEL: Record<string, string> = {
  active: '活跃',
  pending_verification: '待验证',
  frozen: '已冻结',
};

export const STATUS_COLOR: Record<string, string> = {
  active: 'green',
  pending_verification: 'orange',
  frozen: 'red',
};

export const LOCALE_OPTIONS = [
  { label: 'English', value: 'en' },
  { label: '简体中文', value: 'zh-CN' },
  { label: '繁體中文', value: 'zh-TW' },
  { label: 'العربية', value: 'ar' },
];

export const AUDIT_STATUS_LABEL: Record<string, string> = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已驳回',
};

export const AUDIT_STATUS_COLOR: Record<string, string> = {
  pending: 'orange',
  approved: 'green',
  rejected: 'red',
};

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'role',
      label: '角色',
      component: 'Select',
      componentProps: {
        options: ROLE_OPTIONS,
        placeholder: '全部角色',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: STATUS_OPTIONS,
        placeholder: '全部状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'keyword',
      label: '关键词',
      component: 'Input',
      componentProps: {
        placeholder: '昵称 / 邮箱 / 手机 / 推荐码',
        allowClear: true,
      },
    },
    {
      fieldName: 'locale',
      label: '语言',
      component: 'Select',
      componentProps: {
        options: LOCALE_OPTIONS,
        placeholder: '全部语言',
        allowClear: true,
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<EduUserApi.User>['columns'] {
  return [
    {
      field: 'id',
      title: '用户ID',
      minWidth: 100,
    },
    {
      field: 'role',
      title: '角色',
      minWidth: 90,
      slots: { default: 'role' },
    },
    {
      field: 'nickname',
      title: '昵称',
      minWidth: 150,
      showOverflow: 'tooltip',
    },
    {
      field: 'email',
      title: '邮箱',
      minWidth: 220,
      showOverflow: 'tooltip',
    },
    {
      field: 'phone',
      title: '手机号',
      minWidth: 150,
      showOverflow: 'tooltip',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      slots: { default: 'status' },
    },
    {
      field: 'oauthSummary',
      title: 'OAuth 绑定',
      minWidth: 150,
      slots: { default: 'oauthSummary' },
    },
    {
      field: 'teacherAuditStatus',
      title: '教师档案',
      minWidth: 120,
      slots: { default: 'teacherAuditStatus' },
    },
    {
      field: 'createTime',
      title: '注册时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'lastLoginAt',
      title: '最后登录',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
