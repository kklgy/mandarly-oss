<script setup>
/**
 * AppHeader — 全站顶部导航(plan Wave 1.2)
 *
 * PC ≥ 768 视图:64px 高 / 1px 底边 / 白底 / sticky top
 *   [Logo] Mandarly | 老师 套餐 等级测试 [我的课]  🌐▾  登录 [注册]  /  [👤▾]
 *
 * H5 < 768 视图:48px 高 / sticky top
 *   [Logo] Mandarly                                  🌐▾ [登录] / [👤]
 *
 * 我的课 / 头像菜单只在登录态显示
 * 我的课 + TabBar 红点共用 useBookingCountsStore.upcomingCount(plan §11.11)
 *
 * 路由 meta.hideHeader = true 完全不渲染本组件(由 AppLayout 控制)
 * 路由 meta.hideHeaderH5 = true 仅 H5 视口隐藏(由 AppLayout 控制)
 *
 * RTL:flex 容器自动镜像(logical properties),logo 字样不镜像
 */
import { computed, onMounted, watch, ref, onBeforeUnmount } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useBookingCountsStore } from '@/stores/bookingCounts'
import { useBreakpoint } from '@/composables/useBreakpoint'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn, profile } = storeToRefs(userStore)

const countsStore = useBookingCountsStore()
const { upcomingCount } = storeToRefs(countsStore)

const { isPC } = useBreakpoint(768)

// M6:教师身份判定 — 用于角色 chip + 用户菜单分支
const isTeacher = computed(() => profile.value?.role === 'teacher')

// 顶部主导航(PC),按身份切换 — D16 §12.2 工程契约
const STUDENT_NAV_ITEMS = [
  { key: 'home', path: '/', i18n: 'header.nav.home' },
  { key: 'curriculum', path: '/curriculum', i18n: 'header.nav.curriculum' },
  { key: 'teachers', path: '/teachers', i18n: 'header.nav.teachers' },
  {
    key: 'resource',
    i18n: 'header.nav.resourcesGroup',
    children: [
      { key: 'levelCheck', path: '/level-check', i18n: 'header.nav.levelCheck' },
      { key: 'textbook', path: '/textbook', i18n: 'header.nav.textbook' },
      { key: 'resources', path: '/resources', i18n: 'header.nav.resources' }
    ]
  }
]

// D27(2026-05-23):9 个 nav item 收成 3 个 — 了解 Mandarly / 学习资源 / 等级测试
//   前两个 group hover 弹出 dropdown(fade-slide-down 动画),等级测试单链接
//   播客为外链(Spotify);教材 / 资源中心为占位路由,内容就位后替换 component
const PODCAST_URL = 'https://open.spotify.com/episode/0bg0JpN42RnthdG6VcJDcK?si=Q3O39W0XSU6XCrX0BVlhmw'

const PUBLIC_NAV_ITEMS = [
  { key: 'home', path: '/', i18n: 'header.nav.home' },
  { key: 'curriculum', path: '/curriculum', i18n: 'header.nav.curriculum' },
  { key: 'teachers', path: '/teachers', i18n: 'header.nav.teachers' },
  {
    key: 'resource',
    i18n: 'header.nav.resourcesGroup',
    children: [
      { key: 'levelCheck', path: '/level-check', i18n: 'header.nav.levelCheck' },
      { key: 'textbook', path: '/textbook', i18n: 'header.nav.textbook' },
      { key: 'resources', path: '/resources', i18n: 'header.nav.resources' },
      { key: 'podcast', external: PODCAST_URL, i18n: 'header.nav.podcast' }
    ]
  }
]

const TEACHER_NAV_ITEMS = [
  { key: 'tc-dashboard', path: '/teacher-center', i18n: 'teacherCenter.nav.dashboard' },
  { key: 'tc-schedule', path: '/teacher-center/schedule', i18n: 'teacherCenter.nav.schedule' },
  { key: 'tc-orders', path: '/teacher-center/orders', i18n: 'teacherCenter.nav.orders' },
  { key: 'tc-income', path: '/teacher-center/income', i18n: 'teacherCenter.nav.income' }
]

