<script setup>
// =============================================================================
// ProfileLayout.vue — /profile 壳路由
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § ProfileView 信息架构拆分(第 4 轮)
//
// 视图切换规则(响应式 768 分水岭):
//   - PC ≥ 768:左侧 ProfileSidebar(240px sticky)+ 右侧 <RouterView>
//                空 path('/profile')时直接 inline 渲染 AccountView 内容(避免重定向闪烁)
//   - H5 < 768:
//        空 path('/profile')→ ProfileMenuMobile 列表式入口
//        子路径('/profile/account' 等)→ <RouterView> 子页全屏
//
// 协作纪律:
//   - 不修改 i18n locales / App.vue / layouts;
//   - logout 二次 confirm + reset 后跳 /login;
// =============================================================================
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useBreakpoint } from '@/composables/useBreakpoint'
import { useUserStore } from '@/stores/user'
import ProfileSidebar from '@/components/profile/ProfileSidebar.vue'
import ProfileMenuMobile from './ProfileMenuMobile.vue'
import AccountView from './AccountView.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isPC } = useBreakpoint(768)

// 当前是否落在 /profile 空 path(非 account/referral/teacher-stats 子路由)
// route name === 'Profile' 表示空 path child(保留此 name 是为 AppLayout/AppTabBar active 高亮兼容)
const isRootProfile = computed(() => route.name === 'Profile')

onMounted(async () => {
  // 任一子页进入都尝试 refresh profile,确保 nickname/role/timezone 等是最新
  if (userStore.isLoggedIn) {
    try {
      await userStore.refreshProfile()
    } catch {
      // 忽略 — isLoggedIn 仍为真
    }
  }
})

async function handleLogout() {
  try {
    await ElMessageBox.confirm(
      t('profile.account.logout.confirm'),
      t('profile.account.logout.title'),
      {
        confirmButtonText: t('profile.account.logout.confirmBtn'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch {
    return // 用户取消
  }

  try {
    await userStore.logout()
    // plan §11.13 — 切账号清理:logout 后 fire 干净
    try {
      localStorage.removeItem('mandarly_pkg_currency')
      localStorage.removeItem('mandarly_pkg_currency_manual')
      // TODO Wave 4 — 收口其它 user-scoped localStorage(filter 偏好等)
    } catch {
      // localStorage 可能被禁用,忽略
    }
    ElMessage.success(t('profile.account.logout.success'))
    router.push('/login')
  } catch {
    ElMessage.error(t('common.error'))
  }
}
</script>

<template>
  <div
    class="profile-layout"
    :class="{ 'profile-layout--pc': isPC, 'profile-layout--h5': !isPC }"
  >
    <!-- ============ PC 双栏(≥768) ============ -->
    <div v-if="isPC" class="profile-layout__pc-shell">
      <h1 class="profile-layout__page-title">{{ t('profile.title') }}</h1>

      <div class="profile-layout__pc-grid">
        <ProfileSidebar @logout="handleLogout" />

        <main class="profile-layout__pc-content">
          <!-- 空 path 时 inline 渲染 AccountView,避免 redirect 闪烁;
               否则 RouterView 渲染子页(/account /referral /teacher-stats) -->
          <AccountView v-if="isRootProfile" @logout="handleLogout" />
          <router-view v-else @logout="handleLogout" />
        </main>
      </div>
    </div>

    <!-- ============ H5(<768) ============ -->
    <div v-else class="profile-layout__h5-shell">
      <!-- 空 path 显示主入口 list -->
      <ProfileMenuMobile v-if="isRootProfile" @logout="handleLogout" />
      <!-- 子页 — RouterView 全屏 -->
      <router-view v-else @logout="handleLogout" />
    </div>
  </div>
</template>

<style scoped lang="scss">

.profile-layout {
  min-height: 100vh;
  background: brand.$color-bg-page;
  box-sizing: border-box;
}

// ---------- PC 双栏 ----------
.profile-layout--pc {
  padding: brand.$spacing-8 brand.$spacing-6 brand.$spacing-12;

  .profile-layout__pc-shell {
    max-width: 1200px;
    margin: 0 auto;
  }

  .profile-layout__page-title {
    margin: 0 0 brand.$spacing-6;
    font-size: brand.$font-size-2xl;
    font-weight: 700;
    color: brand.$color-text-primary;
  }

  .profile-layout__pc-grid {
    display: flex;
    align-items: flex-start;
    gap: brand.$spacing-8;
  }

  .profile-layout__pc-content {
    flex: 1;
    min-width: 0; // 防止 grid 子项 overflow 撑开父
  }
}

// ---------- H5 ----------
.profile-layout--h5 {
  // 子页 / 主入口自身处理 padding(不在壳上加 padding,避免双层 padding)
  padding: 0;
}
</style>
