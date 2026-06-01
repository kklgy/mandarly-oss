<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduRefundApi } from '#/api/edu/refund';

import { Page, useVbenModal } from '@vben/common-ui';

import { Tag } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getRefundPage } from '#/api/edu/refund';

import {
  REFUND_STATUS_COLOR,
  REFUND_STATUS_LABEL,
  useGridColumns,
  useGridFormSchema,
} from './data';
import RefundAuditModal from './modules/refund-audit-modal.vue';

/** 审核 modal */
const [AuditModal, auditModalApi] = useVbenModal({
  connectedComponent: RefundAuditModal,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 批准退款 */
function handleApprove(row: EduRefundApi.Refund) {
  auditModalApi.setData({ refund: row, mode: 'approve' }).open();
}

/** 拒绝退款 */
function handleReject(row: EduRefundApi.Refund) {
  auditModalApi.setData({ refund: row, mode: 'reject' }).open();
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
          return await getRefundPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<EduRefundApi.Refund>,
});
</script>

<template>
  <Page auto-content-height>
    <AuditModal @success="handleRefresh" />

    <Grid table-title="退款工单列表">
      <!-- 建议金额 -->
      <template #suggestedAmountUsd="{ row }">
        <span v-if="row.suggestedAmountUsd != null">
          ${{ Number(row.suggestedAmountUsd).toFixed(2) }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 最终金额 -->
      <template #finalAmountUsd="{ row }">
        <span v-if="row.finalAmountUsd != null" class="font-semibold">
          ${{ Number(row.finalAmountUsd).toFixed(2) }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 状态 badge -->
      <template #status="{ row }">
        <Tag :color="REFUND_STATUS_COLOR[row.status] || 'default'">
          {{ REFUND_STATUS_LABEL[row.status] || row.status }}
        </Tag>
      </template>

      <!-- 操作 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '批准',
              type: 'link',
              ifShow: row.status === 'pending',
              auth: ['edu:refund:approve'],
              onClick: handleApprove.bind(null, row),
            },
            {
              label: '拒绝',
              type: 'link',
              danger: true,
              ifShow: row.status === 'pending',
              auth: ['edu:refund:approve'],
              onClick: handleReject.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
