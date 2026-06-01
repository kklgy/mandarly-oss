<script setup>
// =============================================================================
// <ReviewCard> — 单条评价
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § TeacherDetailView § E2
//
// 字段:
//   - rating(1~5)
//   - studentDisplayName + studentAvatar(可选)
//   - content(用户生成, dir="auto" 防 RTL bidi)
//   - tags[]: i18n_message code 数组, 调用方负责 t() 解析(组件内做兜底)
//   - createdLabel: 调用方拼好的 dayjs 时区时间字符串
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  review: {
    type: Object,
    required: true
  }
})

const { t, te } = useI18n()
const t2 = (key, fallback) => (te(key) ? t(key) : fallback)

const stars = computed(() => Array.from({ length: 5 }, (_, i) => i + 1))

const ratingNum = computed(() => Number(props.review?.rating || 0))

const ariaLabel = computed(() => {
  return t2('review.card.starsAria', `${ratingNum.value} stars`).replace(
    '{n}',
    ratingNum.value
  )
})

const avatarLetter = computed(() => {
  const src = props.review?.studentDisplayName || 'S'
  return String(src).slice(0, 1).toUpperCase()
})

function tagLabel(code) {
  if (te(`review.tag.${code}`)) return t(`review.tag.${code}`)
  return code
}
</script>

<template>
  <article class="rv-card">
    <header class="rv-card__head">
      <div class="rv-card__author">
        <div
          v-if="review.studentAvatar"
          class="rv-card__avatar"
        >
          <img
            :src="review.studentAvatar"
            :alt="review.studentDisplayName || ''"
          />
        </div>
        <div v-else class="rv-card__avatar rv-card__avatar--placeholder">
          {{ avatarLetter }}
        </div>

        <div class="rv-card__author-meta">
          <span class="rv-card__name">{{ review.studentDisplayName || 'Student' }}</span>
          <span
            class="rv-card__stars"
            :aria-label="ariaLabel"
          >
            <span
              v-for="n in stars"
              :key="n"
              :class="['rv-card__star', n <= ratingNum ? 'is-on' : '']"
              aria-hidden="true"
            >★</span>
          </span>
        </div>
      </div>

      <time v-if="review.createdLabel" class="rv-card__time">
        {{ review.createdLabel }}
      </time>
    </header>

    <p
      v-if="review.content"
      class="rv-card__content"
      dir="auto"
    >{{ review.content }}</p>

    <ul
      v-if="review.tags && review.tags.length"
      class="rv-card__tags"
    >
      <li
        v-for="(code, idx) in review.tags"
        :key="idx"
        class="rv-card__tag"
      >
        {{ tagLabel(code) }}
      </li>
    </ul>
  </article>
</template>

<style scoped lang="scss">

.rv-card {
  background: var(--color-bg-card);
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-card;
  padding: brand.$spacing-4 brand.$spacing-5;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-3;

  &__head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: brand.$spacing-3;
    flex-wrap: wrap;
  }

  &__author {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    min-width: 0;
  }

  &__avatar {
    width: 40px;
    height: 40px;
    border-radius: brand.$radius-full;
    overflow: hidden;
    flex-shrink: 0;
    background: brand.$color-bg-strong;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    &--placeholder {
      display: grid;
      place-items: center;
      background: brand.$brand-gradient;
      color: brand.$color-text-inverse;
      font-weight: 600;
      font-size: brand.$font-size-base;
    }
  }

  &__author-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__name {
    font-size: brand.$font-size-sm;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__stars {
    display: inline-flex;
    gap: 2px;
    font-size: brand.$font-size-base;
  }

  &__star {
    color: brand.$color-divider;

    &.is-on {
      color: brand.$brand-primary;
    }
  }

  &__time {
    font-size: brand.$font-size-xs;
    color: brand.$color-text-tertiary;
    flex-shrink: 0;
  }

  &__content {
    margin: 0;
    font-size: brand.$font-size-base;
    line-height: brand.$line-height-base;
    color: brand.$color-text-body;
    word-break: break-word;
  }

  &__tags {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
  }

  &__tag {
    padding: 2px brand.$spacing-2;
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
    border-radius: brand.$radius-sm;
    font-size: brand.$font-size-xs;
    font-weight: 500;
  }
}
</style>
