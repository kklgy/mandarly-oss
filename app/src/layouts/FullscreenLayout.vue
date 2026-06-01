<script setup>
/**
 * FullscreenLayout — 整屏壳(plan Wave 1.1 裸壳,Wave 5 H 填充课堂 topbar / 离开 confirm)
 *
 * Wave 1 现状:整屏 wrapper,无任何 chrome
 * 用途:
 *   - /classroom/:orderId(LCIC iframe 占满,Wave 5 H 加 48px 返回栏 + iframe 错误重连)
 *   - /auth/callback/:provider(OAuth 回跳瞬间,无视觉干扰)
 *   - /classroom-stub(开发模拟)
 */
</script>

<template>
  <div class="fullscreen-layout">
    <slot />
  </div>
</template>

<style lang="scss" scoped>
.fullscreen-layout {
  min-height: 100vh;
  /* 现代浏览器优先 100dvh(移动端 URL bar 收起时不闪),fallback 100vh */
  min-height: 100dvh;
  background: var(--color-bg-page);
  display: flex;
  flex-direction: column;
}
</style>
