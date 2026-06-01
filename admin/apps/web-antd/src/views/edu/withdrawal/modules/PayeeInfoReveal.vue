<script lang="ts" setup>
import type { EduWithdrawalApi } from '#/api/edu/withdrawal';

import { computed, onBeforeUnmount, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { IconifyIcon } from '@vben/icons';

import { Alert, Button, message } from 'ant-design-vue';

import { revealWithdrawalPayee } from '#/api/edu/withdrawal';
import { $t } from '#/locales';

import { getPayeeMethodLabel } from '../data';

/** spec §6.5 ⭐ 最敏感:payee_info 明文揭示
 *
 *  1. 弹层 = 二次确认 + 警告(永久审计日志)
 *  2. 点「确认查看」→ 调 GET /reveal-payee → 后端写一次性审计日志
 *  3. 弹层显示明文 + 3 秒倒计时
 *  4. 倒计时归零自动遮罩(不关闭弹层),需手动关闭
 *
 *  权限:edu:withdrawal:reveal-payee(后端 @PreAuthorize 已校验,前端入口隐藏即可)
 */
interface ModalData {
  withdrawal: EduWithdrawalApi.Withdrawal;
}

const modalData = ref<ModalData>();
/** 步骤:'confirm' = 二次确认页;'reveal' = 明文展示 + 倒计时;'masked' = 倒计时归零遮罩 */
const step = ref<'confirm' | 'masked' | 'reveal'>('confirm');
/** 明文 payee_info */
const payeeInfo = ref<string>('');
/** 倒计时秒数 */
const countdown = ref<number>(3);
/** 倒计时 timer */
let countdownTimer: number | undefined;
/** 加载态 */
const loading = ref<boolean>(false);

const withdrawal = computed(() => modalData.value?.withdrawal);
const PAYEE_METHOD_LABEL = computed(() => getPayeeMethodLabel());

/** 清理倒计时(关闭弹层 / 卸载组件) */
function clearCountdown() {
  if (countdownTimer !== undefined) {
    window.clearInterval(countdownTimer);
    countdownTimer = undefined;
  }
}

/** 启动 3 秒倒计时 */
function startCountdown() {
  countdown.value = 3;
  clearCountdown();
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearCountdown();
      // 倒计时归零:遮罩 + 清空明文(只读到一次内存,关闭后无法重新看)
      payeeInfo.value = '';
      step.value = 'masked';
    }
  }, 1000);
}

/** 二次确认 → 调接口 → 展示明文 + 倒计时 */
async function handleConfirmReveal() {
  if (!withdrawal.value?.id) {
    message.error($t('edu.withdrawal.error.notFound'));
    return;
  }
  loading.value = true;
  try {
    payeeInfo.value = await revealWithdrawalPayee(withdrawal.value.id);
    step.value = 'reveal';
    startCountdown();
  } catch (error) {
    // 接口错误(无权限 / 不存在 / 解密失败)由全局拦截器提示,这里兜底
    if (error instanceof Error) {
      message.error(error.message || $t('edu.withdrawal.toast.loadFailed'));
    }
  } finally {
    loading.value = false;
  }
}

const [Modal, modalApi] = useVbenModal({
  title: $t('edu.withdrawal.modal.revealTitle'),
  /** 此 modal 不走 onConfirm,只显示 / 关闭 */
  showConfirmButton: false,
  cancelText: $t('edu.withdrawal.modal.cancel'),
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      clearCountdown();
      payeeInfo.value = '';
      step.value = 'confirm';
      countdown.value = 3;
      modalData.value = undefined;
      return;
    }
    modalData.value = modalApi.getData<ModalData>();
  },
});

onBeforeUnmount(() => {
  clearCountdown();
});
</script>

