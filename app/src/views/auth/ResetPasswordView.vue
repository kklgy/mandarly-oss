<script setup>
/**
 * ResetPasswordView.vue — /reset-password
 *
 * Wave 5 G:套 AuthLayout 双栏壳(layout='auth' 已在 router meta 配)。
 * View 仅渲染卡片表单 + 三步流 — Logo + 大标题已转移到 AuthHero(PC 左栏)。
 *
 * 三步流:
 *   step 1: 输入邮箱
 *   step 2: 输入验证码
 *   step 3: 输入新密码 + 确认密码 → 提交
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import * as authApi from '@/api/auth'
import VerifyCodeInput from '@/components/auth/VerifyCodeInput.vue'

const router = useRouter()
const { t } = useI18n()

const step = ref(1)
const submitting = ref(false)
const emailSent = ref(false)

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const step1Ref = ref(null)
const step3Ref = ref(null)

const step1Rules = {
  email: [
    { required: true, message: () => t('auth.validation.emailRequired'), trigger: 'blur' },
    { type: 'email', message: () => t('auth.validation.emailInvalid'), trigger: 'blur' }
  ]
}

const step3Rules = {
  newPassword: [
    { required: true, message: t('auth.error.passwordWeak'), trigger: 'blur' },
    { min: 8, message: t('auth.error.passwordWeak'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: () => t('auth.resetPassword.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) callback(new Error(t('auth.resetPassword.passwordMismatch')))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function sendCode() {
  if (!form.email) throw new Error(t('auth.validation.emailRequired'))
  await authApi.sendEmailCode(form.email, 'reset')
  emailSent.value = true
}

async function handleStep1() {
  if (!step1Ref.value) return
  try {
    await step1Ref.value.validate()
  } catch {
    return
  }
  // 直接进入 step 2(先发码)
  submitting.value = true
  try {
    await authApi.sendEmailCode(form.email, 'reset')
    emailSent.value = true
    step.value = 2
  } catch (e) {
    // 后端 ErrorCodeConstants 用数字 code,字符串比较永远 false → fallback unavailable
    const code = e?.code
    if (code === 1002113) {
      ElMessage.error(t('auth.error.smtpNotConfigured'))
    } else if (code === 1002106) {
      ElMessage.error(t('auth.error.userNotFound'))
    } else {
      ElMessage.error(e?.message || t('auth.resetPassword.unavailable'))
    }
  } finally {
    submitting.value = false
  }
}

function handleStep2() {
  if (!form.code.trim()) {
    ElMessage.warning(t('auth.validation.codeRequired'))
    return
  }
  step.value = 3
}

async function handleStep3() {
  if (!step3Ref.value) return
  try {
    await step3Ref.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await authApi.resetPassword({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword
    })
    ElMessage.success(t('auth.resetPassword.success'))
    router.replace('/login')
  } catch (e) {
    // 后端 ErrorCodeConstants 用数字 code,字符串比较永远 false → fallback unavailable
    const code = e?.code
    if (code === 1002101) {
      ElMessage.error(t('auth.error.codeExpired'))
    } else if (code === 1002102) {
      ElMessage.error(t('auth.error.codeInvalid'))
    } else {
      ElMessage.error(e?.message || t('auth.resetPassword.unavailable'))
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="reset-card">
    <!-- 标题 — H5 沿用文字标题(PC 大标题在 AuthHero) -->
    <header class="reset-card__header">
      <h1 class="reset-card__title">{{ t('auth.resetPassword.title') }}</h1>
    </header>

    <!-- 步骤指示器 — 主色实底标记 active -->
    <div class="reset-steps" role="list" aria-label="reset progress">
      <template v-for="n in 3" :key="n">
        <div
          class="reset-steps__item"
          :class="{ 'is-active': step === n, 'is-done': step > n }"
          role="listitem"
          :aria-current="step === n ? 'step' : undefined"
        >
          {{ n }}
        </div>
        <div v-if="n < 3" class="reset-steps__bar" :class="{ 'is-done': step > n }" aria-hidden="true" />
      </template>
    </div>

    <!-- Step 1: 输入邮箱 -->
    <div v-if="step === 1">
      <el-form
        ref="step1Ref"
        :model="form"
        :rules="step1Rules"
        label-position="top"
        @submit.prevent="handleStep1"
      >
        <el-form-item :label="t('auth.resetPassword.email')" prop="email">
          <el-input
            v-model="form.email"
            type="email"
            size="large"
            :placeholder="t('auth.resetPassword.email')"
            autocomplete="email"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="reset-card__btn"
            size="large"
            native-type="submit"
            :loading="submitting"
          >
            {{ t('auth.resetPassword.sendCode') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Step 2: 输入验证码 -->
    <div v-else-if="step === 2" class="reset-step2">
      <p class="reset-step2__hint">{{ t('auth.resetPassword.sentTo', { email: form.email }) }}</p>
      <el-form label-position="top" @submit.prevent="handleStep2">
        <el-form-item :label="t('auth.resetPassword.code')">
          <VerifyCodeInput
            v-model="form.code"
            :on-send="sendCode"
            :placeholder="t('auth.resetPassword.code')"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="reset-card__btn"
            size="large"
            native-type="submit"
          >
            {{ t('auth.resetPassword.next') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Step 3: 输入新密码 -->
    <div v-else>
      <el-form
        ref="step3Ref"
        :model="form"
        :rules="step3Rules"
        label-position="top"
        @submit.prevent="handleStep3"
      >
        <el-form-item :label="t('auth.resetPassword.newPassword')" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            size="large"
            :placeholder="t('auth.resetPassword.newPassword')"
            autocomplete="new-password"
          />
        </el-form-item>

        <el-form-item :label="t('auth.resetPassword.confirmPassword')" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            size="large"
            :placeholder="t('auth.resetPassword.confirmPassword')"
            autocomplete="new-password"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="reset-card__btn"
            size="large"
            native-type="submit"
            :loading="submitting"
          >
            {{ t('auth.resetPassword.submit') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 返回登录 -->
    <footer class="reset-card__back">
      <router-link to="/login">
        <span class="reset-card__back-arrow" aria-hidden="true">←</span>
        {{ t('auth.resetPassword.backToLogin') }}
      </router-link>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.reset-card {
  width: 100%;
  max-width: 440px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: brand.$spacing-8;

  @media (min-width: brand.$bp-laptop) {
    padding: brand.$spacing-10 brand.$spacing-8;
    box-shadow: none;
  }

  @media (max-width: 1023px) {
    box-shadow: var(--shadow-base);
  }

  @media (max-width: brand.$bp-tablet) {
    padding: brand.$spacing-6 brand.$spacing-4;
    box-shadow: none;
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

  &__btn {
    width: 100%;
    min-height: 44px;
  }

  &__back {
    margin-block-start: brand.$spacing-5;
    text-align: center;
    font-size: brand.$font-size-sm;

    a {
      color: var(--color-text-secondary);
      text-decoration: none;

      &:hover {
        color: var(--color-primary-deep);
      }
    }
  }

  &__back-arrow {
    display: inline-block;
    margin-inline-end: brand.$spacing-1;
  }

  :deep(.el-input__inner) {
    font-size: brand.$font-size-md;
  }
}

[dir="rtl"] .reset-card__back-arrow {
  transform: scaleX(-1);
}

.reset-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: brand.$spacing-2;
  margin-block-end: brand.$spacing-6;

  &__item {
    width: 32px;
    height: 32px;
    border-radius: var(--radius-full);
    border: 2px solid var(--color-border);
    display: grid;
    place-items: center;
    font-size: brand.$font-size-sm;
    font-weight: 600;
    color: var(--color-text-tertiary);
    background: var(--color-bg-card);
    transition: all 0.2s;

    &.is-active {
      border-color: var(--color-primary);
      background: var(--color-primary);
      color: var(--color-text-inverse);
      box-shadow: var(--ring-focus);
    }

    &.is-done {
      border-color: var(--color-primary-deep);
      background: var(--color-primary-deep);
      color: var(--color-text-inverse);
    }
  }

  &__bar {
    height: 2px;
    width: 32px;
    background: var(--color-border);
    transition: background-color 0.2s;

    &.is-done {
      background: var(--color-primary-deep);
    }
  }
}

.reset-step2 {
  &__hint {
    margin: 0 0 brand.$spacing-4;
    font-size: brand.$font-size-sm;
    color: var(--color-text-secondary);
    line-height: brand.$line-height-base;
  }
}
</style>
