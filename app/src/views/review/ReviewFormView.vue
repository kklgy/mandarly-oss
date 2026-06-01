<script setup>
/**
 * ReviewFormView — 课后评价(Wave 5 第 11 轮步骤化)
 *
 * 响应式策略(useBreakpoint):
 *  - H5 < 768: 3 步 wizard(评分 → 标签 → 文字 + 匿名)
 *  - PC ≥ 768: 单页流式三段(同样 3 节,但不分步)
 *
 * 编辑窗口:
 *  - 首次提交后 72h(后端 review.editable_until_at 写入)
 *  - 过期后 isExpired,所有交互 disabled
 *
 * 提交成功:
 *  - <ReviewSuccess /> 全屏覆盖 1.5s 后 router.replace('/orders?tab=finished')
 */

import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElSwitch } from 'element-plus'
import { fromUTC, formatLocal } from '@/utils/datetime'
import { useBookingCountsStore } from '@/stores/bookingCounts'
import { saveReview, updateReview, getReviewByOrder, getReviewTags } from '@/api/review'
import { getOrder } from '@/api/booking'
import { useBreakpoint } from '@/composables/useBreakpoint'
import StarRating from '@/components/review/StarRating.vue'
import TagPicker from '@/components/review/TagPicker.vue'
import ReviewStepIndicator from '@/components/review/ReviewStepIndicator.vue'
import EditWindowProgress from '@/components/review/EditWindowProgress.vue'
import ReviewSuccess from '@/components/review/ReviewSuccess.vue'

const props = defineProps({
  orderId: { type: [String, Number], required: true }
})

const router = useRouter()
const { t, locale } = useI18n()
const { isPC } = useBreakpoint()

// ------------- 常量 -------------
const TOTAL_STEPS = 3
const CONTENT_MAX = 200
const MAX_TAGS = 5

// ------------- 状态 -------------
const loading = ref(true)
const submitting = ref(false)
const submitted = ref(false)  // 提交成功后切到 ReviewSuccess
const errorMsg = ref('')

const order = ref(null)
const existing = ref(null)        // 已有评价 → edit mode
const presetTags = ref([])        // GET /edu/review/tags

// 表单
const rating = ref(0)
const selectedTags = ref([])
const customTags = ref([])
const content = ref('')
const isAnonymous = ref(false)

// H5 步骤
const currentStep = ref(1)

// 提交成功后跳转的 timer(unmount 时需清掉,防止 stale state)
let pendingRedirectTimer = null

// ------------- 计算 -------------
const mode = computed(() => (existing.value ? 'edit' : 'write'))

const isExpired = computed(() => {
  if (mode.value !== 'edit') return false
  if (!existing.value) return false
  if (existing.value.editableUntilAt) {
    return fromUTC(existing.value.editableUntilAt).valueOf() <= Date.now()
  }
  // fallback: createTime + 72h(SQL patch §I.5 已 backfill,极少触发)
  return fromUTC(existing.value.createTime).add(72, 'hour').valueOf() <= Date.now()
})

const ratingTexts = computed(() => [
  t('review.rate.text.1'),
  t('review.rate.text.2'),
  t('review.rate.text.3'),
  t('review.rate.text.4'),
  t('review.rate.text.5')
])

const contentLen = computed(() => Array.from(content.value || '').length)

const canSubmit = computed(() => {
  if (submitting.value) return false
  if (rating.value < 1 || rating.value > 5) return false
  if (selectedTags.value.length > MAX_TAGS) return false
  if (contentLen.value > CONTENT_MAX) return false
  if (isExpired.value) return false
  return true
})

// 步骤跳转限制(H5)
const canGoNextFromStep1 = computed(() => rating.value >= 1)
const canSubmitFromStep3 = canSubmit

// 课程时间展示(走 utils/datetime UTC → 用户时区,避免 shadow useI18n 的 t)
const courseTimeText = computed(() => {
  const ts = order.value?.scheduledAt || order.value?.startAt
  return ts ? formatLocal(ts, 'M/D HH:mm') : ''
})

