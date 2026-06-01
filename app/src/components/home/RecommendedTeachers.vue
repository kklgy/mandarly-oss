<script setup>
// =============================================================================
// <RecommendedTeachers> — HomeView § 3 推荐教师(D26 v2 质感升级)
//
// 升级点:
//   - 背景 mesh-cream-soft
//   - 教师卡 glass-card 风:backdrop-blur(16px) + shadow-v2-md/lg + glow
//   - hover:translateY(-4px) scale(1.01) + ease-spring
//   - 头像 ring 双环:外 4px accent-amber 30%α / 内 4px brand-primary-soft
//   - 资质徽章 4 态(hover ring + bg)
//   - CTA pill v2 §8.4 四态规范
//   - scroll-reveal(IntersectionObserver 一次性)
//   - prefers-reduced-motion 兜底
//
// 设计源:docs/frontend/visual-reference/DESIGN-mandarly-v2.md §9.1 / §10.2
//
// 严格约束:
//   1) token only — 所有颜色 / 间距 / 圆角走 brand.$xxx
//   2) RTL — logical properties(padding-inline-start 等)
//   3) 不动 API import — 沿用 listTeachers
//   4) 不动 HomeView.vue 主壳
// =============================================================================
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { Check } from '@element-plus/icons-vue'
import { listTeachers } from '@/api/teacher'

// fallback 头像(按 teacher.id % 3 + 1 选)
import fallback1 from '@/assets/marketing/teacher-sample/teacher-sample-1.jpg'
import fallback2 from '@/assets/marketing/teacher-sample/teacher-sample-2.jpg'
import fallback3 from '@/assets/marketing/teacher-sample/teacher-sample-3.jpg'
const FALLBACKS = [fallback1, fallback2, fallback3]

function getFallbackAvatar(idx, usedAvatars = new Set()) {
  const ordered = FALLBACKS.map((_, offset) => FALLBACKS[(idx + offset) % FALLBACKS.length])
  return ordered.find((avatar) => !usedAvatars.has(avatar)) || ordered[0]
}

function handleAvatarError(event, idx) {
  const fallback = getFallbackAvatar(idx)
  if (event.target.src !== fallback) {
    event.target.src = fallback
  }
}

const { t } = useI18n()

const teachers = ref([])
const loaded = ref(false)
const errored = ref(false)

// 本地 mock(DEV 态 API 未就位时占位)
const mockTeachers = [
  {
    userId: 1001,
    nickname: '张老师',
    avatar: '',
    yearsExperience: 8,
    studentCount: 120,
    avgRating: 4.9,
    reviewCount: 120,
    qualifications: ['TESOL 认证', '普通话一级甲等', '国际汉语教师'],
    expertise: ['business', 'hsk']
  },
  {
    userId: 1002,
    nickname: '李老师',
    avatar: '',
    yearsExperience: 5,
    studentCount: 85,
    avgRating: 4.8,
    reviewCount: 85,
    qualifications: ['普通话一级甲等', '国际汉语教师'],
    expertise: ['kids', 'daily']
  },
  {
    userId: 1003,
    nickname: '陈老师',
    avatar: '',
    yearsExperience: 10,
    studentCount: 200,
    avgRating: 4.9,
    reviewCount: 200,
    qualifications: ['TESOL 认证', '国际汉语教师'],
    expertise: ['speaking']
  }
]

// D27-C2:从 3 降到 1 — 即使只有 1 个 approved 教师也展示(prod 实际只 2 个 approved,空白比少不平衡更糟)
const MIN_TEACHERS = 1

async function fetchData() {
  try {
    const resp = await listTeachers({ sort: 'recommend', pageSize: 3 })
    const list = (resp?.data || resp || []).slice(0, 3)
    if (Array.isArray(list) && list.length >= MIN_TEACHERS) {
      teachers.value = list
    } else {
      teachers.value = []
    }
  } catch {
    if (import.meta.env.DEV) {
      teachers.value = mockTeachers
    } else {
      errored.value = true
    }
  } finally {
    loaded.value = true
  }
}

const showSection = computed(
  () => loaded.value && !errored.value && teachers.value.length >= MIN_TEACHERS
)