const navItems = computed(() => {
  if (isTeacher.value) return TEACHER_NAV_ITEMS
  if (!isLoggedIn.value) return PUBLIC_NAV_ITEMS
  return STUDENT_NAV_ITEMS
})

const activeNavKey = computed(() => {
  if (isTeacher.value) {
    if (route.path === '/teacher-center' || route.path === '/teacher-center/') return 'tc-dashboard'
    if (route.path.startsWith('/teacher-center/schedule')) return 'tc-schedule'
    if (route.path.startsWith('/teacher-center/orders')) return 'tc-orders'
    if (route.path.startsWith('/teacher-center/income')) return 'tc-income'
    return ''
  }
  if (route.path === '/') return 'home'
  if (route.path.startsWith('/curriculum')) return 'curriculum'
  // D27 group 父项 active:子项命中时父 group 也高亮
  if (route.path.startsWith('/textbook') || route.path.startsWith('/resources') || route.path.startsWith('/level-check')) return 'resource'
  if (route.path.startsWith('/teachers') || route.path.startsWith('/teacher/')) return 'teachers'
  if (route.path.startsWith('/packages') || route.path.startsWith('/my/packages')) return 'packages'
  if (route.path === '/orders' || route.path.startsWith('/orders')) return 'myOrders'
  return ''
})

// D27 dropdown hover state — 鼠标 enter 即开,leave 150ms 后关(允许 transit 到 panel)
const openGroupKey = ref(null)
const closeTimers = new Map()

function openGroup(key) {
  const timer = closeTimers.get(key)
  if (timer) { clearTimeout(timer); closeTimers.delete(key) }
  openGroupKey.value = key
}

function closeGroup(key) {
  const timer = setTimeout(() => {
    if (openGroupKey.value === key) openGroupKey.value = null
    closeTimers.delete(key)
  }, 150)
  closeTimers.set(key, timer)
}

onBeforeUnmount(() => {
  closeTimers.forEach((t) => clearTimeout(t))
  closeTimers.clear()
})

// 登录态拉一次 booking counts(已登录场景下;未登录 store 内置兜底返 0)
onMounted(() => {
  if (isLoggedIn.value) countsStore.fetch()
})
watch(isLoggedIn, (v) => {
  if (v) countsStore.fetch(true)
  else countsStore.reset()
})

// flat 找项 — 支持 group children
function findNavItem(key) {
  for (const item of navItems.value) {
    if (item.key === key) return item
    if (item.children) {
      const child = item.children.find(c => c.key === key)
      if (child) return child
    }
  }
  return null
}

