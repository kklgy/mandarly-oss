<script setup>
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

const CLASS_VIDEO_URL = 'https://mandarly-demo-bucket.cos.ap-hongkong.myqcloud.com/marketing/home/mandarly-class-demo-20260526.mp4'
</script>

<template>
  <section class="hero-section" :class="{ 'hero-section--rtl': locale === 'ar' }">
    <div class="hero-section__inner">
      <div class="hero-section__copy">
        <p class="hero-section__eyebrow">{{ t('home.hero.eyebrow') }}</p>
        <h1 class="hero-section__title">{{ t('home.hero.title') }}</h1>
        <p class="hero-section__subtitle">{{ t('home.hero.subtitle') }}</p>

        <div class="hero-section__cta" :aria-label="t('home.hero.title')">
          <router-link
            :to="{ path: '/register', query: { redirect: '/booking/free-trial' } }"
            class="hero-section__btn hero-section__btn--primary"
          >
            {{ t('home.hero.cta.freeTrial') }}
          </router-link>
          <router-link to="/curriculum" class="hero-section__btn hero-section__btn--secondary">
            {{ t('home.hero.cta.curriculum') }}
          </router-link>
        </div>

        <p class="hero-section__trust">{{ t('home.hero.trust') }}</p>
      </div>

      <div class="hero-section__video-panel">
        <p class="hero-section__video-title">{{ t('home.hero.video.title') }}</p>
        <div class="hero-section__video-card">
          <video
            class="hero-section__video"
            :src="CLASS_VIDEO_URL"
            muted
            controls
            loop
            autoplay
            playsinline
            preload="metadata"
          />
        </div>
        <p class="hero-section__video-caption">{{ t('home.hero.video.caption') }}</p>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.hero-section {
  background:
    radial-gradient(circle at 10% 15%, rgba(172, 114, 44, 0.18), transparent 28%),
    linear-gradient(135deg, brand.$canvas-warm 0%, brand.$color-bg-card 48%, brand.$brand-primary-soft 100%);
  padding: brand.$spacing-10 brand.$spacing-4 brand.$spacing-12;
  overflow: hidden;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-16 brand.$spacing-8;
  }
}

.hero-section__inner {
  max-width: 1200px;
  margin-inline: auto;
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-8;
  align-items: center;

  @media (min-width: brand.$bp-laptop) {
    grid-template-columns: minmax(0, 0.92fr) minmax(520px, 1.08fr);
    gap: brand.$spacing-12;
  }
}

.hero-section__copy {
  text-align: start;
}

.hero-section__eyebrow {
  margin: 0 0 brand.$spacing-3;
  display: inline-flex;
  border: 1px solid rgba(172, 114, 44, 0.18);
  border-radius: brand.$radius-full;
  padding: brand.$spacing-2 brand.$spacing-4;
  background: rgba(255, 255, 255, 0.72);
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-sm;
  font-weight: 700;
}

.hero-section__title {
  margin: 0;
  color: brand.$brand-navy;
  font-family: brand.$font-display;
  font-size: 42px;
  font-weight: 500;
  letter-spacing: 0;
  line-height: brand.$lh-display-lg;

  @media (min-width: brand.$bp-tablet) {
    font-size: 58px;
  }

  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
    font-weight: 700;
  }
}

.hero-section__subtitle {
  max-width: 560px;
  margin: brand.$spacing-5 0 0;
  color: brand.$body;
  font-size: brand.$font-size-lg;
  line-height: brand.$line-height-loose;
}

.hero-section__cta {
  display: flex;
  flex-wrap: wrap;
  gap: brand.$spacing-3;
  margin-block-start: brand.$spacing-8;
}

.hero-section__btn {
  min-height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: brand.$radius-base;
  padding: 0 brand.$spacing-6;
  text-decoration: none;
  font-size: brand.$font-size-md;
  font-weight: 700;
  transition: transform brand.$duration-base brand.$ease-out, box-shadow brand.$duration-base brand.$ease-out;

  &:hover {
    transform: translateY(-2px);
  }
}

.hero-section__btn--primary {
  background: brand.$brand-gradient-cta;
  color: brand.$color-text-inverse;
  box-shadow: brand.$shadow-brand;
}

.hero-section__btn--secondary {
  background: brand.$color-bg-card;
  color: brand.$brand-navy;
  border: 1px solid rgba(172, 114, 44, 0.32);
}

.hero-section__trust {
  margin: brand.$spacing-6 0 0;
  color: brand.$muted;
  font-size: brand.$font-size-sm;
  font-weight: 600;
  line-height: brand.$line-height-base;
}

.hero-section__video-panel {
  min-width: 0;
}

.hero-section__video-title {
  margin: 0 0 brand.$spacing-3;
  color: brand.$brand-navy;
  font-size: brand.$font-size-xl;
  font-weight: 800;
  text-align: start;
}

.hero-section__video-card {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: brand.$radius-2xl;
  background: brand.$brand-navy;
  box-shadow: brand.$shadow-v2-2xl;
  border: 1px solid rgba(255, 255, 255, 0.76);
}

.hero-section__video {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  cursor: pointer;
}

.hero-section__video-caption {
  margin: brand.$spacing-3 0 0;
  color: brand.$muted;
  font-size: brand.$font-size-base;
  line-height: brand.$line-height-base;
}

@media (max-width: brand.$bp-mobile) {
  .hero-section__title {
    font-size: 34px;
  }

  .hero-section__cta {
    flex-direction: column;
  }

  .hero-section__btn {
    width: 100%;
  }
}
</style>
