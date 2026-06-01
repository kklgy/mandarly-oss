<script setup>
// =============================================================================
// <EmptyState> — 通用空态组件(Mandarly 共享)
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md §8.3 EmptyState
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md §F TeacherListView 空态
//   - docs/frontend/ui-style-guide-v1.md(token / 组件库选型 / RTL / i18n)
//
// 复用场景:
//   - Wave 4 TeacherList 搜索 / 筛选无结果(variant=no-result)
//   - Wave 2 TeacherDetail 无评价(variant=no-data)
//   - Wave 3 Profile 无邀请(variant=no-data,加 CTA 复制链接)
//   - Wave 3 MyOrders 6 个空 tab(compact=true,variant=no-data)
//   - 通用网络错误(variant=error)
//   - P1+ 收藏夹空(variant=no-favorite)
//
// 严格约束:
//   1) i18n 不在组件内 — title / description / actionText 由调用方传字面量
//   2) token only — 0 硬编码颜色 / 字号 / 间距 / 圆角
//   3) action-link 用 <router-link>(SEO + 右键打开新 tab)
//   4) RTL — 文字 center 不受影响,按钮 / 容器用 logical properties
//   5) a11y — role=status + aria-live=polite
// =============================================================================
import { computed } from 'vue'
import { Box, Search, WarningFilled, Star } from '@element-plus/icons-vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'no-data',
    validator: (v) => ['no-data', 'no-result', 'error', 'no-favorite'].includes(v)
  },
  // EP icon name(@element-plus/icons-vue)或组件;为空时按 variant 兜底
  icon: {
    type: [String, Object],
    default: ''
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  // 主 CTA 文案;为空 / 不传 → 不显示按钮(也不显示 #action slot 的兜底)
  actionText: {
    type: String,
    default: ''
  },
  // 主 CTA 跳转路由(与 actionText 配套)
  actionLink: {
    type: [String, Object],
    default: ''
  },
  // 紧凑模式:嵌入 tab / 卡片 / 抽屉时高度从 ~280px → ~160px,无暖米底
  compact: {
    type: Boolean,
    default: false
  }
})

// variant 默认 icon 兜底表(prop.icon 优先,#icon slot 优先级最高)
const variantIconMap = {
  'no-data': Box,
  'no-result': Search,
  'error': WarningFilled,
  'no-favorite': Star
}

const resolvedIcon = computed(() => props.icon || variantIconMap[props.variant] || Box)
</script>

<template>
  <div
    class="empty-state"
    :class="{ 'empty-state--compact': compact, [`empty-state--${variant}`]: true }"
    role="status"
    aria-live="polite"
  >
    <div class="empty-state__icon-wrap">
      <slot name="icon">
        <el-icon class="empty-state__icon">
          <component :is="resolvedIcon" />
        </el-icon>
      </slot>
    </div>

    <h3 class="empty-state__title">{{ title }}</h3>

    <p v-if="description" class="empty-state__desc">{{ description }}</p>

    <div v-if="$slots.action || (actionText && actionLink)" class="empty-state__action">
      <slot name="action">
        <router-link v-if="actionText && actionLink" :to="actionLink" class="empty-state__action-link">
          <el-button type="primary">{{ actionText }}</el-button>
        </router-link>
      </slot>
    </div>
  </div>
</template>

<style scoped lang="scss">

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding-block: brand.$spacing-12;
  padding-inline: brand.$spacing-6;
  background: var(--color-primary-soft);
  border-radius: var(--radius-lg);
  min-height: 280px;
  box-sizing: border-box;

  // 紧凑模式:嵌入 tab / 卡片内,无暖米底,缩短一半 padding
  &--compact {
    padding-block: brand.$spacing-6;
    padding-inline: brand.$spacing-3;
    background: transparent;
    border-radius: 0;
    min-height: 160px;
  }

  // 错误态可微调色调,但仍走 token(避免硬编)
  &--error .empty-state__icon-wrap {
    box-shadow: 0 0 0 8px var(--color-error-soft);
  }

  &__icon-wrap {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 120px;
    height: 120px;
    border-radius: var(--radius-full);
    background: var(--color-bg-card);
    box-shadow: var(--shadow-base);
    margin-block-end: brand.$spacing-5;
    color: var(--color-primary);

    .empty-state--compact & {
      width: 64px;
      height: 64px;
      margin-block-end: brand.$spacing-3;
      box-shadow: none;
      background: transparent;
    }
  }

  &__icon {
    font-size: 56px;
    color: var(--color-primary);

    .empty-state--compact & {
      font-size: 32px;
    }
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: var(--color-text-primary);
    line-height: brand.$line-height-tight;
  }

  &__desc {
    margin-block-start: brand.$spacing-2;
    margin-block-end: 0;
    font-size: brand.$font-size-base;
    color: var(--color-text-secondary);
    line-height: brand.$line-height-base;
    max-width: 360px;
  }

  &__action {
    margin-block-start: brand.$spacing-5;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: brand.$spacing-3;
    flex-wrap: wrap;

    .empty-state--compact & {
      margin-block-start: brand.$spacing-3;
    }
  }

  &__action-link {
    text-decoration: none;
  }
}
</style>
