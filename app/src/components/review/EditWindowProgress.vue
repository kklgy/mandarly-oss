<script setup>
/**
 * EditWindowProgress — 编辑窗口进度条(Wave 5 第 11 轮)
 *
 * 视觉:`⏰ 还剩 47 小时编辑       ████████░░░░░░░░░ 65%`
 *  - 主色填充,从右往左减少表示剩余时间
 *  - < 6h 整条变 warning 色
 *  - 已过期:文案"✗ 编辑窗口已过",条灰底
 *
 * Props:
 *  - createdAt        : Date | string ISO 评价首次提交时间
 *  - editableUntilAt  : Date | string ISO 编辑窗口截止(优先用此;若为空回退用 createdAt + windowHours)
 *  - windowHours      : Number 默认 72
 *
 * Computed:
 *  - elapsedRatio: 0-1 已过比例;remainingRatio = 1 - elapsedRatio,主色填充宽
 *  - remainingHours: 剩余整点小时数
 *  - state: 'ok' | 'urgent'(< 6h) | 'expired'
 */

import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { fromUTC } from '@/utils/datetime'

const props = defineProps({
  createdAt: { type: [String, Date, Number], default: null },
  editableUntilAt: { type: [String, Date, Number], default: null },
  windowHours: { type: Number, default: 72 }
})

const { t } = useI18n()

// 每分钟刷新一次,无需秒级
const now = ref(Date.now())
let timer = null

onMounted(() => {
  timer = setInterval(() => { now.value = Date.now() }, 60_000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const startTs = computed(() => {
  if (!props.createdAt) return null
  return fromUTC(props.createdAt).valueOf()
})

const endTs = computed(() => {
  if (props.editableUntilAt) return fromUTC(props.editableUntilAt).valueOf()
  if (startTs.value !== null) return startTs.value + props.windowHours * 3600 * 1000
  return null
})

const remainingHours = computed(() => {
  if (endTs.value === null) return null
  const diffMs = endTs.value - now.value
  return Math.max(0, Math.ceil(diffMs / (3600 * 1000)))
})

const elapsedRatio = computed(() => {
  if (startTs.value === null || endTs.value === null) return 0
  const total = endTs.value - startTs.value
  if (total <= 0) return 1
  const elapsed = now.value - startTs.value
  return Math.max(0, Math.min(1, elapsed / total))
})

const remainingRatio = computed(() => Math.max(0, 1 - elapsedRatio.value))

const state = computed(() => {
  if (remainingHours.value === null) return 'ok'
  if (remainingHours.value <= 0) return 'expired'
  if (remainingHours.value < 6) return 'urgent'
  return 'ok'
})

defineExpose({ state, remainingHours })
</script>

<template>
  <div class="edit-window" :class="['is-' + state]" v-if="startTs !== null">
    <div class="edit-window__header">
      <span class="edit-window__icon" aria-hidden="true">
        <template v-if="state === 'expired'">✗</template>
        <template v-else>⏰</template>
      </span>
      <span class="edit-window__label">
        <template v-if="state === 'expired'">{{ t('review.editWindow.expired') }}</template>
        <template v-else-if="state === 'urgent'">{{ t('review.editWindow.urgent', { hours: remainingHours }) }}</template>
        <template v-else>{{ t('review.editWindow.remaining', { hours: remainingHours }) }}</template>
      </span>
      <span v-if="state !== 'expired'" class="edit-window__pct">
        {{ Math.round(remainingRatio * 100) }}%
      </span>
    </div>

    <div class="edit-window__bar" role="progressbar"
      :aria-valuenow="Math.round(remainingRatio * 100)"
      :aria-valuemin="0" :aria-valuemax="100">
      <div class="edit-window__bar-fill" :style="{ inlineSize: (remainingRatio * 100) + '%' }" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.edit-window {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
  padding: brand.$spacing-3 brand.$spacing-4;
  background: brand.$brand-primary-soft;
  border-radius: brand.$radius-base;
  border: 1px solid transparent;
  transition: background 0.18s, border-color 0.18s;

  &.is-urgent {
    background: rgba(250, 173, 20, 0.10);
    border-color: rgba(250, 173, 20, 0.32);
  }

  &.is-expired {
    background: brand.$color-bg-strong;
    color: brand.$color-text-tertiary;
  }
}

.edit-window__header {
  display: flex;
  align-items: center;
  gap: brand.$spacing-2;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-primary;

  .is-urgent & { color: brand.$color-warning; font-weight: 500; }
  .is-expired & { color: brand.$color-text-tertiary; }
}

.edit-window__icon {
  font-size: brand.$font-size-base;
  line-height: 1;
}

.edit-window__label {
  flex: 1;
}

.edit-window__pct {
  font-variant-numeric: tabular-nums;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}

.edit-window__bar {
  position: relative;
  block-size: 4px;
  background: rgba(0, 0, 0, 0.06);
  border-radius: brand.$radius-sm;
  overflow: hidden;
}

.edit-window__bar-fill {
  block-size: 100%;
  background: brand.$brand-primary;
  border-radius: inherit;
  transition: inline-size 0.36s ease, background 0.18s;

  .is-urgent & { background: brand.$color-warning; }
  .is-expired & { background: brand.$color-text-tertiary; }
}
</style>