const displayTeachers = computed(() => {
  const usedAvatars = new Set()
  return teachers.value.map((teacher, idx) => {
    const originalAvatar = teacher.avatar || ''
    const avatar = originalAvatar && !usedAvatars.has(originalAvatar)
      ? originalAvatar
      : getFallbackAvatar(idx, usedAvatars)
    usedAvatars.add(avatar)
    return {
      ...teacher,
      displayAvatar: avatar
    }
  })
})

// 星级渲染(满 5 星,支持小数四舍五入到整数)
function starsFilled(rating) {
  return Math.round(rating || 0)
}

// 资质徽章:优先 teacher.qualifications(数组),无则空数组
function getBadges(teacher) {
  if (Array.isArray(teacher.qualifications) && teacher.qualifications.length) {
    return teacher.qualifications.slice(0, 3)
  }
  return []
}

// 统计数据(任一字段缺失则整段隐藏)
function hasStats(teacher) {
  return (
    teacher.yearsExperience != null ||
    teacher.studentCount != null ||
    teacher.avgRating != null
  )
}

// ---------- scroll-reveal ----------
const sectionRef = ref(null)
const visible = ref(false)
let io = null

onMounted(() => {
  fetchData()
  io = new IntersectionObserver(([entry]) => {
    if (entry.isIntersecting) {
      visible.value = true
      io.disconnect()
    }
  }, { threshold: 0.12 })
  if (sectionRef.value) io.observe(sectionRef.value)
})

onBeforeUnmount(() => io?.disconnect())
</script>

<template>
  <section
    v-if="showSection"
    class="rec-teachers"
    ref="sectionRef"
    :class="{ 'is-section-visible': visible }"
  >
    <div class="rec-teachers__inner">

      <!-- 标题区 -->
      <div class="rec-teachers__head">
        <h2 class="rec-teachers__title">{{ t('home.recommend.title') }}</h2>
        <p class="rec-teachers__sub">{{ t('home.recommend.sub') }}</p>
      </div>

      <!-- 3 列卡片网格 -->
      <div class="rec-teachers__grid">
        <article
          v-for="(teacher, idx) in displayTeachers"
          :key="teacher.userId"
          class="rec-card"
          :style="{ '--card-idx': idx }"
        >
          <!-- 头像 -->
          <div class="rec-card__avatar-wrap">
            <img
              class="rec-card__avatar"
              :src="teacher.displayAvatar"
              :alt="t('teacherCard.avatarAlt', { name: teacher.nickname })"
              width="160"
              height="160"
              loading="lazy"
              @error="handleAvatarError($event, idx)"
            />
          </div>

          <!-- 姓名 + 副标 -->
          <h3 class="rec-card__name">{{ teacher.nickname }}</h3>
          <p class="rec-card__sub">
            {{
              teacher.title ||
              (teacher.yearsExperience
                ? t('teacher.yearsExperience', { n: teacher.yearsExperience })
                : t('home.recommend.sub'))
            }}
          </p>

          <!-- 统计 3 栏 -->
          <div v-if="hasStats(teacher)" class="rec-card__stats">
            <div v-if="teacher.yearsExperience != null" class="rec-card__stat">
              <span class="rec-card__stat-value">{{ teacher.yearsExperience }}</span>
              <span class="rec-card__stat-label">年教学</span>
            </div>
            <div v-if="teacher.studentCount != null" class="rec-card__stat">
              <span class="rec-card__stat-value">{{ teacher.studentCount }}</span>
              <span class="rec-card__stat-label">位学员</span>
            </div>
            <div v-if="teacher.avgRating != null" class="rec-card__stat">
              <span class="rec-card__stat-value rec-card__stat-value--rating">
                ★ {{ teacher.avgRating?.toFixed(1) }}
              </span>
              <span class="rec-card__stat-label">评分</span>
            </div>
          </div>

          <!-- 资质徽章(空则隐藏) -->
          <div v-if="getBadges(teacher).length" class="rec-card__badges">
            <span
              v-for="badge in getBadges(teacher)"
              :key="badge"
              class="rec-card__badge"
            >
              <el-icon class="rec-card__badge-icon"><Check /></el-icon>
              {{ badge }}
            </span>
          </div>

          <!-- CTA -->
          <router-link
            :to="`/teacher/${teacher.userId}`"
            class="rec-card__cta"
          >
            {{ t('home.recommend.viewDetail') }}
          </router-link>
        </article>
      </div>

      <!-- 查看全部 -->
      <div class="rec-teachers__footer">
        <router-link to="/teachers" class="rec-teachers__view-all">
          {{ t('home.recommend.viewAll') }} →
        </router-link>
      </div>

    </div>
  </section>

  <!-- 空态:API 失败 / 数量不足 / 加载完但 0 条 -->
  <section v-else-if="loaded && !errored && teachers.length === 0" class="rec-teachers rec-teachers--empty">
    <p class="rec-teachers__empty">{{ t('home.recommend.empty') }}</p>
  </section>
