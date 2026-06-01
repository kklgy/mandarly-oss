<script setup>
// =============================================================================
// <TeacherHero> — TeacherDetail § A 教师卡片头部
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § TeacherDetailView 改造 § A
//
// 内容: 头像(120) + 名字 + ⭐ 评分(评价数) + 母语标签 + 头部速览 chip
//       + (可选)收藏 ♥ 按钮(P1 占位 disabled)+ "今日可约" 数量提示
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Star, Check } from '@element-plus/icons-vue'

const props = defineProps({
  teacher: {
    type: Object,
    required: true
  },
  reviewStat: {
    type: Object,
    default: null
  },
  showFavorite: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click-favorite'])

const { t, te } = useI18n()
const t2 = (key, fallback, payload) => (te(key) ? t(key, payload || {}) : fallback)

const avatarLetter = computed(() => {
  const src = props.teacher?.nickname || props.teacher?.intro || '?'
  return String(src).slice(0, 1).toUpperCase()
})

const ratingDisplay = computed(() => {
  const r = props.reviewStat?.avgRating
  if (typeof r !== 'number' || Number.isNaN(r)) return null
  return r.toFixed(1)
})

const reviewCount = computed(() => props.reviewStat?.reviewCount || 0)

const reviewCountLabel = computed(() => {
  const n = reviewCount.value
  return t2(
    'teacherDetail.hero.reviews',
    `${n} 条评价`,
    { count: n, n }
  ).replace('{count}', n).replace('{n}', n)
})

const todayAvailableCount = computed(() => props.teacher?.todayAvailableCount || 0)

const todayAvailableLabel = computed(() => {
  const n = todayAvailableCount.value
  if (!n) return ''
  const tpl = t2(
    'teacherDetail.hero.todayAvailable',
    `今日可约 ${n} 节`,
    { count: n, n }
  )
  return tpl.replace('{count}', n).replace('{n}', n)
})

const accentLabel = computed(() => {
  const accent = props.teacher?.accent
  if (!accent) return ''
  const list = Array.isArray(accent) ? accent : [accent]
  return list
    .map((code) => t2(`teacher.accent.${code}`, code))
    .filter(Boolean)
    .join(' · ')
})

const headerChips = computed(() => {
  const arr = props.teacher?.expertise || []
  return arr.slice(0, 3).map((code) => t2(`teacher.expertise.${code}`, code))
})

// D25: 资质徽章(teacher.qualifications 数组,最多 3 条)
const qualificationBadges = computed(() => {
  const arr = props.teacher?.qualifications
  if (Array.isArray(arr) && arr.length) return arr.slice(0, 3)
  return []
})

function onFav() {
  if (props.showFavorite) emit('click-favorite', { userId: props.teacher?.userId })
}
</script>

<template>
  <header class="teacher-hero">
    <div class="teacher-hero__avatar-wrap">
      <img
        v-if="teacher.avatar"
        :src="teacher.avatar"
        :alt="teacher.nickname || ''"
        class="teacher-hero__avatar-img"
      />
      <div v-else class="teacher-hero__avatar-placeholder">
        {{ avatarLetter }}
      </div>
    </div>

    <div class="teacher-hero__main">
      <div class="teacher-hero__name-row">
        <h1 v-if="teacher.nickname" class="teacher-hero__name">{{ teacher.nickname }}</h1>
        <button
          v-if="showFavorite"
          type="button"
          class="teacher-hero__fav"
          :aria-label="t2('teacherDetail.hero.favorite', '收藏')"
          disabled
          :title="t2('teacherCard.favoriteSoon', '敬请期待')"
          @click="onFav"
        >
          <el-icon><Star /></el-icon>
        </button>
      </div>

      <p
        v-if="ratingDisplay || reviewCount"
        class="teacher-hero__rating"
        :aria-label="ratingDisplay ? `${ratingDisplay} stars, ${reviewCount} reviews` : null"
      >
        <span class="teacher-hero__rating-num" aria-hidden="true">
          ★ {{ ratingDisplay || '—' }}
        </span>
        <span class="teacher-hero__rating-count">({{ reviewCountLabel }})</span>
      </p>

      <p v-if="accentLabel" class="teacher-hero__sub" dir="auto">
        {{ accentLabel }}
      </p>

      <p v-if="todayAvailableLabel" class="teacher-hero__today">
        <span class="teacher-hero__today-dot" aria-hidden="true">●</span>
        {{ todayAvailableLabel }}
      </p>

      <ul v-if="headerChips.length" class="teacher-hero__chips">
        <li
          v-for="(chip, idx) in headerChips"
          :key="idx"
          class="teacher-hero__chip"
        >
          {{ chip }}
        </li>
      </ul>

      <!-- D25: 资质徽章 chip pill 化(主色底 + check icon) -->
      <ul v-if="qualificationBadges.length" class="teacher-hero__badges">
        <li
          v-for="(badge, idx) in qualificationBadges"
          :key="idx"
          class="teacher-hero__badge"
        >
          <el-icon class="teacher-hero__badge-icon"><Check /></el-icon>
          {{ badge }}
        </li>
      </ul>
    </div>
  </header>
</template>

<style scoped lang="scss">

.teacher-hero {
  background: var(--color-bg-card);
  border-radius: brand.$radius-xl;
  padding: brand.$spacing-6 brand.$spacing-5;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: brand.$spacing-4;
  box-shadow: brand.$shadow-sm;

  @media (min-width: brand.$bp-tablet) {
    flex-direction: row;
    align-items: flex-start;
    text-align: start;
    padding: brand.$spacing-8;
    gap: brand.$spacing-6;
  }

  &__avatar-wrap {
    flex-shrink: 0;
    width: 160px; // D25: 120 → 160px
    height: 160px;
    border-radius: brand.$radius-full;
    overflow: hidden;
    // D25: ring 8px brand-primary-soft
    box-shadow: 0 0 0 8px brand.$brand-primary-soft, brand.$shadow-brand;
  }

  &__avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__avatar-placeholder {
    width: 100%;
    height: 100%;
    display: grid;
    place-items: center;
    background: brand.$brand-gradient;
    color: brand.$color-text-inverse;
    font-size: brand.$font-size-3xl;
    font-weight: 700;
  }

  &__main {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
    min-width: 0;
    align-items: center;

    @media (min-width: brand.$bp-tablet) {
      align-items: flex-start;
    }
  }

  &__name-row {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    width: 100%;
    justify-content: center;

    @media (min-width: brand.$bp-tablet) {
      justify-content: flex-start;
    }
  }

  &__name {
    margin: 0;
    font-size: brand.$font-size-3xl; // D25: 2xl → 3xl Mobile
    font-weight: 700;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;

    @media (min-width: brand.$bp-tablet) {
      font-size: brand.$font-size-4xl; // D25: 3xl → 4xl(38px)PC
    }
  }

  &__fav {
    width: 36px;
    height: 36px;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-full;
    background: var(--color-bg-card);
    color: brand.$color-text-tertiary;
    cursor: not-allowed;
    display: grid;
    place-items: center;

    &:disabled {
      opacity: 0.6;
    }
  }

  &__rating {
    margin: 0;
    display: inline-flex;
    align-items: baseline;
    gap: brand.$spacing-2;
    font-size: brand.$font-size-base;
    color: brand.$color-text-secondary;
  }

  &__rating-num {
    font-size: brand.$font-size-lg;
    color: brand.$brand-primary-deep;
    font-weight: 600;
  }

  &__sub {
    margin: 0;
    font-size: brand.$font-size-base;
    color: brand.$color-text-secondary;
  }

  &__today {
    margin: 0;
    font-size: brand.$font-size-sm;
    color: brand.$brand-primary-deep;
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-1;
    background: brand.$brand-primary-soft;
    padding: brand.$spacing-1 brand.$spacing-3;
    border-radius: brand.$radius-pill;
  }

  &__today-dot {
    color: brand.$brand-primary;
    font-size: brand.$font-size-xs;
  }

  &__chips {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
    justify-content: center;

    @media (min-width: brand.$bp-tablet) {
      justify-content: flex-start;
    }
  }

  &__chip {
    padding: 2px brand.$spacing-3;
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-sm;
    border-radius: brand.$radius-pill;
    font-weight: 500;
  }

  // D25: 资质徽章 chip — 主色 pill(check + 文字)
  &__badges {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
    justify-content: center;

    @media (min-width: brand.$bp-tablet) {
      justify-content: flex-start;
    }
  }

  &__badge {
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-1;
    padding: brand.$spacing-1 brand.$spacing-3;
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-xs;
    border-radius: brand.$radius-pill;
    font-weight: 600;
    line-height: 1.5;
  }

  &__badge-icon {
    font-size: brand.$font-size-xs;
    color: brand.$brand-primary;
    flex-shrink: 0;
  }
}
</style>