// ------------- 数据加载 -------------
onMounted(async () => {
  try {
    const [orderResp, reviewResp, tagsResp] = await Promise.all([
      getOrder(Number(props.orderId), locale.value),
      getReviewByOrder(Number(props.orderId)),
      getReviewTags().catch(() => null)  // tags 接口可降级,为空时 fallback 走硬编码
    ])
    order.value = orderResp
    if (reviewResp) {
      existing.value = reviewResp
      rating.value = reviewResp.rating || 0
      content.value = reviewResp.content || ''
      selectedTags.value = Array.isArray(reviewResp.tags) ? [...reviewResp.tags] : []
      customTags.value = Array.isArray(reviewResp.customTags) ? [...reviewResp.customTags] : []
      isAnonymous.value = reviewResp.isAnonymous === true
    }
    // tags fallback:M3 6 条 + Wave 5 6 条
    presetTags.value = Array.isArray(tagsResp) && tagsResp.length > 0
      ? tagsResp
      : ['patient', 'native_accent', 'good_pace', 'well_prepared',
         'interactive', 'good_material', 'std_pronunciation', 'humorous',
         'encouraging', 'proper_difficulty', 'audio_issue', 'late']
  } catch (e) {
    errorMsg.value = e?.message || t('review.error.loadFailed')
  } finally {
    loading.value = false
  }
})

