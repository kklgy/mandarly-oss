<script setup>
/**
 * ReviewStepIndicator — H5 顶部 1-2-3 步骤指示(Wave 5 第 11 轮)
 *
 * Props:
 *  - current : Number 当前步(1-3)
 *  - total   : Number 总步数(默认 3)
 *
 * 视觉:
 *  - 圆点(8px)+ 短横线连接
 *  - 当前步 主色实底 + 白点描边
 *  - 已完成 success 绿(可选,简化为主色更柔和的 deep)
 *  - 未到 灰底
 */

import { computed } from 'vue'

const props = defineProps({
  current: { type: Number, required: true },
  total: { type: Number, default: 3 }
})

const steps = computed(() =>
  Array.from({ length: props.total }, (_, i) => {
    const n = i + 1
    return {
      n,
      state: n < props.current ? 'done' : n === props.current ? 'active' : 'pending'
    }
  })
)
</script>

<template>
  <div class="step-indicator" role="progressbar" :aria-valuenow="current" :aria-valuemin="1" :aria-valuemax="total">
    <template v-for="(step, idx) in steps" :key="step.n">
      <span class="step-indicator__dot" :class="['is-' + step.state]" :aria-current="step.state === 'active' ? 'step' : null">{{ step.n }}</span>
      <span v-if="idx < steps.length - 1" class="step-indicator__line" :class="{ 'is-done': step.state === 'done' }" />
    </template>
  </div>
</template>

<style lang="scss" scoped>
.step-indicator {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;
}

.step-indicator__dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  inline-size: 24px;
  block-size: 24px;
  border-radius: brand.$radius-full;
  font-size: brand.$font-size-xs;
  font-weight: 600;
  line-height: 1;
  background: brand.$color-bg-strong;
  color: brand.$color-text-tertiary;
  border: 2px solid transparent;
  transition: background 0.18s, color 0.18s, border-color 0.18s;

  &.is-active {
    background: brand.$brand-primary;
    color: brand.$color-text-inverse;
    border-color: brand.$brand-primary-soft;
    box-shadow: brand.$shadow-brand;
  }

  &.is-done {
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
    border-color: brand.$brand-primary-soft;
  }
}

.step-indicator__line {
  inline-size: 24px;
  block-size: 2px;
  background: brand.$color-border;
  border-radius: brand.$radius-sm;
  transition: background 0.18s;

  &.is-done {
    background: brand.$brand-primary;
  }
}
</style>
