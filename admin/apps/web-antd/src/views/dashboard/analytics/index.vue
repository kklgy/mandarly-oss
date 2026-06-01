<script lang="ts" setup>
import type { EduReportApi } from '#/api/edu/report';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Table, Tag } from 'ant-design-vue';

import { getEduReportOverview } from '#/api/edu/report';

const loading = ref(false);
const overview = ref<EduReportApi.Overview>();

const periodColumns = [
  { title: '区间', dataIndex: 'label', key: 'label', width: 120 },
  { title: '新增学员', dataIndex: 'newStudents', key: 'newStudents' },
  { title: '新增教师', dataIndex: 'newTeachers', key: 'newTeachers' },
  { title: '课程订单', dataIndex: 'courseOrders', key: 'courseOrders' },
  { title: '成功支付', dataIndex: 'paidPaymentOrders', key: 'paidPaymentOrders' },
  { title: '结算金额(USD)', dataIndex: 'paidAmountUsd', key: 'paidAmountUsd' },
  { title: '已退款', dataIndex: 'refundedOrders', key: 'refundedOrders' },
  { title: '退款金额(USD)', dataIndex: 'refundedAmountUsd', key: 'refundedAmountUsd' },
];

function formatInteger(val?: null | number | string) {
  const num = Number(val ?? 0);
  return Number.isFinite(num) ? num.toLocaleString() : '0';
}

function formatUsd(val?: null | number | string) {
  const num = Number(val ?? 0);
  return Number.isFinite(num) ? `$${num.toFixed(2)}` : '$0.00';
}

function formatUtcLocal(utcStr?: string | null) {
  if (!utcStr) return '-';
  return new Date(utcStr).toLocaleString();
}

const metricCards = computed(() => {
  const data = overview.value;
  return [
    {
      description: 'C 端 student 账号',
      label: '注册学员',
      value: formatInteger(data?.totalStudents),
    },
    {
      description: `已通过 ${formatInteger(data?.approvedTeachers)} / 待审核 ${formatInteger(data?.pendingTeacherAudits)}`,
      label: '入驻教师',
      value: formatInteger(data?.totalTeachers),
    },
    {
      description: `待上课 ${formatInteger(data?.upcomingCourseOrders)} / 异常 ${formatInteger(data?.abnormalCourseOrders)}`,
      label: '课程订单',
      value: formatInteger(data?.totalCourseOrders),
    },
    {
      description: `成功订单 ${formatInteger(data?.paidPaymentOrders)}`,
      label: '支付结算',
      value: formatUsd(data?.paidAmountUsd),
    },
    {
      description: `已退款 ${formatUsd(data?.refundedAmountUsd)}`,
      label: '待处理退款',
      value: formatInteger(data?.pendingRefunds),
    },
  ];
});

async function loadOverview() {
  loading.value = true;
  try {
    overview.value = await getEduReportOverview();
  } finally {
    loading.value = false;
  }
}

onMounted(loadOverview);
</script>

<template>
  <Page auto-content-height>
    <div class="space-y-5 p-5">
      <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <div
          v-for="card in metricCards"
          :key="card.label"
          class="rounded-lg border border-border bg-card p-5 shadow-sm"
        >
          <div class="text-sm font-medium text-muted-foreground">
            {{ card.label }}
          </div>
          <div class="mt-4 text-3xl font-semibold text-foreground">
            {{ loading ? '...' : card.value }}
          </div>
          <div class="mt-3 text-xs text-muted-foreground">
            {{ card.description }}
          </div>
        </div>
      </section>

      <section class="rounded-lg border border-border bg-card p-5 shadow-sm">
        <div class="mb-4 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 class="text-lg font-semibold text-foreground">运营分析</h1>
            <div class="mt-1 text-xs text-muted-foreground">
              更新时间 {{ formatUtcLocal(overview?.lastUpdatedAt) }}
            </div>
          </div>
          <Tag color="blue">UTC 统计口径</Tag>
        </div>

        <Table
          :columns="periodColumns"
          :data-source="overview?.periods || []"
          :loading="loading"
          :pagination="false"
          row-key="label"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <span v-if="column.key === 'paidAmountUsd'">
              {{ formatUsd(record.paidAmountUsd) }}
            </span>
            <span v-else-if="column.key === 'refundedAmountUsd'">
              {{ formatUsd(record.refundedAmountUsd) }}
            </span>
            <span v-else>
              {{ record[column.dataIndex] }}
            </span>
          </template>
        </Table>
      </section>
    </div>
  </Page>
</template>
