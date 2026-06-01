<script setup>
/**
 * TeacherRegisterView.vue — /teacher/register — 教师注册
 *
 * D27(2026-05-23):用户决策 — 教师注册必填手机号 + 真实姓名;
 *   - 砍掉 邮箱/手机号 二 tab,统一手机号 SMS 注册(海外教师 +852/+886/+1 等都可发)
 *   - 加 realName 字段(必填)→ payload.nickname
 *   - 其他资料(教学经验/学历/教师资格证)继续在 D19 资质认证页(/teacher-center/profile-edit)填
 *
 * Wave 5 G:套 AuthLayout 双栏壳(layout='auth' 已在 router meta 配)。
 * View 仅渲染卡片表单 + 教师资质介绍块 — Logo + 大标题已转移到 AuthHero(PC 左栏)。
 *
 * role 固定为 'teacher'
 */
import { computed, ref, reactive, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAuthChannelsStore } from '@/stores/authChannels'
import { safeRedirect } from '@/utils/safeRedirect'
import MobileWithCountryCode from '@/components/auth/MobileWithCountryCode.vue'
import VerifyCodeInput from '@/components/auth/VerifyCodeInput.vue'
import SocialButtons from '@/components/auth/SocialButtons.vue'
import * as authApi from '@/api/auth'

const route = useRoute()
const router = useRouter()
const { t, locale, tm, rt } = useI18n()
const userStore = useUserStore()
const authChannelsStore = useAuthChannelsStore()
const { channels } = storeToRefs(authChannelsStore)
onMounted(() => authChannelsStore.ensureLoaded())

const submitting = ref(false)

// D19-A8: 教师注册支持 OAuth(socialButtons 透传 role='teacher',后端 social-login 反射建 draft profile)
const socialEnabled = computed(() => ({
  google: channels.value.google,
  apple: channels.value.apple
}))

const teacherIntroPoints = computed(() => {
  const points = tm('auth.register.teacherIntro.points')
  return Array.isArray(points) ? points.map(point => rt(point)) : []
})

const form = reactive({
  realName: '',
  phone: '',
  code: '',
  refCode: '',
  agreedTerms: false
})

const phoneFormRef = ref(null)

