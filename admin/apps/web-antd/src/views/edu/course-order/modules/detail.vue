<script lang="ts" setup>
import type { EduCourseOrderApi } from '#/api/edu/course-order';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import {
  Button,
  Descriptions,
  DescriptionsItem,
  Tag,
} from 'ant-design-vue';

import { getCourseOrder } from '#/api/edu/course-order';

import {
  ABNORMAL_REASON_LABEL,
  CANCELLED_BY_LABEL,
  COURSE_ORDER_STATUS_COLOR,
  COURSE_ORDER_STATUS_LABEL,
  TEACHER_SETTLE_STATUS_COLOR,
  TEACHER_SETTLE_STATUS_LABEL,
} from '../data';

const order = ref<EduCourseOrderApi.CourseOrder>();
const loading = ref(false);

const statusText = computed(() => {
  const s = order.value?.status;
  return s ? COURSE_ORDER_STATUS_LABEL[s] || s : '-';
});

const statusColor = computed(() => {
  const s = order.value?.status;
  return s ? COURSE_ORDER_STATUS_COLOR[s] || 'default' : 'default';
});

const cancelledByText = computed(() => {
  const c = order.value?.cancelledBy;
  return c ? CANCELLED_BY_LABEL[c] || c : '-';
});

const settleStatusText = computed(() => {
  const s = order.value?.teacherSettleStatus;
  return s ? TEACHER_SETTLE_STATUS_LABEL[s] || s : '-';
});

const settleStatusColor = computed(() => {
  const s = order.value?.teacherSettleStatus;
  return s ? TEACHER_SETTLE_STATUS_COLOR[s] || 'default' : 'default';
});

const priceText = computed(() => {
  if (!order.value) return '-';
  const price = order.value.priceDisplay ?? '';
  const ccy = order.value.currency || '';
  return `${price} ${ccy}`.trim();
});

const teacherAmountText = computed(() => {
  if (!order.value || order.value.teacherAmount == null) return '-';
  const amt = order.value.teacherAmount;
  const ccy = order.value.currency || '';
  return `${amt} ${ccy}`.trim();
});

const [Drawer, drawerApi] = useVbenDrawer({
  title: '课程订单详情',
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      order.value = undefined;
      return;
    }
    const data = drawerApi.getData<{ id: number }>();
    if (!data?.id) {
      return;
    }
    loading.value = true;
    try {
      order.value = await getCourseOrder(data.id);
    } finally {
      loading.value = false;
    }
  },
});
</script>

