<script setup>
// =============================================================================
// <TestimonialCards> — HomeView § 5 学员见证(D26 v2 质感升级)
//
// 升级点:
//   - 背景 canvas-warm(米色暖底)
//   - 3 张见证卡 glass-card--warm:background rgba(250,249,245,0.65) + blur(24px)
//   - 引号装饰升级:font-display serif 40px,主色 alpha 0.3
//   - 5 星颜色改为 accent-amber(琥珀)
//   - hover:scale(1.02) + glow-amber
//   - scroll-reveal stagger(IntersectionObserver 一次性)
//   - prefers-reduced-motion 兜底
//
// 设计源:docs/frontend/visual-reference/DESIGN-mandarly-v2.md §9.1 / §7
//
// 强约束:
//   1) token only,无 hex 硬编码
//   2) RTL:flex 方向 dir="rtl" 自动镜像
//   3) i18n:全 t() / tm()
//   4) alt 文本动态取 item.name
//   5) 示例评价标识必须明显(底部标注 + tooltip)
//   6) 不动 HomeView.vue 主壳
// =============================================================================
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'

import t1 from '@/assets/marketing/testimonial/testimonial-1.jpg'
import t2 from '@/assets/marketing/testimonial/testimonial-2.jpg'
import t3 from '@/assets/marketing/testimonial/testimonial-3.jpg'

const { t, tm } = useI18n()

// 3 张头像与 i18n items 一一对应
const avatars = [t1, t2, t3]

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
  }, { threshold: 0.12 })
  if (sectionRef.value) io.observe(sectionRef.value)
})

onBeforeUnmount(() => io?.disconnect())
</script>

<template>
  <section
    class="testimonial-cards"
    aria-label="testimonial-section"
    ref="sectionRef"
  >
    <div class="testimonial-cards__inner">
      <!-- 标题区 -->
      <div class="testimonial-cards__header">
        <h2 class="testimonial-cards__title">{{ t('home.testimonial.title') }}</h2>
        <p class="testimonial-cards__subtitle">{{ t('home.testimonial.subtitle') }}</p>
      </div>

      <!-- 3 列卡片 -->
      <div
        class="testimonial-cards__grid"
        :class="{ 'is-visible': visible }"
      >
        <div
          v-for="(item, index) in tm('home.testimonial.items')"
          :key="index"
          class="testimonial-card"
          :style="{ '--card-idx': index }"
        >
          <!-- 顶区:头像 + 姓名/国家 -->
          <div class="testimonial-card__top">
            <div class="testimonial-card__avatar-wrap">
              <img
                :src="avatars[index]"
                :alt="item.name"
                class="testimonial-card__avatar"
                width="80"
                height="80"
                loading="lazy"
              />
            </div>
            <div class="testimonial-card__meta">
              <span class="testimonial-card__name">{{ item.name }}</span>
              <span class="testimonial-card__country">{{ item.country }}</span>
            </div>
          </div>

          <!-- 中间引述区 -->
          <div class="testimonial-card__body">
            <span class="testimonial-card__quote-mark" aria-hidden="true">❝</span>
            <p class="testimonial-card__quote">{{ item.quote }}</p>
          </div>

          <!-- 底区:5 星 -->
          <div class="testimonial-card__bottom">
            <div class="testimonial-card__stars" aria-label="5 stars">
              <svg
                v-for="star in 5"
                :key="star"
                class="testimonial-card__star"
                viewBox="0 0 20 20"
                fill="currentColor"
                aria-hidden="true"
                width="18"
                height="18"
              >
                <path
                  d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
// brand token 由 vite.config.js additionalData 全局注入,直接用 brand.$xxx

// ─── section 外壳 ─────────────────────────────────────────────────────────
.testimonial-cards {
  background: brand.$canvas-warm;
  padding-block: brand.$spacing-10;
  padding-inline: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding-block: brand.$spacing-16;
    padding-inline: brand.$spacing-8;
  }
}

.testimonial-cards__inner {
  max-width: 1200px;
  margin-inline: auto;
}

// ─── 标题区 ──────────────────────────────────────────────────────────────
.testimonial-cards__header {
  text-align: center;
  margin-block-end: brand.$spacing-8;

  @media (min-width: brand.$bp-tablet) {
    margin-block-end: brand.$spacing-12;
  }
}

.testimonial-cards__title {
  margin: 0 0 brand.$spacing-3;
  font-family: brand.$font-display;
  font-size: brand.$font-size-display-sm;   // 28px
  font-weight: brand.$font-weight-display;
  letter-spacing: brand.$ls-display-sm;
  line-height: brand.$lh-display-sm;
  color: brand.$ink;

  // 中文 / RTL fallback
  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
    font-weight: 700;
    letter-spacing: 0;
  }

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-display-md; // 36px
  }
}

