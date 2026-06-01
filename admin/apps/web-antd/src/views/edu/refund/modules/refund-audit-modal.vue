<script lang="ts" setup>
import type { EduRefundApi } from '#/api/edu/refund';

import { computed, ref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  Descriptions,
  DescriptionsItem,
  Form,
  FormItem,
  InputNumber,
  message,
  Textarea,
} from 'ant-design-vue';

import { approveRefund, rejectRefund } from '#/api/edu/refund';

/** mode: 'approve' | 'reject' */
type AuditMode = 'approve' | 'reject';

interface ModalData {
  refund: EduRefundApi.Refund;
  mode: AuditMode;
}

const emit = defineEmits(['success']);

const modalData = ref<ModalData>();
const finalAmountUsd = ref<number | undefined>();
const adjustReason = ref('');
const auditNote = ref('');

const isApprove = computed(() => modalData.value?.mode === 'approve');

const refund = computed(() => modalData.value?.refund);

const suggestedAmount = computed(() => {
  const v = refund.value?.suggestedAmountUsd;
  if (v == null) return null;
  return Number(v);
});

const amountSettledUsd = computed(() => {
  // 最大可退 = suggestedAmountUsd(前端用此做上限,后端以 payment.amount_settled_usd 为准)
  return suggestedAmount.value ?? 0;
});

/** finalAmountUsd 是否与建议金额不同 */
const isAmountChanged = computed(() => {
  if (!isApprove.value) return false;
  if (finalAmountUsd.value == null || suggestedAmount.value == null)
    return false;
  return Math.abs(finalAmountUsd.value - suggestedAmount.value) > 0.001;
});

/** 验证表单 */
function validate(): string | null {
  if (isApprove.value) {
    if (finalAmountUsd.value == null) return '请填写最终退款金额';
    if (finalAmountUsd.value < 0) return '最终退款金额不能为负数';
    if (isAmountChanged.value && adjustReason.value.trim().length < 5) {
      return '调整原因不少于 5 个字符';
    }
  } else {
    if (auditNote.value.trim().length < 5) {
      return '拒绝理由不少于 5 个字符';
    }
  }
  return null;
}

/** 初始化 finalAmountUsd 为建议值 */
watch(
  () => modalData.value,
  (data) => {
    if (data?.mode === 'approve' && data.refund) {
      const v = data.refund.suggestedAmountUsd;
      finalAmountUsd.value = v != null ? Number(v) : undefined;
    }
    adjustReason.value = '';
    auditNote.value = '';
  },
  { deep: true },
);

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const errMsg = validate();
    if (errMsg) {
      message.warning(errMsg);
      return;
    }
    if (!refund.value?.id) {
      message.error('缺少退款工单 ID,无法提交');
      return;
    }
    modalApi.lock();
    try {
      if (isApprove.value) {
        await approveRefund(refund.value.id, {
          finalAmountUsd: finalAmountUsd.value!,
          adjustReason: isAmountChanged.value ? adjustReason.value : undefined,
          auditNote: auditNote.value || undefined,
        });
        message.success('退款已批准,正在调用 Stripe...');
      } else {
        await rejectRefund(refund.value.id, {
          auditNote: auditNote.value,
        });
        message.success('退款申请已拒绝');
      }
      await modalApi.close();
      emit('success');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      modalData.value = undefined;
      finalAmountUsd.value = undefined;
      adjustReason.value = '';
      auditNote.value = '';
      return;
    }
    modalData.value = modalApi.getData<ModalData>();
  },
});

const modalTitle = computed(() =>
  isApprove.value ? '审批退款 — 批准' : '审批退款 — 拒绝',
);
</script>

<template>
  <Modal :title="modalTitle">
    <div v-if="refund" class="space-y-4">
      <!-- 基础信息展示 -->
      <Descriptions :column="2" bordered size="small">
        <DescriptionsItem label="工单ID">{{ refund.id }}</DescriptionsItem>
        <DescriptionsItem label="学生ID">{{ refund.userId }}</DescriptionsItem>
        <DescriptionsItem label="关联支付单">{{ refund.paymentId }}</DescriptionsItem>
        <DescriptionsItem label="学生套餐ID">{{ refund.studentPackageId ?? '-' }}</DescriptionsItem>
        <DescriptionsItem label="系统建议金额(USD)" :span="2">
          <span class="font-semibold text-blue-600">
            ${{ suggestedAmount != null ? suggestedAmount.toFixed(2) : '-' }}
          </span>
        </DescriptionsItem>
        <DescriptionsItem label="申请原因" :span="2">
          {{ refund.applyReason || '-' }}
        </DescriptionsItem>
      </Descriptions>

      <!-- approve 表单 -->
      <Form
        v-if="isApprove"
        layout="vertical"
        class="mt-2"
      >
        <FormItem
          label="最终退款金额(USD)"
          :required="true"
        >
          <InputNumber
            v-model:value="finalAmountUsd"
            :min="0"
            :max="amountSettledUsd"
            :precision="2"
            :step="0.01"
            class="w-full"
            placeholder="请输入最终退款金额"
          />
          <div class="mt-1 text-xs text-gray-400">
            范围 [0, {{ amountSettledUsd.toFixed(2) }}] USD
          </div>
        </FormItem>

        <FormItem
          v-if="isAmountChanged"
          label="调整原因"
          :required="true"
          help="与建议金额不同时必填(≥5 字)"
        >
          <Textarea
            v-model:value="adjustReason"
            :rows="3"
            :maxlength="500"
            show-count
            placeholder="请填写调整原因(不少于 5 字)"
          />
        </FormItem>

        <FormItem label="审核备注(选填)">
          <Textarea
            v-model:value="auditNote"
            :rows="2"
            :maxlength="500"
            show-count
            placeholder="审核备注(可选)"
          />
        </FormItem>
      </Form>

      <!-- reject 表单 -->
      <Form
        v-else
        layout="vertical"
        class="mt-2"
      >
        <FormItem
          label="拒绝理由"
          :required="true"
          help="不少于 5 个字符"
        >
          <Textarea
            v-model:value="auditNote"
            :rows="4"
            :maxlength="500"
            show-count
            placeholder="请输入拒绝理由(不少于 5 字)"
          />
        </FormItem>
      </Form>
    </div>

    <div v-else class="p-4 text-center text-gray-400">加载中...</div>
  </Modal>
</template>
