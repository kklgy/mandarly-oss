<script setup>
/**
 * CurrencySwitcher.vue — § A 币种切换器
 *
 * 设计稿: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § PackageListView 改造 § A
 * 用法:   <CurrencySwitcher v-model="currency" />
 *
 * P0:HKD / USD / CNY 三选一; M5+ 海外: GHS / AED 等扩 options
 *
 * 默认值推断 (调用方可不传 v-model 初值, 直接 v-model 双绑外部 currency):
 *   - 用户手动选择优先
 *   - 无手动选择时按语言推断: en/ar -> USD, zh-CN -> CNY, zh-TW -> HKD
 *
 * 持久化: localStorage('mandarly_pkg_currency')
 *   - 仅用户手动切换时写入,并配套 mandarly_pkg_currency_manual=1
 *   - 切账号清理: 由 useUserStore.logout() 末尾 reset() 时清理(主 agent 收尾)
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: {
    type: String,
    default: 'HKD'
  }
})
const emit = defineEmits(['update:modelValue', 'change'])

const { t, te } = useI18n()
const tt = (key, fb) => (te(key) ? t(key) : fb)

const options = computed(() => [
  { value: 'HKD', label: tt('packages.currency.hkd', 'HKD') },
  { value: 'USD', label: tt('packages.currency.usd', 'USD') },
  { value: 'CNY', label: tt('packages.currency.cny', 'CNY') }
])

const labelText = computed(() => tt('packages.currency.label', '币种'))

function onChange(val) {
  emit('update:modelValue', val)
  emit('change', val)
  if (typeof window !== 'undefined') {
    try {
      localStorage.setItem('mandarly_pkg_currency', val)
      localStorage.setItem('mandarly_pkg_currency_manual', '1')
    } catch (e) {
      // 隐私模式 / quota 满 — 忽略
    }
  }
}
</script>

<template>
  <div class="currency-switcher">
    <span class="currency-switcher__label">{{ labelText }}</span>
    <el-select
      :model-value="modelValue"
      size="small"
      class="currency-switcher__select"
      :aria-label="labelText"
      @update:model-value="onChange"
    >
      <el-option
        v-for="opt in options"
        :key="opt.value"
        :label="opt.label"
        :value="opt.value"
      />
    </el-select>
  </div>
</template>

<script>
/**
 * 默认 currency 推断 — 调用方在 onMounted 之前调用
 * 优先级:用户手动选择 > locale 默认
 */
export function inferDefaultCurrency(locale) {
  if (typeof window !== 'undefined') {
    try {
      const manual = localStorage.getItem('mandarly_pkg_currency_manual') === '1'
      const stored = localStorage.getItem('mandarly_pkg_currency')
      if (manual && stored && ['HKD', 'USD', 'CNY'].includes(stored)) return stored
    } catch (e) {
      // ignore
    }
  }
  if (locale === 'zh-CN') return 'CNY'
  if (locale === 'zh-TW') return 'HKD'
  return 'USD'
}
</script>

<style scoped lang="scss">
.currency-switcher {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;

  &__label {
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
  }

  &__select {
    width: 96px;
  }
}
</style>
