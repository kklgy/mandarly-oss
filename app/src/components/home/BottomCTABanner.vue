<script setup>
// =============================================================================
// <BottomCTABanner> — HomeView § 7 底部 CTA banner(D26 v2 质感升级)
//
// 升级点:
//   - 背景改 mesh-bottom-cta(深色多色 mesh,替代 brand-gradient-bottom-cta)
//   - 装饰 blob 保留并增强(2 个,跟 HeroSection 同套路)
//   - 加 grain overlay(opacity 0.05,mix-blend-mode overlay)
//   - 加 scroll-reveal(IntersectionObserver 一次性)
//   - 大字 H2 改 font-display serif italic + font-size-display-lg(48px PC)
//   - 双 CTA pill 升级 v2 §8.4 四态:hover translateY -2px + shadow-glow-primary; active translateY 0 + shadow-v2-sm
//   - prefers-reduced-motion 兜底
//
// 设计源:docs/frontend/visual-reference/DESIGN-mandarly-v2.md §9.1 / §8.4
//
// 文案 i18n:home.cta.{title|sub|btn|teacherJoin}
// 路由:主 CTA → /level-check,次 CTA → /teacher/register
//
// 严格约束:
//   1) token only
//   2) RTL — font-display italic 仅 :not(:lang(ar));CTA gap 在 RTL 自动反
//   3) <router-link> 包按钮
//   4) WCAG AA — white 700 weight on mesh 最深点 #0E2241(对比度 ≥ 4.5)
// =============================================================================
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

// ---------- scroll-reveal ----------
const sectionRef = ref(null)
const visible = ref(false)
let io = null

onMounted(() => {
  io = new IntersectionObserver(([entry]) => {
    if (entry.isIntersecting) {
      visible.value = true
      io.disconnect()
    }
  }, { threshold: 0.15 })
  if (sectionRef.value) io.observe(sectionRef.value)
})

onBeforeUnmount(() => io?.disconnect())
</script>

<template>
  <section
    class="bottom-cta"
    :lang="locale"
    :class="{
      'bottom-cta--rtl': locale === 'ar',
      'is-visible': visible
    }"
    ref="sectionRef"
  >
    <!-- grain overlay(纸质感,opacity 0.05)-->
    <div class="bottom-cta__grain" aria-hidden="true" />

    <!-- 装饰 blob 1(左上)+ blob 2(右下) -->
    <span class="bottom-cta__blob bottom-cta__blob--tl" aria-hidden="true" />
    <span class="bottom-cta__blob bottom-cta__blob--br" aria-hidden="true" />

    <div class="bottom-cta__inner">
      <h2 class="bottom-cta__title">{{ t('home.cta.title') }}</h2>
      <p class="bottom-cta__sub">{{ t('home.cta.sub') }}</p>

      <div class="bottom-cta__actions">
        <!-- primary CTA:白底主色文字 pill -->
        <router-link to="/level-check" class="bottom-cta__link">
          <button class="bottom-cta__btn bottom-cta__btn--primary">
            {{ t('home.cta.btn') }}
          </button>
        </router-link>

        <!-- secondary CTA:透明白边白字 pill -->
        <router-link to="/teacher/register" class="bottom-cta__link">
          <button class="bottom-cta__btn bottom-cta__btn--secondary">
            {{ t('home.cta.teacherJoin') }}
          </button>
        </router-link>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.bottom-cta {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  // v2 深色多色 mesh(替代简单 linear-gradient)
  background: brand.$mesh-bottom-cta;
  min-height: 280px;
  padding-block: brand.$spacing-12;
  padding-inline: brand.$spacing-4;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;

  @media (min-width: brand.$bp-tablet) {
    min-height: 360px;
    padding-block: brand.$spacing-20;
  }
}

// -------- grain overlay(轻微噪点,纸质感)--------
.bottom-cta__grain {
  position: absolute;
  inset: 0;
  background-image: brand.$grain-svg;
  opacity: 0.05;
  mix-blend-mode: overlay;
  pointer-events: none;
  z-index: 1;
}

// -------- 装饰 blob(增强,2 个光晕)--------
.bottom-cta__blob {
  position: absolute;
  border-radius: brand.$radius-full;
  pointer-events: none;
  z-index: 0;
  filter: blur(60px);
  opacity: 0.45;
}

.bottom-cta__blob--tl {
  width: 320px;
  height: 320px;
  inset-block-start: -120px;
  inset-inline-start: -80px;
  background: radial-gradient(circle, rgba(172, 114, 44, 0.50) 0%, transparent 70%);

  @media (max-width: brand.$bp-tablet) {
    width: 220px;
    height: 220px;
    filter: blur(40px);
  }
}

