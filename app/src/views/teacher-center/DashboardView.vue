<script setup>
/**
 * DashboardView — 教师中心首页(M6 §4 / spec §5.8 / D16 P2)
 *
 * 4 数据卡:本周课次 / 本月收入 / 待结算 / 评价分
 *   数据源 = 后端单一聚合 endpoint `/edu/teacher-center/dashboard/summary`
 *   D16 P2 之前用 Promise.allSettled 三接口 + 前端 filter 实现,口径不准且 N+1。
 * 4 快捷入口:排课 / 订单 / 收入 / 提现
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { Calendar, Document, Money, Wallet } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getTeacherDashboardSummary } from '@/api/teacher-center/dashboard'
import IncomeStatCard from '@/components/teacher-center/IncomeStatCard.vue'
import AuditStatusBanner from '@/components/teacher-center/AuditStatusBanner.vue'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const { profile } = storeToRefs(userStore)

const welcomeName = computed(() => profile.value?.nickname || profile.value?.email || '')

// ---------- 数据 ----------
const summary = ref({
  weeklyClassCount: 0,
  monthlyIncomeUsd: 0,
  pendingSettleUsd: 0,
  totalEarnedUsd: 0,
  availableUsd: 0,
  ratingAvg: null,
  ratingCount: 0
})
const loading = ref(true)

async function loadAll() {
  loading.value = true
  try {
    const data = await getTeacherDashboardSummary()
    if (data) {
      summary.value = {
        weeklyClassCount: Number(data.weeklyClassCount ?? 0),
        monthlyIncomeUsd: Number(data.monthlyIncomeUsd ?? 0),
        pendingSettleUsd: Number(data.pendingSettleUsd ?? 0),
        totalEarnedUsd: Number(data.totalEarnedUsd ?? 0),
        availableUsd: Number(data.availableUsd ?? 0),
        ratingAvg: data.ratingAvg ?? null,
        ratingCount: Number(data.ratingCount ?? 0)
      }
    }
  } catch (e) {
    console.warn('[TeacherDashboard] summary load failed', e)
  } finally {
    loading.value = false
  }
}

const ratingDisplay = computed(() => {
  if (!summary.value.ratingCount) return '—'
  return Number(summary.value.ratingAvg).toFixed(1)
})

// ---------- 快捷入口 ----------
const QUICK_ACTIONS = [
  { key: 'schedule', path: '/teacher-center/schedule', icon: Calendar, i18n: 'teacherCenter.dashboard.quickActionSchedule' },
  { key: 'orders', path: '/teacher-center/orders', icon: Document, i18n: 'teacherCenter.dashboard.quickActionOrders' },
  { key: 'income', path: '/teacher-center/income', icon: Money, i18n: 'teacherCenter.dashboard.quickActionIncome' },
  { key: 'withdrawal', path: '/teacher-center/withdrawal', icon: Wallet, i18n: 'teacherCenter.dashboard.quickActionWithdrawal' }
]

onMounted(() => loadAll())
</script>

<template>
  <div class="tc-dash">
    <!-- 审核状态横幅(D19 Phase B / spec §B8):pending 黄、rejected 红 + 去补充,approved 收起 -->
    <AuditStatusBanner
      :status="profile?.teacherAuditStatus"
      :reason="profile?.teacherRejectReason || ''"
    />

    <header class="tc-dash__header">
      <h1 class="tc-dash__title">{{ t('teacherCenter.dashboard.title') }}</h1>
      <p class="tc-dash__welcome">{{ t('teacherCenter.dashboard.welcome', { name: welcomeName }) }}</p>
    </header>

    <!-- 4 数据卡 -->
    <section class="tc-dash__stats" aria-label="teacher stats">
      <!-- 本周课次 -->
      <div class="tc-dash__stat-card tc-dash__stat-card--num">
        <span class="tc-dash__stat-label">{{ t('teacherCenter.dashboard.weeklyClassesLabel') }}</span>
        <strong class="tc-dash__stat-value">{{ loading ? '—' : summary.weeklyClassCount }}</strong>
      </div>

      <!-- 本月收入(按教师时区当月窗口聚合,D16 P2 修正自累计入账) -->
      <IncomeStatCard
        :label="t('teacherCenter.dashboard.monthlyIncomeLabel')"
        :amount="summary.monthlyIncomeUsd"
        variant="default"
      />

      <!-- 待结算(在途 T+7) -->
      <IncomeStatCard
        :label="t('teacherCenter.dashboard.pendingSettleLabel')"
        :amount="summary.pendingSettleUsd"
        variant="warning"
      />

      <!-- 评价分 -->
      <div class="tc-dash__stat-card tc-dash__stat-card--rating">
        <span class="tc-dash__stat-label">{{ t('teacherCenter.dashboard.ratingLabel') }}</span>
        <span class="tc-dash__stat-value">
          ⭐ {{ ratingDisplay }}
          <small v-if="summary.ratingCount">({{ summary.ratingCount }})</small>
        </span>
      </div>
    </section>

    <!-- 4 快捷入口 -->
    <section class="tc-dash__quick" aria-label="quick actions">
      <h2 class="tc-dash__subtitle">{{ t('teacherCenter.dashboard.quickActionsTitle') }}</h2>
      <div class="tc-dash__quick-grid">
        <button
          v-for="qa in QUICK_ACTIONS"
          :key="qa.key"
          type="button"
          class="tc-dash__quick-item"
          @click="router.push(qa.path)"
        >
          <el-icon class="tc-dash__quick-icon"><component :is="qa.icon" /></el-icon>
          <span class="tc-dash__quick-label">{{ t(qa.i18n) }}</span>
        </button>
      </div>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.tc-dash {
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }

  &__header {
    margin-block-end: brand.$spacing-6;
  }

  &__title {
    margin: 0 0 brand.$spacing-2;
    font-size: brand.$font-size-2xl;
    font-weight: 700;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;
  }

  &__welcome {
    margin: 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-base;
  }

  &__subtitle {
    margin: 0 0 brand.$spacing-4;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  // 4 卡 grid
  &__stats {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: brand.$spacing-3;
    margin-block-end: brand.$spacing-6;

    @media (min-width: brand.$bp-laptop) {
      grid-template-columns: repeat(4, 1fr);
      gap: brand.$spacing-4;
    }
  }

  &__stat-card {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
    padding: brand.$spacing-5;
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    box-shadow: brand.$shadow-sm;
    min-height: 120px;

    &--num .tc-dash__stat-value {
      font-size: brand.$font-size-3xl;
      font-weight: 700;
      color: brand.$brand-primary-deep;
    }

    &--rating .tc-dash__stat-value {
      font-size: brand.$font-size-2xl;
      font-weight: 600;
      color: brand.$color-text-primary;

      small {
        font-size: brand.$font-size-sm;
        color: brand.$color-text-tertiary;
        margin-inline-start: brand.$spacing-1;
      }
    }
  }

  &__stat-label {
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
  }

  &__stat-value {
    line-height: brand.$line-height-tight;
  }

  // 快捷入口
  &__quick-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: brand.$spacing-4;

    @media (min-width: brand.$bp-tablet) {
      grid-template-columns: repeat(4, 1fr);
    }
  }

  &__quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: brand.$spacing-2;
    padding: brand.$spacing-6 brand.$spacing-3;
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-base;
    cursor: pointer;
    transition: border-color 0.15s, box-shadow 0.15s, transform 0.15s;

    &:hover {
      border-color: brand.$brand-primary;
      box-shadow: brand.$shadow-md;
      transform: translateY(-1px);
    }
  }

  &__quick-icon {
    font-size: 28px;
    color: brand.$brand-primary-deep;
  }

  &__quick-label {
    font-weight: 500;
  }
}
</style>
