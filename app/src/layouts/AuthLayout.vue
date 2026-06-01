<script setup>
/**
 * AuthLayout — 鉴权页壳
 *
 * Wave 5 G(P2-1)装配:
 *   PC ≥ 1024: 双栏 50/50 — 左栏 AuthHero(品牌氛围 + 插画 + 信任信号),右栏卡片表单
 *   < 1024:    单栏沿用 Wave 1 现状(顶部精简 logo + lang),AuthHero 整块隐藏
 *
 * 视觉契约: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § Auth 页 PC 双栏氛围版(第 9 轮定版)
 *
 * 不挂 AppHeader / AppFooter / AppTabBar(由 route.meta.layout='auth' 路由独占壳)
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import AuthHero from '@/components/auth/AuthHero.vue'
import { useBreakpoint } from '@/composables/useBreakpoint'

const route = useRoute()
// 1024px 以上启用双栏氛围(Wave 1 沉淀的 useBreakpoint,统一断点逻辑)
const { isPC: isWide } = useBreakpoint(1024)

// 按路由名映射 hero variant — 4 张插画 + 4 套 slogan 文案
const heroVariant = computed(() => {
  switch (route.name) {
    case 'Login': return 'login'
    case 'Register': return 'register'
    case 'TeacherRegister': return 'teacherRegister'
    case 'ResetPassword': return 'resetPassword'
    default: return 'login'
  }
})
</script>

<template>
  <div class="auth-layout" :class="{ 'auth-layout--wide': isWide }">
    <!-- 左栏氛围(仅 PC ≥ 1024) -->
    <AuthHero v-if="isWide" :variant="heroVariant" class="auth-layout__hero" />

    <!-- 右栏 / 单栏 主区 -->
    <div class="auth-layout__pane">
      <!-- 顶部条 — H5 模式下显示 logo + lang;PC 模式下右上角浮动 lang -->
      <div class="auth-layout__topbar" :class="{ 'is-corner': isWide }">
        <router-link
          v-if="!isWide"
          to="/"
          class="auth-layout__brand"
          aria-label="Mandarly home"
        >
          <img
            src="/logo-256.png?v=v20260526-mark-only"
            alt="Mandarly"
            class="auth-layout__logo"
            @error="(e) => e.target.style.display='none'"
          />
          <span class="auth-layout__brand-name">Mandarly</span>
        </router-link>
        <LanguageSwitcher class="auth-layout__lang" />
      </div>

      <main class="auth-layout__main">
        <slot />
      </main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.auth-layout {
  min-height: 100vh;
  background: var(--color-bg-page);
  display: flex;
  flex-direction: column;

  // PC ≥ 1024 双栏装配
  &--wide {
    display: grid;
    grid-template-columns: 1fr 1fr;
    background: var(--color-bg-card);
  }

  &__hero {
    grid-column: 1;
  }

  &__pane {
    position: relative;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background: var(--color-bg-card);
  }

  &__topbar {
    height: 56px;
    padding-inline: brand.$spacing-6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: brand.$spacing-3;
    background: transparent;

    @media (max-width: brand.$bp-tablet) {
      padding-inline: brand.$spacing-4;
      height: 48px;
    }

    // PC 模式: 右上角浮 lang(无 logo,Logo 已在 hero)
    &.is-corner {
      position: absolute;
      inset-block-start: 0;
      inset-inline-end: 0;
      inset-inline-start: auto;
      width: auto;
      height: auto;
      padding: brand.$spacing-4;
      justify-content: flex-end;
      z-index: brand.$z-fixed;
    }
  }

  &__brand {
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-2;
    text-decoration: none;
    color: var(--color-text-primary);
  }

  &__logo {
    height: 32px;
    width: auto;
  }

  &__brand-name {
    font-family: var(--font-family-heading-en);
    font-style: italic;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    direction: ltr;
    unicode-bidi: isolate;
  }

  &__main {
    flex: 1;
    position: relative;
    isolation: isolate;
    // D26-H:overflow hidden 避免 ::before/::after blob 撑大 body 出空白
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: brand.$spacing-8 brand.$spacing-4;

    // D26:右侧表单区不再纯白裸露 — 加 mesh soft + 装饰 blob(D26-H 浓度提高)
    background: brand.$mesh-login-soft;

    // 装饰 blob 1(右上,主色 gold 光晕)— D26-H 浓度 0.18 → 0.38
    &::before {
      content: '';
      position: absolute;
      inset-block-start: -120px;
      inset-inline-end: -100px;
      width: 360px;
      height: 360px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(172, 114, 44, 0.38) 0%, transparent 70%);
      filter: blur(50px);
      z-index: -1;
      pointer-events: none;
    }

    // 装饰 blob 2(左下,暖珊瑚)— D26-H 浓度 0.15 → 0.32
    &::after {
      content: '';
      position: absolute;
      inset-block-end: -140px;
      inset-inline-start: -80px;
      width: 320px;
      height: 320px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(204, 120, 92, 0.32) 0%, transparent 70%);
      filter: blur(50px);
      z-index: -1;
      pointer-events: none;
    }

    @media (min-width: brand.$bp-laptop) {
      padding: brand.$spacing-12 brand.$spacing-8;
    }

    // 表单内容置于 blob 之上
    > * {
      position: relative;
      z-index: 1;
    }
  }
}
</style>
