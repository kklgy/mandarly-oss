<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'
import { listQuestions, submitAnswers } from '@/api/levelCheck'
import { getSessionId } from '@/utils/sessionId'
import { buildHead, jsonLd, SITE_ORIGIN, SITE_NAME } from '@/utils/seo'

const router = useRouter()
const { t, locale } = useI18n()

const headPayload = computed(() => {
  const base = buildHead({
    title: t('seo.levelCheck.title'),
    description: t('seo.levelCheck.description'),
    keywords: t('seo.levelCheck.keywords'),
    path: '/level-check',
    locale: locale.value
  })
  return {
    ...base,
    script: [
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'Quiz',
        name: t('seo.levelCheck.title'),
        description: t('seo.levelCheck.description'),
        url: SITE_ORIGIN + '/level-check',
        about: {
          '@type': 'Thing',
          name: 'Mandarin Chinese proficiency'
        },
        educationalUse: 'placement test',
        learningResourceType: 'Assessment',
        provider: {
          '@type': 'EducationalOrganization',
          name: SITE_NAME,
          url: SITE_ORIGIN + '/'
        },
        isAccessibleForFree: true
      })
    ]
  }
})

useHead(headPayload)

const questions = ref([])
const answers = ref({})       // { questionCode: optionCode }
const currentIdx = ref(0)
const email = ref('')
const loading = ref(true)
const submitting = ref(false)
const errorMsg = ref('')

const total = computed(() => questions.value.length)
const currentQuestion = computed(() => questions.value[currentIdx.value])
const isLast = computed(() => currentIdx.value === total.value - 1)
const currentAnswer = computed(() => {
  const q = currentQuestion.value
  return q ? answers.value[q.questionCode] : null
})
const canProceed = computed(() => !!currentAnswer.value)
const progressPct = computed(() => {
  if (total.value === 0) return 0
  return Math.round(((currentIdx.value + 1) / total.value) * 100)
})

const proofItems = computed(() => [
  { value: t('s0.proof.questions.value'), label: t('s0.proof.questions.label') },
  { value: t('s0.proof.login.value'), label: t('s0.proof.login.label') },
  { value: t('s0.proof.match.value'), label: t('s0.proof.match.label') }
])

const flowItems = computed(() => [
  t('s0.flow.level'),
  t('s0.flow.goal'),
  t('s0.flow.pace'),
  t('s0.flow.learner')
])

onMounted(loadQuestions)

async function loadQuestions() {
  loading.value = true
  errorMsg.value = ''
  try {
    questions.value = await listQuestions()
  } catch (e) {
    errorMsg.value = t('s0.error.loadFailed')
  } finally {
    loading.value = false
  }
}

function selectOption(optionCode) {
  const q = currentQuestion.value
  if (!q) return
  answers.value = { ...answers.value, [q.questionCode]: optionCode }
}

function goNext() {
  if (!canProceed.value) return
  if (!isLast.value) currentIdx.value += 1
}

function goPrev() {
  if (currentIdx.value > 0) currentIdx.value -= 1
}

async function onSubmit() {
  if (!canProceed.value) return
  submitting.value = true
  errorMsg.value = ''
  const payload = {
    sessionId: getSessionId(),
    locale: locale.value,
    email: email.value || undefined,
    answers: Object.entries(answers.value).map(([questionCode, optionCode]) => ({
      questionCode,
      optionCode
    }))
  }
  try {
    const result = await submitAnswers(payload)
    router.push({ name: 'LevelCheckResult', params: { submissionId: result.submissionId } })
  } catch (e) {
    errorMsg.value = t('s0.error.submitFailed')
    submitting.value = false
  }
}
</script>