function goNav(key) {
  const item = findNavItem(key)
  if (item && item.external) {
    // D27:外链(如播客)直接开新 tab,user activation 必须在同步栈内
    window.open(item.external, '_blank', 'noopener,noreferrer')
    openGroupKey.value = null
    return
  }
  if (item && item.hash) {
    // D26:hash 锚点 — 不在 home 时先跳 home,再 smooth scroll
    const targetId = item.hash.replace(/^#/, '')
    const scroll = () => {
      const el = document.getElementById(targetId)
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
    if (route.path !== '/') {
      router.push('/').then(() => setTimeout(scroll, 80))
    } else {
      scroll()
    }
    openGroupKey.value = null
    return
  }
  if (item) router.push(item.path)
  else if (key === 'myOrders') router.push('/orders')
  openGroupKey.value = null
}

async function onLogout() {
  await userStore.logout()
  router.push('/')
}

const avatarText = computed(() => {
  const nick = profile.value?.nickname || profile.value?.email || 'U'
  return nick.charAt(0).toUpperCase()
})
</script>

<template>
  <header class="app-header" :class="{ 'app-header--h5': !isPC }">
    <div class="app-header__inner">
      <!-- Logo -->
      <router-link to="/" class="app-header__brand" aria-label="Mandarly">
        <img src="/logo-256.png?v=v20260526-mark-only" alt="Mandarly" class="app-header__logo" @error="(e) => e.target.style.display='none'" />
        <span class="app-header__brand-copy">
          <span class="app-header__brand-name">Mandarly</span>
          <span class="app-header__brand-slogan">Online Chinese Speaking Classes</span>
        </span>
      </router-link>

      <!-- PC 主导航 — D16:学生/教师 NAV 按 isTeacher 切换 / D27:public 状态 group hover dropdown -->
      <nav v-if="isPC" class="app-header__nav" aria-label="primary navigation">
        <template v-for="item in navItems" :key="item.key">
          <!-- 单项(无 children)— 学生/教师 NAV 或 public 等级测试 -->
          <button
            v-if="!item.children"
            type="button"
            class="app-header__nav-item"
            :class="{ 'is-active': activeNavKey === item.key }"
            @click="goNav(item.key)"
          >
            {{ t(item.i18n) }}
          </button>
          <!-- group dropdown — hover 展开抽屉 -->
          <div
            v-else
            class="app-header__nav-group"
            @mouseenter="openGroup(item.key)"
            @mouseleave="closeGroup(item.key)"
          >
            <button
              type="button"
              class="app-header__nav-item app-header__nav-item--group"
              :class="{
                'is-active': activeNavKey === item.key,
                'is-open': openGroupKey === item.key
              }"
              :aria-expanded="openGroupKey === item.key"
              :aria-haspopup="true"
              @click="openGroup(item.key)"
            >
              {{ t(item.i18n) }}
              <svg class="app-header__nav-chevron" width="10" height="10" viewBox="0 0 10 10" aria-hidden="true">
                <path d="M2 3.5 L5 6.5 L8 3.5" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <transition name="nav-dropdown">
              <div v-if="openGroupKey === item.key" class="app-header__dropdown" role="menu">
                <button
                  v-for="child in item.children"
                  :key="child.key"
                  type="button"
                  class="app-header__dropdown-item"
                  role="menuitem"
                  @click="goNav(child.key)"
                >
                  {{ t(child.i18n) }}
                </button>
              </div>
            </transition>
          </div>
        </template>
        <!-- 学生侧"我的课"badge(教师顶栏第 3 项已是"订单",不再重复) -->
        <button
          v-if="isLoggedIn && !isTeacher"
          type="button"
          class="app-header__nav-item"
          :class="{ 'is-active': activeNavKey === 'myOrders' }"
          @click="goNav('myOrders')"
        >
          <el-badge
            :value="upcomingCount"
            :hidden="upcomingCount === 0"
            :max="99"
            class="app-header__nav-badge"
          >
            {{ t('header.nav.myOrders') }}
          </el-badge>
        </button>
      </nav>

      <!-- 右侧操作区 — D16:撤掉教师 chip,顶栏 NAV 已标识教师身份 -->
      <div class="app-header__actions">
        <LanguageSwitcher class="app-header__lang" />

        <!-- 未登录:PC 文本登录 + ghost 注册;H5 仅文本登录 -->
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="app-header__login-link">
            {{ t('header.action.login') }}
          </router-link>
          <router-link v-if="isPC" to="/register" class="app-header__register-btn">
            {{ t('header.action.register') }}
          </router-link>
        </template>

        <!-- 登录:头像 dropdown -->
        <el-dropdown v-else trigger="click" placement="bottom-end">
          <button type="button" class="app-header__avatar" :aria-label="avatarText">
            <img
              v-if="profile?.avatarUrl"
              :src="profile.avatarUrl"
              :alt="avatarText"
              class="app-header__avatar-img"
            />
            <span v-else class="app-header__avatar-text">{{ avatarText }}</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <!-- D16:身份分流 — 教师下拉 7 项无学生侧"我的套餐",学生下拉保持现状 -->
              <template v-if="isTeacher">
                <el-dropdown-item @click="router.push('/teacher-center')">
                  {{ t('teacherCenter.nav.dashboard') }}
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/teacher-center/orders')">
                  {{ t('header.menu.myOrdersTeacher') }}
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/teacher-center/income')">
                  {{ t('header.menu.income') }}
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/teacher-center/withdrawal')">
                  {{ t('header.menu.withdrawal') }}
                </el-dropdown-item>
                <!-- D19 Phase B / B9:教师"个人资料"指向教师中心档案编辑,与 AuditStatusBanner"去补充"按钮对齐 -->
                <el-dropdown-item divided @click="router.push('/teacher-center/profile-edit')">
                  {{ t('header.menu.profileTeacher') }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="onLogout">
                  {{ t('header.menu.logout') }}
                </el-dropdown-item>
              </template>
              <template v-else>
                <el-dropdown-item @click="router.push('/profile')">
                  {{ t('header.menu.profile') }}
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/my/packages')">
                  {{ t('header.menu.myPackages') }}
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/orders')">
                  {{ t('header.nav.myOrders') }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="onLogout">
                  {{ t('header.menu.logout') }}
                </el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<style lang="scss" scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 1020;
  background: var(--color-bg-card);
  border-block-end: 1px solid var(--color-border);

  &__inner {
    max-width: 1280px;
    height: 64px;
    margin: 0 auto;
    padding-inline: brand.$spacing-6;
    display: flex;
    align-items: center;
    gap: brand.$spacing-6;
  }

  &--h5 &__inner {
    height: 48px;
    padding-inline: brand.$spacing-4;
    gap: brand.$spacing-3;
  }

  &__brand {
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-2;
    text-decoration: none;
    color: var(--color-text-primary);
    flex-shrink: 0;
  }

  &__logo {
    width: 40px;
    height: 40px;
    object-fit: contain;
  }

  &--h5 &__logo {
    width: 32px;
    height: 32px;
  }

  &__brand-copy {
    display: inline-flex;
    flex-direction: column;
    justify-content: center;
    gap: 2px;
    min-width: 0;
  }

  &__brand-name {
    font-family: var(--font-family-heading-en);
    font-style: italic;
    font-size: 22px;
    font-weight: 600;
    line-height: 0.95;
    color: brand.$brand-navy;
    /* 品牌字样 LTR 锁定,不跟随 RTL 镜像 */
    direction: ltr;
    unicode-bidi: isolate;
  }

  &--h5 &__brand-name {
    font-size: brand.$font-size-lg;
  }

  &__brand-slogan {
    font-size: 9px;
    font-weight: 600;
    line-height: 1;
    color: brand.$brand-primary;
    direction: ltr;
    letter-spacing: 0;
    text-transform: uppercase;
    unicode-bidi: isolate;
    white-space: nowrap;
  }

  &--h5 &__brand-slogan {
    display: none;
  }

  // D27:顶栏 nav 现代化 — 参考 Stripe / Linear / Anthropic
  //   字号 14px medium / 字色 ink 偏深 / hover 出 underline expand 动画 + 微微提色
  //   active 时 underline 长态显示
  &__nav {
    display: flex;
    align-items: center;
    gap: 4px;
    flex: 1;
    margin-inline-start: brand.$spacing-6;
  }

  &__nav-item {
    background: transparent;
    border: none;
    padding: brand.$spacing-3 brand.$spacing-3;
    font: inherit;
    font-size: 14px;
    font-weight: 500;
    letter-spacing: -0.005em;
    color: rgba(20, 20, 19, 0.72); // ink 72% — 比 secondary 更稳重
    cursor: pointer;
    border-radius: 0;
    position: relative;
    white-space: nowrap;
    transition: color 0.18s ease;

    // underline expand 动画(hover / active 共用 ::before)
    &::before {
      content: '';
      position: absolute;
      inset-inline: brand.$spacing-3;
      inset-block-end: 8px;
      height: 2px;
      background: var(--color-primary);
      border-radius: 2px;
      transform: scaleX(0);
      transform-origin: center;
      transition: transform 0.22s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &:hover {
      color: rgba(20, 20, 19, 0.95);

      &::before {
        transform: scaleX(0.7);
        opacity: 0.6;
      }
    }

    &.is-active {
      color: rgba(20, 20, 19, 1);
      font-weight: 600;

      &::before {
        transform: scaleX(1);
        opacity: 1;
      }
    }
  }

  // D27:group 父项 + chevron + dropdown
  &__nav-group {
    position: relative;
    display: inline-flex;
    align-items: stretch;
  }

  &__nav-item--group {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  &__nav-chevron {
    transition: transform 0.2s ease;

    .is-open & {
      transform: rotate(180deg);
    }
  }

  &__dropdown {
    position: absolute;
    inset-block-start: calc(100% + 4px);
    inset-inline-start: 50%;
    transform: translateX(-50%);
    min-width: 200px;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: 12px;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08),
                0 4px 12px rgba(0, 0, 0, 0.04);
    padding: 6px;
    display: flex;
    flex-direction: column;
    gap: 2px;
    z-index: 1030;
  }

  &__dropdown-item {
    background: transparent;
    border: none;
    padding: 8px 12px;
    font: inherit;
    font-size: 13px;
    font-weight: 500;
    color: rgba(20, 20, 19, 0.78);
    text-align: start;
    cursor: pointer;
    border-radius: 8px;
    white-space: nowrap;
    transition: background 0.15s ease, color 0.15s ease;

    &:hover {
      background: var(--color-bg-strong);
      color: rgba(20, 20, 19, 1);
    }

    &:focus-visible {
      outline: none;
      background: var(--color-bg-strong);
      box-shadow: var(--ring-focus);
    }
  }

  &__nav-badge {
    /* el-badge 在按钮内 inline 显示,避免 badge 跑到按钮外 */
    --el-badge-bg-color: var(--color-error);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    margin-inline-start: auto;
  }

  &--h5 &__actions {
    gap: brand.$spacing-2;
  }

  // D27:登录 + 注册 双 pill 形态统一 — 都有背景块,只是色重不同表达主/次 CTA
  //   登录 = subtle filled(浅灰底 + ink 字)secondary
  //   注册 = solid filled(主色 orange + 白字)primary
  //   两者等高 32 / 同字号 13 / 同字重 600 / 同 pill 圆角 / 同 padding
  //   简体中文(LanguageSwitcher)继续 ghost 文字下拉,作为独立的语言切换
  &__login-link,
  &__register-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 32px;
    padding: 0 brand.$spacing-4;
    border-radius: var(--radius-full);
    text-decoration: none;
    font-size: 13px;
    font-weight: 600;
    letter-spacing: -0.005em;
    border: none;
    transition: background 0.15s, color 0.15s, transform 0.15s, box-shadow 0.18s;
  }

  &__login-link {
    background: rgba(20, 20, 19, 0.06);
    color: rgba(20, 20, 19, 0.85);

    &:hover {
      background: rgba(20, 20, 19, 0.12);
      color: rgba(20, 20, 19, 1);
    }
  }

  &__register-btn {
    background: var(--color-primary);
    color: var(--color-text-inverse);

    &:hover {
      background: var(--color-primary-deep);
      transform: translateY(-1px);
      box-shadow: var(--shadow-brand);
    }
  }

  // D27:LanguageSwitcher 组件 base 已改 ghost 风(无 border / pill / 32px / 13px medium),
  // 这里不再额外覆盖

  &__avatar {
    width: 36px;
    height: 36px;
    border-radius: var(--radius-full);
    background: var(--color-primary);
    color: var(--color-text-inverse);
    border: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: brand.$font-size-base;
    font-weight: 700;
    cursor: pointer;
    transition: box-shadow 0.15s;
    overflow: hidden;
    padding: 0;

    &:hover {
      box-shadow: var(--shadow-brand);
    }
  }

  &--h5 &__avatar {
    width: 32px;
    height: 32px;
    font-size: brand.$font-size-sm;
  }

  &__avatar-text {
    line-height: 1;
  }

  &__avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

// D27:dropdown fade-slide-down 出入动画
.nav-dropdown-enter-active,
.nav-dropdown-leave-active {
  transition:
    opacity 0.18s cubic-bezier(0.16, 1, 0.3, 1),
    transform 0.22s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-dropdown-enter-from {
  opacity: 0;
  transform: translate(-50%, -6px);
}

.nav-dropdown-leave-to {
  opacity: 0;
  transform: translate(-50%, -4px);
}

@media (prefers-reduced-motion: reduce) {
  .nav-dropdown-enter-active,
  .nav-dropdown-leave-active {
    transition: opacity 0.12s ease;
  }
  .nav-dropdown-enter-from,
  .nav-dropdown-leave-to {
    transform: translateX(-50%);
  }
}
</style>
