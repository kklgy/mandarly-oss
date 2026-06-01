<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduTeacherIncomeApi } from '#/api/edu/teacher-income';

import { Page, useVbenModal } from '@vben/common-ui';

import { Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTeacherIncomePage } from '#/api/edu/teacher-income';

import {
  INCOME_TYPE_COLOR,
  INCOME_TYPE_LABEL,
  useGridColumns,
  useGridFormSchema,
} from './data';
import BalanceCard from './BalanceCard.vue';

/** 余额看板 modal */
const [BalanceModal, balanceModalApi] = useVbenModal({
  connectedComponent: BalanceCard,
  destroyOnClose: true,
});

/** 打开余额看板 */
function handleViewBalance(row: EduTeacherIncomeApi.TeacherIncome) {
  balanceModalApi.setData({ teacherId: row.teacherId }).open();
}

const [Grid] = useVbenVxeGrid({
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
          return await getTeacherIncomePage({
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
  } as VxeTableGridOptions<EduTeacherIncomeApi.TeacherIncome>,
});
</script>

<template>
  <Page auto-content-height>
    <BalanceModal />

    <Grid table-title="教师收入流水">
      <!-- 金额:正绿负红 -->
      <template #amountUsd="{ row }">
        <span
          :class="Number(row.amountUsd) >= 0 ? 'text-green-600' : 'text-red-500'"
          class="font-semibold"
        >
          {{ Number(row.amountUsd) >= 0 ? '+' : '' }}${{
            Math.abs(Number(row.amountUsd)).toFixed(2)
          }}
        </span>
      </template>

      <!-- 收入类型 badge -->
      <template #type="{ row }">
        <Tag :color="INCOME_TYPE_COLOR[row.type] || 'default'">
          {{ INCOME_TYPE_LABEL[row.type] || row.type }}
        </Tag>
      </template>

      <!-- 结算时间:UTC + 本地双行 -->
      <template #settledAt="{ row }">
        <div v-if="row.settledAt">
          <div>{{ new Date(row.settledAt).toLocaleString() }}</div>
          <div class="text-xs text-gray-400">{{ row.settledAt }} (UTC)</div>
        </div>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 操作 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '余额',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['edu:teacher-income:query'],
              onClick: handleViewBalance.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