<template>
  <div class="lc-page">
    <section class="lc-hero">
      <div class="lc-hero__copy">
        <p class="lc-kicker">{{ t('s0.kicker') }}</p>
        <h1 class="lc-title">{{ t('s0.title') }}</h1>
        <p class="lc-subtitle">{{ t('s0.subtitle') }}</p>
        <dl class="lc-proof">
          <div v-for="item in proofItems" :key="item.label" class="lc-proof__item">
            <dt>{{ item.value }}</dt>
            <dd>{{ item.label }}</dd>
          </div>
        </dl>
      </div>
      <div class="lc-hero__media" aria-hidden="true">
        <img src="/hero-mandarly.webp" alt="" width="600" height="400" />
      </div>
    </section>

    <div class="lc-container">
      <aside class="lc-flow" :aria-label="t('s0.flowLabel')">
        <span
          v-for="(item, idx) in flowItems"
          :key="item"
          class="lc-flow__item"
          :class="{ 'is-active': idx === currentIdx, 'is-done': idx < currentIdx }"
        >
          <span class="lc-flow__index">{{ idx + 1 }}</span>
          <span class="lc-flow__text">{{ item }}</span>
        </span>
      </aside>

      <div v-if="loading" class="lc-state">{{ t('common.loading') }}</div>

      <div v-else-if="errorMsg && questions.length === 0" class="lc-state lc-state--error">
        <p>{{ errorMsg }}</p>
        <button class="lc-btn lc-btn--primary" @click="loadQuestions">{{ t('common.retry') }}</button>
      </div>

      <section v-else-if="currentQuestion" class="lc-card" :aria-label="t('s0.cardLabel')">
        <div class="lc-progress">
          <div class="lc-progress__top">
            <span class="lc-progress__label">
              {{ t('s0.progress', { current: currentIdx + 1, total }) }}
            </span>
            <span class="lc-progress__percent">{{ progressPct }}%</span>
          </div>
          <div class="lc-progress__bar">
            <div class="lc-progress__fill" :style="{ width: `${progressPct}%` }" />
          </div>
        </div>

        <h2 class="lc-question">{{ t(currentQuestion.questionI18nCode) }}</h2>

        <div class="lc-options">
          <button
            v-for="opt in currentQuestion.options"
            :key="opt.optionCode"
            class="lc-option"
            :class="{ 'lc-option--selected': currentAnswer === opt.optionCode }"
            type="button"
            @click="selectOption(opt.optionCode)"
          >
            <span class="lc-option__radio" />
            <span class="lc-option__text" dir="auto">{{ t(opt.optionI18nCode) }}</span>
          </button>
        </div>

        <div v-if="isLast" class="lc-email">
          <label class="lc-email__label" for="lc-email-input">
            {{ t('s0.optionalEmail.label') }}
          </label>
          <input
            id="lc-email-input"
            v-model="email"
            type="email"
            class="lc-email__input"
            :placeholder="t('s0.optionalEmail.placeholder')"
          />
        </div>

        <p v-if="errorMsg" class="lc-error-inline">{{ errorMsg }}</p>

        <footer class="lc-actions">
          <button
            v-if="currentIdx > 0"
            class="lc-btn lc-btn--ghost"
            type="button"
            :disabled="submitting"
            @click="goPrev"
          >
            {{ t('common.back') }}
          </button>
          <button
            v-if="!isLast"
            class="lc-btn lc-btn--primary"
            type="button"
            :disabled="!canProceed"
            @click="goNext"
          >
            {{ t('common.next') }}
          </button>
          <button
            v-else
            class="lc-btn lc-btn--primary"
            type="button"
            :disabled="!canProceed || submitting"
            @click="onSubmit"
          >
            {{ submitting ? t('common.loading') : t('common.submit') }}
          </button>
        </footer>
      </section>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.lc-page {
  min-height: 100vh;
  background:
    linear-gradient(180deg, brand.$brand-primary-soft 0%, brand.$color-bg-page 42%, brand.$color-bg-page 100%);
  padding: brand.$spacing-6 brand.$spacing-4 brand.$spacing-12;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-10 brand.$spacing-6 brand.$spacing-16;
  }
}

.lc-hero {
  max-width: 1120px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-6;
  align-items: center;
  padding-block: brand.$spacing-6 brand.$spacing-8;

  @media (min-width: brand.$bp-laptop) {
    grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
    gap: brand.$spacing-10;
    padding-block: brand.$spacing-10 brand.$spacing-12;
  }
}

