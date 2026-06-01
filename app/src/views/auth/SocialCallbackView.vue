<script setup>
/**
 * SocialCallbackView.vue — /auth/callback/:provider
 *
 * OAuth 回跳处理:
 *   1. 读取 sessionStorage 中保存的 redirect 目标
 *   2. 用 provider + code + state 调 userStore.loginBySocial
 *   3. 成功 → router.replace(redirect)
 *   4. 失败 → ElMessage.error + router.replace('/login')
 */
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { safeRedirect } from '@/utils/safeRedirect'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

onMounted(async () => {
  const provider = route.params.provider
  const code = route.query.code
  const state = route.query.state
  const error = route.query.error

  // sessionStorage 写入侧应已 safeRedirect(由 LoginView/RegisterView OAuth 起始流程负责),
  // 但 defense-in-depth 在 callback 再过一遍白名单——sessionStorage 可能被旧版本污染或 XSS 写入
  const rawRedirect = sessionStorage.getItem('mandarly_oauth_redirect')
  sessionStorage.removeItem('mandarly_oauth_redirect')

  // D19-A8: TeacherRegisterView 跳转前缓存 role,这里取出后透传给后端;非 teacher 一律按 student 处理
  const rawRole = sessionStorage.getItem('mandarly_oauth_role')
  sessionStorage.removeItem('mandarly_oauth_role')
  const role = rawRole === 'teacher' ? 'teacher' : 'student'

  if (error) {
    ElMessage.error(t('auth.callback.failed'))
    router.replace({ path: '/login', query: { msg: 'cancelled' } })
    return
  }

  try {
    await userStore.loginBySocial(provider, code, state, role)
    // M6 §3 spec §4.7:role-based redirect — 有原 redirect 走原 redirect;否则 teacher → /teacher-center
    let redirect
    if (rawRedirect) {
      redirect = safeRedirect(rawRedirect, '/', router)
    } else {
      redirect = userStore.profile?.role === 'teacher' ? '/teacher-center' : '/'
    }
    router.replace(redirect)
  } catch (e) {
    ElMessage.error(t('auth.error.socialFetchFailed'))
    router.replace({ path: '/login', query: { msg: 'social-failed' } })
  }
})
</script>

<template>
  <div class="callback-page">
    <div class="callback-loading">
      <div class="callback-loading__spinner" aria-hidden="true"></div>
      <p class="callback-loading__text">{{ $t('auth.callback.processing') }}</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.callback-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: var(--color-bg-page);
}

.callback-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-4;

  &__spinner {
    width: 40px;
    height: 40px;
    border: 3px solid var(--color-border);
    border-top-color: var(--color-primary);
    border-radius: var(--radius-full);
    animation: spin 0.8s linear infinite;
  }

  &__text {
    margin: 0;
    font-size: brand.$font-size-md;
    color: var(--color-text-secondary);
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
