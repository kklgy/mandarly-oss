<script lang="ts" setup>
import type { InfraConfigApi } from '#/api/infra/config';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Alert, Button, Card, InputNumber, message, Space, Spin } from 'ant-design-vue';

import { getConfigPage, updateConfig } from '#/api/infra/config';

type ConfigPageQuery = Parameters<typeof getConfigPage>[0] & {
  key: string;
};

type SettlementConfigKey =
  | 'mandarly.income.teacher_lesson_fee_usd'
  | 'mandarly.income.frozen_days'
  | 'mandarly.withdrawal.min_amount';

type SettlementConfigItem = {
  key: SettlementConfigKey;
  title: string;
  description: string;
  unit: string;
  min: number;
  precision: number;
};

const CONFIG_ITEMS: SettlementConfigItem[] = [
  {
    key: 'mandarly.income.teacher_lesson_fee_usd',
    title: '普通课教师课时费',
    description: '当前一期规则:普通 1v1 课程完成后,每节按该金额计入教师收入。',
    unit: 'USD / 节',
    min: 0,
    precision: 2,
  },
  {
    key: 'mandarly.income.frozen_days',
    title: '收入冻结期',
    description: '课程完成后先进入冻结余额,到期后转为可提现余额。',
    unit: '天',
    min: 0,
    precision: 0,
  },
  {
    key: 'mandarly.withdrawal.min_amount',
    title: '最低提现金额',
    description: '教师可提现余额达到该金额后才允许提交提现申请。',
    unit: 'USD',
    min: 0,
    precision: 2,
  },
];

const loading = ref(false);
const savingKey = ref<SettlementConfigKey>();
const configMap = reactive<Record<string, InfraConfigApi.Config | undefined>>({});
const valueMap = reactive<Record<string, number | undefined>>({});

async function fetchConfigByKey(key: SettlementConfigKey) {
  const page = await getConfigPage({
    key,
    pageNo: 1,
    pageSize: 20,
  } as ConfigPageQuery);
  return page.list.find((item) => item.key === key);
}

async function loadConfigs() {
  loading.value = true;
  try {
    const rows = await Promise.all(CONFIG_ITEMS.map((item) => fetchConfigByKey(item.key)));
    rows.forEach((row, index) => {
      const key = CONFIG_ITEMS[index]!.key;
      configMap[key] = row;
      valueMap[key] = row?.value == null ? undefined : Number(row.value);
    });
  } finally {
    loading.value = false;
  }
}

async function handleSave(item: SettlementConfigItem) {
  const config = configMap[item.key];
  const value = valueMap[item.key];
  if (!config) {
    message.error(`未找到配置项:${item.key}`);
    return;
  }
  if (value == null || Number.isNaN(value)) {
    message.error('请输入有效数值');
    return;
  }
  savingKey.value = item.key;
  try {
    await updateConfig({
      ...config,
      value: item.precision === 0 ? String(Math.trunc(value)) : value.toFixed(item.precision),
    });
    message.success('保存成功。新值只影响后续新预约订单,历史订单不追溯。');
    await loadConfigs();
  } finally {
    savingKey.value = undefined;
  }
}

onMounted(loadConfigs);
</script>

<template>
  <Page auto-content-height title="结算配置">
    <div class="settlement-config">
      <Alert
        show-icon
        type="warning"
        message="当前为一期临时结算模型"
        description="普通课仍按固定课时费结算。后续如改为按套餐实收比例、教师等级或市场阶梯价分成,需要重新设计结算规则。这里的修改只影响后续新预约订单,历史订单不追溯。"
      />

      <Spin :spinning="loading">
        <div class="settlement-config__grid">
          <Card v-for="item in CONFIG_ITEMS" :key="item.key" :title="item.title">
            <p class="settlement-config__desc">{{ item.description }}</p>
            <p class="settlement-config__key">{{ item.key }}</p>
            <Space>
              <InputNumber
                v-model:value="valueMap[item.key]"
                :min="item.min"
                :precision="item.precision"
                class="settlement-config__input"
              />
              <span class="settlement-config__unit">{{ item.unit }}</span>
              <Button
                type="primary"
                :loading="savingKey === item.key"
                @click="handleSave(item)"
              >
                保存
              </Button>
            </Space>
          </Card>
        </div>
      </Spin>
    </div>
  </Page>
</template>

<style scoped>
.settlement-config {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.settlement-config__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.settlement-config__desc {
  margin-bottom: 8px;
  color: rgb(100 116 139);
}

.settlement-config__key {
  margin-bottom: 16px;
  color: rgb(148 163 184);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.settlement-config__input {
  width: 160px;
}

.settlement-config__unit {
  color: rgb(71 85 105);
}
</style>
