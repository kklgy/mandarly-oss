<script setup>
/**
 * OrdersView — 教师订单中心(M6 T2 / spec §5.2)
 *
 * 3 tab:待上课 / 已上课 / 已取消
 * 教师视角字段:学生头像 + 昵称 + 上课时间 + 时长 + 课时费 + 结算状态
 * 课前 5 min 高亮「进入课堂」(沿用 classroom 跳转;教师与学生路由都是 /classroom/:orderId)
 * 教师无「取消」按钮(只有学生能取消)
 *
 * 注:学生端 MyOrdersView 的 OrderCard 含「取消 / 退款进度」等学生场景,教师视角字段差异较大,
 * 这里独立写一个轻量教师订单卡(TeacherOrderCard 内联在本 view),不抽公共组件,避免侵入。
 */
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElPagination, ElTabs, ElTabPane, ElButton } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import dayjs from '@/utils/datetime'
import { fromUTC, getUserTimezone, isClassJoinable } from '@/utils/datetime'
import { listTeacherOrders } from '@/api/teacher-center/orders'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()

// ---------- tab 状态 + URL 同步 ----------
// 后端 status 可选 upcoming / finished / cancelled,我们 tab 直接映射
const TABS = ['upcoming', 'finished', 'cancelled']
const activeTab = ref(TABS.includes(route.query.tab) ? route.query.tab : 'upcoming')

watch(activeTab, (tab) => {
  router.replace({ query: { ...route.query, tab } })
  pageNo.value = 1
  load()
})

// ---------- 列表 ----------
const orders = ref([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)
const loading = ref(true)
const errorMsg = ref('')

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const page = await listTeacherOrders({
      status: activeTab.value,
      pageNo: pageNo.value,
      pageSize: pageSize.value
    })
    orders.value = page?.list || []
    total.value = page?.total || 0
  } catch (e) {
    errorMsg.value = e?.message || t('teacherCenter.orders.loadFailed')
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => load())

// ---------- 时间 / 时区显示 ----------
const tz = computed(() => getUserTimezone())

const dateFmt = computed(() => {
  if (locale.value === 'en') return 'MMM D, YYYY · HH:mm'
  if (locale.value === 'ar') return 'D MMMM YYYY · HH:mm'
  return 'YYYY-MM-DD HH:mm'
})

function formatScheduled(utcStr) {
  if (!utcStr) return ''
  return fromUTC(utcStr, tz.value).format(dateFmt.value)
}

// ---------- 「进入课堂」按钮态 ----------
function canJoinClass(order) {
  if (order.status !== 'upcoming') return false
  return isClassJoinable(order.scheduledAt, order.duration || 30)
}

function classButtonText(order) {
  if (canJoinClass(order)) return t('teacherCenter.orders.card.enterClass')
  return t('teacherCenter.orders.card.enterClassNotYet')
}

function onEnterClass(order) {
  if (!canJoinClass(order)) return
  router.push({ name: 'Classroom', params: { orderId: order.id } })
}

// ---------- 结算状态 ----------
function settleLabel(order) {
  if (order.status === 'cancelled') return t('teacherCenter.orders.card.cancelledByStudent')
  switch (order.teacherSettleStatus) {
    case 'frozen': return t('teacherCenter.orders.card.settleFrozen')
    case 'available': return t('teacherCenter.orders.card.settleAvailable')
    case 'reverted': return t('teacherCenter.orders.card.settleCancelled')
    default: return ''
  }
}

function settleClass(order) {
  if (order.status === 'cancelled') return 'is-cancelled'
  switch (order.teacherSettleStatus) {
    case 'frozen': return 'is-frozen'
    case 'available': return 'is-available'
    case 'reverted': return 'is-cancelled'
    default: return ''
  }
}

// ---------- 学生头像首字母兜底 ----------
function avatarLetter(name) {
  return (name || '?').slice(0, 1).toUpperCase()
}

function onPageChange(p) {
  pageNo.value = p
  load()
}
</script>

