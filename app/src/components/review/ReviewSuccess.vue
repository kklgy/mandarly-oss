<script setup>
/**
 * ReviewSuccess — 评价提交成功全屏轻量过渡(Wave 5 第 11 轮)
 *
 * 视觉:✨ + "感谢你的评价" + "你的反馈对我们很重要"
 *  - 主色光晕动效(scale + opacity fade-in)
 *  - 1.5s 后自动跳转(由父级控制 setTimeout)
 *  - 底部小进度条提示倒计时
 *
 * Props:
 *  - duration : Number 自动跳转毫秒(默认 1500)
 *  - showRedirectBar : Boolean 显示底部进度条
 *
 * Events:
 *  - back: 用户主动点"返回我的课"
 */

import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  duration: { type: Number, default: 1500 },
  showRedirectBar: { type: Boolean, default: true }
})

const emit = defineEmits(['back'])

const { t } = useI18n()
const elapsed = ref(0)
let raf = null
let startTs = 0

onMounted(() => {
  if (!props.showRedirectBar) return
  startTs = performance.now()
  const tick = (ts) => {
    elapsed.value = Math.min(props.duration, ts - startTs)
    if (elapsed.value < props.duration) {
      raf = requestAnimationFrame(tick)
    }
  }
  raf = requestAnimationFrame(tick)
})

onUnmounted(() => {
  if (raf) cancelAnimationFrame(raf)
})

const ratio = () => Math.min(1, elapsed.value / props.duration)
</script>

<template>
  <div class="review-success" role="status" aria-live="polite">
    <div class="review-success__halo" aria-hidden="true">
      <span class="review-success__icon">✨</span>
    </div>
    <h2 class="review-success__title">{{ t('review.success.title') }}</h2>
    <p class="review-success__subtitle">{{ t('review.success.subtitle') }}</p>

    <div v-if="showRedirectBar" class="review-success__redirect">
      <p class="review-success__redirect-text">{{ t('review.success.redirect') }}</p>
      <div class="review-success__bar">
        <div class="review-success__bar-fill" :style="{ inlineSize: (ratio() * 100) + '%' }" />
      </div>
    </div>

    <button type="button" class="review-success__back" @click="emit('back')">
      {{ t('review.success.backToOrders') }}
    </button>
  </div>
</template>

<style lang="scss" scoped>
.review-success {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: brand.$spacing-12 brand.$spacing-6;
  gap: brand.$spacing-4;
  animation: success-fade-in 0.36s ease-out;
}

@keyframes success-fade-in {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.review-success__halo {
  position: relative;
  inline-size: 96px;
  block-size: 96px;
  border-radius: brand.$radius-full;
  background: brand.$brand-primary-soft;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-block-end: brand.$spacing-4;
  animation: halo-pulse 1.4s ease-out infinite;
  box-shadow: brand.$shadow-brand;
}

@keyframes halo-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.06); }
}

.review-success__icon {
  font-size: 48px;
  line-height: 1;
}

.review-success__title {
  margin: 0;
  font-size: brand.$font-size-2xl;
  font-weight: 700;
  color: brand.$color-text-primary;
}

.review-success__subtitle {
  margin: 0;
  font-size: brand.$font-size-md;
  color: brand.$color-text-secondary;
}

.review-success__redirect {
  margin-block-start: brand.$spacing-6;
  inline-size: 200px;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
}

.review-success__redirect-text {
  margin: 0;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}

.review-success__bar {
  inline-size: 100%;
  block-size: 3px;
  background: brand.$color-bg-strong;
  border-radius: brand.$radius-sm;
  overflow: hidden;
}

.review-success__bar-fill {
  block-size: 100%;
  background: brand.$brand-primary;
  border-radius: inherit;
  transition: inline-size 0.05s linear;
}

.review-success__back {
  margin-block-start: brand.$spacing-4;
  padding: brand.$spacing-2 brand.$spacing-5;
  background: transparent;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;

  &:hover {
    border-color: brand.$brand-primary;
    color: brand.$brand-primary-deep;
  }
}
</style>