const phoneRules = {
  realName: [
    { required: true, message: () => t('auth.validation.realNameRequired'), trigger: 'blur' },
    { min: 2, max: 40, message: () => t('auth.validation.realNameRequired'), trigger: 'blur' }
  ],
  phone: [
    { required: true, message: () => t('auth.phone.required'), trigger: 'blur' },
    { pattern: /^\+[0-9]{7,15}$/, message: () => t('auth.phone.invalid'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: () => t('auth.validation.codeRequired'), trigger: 'blur' }
  ]
}

function mapErrorMessage(e) {
  const MAP = {
    1002104: 'auth.error.teacherEmailExists',
    1002105: 'auth.error.teacherPhoneExists',
    1002108: 'auth.error.referralInvalid',
    1002113: 'auth.error.smtpNotConfigured',
    1002114: 'auth.error.passwordWeak',
    1002117: 'auth.error.ipRegisterLimit'
  }
  const key = MAP[e?.code]
  if (key) return t(key)
  return e?.message || t('common.error')
}

async function sendSmsCode() {
  if (!/^\+[0-9]{7,15}$/.test(form.phone)) throw new Error(t('auth.phone.invalid'))
  await authApi.sendSmsCode(form.phone, 'register')
}

async function handleSubmit() {
  if (!phoneFormRef.value) return
  try {
    await phoneFormRef.value.validate()
  } catch {
    return
  }
  if (!form.agreedTerms) {
    ElMessage.warning(t('auth.validation.termsRequired'))
    return
  }
  submitting.value = true
  try {
    await userStore.registerByPhone({
      role: 'teacher',
      phone: form.phone,
      code: form.code,
      nickname: form.realName.trim(),
      locale: locale.value,
      tz: Intl.DateTimeFormat().resolvedOptions().timeZone,
      refCode: form.refCode || undefined
    })
    const target = route.query.redirect
      ? safeRedirect(route.query.redirect, '/teacher-center', router)
      : '/teacher-center'
    router.replace(target)
  } catch (e) {
    ElMessage.error(mapErrorMessage(e))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="teacher-register-card">
    <!-- 标题 — H5 沿用文字标题(PC 大标题在 AuthHero) -->
    <header class="teacher-register-card__header">
      <h1 class="teacher-register-card__title">{{ t('auth.register.title') }}</h1>
    </header>

    <p class="teacher-register-card__notice">
      {{ t('auth.register.teacherReviewNotice') }}
    </p>

    <!-- 教师资质介绍区块 -->
    <div class="teacher-intro">
      <h2 class="teacher-intro__heading">{{ t('auth.register.teacherIntro.heading') }}</h2>
      <ul class="teacher-intro__list">
        <li
          v-for="(point, idx) in teacherIntroPoints"
          :key="idx"
          class="teacher-intro__item"
        >
          <span class="teacher-intro__bullet" aria-hidden="true">✓</span>
          <span>{{ point }}</span>
        </li>
      </ul>
    </div>

    <el-form
      ref="phoneFormRef"
      :model="form"
      :rules="phoneRules"
      label-position="top"
      @submit.prevent="handleSubmit"
    >
      <el-form-item :label="t('auth.register.realName')" prop="realName">
        <el-input
          v-model="form.realName"
          size="large"
          maxlength="40"
          :placeholder="t('auth.register.realNamePlaceholder')"
        />
      </el-form-item>

      <el-form-item :label="t('auth.login.phone')" prop="phone">
        <MobileWithCountryCode v-model="form.phone" :placeholder="t('auth.login.phone')" />
      </el-form-item>

      <el-form-item :label="t('auth.login.code')" prop="code">
        <VerifyCodeInput
          v-model="form.code"
          :on-send="sendSmsCode"
          :placeholder="t('auth.login.code')"
        />
      </el-form-item>

      <el-form-item prop="agreedTerms">
        <el-checkbox v-model="form.agreedTerms">
          <i18n-t keypath="auth.register.agreeTerms">
            <template #terms>
              <a href="/legal/terms" target="_blank" class="teacher-register-card__link">{{ t('auth.register.termsLabel') }}</a>
            </template>
            <template #privacy>
              <a href="/legal/privacy" target="_blank" class="teacher-register-card__link">{{ t('auth.register.privacyLabel') }}</a>
            </template>
          </i18n-t>
        </el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          class="teacher-register-card__submit"
          size="large"
          native-type="submit"
          :loading="submitting"
        >
          {{ t('auth.register.submit') }}
        </el-button>
      </el-form-item>
    </el-form>

    <div class="teacher-register-card__divider">
      <span>{{ t('auth.register.socialDivider') }}</span>
    </div>

    <div class="teacher-register-card__social">
      <SocialButtons
        :providers="['google', 'apple']"
        :enabled="socialEnabled"
        role="teacher"
      />
    </div>

    <!-- 去登录 -->
    <footer class="teacher-register-card__footer">
      <span class="teacher-register-card__footer-text">{{ t('auth.register.haveAccount') }}</span>
      <router-link to="/login" class="teacher-register-card__footer-link">
        {{ t('auth.register.goLogin') }}
      </router-link>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.teacher-register-card {
  width: 100%;
  max-width: 520px;
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

  &__submit {
    width: 100%;
    min-height: 44px;
  }

  &__link {
    color: var(--color-info);
    text-decoration: underline;
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

  &__notice {
    margin-block-start: 0;
    margin-block-end: brand.$spacing-5;
    padding: brand.$spacing-3 brand.$spacing-4;
    border: 1px solid var(--color-primary);
    border-radius: var(--radius-base);
    background: var(--color-primary-soft);
    color: var(--color-text-primary);
    font-size: brand.$font-size-sm;
    font-weight: 600;
    line-height: brand.$line-height-base;
    overflow-wrap: anywhere;
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

  :deep(.el-input__inner) {
    font-size: brand.$font-size-md;
  }

  :deep(.el-checkbox__label) {
    white-space: normal;
    line-height: brand.$line-height-base;
  }
}

// 资质介绍区块
.teacher-intro {
  background: var(--color-primary-soft);
  border-radius: var(--radius-base);
  padding: brand.$spacing-5 brand.$spacing-6;
  margin-block-end: brand.$spacing-6;
  overflow-wrap: anywhere;

  &__heading {
    margin: 0 0 brand.$spacing-3;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: var(--color-text-primary);
    line-height: brand.$line-height-tight;
  }

  &__list {
    margin: 0;
    padding: 0;
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__item {
    display: flex;
    align-items: flex-start;
    gap: brand.$spacing-2;
    font-size: brand.$font-size-sm;
    color: var(--color-text-secondary);
    line-height: brand.$line-height-base;
    min-width: 0;
  }

  &__bullet {
    color: var(--color-primary-deep);
    font-weight: 700;
    flex-shrink: 0;
    margin-block-start: 1px;
  }

  @media (max-width: brand.$bp-tablet) {
    padding: brand.$spacing-4;
  }
}
</style>
