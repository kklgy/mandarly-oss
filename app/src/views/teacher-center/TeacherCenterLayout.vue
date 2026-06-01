<script setup>
/**
 * TeacherCenterLayout — 教师中心壳路由(M6 §3 / §4)
 *
 * 装配:
 *   PC ≥ 768:
 *     ┌────────────────────────────────────────────────┐
 *     │ Dashboard / Schedule / Orders / Income / W'... │  ← 内嵌 SideNav(左 200px)
 *     │  ┌────────────────────────────────────────┐    │
 *     │  │ <router-view>                          │    │
 *     │  └────────────────────────────────────────┘    │
 *     └────────────────────────────────────────────────┘
 *   H5 < 768:
 *     ┌──────────────────────┐
 *     │ ◀ 当前页标题          │  ← 内嵌 topbar(回退键 + 当前页 label)
 *     ├──────────────────────┤
 *     │ <router-view>        │
 *     ├──────────────────────┤
 *     │ AppTabBar(教师套)    │  ← 由 AppLayout 渲染,内部 computed 切教师/学生 tab
 *     └──────────────────────┘
 *
 * 路由 meta.hideHeaderH5 = true → AppHeader 在 H5 隐藏,本组件 topbar 兜上。
 *
 * RTL:flex 自动镜像,侧栏方向跟随 dir。
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useBreakpoint } from '@/composables/useBreakpoint'
import { Calendar, Document, Money, Wallet, HomeFilled, ArrowLeft, User, Files } from '@element-plus/icons-vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const { isPC } = useBreakpoint(768)

// 侧栏导航项(PC 视图);路由 name → 图标 / i18n key
const NAV_ITEMS = [
  { name: 'TeacherCenter', path: '/teacher-center', icon: HomeFilled, i18n: 'teacherCenter.nav.dashboard' },
  // D19 Phase B / B9:档案 + 资质入口接入(B6/B7 占位 stub,正式实现见对应 task)
  { name: 'TeacherCenterProfileEdit', path: '/teacher-center/profile-edit', icon: User, i18n: 'teacherCenter.nav.profileEdit' },
  { name: 'TeacherCenterQualification', path: '/teacher-center/qualification', icon: Files, i18n: 'teacherCenter.nav.qualification' },
  { name: 'TeacherCenterSchedule', path: '/teacher-center/schedule', icon: Calendar, i18n: 'teacherCenter.nav.schedule' },
  { name: 'TeacherCenterOrders', path: '/teacher-center/orders', icon: Document, i18n: 'teacherCenter.nav.orders' },
  { name: 'TeacherCenterIncome', path: '/teacher-center/income', icon: Money, i18n: 'teacherCenter.nav.income' },
  { name: 'TeacherCenterWithdrawal', path: '/teacher-center/withdrawal', icon: Wallet, i18n: 'teacherCenter.nav.withdrawal' }
]

// 当前页 active key:WithdrawalApply 高亮 Withdrawal
const activeName = computed(() => {
  const n = route.name
  if (n === 'TeacherCenterWithdrawalApply') return 'TeacherCenterWithdrawal'
  return n
})

// H5 topbar 当前页标题(根据 route.name 取 nav i18n key)
// WithdrawalApply 单独显示「申请提现」
const h5Title = computed(() => {
  if (route.name === 'TeacherCenterWithdrawalApply') {
    return t('teacherCenter.withdrawal.apply.title')
  }
  const item = NAV_ITEMS.find((n) => n.name === activeName.value)
  return item ? t(item.i18n) : t('teacherCenter.nav.dashboard')
})

// H5 topbar 返回按钮:WithdrawalApply 回到列表;其它默认回 dashboard;dashboard 上不显示返回
const h5BackPath = computed(() => {
  if (route.name === 'TeacherCenterWithdrawalApply') return '/teacher-center/withdrawal'
  if (route.name === 'TeacherCenter') return null
  return '/teacher-center'
})

function go(item) {
  if (route.path !== item.path) router.push(item.path)
}

function onH5Back() {
  if (h5BackPath.value) router.push(h5BackPath.value)
}
</script>

<template>
  <div class="tc-layout" :class="{ 'tc-layout--h5': !isPC }">
    <!-- H5 topbar(让位 AppHeader,由 hideHeaderH5 控制) -->
    <header v-if="!isPC" class="tc-topbar">
      <button
        v-if="h5BackPath"
        type="button"
        class="tc-topbar__back"
        :aria-label="t('common.back', { defaultValue: '返回' })"
        @click="onH5Back"
      >
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="tc-topbar__title">{{ h5Title }}</h1>
    </header>

    <div class="tc-layout__body">
      <!-- PC 侧栏 -->
      <aside v-if="isPC" class="tc-side">
        <nav class="tc-side__nav" aria-label="teacher center navigation">
          <button
            v-for="item in NAV_ITEMS"
            :key="item.name"
            type="button"
            class="tc-side__item"
            :class="{ 'is-active': activeName === item.name }"
            @click="go(item)"
          >
            <el-icon class="tc-side__icon"><component :is="item.icon" /></el-icon>
            <span class="tc-side__label">{{ t(item.i18n) }}</span>
          </button>
        </nav>
      </aside>

      <!-- 主内容区(router-view 注入) -->
      <main class="tc-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.tc-layout {
  min-height: 100%;
  background: brand.$color-bg-page;

  &__body {
    display: flex;
    gap: brand.$spacing-6;
    max-width: 1280px;
    margin: 0 auto;
    padding: brand.$spacing-6;

    @media (max-width: brand.$bp-tablet) {
      padding: 0;
      gap: 0;
      flex-direction: column;
    }
  }

  &--h5 &__body {
    padding-block-start: brand.$spacing-4;
  }
}

// ===== PC 侧栏 =====
.tc-side {
  flex-shrink: 0;
  width: 200px;
  position: sticky;
  inset-block-start: calc(64px + #{brand.$spacing-6});
  align-self: flex-start;

  &__nav {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-1;
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    padding: brand.$spacing-3;
    box-shadow: brand.$shadow-base;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    padding: brand.$spacing-3 brand.$spacing-4;
    background: transparent;
    border: none;
    border-radius: brand.$radius-base;
    color: brand.$color-text-secondary;
    cursor: pointer;
    font-size: brand.$font-size-base;
    text-align: start;
    transition: background 0.15s, color 0.15s;

    &:hover {
      background: brand.$color-bg-page;
      color: brand.$color-text-primary;
    }

    &.is-active {
      background: brand.$brand-primary-soft;
      color: brand.$brand-primary-deep;
      font-weight: 600;
    }
  }

  &__icon {
    font-size: 18px;
  }
}

// ===== 主内容 =====
.tc-main {
  flex: 1;
  min-width: 0; // 避免子内容撑爆 flex
}

// ===== H5 topbar =====
.tc-topbar {
  position: sticky;
  inset-block-start: 0;
  z-index: brand.$z-sticky;
  display: flex;
  align-items: center;
  gap: brand.$spacing-2;
  background: brand.$color-bg-card;
  border-block-end: 1px solid brand.$color-border;
  padding: brand.$spacing-3 brand.$spacing-4;
  min-height: 48px;

  &__back {
    background: transparent;
    border: none;
    padding: brand.$spacing-1;
    cursor: pointer;
    color: brand.$color-text-primary;
    display: inline-flex;
    align-items: center;
    border-radius: brand.$radius-full;
    transition: background 0.15s;

    &:hover {
      background: brand.$color-bg-strong;
    }

    .el-icon {
      font-size: 20px;
    }

    // RTL 反转返回箭头
    [dir='rtl'] & .el-icon {
      transform: scaleX(-1);
    }
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;
  }
}
</style>
