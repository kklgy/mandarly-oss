<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduPaymentApi } from '#/api/edu/payment';

import { Page, useVbenDrawer } from '@vben/common-ui';

import { Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getPaymentPage } from '#/api/edu/payment';

import {
  formatCurrencyAmount,
  formatUsdAmount,
  PAYMENT_STATUS_COLOR,
  PAYMENT_STATUS_LABEL,
  useGridColumns,
  useGridFormSchema,
} from './data';
import Detail from './modules/detail.vue';

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 打开详情抽屉 */
function handleDetail(row: EduPaymentApi.Payment) {
  detailDrawerApi.setData({ id: row.id }).open();
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
          return await getPaymentPage({
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
  } as VxeTableGridOptions<EduPaymentApi.Payment>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="支付订单列表">
      <!-- 请求金额 -->
      <template #amountRequest="{ row }">
        <span v-if="row.amountRequest != null">
          {{ formatCurrencyAmount(row.amountRequest, row.currencyRequest) }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 实付金额(原币种) -->
      <template #amountPaid="{ row }">
        <span v-if="row.amountPaid != null">
          {{ formatCurrencyAmount(row.amountPaid, row.currencyPaid) }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 结算金额 USD -->
      <template #amountSettledUsd="{ row }">
        <span v-if="row.amountSettledUsd != null">
          {{ formatUsdAmount(row.amountSettledUsd) }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 折扣 -->
      <template #discountAmountUsd="{ row }">
        <span
          v-if="row.discountAmountUsd != null && Number(row.discountAmountUsd) > 0"
          class="text-green-600"
        >
          -{{ formatUsdAmount(row.discountAmountUsd) }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 状态 badge -->
      <template #status="{ row }">
        <Tag :color="PAYMENT_STATUS_COLOR[row.status] || 'default'">
          {{ PAYMENT_STATUS_LABEL[row.status] || row.status }}
        </Tag>
      </template>

      <!-- 支付时间:UTC + 本地双行 -->
      <template #paidAt="{ row }">
        <div v-if="row.paidAt">
          <div>{{ new Date(row.paidAt).toLocaleString() }}</div>
          <div class="text-xs text-gray-400">{{ row.paidAt }} (UTC)</div>
        </div>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 操作 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['edu:payment:query'],
              onClick: handleDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
