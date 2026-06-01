<script setup>
/**
 * AuthHero — 鉴权页左栏氛围区(PC ≥ 1024 显示;< 1024 整块隐藏)
 *
 * 装配:
 *   - 顶部: Logo + Mandarly 字样(Georgia italic)
 *   - 中部: 主标题(Georgia italic ink) + 副标题(可选,muted)
 *   - 中下: 品牌插画(SVG,P0 主色渐变兜底)
 *   - 底部: AuthTrustSignals(3 项小标)
 *
 * D25 photo-visual 模式(login / register):
 *   - 左栏铺真实营销照片(feature-1on1.jpg / feature-teacher-cert.jpg)
 *   - 叠 brand.$brand-gradient-bottom-cta α0.7 渐变遮罩 → 白字可读
 *   - 中下显示 auth.heroQuote 引述(24px italic white)+ trust 文字
 *   - teacherRegister / resetPassword 保持原 SVG 插画模式
 *
 * 文案: i18n authHero.{variant}.{title|subtitle} + auth.heroQuote.{login|register}
 * 插画: SVG @/assets/images/auth-{teacher|reset}-illust.svg(非 photo 模式)
 * 照片: @/assets/marketing/feature/feature-1on1.jpg / feature-teacher-cert.jpg
 *
 * 来源: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § Auth 双栏氛围版(第 9 轮定版)
 *        D25 规格: docs/progress/D25-UI视觉浓度升级-plan.md §5 T3
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import AuthTrustSignals from './AuthTrustSignals.vue'

import illustTeacher from '@/assets/images/auth-teacher-illust.svg'
import illustReset from '@/assets/images/auth-reset-illust.svg'

// D25 营销真实照片(photo-visual 模式)
// D27(2026-05-23):project owner提供新图,替换登录 / 注册 AuthHero 配图
import photoLogin from '@/assets/marketing/auth/teacher-whiteboard.png'
import photoRegister from '@/assets/marketing/auth/chinese-dictionary.png'

const props = defineProps({
  /** 'login' | 'register' | 'teacherRegister' | 'resetPassword' */
  variant: {
    type: String,
    default: 'login',
    validator: (v) => ['login', 'register', 'teacherRegister', 'resetPassword'].includes(v)
  }
})

const { t, te } = useI18n()

// photo-visual 模式:login / register 使用真实照片 + 渐变叠层
const PHOTO_VARIANTS = ['login', 'register']
const isPhotoMode = computed(() => PHOTO_VARIANTS.includes(props.variant))

const PHOTO_MAP = {
  login: photoLogin,
  register: photoRegister
}

const ILLUST_MAP = {
  teacherRegister: illustTeacher,
  resetPassword: illustReset
}

const photo = computed(() => PHOTO_MAP[props.variant] || null)
const illustration = computed(() => ILLUST_MAP[props.variant] || illustReset)

const titleKey = computed(() => `authHero.${props.variant}.title`)
const subtitleKey = computed(() => `authHero.${props.variant}.subtitle`)
const subtitle = computed(() => (te(subtitleKey.value) ? t(subtitleKey.value) : ''))

// photo 模式的品牌引述(24px italic white)
const heroQuoteKey = computed(() => `auth.heroQuote.${props.variant}`)
const heroQuote = computed(() => (te(heroQuoteKey.value) ? t(heroQuoteKey.value) : ''))

// 信任文案(photo 模式底部)— 复用 authHero.trust.teachers 文案
const trustLine = computed(() => (te('authHero.trust.teachers') ? t('authHero.trust.teachers', { count: 200 }) : ''))

// 品牌副名 — 默认空,品牌团队后续若需中文副名通过 auth.brand.subname 控制
const brandSubname = computed(() => (te('auth.brand.subname') ? t('auth.brand.subname') : ''))
</script>

