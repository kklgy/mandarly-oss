import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

/** 审核状态选项 */
export const AUDIT_STATUS_OPTIONS = [
  { label: '草稿', value: 'draft' },
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已驳回', value: 'rejected' },
];

/** 审核状态 → a-tag color 映射 */
export const AUDIT_STATUS_COLOR: Record<string, string> = {
  draft: 'default',
  pending: 'orange',
  approved: 'green',
  rejected: 'red',
};

/** 审核状态 → 文案映射 */
export const AUDIT_STATUS_LABEL: Record<string, string> = {
  draft: '草稿',
  pending: '待审核',
  approved: '已通过',
  rejected: '已驳回',
};

/** 口音选项 */
export const ACCENT_OPTIONS = [
  { label: '大陆', value: 'mainland' },
  { label: '台湾', value: 'taiwan' },
  { label: '香港', value: 'hk' },
  { label: '混合', value: 'mixed' },
];

/** 口音 → 文案映射 */
export const ACCENT_LABEL: Record<string, string> = {
  mainland: '大陆',
  taiwan: '台湾',
  hk: '香港',
  mixed: '混合',
};

/** 资质文件类型 → 文案映射(D19 Phase B / B11) */
export const DOC_TYPE_LABEL: Record<string, string> = {
  id_card: '身份证',
  passport: '护照',
  teaching_cert: '教学证书',
  experience_proof: '教学经验证明',
};

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'auditStatus',
      label: '审核状态',
      component: 'Select',
      componentProps: {
        options: AUDIT_STATUS_OPTIONS,
        placeholder: '请选择审核状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'introKeyword',
      label: '自我介绍',
      component: 'Input',
      componentProps: {
        placeholder: '请输入关键字',
        allowClear: true,
      },
    },
    {
      fieldName: 'accent',
      label: '口音',
      component: 'Select',
      componentProps: {
        options: ACCENT_OPTIONS,
        placeholder: '请选择口音',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'userId',
      title: '用户ID',
      minWidth: 100,
    },
    {
      field: 'phone',
      title: '手机号',
      minWidth: 150,
      showOverflow: 'tooltip',
    },
    {
      field: 'intro',
      title: '自我介绍',
      minWidth: 280,
      showOverflow: 'tooltip',
    },
    {
      field: 'auditStatus',
      title: '审核状态',
      minWidth: 110,
      slots: { default: 'auditStatus' },
    },
    {
      field: 'accent',
      title: '口音',
      minWidth: 100,
      slots: { default: 'accent' },
    },
    {
      field: 'yearsExperience',
      title: '教龄(年)',
      minWidth: 100,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