.lc-hero__copy {
  text-align: center;

  @media (min-width: brand.$bp-laptop) {
    text-align: start;
  }
}

.lc-kicker {
  display: inline-flex;
  align-items: center;
  margin: 0 0 brand.$spacing-3;
  padding: brand.$spacing-1 brand.$spacing-3;
  border: 1px solid rgba(255, 166, 10, 0.28);
  border-radius: brand.$radius-pill;
  background: rgba(255, 255, 255, 0.72);
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-xs;
  font-weight: 700;
}

.lc-title {
  margin: 0 0 brand.$spacing-3;
  font-size: brand.$font-size-3xl;
  font-weight: 700;
  color: brand.$color-text-primary;
  line-height: brand.$line-height-tight;

  @media (min-width: brand.$bp-tablet) {
    font-size: 44px;
  }
}

.lc-subtitle {
  margin: 0;
  font-size: brand.$font-size-base;
  color: brand.$color-text-secondary;
  line-height: brand.$line-height-base;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-md;
  }
}

.lc-proof {
  margin: brand.$spacing-6 0 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: brand.$spacing-3;

  @media (max-width: #{brand.$bp-tablet - 1px}) {
    grid-template-columns: 1fr;
  }
}

.lc-proof__item {
  margin: 0;
  padding: brand.$spacing-4;
  border: 1px solid rgba(255, 166, 10, 0.18);
  border-radius: brand.$radius-card;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: brand.$shadow-sm;

  dt {
    margin: 0;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-xl;
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

.lc-hero__media {
  display: none;

  @media (min-width: brand.$bp-laptop) {
    display: block;
  }

  img {
    display: block;
    width: 100%;
    height: auto;
    aspect-ratio: 3 / 2;
    object-fit: cover;
    border-radius: brand.$radius-xl;
    box-shadow: brand.$shadow-lg;
  }
}

.lc-container {
  max-width: 1120px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-5;

  @media (min-width: brand.$bp-laptop) {
    grid-template-columns: 280px minmax(0, 1fr);
    align-items: start;
    gap: brand.$spacing-8;
  }
}

.lc-flow {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: brand.$spacing-2;

  @media (min-width: brand.$bp-laptop) {
    position: sticky;
    inset-block-start: 88px;
    grid-template-columns: 1fr;
    padding: brand.$spacing-4;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-card;
    background: rgba(255, 255, 255, 0.86);
    box-shadow: brand.$shadow-sm;
  }
}

.lc-flow__item {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: brand.$spacing-2;
  padding: brand.$spacing-2;
  border-radius: brand.$radius-base;
  color: brand.$color-text-tertiary;
  font-size: brand.$font-size-xs;
  font-weight: 600;

  &.is-active {
    color: brand.$color-text-primary;
    background: brand.$brand-primary-soft;
  }

  &.is-done {
    color: brand.$brand-primary-deep;
  }
}

.lc-flow__index {
  flex-shrink: 0;
  display: inline-grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: brand.$radius-full;
  background: brand.$color-bg-card;
  border: 1px solid currentColor;
}

.lc-flow__text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lc-card {
  background: brand.$color-bg-card;
  border: 1px solid rgba(255, 166, 10, 0.18);
  border-radius: brand.$radius-xl;
  padding: brand.$spacing-6 brand.$spacing-4;
  box-shadow: brand.$shadow-md;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8;
  }
}

.lc-progress {
  margin-bottom: brand.$spacing-6;
}

.lc-progress__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: brand.$spacing-3;
  margin-bottom: brand.$spacing-2;
}

.lc-progress__label {
  display: block;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
  text-align: start;
}

.lc-progress__percent {
  flex-shrink: 0;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-xs;
  font-weight: 700;
}

.lc-progress__bar {
  height: 8px;
  background: brand.$color-border-light;
  border-radius: brand.$radius-full;
  overflow: hidden;
}

