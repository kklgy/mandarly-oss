<script setup>
/**
 * AppTabBar — H5 + 登录态底部 tabbar(plan Wave 1.2 + M6 双套)
 *
 * 显示条件由 AppLayout 控制:仅当 (isH5 && isLoggedIn) 时挂载
 * 高度 56px + iOS safe-area
 *
 * 双套 tab(M6 §3 spec §4.5):
 *   - 学生套:首页 / 教师 / 订单 / 我的(badge 走 useBookingCountsStore.upcomingCount)
 *   - 教师套:排课 / 订单 / 收入 / 我的(教师中心子页 startsWith('/teacher-center') 切换)
 *
 * RTL:flex 自动镜像
 */
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { HomeFilled, Avatar, Calendar, User, Document, Money } from '@element-plus/icons-vue'
import { useBookingCountsStore } from '@/stores/bookingCounts'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const countsStore = useBookingCountsStore()
const { upcomingCount } = storeToRefs(countsStore)

// 学生 tab(原有)
const STUDENT_TABS = [
  { key: 'home', path: '/', icon: HomeFilled, i18n: 'tabbar.home' },
  { key: 'teachers', path: '/teachers', icon: Avatar, i18n: 'tabbar.teachers' },
  { key: 'myOrders', path: '/orders', icon: Calendar, i18n: 'tabbar.myOrders', badge: true },
  { key: 'profile', path: '/profile', icon: User, i18n: 'tabbar.profile' }
]

// 教师 tab(M6)— 4 项:排课 / 订单 / 收入 / 我的
// 「我的」仍回 /profile(用户中心,通用账户管理走 student 区),教师中心首页通过 AppHeader 教师 chip / role-chip 入
const TEACHER_TABS = [
  { key: 'tc-schedule', path: '/teacher-center/schedule', icon: Calendar, i18n: 'teacherCenter.nav.schedule' },
  { key: 'tc-orders', path: '/teacher-center/orders', icon: Document, i18n: 'teacherCenter.nav.orders' },
  { key: 'tc-income', path: '/teacher-center/income', icon: Money, i18n: 'teacherCenter.nav.income' },
  { key: 'profile', path: '/profile', icon: User, i18n: 'tabbar.profile' }
]

const isTeacherCenter = computed(() => route.path.startsWith('/teacher-center'))
const tabs = computed(() => (isTeacherCenter.value ? TEACHER_TABS : STUDENT_TABS))

const activeKey = computed(() => {
  if (isTeacherCenter.value) {
    if (route.path.startsWith('/teacher-center/schedule')) return 'tc-schedule'
    // orders + 详情等子路径
    if (route.path.startsWith('/teacher-center/orders')) return 'tc-orders'
    if (route.path.startsWith('/teacher-center/income')) return 'tc-income'
    // withdrawal 不在 tab 里(收入 → 申请 → 记录),不高亮 tab
    if (route.path === '/profile' || route.path.startsWith('/profile/')) return 'profile'
    return ''
  }
  // 学生套(原有逻辑)
  if (route.path === '/') return 'home'
  if (route.path.startsWith('/teachers') || route.path.startsWith('/teacher/')) return 'teachers'
  if (route.path === '/orders' || route.path.startsWith('/orders/')) return 'myOrders'
  if (route.path === '/profile' || route.path.startsWith('/profile/') || route.path.startsWith('/my/')) return 'profile'
  return ''
})

onMounted(() => countsStore.fetch())

function go(tab) {
  if (route.path !== tab.path) router.push(tab.path)
}
</script>

<template>
  <nav class="app-tabbar" aria-label="primary mobile navigation">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      class="app-tabbar__item"
      :class="{ 'is-active': activeKey === tab.key }"
      @click="go(tab)"
    >
      <span class="app-tabbar__icon-wrap">
        <el-badge
          v-if="tab.badge"
          :value="upcomingCount"
          :hidden="upcomingCount === 0"
          :max="99"
          class="app-tabbar__badge"
        >
          <el-icon class="app-tabbar__icon"><component :is="tab.icon" /></el-icon>
        </el-badge>
        <el-icon v-else class="app-tabbar__icon"><component :is="tab.icon" /></el-icon>
      </span>
      <span class="app-tabbar__label">{{ t(tab.i18n) }}</span>
    </button>
  </nav>
</template>

<style lang="scss" scoped>
.app-tabbar {
  position: fixed;
  inset-inline: 0;
  inset-block-end: 0;
  z-index: 1030;
  background: var(--color-bg-card);
  border-block-start: 1px solid var(--color-border);
  box-shadow: 0 -1px 4px rgba(0, 0, 0, 0.04);
  padding-block-end: env(safe-area-inset-bottom, 0px);
  display: flex;
  align-items: stretch;
  justify-content: space-around;

  &__item {
    flex: 1;
    background: transparent;
    border: none;
    padding: brand.$spacing-1 brand.$spacing-2 brand.$spacing-2;
    cursor: pointer;
    color: var(--color-text-tertiary);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    min-height: 56px;
    transition: color 0.15s;

    &:hover {
      color: var(--color-text-secondary);
    }

    &.is-active {
      color: var(--color-primary);

      .app-tabbar__label {
        font-weight: 600;
      }
    }
  }

  &__icon-wrap {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 24px;
  }

  &__icon {
    font-size: 22px;
  }

  &__badge {
    --el-badge-bg-color: var(--color-error);
  }

  &__label {
    font-size: brand.$font-size-xs;
    line-height: 1.2;
  }
}
</style>