// ------------- 步骤切换(H5)-------------
function goNext() {
  if (currentStep.value < TOTAL_STEPS) {
    currentStep.value++
    if (typeof window !== 'undefined') window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

function goPrev() {
  if (currentStep.value > 1) {
    currentStep.value--
    if (typeof window !== 'undefined') window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// ------------- 提交 -------------
async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  errorMsg.value = ''
  try {
    const payload = {
      orderId: Number(props.orderId),
      rating: rating.value,
      content: (content.value || '').trim() || null,
      tags: [...selectedTags.value],
      isAnonymous: isAnonymous.value,
      customTags: customTags.value.map((s) => s.trim()).filter(Boolean)
    }
    if (mode.value === 'write') {
      await saveReview(payload)
    } else {
      await updateReview(payload)
    }
    submitted.value = true
    // plan §11.11:评价提交后强刷 booking counts(toReview/finished badge 跨 tab 同步)
    try { useBookingCountsStore().fetch(true) } catch {}
    pendingRedirectTimer = setTimeout(() => {
      pendingRedirectTimer = null
      router.replace('/orders?tab=finished')
    }, 1500)
  } catch (e) {
    errorMsg.value = e?.message || t('review.error.submitFailed')
  } finally {
    submitting.value = false
  }
}

function onSuccessBack() {
  router.replace('/orders?tab=finished')
}

onBeforeUnmount(() => {
  if (pendingRedirectTimer) {
    clearTimeout(pendingRedirectTimer)
    pendingRedirectTimer = null
  }
})

function back() {
  router.back()
}
</script>

<template>
  <div class="review-page">
    <!-- 提交成功:全屏覆盖 -->
    <ReviewSuccess
      v-if="submitted"
      :duration="1500"
      @back="onSuccessBack"
    />

    <template v-else>
      <!-- topbar -->
      <header class="review-page__topbar">
        <button class="review-page__back" type="button" @click="back" :aria-label="t('common.back')">
          <span aria-hidden="true">←</span>
          <span class="review-page__back-text">{{ t('review.action.cancel') }}</span>
        </button>
        <ReviewStepIndicator
          v-if="!isPC && !loading && !errorMsg"
          :current="currentStep"
          :total="TOTAL_STEPS"
        />
        <span class="review-page__topbar-spacer" />
      </header>

      <!-- 加载 / 错误 -->
      <div v-if="loading" class="review-page__state">{{ t('common.loading') }}</div>
      <div v-else-if="errorMsg && !order" class="review-page__state review-page__state--error">
        <p>{{ errorMsg }}</p>
        <button class="review-page__retry" type="button" @click="$router.go(0)">{{ t('common.retry') }}</button>
      </div>

      <main v-else class="review-page__main" :class="{ 'is-pc': isPC, 'is-h5': !isPC }">
        <!-- 顶部 hero(教师 + 课程时间)-->
        <section class="review-page__hero">
          <h1 class="review-page__title">
            {{ t('review.title.write', { teacher: order?.teacherNickname || '' }) }}
          </h1>
          <p v-if="courseTimeText" class="review-page__course-time">
            {{ t('review.courseTime', { time: courseTimeText }) }}
          </p>

          <!-- 编辑窗口进度条(只在 edit mode 显示)-->
          <EditWindowProgress
            v-if="mode === 'edit' && existing?.createTime"
            class="review-page__edit-window"
            :created-at="existing.createTime"
            :editable-until-at="existing.editableUntilAt"
            :window-hours="72"
          />
        </section>

        <!-- ============ PC 单页流式 ============ -->
        <template v-if="isPC">
          <!-- § 1 评分 -->
          <section class="review-page__section">
            <h2 class="review-page__section-label">{{ t('review.step1.title') }}</h2>
            <div class="review-page__rating">
              <StarRating
                v-model="rating"
                size="large"
                :readonly="isExpired"
                :texts="ratingTexts"
                :aria-label="t('review.rating.label')"
              />
            </div>
          </section>

          <!-- § 2 标签 -->
          <section class="review-page__section">
            <h2 class="review-page__section-label">
              {{ t('review.step2.title') }}
              <span class="review-page__hint">— {{ t('review.tags.maxHint', { n: MAX_TAGS }) }}</span>
            </h2>
            <TagPicker
              :preset-tags="presetTags"
              :tags-value="selectedTags"
              :custom-value="customTags"
              :max-tags="MAX_TAGS"
              :max-custom="3"
              :custom-max-len="8"
              :disabled="isExpired"
              @update:tags="(v) => (selectedTags = v)"
              @update:custom="(v) => (customTags = v)"
            />
          </section>

          <!-- § 3 文字 + 匿名 -->
          <section class="review-page__section">
            <h2 class="review-page__section-label">{{ t('review.step3.title') }}</h2>
            <textarea
              v-model="content"
              class="review-page__textarea"
              :placeholder="t('review.text.placeholder')"
              rows="4"
              :maxlength="CONTENT_MAX * 2"
              :disabled="isExpired"
              :aria-label="t('review.text.label')"
            />
            <div class="review-page__textarea-meta">
              <span :class="{ 'is-over': contentLen > CONTENT_MAX }">{{ contentLen }} / {{ CONTENT_MAX }}</span>
            </div>

            <div class="review-page__anon">
              <ElSwitch v-model="isAnonymous" :disabled="isExpired" />
              <div class="review-page__anon-text">
                <span class="review-page__anon-label">{{ t('review.anonymous.label') }}</span>
                <span class="review-page__anon-desc">{{ t('review.anonymous.desc') }}</span>
              </div>
            </div>
          </section>

          <p v-if="errorMsg" class="review-page__error">{{ errorMsg }}</p>

          <footer class="review-page__actions">
            <button class="review-page__btn review-page__btn--ghost" type="button" :disabled="submitting" @click="back">
              {{ t('review.action.cancel') }}
            </button>
            <button class="review-page__btn review-page__btn--primary" type="button" :disabled="!canSubmit" @click="submit">
              {{ submitting ? t('review.submitting') : t('review.submit') }}
            </button>
          </footer>
        </template>

        <!-- ============ H5 步骤 wizard ============ -->
        <template v-else>
          <!-- 顶部回显(step 2/3 时)-->
          <section v-if="currentStep > 1" class="review-page__recap">
            <StarRating
              :model-value="rating"
              size="small"
              readonly
              :texts="ratingTexts"
            />
            <div v-if="currentStep === 3 && (selectedTags.length || customTags.length)" class="review-page__recap-tags">
              <span v-for="code in selectedTags" :key="code" class="review-page__recap-tag">
                {{ t(`review.tag.${code}`) }}
              </span>
              <span v-for="tag in customTags" :key="'r-' + tag" class="review-page__recap-tag is-custom">
                {{ tag }}
              </span>
            </div>
          </section>

          <!-- Step 1: 评分 -->
          <section v-if="currentStep === 1" class="review-page__section review-page__section--centered">
            <h2 class="review-page__section-label">{{ t('review.step1.title') }}</h2>
            <p class="review-page__section-guide">{{ t('review.step1.guide') }}</p>
            <div class="review-page__rating">
              <StarRating
                v-model="rating"
                size="large"
                :readonly="isExpired"
                :texts="ratingTexts"
                :aria-label="t('review.rating.label')"
              />
            </div>
          </section>

          <!-- Step 2: 标签 -->
          <section v-else-if="currentStep === 2" class="review-page__section">
            <h2 class="review-page__section-label">{{ t('review.step2.title') }}</h2>
            <p class="review-page__hint review-page__hint--block">{{ t('review.tags.maxHint', { n: MAX_TAGS }) }}</p>
            <TagPicker
              :preset-tags="presetTags"
              :tags-value="selectedTags"
              :custom-value="customTags"
              :max-tags="MAX_TAGS"
              :max-custom="3"
              :custom-max-len="8"
              :disabled="isExpired"
              @update:tags="(v) => (selectedTags = v)"
              @update:custom="(v) => (customTags = v)"
            />
          </section>

          <!-- Step 3: 文字 + 匿名 -->
          <section v-else-if="currentStep === 3" class="review-page__section">
            <h2 class="review-page__section-label">{{ t('review.step3.title') }}</h2>
            <textarea
              v-model="content"
              class="review-page__textarea"
              :placeholder="t('review.text.placeholder')"
              rows="4"
              :maxlength="CONTENT_MAX * 2"
              :disabled="isExpired"
              :aria-label="t('review.text.label')"
            />
            <div class="review-page__textarea-meta">
              <span :class="{ 'is-over': contentLen > CONTENT_MAX }">{{ contentLen }} / {{ CONTENT_MAX }}</span>
            </div>

            <div class="review-page__anon">
              <ElSwitch v-model="isAnonymous" :disabled="isExpired" />
              <div class="review-page__anon-text">
                <span class="review-page__anon-label">{{ t('review.anonymous.label') }}</span>
                <span class="review-page__anon-desc">{{ t('review.anonymous.desc') }}</span>
              </div>
            </div>

            <p v-if="errorMsg" class="review-page__error">{{ errorMsg }}</p>
          </section>

          <!-- H5 sticky bottom 操作栏 -->
          <footer class="review-page__sticky-actions">
            <button
              v-if="currentStep > 1"
              class="review-page__btn review-page__btn--ghost"
              type="button"
              :disabled="submitting"
              @click="goPrev"
            >
              {{ t('review.action.prev') }}
            </button>
            <button
              v-if="currentStep < TOTAL_STEPS"
              class="review-page__btn review-page__btn--primary"
              type="button"
              :disabled="currentStep === 1 ? !canGoNextFromStep1 : false"
              @click="goNext"
            >
              {{ t('review.action.next') }}
            </button>
            <button
              v-else
              class="review-page__btn review-page__btn--primary"
              type="button"
              :disabled="!canSubmitFromStep3"
              @click="submit"
            >
              {{ submitting ? t('review.submitting') : t('review.submit') }}
            </button>
          </footer>
        </template>
      </main>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.review-page {
  min-block-size: 100vh;
  background: brand.$color-bg-page;
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8 brand.$spacing-6;
  }
}

.review-page__topbar {
  max-inline-size: 720px;
  margin: 0 auto brand.$spacing-4;
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;

  @media (min-width: brand.$bp-tablet) {
    margin-block-end: brand.$spacing-6;
  }
}

.review-page__back {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-1;
  background: transparent;
  border: none;
  color: brand.$brand-primary-deep;
  font: inherit;
  font-size: brand.$font-size-base;
  font-weight: 500;
  cursor: pointer;
  padding: brand.$spacing-1 brand.$spacing-2;
  border-radius: brand.$radius-base;
  transition: background 0.15s;

  &:hover {
    background: brand.$brand-primary-soft;
  }

  [dir='rtl'] & span[aria-hidden] {
    transform: scaleX(-1);
    display: inline-block;
  }
}

.review-page__back-text {
  @media (max-width: brand.$bp-mobile) {
    display: none;
  }
}

.review-page__topbar-spacer {
  flex: 1;
}

.review-page__state {
  max-inline-size: 720px;
  margin: 0 auto;
  text-align: center;
  padding: brand.$spacing-12 brand.$spacing-4;
  color: brand.$color-text-secondary;

  &--error { color: brand.$color-error; }
}

.review-page__retry {
  margin-block-start: brand.$spacing-3;
  padding: brand.$spacing-2 brand.$spacing-5;
  background: brand.$brand-primary;
  color: brand.$color-text-inverse;
  border: 1.5px solid brand.$brand-primary;
  border-radius: brand.$radius-base;
  cursor: pointer;
  font: inherit;
}

.review-page__main {
  max-inline-size: 720px;
  margin: 0 auto;
  background: brand.$color-bg-card;
  border-radius: brand.$radius-card;
  box-shadow: brand.$shadow-base;
  padding: brand.$spacing-5;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8;
  }

  &.is-h5 {
    padding-block-end: 96px;  // 让位 sticky bottom
  }
}

.review-page__hero {
  margin-block-end: brand.$spacing-6;
  padding-block-end: brand.$spacing-5;
  border-block-end: 1px solid brand.$color-divider;
}

.review-page__title {
  margin: 0 0 brand.$spacing-2;
  font-size: brand.$font-size-xl;
  font-weight: 700;
  color: brand.$color-text-primary;
  line-height: brand.$line-height-tight;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-2xl;
  }
}