.lc-progress__fill {
  height: 100%;
  background: brand.$brand-primary;
  border-radius: brand.$radius-full;
  transition: width 0.3s ease;

  :global(html[dir="rtl"]) & {
    margin-inline-start: auto;
  }
}

.lc-question {
  margin: 0 0 brand.$spacing-6;
  font-size: brand.$font-size-xl;
  font-weight: 600;
  line-height: brand.$line-height-tight;
  color: brand.$color-text-primary;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-2xl;
  }
}

.lc-options {
  display: grid;
  gap: brand.$spacing-3;
  margin-bottom: brand.$spacing-6;
}

.lc-option {
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;
  padding: brand.$spacing-4;
  background: brand.$color-bg-card;
  border: 1.5px solid brand.$color-border;
  border-radius: brand.$radius-card;
  font: inherit;
  font-size: brand.$font-size-md;
  color: brand.$color-text-primary;
  cursor: pointer;
  text-align: start;
  transition: border-color 0.18s, background 0.18s, transform 0.05s;

  &:hover {
    border-color: brand.$brand-primary;
    background: brand.$brand-primary-soft;
    box-shadow: brand.$ring-focus;
  }

  &:active {
    transform: scale(0.99);
  }

  &--selected {
    border-color: brand.$brand-primary;
    background: brand.$brand-primary-soft;
    box-shadow: brand.$ring-focus;

    .lc-option__radio {
      border-color: brand.$brand-primary;
      background: brand.$brand-primary;

      &::after {
        opacity: 1;
        transform: scale(1);
      }
    }
  }
}

.lc-option__radio {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border: 2px solid brand.$color-border;
  border-radius: brand.$radius-full;
  background: brand.$color-bg-card;
  position: relative;
  transition: border-color 0.18s, background 0.18s;

  &::after {
    content: '';
    position: absolute;
    inset: 4px;
    border-radius: brand.$radius-full;
    background: brand.$color-bg-card;
    opacity: 0;
    transform: scale(0.6);
    transition: opacity 0.18s, transform 0.18s;
  }
}

.lc-option__text {
  flex: 1;
  line-height: brand.$line-height-base;
}

.lc-email {
  margin-bottom: brand.$spacing-6;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
}

.lc-email__label {
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
}

.lc-email__input {
  padding: brand.$spacing-3 brand.$spacing-4;
  border: 1.5px solid brand.$color-border;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-base;
  color: brand.$color-text-primary;
  background: brand.$color-bg-card;
  transition: border-color 0.18s;

  &:focus {
    outline: none;
    border-color: brand.$brand-primary;
    box-shadow: brand.$ring-focus;
  }
}

.lc-error-inline {
  margin: 0 0 brand.$spacing-4;
  color: brand.$color-error;
  font-size: brand.$font-size-sm;
  text-align: start;
}

.lc-actions {
  display: flex;
  justify-content: flex-end;
  gap: brand.$spacing-3;

  @media (max-width: #{brand.$bp-mobile - 1px}) {
    flex-direction: column-reverse;
  }
}

.lc-btn {
  min-width: 120px;
  padding: brand.$spacing-3 brand.$spacing-6;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-md;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.18s, color 0.18s, box-shadow 0.18s, transform 0.05s;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &:active:not(:disabled) {
    transform: scale(0.98);
  }

  &--primary {
    background: brand.$brand-gradient;
    color: brand.$color-text-inverse;
    border: 1.5px solid brand.$brand-primary;

    &:hover:not(:disabled) {
      background: brand.$brand-primary-deep;
      border-color: brand.$brand-primary-deep;
      box-shadow: brand.$shadow-brand;
    }
  }

  &--ghost {
    background: transparent;
    color: brand.$color-text-secondary;
    border: 1.5px solid brand.$color-border;

    &:hover:not(:disabled) {
      border-color: brand.$brand-primary;
      color: brand.$brand-primary;
    }
  }
}

.lc-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: brand.$spacing-12 brand.$spacing-4;
  color: brand.$color-text-secondary;

  &--error p {
    margin-bottom: brand.$spacing-4;
    color: brand.$color-error;
  }
}
</style>
