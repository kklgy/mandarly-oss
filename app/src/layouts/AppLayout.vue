<script setup>
/**
 * AppLayout — 默认 app 壳(plan Wave 1.1)
 *
 * 装配:
 *   ┌─ AppHeader (PC 64 / H5 48)         除非 meta.hideHeader 或 (isH5 && meta.hideHeaderH5)
 *   ├─ <main>                             slot 入,RouterView 内容
 *   ├─ AppFooter (PC + 公开营销页)         由 isPC && PUBLIC_FOOTER_ROUTES 命中决定
 *   └─ AppTabBar (H5 + 登录态)             由 isH5 && isLoggedIn 决定
 *
 * 选择 AuthLayout / FullscreenLayout 由上层 App.vue 通过 route.meta.layout 切换,
 * 本组件只承担 layout='app' 的实际装配。
 */
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useBreakpoint } from '@/composables/useBreakpoint'
import AppHeader from './AppHeader.vue'
import AppFooter from './AppFooter.vue'
import AppTabBar from './AppTabBar.vue'
import SupportChatWidget from '@/components/support/SupportChatWidget.vue'

const route = useRoute()
const userStore = useUserStore()
const { isLoggedIn } = storeToRefs(userStore)
const { isPC, isH5 } = useBreakpoint(768)

// 显示 AppFooter 的公开营销页路由名(plan §1.2 + DESIGN.md 第 1 轮 AppFooter)
const PUBLIC_FOOTER_ROUTES = new Set([
  'Home',
  'TeacherList',
  'TeacherDetail',
  'LevelCheck',
  'LevelCheckResult',
  'LegalPrivacy',
  'LegalTerms'
])

const showHeader = computed(() => {
  if (route.meta.hideHeader) return false
  if (isH5.value && route.meta.hideHeaderH5) return false
  return true
})

const showFooter = computed(() => isPC.value && PUBLIC_FOOTER_ROUTES.has(route.name))
const showTabBar = computed(() => isH5.value && isLoggedIn.value)
const showSupportChat = computed(() => {
  if (isH5.value && route.meta.hideSupportChatH5) return false
  return true
})
</script>

<template>
  <div class="app-shell" :class="{ 'app-shell--has-tabbar': showTabBar }">
    <AppHeader v-if="showHeader" />
    <main class="app-shell__main">
      <slot />
    </main>
    <AppFooter v-if="showFooter" />
    <AppTabBar v-if="showTabBar" />
    <SupportChatWidget v-if="showSupportChat" :has-tabbar="showTabBar" />
  </div>
</template>

<style lang="scss" scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-page);

  &__main {
    flex: 1;
    /* main 自身不内边距,由各页面自行控制 */
  }

  /* H5 登录态有 TabBar 时,main 底部留 56 + safe-area 防遮挡 */
  &--has-tabbar &__main {
    padding-block-end: calc(56px + env(safe-area-inset-bottom, 0px));
  }
}
</style>
