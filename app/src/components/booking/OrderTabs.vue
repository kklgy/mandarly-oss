<script setup>
// =============================================================================
// <OrderTabs> — MyOrders 6-tab 横向 sticky 切换器
// =============================================================================
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md §"### MyOrdersView 拆分" → OrderTabs
//
// 严格约束:
//   1) tab 顺序 plan 锁定:upcoming / toReview / finished / cancelled / refunding / all
//   2) 数字徽章只在有 count > 0 时显示(upcoming / toReview / refunding 三个)
//   3) RTL 自动镜像:flex 容器 + logical properties
//   4) H5 横滑:overflow-x:auto + scroll-snap;隐藏滚动条
//   5) sticky top:0(MyOrdersView shell 控制 H5 是否在 AppHeader 之下)
//   6) 切 tab → emit update:modelValue,父级 router.replace 同步 ?tab=
//   7) 0 硬编品牌色 / px / 字号(token only)
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  },
  // 由 useBookingCountsStore 透传:{ upcoming: N, toReview: N, refunding: N }
  // 0/null/undefined 均不显示徽章
  counts: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

const TABS = ['upcoming', 'toReview', 'finished', 'cancelled', 'refunding', 'all']

const tabs = computed(() =>
  TABS.map((key) => ({
    key,
    label: t(`orders.tabs.${key}`),
    badge: badgeFor(key)
  }))
)

function badgeFor(key) {
  // 仅 upcoming / toReview / refunding 三个 tab 显示徽章;0 不显示(保留 falsy)
  if (key === 'upcoming') return props.counts.upcoming || 0
  if (key === 'toReview') return props.counts.toReview || 0
  if (key === 'refunding') return props.counts.refunding || 0
  return 0
}

function select(key) {
  if (key === props.modelValue) return
  emit('update:modelValue', key)
}
</script>

<template>
  <nav class="order-tabs" role="tablist" :aria-label="t('orders.title')">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      role="tab"
      class="order-tabs__tab"
      :class="{ 'order-tabs__tab--active': tab.key === modelValue }"
      :aria-selected="tab.key === modelValue"
      @click="select(tab.key)"
    >
      <span class="order-tabs__label">{{ tab.label }}</span>
      <el-badge
        v-if="tab.badge > 0"
        :value="tab.badge"
        :max="99"
        class="order-tabs__badge"
      />
    </button>
  </nav>
</template>

<style lang="scss" scoped>
.order-tabs {
  position: sticky;
  inset-block-start: 0;
  z-index: brand.$z-sticky;
  display: flex;
  flex-wrap: nowrap;
  gap: brand.$spacing-4;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  padding: brand.$spacing-2 brand.$spacing-4;
  background: brand.$color-bg-card;
  border-block-end: 1px solid brand.$color-border;

  // 隐藏滚动条但保留滚动
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }

  @media (min-width: brand.$bp-tablet) {
    gap: brand.$spacing-6;
    padding: brand.$spacing-3 brand.$spacing-6;
  }
}

.order-tabs__tab {
  flex-shrink: 0;
  scroll-snap-align: center;
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;
  padding: brand.$spacing-2 brand.$spacing-1;
  background: transparent;
  border: none;
  border-block-end: 2px solid transparent;
  margin-block-end: -1px;
  color: brand.$color-text-secondary;
  font: inherit;
  font-size: brand.$font-size-base;
  font-weight: 500;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.15s ease, border-color 0.15s ease;

  &:hover {
    color: brand.$color-text-primary;
  }

  &:focus-visible {
    outline: none;
    box-shadow: brand.$ring-focus;
    border-radius: brand.$radius-sm;
  }

  &--active {
    color: brand.$brand-primary-deep;
    border-block-end-color: brand.$brand-primary;
    font-weight: 600;
  }
}

// EP <el-badge> 内部红点已在 brand-token 体系外,这里给容器一点节制让它不撑大 tab 行
.order-tabs__badge {
  // EP 默认使用绝对定位 + 右上角偏移 — 我们只用纯数字徽章贴 label 后面
  :deep(.el-badge__content) {
    position: static;
    transform: none;
    background: brand.$brand-primary;
    color: brand.$color-text-inverse;
    border: none;
    padding: 0 brand.$spacing-2;
    font-size: brand.$font-size-xs;
    font-weight: 600;
    line-height: 1.5;
    border-radius: brand.$radius-pill;
  }
}
</style>
