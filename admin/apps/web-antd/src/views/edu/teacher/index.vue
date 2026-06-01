<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduTeacherProfileApi } from '#/api/edu/teacher-profile';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';

import { message, Tag } from 'ant-design-vue';
import { useRoute } from 'vue-router';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  auditTeacherProfile,
  getTeacherProfilePage,
} from '#/api/edu/teacher-profile';

import {
  ACCENT_LABEL,
  AUDIT_STATUS_COLOR,
  AUDIT_STATUS_LABEL,
  useGridColumns,
  useGridFormSchema,
} from './data';
import Detail from './modules/detail.vue';
import RejectForm from './modules/reject-form.vue';

/** 详情抽屉 */
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: Detail,
  destroyOnClose: true,
});
const route = useRoute();

/** 驳回弹窗 */
const [RejectFormModal, rejectFormModalApi] = useVbenModal({
  connectedComponent: RejectForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 查看详情 */
function handleDetail(row: EduTeacherProfileApi.TeacherProfile) {
  detailDrawerApi.setData({ userId: row.userId }).open();
}

/** 通过审核 */
async function handleApprove(row: EduTeacherProfileApi.TeacherProfile) {
  await confirm('确认通过该教师审核?');
  const hideLoading = message.loading({ content: '提交中...', duration: 0 });
  try {
    await auditTeacherProfile({
      userId: row.userId,
      action: 'approve',
    });
    message.success('已通过');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 打开驳回弹窗 */
function handleOpenReject(row: EduTeacherProfileApi.TeacherProfile) {
  rejectFormModalApi.setData(row).open();
}

function toPositiveNumber(value: unknown) {
  const first = Array.isArray(value) ? value[0] : value;
  const num = Number(first);
  return Number.isFinite(num) && num > 0 ? num : undefined;
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getTeacherProfilePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: toPositiveNumber(route.query.userId),
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'userId',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<EduTeacherProfileApi.TeacherProfile>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer @success="handleRefresh" />
    <RejectFormModal @success="handleRefresh" />

    <Grid table-title="教师审核列表">
      <template #auditStatus="{ row }">
        <Tag :color="(row.auditStatus && AUDIT_STATUS_COLOR[row.auditStatus]) || 'default'">
          {{ (row.auditStatus && AUDIT_STATUS_LABEL[row.auditStatus]) || row.auditStatus || '-' }}
        </Tag>
      </template>

      <template #accent="{ row }">
        {{ (row.accent && ACCENT_LABEL[row.accent]) || row.accent || '-' }}
      </template>

      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'link',
              auth: ['edu:teacher-profile:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '通过',
              type: 'link',
              ifShow: row.auditStatus === 'pending',
              auth: ['edu:teacher-profile:audit'],
              onClick: handleApprove.bind(null, row),
            },
            {
              label: '驳回',
              type: 'link',
              danger: true,
              ifShow: row.auditStatus === 'pending',
              auth: ['edu:teacher-profile:audit'],
              onClick: handleOpenReject.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
