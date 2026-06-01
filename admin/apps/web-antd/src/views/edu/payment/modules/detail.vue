<script lang="ts" setup>
import type { EduPaymentApi } from '#/api/edu/payment';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import {
  Button,
  Descriptions,
  DescriptionsItem,
  Tag,
} from 'ant-design-vue';

import { getPayment } from '#/api/edu/payment';

import {
  formatCurrencyAmount,
  formatUsdAmount,
  PAYMENT_STATUS_COLOR,
  PAYMENT_STATUS_LABEL,
} from '../data';

const payment = ref<EduPaymentApi.Payment>();
const loading = ref(false);

const statusText = computed(() => {
  const s = payment.value?.status;
  return s ? PAYMENT_STATUS_LABEL[s] || s : '-';
});

const statusColor = computed(() => {
  const s = payment.value?.status;
  return s ? PAYMENT_STATUS_COLOR[s] || 'default' : 'default';
});

function formatUtcLocal(utcStr?: string | null) {
  if (!utcStr) return null;
  return {
    local: new Date(utcStr).toLocaleString(),
    utc: utcStr,
  };
}

const [Drawer, drawerApi] = useVbenDrawer({
  title: '支付订单详情',
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      payment.value = undefined;
      return;
    }
    const data = drawerApi.getData<{ id: number }>();
    if (!data?.id) return;
    loading.value = true;
    try {
      payment.value = await getPayment(data.id);
    } finally {
      loading.value = false;
    }
  },
});
</script>

<template>
  <Drawer class="w-2/5">
    <div v-if="loading" class="p-4 text-center text-gray-400">加载中...</div>

    <div v-else-if="payment" class="space-y-4">
      <!-- 基础信息 -->
      <div>
        <div class="mb-2 font-medium">基础信息</div>
        <Descriptions :column="2" bordered size="small">
          <DescriptionsItem label="支付单ID">{{ payment.id }}</DescriptionsItem>
          <DescriptionsItem label="学生ID">{{ payment.userId }}</DescriptionsItem>
          <DescriptionsItem label="套餐ID">{{ payment.packageId }}</DescriptionsItem>
          <DescriptionsItem label="学生套餐ID">{{ payment.studentPackageId ?? '-' }}</DescriptionsItem>
          <DescriptionsItem label="状态" :span="2">
            <Tag :color="statusColor">{{ statusText }}</Tag>
          </DescriptionsItem>
        </Descriptions>
      </div>

      <!-- Stripe 字段 -->
      <div>
        <div class="mb-2 font-medium">Stripe 字段</div>
        <Descriptions :column="1" bordered size="small">
          <DescriptionsItem label="Checkout Session ID">
            <span class="break-all font-mono text-xs">
              {{ payment.channelSessionId || '-' }}
            </span>
          </DescriptionsItem>
          <DescriptionsItem label="PaymentIntent ID">
            <span class="break-all font-mono text-xs">
              {{ payment.channelPaymentIntentId || '-' }}
            </span>
          </DescriptionsItem>
          <DescriptionsItem label="Charge ID">
            <span class="break-all font-mono text-xs">
              {{ payment.channelChargeId || '-' }}
            </span>
          </DescriptionsItem>
          <DescriptionsItem label="支付方式">
            {{ payment.paymentMethodType || '-' }}
          </DescriptionsItem>
        </Descriptions>
      </div>

      <!-- 金额信息 -->
      <div>
        <div class="mb-2 font-medium">金额信息</div>
        <Descriptions :column="2" bordered size="small">
          <DescriptionsItem label="请求金额">
            {{ formatCurrencyAmount(payment.amountRequest, payment.currencyRequest) }}
          </DescriptionsItem>
          <DescriptionsItem label="请求币种">
            {{ payment.currencyRequest || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="实付金额">
            {{ formatCurrencyAmount(payment.amountPaid, payment.currencyPaid) }}
          </DescriptionsItem>
          <DescriptionsItem label="付款币种">
            {{ payment.currencyPaid || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="结算金额(USD)">
            {{ formatUsdAmount(payment.amountSettledUsd) }}
          </DescriptionsItem>
          <DescriptionsItem label="折扣金额(USD)">
            <span
              v-if="payment.discountAmountUsd != null && Number(payment.discountAmountUsd) > 0"
              class="text-green-600"
            >
              -{{ formatUsdAmount(payment.discountAmountUsd) }}
            </span>
            <span v-else class="text-gray-400">-</span>
          </DescriptionsItem>
          <DescriptionsItem label="推荐人ID">
            {{ payment.referrerUserId ?? '-' }}
          </DescriptionsItem>
        </Descriptions>
      </div>

      <!-- 时间信息 -->
      <div>
        <div class="mb-2 font-medium">时间信息</div>
        <Descriptions :column="1" bordered size="small">
          <DescriptionsItem label="支付时间">
            <template v-if="formatUtcLocal(payment.paidAt)">
              <div>{{ formatUtcLocal(payment.paidAt)?.local }}</div>
              <div class="text-xs text-gray-400">
                {{ formatUtcLocal(payment.paidAt)?.utc }} (UTC)
              </div>
            </template>
            <span v-else class="text-gray-400">-</span>
          </DescriptionsItem>
          <DescriptionsItem label="过期时间">
            <template v-if="formatUtcLocal(payment.expiredAt)">
              <div>{{ formatUtcLocal(payment.expiredAt)?.local }}</div>
              <div class="text-xs text-gray-400">
                {{ formatUtcLocal(payment.expiredAt)?.utc }} (UTC)
              </div>
            </template>
            <span v-else class="text-gray-400">-</span>
          </DescriptionsItem>
          <DescriptionsItem label="创建时间">
            <template v-if="formatUtcLocal(payment.createTime)">
              <div>{{ formatUtcLocal(payment.createTime)?.local }}</div>
              <div class="text-xs text-gray-400">
                {{ formatUtcLocal(payment.createTime)?.utc }} (UTC)
              </div>
            </template>
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
