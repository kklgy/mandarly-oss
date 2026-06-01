<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduReferralApi } from '#/api/edu/referral';
import type { InfraConfigApi } from '#/api/infra/config';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Card, InputNumber, message, Space, Spin, Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getReferralPage } from '#/api/edu/referral';
import { getConfigPage, updateConfig } from '#/api/infra/config';

import {
  REFERRAL_STATUS_COLOR,
  REFERRAL_STATUS_LABEL,
  useGridColumns,
  useGridFormSchema,
} from './data';

type ConfigPageQuery = Parameters<typeof getConfigPage>[0] & {
  key: string;
};

type ReferralConfigKey =
  | 'mandarly.referral.referee_discount_usd'
  | 'mandarly.referral.referrer_reward_package_id';

type ReferralConfigItem = {
  description: string;
  key: ReferralConfigKey;
  min: number;
  precision: number;
  title: string;
  unit: string;
};

const CONFIG_ITEMS: ReferralConfigItem[] = [
  {
    key: 'mandarly.referral.referee_discount_usd',
    title: '被推荐人首单折扣',
    description: '新用户使用有效推荐码完成首单时应用的折扣。',
    unit: 'USD',
    min: 0,
    precision: 2,
  },
  {
    key: 'mandarly.referral.referrer_reward_package_id',
    title: '推荐人奖励套餐',
    description: '被推荐人首单支付成功后,给推荐人发放的套餐 ID。',
    unit: 'package.id',
    min: 1,
    precision: 0,
  },
];

const configLoading = ref(false);
const savingKey = ref<ReferralConfigKey>();
const configMap = reactive<Record<string, InfraConfigApi.Config | undefined>>({});
const valueMap = reactive<Record<string, number | undefined>>({});

async function fetchConfigByKey(key: ReferralConfigKey) {
  const page = await getConfigPage({
    key,
    pageNo: 1,
    pageSize: 20,
  } as ConfigPageQuery);
  return page.list.find((item) => item.key === key);
}

async function loadConfigs() {
  configLoading.value = true;
  try {
    const rows = await Promise.all(CONFIG_ITEMS.map((item) => fetchConfigByKey(item.key)));
    rows.forEach((row, index) => {
      const key = CONFIG_ITEMS[index]!.key;
      configMap[key] = row;
      valueMap[key] = row?.value == null ? undefined : Number(row.value);
    });
  } finally {
    configLoading.value = false;
  }
}

async function handleSaveConfig(item: ReferralConfigItem) {
  const config = configMap[item.key];
  const value = valueMap[item.key];
  if (!config) {
    message.error(`未找到配置项:${item.key}`);
    return;
  }
  if (value == null || Number.isNaN(value) || value < item.min) {
    message.error('请输入有效数值');
    return;
  }

  savingKey.value = item.key;
  try {
    await updateConfig({
      ...config,
      value: item.precision === 0 ? String(Math.trunc(value)) : value.toFixed(item.precision),
    });
    message.success('保存成功。新值只影响后续推荐链路,历史记录不追溯。');
    await loadConfigs();
  } finally {
    savingKey.value = undefined;
  }
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
          return await getReferralPage({
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
  } as VxeTableGridOptions<EduReferralApi.ReferralRecord>,
});

onMounted(loadConfigs);
</script>

<template>
  <Page auto-content-height>
    <div class="referral-admin">
      <Spin :spinning="configLoading">
        <div class="referral-admin__config-grid">
          <Card v-for="item in CONFIG_ITEMS" :key="item.key" :title="item.title">
            <p class="referral-admin__desc">{{ item.description }}</p>
            <p class="referral-admin__key">{{ item.key }}</p>
            <Space>
              <InputNumber
                v-model:value="valueMap[item.key]"
                :min="item.min"
                :precision="item.precision"
                class="referral-admin__input"
              />
              <span class="referral-admin__unit">{{ item.unit }}</span>
              <Button
                type="primary"
                :loading="savingKey === item.key"
                @click="handleSaveConfig(item)"
              >
                保存
              </Button>
            </Space>
          </Card>

          <Card title="防刷规则">
            <div class="referral-admin__rules">
              <div class="referral-admin__rule">
                <Tag color="success">已启用</Tag>
                <span>推荐码不能引用自己</span>
              </div>
              <div class="referral-admin__rule">
                <Tag color="success">已启用</Tag>
                <span>被推荐人仅首单可用推荐码</span>
              </div>
              <div class="referral-admin__rule">
                <Tag color="success">已启用</Tag>
                <span>同一被推荐人不能重复绑定推荐链路</span>
              </div>
            </div>
          </Card>
        </div>
      </Spin>

      <Grid table-title="推荐记录列表">
      <!-- 状态 badge -->
      <template #status="{ row }">
        <Tag :color="REFERRAL_STATUS_COLOR[row.status] || 'default'">
          {{ REFERRAL_STATUS_LABEL[row.status] || row.status }}
        </Tag>
      </template>

      <!-- 折扣金额 -->
      <template #refereeDiscountAmountUsd="{ row }">
        <span
          v-if="row.refereeDiscountAmountUsd != null && Number(row.refereeDiscountAmountUsd) > 0"
          class="text-green-600"
        >
          -${{ Number(row.refereeDiscountAmountUsd).toFixed(2) }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 绑定时间:UTC + 本地双行 -->
      <template #boundAt="{ row }">
        <div v-if="row.boundAt">
          <div>{{ new Date(row.boundAt).toLocaleString() }}</div>
          <div class="text-xs text-gray-400">{{ row.boundAt }} (UTC)</div>
        </div>
        <span v-else class="text-gray-400">-</span>
      </template>
      </Grid>
    </div>
  </Page>
</template>

<style scoped>
.referral-admin {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.referral-admin__config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.referral-admin__desc {
  margin-bottom: 8px;
  color: rgb(100 116 139);
}

.referral-admin__key {
  margin-bottom: 16px;
  color: rgb(148 163 184);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.referral-admin__input {
  width: 160px;
}

.referral-admin__unit {
  color: rgb(71 85 105);
}

.referral-admin__rules {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.referral-admin__rule {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgb(51 65 85);
}
</style>
