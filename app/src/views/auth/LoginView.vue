<script setup>
/**
 * LoginView.vue — /login
 *
 * Wave 5 G:套 AuthLayout 双栏壳(layout='auth' 已在 router meta 配)。
 * View 仅渲染卡片表单 — Logo + 大标题已转移到 AuthHero(PC 左栏);H5 沿用精简标题。
 *
 * 一屏式登录:邮箱 / 手机轻量切换 + Google / Apple 图标入口
 * 支持 redirect 参数:
 *   - 邮箱/手机登录成功后 → router.replace(safeRedirect(redirect))
 *   - 第三方登录前 → sessionStorage.setItem('mandarly_oauth_redirect', redirect)
 */
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAuthChannelsStore } from '@/stores/authChannels'
import { safeRedirect } from '@/utils/safeRedirect'
import EmailLoginForm from '@/components/auth/EmailLoginForm.vue'
import PhoneLoginForm from '@/components/auth/PhoneLoginForm.vue'
import SocialButtons from '@/components/auth/SocialButtons.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const authChannelsStore = useAuthChannelsStore()
const { channels } = storeToRefs(authChannelsStore)
onMounted(() => authChannelsStore.ensureLoaded())

const activeMethod = ref('email')
const loading = ref(false)

// 当前 method 实际不可用时,自动切到第一个可用的 method
watch(channels, (c) => {
  if (activeMethod.value === 'email' && !c.email && c.sms) {
    activeMethod.value = 'phone'
  } else if (activeMethod.value === 'phone' && !c.sms && c.email) {
    activeMethod.value = 'email'
  }
}, { immediate: true, deep: true })

const socialEnabled = computed(() => ({
  google: channels.value.google,
  apple: channels.value.apple
}))

/**
 * 计算登录成功后默认 redirect:
 * - 有 query.redirect → 优先(被踢回的场景)
 * - 否则按角色:teacher → /teacher-center;其它 → /
 * 经 safeRedirect 白名单兜底(plan Wave 1.5.1)
 */
function resolveDefaultRedirect() {
  if (route.query.redirect) {
    return safeRedirect(route.query.redirect, '/', router)
  }
  return userStore.profile?.role === 'teacher' ? '/teacher-center' : '/'
}

/**
 * 把后端 error code 映射到 i18n key,fallback 后端 msg。
 *
 * 后端 ErrorCodeConstants 用 7 位数字码(1_002_100 ~ 1_002_120)。早期 MAP 用了
 * 字符串 enum 作 key,与后端数字码永远对不上 → fallback 'common.error' 显示"出错了"。
 * 改为数字键 + 双层 fallback(i18n key → e.message → common.error)。
 */
function mapErrorMessage(e) {
  const MAP = {
    1002100: 'auth.error.invalidCredentials',
    1002101: 'auth.error.codeExpired',
    1002102: 'auth.error.codeInvalid',
    1002103: 'auth.error.codeCooldown',
    1002104: 'auth.error.emailExists',
    1002105: 'auth.error.phoneExists',
    1002106: 'auth.error.userNotFound',
    1002107: 'auth.error.userFrozen',
    1002108: 'auth.error.referralInvalid',
    1002109: 'auth.error.socialNotConfigured',
    1002110: 'auth.error.socialFetchFailed',
    1002111: 'auth.error.tokenExpired',
    1002113: 'auth.error.smtpNotConfigured',
    1002114: 'auth.error.passwordWeak',
    1002115: 'auth.error.rateLimitDaily',
    1002116: 'auth.error.loginFailedLocked',
    1002117: 'auth.error.ipRegisterLimit',
    1002118: 'auth.error.socialAccountConflict'
  }
  const key = MAP[e?.code]
  if (key) return t(key)
  return e?.message || t('common.error')
}

async function onEmailLogin({ email, password }) {
  if (loading.value) return
  loading.value = true
  try {
    await userStore.loginByEmail(email, password)
    // open redirect 防御:走 safeRedirect 白名单(plan Wave 1.5.1)
    router.replace(resolveDefaultRedirect())
  } catch (e) {
    ElMessage.error(mapErrorMessage(e))
  } finally {
    loading.value = false
  }
}

