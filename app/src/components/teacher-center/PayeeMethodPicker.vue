<script setup>
/**
 * PayeeMethodPicker — 收款方式选择(M6 spec §5.4)
 *
 * 5 method:wechat / alipay / paypal / bank_card / other
 * UI:radio 横向铺一行(H5 上 wrap;PC ≥ 768 单行)
 *
 * Props/Emits:
 *   - v-model:string method 值
 *   - disabled: 提交中 disable
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: { type: String, default: '' },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

const METHODS = [
  { value: 'wechat',    i18n: 'teacherCenter.withdrawal.apply.payeeMethodWechat',   icon: '💬' },
  { value: 'alipay',    i18n: 'teacherCenter.withdrawal.apply.payeeMethodAlipay',   icon: '🅰️' },
  { value: 'paypal',    i18n: 'teacherCenter.withdrawal.apply.payeeMethodPaypal',   icon: '💳' },
  { value: 'bank_card', i18n: 'teacherCenter.withdrawal.apply.payeeMethodBankCard', icon: '🏦' },
  { value: 'other',     i18n: 'teacherCenter.withdrawal.apply.payeeMethodOther',    icon: '⋯' }
]

const selected = computed(() => props.modelValue)

function onSelect(m) {
  if (props.disabled) return
  emit('update:modelValue', m.value)
}
</script>

<template>
  <div class="payee-method" role="radiogroup">
    <button
      v-for="m in METHODS"
      :key="m.value"
      type="button"
      class="payee-method__item"
      :class="{ 'is-active': selected === m.value, 'is-disabled': disabled }"
      role="radio"
      :aria-checked="selected === m.value"
      :disabled="disabled"
      @click="onSelect(m)"
    >
      <span class="payee-method__icon" aria-hidden="true">{{ m.icon }}</span>
      <span class="payee-method__label">{{ t(m.i18n) }}</span>
    </button>
  </div>
</template>

<style lang="scss" scoped>
.payee-method {
  display: flex;
  flex-wrap: wrap;
  gap: brand.$spacing-2;

  &__item {
    flex: 1 1 calc(50% - #{brand.$spacing-2});
    min-width: 100px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: brand.$spacing-2;
    padding: brand.$spacing-3 brand.$spacing-3;
    background: brand.$color-bg-card;
    border: 1.5px solid brand.$color-border;
    border-radius: brand.$radius-base;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-base;
    cursor: pointer;
    transition: border-color 0.15s, background 0.15s, color 0.15s;

    @media (min-width: brand.$bp-tablet) {
      flex: 1 1 0;
    }

    &:hover:not(.is-disabled) {
      border-color: brand.$brand-primary;
      background: brand.$brand-primary-soft;
    }

    &.is-active {
      border-color: brand.$brand-primary;
      background: brand.$brand-primary-soft;
      color: brand.$brand-primary-deep;
      font-weight: 600;
    }

    &.is-disabled {
      cursor: not-allowed;
      opacity: 0.6;
    }
  }

  &__icon {
    font-size: brand.$font-size-md;
    line-height: 1;
  }
}
</style>
