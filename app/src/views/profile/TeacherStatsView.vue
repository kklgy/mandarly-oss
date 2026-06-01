<script setup>
// =============================================================================
// TeacherStatsView.vue — /profile/teacher-stats(仅 role=teacher)
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § /profile/teacher-stats 详细规格
//
// 内容:
//   §A 3 数字卡(平均评分 / 已完成课时 / 评价数)
//   §B 本月趋势(P0 纯文字摘要,P2 接 ECharts 月度柱状图)
//
// 路由守卫:meta.teacherOnly = true(在 router 配置)
//   本组件 setup 内补 兜底:role !== 'teacher' → router.replace('/profile')
//
// API:
//   - GET /app-api/edu/review/teacher/me/stat 已存在 → getMyTeacherStat()
//     返 { avgRating, reviewCount, finishedOrderCount }
//   - 月度统计(本月已完成 / 待上)P0 复用 booking API 或后端补 endpoint
//     (暂不实现,占位文案 — 后端 endpoint 待确定)
// =============================================================================
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Star, Tickets, ChatLineRound } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'
import { getMyTeacherStat } from '@/api/review'
import StatCard from '@/components/StatCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const { profile } = storeToRefs(userStore)

const loading = ref(false)
const errorMsg = ref('')
const stat = ref(null) // { avgRating, reviewCount, finishedOrderCount }

const isTeacher = computed(() => profile.value?.role === 'teacher')

onMounted(async () => {
  // 角色兜底守卫(router.beforeEnter 是首选,本组件再补一次)
  if (!isTeacher.value) {
    router.replace('/profile')
    return
  }
  await loadStat()
})

async function loadStat() {
  loading.value = true
  errorMsg.value = ''
  try {
    stat.value = await getMyTeacherStat()
  } catch {
    errorMsg.value = t('common.error')
  } finally {
    loading.value = false
  }
}

const ratingDisplay = computed(() => {
  if (!stat.value || !stat.value.reviewCount) return '—'
  return Number(stat.value.avgRating).toFixed(1)
})

function handleBack() {
  router.push('/profile')
}
</script>

<template>
  <div class="teacher-stats-view" v-loading="loading">
    <!-- H5 子页返回(PC 不显示) -->
    <header class="teacher-stats-view__topbar">
      <button class="teacher-stats-view__back" type="button" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ t('profile.menu.teacherStats') }}</span>
      </button>
    </header>

    <div v-if="errorMsg" class="teacher-stats-view__error">
      <EmptyState
        variant="error"
        :title="t('common.networkError.title')"
        :description="errorMsg"
      >
        <template #action>
          <el-button type="primary" @click="loadStat">{{ t('common.retry') }}</el-button>
        </template>
      </EmptyState>
    </div>

    <template v-else>
      <!-- §A 3 数字卡 -->
      <section class="teacher-stats-view__section">
        <h2 class="teacher-stats-view__heading">{{ t('profile.teacherStats.title') }}</h2>

        <div class="teacher-stats-view__stats">
          <StatCard
            color="primary"
            :icon="Star"
            :value="ratingDisplay"
            :label="t('profile.teacherStats.stat.avgRating')"
          />
          <StatCard
            color="primary"
            :icon="Tickets"
            :value="stat?.finishedOrderCount ?? 0"
            :label="t('profile.teacherStats.stat.lessonCount')"
          />
          <StatCard
            color="primary"
            :icon="ChatLineRound"
            :value="stat?.reviewCount ?? 0"
            :label="t('profile.teacherStats.stat.reviewCount')"
          />
        </div>
      </section>

      <!-- §B 本月趋势(P0 占位) -->
      <section class="teacher-stats-view__section">
        <h2 class="teacher-stats-view__heading">
          {{ t('profile.teacherStats.monthlyTrend') }}
        </h2>
        <p class="teacher-stats-view__placeholder">
          {{ t('profile.teacherStats.monthlyPlaceholder') }}
        </p>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">

.teacher-stats-view {
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }
}

.teacher-stats-view__topbar {
  margin-block-end: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    display: none;
  }
}

.teacher-stats-view__back {
  background: transparent;
  border: none;
  color: brand.$brand-primary-deep;
  font: inherit;
  font-size: brand.$font-size-md;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;

  &:hover { color: brand.$brand-primary; }
}

.teacher-stats-view__error {
  padding: brand.$spacing-6 0;
}

.teacher-stats-view__section {
  background: brand.$color-bg-card;
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6;
  margin-block-end: brand.$spacing-5;
  box-shadow: brand.$shadow-base;
}

.teacher-stats-view__heading {
  margin: 0 0 brand.$spacing-5;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  color: brand.$color-text-primary;
}

.teacher-stats-view__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: brand.$spacing-3;

  @media (max-width: brand.$bp-mobile) {
    grid-template-columns: 1fr;
  }
}

.teacher-stats-view__placeholder {
  margin: 0;
  font-size: brand.$font-size-base;
  color: brand.$color-text-secondary;
  line-height: brand.$line-height-base;
}
</style>
