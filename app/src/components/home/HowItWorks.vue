<script setup>
import { useI18n } from 'vue-i18n'
import levelCheck from '@/assets/marketing/home-how-icons/level-check.png'
import matchTutor from '@/assets/marketing/home-how-icons/match-tutor.png'
import bookLesson from '@/assets/marketing/home-how-icons/book-lesson.png'
import liveClass from '@/assets/marketing/home-how-icons/live-class.png'

const { t, tm } = useI18n()

const stepImages = [levelCheck, matchTutor, bookLesson, liveClass]
</script>

<template>
  <section class="how-it-works">
    <div class="how-it-works__inner">
      <div class="how-it-works__header">
        <h2>{{ t('home.how.title') }}</h2>
        <p>{{ t('home.how.subtitle') }}</p>
      </div>

      <div class="how-it-works__steps">
        <template
          v-for="(step, index) in tm('home.how.steps')"
          :key="step.title"
        >
          <article class="how-it-works__card">
            <span class="how-it-works__number">{{ index + 1 }}</span>
            <img
              :src="stepImages[index]"
              :alt="step.title"
              class="how-it-works__image"
              loading="lazy"
            />
            <h3>{{ step.title }}</h3>
            <p>{{ step.desc }}</p>
          </article>
          <span
            v-if="index < 3"
            class="how-it-works__arrow"
            aria-hidden="true"
          >
            →
          </span>
        </template>
      </div>

      <router-link to="/register" class="how-it-works__cta">
        {{ t('home.how.cta') }}
      </router-link>
    </div>
  </section>
</template>

<style scoped lang="scss">
.how-it-works {
  background: brand.$mesh-cream-soft;
  padding: brand.$spacing-12 brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-16 brand.$spacing-8;
  }
}

.how-it-works__inner {
  max-width: 1200px;
  margin-inline: auto;
}

.how-it-works__header {
  text-align: center;

  h2 {
    margin: 0;
    color: brand.$brand-navy;
    font-family: brand.$font-display;
    font-size: brand.$font-size-display-md;
    font-weight: 500;
    line-height: brand.$lh-display-md;
    letter-spacing: 0;

    &:lang(zh-CN),
    &:lang(zh-TW),
    &:lang(ar) {
      font-family: brand.$font-sans;
      font-weight: 800;
    }
  }

  p {
    margin: brand.$spacing-2 0 0;
    color: brand.$muted;
    font-size: brand.$font-size-md;
    line-height: brand.$line-height-base;
  }
}

.how-it-works__steps {
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-4;
  margin-block-start: brand.$spacing-8;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: 1fr 32px 1fr 32px 1fr 32px 1fr;
    align-items: center;
    gap: brand.$spacing-3;
  }
}

.how-it-works__card {
  position: relative;
  min-height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  text-align: center;
  padding: brand.$spacing-6 brand.$spacing-5;
  border: 1px solid rgba(26, 54, 96, 0.10);
  border-radius: brand.$radius-xl;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: brand.$shadow-v2-md;

  h3 {
    margin: brand.$spacing-3 0 brand.$spacing-2;
    color: brand.$brand-navy;
    font-size: brand.$font-size-title-md;
    font-weight: 800;
    line-height: brand.$line-height-tight;
  }

  p {
    margin: 0;
    color: brand.$body;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-loose;
  }
}

.how-it-works__number {
  position: absolute;
  inset-block-start: brand.$spacing-4;
  inset-inline-start: brand.$spacing-4;
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: brand.$radius-full;
  background: brand.$brand-navy;
  color: brand.$color-text-inverse;
  font-weight: 800;
}

.how-it-works__image {
  width: 118px;
  height: 118px;
  object-fit: contain;
  margin-block-start: brand.$spacing-3;
}

.how-it-works__arrow {
  display: none;

  @media (min-width: brand.$bp-tablet) {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: brand.$brand-gold;
    font-size: brand.$font-size-2xl;
    font-weight: 800;
  }
}

.how-it-works__cta {
  width: fit-content;
  min-height: 48px;
  margin: brand.$spacing-8 auto 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 brand.$spacing-8;
  border-radius: brand.$radius-base;
  background: brand.$brand-gradient-cta;
  color: brand.$color-text-inverse;
  text-decoration: none;
  font-size: brand.$font-size-md;
  font-weight: 800;
  box-shadow: brand.$shadow-brand;
}
</style>