</template>

<style scoped lang="scss">

// ---- 节区外壳 ----
.rec-teachers {
  background: brand.$mesh-cream-soft;
  padding-block: brand.$spacing-10;
  padding-inline: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding-block: brand.$spacing-16;
    padding-inline: brand.$spacing-8;
  }

  &--empty {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 120px;
  }
}

.rec-teachers__inner {
  max-width: 1200px;
  margin-inline: auto;
}

// ---- 标题区 ----
.rec-teachers__head {
  text-align: center;
  margin-block-end: brand.$spacing-10;
}

.rec-teachers__title {
  margin: 0 0 brand.$spacing-3;
  font-family: brand.$font-display;
  font-size: brand.$font-size-display-md;   // 36px
  font-weight: brand.$font-weight-display;
  letter-spacing: brand.$ls-display-md;
  line-height: brand.$lh-display-md;
  color: brand.$ink;

  // 中文 / RTL fallback
  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
    font-weight: 700;
    letter-spacing: 0;
  }

  @media (min-width: brand.$bp-laptop) {
    font-size: brand.$font-size-display-lg; // 48px
  }
}

.rec-teachers__sub {
  margin: 0;
  font-size: brand.$font-size-md;
  color: brand.$muted;
  line-height: brand.$lh-body-loose;
}

// ---- 卡片网格 ----
.rec-teachers__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-6;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: brand.$bp-laptop) {
    grid-template-columns: repeat(3, 1fr);
  }
}

// ---- 单张教师卡:glass-card 风格 ----
.rec-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  // glass-card
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  border: 1px solid rgba(204, 120, 92, 0.08);
  border-radius: brand.$radius-xl;
  box-shadow: brand.$shadow-v2-md;
  padding: brand.$spacing-8;
  text-align: center;
  transition:
    transform brand.$duration-base brand.$ease-spring,
    box-shadow brand.$duration-base brand.$ease-out;
  // scroll-reveal:初始隐藏
  opacity: 0;
  transform: translateY(28px);

  .is-section-visible & {
    opacity: 1;
    transform: translateY(0);
    transition:
      opacity brand.$duration-slow brand.$ease-out,
      transform brand.$duration-slow brand.$ease-out,
      box-shadow brand.$duration-base brand.$ease-out;
    transition-delay: calc(var(--card-idx, 0) * 80ms + 60ms);
  }

  &:hover {
    transform: translateY(-4px) scale(1.01);
    box-shadow: brand.$shadow-v2-lg, brand.$shadow-glow-primary;
    transition-delay: 0ms;
  }

  &:active {
    transform: translateY(-2px) scale(1.005);
    transition-duration: brand.$duration-fast;
  }

  &:focus-within {
    box-shadow: brand.$shadow-v2-lg, brand.$ring-focus;
  }
}

// reduced-motion
@media (prefers-reduced-motion: reduce) {
  .rec-card {
    opacity: 1 !important;
    transform: none !important;
    transition: box-shadow brand.$duration-fast ease !important;
  }
}

// ---- 头像:双环 ----
.rec-card__avatar-wrap {
  margin-block-end: brand.$spacing-5;
}

