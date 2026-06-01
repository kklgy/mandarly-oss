<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'
import { getResult } from '@/api/levelCheck'
import { useUserStore } from '@/stores/user'
import { buildHead } from '@/utils/seo'

const route = useRoute()
const { t, te, locale } = useI18n()
const userStore = useUserStore()
const isContentUnlocked = computed(() => userStore.isLoggedIn)

useHead(computed(() => buildHead({
  title: t('seo.levelCheckResult.title'),
  description: t('seo.levelCheckResult.description'),
  path: `/level-check/result/${route.params.submissionId}`,
  locale: locale.value,
  robots: 'noindex, follow'
})))

const submissionId = computed(() => Number(route.params.submissionId))
const result = ref(null)
const loading = ref(true)
const errorMsg = ref('')

const heading = computed(() => {
  const lvl = result.value?.inferredLevel || 'beginner'
  return t(`s0.result.heading.${lvl}`)
})
const levelLabel = computed(() => {
  const lvl = result.value?.inferredLevel || 'beginner'
  return t(`s0.result.level.${lvl}`)
})
const packageDisplayName = computed(() => {
  const pkg = result.value?.recommendedPackage
  if (!pkg) return ''
  if (pkg.name) return pkg.name
  if (pkg.nameI18nCode && te(pkg.nameI18nCode)) return t(pkg.nameI18nCode)
  return t('s0.result.packageDefaultName')
})

const resultProofItems = computed(() => {
  const items = [
    { value: levelLabel.value, label: t('s0.result.proof.level') }
  ]
  if (!isContentUnlocked.value) {
    items.push({
      value: t('s0.result.proof.nextReady'),
      label: t('s0.result.proof.nextStep')
    })
    return items
  }
  items.push(
    {
      value: String(result.value?.recommendedTeachers?.length || 0),
      label: t('s0.result.proof.teachers')
    },
    {
      value: result.value?.recommendedPackage ? t('s0.result.proof.planReady') : t('s0.result.proof.planLater'),
      label: t('s0.result.proof.plan')
    }
  )
  return items
})

const decoratedTeachers = computed(() => {
  return (result.value?.recommendedTeachers || []).map((teacher) => ({
    ...teacher,
    translatedExpertise: (teacher.expertise || [])
      .map((tag) => (te(`teacher.expertise.${tag}`) ? t(`teacher.expertise.${tag}`) : tag))
      .slice(0, 3),
    accentLabel: teacher.accent && te(`teacher.accent.${teacher.accent}`)
      ? t(`teacher.accent.${teacher.accent}`)
      : teacher.accent
  }))
})

onMounted(async () => {
  try {
    result.value = await getResult(submissionId.value)
  } catch (e) {
    errorMsg.value = t('common.error')
  } finally {
    loading.value = false
  }
})

function packageWeeklyLabel(pkg) {
  if (!pkg) return ''
  if (pkg.isFreeTrial) return t('s0.result.freePackage')
  const n = pkg.weeklyCount || 0
  return n === 1 ? t('s0.result.perWeek', { n }) : t('s0.result.perWeekPlural', { n })
}