<template>
  <Modal>
    <div v-if="withdrawal" class="space-y-4">
      <!-- step 1:二次确认 -->
      <template v-if="step === 'confirm'">
        <!-- Alert 详细文案 zh-TW 未覆盖,保留中文(辅助型 warning,Phase E 评估补 key) -->
        <Alert type="warning" show-icon>
          <template #message>即将查看教师收款信息明文</template>
          <template #description>
            {{ $t('edu.withdrawal.modal.revealWarning') }}
            此操作将被系统永久记录(管理员 ID / 提现申请 ID / 时间 / IP),
            可在「系统管理 → 操作日志」追溯。
            明文将仅显示 **3 秒**,之后自动遮罩。
          </template>
        </Alert>

        <div class="rounded bg-gray-50 p-3 text-sm">
          <div class="flex justify-between py-1">
            <span class="text-gray-500">
              {{ $t('edu.withdrawal.column.id') }}
            </span>
            <span class="font-medium">{{ withdrawal.id }}</span>
          </div>
          <div class="flex justify-between py-1">
            <span class="text-gray-500">
              {{ $t('edu.withdrawal.column.teacher') }}
            </span>
            <span class="font-medium">{{ withdrawal.teacherId }}</span>
          </div>
          <div class="flex justify-between py-1">
            <span class="text-gray-500">
              {{ $t('edu.withdrawal.column.payeeMethod') }}
            </span>
            <span class="font-medium">
              {{
                PAYEE_METHOD_LABEL[withdrawal.payeeMethod] ||
                withdrawal.payeeMethod
              }}
            </span>
          </div>
          <div class="flex justify-between py-1">
            <span class="text-gray-500">
              {{ $t('edu.withdrawal.column.payeeMasked') }}
            </span>
            <span class="font-medium text-gray-400">
              {{ withdrawal.payeeInfoMasked || '-' }}
            </span>
          </div>
        </div>

        <div class="flex justify-center pt-2">
          <Button
            type="primary"
            danger
            :loading="loading"
            @click="handleConfirmReveal"
          >
            <IconifyIcon icon="ant-design:eye-outlined" class="mr-1" />
            {{ $t('edu.withdrawal.modal.revealConfirm') }}
          </Button>
        </div>
      </template>

      <!-- step 2:明文展示 + 倒计时 -->
      <template v-else-if="step === 'reveal'">
        <!-- 倒计时 alert 文案 zh-TW 未覆盖,保留中文 -->
        <Alert type="error" show-icon>
          <template #message>明文展示中 — 此操作已记录</template>
          <template #description>
            <span class="font-bold">{{ countdown }}</span>
            秒后自动遮罩,关闭后无法再次查看(需重新申请权限)。
          </template>
        </Alert>

        <div
          class="rounded-lg border-2 border-red-200 bg-red-50 p-4 text-center"
        >
          <div class="mb-2 text-xs text-gray-500">
            {{ PAYEE_METHOD_LABEL[withdrawal.payeeMethod] }}
            {{ $t('edu.withdrawal.detail.payeeInfoTitle') }}
          </div>
          <div
            class="select-text break-all font-mono text-base font-semibold tracking-wider text-red-700"
          >
            {{ payeeInfo || '-' }}
          </div>
        </div>

        <!-- 倒计时进度条 + 数字 -->
        <div class="flex items-center justify-center gap-3">
          <div
            class="flex h-12 w-12 items-center justify-center rounded-full bg-red-100 text-2xl font-bold text-red-600"
          >
            {{ countdown }}
          </div>
          <span class="text-sm text-gray-500">
            剩余 {{ countdown }} 秒,之后自动遮罩
          </span>
        </div>
      </template>

      <!-- step 3:已遮罩(倒计时归零) -->
      <template v-else>
        <!-- 已遮罩 alert 文案 zh-TW 未覆盖,保留中文 -->
        <Alert type="info" show-icon>
          <template #message>明文已自动遮罩</template>
          <template #description>
            3 秒展示时间已结束,如需再次查看请关闭后重新点击「查看完整」入口。
            每次查看都会写入审计日志。
          </template>
        </Alert>

        <div
          class="rounded-lg border-2 border-dashed border-gray-300 bg-gray-50 p-4 text-center"
        >
          <div class="mb-2 text-xs text-gray-500">
            {{ PAYEE_METHOD_LABEL[withdrawal.payeeMethod] }}
            {{ $t('edu.withdrawal.detail.payeeInfoTitle') }}
          </div>
          <div
            class="font-mono text-base font-semibold tracking-wider text-gray-400"
          >
            ●●●● ●●●● ●●●● ●●●●
          </div>
        </div>
      </template>
    </div>

    <div v-else class="p-4 text-center text-gray-400">
      {{ $t('edu.withdrawal.modal.submitting') }}
    </div>
  </Modal>
</template>