<template>
  <Drawer class="w-2/5">
    <div v-if="loading" class="p-4 text-center text-gray-400">加载中...</div>

    <div v-else-if="order" class="space-y-4">
      <!-- 基础信息 -->
      <div>
        <div class="mb-2 font-medium">基础信息</div>
        <Descriptions :column="2" bordered size="small">
          <DescriptionsItem label="订单ID">
            {{ order.id }}
          </DescriptionsItem>
          <DescriptionsItem label="时长(分钟)">
            {{ order.duration ?? '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="学生ID">
            {{ order.studentId }}
          </DescriptionsItem>
          <DescriptionsItem label="教师ID">
            {{ order.teacherId }}
          </DescriptionsItem>
          <DescriptionsItem label="上课时间" :span="2">
            <div>
              {{ order.scheduledAt ? formatDateTime(order.scheduledAt) : '-' }}
            </div>
            <div v-if="order.scheduledAt" class="text-xs text-gray-400">
              {{ order.scheduledAt }} (UTC)
            </div>
          </DescriptionsItem>
          <DescriptionsItem label="创建时间" :span="2">
            <div>
              {{ order.createTime ? formatDateTime(order.createTime) : '-' }}
            </div>
            <div v-if="order.createTime" class="text-xs text-gray-400">
              {{ order.createTime }} (UTC)
            </div>
          </DescriptionsItem>
        </Descriptions>
      </div>

      <!-- 价格信息 -->
      <div>
        <div class="mb-2 font-medium">价格信息</div>
        <Descriptions :column="2" bordered size="small">
          <DescriptionsItem label="订单金额">
            {{ priceText }}
          </DescriptionsItem>
          <DescriptionsItem label="币种">
            {{ order.currency || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="学生套餐ID">
            {{ order.studentPackageId ?? '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="免费体验">
            <Tag v-if="order.isFreeTrial" color="gold">是</Tag>
            <span v-else class="text-gray-400">否</span>
          </DescriptionsItem>
          <DescriptionsItem label="是否已退课">
            <Tag v-if="order.isRefundedClass" color="orange">是</Tag>
            <span v-else class="text-gray-400">否</span>
          </DescriptionsItem>
        </Descriptions>
      </div>

      <!-- 状态信息 -->
      <div>
        <div class="mb-2 font-medium">状态信息</div>
        <Descriptions :column="2" bordered size="small">
          <DescriptionsItem label="订单状态">
            <Tag :color="statusColor">{{ statusText }}</Tag>
          </DescriptionsItem>
          <DescriptionsItem
            v-if="order.cancelledBy || order.cancelledAt || order.cancelReason"
            label="取消方"
          >
            {{ cancelledByText }}
          </DescriptionsItem>
          <DescriptionsItem v-if="order.cancelledAt" label="取消时间" :span="2">
            <div>{{ formatDateTime(order.cancelledAt) }}</div>
            <div class="text-xs text-gray-400">
              {{ order.cancelledAt }} (UTC)
            </div>
          </DescriptionsItem>
          <DescriptionsItem
            v-if="order.cancelReason"
            label="取消原因"
            :span="2"
          >
            <div class="whitespace-pre-wrap">{{ order.cancelReason }}</div>
          </DescriptionsItem>
          <DescriptionsItem v-if="order.finishedAt" label="完课时间" :span="2">
            <div>{{ formatDateTime(order.finishedAt) }}</div>
            <div class="text-xs text-gray-400">{{ order.finishedAt }} (UTC)</div>
          </DescriptionsItem>
        </Descriptions>
      </div>

      <!-- 异常处置(仅 status=abnormal 时显示) -->
      <div v-if="order.status === 'abnormal'">
        <div class="mb-2 font-medium">异常处置</div>
        <Descriptions :column="2" bordered size="small">
          <DescriptionsItem
            v-if="order.abnormalReason"
            label="异常原因(自动诊断)"
            :span="2"
          >
            <Tag color="red">
              {{
                ABNORMAL_REASON_LABEL[order.abnormalReason] ||
                order.abnormalReason
              }}
            </Tag>
            <span class="ml-2 text-xs text-gray-400">
              {{ order.abnormalReason }}
            </span>
          </DescriptionsItem>
          <DescriptionsItem
            v-if="order.abnormalResolution"
            label="处置结论(客服)"
            :span="2"
          >
            <div class="whitespace-pre-wrap">{{ order.abnormalResolution }}</div>
          </DescriptionsItem>
          <DescriptionsItem
            v-if="order.abnormalProcessedBy"
            label="处置人 ID"
          >
            {{ order.abnormalProcessedBy }}
          </DescriptionsItem>
          <DescriptionsItem
            v-if="order.abnormalProcessedAt"
            label="处置时间"
          >
            <div>{{ formatDateTime(order.abnormalProcessedAt) }}</div>
            <div class="text-xs text-gray-400">
              {{ order.abnormalProcessedAt }} (UTC)
            </div>
          </DescriptionsItem>
        </Descriptions>
      </div>

      <!-- 教师结算 -->
      <div>
        <div class="mb-2 font-medium">教师结算</div>
        <Descriptions :column="2" bordered size="small">
          <DescriptionsItem label="教师收入">
            {{ teacherAmountText }}
          </DescriptionsItem>
          <DescriptionsItem label="结算状态">
            <Tag
              v-if="order.teacherSettleStatus"
              :color="settleStatusColor"
            >
              {{ settleStatusText }}
            </Tag>
            <span v-else class="text-gray-400">-</span>
          </DescriptionsItem>
        </Descriptions>
      </div>
    </div>

    <div v-else class="p-4 text-center text-gray-400">暂无数据</div>

    <template #footer>
      <Button @click="drawerApi.close()">关闭</Button>
    </template>
  </Drawer>
</template>