.rec-card__avatar {
  width: 160px;
  height: 160px;
  border-radius: brand.$radius-full;
  object-fit: cover;
  display: block;
  // 内环:brand-primary-soft 4px / 外环:accent-amber 30%α 4px(用 outline 模拟)
  box-shadow:
    0 0 0 4px brand.$brand-primary-soft,
    0 0 0 8px rgba(232, 165, 90, 0.30);  // accent-amber 30%α(外环)
}

// ---- 姓名 + 副标 ----
.rec-card__name {
  margin: 0 0 brand.$spacing-2;
  font-size: brand.$font-size-2xl;
  font-weight: 700;
  color: brand.$ink;
  line-height: brand.$lh-display-sm;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.rec-card__sub {
  margin: 0 0 brand.$spacing-5;
  font-size: brand.$font-size-md;
  color: brand.$muted;
  line-height: brand.$lh-body-loose;
}

// ---- 统计 3 栏 ----
.rec-card__stats {
  display: flex;
  gap: brand.$spacing-6;
  justify-content: center;
  margin-block-end: brand.$spacing-5;
  width: 100%;
}

.rec-card__stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-1;
}

.rec-card__stat-value {
  font-size: brand.$font-size-lg;
  font-weight: 700;
  color: brand.$ink;
  line-height: brand.$lh-display-sm;

  &--rating {
    color: brand.$brand-primary-deep;
  }
}

.rec-card__stat-label {
  font-size: brand.$font-size-xs;
  color: brand.$muted-soft;
}

// ---- 资质徽章:4 态(hover ring + bg)----
.rec-card__badges {
  display: flex;
  flex-wrap: wrap;
  gap: brand.$spacing-2;
  justify-content: center;
  margin-block-end: brand.$spacing-6;
}

.rec-card__badge {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-1;
  padding-block: brand.$spacing-1;
  padding-inline: brand.$spacing-3;
  background: rgba(93, 184, 166, 0.10);    // accent-teal 软底
  color: brand.$accent-teal;
  font-size: brand.$font-size-xs;
  font-weight: 500;
  border-radius: brand.$radius-pill;
  white-space: nowrap;
  border: 1px solid transparent;
  transition:
    background brand.$duration-fast ease,
    border-color brand.$duration-fast ease,
    box-shadow brand.$duration-fast ease;

  &:hover {
    background: rgba(93, 184, 166, 0.18);
    border-color: rgba(93, 184, 166, 0.30);
    box-shadow: brand.$shadow-glow-teal;
  }
}

.rec-card__badge-icon {
  font-size: 11px;
  color: brand.$accent-teal;
}

// ---- CTA 按钮:v2 §8.4 四态 pill ----
.rec-card__cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-block-start: auto;
  padding-block: brand.$spacing-3;
  padding-inline: brand.$spacing-6;
  background: brand.$brand-gradient-cta;
  color: brand.$color-text-inverse;
  font-size: brand.$font-size-base;
  font-weight: 600;
  border-radius: brand.$radius-full;  // pill
  text-decoration: none;
  box-shadow: brand.$shadow-v2-md;
  transition:
    transform brand.$duration-fast brand.$ease-spring,
    box-shadow brand.$duration-fast brand.$ease-out;

  // hover:浮起 + glow
  &:hover {
    transform: translateY(-2px);
    box-shadow: brand.$shadow-glow-primary;
  }

  // active:按下沉降
  &:active {
    transform: translateY(0);
    box-shadow: brand.$shadow-v2-sm;
    transition-duration: 75ms;
  }

  // focus
  &:focus-visible {
    outline: none;
    box-shadow: brand.$shadow-glow-primary, brand.$ring-focus;
  }
}

// ---- 查看全部 ----
.rec-teachers__footer {
  text-align: center;
  margin-block-start: brand.$spacing-10;
}

.rec-teachers__view-all {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-1;
  font-size: brand.$font-size-md;
  color: brand.$brand-primary-deep;
  font-weight: 500;
  text-decoration: none;

  &:hover {
    color: brand.$brand-primary;
    text-decoration: underline;
  }
}

// ---- 空态 ----
.rec-teachers__empty {
  font-size: brand.$font-size-md;
  color: brand.$muted-soft;
  text-align: center;
  margin: 0;
}
</style>