function formatPrice(pkg) {
  if (!pkg) return ''
  if (pkg.isFreeTrial) return t('s0.result.freePackage')
  if (pkg.price === null || pkg.price === undefined) return ''
  const num = Number(pkg.price)
  if (!Number.isFinite(num) || num <= 0) return ''
  const formatted = new Intl.NumberFormat(locale.value, { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num)
  return `${pkg.currency} ${formatted}`
}

function openSupport() {
  window.dispatchEvent(new CustomEvent('mandarly:open-support'))
}
</script>

<template>
  <div class="lcr-page">
    <div class="lcr-container">
      <div v-if="loading" class="lcr-state">{{ t('common.loading') }}</div>

      <div v-else-if="errorMsg" class="lcr-state lcr-state--error">{{ errorMsg }}</div>

      <template v-else-if="result">
        <header class="lcr-hero">
          <div class="lcr-hero__copy">
            <span class="lcr-level-badge">{{ levelLabel }}</span>
            <h1 class="lcr-heading">{{ heading }}</h1>
            <p class="lcr-subtitle">
              {{ isContentUnlocked ? t('s0.result.subtitle') : t('s0.result.subtitleGuest') }}
            </p>
          </div>
          <dl class="lcr-proof">
            <div v-for="item in resultProofItems" :key="item.label" class="lcr-proof__item">
              <dt>{{ item.value }}</dt>
              <dd>{{ item.label }}</dd>
            </div>
          </dl>
        </header>

        <section v-if="isContentUnlocked" class="lcr-section">
          <div class="lcr-section__head">
            <p class="lcr-section__eyebrow">{{ t('s0.result.teachersEyebrow') }}</p>
            <h2 class="lcr-section__title">{{ t('s0.result.teachersTitle') }}</h2>
          </div>

          <div v-if="decoratedTeachers.length" class="lcr-grid">
            <article
              v-for="teacher in decoratedTeachers"
              :key="teacher.userId"
              class="lcr-card lcr-teacher"
            >
              <div class="lcr-teacher__media">
                <img
                  v-if="teacher.avatar"
                  :src="teacher.avatar"
                  :alt="teacher.nickname || ''"
                  class="lcr-teacher__avatar-img"
                  loading="lazy"
                  width="320"
                  height="240"
                />
                <div v-else class="lcr-teacher__avatar" aria-hidden="true">
                  {{ (teacher.nickname || teacher.intro || '?').slice(0, 1).toUpperCase() }}
                </div>
              </div>
              <div class="lcr-teacher__body">
                <p v-if="teacher.nickname" class="lcr-teacher__name">{{ teacher.nickname }}</p>
                <p class="lcr-teacher__intro" dir="auto">{{ teacher.intro }}</p>
                <ul class="lcr-tags">
                  <li v-for="tag in teacher.translatedExpertise" :key="tag" class="lcr-tag">{{ tag }}</li>
                  <li v-if="teacher.accentLabel" class="lcr-tag lcr-tag--soft">{{ teacher.accentLabel }}</li>
                </ul>
                <div class="lcr-teacher__meta">
                  <span v-if="teacher.yearsExperience">
                    {{ t('s0.result.yearsExperience', { n: teacher.yearsExperience }) }}
                  </span>
                  <span v-if="teacher.introVideoUrl" class="lcr-teacher__video">
                    {{ t('s0.result.hasIntroVideo') }}
                  </span>
                </div>
              </div>
              <router-link class="lcr-btn lcr-btn--primary" :to="`/teacher/${teacher.userId}`">
                {{ t('s0.result.bookNow') }}
              </router-link>
            </article>
          </div>

          <div v-else class="lcr-state">{{ t('s0.result.empty') }}</div>
        </section>

        <section v-if="isContentUnlocked && result.recommendedPackage" class="lcr-section">
          <div class="lcr-section__head">
            <p class="lcr-section__eyebrow">{{ t('s0.result.packageEyebrow') }}</p>
            <h2 class="lcr-section__title">{{ t('s0.result.packageTitle') }}</h2>
          </div>
          <article class="lcr-card lcr-package">
            <div class="lcr-package__head">
              <div>
                <p class="lcr-package__name">{{ packageDisplayName }}</p>
                <p class="lcr-package__desc">{{ t('s0.result.packageDesc') }}</p>
              </div>
              <p v-if="formatPrice(result.recommendedPackage)" class="lcr-package__price">
                {{ formatPrice(result.recommendedPackage) }}
              </p>
            </div>
            <ul class="lcr-package__meta">
              <li v-if="result.recommendedPackage.weeklyCount">
                {{ packageWeeklyLabel(result.recommendedPackage) }}
              </li>
              <li v-if="result.recommendedPackage.totalCount">
                {{ t('s0.result.total', { n: result.recommendedPackage.totalCount }) }}
              </li>
              <li v-if="result.recommendedPackage.validityDays">
                {{ t('s0.result.validity', { days: result.recommendedPackage.validityDays }) }}
              </li>
            </ul>
            <router-link class="lcr-package__cta" to="/packages">
              {{ t('s0.result.viewPackages') }}
            </router-link>
          </article>
        </section>

        <footer class="lcr-footer">
          <router-link
            v-if="isContentUnlocked"
            class="lcr-btn lcr-btn--ghost"
            to="/teachers"
          >
            {{ t('s0.result.viewMoreTeachers') }}
          </router-link>
          <router-link
            v-else
            class="lcr-btn lcr-btn--ghost"
            :to="{ path: '/register', query: { redirect: route.fullPath } }"
          >
            {{ t('s0.result.registerToContinue') }}
          </router-link>
          <button class="lcr-btn lcr-btn--support" type="button" @click="openSupport">
            {{ t('s0.result.contactSupport') }}
          </button>
        </footer>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.lcr-page {
  min-height: 100vh;
  background:
    linear-gradient(180deg, brand.$brand-primary-soft 0%, brand.$color-bg-page 34%, brand.$color-bg-page 100%);
  padding: brand.$spacing-6 brand.$spacing-4 brand.$spacing-12;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-10 brand.$spacing-6 brand.$spacing-16;
  }
}

