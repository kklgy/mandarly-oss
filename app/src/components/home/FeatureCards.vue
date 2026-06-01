<script setup>
// =============================================================================
// <FeatureCards> — HomeView § 2 图文交错特色展示
//
// D26(2026-05-21):v2 质感升级
//   - 背景 canvas-warm(米色暖底)
//   - 标题升级 font-display serif 大字(中文/AR fallback sans bold)
//   - 图片 box-shadow → shadow-v2-2xl(40px deep shadow)
//   - 每个 feature section scroll-reveal(fade-up,stagger 子元素)
//   - 图文区 icon-wrap 升级(accent-teal 背景)
//   - prefers-reduced-motion 兜底
//
// 设计源:docs/frontend/visual-reference/DESIGN-mandarly-v2.md §9.1
//
// 布局保留 D25:
//   PC ≥1024  4 个卡片纵向 stack,图文左右交错
//   Tablet 768-1023  2×2 网格,图上文下
//   Mobile <768  单栏 stack,图上文下
//
// 强约束:
//   1) token only,无 hex 硬编码
//   2) RTL:flex-row 不写 row-reverse,CSS logical + RTL auto mirror
//   3) i18n:全 t('home.feature.xxx.yyy')
//   4) 不动 HomeView.vue 主壳
// =============================================================================
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowRight, User, Microphone, ChatDotRound, Medal } from '@element-plus/icons-vue'
import { markRaw } from 'vue'

// 静态图导入(webp 优先,jpg 兜底)
import img1on1Jpg from '@/assets/marketing/feature/feature-1on1.jpg'
import imgSpeakingJpg from '@/assets/marketing/feature/feature-speaking.jpg'
// D27(2026-05-23):与 HeroSection 对换 — feature 第 3 卡用单孩 tablet 温馨场景
import imgInteractiveJpg from '@/assets/marketing/hero/hero-child-tablet.jpg'
import imgTeacherCertJpg from '@/assets/marketing/feature/feature-teacher-cert.jpg'

const { t } = useI18n()

const features = [
  {
    key: 'native',
    icon: markRaw(User),
    imgJpg: img1on1Jpg,
    imgWebp: null, // Phase 0 只有 jpg;webp 入库后改为 import
  },
  {
    key: 'levelCheck',
    icon: markRaw(Microphone),
    imgJpg: imgSpeakingJpg,
    imgWebp: null,
  },
  {
    key: 'flexible',
    icon: markRaw(ChatDotRound),
    imgJpg: imgInteractiveJpg,
    imgWebp: null,
  },
  {
    key: 'professional',
    icon: markRaw(Medal),
    imgJpg: imgTeacherCertJpg,
    imgWebp: null,
  },
]

// ---------- scroll-reveal (每个 feature item 独立 observer) ----------
const itemRefs = ref([])
const itemsVisible = ref([false, false, false, false])
const observers = []

function setItemRef(el, idx) {
  if (el) itemRefs.value[idx] = el
}

onMounted(() => {
  itemRefs.value.forEach((el, idx) => {
    if (!el) return
    const obs = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        itemsVisible.value[idx] = true
        obs.disconnect()
      }
    }, { threshold: 0.12 })
    obs.observe(el)
    observers.push(obs)
  })
})

onBeforeUnmount(() => observers.forEach(o => o.disconnect()))
</script>

<template>
  <section class="feature-cards">
    <div class="feature-cards__inner">
      <h2 class="feature-cards__title">{{ t('home.feature.title') }}</h2>

      <div class="feature-cards__list">
        <div
          v-for="(f, idx) in features"
          :key="f.key"
          class="feature-cards__item"
          :class="{
            'feature-cards__item--reverse': idx % 2 === 1,
            'is-visible': itemsVisible[idx]
          }"
          :ref="(el) => setItemRef(el, idx)"
          :style="{ '--item-idx': idx }"
        >
          <!-- 文区 -->
          <div class="feature-cards__text">
            <div class="feature-cards__icon-wrap">
              <el-icon class="feature-cards__icon">
                <component :is="f.icon" />
              </el-icon>
            </div>
            <h3 class="feature-cards__card-title">
              {{ t(`home.feature.${f.key}.title`) }}
            </h3>
            <p class="feature-cards__card-desc">
              {{ t(`home.feature.${f.key}.desc`) }}
            </p>
            <router-link to="/teachers" class="feature-cards__cta">
              {{ t('home.feature.learnMore') }}
              <el-icon class="feature-cards__cta-arrow"><ArrowRight /></el-icon>
            </router-link>
          </div>

          <!-- 图区 -->
          <div class="feature-cards__img-wrap">
            <picture>
              <source
                v-if="f.imgWebp"
                :srcset="f.imgWebp"
                type="image/webp"
              />
              <img
                :src="f.imgJpg"
                :alt="t(`home.feature.${f.key}.title`)"
                class="feature-cards__img"
                loading="lazy"
                width="600"
                height="400"
              />
            </picture>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.feature-cards {
  background: brand.$canvas-warm;
  padding-block: brand.$spacing-12;
  padding-inline: brand.$spacing-4;

  @media (min-width: brand.$bp-laptop) {
    padding-block: brand.$spacing-20;
    padding-inline: brand.$spacing-8;
  }
}

.feature-cards__inner {
  max-width: 1200px;
  margin-inline: auto;
}

