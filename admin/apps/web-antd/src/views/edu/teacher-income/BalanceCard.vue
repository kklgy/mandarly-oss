<script lang="ts" setup>
import type { EduTeacherIncomeApi } from '#/api/edu/teacher-income';
import type { EduWithdrawalApi } from '#/api/edu/withdrawal';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenModal } from '@vben/common-ui';

import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Card,
  Descriptions,
  DescriptionsItem,
  Empty,
  Statistic,
  Tag,
} from 'ant-design-vue';

import { getTeacherBalance } from '#/api/edu/teacher-income';
import { getWithdrawalPage } from '#/api/edu/withdrawal';
import {
  WITHDRAWAL_STATUS_COLOR,
  WITHDRAWAL_STATUS_LABEL,
} from '#/views/edu/withdrawal/data';

const balance = ref<EduTeacherIncomeApi.TeacherBalance>();
/** 该教师最近 5 条提现历史(M6 C4 联动) */
const withdrawals = ref<EduWithdrawalApi.Withdrawal[]>([]);
const loading = ref(false);
const teacherIdLabel = ref<number>();
const router = useRouter();

function formatUsd(val?: null | number | string) {
  if (val == null) return '$0.00';
  return `$${Number(val).toFixed(2)}`;
}

function formatUtcLocal(utcStr?: null | string) {
  if (!utcStr) return '-';
  return new Date(utcStr).toLocaleString();
}

/** 跳转到提现审核列表并按 teacherId 筛选 */
function handleJumpWithdrawal() {
  if (!teacherIdLabel.value) return;
  router.push({
    path: '/edu/withdrawal',
    query: { teacherId: String(teacherIdLabel.value) },
  });
}

const [Modal, modalApi] = useVbenModal({
  title: '教师余额详情',
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      balance.value = undefined;
      withdrawals.value = [];
      return;
    }
    const data = modalApi.getData<{ teacherId: number }>();
    if (!data?.teacherId) return;
    teacherIdLabel.value = data.teacherId;
    loading.value = true;
    try {
      // 并行加载余额 + 最近 5 条提现历史
      const [balanceRes, withdrawalRes] = await Promise.all([
        getTeacherBalance(data.teacherId),
        getWithdrawalPage({
          pageNo: 1,
          pageSize: 5,
          teacherId: data.teacherId,
        }).catch(() => ({ list: [], total: 0 })),
      ]);
      balance.value = balanceRes;
      withdrawals.value = withdrawalRes.list || [];
    } finally {
      loading.value = false;
    }
  },
});
</script>

<template>
  <Modal>
    <div v-if="loading" class="p-4 text-center text-gray-400">加载中...</div>

    <div v-else-if="balance" class="space-y-4">
      <!-- 三卡片:冻结 / 可用 / 累计 -->
      <div class="grid grid-cols-3 gap-3">
        <Card size="small" class="text-center">
          <Statistic
            title="冻结中(USD)"
            :value="Number(balance.frozenUsd)"
            prefix="$"
            :precision="2"
            :value-style="{ color: '#faad14' }"
          />
        </Card>
        <Card size="small" class="text-center">
          <Statistic
            title="可提现(USD)"
            :value="Number(balance.availableUsd)"
            prefix="$"
            :precision="2"
            :value-style="{ color: '#52c41a' }"
          />
        </Card>
        <Card size="small" class="text-center">
          <Statistic
            title="累计已赚(USD)"
            :value="Number(balance.totalEarnedUsd)"
            prefix="$"
            :precision="2"
            :value-style="{ color: '#1677ff' }"
          />
        </Card>
      </div>

      <!-- 详情 -->
      <Descriptions :column="2" bordered size="small">
        <DescriptionsItem label="教师ID">{{ balance.teacherId }}</DescriptionsItem>
        <DescriptionsItem label="累计已提现">
          {{ formatUsd(balance.totalWithdrawnUsd) }}
        </DescriptionsItem>
        <DescriptionsItem label="最后重建时间" :span="2">
          {{ formatUtcLocal(balance.lastRebuildAt) }}
        </DescriptionsItem>
      </Descriptions>

      <!-- 最近提现历史(M6 C4 联动) -->
      <div>
        <div class="mb-2 flex items-center justify-between">
          <div class="text-sm font-medium">
            最近提现申请
            <span class="ml-1 text-xs text-gray-400">最近 5 条</span>
          </div>
          <Button
            type="link"
            size="small"
            v-access:code="['edu:withdrawal:query']"
            @click="handleJumpWithdrawal"
          >
            查看全部
            <IconifyIcon icon="ant-design:arrow-right-outlined" class="ml-1" />
          </Button>
        </div>

        <div
          v-if="withdrawals.length > 0"
          class="rounded border border-gray-100"
        >
          <div
            v-for="w in withdrawals"
            :key="w.id"
            class="flex items-center justify-between border-b border-gray-100 px-3 py-2 last:border-b-0"
          >
            <div class="flex flex-col">
              <div class="text-sm font-medium">#{{ w.id }}</div>
              <div class="text-xs text-gray-400">
                {{ formatUtcLocal(w.appliedAt) }}
              </div>
            </div>
            <div class="flex items-center gap-3">
              <span class="font-semibold text-blue-600">
                ${{ Number(w.amount).toFixed(2) }}
              </span>
              <Tag
                :color="WITHDRAWAL_STATUS_COLOR[w.status] || 'default'"
                class="m-0"
              >
                {{ WITHDRAWAL_STATUS_LABEL[w.status] || w.status }}
              </Tag>
            </div>
          </div>
        </div>

        <Empty
          v-else
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
          description="该教师暂无提现申请"
          class="py-4"
        />
      </div>
    </div>

    <div v-else class="p-4 text-center text-gray-400">暂无数据</div>

    <template #footer>
      <Button @click="modalApi.close()">关闭</Button>
    </template>
  </Modal>
</template>
