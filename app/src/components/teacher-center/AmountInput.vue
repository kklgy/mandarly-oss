<script setup>
/**
 * AmountInput — 金额输入(M6 spec §5.4)
 *
 * - USD 输入 + 右侧单位
 * - 前端校验:>= min,<= max(available)
 * - 「全部」按钮 — 一键填 max
 *
 * Props:
 *   - v-model: number | ''(空串表示未输入)
 *   - min / max: 数值边界
 *   - placeholder, disabled
 *
 * Emits:
 *   - update:modelValue
 *   - validate: { valid, reason }(每次输入后实时触发)
 */
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: { type: [Number, String], default: '' },
  min: { type: Number, default: 0 },
  max: { type: Number, default: Infinity },
  placeholder: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  unit: { type: String, default: 'USD' }
})

const emit = defineEmits(['update:modelValue', 'validate'])

const { t } = useI18n()

// 校验态
const validation = computed(() => {
  const v = props.modelValue
  if (v === '' || v == null) {
    return { valid: false, reason: 'empty' }
  }
  const num = Number(v)
  if (!isFinite(num) || num <= 0) {
    return { valid: false, reason: 'invalid', message: t('teacherCenter.errors.amountInvalid') }
  }
  if (num < props.min) {
    return { valid: false, reason: 'belowMin', message: t('teacherCenter.errors.withdrawalBelowMin', { min: props.min }) }
  }
  if (num > props.max) {
    return { valid: false, reason: 'aboveMax', message: t('teacherCenter.errors.withdrawalAboveBalance') }
  }
  return { valid: true }
})

watch(validation, (v) => emit('validate', v), { immediate: true })

function onInput(e) {
  // 只允许数字 + 小数点;过滤其它字符
  let val = e.target.value.replace(/[^\d.]/g, '')
  // 限制最多一位小数点 + 2 位小数
  const parts = val.split('.')
  if (parts.length > 2) val = parts[0] + '.' + parts.slice(1).join('')
  if (parts[1] && parts[1].length > 2) {
    val = parts[0] + '.' + parts[1].slice(0, 2)
  }
  emit('update:modelValue', val)
}

function onAllIn() {
  if (props.disabled || !isFinite(props.max)) return
  emit('update:modelValue', String(props.max))
}
</script>

<template>
  <div class="amount-input" :class="{ 'is-invalid': !validation.valid && modelValue !== '' }">
    <input
      type="text"
      inputmode="decimal"
      class="amount-input__field"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      @input="onInput"
    />
    <span class="amount-input__unit">{{ unit }}</span>
    <button
      type="button"
      class="amount-input__all-in"
      :disabled="disabled || !isFinite(max) || max <= 0"
      @click="onAllIn"
    >
      {{ t('teacherCenter.withdrawal.apply.amountAllInBtn') }}
    </button>
  </div>
  <p
    v-if="!validation.valid && validation.message && modelValue !== ''"
    class="amount-input__error"
  >
    {{ validation.message }}
  </p>
</template>

<style lang="scss" scoped>
.amount-input {
  display: flex;
  align-items: stretch;
  background: brand.$color-bg-card;
  border: 1.5px solid brand.$color-border;
  border-radius: brand.$radius-base;
  overflow: hidden;
  transition: border-color 0.15s, box-shadow 0.15s;

  &:focus-within {
    border-color: brand.$brand-primary;
    box-shadow: brand.$ring-focus;
  }

  &.is-invalid {
    border-color: brand.$color-error;
  }

  &__field {
    flex: 1;
    border: none;
    outline: none;
    padding: brand.$spacing-3 brand.$spacing-4;
    font-size: brand.$font-size-md;
    color: brand.$color-text-primary;
    background: transparent;
    font-variant-numeric: tabular-nums;
    min-width: 0;

    // iOS Safari 防自动 zoom
    @media (max-width: brand.$bp-tablet) {
      font-size: 16px;
    }

    &:disabled {
      color: brand.$color-text-tertiary;
      cursor: not-allowed;
    }
  }

  &__unit {
    display: inline-flex;
    align-items: center;
    padding: 0 brand.$spacing-3;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
    background: brand.$color-bg-page;
    border-inline-start: 1px solid brand.$color-border-light;
  }

  &__all-in {
    border: none;
    background: brand.$color-bg-page;
    color: brand.$brand-primary-deep;
    font-weight: 600;
    padding: 0 brand.$spacing-4;
    cursor: pointer;
    font-size: brand.$font-size-sm;
    border-inline-start: 1px solid brand.$color-border-light;
    transition: background 0.15s;

    &:hover:not(:disabled) {
      background: brand.$brand-primary-soft;
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.5;
    }
  }

  &__error {
    margin: brand.$spacing-1 0 0;
    color: brand.$color-error;
    font-size: brand.$font-size-sm;
  }
}
</style>