<template>
  <!-- ===================== photo-visual 模式(login / register) ===================== -->
  <aside
    v-if="isPhotoMode"
    class="auth-hero auth-hero--photo"
    aria-label="Mandarly brand"
  >
    <!-- 底图 -->
    <img
      :src="photo"
      alt=""
      class="auth-hero__photo"
      aria-hidden="true"
      loading="lazy"
      decoding="async"
    />

    <!-- 渐变叠层:brand.$brand-gradient-bottom-cta(135deg navy→gold) α0.7 -->
    <div class="auth-hero__overlay" aria-hidden="true" />

    <!-- 前景内容:竖向三分布局(顶/中/底) -->
    <div class="auth-hero__photo-content">
      <!-- 顶部 Logo(白色) -->
      <router-link
        to="/"
        class="auth-hero__brand auth-hero__brand--white"
        aria-label="Mandarly home"
      >
        <img
          src="/logo-256.png?v=v20260526-mark-only"
          alt="Mandarly"
          class="auth-hero__logo auth-hero__logo--white"
          @error="(e) => e.target.style.display='none'"
        />
        <span class="auth-hero__brand-name auth-hero__brand-name--white">Mandarly</span>
      </router-link>

      <!-- 中部:品牌引述 -->
      <div class="auth-hero__quote-wrap">
        <blockquote v-if="heroQuote" class="auth-hero__quote">
          {{ heroQuote }}
        </blockquote>
      </div>

      <!-- 底部:trust 文字 -->
      <p v-if="trustLine" class="auth-hero__trust-line">
        {{ trustLine }}
      </p>
    </div>
  </aside>

  <!-- ===================== 原 SVG 插画模式(teacherRegister / resetPassword) ===================== -->
  <aside
    v-else
    class="auth-hero"
    aria-label="Mandarly brand"
  >
    <!-- 顶部 Logo + 品牌字样 -->
    <router-link
      to="/"
      class="auth-hero__brand"
      aria-label="Mandarly home"
    >
      <img
        src="/logo-256.png?v=v20260526-mark-only"
        alt="Mandarly"
        class="auth-hero__logo"
        @error="(e) => e.target.style.display='none'"
      />
      <div class="auth-hero__brand-text">
        <span class="auth-hero__brand-name">Mandarly</span>
        <span v-if="brandSubname" class="auth-hero__brand-subname">{{ brandSubname }}</span>
      </div>
    </router-link>

    <!-- 中部 slogan + 插画 -->
    <div class="auth-hero__body">
      <div class="auth-hero__slogan">
        <h2 class="auth-hero__title">{{ t(titleKey) }}</h2>
        <p v-if="subtitle" class="auth-hero__subtitle">{{ subtitle }}</p>
      </div>

      <div class="auth-hero__illust-wrap">
        <img :src="illustration" alt="" class="auth-hero__illust" aria-hidden="true" />
      </div>
    </div>

    <!-- 底部信任信号 -->
    <div class="auth-hero__trust">
      <AuthTrustSignals />
    </div>
  </aside>
</template>

