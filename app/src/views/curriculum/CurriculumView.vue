<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'
import { buildHead } from '@/utils/seo'

const { t, tm, locale } = useI18n()

const headPayload = computed(() => buildHead({
  title: t('seo.curriculum.title'),
  description: t('seo.curriculum.description'),
  keywords: t('seo.curriculum.keywords'),
  path: '/curriculum',
  locale: locale.value
}))

useHead(headPayload)
</script>

<template>
  <main class="curriculum-view">
    <section class="curriculum-hero">
      <div class="curriculum-hero__inner">
        <p class="curriculum-hero__eyebrow">{{ t('curriculum.hero.eyebrow') }}</p>
        <h1>{{ t('curriculum.hero.title') }}</h1>
        <p>{{ t('curriculum.hero.subtitle') }}</p>
        <router-link :to="{ path: '/register', query: { redirect: '/booking/free-trial' } }" class="curriculum-hero__cta">
          {{ t('curriculum.hero.cta') }}
        </router-link>
      </div>
    </section>

    <section class="curriculum-overview">
      <div class="curriculum-overview__inner">
        <div class="curriculum-overview__intro">
          <p class="curriculum-overview__eyebrow">{{ t('curriculum.overview.eyebrow') }}</p>
          <h2>{{ t('curriculum.overview.title') }}</h2>
          <p>{{ t('curriculum.overview.desc') }}</p>
        </div>

        <div class="curriculum-overview__grid">
          <article
            v-for="(stage, index) in tm('curriculum.stages')"
            :key="stage.name"
            class="stage-card"
          >
            <div class="stage-card__top">
              <span class="stage-card__index">{{ index + 1 }}</span>
              <span class="stage-card__hsk">{{ stage.hsk }}</span>
            </div>
            <h3>{{ stage.name }}</h3>
            <p class="stage-card__cn">{{ stage.chineseName }}</p>

            <dl class="stage-card__meta">
              <div>
                <dt>{{ t('curriculum.labels.learners') }}</dt>
                <dd>{{ stage.learners }}</dd>
              </div>
              <div>
                <dt>{{ t('curriculum.labels.goal') }}</dt>
                <dd>{{ stage.goal }}</dd>
              </div>
            </dl>

            <div class="stage-card__skills">
              <p>{{ t('curriculum.labels.skills') }}</p>
              <ul>
                <li v-for="skill in stage.skills" :key="skill">{{ skill }}</li>
              </ul>
            </div>

            <router-link to="/level-check" class="stage-card__button">
              {{ stage.button }}
            </router-link>
          </article>
        </div>
      </div>
    </section>

    <section class="curriculum-cta">
      <div class="curriculum-cta__inner">
        <h2>{{ t('curriculum.bottomCta.title') }}</h2>
        <p>{{ t('curriculum.bottomCta.desc') }}</p>
        <router-link :to="{ path: '/register', query: { redirect: '/booking/free-trial' } }">
          {{ t('curriculum.bottomCta.button') }}
        </router-link>
      </div>
    </section>
  </main>
</template>

<style scoped lang="scss">
.curriculum-view {
  background: brand.$canvas-warm;
}

.curriculum-hero {
  background:
    radial-gradient(circle at 18% 18%, rgba(208, 154, 76, 0.28), transparent 30%),
    linear-gradient(135deg, brand.$brand-navy 0%, brand.$surface-dark-soft 100%);
  color: brand.$color-text-inverse;
  padding: brand.$spacing-16 brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-24 brand.$spacing-8;
  }
}

.curriculum-hero__inner,
.curriculum-overview__inner,
.curriculum-cta__inner {
  max-width: 1200px;
  margin-inline: auto;
}

.curriculum-hero__inner {
  max-width: 840px;
  text-align: center;
}

.curriculum-hero__eyebrow,
.curriculum-overview__eyebrow {
  margin: 0 0 brand.$spacing-3;
  color: brand.$brand-primary-light;
  font-size: brand.$font-size-sm;
  font-weight: 800;
}

.curriculum-hero h1 {
  margin: 0;
  font-family: brand.$font-display;
  font-size: 42px;
  font-weight: 500;
  letter-spacing: 0;
  line-height: brand.$lh-display-lg;

  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
    font-weight: 700;
  }

  @media (min-width: brand.$bp-tablet) {
    font-size: 60px;
  }
}

