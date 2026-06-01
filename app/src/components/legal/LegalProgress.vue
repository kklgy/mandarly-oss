<script setup>
/**
 * LegalProgress — 法务页顶部 sticky 阅读进度条(Wave 5 第 12 轮)
 *
 *  - position: sticky; inset-block-start: 64px / H5 48px(让位 AppHeader)
 *  - height: 4px,主色填充
 *  - progress < 1% 用 v-show 隐藏(避免顶部就有进度感)
 *
 * Props:
 *  - progress : Number 0-100
 */

defineProps({
  progress: { type: Number, required: true }
})
</script>

<template>
  <div
    v-show="progress >= 1"
    class="legal-progress"
    role="progressbar"
    :aria-valuenow="Math.round(progress)"
    aria-valuemin="0"
    aria-valuemax="100"
    :aria-label="$t('legal.progressLabel')"
  >
    <div class="legal-progress__fill" :style="{ inlineSize: progress + '%' }" />
  </div>
</template>

<style lang="scss" scoped>
.legal-progress {
  position: sticky;
  inset-block-start: 64px;     // PC AppHeader 64px
  inset-inline-start: 0;
  inline-size: 100%;
  block-size: 4px;
  background: rgba(0, 0, 0, 0.04);
  z-index: brand.$z-sticky;

  @media (max-width: brand.$bp-tablet - 1) {
    inset-block-start: 48px;   // H5 AppHeader 48px(隐藏时 0,但 sticky 仍以 viewport 顶为准则不闪)
  }
}

.legal-progress__fill {
  block-size: 100%;
  background: brand.$brand-primary;
  transition: inline-size 0.1s linear;
}

@media print {
  .legal-progress {
    display: none !important;
  }
}
</style>
