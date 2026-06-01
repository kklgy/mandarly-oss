<script setup>
// =============================================================================
// <TeacherCard> — 教师信息卡片(Mandarly 共享, 3 variant)
//
// 契约: app/src/components/SHARED-API.md §1
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md §8.1 + § G(第 6 轮)
//
// variant:
//   - home    : Home 推荐 carousel(280px 卡, 头像 4:3, 名/评分/价格 + 1 chip + 浮签)
//   - list    : TeacherList 主网格(响应式列, 头像 4:3, 5 字段 + 2 chip + 价格 + 浮签)
//   - compact : 详情"相关老师" / 搜索建议(头像 1:1 圆 80px, 仅名 + 评分)
//
// 严格约束:
//   1) i18n: 调用方传 displayAccent / displayExpertise[] / period;
//      徽章文案是 § 0.3 白名单例外, key teacherCard.badge.{todayAvailable,newTeacher}
//   2) token only — 0 硬编码
//   3) 卡片整体 <router-link>(SEO + 右键打开 tab 友好), ♥ 点击 stopPropagation
//   4) RTL: 浮签 inset-inline-*, 价格 text-align: end
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Star } from '@element-plus/icons-vue'

const props = defineProps({
  teacher: {
    type: Object,
    required: true
  },
  variant: {
    type: String,
    default: 'list',
    validator: (v) => ['home', 'list', 'compact'].includes(v)
  },
  showFavorite: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click-favorite'])

const { t, te } = useI18n()

const t2 = (key, fallback, params) => (te(key) ? t(key, params) : fallback)

// 徽章: 优先 todayAvailable > newTeacher
const activeBadge = computed(() => {
  const list = props.teacher?.badges || []
  if (list.includes('todayAvailable')) {
    return {
      type: 'today',
      label: t2('teacherCard.badge.todayAvailable', '今日可约')
    }
  }
  if (list.includes('newTeacher')) {
    return {
      type: 'new',
      label: t2('teacherCard.badge.newTeacher', '新教师')
    }
  }
  return null
})

// chip 数量按 variant 限制
const chipCount = computed(() => {
  if (props.variant === 'home') return 1
  if (props.variant === 'list') return 2
  return 0
})

const visibleExpertise = computed(() => {
  const arr = props.teacher?.displayExpertise || []
  return chipCount.value > 0 ? arr.slice(0, chipCount.value) : []
})

const avatarLetter = computed(() => {
  const src = props.teacher?.nickname || '?'
  return String(src).slice(0, 1).toUpperCase()
})

const avatarAlt = computed(() => {
  const name = props.teacher?.nickname || ''
  return t2('teacherCard.avatarAlt', `Tutor ${name} avatar`, { name })
})

const avatarSize = computed(() => {
  return props.variant === 'compact'
    ? { width: 160, height: 160 }
    : { width: 480, height: 360 }
})

const hasIntroVideo = computed(() => {
  return Boolean(props.teacher?.hasIntroVideo || props.teacher?.introVideoUrl)
})

const ratingDisplay = computed(() => {
  const r = props.teacher?.avgRating
  const count = Number(props.teacher?.reviewCount || 0)
  if (typeof r !== 'number' || Number.isNaN(r) || r <= 0 || count <= 0) return null
  return r.toFixed(1)
})

const ratingAriaLabel = computed(() => {
  return ratingDisplay.value ? `${ratingDisplay.value} stars` : null
})

const ratingMeta = computed(() => {
  if (ratingDisplay.value) {
    const count = Number(props.teacher?.reviewCount || 0)
    return t2('teacherCard.reviewCount', `(${count} reviews)`, { count })
  }
  return t2('teacherCard.noReviews', 'No reviews yet')
})

const lessonMeta = computed(() => {
  const count = Number(props.teacher?.finishedOrderCount || 0)
  if (count > 0) {
    return t2('teacherCard.finishedLessons', `${count} completed lessons`, { count })
  }
  return t2('teacherCard.newTeacherQualified', 'New tutor · verified')
})

const timezoneMeta = computed(() => {
  const tz = props.teacher?.teacherTimezone || props.teacher?.timezone
  if (!tz || props.variant === 'compact') return ''
  const location = String(tz).split('/').pop().replaceAll('_', ' ')
  const offset = timezoneOffset(tz)
  const tpl = t2('teacherCard.timezoneMeta', '{location} · {offset}', { location, offset })
  return tpl.replace('{location}', location).replace('{offset}', offset)
})

function timezoneOffset(tz) {
  try {
    const parts = new Intl.DateTimeFormat('en', {
      timeZone: tz,
      timeZoneName: 'shortOffset'
    }).formatToParts(new Date())
    return (parts.find((p) => p.type === 'timeZoneName')?.value || tz).replace(/^GMT/, 'UTC')
  } catch (e) {
    return tz
  }
}

function onFavoriteClick(evt) {
  evt.preventDefault()
  evt.stopPropagation()
  if (props.showFavorite) {
    emit('click-favorite', { userId: props.teacher?.userId })
  }
}

const linkTo = computed(() => `/teacher/${props.teacher?.userId}`)
</script>

<template>
  <article
    class="teacher-card"
    :class="[`teacher-card--${variant}`]"
  >
    <router-link :to="linkTo" class="teacher-card__link">
      <!-- 头像 -->
      <div class="teacher-card__media">
        <img
          v-if="teacher.avatar"
          :src="teacher.avatar"
          :alt="avatarAlt"
          :width="avatarSize.width"
          :height="avatarSize.height"
          loading="lazy"
          decoding="async"
          class="teacher-card__avatar"
        />
        <div v-else class="teacher-card__avatar teacher-card__avatar--placeholder">
          <span>{{ avatarLetter }}</span>
        </div>

        <!-- 徽章: top-start -->
        <span
          v-if="activeBadge && variant !== 'compact'"
          class="teacher-card__badge"
          :class="[`teacher-card__badge--${activeBadge.type}`]"
        >
          {{ activeBadge.label }}
        </span>

        <span
          v-if="hasIntroVideo && variant !== 'compact'"
          class="teacher-card__video-badge"
        >
          <span aria-hidden="true">▶</span>
          {{ t2('teacherCard.badge.introVideo', 'Video') }}
        </span>

        <!-- 收藏: top-end -->
        <button
          v-if="showFavorite && variant !== 'compact'"
          type="button"
          class="teacher-card__fav"
          :aria-label="t2('teacherCard.favorite', '收藏')"
          disabled
          :title="t2('teacherCard.favoriteSoon', '敬请期待')"
          @click="onFavoriteClick"
        >
          <el-icon><Star /></el-icon>
        </button>
      </div>

      <!-- 信息块 -->
      <div class="teacher-card__body">
        <header class="teacher-card__head">
          <h3 class="teacher-card__name">{{ teacher.nickname }}</h3>
          <span
            v-if="ratingDisplay"
            class="teacher-card__rating"
            :aria-label="ratingAriaLabel"
          >
            <span aria-hidden="true">★</span> {{ ratingDisplay }}
          </span>
        </header>

        <p v-if="variant !== 'compact'" class="teacher-card__proof">
          <span>{{ ratingMeta }}</span>
          <span>{{ lessonMeta }}</span>
        </p>

        <p
          v-if="variant !== 'compact' && teacher.displayAccent"
          class="teacher-card__accent"
          dir="auto"
        >
          {{ teacher.displayAccent }}
        </p>

        <p
          v-if="timezoneMeta"
          class="teacher-card__timezone"
          dir="auto"
        >
          {{ timezoneMeta }}
        </p>

        <ul
          v-if="visibleExpertise.length"
          class="teacher-card__chips"
        >
          <li
            v-for="(chip, idx) in visibleExpertise"
            :key="idx"
            class="teacher-card__chip"
          >
            {{ chip }}
          </li>
        </ul>
      </div>
    </router-link>
  </article>
</template>

<style scoped lang="scss">

.teacher-card {
  background: var(--color-bg-card);
  border-radius: brand.$radius-card;
  overflow: hidden;
  position: relative;
  display: block;
  transition: box-shadow 0.18s ease, transform 0.18s ease;

  &__link {
    display: flex;
    flex-direction: column;
    color: inherit;
    text-decoration: none;
    height: 100%;
  }

  &__media {
    position: relative;
    background: brand.$color-bg-strong;
    overflow: hidden;
  }

  &__avatar {
    display: block;
    width: 100%;
    aspect-ratio: 4 / 3;
    object-fit: cover;

    &--placeholder {
      display: grid;
      place-items: center;
      background: brand.$brand-gradient;
      color: brand.$btn-primary-text;
      font-size: brand.$font-size-3xl;
      font-weight: 700;
    }
  }

  &__video-badge {
    position: absolute;
    inset-inline-end: brand.$spacing-3;
    inset-block-end: brand.$spacing-3;
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-1;
    padding: brand.$spacing-1 brand.$spacing-2;
    border-radius: brand.$radius-pill;
    background: rgba(26, 26, 26, 0.78);
    color: brand.$color-text-inverse;
    font-size: brand.$font-size-xs;
    font-weight: 600;
    z-index: 1;
  }

  &__badge {
    position: absolute;
    inset-block-start: brand.$spacing-3;
    inset-inline-start: brand.$spacing-3;
    padding: brand.$spacing-1 brand.$spacing-2;
    border-radius: brand.$radius-pill;
    font-size: brand.$font-size-xs;
    font-weight: 500;
    line-height: 1;
    z-index: 1;

    &--today {
      background: brand.$brand-primary;
      color: brand.$btn-primary-text;
    }

    &--new {
      background: brand.$color-text-primary;
      color: brand.$color-text-inverse;
    }
  }

  &__fav {
    position: absolute;
    inset-block-start: brand.$spacing-3;
    inset-inline-end: brand.$spacing-3;
    width: 32px;
    height: 32px;
    border: none;
    border-radius: brand.$radius-full;
    background: rgba(255, 255, 255, 0.92);
    color: brand.$color-text-tertiary;
    cursor: not-allowed;
    display: grid;
    place-items: center;
    z-index: 1;

    &:disabled {
      opacity: 0.7;
    }
  }

  &__body {
    padding: brand.$spacing-4 brand.$spacing-5 brand.$spacing-5; // D25: 加大 padding
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
    flex: 1;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: brand.$spacing-3;
  }

  &__name {
    margin: 0;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;
  }

  &__rating {
    color: brand.$color-text-primary;
    font-size: brand.$font-size-base;
    font-weight: 600;
    flex-shrink: 0;

    span {
      color: brand.$color-text-primary;
    }
  }

  &__proof {
    margin: 0;
    display: flex;
    align-items: center;
    gap: brand.$spacing-2;
    flex-wrap: wrap;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-xs;
    line-height: brand.$line-height-base;

    > span + span::before {
      content: '·';
      margin-inline-end: brand.$spacing-2;
      color: brand.$color-border-strong;
    }
  }

  &__accent {
    margin: 0;
    font-size: brand.$font-size-base;
    color: brand.$color-text-secondary;
    line-height: brand.$line-height-base;
  }

  &__timezone {
    margin: 0;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
    line-height: brand.$line-height-base;
  }

  &__chips {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
  }

  &__chip {
    padding: 2px brand.$spacing-3;
    background: brand.$brand-primary-soft;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-xs;
    border-radius: brand.$radius-pill;
    font-weight: 500;
    line-height: brand.$line-height-base;
  }

  // ========================================
  // variant: home — carousel 280px, hover scale
  // ========================================
  &--home {
    width: 280px;
    box-shadow: brand.$shadow-sm;

    &:hover {
      box-shadow: brand.$shadow-md;
      transform: translateY(-2px);
    }
  }

  // ========================================
  // variant: list — 主网格, 100% 宽(D25 浓化)
  // ========================================
  &--list {
    width: 100%;
    box-shadow: brand.$shadow-base;
    border-radius: brand.$radius-xl; // D25: 12 → 16px

    &:hover {
      box-shadow: brand.$shadow-card-hover; // D25: 浮起阴影
      transform: translateY(-2px) scale(1.02);
    }

    // D25: 列表态顶部主色 accent bar(媒体内图片全宽 4:3,
    //      ring 在 overflow:hidden 内不可见,改为顶部 3px 主色条)
    .teacher-card__media {
      border-radius: brand.$radius-xl brand.$radius-xl 0 0;

      &::after {
        content: '';
        position: absolute;
        inset-block-start: 0;
        inset-inline-start: 0;
        inset-inline-end: 0;
        height: 3px;
        background: brand.$brand-gradient-cta;
        z-index: 2;
        pointer-events: none;
      }
    }
  }

  // ========================================
  // variant: compact — 1:1 圆头像, 240px, 极简
  // ========================================
  &--compact {
    width: 240px;
    border: 1px solid brand.$color-border;

    &:hover {
      box-shadow: brand.$shadow-base;
    }

    .teacher-card__link {
      flex-direction: row;
      align-items: center;
      gap: brand.$spacing-3;
      padding: brand.$spacing-3;
    }

    .teacher-card__media {
      flex: 0 0 auto;
      width: 80px;
      background: transparent;
    }

    .teacher-card__avatar {
      width: 80px;
      aspect-ratio: 1 / 1;
      border-radius: brand.$radius-full;
    }

    .teacher-card__body {
      padding: 0;
      gap: brand.$spacing-1;
    }

    .teacher-card__name {
      font-size: brand.$font-size-base;
    }

    .teacher-card__rating {
      font-size: brand.$font-size-sm;
    }
  }
}
</style>
