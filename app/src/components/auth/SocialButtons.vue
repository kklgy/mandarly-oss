<script setup>
/**
 * SocialButtons.vue — Google + Apple 三方登录按钮组
 *
 * Props:
 *   providers: Array<'google'|'apple'> — 要显示的提供商列表
 *   enabled: Record<string, boolean> — 各提供商是否可用(一期 Google/Apple 凭证未就位)
 *   role: 'student' | 'teacher' — D19-A8 教师注册场景传 'teacher',
 *         OAuth 跳转前缓存到 sessionStorage,callback view 回读后透传给后端
 *         (后端 D19-A3 social-login 接 role,新建账号时按 role 落 user.role + draft profile)
 *
 * 一期:disabled + tooltip 展示 i18n.auth.login.socialNotConfigured
 * 二期:enabled 时调 authApi.getSocialRedirectUrl(provider) → window.location.href
 */
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as authApi from '@/api/auth'

const props = defineProps({
  providers: {
    type: Array,
    default: () => ['google', 'apple']
  },
  enabled: {
    type: Object,
    default: () => ({ google: false, apple: false })
  },
  role: {
    type: String,
    default: 'student',
    validator: (v) => ['student', 'teacher'].includes(v)
  }
})

const { t } = useI18n()

const PROVIDER_META = {
  google: {
    label: () => t('auth.login.googleBtn')
  },
  apple: {
    label: () => t('auth.login.appleBtn')
  }
}

async function handleClick(provider) {
  const isEnabled = props.enabled[provider]
  if (!isEnabled) {
    ElMessage.info(t('auth.login.socialNotConfigured'))
    return
  }
  const inAppBrowser = detectInAppBrowser()
  if (provider === 'google' && inAppBrowser) {
    await ElMessageBox.alert(
      t('auth.login.googleUnsupportedBrowserMessage', { browser: inAppBrowser }),
      t('auth.login.googleUnsupportedBrowserTitle'),
      { confirmButtonText: t('common.confirm') }
    )
    return
  }
  try {
    // D19-A8: 教师注册时缓存 role,SocialCallbackView 回读后透传给后端
    // 学生默认不写(callback 兜底为 student),避免污染既有 LoginView 流程
    if (props.role === 'teacher') {
      sessionStorage.setItem('mandarly_oauth_role', 'teacher')
    } else {
      sessionStorage.removeItem('mandarly_oauth_role')
    }
    const { url } = await authApi.getSocialRedirectUrl(provider)
    window.location.href = url
  } catch {
    ElMessage.error(t('auth.error.socialFetchFailed'))
  }
}

