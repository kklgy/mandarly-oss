<script setup>
// =============================================================================
// <StatCard> — 数字统计卡(Mandarly 共享)
//
// 设计源:
//   - app/src/components/SHARED-API.md §5 StatCard(单一真实源)
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § 8.5 / § ProfileView
//
// 复用场景:
//   - Wave 3 ReferralView 3 数字卡(已邀请 / 已奖励 / 累计奖励)
//   - Wave 3 TeacherStatsView 3 数字卡(平均评分 / 已完成课时 / 评价数)
//   - 后续 admin dashboard 复用
//
// 严格约束:
//   1) i18n 不在组件内 — label 由调用方传字面量
//   2) token only — 0 硬编色 / 字号 / 间距 / 圆角
//   3) RTL — flex/gap + logical properties
//   4) a11y — <dl> / <dt> / <dd> 语义结构
//   5) prefix/suffix 同行;icon 与 value 同行 margin-inline-end
// =============================================================================
import { computed } from 'vue'

const props = defineProps({
  // 主数字(金额类调用方应预格式化或用 #value slot 嵌 PriceTag)
  value: {
    type: [String, Number],
    default: ''
  },
  // 描述文字(i18n 调用方)
  label: {
    type: String,
    required: true
  },
  // EP icon name 或组件;null / 不传 不渲染
  icon: {
    type: [String, Object],
    default: null
  },
  // value 前缀,例 ⭐ / HK$
  prefix: {
    type: String,
    default: ''
  },
  // value 后缀单位,例 % / 节
  suffix: {
    type: String,
    default: ''
  },
  // 密度 variant
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'compact'].includes(v)
  },
  // P2 预留 — 趋势对象 { direction, delta }
  trend: {
    type: Object,
    default: null
  },
  // 语义色(P1+):default 用 ink,primary 用主色-deep
  color: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'primary', 'success', 'warning', 'error'].includes(v)
  }
})

// 是否需要渲染 value 区(slot 优先)
const hasValue = computed(() => props.value !== '' && props.value !== null && props.value !== undefined)
</script>

<template>
  <dl
    class="stat-card"
    :class="[`stat-card--${size}`, `stat-card--color-${color}`]"
  >
    <!-- value 区(prefix + icon + value + suffix 同行) -->
    <dd class="stat-card__value-row">
      <el-icon v-if="icon" class="stat-card__icon">
        <component :is="icon" />
      </el-icon>

      <span v-if="prefix" class="stat-card__prefix">{{ prefix }}</span>

      <slot name="value">
        <span v-if="hasValue" class="stat-card__value">{{ value }}</span>
      </slot>

      <span v-if="suffix" class="stat-card__suffix">{{ suffix }}</span>
    </dd>

    <!-- label -->
    <dt class="stat-card__label">{{ label }}</dt>

    <!-- 额外内容(P2 趋势) -->
    <dd v-if="$slots.extra" class="stat-card__extra">
      <slot name="extra" />
    </dd>
  </dl>
</template>

<style scoped lang="scss">

.stat-card {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-1;
  margin: 0;
  background: var(--color-bg-card);
  box-sizing: border-box;

  // default: 双行 + 边框 + 内边距
  &--default {
    padding: brand.$spacing-4 brand.$spacing-6;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;

    .stat-card__value,
    .stat-card__prefix,
    .stat-card__suffix {
      font-size: brand.$font-size-2xl;
      font-weight: 600;
      line-height: brand.$line-height-tight;
    }
  }

  // compact: 单行 / 无边框 / 紧凑
  &--compact {
    padding-block: brand.$spacing-2;
    padding-inline: 0;
    border: none;
    border-radius: 0;
    flex-direction: row;
    align-items: baseline;
    gap: brand.$spacing-2;
    text-align: start;

    .stat-card__value,
    .stat-card__prefix,
    .stat-card__suffix {
      font-size: brand.$font-size-lg;
      font-weight: 600;
    }
  }

  // 语义色 — 控制 value/prefix/suffix 颜色
  &--color-default {
    .stat-card__value,
    .stat-card__prefix,
    .stat-card__suffix {
      color: brand.$color-text-primary;
    }
  }
  &--color-primary {
    .stat-card__value,
    .stat-card__prefix,
    .stat-card__suffix {
      color: brand.$brand-primary-deep;
    }
  }
  &--color-success {
    .stat-card__value,
    .stat-card__prefix,
    .stat-card__suffix {
      color: brand.$color-success;
    }
  }
  &--color-warning {
    .stat-card__value,
    .stat-card__prefix,
    .stat-card__suffix {
      color: brand.$color-warning;
    }
  }
  &--color-error {
    .stat-card__value,
    .stat-card__prefix,
    .stat-card__suffix {
      color: brand.$color-error;
    }
  }

  &__value-row {
    margin: 0;
    display: flex;
    align-items: baseline;
    gap: brand.$spacing-2;
    flex-wrap: nowrap;
  }

  &__icon {
    font-size: brand.$font-size-2xl;
    color: var(--color-primary);
    margin-inline-end: brand.$spacing-2;
    align-self: center;
  }

  &__label {
    margin: 0;
    font-size: brand.$font-size-base;
    font-weight: 400;
    color: brand.$color-text-secondary;
    line-height: brand.$line-height-base;
  }

  &__extra {
    margin: 0;
    margin-block-start: brand.$spacing-2;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-tertiary;
  }
}
</style>
