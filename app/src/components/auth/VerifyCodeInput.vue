<script setup>
/**
 * VerifyCodeInput.vue — 验证码输入框 + 60s 倒计时按钮
 *
 * Props:
 *   modelValue: string — v-model 绑定验证码值
 *   loading: boolean — 发送中
 *   onSend: () => Promise<void> — 点击发送按钮的回调
 *   placeholder: string — 输入框 placeholder
 *
 * Emits:
 *   update:modelValue — v-model 更新
 */
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  },
  onSend: {
    type: Function,
    default: null
  },
  placeholder: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])
const { t } = useI18n()

const countdown = ref(0)
const sending = ref(false)
let timer = null

function startCountdown() {
  countdown.value = 60
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

async function handleSend() {
  if (countdown.value > 0 || sending.value) return
  if (!props.onSend) return
  sending.value = true
  try {
    await props.onSend()
    startCountdown()
  } finally {
    sending.value = false
  }
}

function onInput(val) {
  emit('update:modelValue', val)
}
</script>

<template>
  <div class="verify-code-input">
    <el-input
      :model-value="modelValue"
      :placeholder="placeholder || t('auth.login.code')"
      class="verify-code-input__field"
      @input="onInput"
    />
    <el-button
      class="verify-code-input__btn"
      :disabled="countdown > 0 || sending || loading"
      :loading="sending"
      @click="handleSend"
    >
      <span v-if="countdown > 0">{{ t('auth.code.resend', { n: countdown }) }}</span>
      <span v-else-if="sending">{{ t('auth.code.sent') }}</span>
      <span v-else>{{ t('auth.code.send') }}</span>
    </el-button>
  </div>
</template>

<style lang="scss" scoped>
.verify-code-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 0;
  align-items: flex-start;

  &__field {
    min-width: 0;
  }

  &__btn {
    min-height: 44px;
    border-start-start-radius: 0;
    border-end-start-radius: 0;
    border-inline-start: 0;
    color: var(--color-text-secondary);
    white-space: nowrap;
    min-width: 120px;

    &:hover:not(:disabled) {
      color: var(--color-primary-deep);
      border-color: var(--color-primary);
      background: var(--color-primary-soft);
    }
  }

  :deep(.verify-code-input__field .el-input__wrapper) {
    min-height: 44px;
    border-start-end-radius: 0;
    border-end-end-radius: 0;
  }

  :deep(.verify-code-input__field .el-input__inner) {
    font-size: brand.$font-size-md;
  }

  :deep(.verify-code-input__field .el-input__wrapper.is-focus) {
    position: relative;
    z-index: 1;
  }

  @media (max-width: brand.$bp-mobile) {
    grid-template-columns: minmax(0, 1fr) minmax(104px, auto);

    &__btn {
      min-width: 104px;
      padding-inline: brand.$spacing-3;
    }
  }
}
</style>