.review-page__course-time {
  margin: 0 0 brand.$spacing-3;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-tertiary;
}

.review-page__edit-window {
  margin-block-start: brand.$spacing-3;
}

.review-page__recap {
  display: flex;
  align-items: flex-start;
  gap: brand.$spacing-3;
  flex-wrap: wrap;
  padding: brand.$spacing-3;
  background: brand.$color-bg-page;
  border-radius: brand.$radius-base;
  margin-block-end: brand.$spacing-5;
}

.review-page__recap-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: brand.$spacing-1;
}

.review-page__recap-tag {
  padding: brand.$spacing-1 brand.$spacing-2;
  background: brand.$brand-primary-soft;
  color: brand.$brand-primary-deep;
  border-radius: brand.$radius-pill;
  font-size: brand.$font-size-xs;
  font-weight: 500;

  &.is-custom {
    background: brand.$brand-primary;
    color: brand.$color-text-inverse;
  }
}

.review-page__section {
  margin-block-end: brand.$spacing-6;

  &--centered {
    text-align: center;
  }
}

.review-page__section-label {
  margin: 0 0 brand.$spacing-3;
  font-size: brand.$font-size-base;
  font-weight: 600;
  color: brand.$color-text-primary;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-md;
  }
}

.review-page__hint {
  font-size: brand.$font-size-sm;
  font-weight: 400;
  color: brand.$color-text-tertiary;

  &--block {
    display: block;
    margin: 0 0 brand.$spacing-3;
  }
}

