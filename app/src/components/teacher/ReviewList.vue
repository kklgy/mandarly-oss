<script setup>
// =============================================================================
// <ReviewList> — TeacherDetail § E2 评价列表
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § TeacherDetailView § E2
//
// 功能:
//   - 标题"学员评价 (N)" + 平均分
//   - 列表(默认 5 条 + 加载更多)
//   - 空态: <EmptyState variant="no-data">
//
// API:
//   - props: reviews[] / total / loading / hasMore / avgRating / reviewCount
//   - emit: load-more
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import EmptyState from '@/components/EmptyState.vue'
import ReviewCard from './ReviewCard.vue'

const props = defineProps({
  reviews: {
    type: Array,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  avgRating: {
    type: Number,
    default: null
  },
  reviewCount: {
    type: Number,
    default: 0
  },
  loading: {
    type: Boolean,
    default: false
  },
  hasMore: {
    type: Boolean,
    default: false
  },
  lowVolumeThreshold: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['load-more'])

const { t, te } = useI18n()
const t2 = (key, fallback, payload) => (te(key) ? t(key, payload || {}) : fallback)

const titleText = computed(() => {
  const n = props.reviewCount
  return t2(
    'teacherDetail.review.title',
    `学员评价${n ? ` (${n})` : ''}`,
    { n, count: n }
  ).replace('{n}', n).replace('{count}', n)
})

const avgDisplay = computed(() => {
  const r = props.avgRating
  if (typeof r !== 'number' || Number.isNaN(r)) return null
  return r.toFixed(1)
})

const remaining = computed(() => Math.max(0, props.total - props.reviews.length))

const isLowVolume = computed(() => Number(props.reviewCount || props.total || 0) < props.lowVolumeThreshold)

const loadMoreText = computed(() => {
  if (props.loading) return t2('common.loading', '加载中…')
  const n = remaining.value
  return t2(
    'teacherDetail.review.seeAll',
    `加载更多(还有 ${n} 条)`,
    { n, count: n }
  ).replace('{count}', n).replace('{n}', n)
})

function onLoadMore() {
  if (props.loading || !props.hasMore) return
  emit('load-more')
}
</script>

<template>
  <section class="rv-list">
    <header class="rv-list__head">
      <h2 class="rv-list__title">{{ titleText }}</h2>
      <span v-if="avgDisplay" class="rv-list__avg" aria-hidden="true">
        ★ {{ avgDisplay }}
      </span>
    </header>

    <EmptyState
      v-if="!reviews || reviews.length === 0"
      variant="no-data"
      :title="t2('teacherDetail.review.lowVolume', '期待你成为第一位评价的学员')"
      compact
    />

    <EmptyState
      v-else-if="isLowVolume"
      variant="no-data"
      :title="t2('teacherDetail.review.lowVolume', '期待你成为第一位评价的学员')"
      compact
    />

    <ul
      v-else
      class="rv-list__items"
    >
      <li
        v-for="r in reviews"
        :key="r.orderId || r.id"
      >
        <ReviewCard :review="r" />
      </li>
    </ul>

    <button
      v-if="hasMore"
      type="button"
      class="rv-list__more"
      :disabled="loading"
      @click="onLoadMore"
    >
      {{ loadMoreText }}
    </button>
  </section>
</template>

<style scoped lang="scss">

.rv-list {
  background: var(--color-bg-card);
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6 brand.$spacing-5;
  box-shadow: brand.$shadow-sm;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8;
  }

  &__head {
    display: flex;
    align-items: baseline;
    gap: brand.$spacing-3;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__avg {
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-md;
    font-weight: 600;
  }

  &__items {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
  }

  &__more {
    align-self: center;
    margin-block-start: brand.$spacing-2;
    padding: brand.$spacing-2 brand.$spacing-6;
    background: var(--color-bg-card);
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-base;
    color: brand.$color-text-primary;
    font: inherit;
    font-size: brand.$font-size-sm;
    cursor: pointer;
    transition: border-color 0.15s ease, color 0.15s ease;

    &:hover:not(:disabled) {
      border-color: brand.$brand-primary;
      color: brand.$brand-primary-deep;
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}
</style>