.lcr-container {
  max-width: 960px;
  margin: 0 auto;
}

.lcr-hero {
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-6;
  padding: brand.$spacing-6 brand.$spacing-4;
  background:
    radial-gradient(circle at 10% 0%, rgba(255, 195, 77, 0.22) 0%, transparent 42%),
    brand.$color-bg-card;
  border: 1px solid rgba(255, 166, 10, 0.2);
  border-radius: brand.$radius-xl;
  box-shadow: brand.$shadow-md;
  margin-bottom: brand.$spacing-8;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: minmax(0, 1fr) minmax(300px, 0.75fr);
    align-items: center;
    padding: brand.$spacing-8;
  }
}

.lcr-hero__copy {
  text-align: start;
}

.lcr-level-badge {
  display: inline-block;
  padding: brand.$spacing-1 brand.$spacing-3;
  background: brand.$brand-primary;
  color: brand.$color-text-inverse;
  font-size: brand.$font-size-sm;
  font-weight: 600;
  border-radius: brand.$radius-full;
  margin-bottom: brand.$spacing-3;
}

.lcr-heading {
  margin: 0;
  font-size: brand.$font-size-2xl;
  font-weight: 700;
  color: brand.$color-text-primary;
  line-height: brand.$line-height-tight;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-3xl;
  }
}

.lcr-subtitle {
  margin: brand.$spacing-3 0 0;
  color: brand.$color-text-secondary;
  font-size: brand.$font-size-base;
  line-height: brand.$line-height-base;
}

.lcr-proof {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: brand.$spacing-3;

  @media (max-width: #{brand.$bp-mobile - 1px}) {
    grid-template-columns: 1fr;
  }
}

.lcr-proof__item {
  margin: 0;
  padding: brand.$spacing-4;
  border-radius: brand.$radius-card;
  background: brand.$brand-primary-soft;

  dt {
    margin: 0;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-lg;
    font-weight: 700;
    line-height: brand.$line-height-tight;
  }

  dd {
    margin: brand.$spacing-1 0 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-xs;
    line-height: brand.$line-height-base;
  }
}

.lcr-section {
  margin-bottom: brand.$spacing-8;
}

.lcr-section__head {
  margin-bottom: brand.$spacing-4;
}

.lcr-section__eyebrow {
  margin: 0 0 brand.$spacing-1;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-xs;
  font-weight: 700;
}

.lcr-section__title {
  margin: 0;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  color: brand.$color-text-primary;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-xl;
  }
}

.lcr-grid {
  display: grid;
  gap: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.lcr-card {
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-card;
  padding: brand.$spacing-4;
  box-shadow: brand.$shadow-base;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-6;
  }
}