function detectInAppBrowser() {
  if (typeof navigator === 'undefined') return ''
  const ua = navigator.userAgent || ''
  const matchers = [
    { name: '微信', pattern: /MicroMessenger/i },
    { name: '企业微信', pattern: /wxwork/i },
    { name: 'QQ', pattern: /\bQQ\//i },
    { name: '微博', pattern: /Weibo/i },
    { name: 'Instagram', pattern: /Instagram/i },
    { name: 'Facebook', pattern: /FBAN|FBAV/i },
    { name: 'LINE', pattern: /Line\//i },
    { name: 'Twitter/X', pattern: /Twitter|XTwitter/i },
    { name: 'TikTok', pattern: /TikTok|BytedanceWebview/i },
    { name: 'LinkedIn', pattern: /LinkedInApp/i },
    { name: '钉钉', pattern: /DingTalk/i },
    { name: '飞书', pattern: /Lark/i }
  ]
  return matchers.find((item) => item.pattern.test(ua))?.name || ''
}
</script>

<template>
  <div class="social-buttons">
    <el-tooltip
      v-for="provider in providers"
      :key="provider"
      :content="enabled[provider] ? '' : t('auth.login.socialNotConfigured')"
      :disabled="!!enabled[provider]"
      placement="top"
    >
      <el-button
        class="social-buttons__btn"
        :class="`social-buttons__btn--${provider}`"
        size="large"
        :disabled="!enabled[provider]"
        @click="handleClick(provider)"
      >
        <span class="social-buttons__icon" aria-hidden="true">
          <svg
            v-if="provider === 'google'"
            viewBox="0 0 24 24"
            focusable="false"
            role="img"
          >
            <path fill="#4285F4" d="M21.6 12.2c0-.7-.1-1.3-.2-1.9H12v3.6h5.4c-.2 1.2-.9 2.2-1.9 2.9v2.4h3.1c1.8-1.7 3-4.1 3-7z" />
            <path fill="#34A853" d="M12 22c2.7 0 5-.9 6.6-2.5l-3.1-2.4c-.9.6-2 .9-3.5.9-2.6 0-4.8-1.8-5.6-4.1H3.2v2.5C4.8 19.7 8.2 22 12 22z" />
            <path fill="#FBBC05" d="M6.4 13.9c-.2-.6-.3-1.2-.3-1.9s.1-1.3.3-1.9V7.6H3.2C2.4 8.9 2 10.4 2 12s.4 3.1 1.2 4.4l3.2-2.5z" />
            <path fill="#EA4335" d="M12 5.9c1.5 0 2.8.5 3.8 1.5l2.8-2.8C16.9 3 14.7 2 12 2 8.2 2 4.8 4.3 3.2 7.6l3.2 2.5C7.2 7.7 9.4 5.9 12 5.9z" />
          </svg>
          <svg
            v-else-if="provider === 'apple'"
            viewBox="0 0 24 24"
            focusable="false"
            role="img"
          >
            <path fill="currentColor" d="M16.6 13.2c0-2.1 1.7-3.1 1.8-3.2-1-1.4-2.5-1.6-3-1.6-1.3-.1-2.5.8-3.1.8-.7 0-1.7-.8-2.8-.7-1.4 0-2.8.8-3.5 2.1-1.5 2.5-.4 6.2 1.1 8.3.7 1 1.6 2.2 2.7 2.1 1.1 0 1.5-.7 2.8-.7s1.7.7 2.8.7c1.2 0 1.9-1 2.6-2.1.9-1.2 1.2-2.4 1.2-2.5 0-.1-2.6-1-2.6-3.2zM14.6 7c.6-.7 1-1.7.9-2.7-.9 0-1.9.6-2.5 1.3-.6.7-1 1.6-.9 2.6 1 0 1.9-.5 2.5-1.2z" />
          </svg>
        </span>
        <span>{{ PROVIDER_META[provider]?.label() }}</span>
      </el-button>
    </el-tooltip>
  </div>
</template>

<style lang="scss" scoped>
.social-buttons {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: brand.$spacing-3;
  width: 100%;

  @media (max-width: brand.$bp-mobile) {
    grid-template-columns: 1fr;
  }

  &__btn {
    width: 100%;
    justify-content: center;
    gap: brand.$spacing-2;
    border: 1px solid var(--color-border);
    background: var(--color-bg-card);
    color: var(--color-text-primary);
    border-radius: var(--radius-pill);
    font-weight: 600;
    min-height: 44px;
    box-shadow: brand.$shadow-sm;

    &:hover:not(:disabled) {
      border-color: var(--color-primary);
      background: var(--color-bg-card);
      box-shadow: var(--shadow-brand);
    }

    &:disabled,
    &.is-disabled {
      cursor: not-allowed;
      opacity: 0.65;
    }
  }

  &__btn--google:not(:disabled) {
    border-color: rgba(26, 26, 26, 0.12);
  }

  &__btn--apple:not(:disabled) {
    background: var(--color-text-primary);
    color: var(--color-text-inverse);
    border-color: var(--color-text-primary);
  }

  &__icon {
    display: inline-flex;
    width: 20px;
    height: 20px;
    line-height: 1;

    svg {
      display: block;
      width: 20px;
      height: 20px;
    }
  }
}
</style>
