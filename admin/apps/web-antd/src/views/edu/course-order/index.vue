<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduCourseOrderApi } from '#/api/edu/course-order';

import { Page, useVbenDrawer } from '@vben/common-ui';

import dayjs from 'dayjs';
import { useRoute } from 'vue-router';

import { Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getCourseOrderPage } from '#/api/edu/course-order';

import {
  ABNORMAL_REASON_LABEL,
  CANCELLED_BY_LABEL,
  COURSE_ORDER_STATUS_COLOR,
  COURSE_ORDER_STATUS_LABEL,
  useGridColumns,
  useGridFormSchema,
} from './data';
import Detail from './modules/detail.vue';

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: Detail,
  destroyOnClose: true,
});
const route = useRoute();

/** 把 RangePicker 的 [start, end] 本地时间字符串转 UTC ISO */
function toUtcISO(local?: string) {
  if (!local) return undefined;
  const d = dayjs(local);
  return d.isValid() ? d.toISOString() : undefined;
}

function toPositiveNumber(value: unknown) {
  const first = Array.isArray(value) ? value[0] : value;
  const num = Number(first);
  return Number.isFinite(num) && num > 0 ? num : undefined;
}

/** 打开详情抽屉 */
function handleDetail(row: EduCourseOrderApi.CourseOrder) {
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
          const { scheduledRange, ...rest } = formValues || {};
          const [from, to] = Array.isArray(scheduledRange)
            ? scheduledRange
            : [];
          return await getCourseOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...rest,
            studentId:
              rest.studentId ?? toPositiveNumber(route.query.studentId),
            teacherId:
              rest.teacherId ?? toPositiveNumber(route.query.teacherId),
            scheduledFrom: toUtcISO(from),
            scheduledTo: toUtcISO(to),
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
  } as VxeTableGridOptions<EduCourseOrderApi.CourseOrder>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="课程订单列表">
      <template #priceDisplay="{ row }">
        <span v-if="row.priceDisplay != null">
          {{ row.priceDisplay }} {{ row.currency || '' }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>
      <template #status="{ row }">
        <Tag :color="COURSE_ORDER_STATUS_COLOR[row.status] || 'default'">
          {{ COURSE_ORDER_STATUS_LABEL[row.status] || row.status }}
        </Tag>
      </template>
      <template #abnormalReason="{ row }">
        <span
          v-if="row.status === 'abnormal' && row.abnormalReason"
          class="text-red-500"
        >
          {{ ABNORMAL_REASON_LABEL[row.abnormalReason] || row.abnormalReason }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>
      <template #isFreeTrial="{ row }">
        <Tag v-if="row.isFreeTrial" color="gold">免费体验</Tag>
        <span v-else class="text-gray-400">-</span>
      </template>
      <template #cancelledBy="{ row }">
        <span v-if="row.cancelledBy">
          {{ CANCELLED_BY_LABEL[row.cancelledBy] || row.cancelledBy }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['edu:course-order:query'],
              onClick: handleDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
