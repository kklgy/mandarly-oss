<script setup>
// =============================================================================
// <PriceTag> — 多币种价格格式化(Mandarly 共享)
//
// 契约: app/src/components/SHARED-API.md §3
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md §8.4
//
// 严格约束:
//   1) i18n § 0.3 白名单例外: amount=0 / free=true 时显示"免费"翻译
//      (key 走 common.price.free, 4 套语言)
//   2) token only — 0 硬编码颜色 / 字号 / 间距
//   3) RTL — 容器 text-align: end, 数字保持 LTR(浏览器自动 bidi)
//   4) 千分位用 Intl.NumberFormat,前缀手动拼(避免浏览器返 'HKD' 而非 'HK$')
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Lock } from '@element-plus/icons-vue'

const props = defineProps({
  amount: {
    type: [Number, String],
    default: null
  },
  currency: {
    type: String,
    default: 'HKD',
    validator: (v) => ['HKD', 'USD', 'CNY', 'GHS', 'AED'].includes(v)
  },
  period: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'large', 'inline'].includes(v)
  },
  free: {
    type: Boolean,
    default: false
  },
  locked: {
    type: Boolean,
    default: false
  },
  lockedLabel: {
    type: String,
    default: ''
  },
  // P2 预留, P0/P1 不传; 划线对比价
  discountFromAmount: {
    type: [Number, String, null],
    default: null
  }
})

const { t, te, locale } = useI18n()

// 币种前缀表
const currencyPrefix = {
  HKD: 'HK$',
  USD: 'US$',
  CNY: '¥',
  GHS: 'GH₵',
  AED: 'AED '
}

// 数字归一化(BigDecimal 字符串 / number 都接受)
function toNumber(v) {
  if (v == null || v === '') return NaN
  return typeof v === 'number' ? v : Number(v)
}

const numericAmount = computed(() => toNumber(props.amount))

const isFree = computed(() => {
  if (props.free) return true
  // amount === 0 自动显示"免费"
  return numericAmount.value === 0
})

const freeLabel = computed(() => {
  // 白名单 i18n key: common.price.free
  return te('common.price.free') ? t('common.price.free') : 'Free'
})

const lockedText = computed(() => {
  return props.lockedLabel || (te('common.price.loginToView') ? t('common.price.loginToView') : 'Login to view price')
})

const lockedAmount = computed(() => {
  const prefix = currencyPrefix[props.currency] ?? ''
  return `${prefix} ****`
})

const formattedAmount = computed(() => {
  const n = numericAmount.value
  if (Number.isNaN(n)) return ''
  const prefix = currencyPrefix[props.currency] ?? ''
  try {
    const num = new Intl.NumberFormat(locale.value, { style: 'decimal' }).format(n)
    return `${prefix}${num}`
  } catch {
    return `${prefix}${n}`
  }
})

const numericDiscountFrom = computed(() => toNumber(props.discountFromAmount))
const hasDiscount = computed(() => {
  const n = numericDiscountFrom.value
  return !Number.isNaN(n) && n > numericAmount.value
})
const formattedDiscountFrom = computed(() => {
  if (!hasDiscount.value) return ''
  const prefix = currencyPrefix[props.currency] ?? ''
  try {
    const num = new Intl.NumberFormat(locale.value, { style: 'decimal' }).format(numericDiscountFrom.value)
    return `${prefix}${num}`
  } catch {
    return `${prefix}${numericDiscountFrom.value}`
  }
})
</script>

<template>
  <span class="price-tag" :class="[`price-tag--${size}`, locked ? 'price-tag--locked' : '']">
    <template v-if="locked">
      <span class="price-tag__locked-glass" aria-hidden="true">
        <span class="price-tag__amount">{{ lockedAmount }}</span>
        <span v-if="period && size !== 'inline'" class="price-tag__period">{{ period }}</span>
      </span>
      <span class="price-tag__locked-label">
        <el-icon class="price-tag__locked-icon"><Lock /></el-icon>
        <span>{{ lockedText }}</span>
      </span>
    </template>
    <template v-else-if="isFree">
      <span class="price-tag__amount">{{ freeLabel }}</span>
    </template>
    <template v-else>
      <del v-if="hasDiscount" class="price-tag__del">{{ formattedDiscountFrom }}</del>
      <span class="price-tag__amount">{{ formattedAmount }}</span>
      <span v-if="period && size !== 'inline'" class="price-tag__period">{{ period }}</span>
    </template>
  </span>
</template>

<style scoped lang="scss">

.price-tag {
  display: inline-flex;
  align-items: baseline;
  gap: brand.$spacing-2;
  text-align: end;

  &__amount {
    color: brand.$color-text-primary;
    font-weight: 600;
  }

  &__period {
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-base;
    font-weight: 400;
  }

  &__del {
    color: brand.$color-text-tertiary;
    font-size: brand.$font-size-base;
    margin-inline-end: brand.$spacing-2;
    font-weight: 400;
  }

  &--default {
    .price-tag__amount {
      font-size: brand.$font-size-md;
    }
  }

  &--large {
    .price-tag__amount {
      font-size: brand.$font-size-2xl;
    }
  }

  &--inline {
    .price-tag__amount {
      font-size: inherit;
      color: inherit;
    }
  }

  &--locked {
    position: relative;
    align-items: center;
    padding: brand.$spacing-2 brand.$spacing-3;
    border-radius: brand.$radius-base;
    border: 1px solid brand.$color-border;
    background: brand.$color-bg-card;
    overflow: hidden;

    .price-tag__locked-glass {
      display: inline-flex;
      align-items: baseline;
      gap: brand.$spacing-2;
      filter: blur(3px);
      opacity: 0.72;
      user-select: none;
    }

    .price-tag__locked-label {
      position: absolute;
      inset: 0;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: brand.$spacing-1;
      padding-inline: brand.$spacing-2;
      background: rgba(255, 255, 255, 0.72);
      backdrop-filter: blur(8px);
      color: brand.$brand-primary-deep;
      font-size: brand.$font-size-sm;
      font-weight: 600;
      white-space: nowrap;
    }

    .price-tag__locked-icon {
      font-size: brand.$font-size-base;
    }
  }
}
</style>