<template>
  <div class="tc-orders">
    <header class="tc-orders__header">
      <h1 class="tc-orders__title">{{ t('teacherCenter.orders.title') }}</h1>
      <p class="tc-orders__subtitle">{{ t('teacherCenter.orders.subtitle') }}</p>
    </header>

    <el-tabs v-model="activeTab" class="tc-orders__tabs">
      <el-tab-pane :label="t('teacherCenter.orders.tabs.upcoming')" name="upcoming" />
      <el-tab-pane :label="t('teacherCenter.orders.tabs.finished')" name="finished" />
      <el-tab-pane :label="t('teacherCenter.orders.tabs.cancelled')" name="cancelled" />
    </el-tabs>

    <!-- loading / error / empty / list -->
    <div v-if="loading" class="tc-orders__state">
      {{ t('common.loading', { defaultValue: '加载中…' }) }}
    </div>

    <div v-else-if="errorMsg" class="tc-orders__state tc-orders__state--error">
      <p>{{ errorMsg }}</p>
      <el-button size="small" @click="load">
        {{ t('common.retry', { defaultValue: '重试' }) }}
      </el-button>
    </div>

    <EmptyState
      v-else-if="!orders.length"
      variant="no-data"
      :title="t(`teacherCenter.orders.empty.${activeTab}`)"
      description=""
    />

    <ul v-else class="tc-orders__list">
      <li v-for="order in orders" :key="order.id" class="tc-orders__card">
        <!-- 头像 + 学生 -->
        <div class="tc-orders__student">
          <img
            v-if="order.studentAvatar"
            :src="order.studentAvatar"
            :alt="order.studentNickname || ''"
            class="tc-orders__avatar"
            @error="(e) => { e.target.style.display = 'none' }"
          />
          <span v-else class="tc-orders__avatar tc-orders__avatar--text">
            {{ avatarLetter(order.studentNickname) }}
          </span>
          <div class="tc-orders__student-info">
            <span class="tc-orders__student-name">
              {{ order.studentNickname || `${t('teacherCenter.orders.card.student')} #${order.studentId}` }}
            </span>
            <span class="tc-orders__student-meta">
              {{ formatScheduled(order.scheduledAt) }}
              ·
              {{ t('teacherCenter.orders.card.duration', { n: order.duration || 30 }) }}
              <span v-if="order.isFreeTrial" class="tc-orders__free-trial-tag">trial</span>
            </span>
          </div>
        </div>

        <!-- 结算状态 + 课时费 -->
        <div class="tc-orders__fee-block">
          <span class="tc-orders__settle-label" :class="settleClass(order)">
            {{ settleLabel(order) }}
          </span>
          <span v-if="order.teacherAmount != null" class="tc-orders__fee">
            <span class="tc-orders__fee-label">{{ t('teacherCenter.orders.card.feeLabel') }}</span>
            <strong>US$ {{ Number(order.teacherAmount).toFixed(2) }}</strong>
          </span>
        </div>

        <!-- 操作 -->
        <div v-if="order.status === 'upcoming'" class="tc-orders__actions">
          <el-button
            type="primary"
            :disabled="!canJoinClass(order)"
            @click="onEnterClass(order)"
          >
            {{ classButtonText(order) }}
          </el-button>
        </div>
      </li>
    </ul>

    <!-- 分页(PC 显示;H5 简化为「加载更多」按钮 — 当前先用 EP pagination,M5 已有 H5 适配 SCSS) -->
    <div v-if="!loading && total > pageSize" class="tc-orders__pagination">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNo"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.tc-orders {
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }

  &__header {
    margin-block-end: brand.$spacing-4;
  }

  &__title {
    margin: 0 0 brand.$spacing-1;
    font-size: brand.$font-size-2xl;
    font-weight: 700;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;
  }

  &__subtitle {
    margin: 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
  }

  &__tabs {
    margin-block-end: brand.$spacing-4;
  }

  &__state {
    text-align: center;
    padding: brand.$spacing-12 brand.$spacing-4;
    color: brand.$color-text-secondary;

    &--error {
      color: brand.$color-error;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: brand.$spacing-3;
    }
  }

  &__list {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
  }

  &__card {
    display: grid;
    grid-template-columns: 1fr auto auto;
    align-items: center;
    gap: brand.$spacing-4;
    padding: brand.$spacing-4;
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    box-shadow: brand.$shadow-sm;

    @media (max-width: brand.$bp-tablet) {
      grid-template-columns: 1fr;
      gap: brand.$spacing-3;
    }
  }

  &__student {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    min-width: 0;
  }

  &__avatar {
    width: 44px;
    height: 44px;
    border-radius: brand.$radius-full;
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
    flex-shrink: 0;
    object-fit: cover;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: brand.$font-size-md;

    &--text {
      // 兜底首字母时不渲染 img
    }
  }

  &__student-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__student-name {
    font-weight: 600;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-base;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__student-meta {
    font-size: brand.$font-size-sm;
    color: brand.$color-text-tertiary;
  }

  &__free-trial-tag {
    display: inline-block;
    margin-inline-start: brand.$spacing-1;
    padding: 0 brand.$spacing-1;
    background: brand.$color-info;
    color: brand.$color-text-inverse;
    border-radius: brand.$radius-sm;
    font-size: brand.$font-size-xs;
    text-transform: uppercase;
    font-weight: 600;
  }

  &__fee-block {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: brand.$spacing-1;
    text-align: end;

    @media (max-width: brand.$bp-tablet) {
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
      text-align: start;
    }
  }

  &__settle-label {
    font-size: brand.$font-size-sm;
    padding: 2px brand.$spacing-2;
    border-radius: brand.$radius-sm;
    font-weight: 600;

    &.is-frozen {
      background: rgba(250, 173, 20, 0.12);
      color: brand.$color-warning;
    }

    &.is-available {
      background: rgba(82, 196, 26, 0.12);
      color: brand.$color-success;
    }

    &.is-cancelled {
      background: brand.$color-bg-strong;
      color: brand.$color-text-tertiary;
    }
  }

  &__fee {
    display: flex;
    align-items: baseline;
    gap: brand.$spacing-1;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-md;
  }

  &__fee-label {
    color: brand.$color-text-tertiary;
    font-size: brand.$font-size-xs;
  }

  &__actions {
    display: flex;
    justify-content: flex-end;

    @media (max-width: brand.$bp-tablet) {
      justify-content: stretch;

      .el-button {
        flex: 1;
      }
    }
  }

  &__pagination {
    margin-block-start: brand.$spacing-6;
    display: flex;
    justify-content: center;
  }
}
</style>