// ---- 标题:v2 display serif ----
.feature-cards__title {
  margin: 0 0 brand.$spacing-12;
  text-align: center;
  font-family: brand.$font-display;
  font-size: brand.$font-size-display-md;    // 36px
  font-weight: brand.$font-weight-display;   // 400 serif
  letter-spacing: brand.$ls-display-md;
  line-height: brand.$lh-display-md;
  color: brand.$ink;

  // 中文 / RTL fallback:sans bold
  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
    font-weight: 700;
    letter-spacing: 0;
  }

  @media (min-width: brand.$bp-laptop) {
    font-size: brand.$font-size-display-lg;  // 48px
    margin-block-end: brand.$spacing-16;
  }
}

// ─── 列表容器 ───────────────────────────────────────────────────────────────

.feature-cards__list {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-8;

  @media (min-width: brand.$bp-tablet) and (max-width: calc(brand.$bp-laptop - 1px)) {
    // Tablet 768-1023:2×2 网格
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: brand.$spacing-8;
  }
}

// ─── 单个特色行 — 初始隐藏,待 scroll-reveal ──────────────────────────────

.feature-cards__item {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-6;
  align-items: flex-start;
  // scroll-reveal 初始状态
  opacity: 0;
  transform: translateY(30px);
  transition:
    opacity brand.$duration-slow brand.$ease-out,
    transform brand.$duration-slow brand.$ease-out;
  transition-delay: calc(var(--item-idx, 0) * 60ms);

  &.is-visible {
    opacity: 1;
    transform: translateY(0);
  }

  // Tablet:图上文下(列布局,单格内图在前)
  @media (min-width: brand.$bp-tablet) and (max-width: calc(brand.$bp-laptop - 1px)) {
    flex-direction: column;
    gap: brand.$spacing-5;
  }

  // PC:图文左右并排,文左图右
  @media (min-width: brand.$bp-laptop) {
    flex-direction: row;
    align-items: center;
    gap: brand.$spacing-16;
    padding-block: brand.$spacing-8;
  }

  // 偶数卡(2、4):文右图左 → 调换 DOM order,RTL 自动镜像
  &.feature-cards__item--reverse {
    @media (min-width: brand.$bp-laptop) {
      .feature-cards__img-wrap {
        order: -1; // 图置左
      }
    }
  }
}

// reduced-motion
@media (prefers-reduced-motion: reduce) {
  .feature-cards__item {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
}

// ─── 文区 ─────────────────────────────────────────────────────────────────

.feature-cards__text {
  flex: 1;
  min-width: 0;

  @media (min-width: brand.$bp-laptop) {
    flex: 1 1 45%;
    max-width: 500px;
  }
}

// icon wrap 升级:accent-teal 背景
.feature-cards__icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: brand.$radius-lg;
  background: rgba(93, 184, 166, 0.12);   // accent-teal 软底
  margin-block-end: brand.$spacing-4;
  transition: background brand.$duration-base brand.$ease-out;

  .feature-cards__item:hover & {
    background: rgba(93, 184, 166, 0.20);
  }
}

.feature-cards__icon {
  font-size: 24px;
  color: brand.$accent-teal;
}

// ---- 标题:中等 display serif ----
.feature-cards__card-title {
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

  @media (min-width: brand.$bp-laptop) {
    font-size: brand.$font-size-display-sm;
  }
}

.feature-cards__card-desc {
  margin: 0 0 brand.$spacing-5;
  font-size: brand.$font-size-md;
  color: brand.$muted;
  line-height: brand.$lh-body-loose;
  max-width: 440px;
}

.feature-cards__cta {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-1;
  font-size: brand.$font-size-md;
  font-weight: 600;
  color: brand.$brand-primary;
  text-decoration: none;
  transition: gap brand.$duration-fast ease, color brand.$duration-fast ease;

  &:hover,
  &:focus-visible {
    gap: brand.$spacing-2;
    color: brand.$brand-primary-deep;
    text-decoration: underline;
    text-underline-offset: 3px;
  }
}

.feature-cards__cta-arrow {
  font-size: 16px;
  transition: transform brand.$duration-fast ease;

  .feature-cards__cta:hover & {
    transform: translateX(2px);
  }

  // RTL:箭头朝反方向
  [dir='rtl'] & {
    transform: scaleX(-1);
  }
  [dir='rtl'] .feature-cards__cta:hover & {
    transform: scaleX(-1) translateX(-2px);
  }
}

// ─── 图区 ─────────────────────────────────────────────────────────────────

.feature-cards__img-wrap {
  flex-shrink: 0;
  width: 100%;

  // Mobile:图在文前(order -1)
  order: -1;

  // Tablet:图在文前(沿用)
  @media (min-width: brand.$bp-tablet) and (max-width: calc(brand.$bp-laptop - 1px)) {
    order: -1;
    width: 100%;
  }

  // PC:图和文并排,图占比 50%,固定宽度
  @media (min-width: brand.$bp-laptop) {
    order: 0; // 恢复 DOM 顺序,由 .feature-cards__item--reverse 控制 order: -1
    width: 560px;
    flex-shrink: 0;
  }
}

// 图片:v2 shadow-v2-2xl(40px deep shadow)
.feature-cards__img {
  display: block;
  width: 100%;
  height: auto;
  border-radius: brand.$radius-image;
  box-shadow: brand.$shadow-v2-2xl;
  object-fit: cover;
  transition: box-shadow brand.$duration-base brand.$ease-out, transform brand.$duration-base brand.$ease-out;

  .feature-cards__img-wrap:hover & {
    box-shadow: brand.$shadow-v2-2xl, brand.$shadow-glow-primary;
    transform: scale(1.01);
  }

  @media (min-width: brand.$bp-laptop) {
    height: 360px;
    width: 560px;
  }
}
</style>
