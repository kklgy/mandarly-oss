<script lang="ts" setup>
import type { EduWithdrawalApi } from '#/api/edu/withdrawal';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  Descriptions,
  DescriptionsItem,
  Form,
  FormItem,
  message,
  Textarea,
} from 'ant-design-vue';

import { markWithdrawalFailed } from '#/api/edu/withdrawal';
import { $t } from '#/locales';

import { getPayeeMethodLabel } from '../data';

/** spec §6.4 / §6.6 MarkFailedModal
 *  - 必填 fail_reason(≤ 500 字)
 *  - 提交后 approved → failed,冻结余额回退到「可提现」(后端处理)
 */
interface ModalData {
  withdrawal: EduWithdrawalApi.Withdrawal;
}

const emit = defineEmits(['success']);

const modalData = ref<ModalData>();
const failReason = ref<string>('');

const withdrawal = computed(() => modalData.value?.withdrawal);
const PAYEE_METHOD_LABEL = computed(() => getPayeeMethodLabel());

function validate(): null | string {
  // 校验细节文案 zh-TW 未覆盖,保留中文
  if (failReason.value.trim().length < 5) {
    return '失败原因不少于 5 个字符';
  }
  if (failReason.value.length > 500) {
    return '失败原因不能超过 500 字符';
  }
  return null;
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const errMsg = validate();
    if (errMsg) {
      message.warning(errMsg);
      return;
    }
    if (!withdrawal.value?.id) {
      message.error($t('edu.withdrawal.error.notFound'));
      return;
    }
    modalApi.lock();
    try {
      await markWithdrawalFailed(withdrawal.value.id, {
        failReason: failReason.value,
      });
      message.success($t('edu.withdrawal.toast.markFailedSuccess'));
      await modalApi.close();
      emit('success');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      modalData.value = undefined;
      failReason.value = '';
      return;
    }
    modalData.value = modalApi.getData<ModalData>();
  },
});
</script>

<template>
  <Modal :title="$t('edu.withdrawal.modal.markFailedTitle')">
    <div v-if="withdrawal" class="space-y-4">
      <!-- 基础信息 -->
      <Descriptions :column="2" bordered size="small">
        <DescriptionsItem :label="$t('edu.withdrawal.column.id')">
          {{ withdrawal.id }}
        </DescriptionsItem>
        <DescriptionsItem :label="$t('edu.withdrawal.column.teacher')">
          {{ withdrawal.teacherId }}
        </DescriptionsItem>
        <DescriptionsItem
          :label="$t('edu.withdrawal.column.amount')"
          :span="2"
        >
          <span class="text-base font-semibold text-red-500">
            ${{ Number(withdrawal.amount).toFixed(2) }}
          </span>
        </DescriptionsItem>
        <DescriptionsItem :label="$t('edu.withdrawal.column.payeeMethod')">
          {{
            PAYEE_METHOD_LABEL[withdrawal.payeeMethod] || withdrawal.payeeMethod
          }}
        </DescriptionsItem>
        <DescriptionsItem :label="$t('edu.withdrawal.column.payeeMasked')">
          {{ withdrawal.payeeInfoMasked || '-' }}
        </DescriptionsItem>
      </Descriptions>

      <!-- 失败原因表单 -->
      <Form layout="vertical" class="mt-2">
        <FormItem
          :label="$t('edu.withdrawal.modal.markFailedReasonLabel')"
          :required="true"
          help="不少于 5 字,不超过 500 字。教师可在 app 看到此理由(便于更新收款信息后重新申请)。"
        >
          <Textarea
            v-model:value="failReason"
            :rows="4"
            :maxlength="500"
            show-count
            :placeholder="
              $t('edu.withdrawal.modal.markFailedReasonPlaceholder')
            "
          />
        </FormItem>

        <!-- 回退提示 zh-TW 未覆盖,保留中文 -->
        <div class="rounded bg-red-50 p-2 text-xs text-red-700">
          提交后状态变为「打款失败」,冻结余额将自动回退到教师「可提现」余额。
        </div>
      </Form>
    </div>

    <div v-else class="p-4 text-center text-gray-400">
      {{ $t('edu.withdrawal.modal.submitting') }}
    </div>
  </Modal>
</template>
