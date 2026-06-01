<script setup>
import { useI18n } from 'vue-i18n'
import lessonClock from '@/assets/marketing/home-icons/lesson-clock.png'
import nativeTeacher from '@/assets/marketing/home-icons/native-teacher.png'
import flexibleCalendar from '@/assets/marketing/home-icons/flexible-calendar.png'
import progressChart from '@/assets/marketing/home-icons/progress-chart.png'

const { t } = useI18n()

const trustItems = [
  { key: 'lesson', image: lessonClock },
  { key: 'native', image: nativeTeacher },
  { key: 'schedule', image: flexibleCalendar },
  { key: 'progress', image: progressChart }
]

const statItems = ['activeLearners', 'professionalTutors', 'lessonsCompleted', 'parentSatisfaction']
</script>

<template>
  <section class="stats-bar">
    <div class="stats-bar__trust">
      <p class="stats-bar__trust-title">{{ t('home.trustBar.title') }}</p>
      <div class="stats-bar__trust-grid">
        <article
          v-for="item in trustItems"
          :key="item.key"
          class="stats-bar__trust-item"
        >
          <img
            :src="item.image"
            :alt="t(`home.trustBar.items.${item.key}.title`)"
            class="stats-bar__trust-icon"
            loading="lazy"
          />
          <div>
            <h3>{{ t(`home.trustBar.items.${item.key}.title`) }}</h3>
            <p>{{ t(`home.trustBar.items.${item.key}.desc`) }}</p>
          </div>
        </article>
      </div>
    </div>

    <div class="stats-bar__band" aria-label="Mandarly learning statistics">
      <div
        v-for="key in statItems"
        :key="key"
        class="stats-bar__stat"
      >
        <strong>{{ t(`home.stats.${key}.value`) }}</strong>
        <span>{{ t(`home.stats.${key}.label`) }}</span>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.stats-bar {
  background: brand.$color-bg-card;
}

.stats-bar__trust {
  max-width: 1200px;
  margin-inline: auto;
  padding: brand.$spacing-6 brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8 brand.$spacing-8 brand.$spacing-6;
  }
}

.stats-bar__trust-title {
  margin: 0 0 brand.$spacing-5;
  text-align: center;
  color: brand.$muted;
  font-size: brand.$font-size-lg;
  font-weight: 700;
  line-height: brand.$line-height-base;
}

.stats-bar__trust-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-3;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: repeat(4, 1fr);
    gap: 0;
  }
}

.stats-bar__trust-item {
  display: grid;
  grid-template-columns: 72px 1fr;
  align-items: center;
  gap: brand.$spacing-3;
  min-height: 96px;
  padding: brand.$spacing-4;
  border-radius: brand.$radius-lg;
  background: rgba(255, 250, 242, 0.56);

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: 70px 1fr;
    padding: brand.$spacing-3 brand.$spacing-5;
    border-radius: 0;
    background: transparent;
    border-inline-end: 1px solid rgba(26, 54, 96, 0.12);

    &:last-child {
      border-inline-end: 0;
    }
  }

  h3 {
    margin: 0 0 brand.$spacing-1;
    color: brand.$brand-navy;
    font-size: brand.$font-size-base;
    font-weight: 800;
    line-height: brand.$line-height-tight;
  }

  p {
    margin: 0;
    color: brand.$body;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }
}

.stats-bar__trust-icon {
  width: 64px;
  height: 64px;
  object-fit: contain;
  border-radius: brand.$radius-base;
}

.stats-bar__band {
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0;
  padding: brand.$spacing-8 brand.$spacing-4;
  overflow: hidden;
  background:
    radial-gradient(circle at 20% 10%, rgba(255, 182, 39, 0.18), transparent 24%),
    radial-gradient(circle at 76% 18%, rgba(255, 198, 77, 0.12), transparent 22%),
    linear-gradient(135deg, #071b36 0%, brand.$brand-navy 56%, #08233f 100%);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    opacity: 0.16;
    background-image:
      radial-gradient(circle, rgba(255, 255, 255, 0.5) 1px, transparent 1px);
    background-size: 18px 18px;
    mask-image: linear-gradient(90deg, transparent 0%, #000 18%, #000 82%, transparent 100%);
  }

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: repeat(4, 1fr);
    padding: brand.$spacing-10 brand.$spacing-8;
  }
}

.stats-bar__stat {
  position: relative;
  z-index: 1;
  min-height: 92px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: brand.$spacing-3;

  strong {
    color: brand.$brand-gold;
    font-family: brand.$font-display;
    font-size: brand.$font-size-display-sm;
    font-weight: 700;
    line-height: brand.$lh-display-sm;
  }

  span {
    margin-block-start: brand.$spacing-1;
    color: brand.$color-text-inverse;
    font-size: brand.$font-size-base;
    font-weight: 800;
    line-height: brand.$line-height-base;
  }

  @media (min-width: brand.$bp-tablet) {
    min-height: 104px;

    strong {
      font-size: brand.$font-size-display-md;
    }
  }
}
</style>
