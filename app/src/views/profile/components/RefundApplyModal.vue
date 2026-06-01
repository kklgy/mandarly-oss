<script setup>
/**
 * RefundApplyModal.vue — 申请退款弹窗
 *
 * Props:
 *   paymentId {Number} — 要退款的支付单 ID
 * Emits:
 *   success  — 提交成功
 *   close    — 关闭弹窗
 */
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { applyRefund } from '@/api/payment'

const props = defineProps({
  paymentId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['success', 'close'])

const { t } = useI18n()

const form = reactive({ reason: '' })
const submitting = ref(false)
const errorMsg = ref('')

function validate() {
  if (!form.reason || form.reason.trim().length < 10) {
    errorMsg.value = t('refund.apply.reason.min10')
    return false
  }
  errorMsg.value = ''
  return true
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  try {
    await applyRefund({
      paymentId: props.paymentId,
      reason: form.reason.trim()
    })
    ElMessage.success(t('refund.apply.success'))
    emit('success')
    emit('close')
  } catch (e) {
    errorMsg.value = e?.message || t('refund.apply.error')
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="true"
    :title="t('refund.apply.title')"
    width="440px"
    destroy-on-close
    @close="emit('close')"
  >
    <el-form label-position="top">
      <el-form-item :label="t('refund.apply.reason.label')">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          dir="auto"
          :placeholder="t('refund.apply.reason.placeholder')"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <p v-if="errorMsg" class="ram-error">{{ errorMsg }}</p>
    </el-form>
    <template #footer>
      <el-button @click="emit('close')">{{ t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        :disabled="submitting"
        @click="handleSubmit"
      >
        {{ t('common.submit') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.ram-error {
  margin: brand.$spacing-1 0 0;
  font-size: brand.$font-size-sm;
  color: brand.$color-error;
}
</style>
