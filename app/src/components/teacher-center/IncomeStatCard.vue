<script setup>
/**
 * IncomeStatCard — 教师收入 4 卡片之一(M6 spec §5.3)
 *
 * 与现有共享 <StatCard> 不同的是:
 *   - 金额场景固定 USD 前缀 + 大数字格式化(BigDecimal 字符串 → "1,234.56")
 *   - 主卡(可提现)右上挂「申请提现 →」CTA 按钮槽 #action
 *   - 副卡支持 hint 二行小字
 *
 * Props:
 *   - label / amount / variant(default | primary | warning | info) / hint
 *   - 主卡用 variant='primary' 渲染品牌底
 */
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  amount: { type: [String, Number], default: 0 },
  hint: { type: String, default: '' },
  variant: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'primary', 'warning', 'info'].includes(v)
  },
  currencyPrefix: { type: String, default: 'US$ ' }
})

// 金额格式化(保留 2 位 + 千分位)
const formatted = computed(() => {
  const n = Number(props.amount)
  if (!isFinite(n)) return '0.00'
  return n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})
</script>

<template>
  <div class="income-stat" :class="[`income-stat--${variant}`]">
    <div class="income-stat__head">
      <span class="income-stat__label">{{ label }}</span>
      <slot name="action" />
    </div>

    <div class="income-stat__value-row">
      <span class="income-stat__currency">{{ currencyPrefix }}</span>
      <strong class="income-stat__value">{{ formatted }}</strong>
    </div>

    <p v-if="hint" class="income-stat__hint">{{ hint }}</p>
  </div>
</template>

<style lang="scss" scoped>
.income-stat {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
  padding: brand.$spacing-5 brand.$spacing-5;
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-lg;
  box-shadow: brand.$shadow-sm;
  min-height: 120px;
  transition: box-shadow 0.15s;

  &:hover {
    box-shadow: brand.$shadow-md;
  }

  &--primary {
    background: brand.$brand-gradient;
    color: brand.$color-text-inverse;
    border-color: transparent;

    .income-stat__label,
    .income-stat__currency,
    .income-stat__value,
    .income-stat__hint {
      color: brand.$color-text-inverse;
    }

    .income-stat__hint {
      opacity: 0.85;
    }
  }

  // D17 UX 收敛:warning / info 不再用高亮底,统一白底默认 border;
  // 只保留 primary 橙渐变作为主 CTA 强调(避免 3 种暖色挤在一起稀释 primary)。
  // 语义差异化交给 label 文案(如「在途 T+7」「申请中冻结」)+ hint 二行小字承载。
  &--warning,
  &--info {
    background: brand.$color-bg-card;
    border-color: brand.$color-border;
  }

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: brand.$spacing-2;
  }

  &__label {
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
    font-weight: 500;
  }

  &__value-row {
    display: flex;
    align-items: baseline;
    gap: brand.$spacing-1;
  }

  &__currency {
    font-size: brand.$font-size-base;
    color: brand.$color-text-secondary;
    font-weight: 600;
  }

  &__value {
    font-size: brand.$font-size-2xl;
    font-weight: 700;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;
    font-variant-numeric: tabular-nums;
  }

  &__hint {
    margin: 0;
    font-size: brand.$font-size-xs;
    color: brand.$color-text-tertiary;
    line-height: brand.$line-height-base;
  }
}
</style>
