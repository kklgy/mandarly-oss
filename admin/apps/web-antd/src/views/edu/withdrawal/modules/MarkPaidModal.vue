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

import { ImageUpload } from '#/components/upload';

import { markWithdrawalPaid } from '#/api/edu/withdrawal';
import { $t } from '#/locales';

import { getPayeeMethodLabel } from '../data';

/** spec §6.4 / §6.6 MarkPaidModal
 *  - 选填 paid_proof(凭证截图 URL,复用 ImageUpload 走 infra/file 通道)
 *  - 选填 paid_remark(打款备注,例如对方流水号)
 *  - 提交后 approved → paid,余额清算
 */
interface ModalData {
  withdrawal: EduWithdrawalApi.Withdrawal;
}

const emit = defineEmits(['success']);

const modalData = ref<ModalData>();
const paidProof = ref<string>('');
const paidRemark = ref<string>('');

const withdrawal = computed(() => modalData.value?.withdrawal);
const PAYEE_METHOD_LABEL = computed(() => getPayeeMethodLabel());

function validate(): null | string {
  // 长度校验提示 zh-TW 未覆盖,保留中文
  if (paidProof.value && paidProof.value.length > 512) {
    return '凭证 URL 长度不能超过 512 字符';
  }
  if (paidRemark.value && paidRemark.value.length > 500) {
    return '备注长度不能超过 500 字符';
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
      await markWithdrawalPaid(withdrawal.value.id, {
        paidProof: paidProof.value || undefined,
        paidRemark: paidRemark.value || undefined,
      });
      message.success($t('edu.withdrawal.toast.markPaidSuccess'));
      await modalApi.close();
      emit('success');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      modalData.value = undefined;
      paidProof.value = '';
      paidRemark.value = '';
      return;
    }
    modalData.value = modalApi.getData<ModalData>();
  },
});
</script>

<template>
  <Modal :title="$t('edu.withdrawal.modal.markPaidTitle')">
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
          <span class="text-base font-semibold text-green-600">
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

      <!-- 打款表单 -->
      <Form layout="vertical" class="mt-2">
        <FormItem
          :label="$t('edu.withdrawal.modal.markPaidProofLabel')"
          help="支持 PNG / JPG,单文件 ≤ 2 MB。建议截图保留对方流水号 / 时间。"
        >
          <ImageUpload v-model:value="paidProof" :max-number="1" />
        </FormItem>

        <FormItem
          :label="$t('edu.withdrawal.modal.markPaidRemarkLabel')"
          help="例如对方流水号 / 渠道说明"
        >
          <Textarea
            v-model:value="paidRemark"
            :rows="3"
            :maxlength="500"
            show-count
            :placeholder="$t('edu.withdrawal.modal.markPaidRemarkPlaceholder')"
          />
        </FormItem>

        <!-- 不可逆提示 zh-TW 未覆盖,保留中文 -->
        <div class="rounded bg-amber-50 p-2 text-xs text-amber-700">
          提交后状态变为「已打款」,**不可逆**。若实际打款失败,需通过人工调整流水补正。
        </div>
      </Form>
    </div>

    <div v-else class="p-4 text-center text-gray-400">
      {{ $t('edu.withdrawal.modal.submitting') }}
    </div>
  </Modal>
</template>
