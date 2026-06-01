<script lang="ts" setup>
import type { EduWithdrawalApi } from '#/api/edu/withdrawal';

import { computed, ref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  Descriptions,
  DescriptionsItem,
  Form,
  FormItem,
  message,
  RadioGroup,
  Textarea,
} from 'ant-design-vue';

import { auditWithdrawal } from '#/api/edu/withdrawal';
import { $t } from '#/locales';

import { getPayeeMethodLabel } from '../data';

/** spec §6.4 / §6.6 AuditModal
 *  - Radio 通过 / 驳回
 *  - 驳回时必填 reject_reason(≤ 500 字,前后端双校验)
 *  - 后端 audit(approved=true → approved;approved=false → rejected,余额回退)
 */
interface ModalData {
  withdrawal: EduWithdrawalApi.Withdrawal;
}

const emit = defineEmits(['success']);

const modalData = ref<ModalData>();
/** 默认通过(管理员审核多数场景为通过) */
const approved = ref<boolean>(true);
const rejectReason = ref<string>('');

const withdrawal = computed(() => modalData.value?.withdrawal);
const PAYEE_METHOD_LABEL = computed(() => getPayeeMethodLabel());

function validate(): null | string {
  if (!approved.value && rejectReason.value.trim().length < 5) {
    // 校验细节文案 zh-TW 未覆盖,保留中文(Phase E 评估补 key)
    return '驳回原因不少于 5 个字符';
  }
  if (rejectReason.value.length > 500) {
    return '驳回原因不能超过 500 字符';
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
      await auditWithdrawal(withdrawal.value.id, {
        approved: approved.value,
        rejectReason: approved.value ? undefined : rejectReason.value,
      });
      message.success(
        approved.value
          ? $t('edu.withdrawal.toast.approveSuccess')
          : $t('edu.withdrawal.toast.rejectSuccess'),
      );
      await modalApi.close();
      emit('success');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      modalData.value = undefined;
      approved.value = true;
      rejectReason.value = '';
      return;
    }
    modalData.value = modalApi.getData<ModalData>();
  },
});

watch(approved, (v) => {
  if (v) rejectReason.value = '';
});
</script>

<template>
  <Modal :title="$t('edu.withdrawal.modal.approveTitle')">
    <div v-if="withdrawal" class="space-y-4">
      <!-- 基础信息(只读) -->
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
          <span class="text-base font-semibold text-blue-600">
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
        <DescriptionsItem
          :label="$t('edu.withdrawal.column.appliedAt')"
          :span="2"
        >
          <div v-if="withdrawal.appliedAt">
            <div>{{ new Date(withdrawal.appliedAt).toLocaleString() }}</div>
            <div class="text-xs text-gray-400">
              {{ withdrawal.appliedAt }} (UTC)
            </div>
          </div>
        </DescriptionsItem>
      </Descriptions>

      <!-- 审核表单 -->
      <Form layout="vertical" class="mt-2">
        <!-- 「审核结果」label zh-TW key 树未覆盖,保留中文(辅助文案) -->
        <FormItem label="审核结果" :required="true">
          <RadioGroup
            v-model:value="approved"
            :options="[
              { label: $t('edu.withdrawal.action.approve'), value: true },
              { label: $t('edu.withdrawal.action.reject'), value: false },
            ]"
            option-type="button"
          />
        </FormItem>

        <FormItem
          v-if="!approved"
          :label="$t('edu.withdrawal.modal.rejectReasonLabel')"
          :required="true"
          help="不少于 5 字,不超过 500 字。驳回后冻结余额将回退到「可提现」。"
        >
          <Textarea
            v-model:value="rejectReason"
            :rows="4"
            :maxlength="500"
            show-count
            :placeholder="$t('edu.withdrawal.modal.rejectReasonPlaceholder')"
          />
        </FormItem>

        <!-- 通过 hint:辅助文案 zh-TW 未覆盖,保留中文 -->
        <div v-else class="text-xs text-gray-500">
          通过后状态变更为「已通过待打款」,余额维持冻结。请尽快走 [标记已打款]
          / [标记打款失败] 收口。
        </div>
      </Form>
    </div>

    <div v-else class="p-4 text-center text-gray-400">
      {{ $t('edu.withdrawal.modal.submitting') }}
    </div>
  </Modal>
</template>