<style lang="scss" scoped>
// =============================================================================
// 共用基础
// =============================================================================
.auth-hero {
  position: relative;
  min-height: 100vh;
  padding: brand.$spacing-12 brand.$spacing-10;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: brand.$spacing-8;
  overflow: hidden;

  // 大屏 padding 加码呼吸
  @media (min-width: brand.$bp-desktop) {
    padding: brand.$spacing-16 brand.$spacing-12;
  }

  // < 1024 隐藏整个左栏(mobile 零回归)
  @media (max-width: 1023px) {
    display: none;
  }

  // =============================================================================
  // photo-visual 模式
  // =============================================================================
  &--photo {
    padding: 0;
    background: #1a1a1a; // 图片加载前兜底深色

    // Tablet 768-1023 隐藏(统一 <1024 隐藏)
    @media (max-width: 1023px) {
      display: none;
    }
  }

  &__photo {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: center;
    user-select: none;
    -webkit-user-drag: none;
    // RTL:照片不翻转,保持视觉正确
    direction: ltr;
  }

  &__overlay {
    position: absolute;
    inset: 0;
    background: brand.$brand-gradient-bottom-cta;
    opacity: 0.70;
    // 保证白字在所有色段可读
    mix-blend-mode: multiply;
  }

  &__photo-content {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 100vh;
    padding: brand.$spacing-12 brand.$spacing-10;
    gap: brand.$spacing-8;

    @media (min-width: brand.$bp-desktop) {
      padding: brand.$spacing-16 brand.$spacing-12;
    }
  }

  &__quote-wrap {
    flex: 1;
    display: flex;
    align-items: center;
  }

  &__quote {
    margin: 0;
    padding: 0;
    font-family: var(--font-family-heading-en);
    font-style: italic;
    font-size: brand.$font-size-2xl; // 24px
    font-weight: 600;
    line-height: brand.$line-height-tight;
    color: #ffffff;
    // RTL:文字方向随 locale,但 Georgia italic 仅 LTR 可用,ar 回退 sans
    max-width: 420px;

    @media (min-width: brand.$bp-desktop) {
      font-size: brand.$font-size-3xl;
      max-width: 480px;
    }
  }

  &__trust-line {
    margin: 0;
    font-size: brand.$font-size-sm; // 14px
    color: rgba(255, 255, 255, 0.80);
    line-height: brand.$line-height-base;
  }

  // =============================================================================
  // 品牌 logo 区域(两种模式共用 + 白色变体)
  // =============================================================================
  &__brand {
    width: fit-content;
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-3;
    padding: brand.$spacing-1;
    border-radius: brand.$radius-base;
    color: inherit;
    text-decoration: none;
    outline: none;
    transition:
      transform 0.18s ease,
      box-shadow 0.18s ease;

    &:hover {
      transform: translateY(-1px);
    }

    &:focus-visible {
      box-shadow: brand.$ring-focus;
    }

    // D27(2026-05-23):photo 模式 logo 去 pill 容器 — 用户反馈椭圆圈违和
    //   - 完全去除 background / backdrop-filter / border / box-shadow
    //   - 保留 padding(可点击区域)+ gap
    //   - logo + 文字本身用 drop-shadow 增加可读性,不依赖容器
    &--white {
      background: transparent;
      padding: brand.$spacing-1 brand.$spacing-2;
      border-radius: brand.$radius-base;
      gap: brand.$spacing-2;

      &:hover {
        transform: translateY(-1px);
      }

      &:focus-visible {
        box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.5);
      }
    }
  }

  &__logo {
    height: 56px;
    width: auto;
    filter: drop-shadow(0 4px 12px rgba(172, 114, 44, 0.25)); // brand-primary glow

    // Client logo already has a white badge; keep original colors in photo mode.
    &--white {
      height: 40px;
      filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.35));
    }
  }

  &__brand-text {
    display: flex;
    flex-direction: column;
    line-height: 1.1;
  }

  &__brand-name {
    font-family: var(--font-family-heading-en);
    font-style: italic;
    font-size: brand.$font-size-2xl;
    font-weight: 600;
    color: var(--color-primary-deep);
    direction: ltr;
    unicode-bidi: isolate;

    // D27:photo 模式品牌字纯白色 + 黑色 text-shadow 浮在背景图上,无容器
    &--white {
      color: #ffffff;
      font-size: brand.$font-size-xl;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.45),
                   0 1px 2px rgba(0, 0, 0, 0.35);
    }
  }

  &__brand-subname {
    margin-block-start: 2px;
    font-size: brand.$font-size-xs;
    color: var(--color-text-tertiary);
  }

  // =============================================================================
  // 原 SVG 插画模式专属
  // =============================================================================

  // 原模式背景渐变
  &:not(#{&}--photo) {
    background:
      // 主色光晕(D29 navy / gold 透明)
      radial-gradient(circle at 20% 20%, rgba(172, 114, 44, 0.18) 0%, transparent 60%),
      radial-gradient(circle at 80% 80%, rgba(26, 54, 96, 0.10) 0%, transparent 55%),
      linear-gradient(135deg, var(--color-primary-soft) 0%, var(--color-bg-card) 100%);
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-6;
    flex: 1;
    justify-content: center;
  }

  &__slogan {
    max-width: 460px;
  }

  &__title {
    margin: 0;
    font-family: var(--font-family-heading-en);
    font-style: italic;
    font-size: brand.$font-size-3xl;
    font-weight: 600;
    line-height: brand.$line-height-tight;
    color: var(--color-text-primary);

    @media (min-width: brand.$bp-desktop) {
      font-size: brand.$font-size-4xl;
    }
  }

  &__subtitle {
    margin: brand.$spacing-3 0 0;
    font-size: brand.$font-size-md;
    line-height: brand.$line-height-base;
    color: var(--color-text-secondary);
  }

  &__illust-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 1;
    min-height: 200px;
    padding-block: brand.$spacing-4;
  }

  &__illust {
    width: 100%;
    max-width: 360px;
    height: auto;
    user-select: none;
    -webkit-user-drag: none;
  }

  &__trust {
    margin-block-start: brand.$spacing-4;
  }
}
</style>