async function onPhoneLogin({ phone, code }) {
  if (loading.value) return
  loading.value = true
  try {
    await userStore.loginByPhone(phone, code)
    router.replace(resolveDefaultRedirect())
  } catch (e) {
    ElMessage.error(mapErrorMessage(e))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-card">
    <!-- 标题 — H5 沿用文字标题(PC 大标题在 AuthHero);视觉简洁不带 logo -->
    <header class="login-card__header">
      <h1 class="login-card__title">{{ t('auth.login.title') }}</h1>
    </header>

    <div class="login-card__method-switch" role="tablist" :aria-label="t('auth.login.methodLabel')">
      <button
        type="button"
        class="login-card__method"
        :class="{ 'is-active': activeMethod === 'email' }"
        :disabled="!channels.email"
        role="tab"
        :aria-selected="activeMethod === 'email'"
        @click="activeMethod = 'email'"
      >
        {{ t('auth.login.emailTab') }}
      </button>
      <button
        type="button"
        class="login-card__method"
        :class="{ 'is-active': activeMethod === 'phone' }"
        :disabled="!channels.sms"
        role="tab"
        :aria-selected="activeMethod === 'phone'"
        @click="activeMethod = 'phone'"
      >
        {{ t('auth.login.phoneTab') }}
      </button>
    </div>

    <EmailLoginForm v-if="activeMethod === 'email'" @submit="onEmailLogin" />
    <PhoneLoginForm v-else @submit="onPhoneLogin" />

    <div v-if="activeMethod === 'email'" class="login-card__forgot">
      <router-link to="/reset-password">{{ t('auth.login.forgotPassword') }}</router-link>
    </div>

    <div class="login-card__divider">
      <span>{{ t('auth.login.socialDivider') }}</span>
    </div>

    <div class="login-card__social">
      <SocialButtons
        :providers="['google', 'apple']"
        :enabled="socialEnabled"
      />
    </div>

    <!-- 去注册 -->
    <footer class="login-card__footer">
      <span class="login-card__footer-text">{{ t('auth.login.noAccount') }}</span>
      <router-link to="/register" class="login-card__footer-link">
        {{ t('auth.login.register') }}
      </router-link>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.login-card {
  width: 100%;
  max-width: 440px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: brand.$spacing-8;

  // PC 双栏右栏内 — 不再描阴影/边(与左栏氛围对比形成层级)
  @media (min-width: brand.$bp-laptop) {
    padding: brand.$spacing-10 brand.$spacing-8;
    box-shadow: none;
  }

  // < 1024 单栏卡片 — 保留 shadow-base(无左右栏对比衬托)
  @media (max-width: 1023px) {
    box-shadow: var(--shadow-base);
  }

  @media (max-width: brand.$bp-tablet) {
    padding: brand.$spacing-6 brand.$spacing-4;
    box-shadow: none;
    border-radius: var(--radius-base);
  }

  &__header {
    margin-block-end: brand.$spacing-6;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-2xl;
    font-weight: 700;
    color: var(--color-text-primary);
    line-height: brand.$line-height-tight;
  }

  &__method-switch {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: brand.$spacing-1;
    padding: brand.$spacing-1;
    margin-block-end: brand.$spacing-4;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-base);
    background: var(--color-bg-page);
  }

  &__method {
    min-height: 44px;
    border: 0;
    border-radius: var(--radius-sm);
    background: transparent;
    color: var(--color-text-secondary);
    cursor: pointer;
    font-size: brand.$font-size-base;
    font-weight: 600;

    &.is-active {
      background: var(--color-bg-card);
      color: var(--color-primary-deep);
      box-shadow: brand.$shadow-sm;
    }

    &:disabled {
      cursor: not-allowed;
      color: var(--color-text-disabled);
    }
  }

  &__forgot {
    margin-block-start: brand.$spacing-3;
    text-align: end;

    a {
      font-size: brand.$font-size-sm;
      color: var(--color-primary-deep);
      text-decoration: none;

      &:hover { text-decoration: underline; }
    }
  }

  &__social {
    margin-block-start: brand.$spacing-4;
  }

  &__divider {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-block-start: brand.$spacing-6;
    color: var(--color-text-tertiary);
    font-size: brand.$font-size-xs;

    &::before {
      position: absolute;
      inset-inline: 0;
      top: 50%;
      height: 1px;
      background: var(--color-border);
      content: '';
    }

    span {
      position: relative;
      padding-inline: brand.$spacing-3;
      background: var(--color-bg-card);
    }
  }

  &__footer {
    margin-block-start: brand.$spacing-6;
    text-align: center;
    font-size: brand.$font-size-sm;
    border-block-start: 1px solid var(--color-border);
    padding-block-start: brand.$spacing-4;
  }

  &__footer-text {
    color: var(--color-text-secondary);
    margin-inline-end: brand.$spacing-1;
  }

  &__footer-link {
    color: var(--color-primary-deep);
    font-weight: 500;
    text-decoration: none;

    &:hover {
      color: var(--color-primary);
      text-decoration: underline;
    }
  }

  :deep(.el-input__inner) {
    font-size: brand.$font-size-md;
  }
}

</style>