.curriculum-hero p {
  margin: brand.$spacing-5 auto 0;
  max-width: 760px;
  font-size: brand.$font-size-lg;
  line-height: brand.$line-height-loose;
  color: rgba(255, 255, 255, 0.84);
}

.curriculum-hero__cta,
.curriculum-cta a,
.stage-card__button {
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: brand.$radius-base;
  padding: 0 brand.$spacing-6;
  text-decoration: none;
  font-weight: 800;
}

.curriculum-hero__cta {
  margin-block-start: brand.$spacing-8;
  background: brand.$brand-gradient-cta;
  color: brand.$color-text-inverse;
  box-shadow: brand.$shadow-brand;
}

.curriculum-overview {
  padding: brand.$spacing-12 brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-20 brand.$spacing-8;
  }
}

.curriculum-overview__intro {
  max-width: 760px;
  margin-inline: auto;
  text-align: center;

  h2 {
    margin: 0;
    color: brand.$brand-navy;
    font-size: brand.$font-size-display-md;
    line-height: brand.$lh-display-md;
  }

  p {
    margin: brand.$spacing-4 0 0;
    color: brand.$body;
    font-size: brand.$font-size-md;
    line-height: brand.$line-height-loose;
  }
}

.curriculum-overview__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-5;
  margin-block-start: brand.$spacing-10;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stage-card {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-5;
  padding: brand.$spacing-6;
  border-radius: brand.$radius-xl;
  background: brand.$color-bg-card;
  border: 1px solid rgba(172, 114, 44, 0.14);
  box-shadow: brand.$shadow-v2-md;
}

.stage-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: brand.$spacing-3;
}

.stage-card__index {
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

.stage-card__hsk {
  border-radius: brand.$radius-full;
  padding: brand.$spacing-1 brand.$spacing-3;
  background: brand.$brand-primary-soft;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-xs;
  font-weight: 800;
}

.stage-card h3 {
  margin: 0;
  color: brand.$brand-navy;
  font-size: brand.$font-size-title-lg;
  line-height: brand.$line-height-tight;
}

.stage-card__cn {
  margin: -#{brand.$spacing-3} 0 0;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-md;
  font-weight: 700;
}

.stage-card__meta {
  display: grid;
  gap: brand.$spacing-4;
  margin: 0;

  dt {
    color: brand.$brand-navy;
    font-size: brand.$font-size-sm;
    font-weight: 800;
  }

  dd {
    margin: brand.$spacing-1 0 0;
    color: brand.$body;
    font-size: brand.$font-size-base;
    line-height: brand.$line-height-loose;
  }
}

.stage-card__skills {
  p {
    margin: 0 0 brand.$spacing-2;
    color: brand.$brand-navy;
    font-size: brand.$font-size-sm;
    font-weight: 800;
  }

  ul {
    margin: 0;
    padding-inline-start: brand.$spacing-5;
    color: brand.$body;
    line-height: brand.$line-height-loose;
  }
}

.stage-card__button {
  margin-block-start: auto;
  border: 1px solid rgba(172, 114, 44, 0.28);
  color: brand.$brand-navy;
}

.curriculum-cta {
  padding: 0 brand.$spacing-4 brand.$spacing-16;

  @media (min-width: brand.$bp-tablet) {
    padding: 0 brand.$spacing-8 brand.$spacing-20;
  }
}

.curriculum-cta__inner {
  border-radius: brand.$radius-2xl;
  background: brand.$brand-navy;
  color: brand.$color-text-inverse;
  padding: brand.$spacing-8 brand.$spacing-6;
  text-align: center;
  box-shadow: brand.$shadow-v2-xl;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-12;
  }

  h2 {
    margin: 0;
    font-size: brand.$font-size-3xl;
    line-height: brand.$line-height-tight;
  }

  p {
    max-width: 680px;
    margin: brand.$spacing-4 auto 0;
    color: rgba(255, 255, 255, 0.78);
    line-height: brand.$line-height-loose;
  }

  a {
    margin-block-start: brand.$spacing-6;
    background: brand.$brand-gradient-cta;
    color: brand.$color-text-inverse;
  }
}
</style>