.bottom-cta__blob--br {
  width: 380px;
  height: 380px;
  inset-block-end: -140px;
  inset-inline-end: -100px;
  background: radial-gradient(circle, rgba(154, 52, 18, 0.60) 0%, transparent 70%);
  opacity: 0.50;

  @media (max-width: brand.$bp-tablet) {
    width: 260px;
    height: 260px;
    filter: blur(40px);
  }
}

// -------- 内容容器 --------
.bottom-cta__inner {
  position: relative;
  z-index: 2;       // 浮于 grain + blob 上层
  width: 100%;
  max-width: 720px;
  margin-inline: auto;
  // scroll-reveal 初始状态
  opacity: 0;
  transform: translateY(24px);
  transition:
    opacity brand.$duration-slow brand.$ease-out,
    transform brand.$duration-slow brand.$ease-out;

  .is-visible & {
    opacity: 1;
    transform: translateY(0);
    transition-delay: 80ms;
  }
}

// reduced-motion
@media (prefers-reduced-motion: reduce) {
  .bottom-cta__inner {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
}

// -------- 标题:v2 display serif italic --------
.bottom-cta__title {
  margin: 0 0 brand.$spacing-3;
  // v2:Copernicus serif italic + display-lg 48px PC
  font-family: brand.$font-display;
  font-size: clamp(brand.$font-size-2xl, 5vw, brand.$font-size-display-lg);  // clamp → 48px PC
  font-weight: brand.$font-weight-display;  // 400 serif
  font-style: italic;
  letter-spacing: brand.$ls-display-lg;
  line-height: brand.$lh-display-lg;
  color: brand.$color-text-inverse;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-display-lg;  // 48px
    margin-bottom: brand.$spacing-4;
  }

  // 阿语:不斜体,用 sans
  .bottom-cta--rtl & {
    font-family: brand.$font-sans;
    font-style: normal;
    letter-spacing: 0;
  }

  // 中文 fallback
  &:lang(zh-CN),
  &:lang(zh-TW) {
    font-family: brand.$font-sans;
    font-style: normal;
    font-weight: 700;
    letter-spacing: 0;
  }
}

// -------- 副标题 --------
.bottom-cta__sub {
  margin: 0 0 brand.$spacing-8;
  font-size: brand.$font-size-md;
  color: rgba(255, 255, 255, 0.9);
  line-height: brand.$lh-body-loose;
  max-width: 600px;
  margin-inline: auto;
  margin-block-end: brand.$spacing-8;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-xl; // PC: 20px
  }
}

// -------- CTA 区域 --------
.bottom-cta__link {
  display: inline-block;
  text-decoration: none;
}

.bottom-cta__actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: brand.$spacing-4;
  flex-wrap: wrap;

  // Mobile:单列 stack
  @media (max-width: #{brand.$bp-mobile - 1px}) {
    flex-direction: column;
    align-items: stretch;

    .bottom-cta__link {
      display: block;
    }
  }
}

// -------- 按钮基础 --------
.bottom-cta__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 200px;
  min-height: 52px;
  padding-block: brand.$spacing-3;
  padding-inline: brand.$spacing-8;
  font-size: brand.$font-size-md;
  font-weight: 600;
  border-radius: brand.$radius-full; // pill
  cursor: pointer;
  border: none;
  outline: none;
  // v2 §8.4 四态:spring ease
  transition:
    transform brand.$duration-fast brand.$ease-spring,
    box-shadow brand.$duration-fast brand.$ease-out,
    background brand.$duration-fast ease;
  white-space: nowrap;

  // focus ring(WCAG AA)
  &:focus-visible {
    outline: 3px solid rgba(255, 255, 255, 0.7);
    outline-offset: 2px;
  }

  @media (max-width: #{brand.$bp-mobile - 1px}) {
    width: 100%;
    min-width: unset;
  }
}

// primary:白底 + 主色文字 → hover translateY -2px + glow
.bottom-cta__btn--primary {
  background: brand.$color-bg-card;  // #ffffff
  color: brand.$brand-primary-deep;
  box-shadow: brand.$shadow-v2-lg;

  &:hover {
    transform: translateY(-2px);
    box-shadow: brand.$shadow-glow-primary;
  }

  &:active {
    transform: translateY(0);
    box-shadow: brand.$shadow-v2-sm;
    transition-duration: 75ms;
  }

  &:focus-visible {
    box-shadow: brand.$shadow-glow-primary, brand.$ring-focus;
  }
}

// secondary:透明底 + 白边 + 白字
.bottom-cta__btn--secondary {
  background: transparent;
  color: brand.$color-text-inverse;   // #ffffff
  border: 2px solid brand.$color-text-inverse;

  &:hover {
    background: rgba(255, 255, 255, 0.12);
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.20);
  }

  &:active {
    transform: translateY(0);
    background: rgba(255, 255, 255, 0.18);
    transition-duration: 75ms;
  }
}

// reduced-motion
@media (prefers-reduced-motion: reduce) {
  .bottom-cta__btn {
    transition: none !important;
    transform: none !important;
  }
}
</style>