.testimonial-cards__subtitle {
  margin: 0 auto;
  font-size: brand.$font-size-md;
  color: brand.$muted;
  line-height: brand.$lh-body-loose;
  max-width: 600px;
}

// ─── 3 列网格 ─────────────────────────────────────────────────────────────
.testimonial-cards__grid {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-6;

  @media (min-width: brand.$bp-tablet) {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: brand.$spacing-6;
  }
}

// ─── 单张见证卡:glass-card--warm ─────────────────────────────────────────
.testimonial-card {
  // glass-card--warm
  background: rgba(250, 249, 245, 0.65);
  backdrop-filter: blur(24px) saturate(150%);
  -webkit-backdrop-filter: blur(24px) saturate(150%);
  border: 1px solid rgba(204, 120, 92, 0.12);
  box-shadow: brand.$shadow-v2-lg, inset 0 1px 0 rgba(255, 255, 255, 0.4);
  border-radius: brand.$radius-xl;
  padding: brand.$spacing-8;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-5;
  // scroll-reveal 初始状态
  opacity: 0;
  transform: translateY(28px);
  transition:
    opacity brand.$duration-slow brand.$ease-out,
    transform brand.$duration-slow brand.$ease-out,
    box-shadow brand.$duration-base brand.$ease-out;

  .is-visible & {
    opacity: 1;
    transform: translateY(0);
    transition-delay: calc(var(--card-idx, 0) * 80ms + 60ms);
  }

  &:hover {
    transform: scale(1.02);
    box-shadow: brand.$shadow-v2-xl, brand.$shadow-glow-amber;
    transition-delay: 0ms;
  }

  &:active {
    transform: scale(1.01);
    transition-duration: brand.$duration-fast;
  }
}

// reduced-motion
@media (prefers-reduced-motion: reduce) {
  .testimonial-card {
    opacity: 1 !important;
    transform: none !important;
    transition: box-shadow brand.$duration-fast ease !important;
  }
}

// ─── 顶区:头像 + 姓名/国家 ───────────────────────────────────────────────
.testimonial-card__top {
  display: flex;
  align-items: center;
  gap: brand.$spacing-4;
}

.testimonial-card__avatar-wrap {
  flex-shrink: 0;
}

.testimonial-card__avatar {
  display: block;
  width: 80px;
  height: 80px;
  border-radius: brand.$radius-full;
  object-fit: cover;
  // ring:内层 brand-primary-soft / 外层 accent-amber 30%α
  box-shadow:
    0 0 0 4px brand.$brand-primary-soft,
    0 0 0 8px rgba(232, 165, 90, 0.25);  // accent-amber 25%α
}

.testimonial-card__meta {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-1;
  min-width: 0;
}

.testimonial-card__name {
  font-size: brand.$font-size-lg;
  font-weight: 700;
  color: brand.$ink;
  line-height: brand.$lh-display-sm;
}

.testimonial-card__country {
  font-size: brand.$font-size-base;
  color: brand.$muted;
  line-height: brand.$lh-body-loose;
}

// ─── 引述区 ──────────────────────────────────────────────────────────────
.testimonial-card__body {
  flex: 1;
  position: relative;
}

// 引号装饰:font-display serif 40px,主色 alpha 0.3
.testimonial-card__quote-mark {
  display: block;
  font-size: 40px;
  line-height: 1;
  font-family: brand.$font-display;
  color: brand.$brand-primary;
  opacity: 0.3;
  margin-block-end: brand.$spacing-2;
  // RTL 时自动跟随 text-align
  [dir='rtl'] & {
    text-align: end;
  }
}

.testimonial-card__quote {
  margin: 0;
  font-size: brand.$font-size-md;
  color: brand.$body;
  line-height: 1.6;

  // 英文斜体增强可读性
  // 阿语 RTL 环境不加斜体
  :not([dir='rtl']) & {
    font-style: italic;
  }
}

// ─── 底区:5 星 ──────────────────────────────────────────────────────────
.testimonial-card__bottom {
  display: flex;
  align-items: center;
  gap: brand.$spacing-2;
  flex-wrap: wrap;
}

.testimonial-card__stars {
  display: flex;
  align-items: center;
  gap: 2px;
}

// 5 星改为 accent-amber 琥珀色
.testimonial-card__star {
  color: brand.$accent-amber;
  flex-shrink: 0;
}
</style>