.lcr-teacher {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: brand.$spacing-3;
}

.lcr-teacher__media {
  border-radius: brand.$radius-lg;
  overflow: hidden;
  background: brand.$color-bg-strong;
}

.lcr-teacher__avatar-img {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
}

.lcr-teacher__avatar {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: brand.$brand-gradient;
  color: brand.$color-text-inverse;
  font-size: brand.$font-size-4xl;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.lcr-teacher__body {
  flex: 1;
}

.lcr-teacher__name {
  margin: 0 0 brand.$spacing-2;
  font-size: brand.$font-size-md;
  font-weight: 600;
  color: brand.$color-text-primary;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-lg;
  }
}

.lcr-teacher__intro {
  margin: 0 0 brand.$spacing-3;
  font-size: brand.$font-size-base;
  line-height: brand.$line-height-base;
  color: brand.$color-text-secondary;

  display: -webkit-box;
  min-height: 63px;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.lcr-tags {
  list-style: none;
  margin: 0 0 brand.$spacing-3;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: brand.$spacing-2;

}

.lcr-tag {
  padding: 2px brand.$spacing-2;
  background: brand.$brand-primary-soft;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-xs;
  border-radius: brand.$radius-sm;

  &--soft {
    background: brand.$color-divider;
    color: brand.$color-text-secondary;
  }
}

.lcr-teacher__meta {
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
  display: flex;
  gap: brand.$spacing-3;

}

.lcr-teacher__video {
  color: brand.$color-success;
}

.lcr-package {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
}

.lcr-package__head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: brand.$spacing-3;
  flex-wrap: wrap;
}

.lcr-package__name {
  margin: 0;
  font-size: brand.$font-size-md;
  font-weight: 600;
  color: brand.$color-text-primary;
}

.lcr-package__desc {
  margin: brand.$spacing-1 0 0;
  color: brand.$color-text-secondary;
  font-size: brand.$font-size-sm;
  line-height: brand.$line-height-base;
}

.lcr-package__price {
  margin: 0;
  font-size: brand.$font-size-lg;
  font-weight: 700;
  color: brand.$brand-primary-deep;
}

.lcr-package__meta {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: brand.$spacing-4;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
}

.lcr-package__cta {
  align-self: flex-start;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-sm;
  font-weight: 700;
  text-decoration: none;

  &:hover {
    color: brand.$brand-primary;
  }
}

.lcr-footer {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-3;
  margin-top: brand.$spacing-8;

  @media (min-width: brand.$bp-tablet) {
    flex-direction: row;
    justify-content: center;
  }
}

.lcr-btn {
  display: inline-block;
  padding: brand.$spacing-3 brand.$spacing-6;
  font: inherit;
  font-size: brand.$font-size-md;
  font-weight: 500;
  border-radius: brand.$radius-base;
  text-align: center;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.18s, color 0.18s, box-shadow 0.18s, transform 0.05s;

  &:active {
    transform: scale(0.98);
  }

  &--primary {
    background: brand.$brand-gradient;
    color: brand.$color-text-inverse;
    border: 1.5px solid brand.$brand-primary;

    &:hover {
      background: brand.$brand-primary-deep;
      border-color: brand.$brand-primary-deep;
      box-shadow: brand.$shadow-brand;
    }
  }

  &--ghost {
    background: transparent;
    color: brand.$color-text-secondary;
    border: 1.5px solid brand.$color-border;

    &:hover {
      border-color: brand.$brand-primary;
      color: brand.$brand-primary;
    }
  }

  &--support {
    background: brand.$color-text-primary;
    color: brand.$color-text-inverse;
    border: 1.5px solid brand.$color-text-primary;

    &:hover {
      background: brand.$brand-primary-deep;
      border-color: brand.$brand-primary-deep;
    }
  }
}

.lcr-state {
  text-align: center;
  padding: brand.$spacing-12 brand.$spacing-4;
  color: brand.$color-text-secondary;

  &--error {
    color: brand.$color-error;
  }
}
</style>