.review-page__section-guide {
  margin: 0 0 brand.$spacing-4;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}

.review-page__rating {
  display: flex;
  justify-content: center;
  padding: brand.$spacing-4 0;
}

.review-page__textarea {
  inline-size: 100%;
  padding: brand.$spacing-3 brand.$spacing-4;
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-base;
  color: brand.$color-text-primary;
  resize: vertical;
  min-block-size: 96px;
  transition: border-color 0.15s, box-shadow 0.15s;

  &:focus {
    outline: none;
    border-color: brand.$brand-primary;
    box-shadow: brand.$ring-focus;
  }

  &:disabled {
    background: brand.$color-bg-page;
    cursor: not-allowed;
  }
}

.review-page__textarea-meta {
  margin-block-start: brand.$spacing-1;
  text-align: end;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;

  .is-over {
    color: brand.$color-error;
  }
}

.review-page__anon {
  display: flex;
  align-items: flex-start;
  gap: brand.$spacing-3;
  padding: brand.$spacing-3 brand.$spacing-4;
  background: brand.$color-bg-page;
  border-radius: brand.$radius-base;
  margin-block-start: brand.$spacing-4;
}

.review-page__anon-text {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-1;
}

.review-page__anon-label {
  font-size: brand.$font-size-base;
  font-weight: 500;
  color: brand.$color-text-primary;
}

.review-page__anon-desc {
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
  line-height: brand.$line-height-base;
}

.review-page__error {
  margin: brand.$spacing-4 0 0;
  padding: brand.$spacing-3 brand.$spacing-4;
  border-radius: brand.$radius-base;
  font-size: brand.$font-size-sm;
  background: rgba(255, 77, 79, 0.10);
  color: brand.$color-error;
}

// PC 操作栏
.review-page__actions {
  display: flex;
  justify-content: flex-end;
  gap: brand.$spacing-3;
  margin-block-start: brand.$spacing-6;
  padding-block-start: brand.$spacing-5;
  border-block-start: 1px solid brand.$color-divider;
}

// H5 sticky bottom
.review-page__sticky-actions {
  position: fixed;
  inset-inline: 0;
  inset-block-end: 0;
  display: flex;
  gap: brand.$spacing-3;
  padding: brand.$spacing-3 brand.$spacing-4;
  padding-block-end: calc(brand.$spacing-3 + env(safe-area-inset-bottom));
  background: brand.$color-bg-card;
  border-block-start: 1px solid brand.$color-border;
  z-index: brand.$z-fixed;

  @media (min-width: brand.$bp-tablet) {
    display: none;
  }

  .review-page__btn {
    flex: 1;
  }
}

.review-page__btn {
  padding: brand.$spacing-3 brand.$spacing-6;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-base;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid transparent;
  transition: background 0.15s, border-color 0.15s, color 0.15s;

  &--ghost {
    background: brand.$color-bg-card;
    border-color: brand.$color-border;
    color: brand.$color-text-primary;

    &:hover:not(:disabled) {
      border-color: brand.$color-text-tertiary;
    }
  }

  &--primary {
    background: brand.$brand-primary;
    border-color: brand.$brand-primary;
    color: brand.$color-text-inverse;

    &:hover:not(:disabled) {
      background: brand.$brand-primary-deep;
      border-color: brand.$brand-primary-deep;
    }
  }

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }
}
</style>
