<script setup>
import { useI18n } from 'vue-i18n'
import beginnerIcon from '@/assets/marketing/curriculum-icons/beginner.png'
import foundationIcon from '@/assets/marketing/curriculum-icons/foundation.png'
import intermediateIcon from '@/assets/marketing/curriculum-icons/intermediate.png'
import advancedIcon from '@/assets/marketing/curriculum-icons/advanced.png'

const { t, tm } = useI18n()

const stageImages = [beginnerIcon, foundationIcon, intermediateIcon, advancedIcon]
</script>

<template>
  <section class="curriculum-path">
    <div class="curriculum-path__inner">
      <div class="curriculum-path__header">
        <p class="curriculum-path__eyebrow">{{ t('home.curriculumPath.eyebrow') }}</p>
        <h2 class="curriculum-path__title">{{ t('home.curriculumPath.title') }}</h2>
        <p class="curriculum-path__subtitle">{{ t('home.curriculumPath.subtitle') }}</p>
      </div>

      <div class="curriculum-path__grid">
        <article
          v-for="(stage, index) in tm('home.curriculumPath.stages')"
          :key="stage.name"
          class="curriculum-path__card"
          :class="`curriculum-path__card--${index + 1}`"
        >
          <span class="curriculum-path__badge">{{ stage.label }}</span>
          <img
            :src="stageImages[index]"
            :alt="stage.name"
            class="curriculum-path__image"
            loading="lazy"
          />
          <h3>{{ stage.name }}</h3>
          <p>{{ stage.desc }}</p>
        </article>
      </div>

      <router-link to="/curriculum" class="curriculum-path__link">
        {{ t('home.curriculumPath.cta') }}
      </router-link>
    </div>
  </section>
</template>

<style scoped lang="scss">
.curriculum-path {
  background: brand.$color-bg-card;
  padding: brand.$spacing-12 brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-16 brand.$spacing-8;
  }
}

.curriculum-path__inner {
  max-width: 1200px;
  margin-inline: auto;
}

.curriculum-path__header {
  max-width: 720px;
  margin-inline: auto;
  text-align: center;
}

.curriculum-path__eyebrow {
  margin: 0 0 brand.$spacing-2;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-sm;
  font-weight: 800;
}

.curriculum-path__title {
  margin: 0;
  color: brand.$brand-navy;
  font-family: brand.$font-display;
  font-size: brand.$font-size-display-md;
  font-weight: 500;
  letter-spacing: 0;
  line-height: brand.$lh-display-md;

  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
    font-weight: 700;
  }
}

.curriculum-path__subtitle {
  margin: brand.$spacing-3 0 0;
  color: brand.$muted;
  font-size: brand.$font-size-md;
  line-height: brand.$line-height-loose;
}

.curriculum-path__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-4;
  margin-block-start: brand.$spacing-10;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: repeat(4, 1fr);
  }
}

.curriculum-path__card {
  min-height: 310px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 0 brand.$spacing-5 brand.$spacing-6;
  border-radius: brand.$radius-xl;
  background: linear-gradient(180deg, brand.$brand-primary-soft 0%, brand.$color-bg-card 62%);
  border: 1px solid rgba(172, 114, 44, 0.14);
  box-shadow: brand.$shadow-v2-md;
  overflow: hidden;

  h3 {
    margin: brand.$spacing-3 0 brand.$spacing-2;
    color: brand.$brand-navy;
    font-size: brand.$font-size-title-md;
    line-height: brand.$line-height-tight;
  }

  p {
    margin: 0;
    color: brand.$body;
    font-size: brand.$font-size-base;
    line-height: brand.$line-height-loose;
  }
}

.curriculum-path__card--2 {
  background: linear-gradient(180deg, rgba(172, 114, 44, 0.12) 0%, brand.$color-bg-card 62%);
}

.curriculum-path__card--3 {
  background: linear-gradient(180deg, rgba(26, 54, 96, 0.10) 0%, brand.$color-bg-card 62%);
}

.curriculum-path__card--4 {
  background: linear-gradient(180deg, rgba(142, 87, 29, 0.12) 0%, brand.$color-bg-card 62%);
}

.curriculum-path__badge {
  min-width: 100%;
  min-height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-inline: calc(brand.$spacing-5 * -1);
  padding: 0 brand.$spacing-4;
  color: brand.$brand-navy;
  background: rgba(255, 255, 255, 0.56);
  font-weight: 800;
}

.curriculum-path__image {
  width: 146px;
  height: 146px;
  object-fit: contain;
  margin-block-start: brand.$spacing-5;
}

.curriculum-path__link {
  width: fit-content;
  min-height: 44px;
  margin: brand.$spacing-8 auto 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 brand.$spacing-6;
  border-radius: brand.$radius-base;
  background: brand.$brand-gradient-cta;
  color: brand.$color-text-inverse;
  text-decoration: none;
  font-weight: 800;
  box-shadow: brand.$shadow-brand;
}
</style>
