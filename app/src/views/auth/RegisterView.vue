<script setup>
/**
 * RegisterView.vue — /register — 学生注册
 *
 * Wave 5 G:套 AuthLayout 双栏壳(layout='auth' 已在 router meta 配)。
 * View 仅渲染卡片表单 — Logo + 大标题已转移到 AuthHero(PC 左栏)。
 *
 * role 固定为 'student'
 * form: tab(邮箱/手机) + 推荐码(选填) + 条款勾选
 */
import { ref, reactive, watch, onMounted, computed } from 'vue'
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
const { t, locale } = useI18n()
const userStore = useUserStore()
const authChannelsStore = useAuthChannelsStore()
const { channels } = storeToRefs(authChannelsStore)

const activeMethod = ref('email')
const submitting = ref(false)

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

const form = reactive({
  email: '',
  phone: '',
  password: '',
  code: '',
  refCode: '',
  agreedTerms: false
})

onMounted(() => {
  authChannelsStore.ensureLoaded()
  const refCode = typeof route.query.ref === 'string' ? route.query.ref : ''
  if (refCode && !form.refCode) {
    form.refCode = refCode.trim().toUpperCase()
  }
})

const emailFormRef = ref(null)
const phoneFormRef = ref(null)

const emailRules = {
  email: [
    { required: true, message: () => t('auth.validation.emailRequired'), trigger: 'blur' },
    { type: 'email', message: () => t('auth.validation.emailInvalid'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('auth.error.passwordWeak'), trigger: 'blur' },
    { min: 8, message: t('auth.error.passwordWeak'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: () => t('auth.validation.codeRequired'), trigger: 'blur' }
  ],
  agreedTerms: [
    {
      validator: (rule, value, callback) => {
        if (!value) callback(new Error(t('auth.validation.termsRequired')))
        else callback()
      },
      trigger: 'change'
    }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: () => t('auth.phone.required'), trigger: 'blur' },
    { pattern: /^\+[0-9]{7,15}$/, message: () => t('auth.phone.invalid'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: () => t('auth.validation.codeRequired'), trigger: 'blur' }
  ],
  agreedTerms: [
    {
      validator: (rule, value, callback) => {
        if (!value) callback(new Error(t('auth.validation.termsRequired')))
        else callback()
      },
      trigger: 'change'
    }
  ]
}

function mapErrorMessage(e) {
  // 数字错误码映射(后端 ErrorCodeConstants 一一对应)
  const MAP = {
    1002104: 'auth.error.emailExists',
    1002105: 'auth.error.phoneExists',
    1002108: 'auth.error.referralInvalid',
    1002113: 'auth.error.smtpNotConfigured',
    1002114: 'auth.error.passwordWeak',
    1002117: 'auth.error.ipRegisterLimit'
  }
  const key = MAP[e?.code]
  if (key) return t(key)
  // 未识别码:回退后端 msg(已国际化前端文案可能没覆盖,后端 msg 通常已经是清晰提示)
  return e?.message || t('common.error')
}

async function sendEmailCode() {
  if (!form.email) throw new Error(t('auth.validation.emailRequired'))
  await authApi.sendEmailCode(form.email, 'register')
}

async function sendSmsCode() {
  if (!/^\+[0-9]{7,15}$/.test(form.phone)) throw new Error(t('auth.phone.invalid'))
  await authApi.sendSmsCode(form.phone, 'register')
}

async function handleSubmitEmail() {
  if (!emailFormRef.value) return
  try {
    await emailFormRef.value.validate()
  } catch {
    return
  }
  if (!form.agreedTerms) {
    ElMessage.warning(t('auth.validation.termsRequired'))
    return
  }
  await doRegister({
    type: 'email',
    email: form.email,
    password: form.password,
    code: form.code,
    refCode: form.refCode || undefined
  })
}

async function handleSubmitPhone() {
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
  await doRegister({
    type: 'phone',
    phone: form.phone,
    code: form.code,
    refCode: form.refCode || undefined
  })
}

async function doRegister(payload) {
  submitting.value = true
  try {
    const common = {
      role: 'student',
      locale: locale.value,
      tz: Intl.DateTimeFormat().resolvedOptions().timeZone
    }
    if (payload.type === 'email') {
      await userStore.registerByEmail({ ...common, ...payload })
    } else {
      await userStore.registerByPhone({ ...common, ...payload })
    }
    // open redirect 防御:走 safeRedirect 白名单(plan Wave 1.5.1)
    router.replace(safeRedirect(route.query.redirect, '/', router))
  } catch (e) {
    ElMessage.error(mapErrorMessage(e))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="register-card">
    <!-- 标题 — H5 沿用文字标题(PC 大标题在 AuthHero) -->
    <header class="register-card__header">
      <h1 class="register-card__title">{{ t('auth.register.title') }}</h1>
      <p class="register-card__subtitle">{{ t('auth.register.studentHint') }}</p>
    </header>

    <router-link to="/teacher/register" class="register-card__teacher-entry">
      <span class="register-card__teacher-entry-copy">
        {{ t('auth.register.teacherEntryText') }}
      </span>
      <span class="register-card__teacher-entry-action">
        {{ t('auth.register.teacherEntryAction') }}
      </span>
    </router-link>

    <div class="register-card__method-switch" role="tablist" :aria-label="t('auth.register.methodLabel')">
      <button
        type="button"
        class="register-card__method"
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
        class="register-card__method"
        :class="{ 'is-active': activeMethod === 'phone' }"
        :disabled="!channels.sms"
        role="tab"
        :aria-selected="activeMethod === 'phone'"
        @click="activeMethod = 'phone'"
      >
        {{ t('auth.login.phoneTab') }}
      </button>
    </div>

    <el-form
      v-if="activeMethod === 'email'"
      ref="emailFormRef"
      :model="form"
      :rules="emailRules"
      label-position="top"
      @submit.prevent="handleSubmitEmail"
    >
      <el-form-item :label="t('auth.login.email')" prop="email">
        <el-input v-model="form.email" type="email" size="large" :placeholder="t('auth.login.email')" />
      </el-form-item>

      <el-form-item :label="t('auth.login.password')" prop="password">
        <el-input v-model="form.password" type="password" show-password size="large" :placeholder="t('auth.login.password')" />
      </el-form-item>

      <el-form-item :label="t('auth.login.code')" prop="code">
        <VerifyCodeInput
          v-model="form.code"
          :on-send="sendEmailCode"
          :placeholder="t('auth.login.code')"
        />
      </el-form-item>

      <el-form-item :label="t('auth.register.referralCode')" prop="refCode">
        <el-input v-model="form.refCode" size="large" :placeholder="t('auth.register.referralCode')" />
      </el-form-item>

      <el-form-item prop="agreedTerms">
        <el-checkbox v-model="form.agreedTerms">
          <i18n-t keypath="auth.register.agreeTerms">
            <template #terms>
              <a href="/legal/terms" target="_blank" class="register-card__link">{{ t('auth.register.termsLabel') }}</a>
            </template>
            <template #privacy>
              <a href="/legal/privacy" target="_blank" class="register-card__link">{{ t('auth.register.privacyLabel') }}</a>
            </template>
          </i18n-t>
        </el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          class="register-card__submit"
          size="large"
          native-type="submit"
          :loading="submitting"
        >
          {{ t('auth.register.submit') }}
        </el-button>
      </el-form-item>
    </el-form>

    <el-form
      v-else
      ref="phoneFormRef"
      :model="form"
      :rules="phoneRules"
      label-position="top"
      @submit.prevent="handleSubmitPhone"
    >
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

      <el-form-item :label="t('auth.register.referralCode')" prop="refCode">
        <el-input v-model="form.refCode" size="large" :placeholder="t('auth.register.referralCode')" />
      </el-form-item>

      <el-form-item prop="agreedTerms">
        <el-checkbox v-model="form.agreedTerms">
          <i18n-t keypath="auth.register.agreeTerms">
            <template #terms>
              <a href="/legal/terms" target="_blank" class="register-card__link">{{ t('auth.register.termsLabel') }}</a>
            </template>
            <template #privacy>
              <a href="/legal/privacy" target="_blank" class="register-card__link">{{ t('auth.register.privacyLabel') }}</a>
            </template>
          </i18n-t>
        </el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          class="register-card__submit"
          size="large"
          native-type="submit"
          :loading="submitting"
        >
          {{ t('auth.register.submit') }}
        </el-button>
      </el-form-item>
    </el-form>

    <div class="register-card__divider">
      <span>{{ t('auth.register.socialDivider') }}</span>
    </div>

    <div class="register-card__social">
      <SocialButtons
        :providers="['google', 'apple']"
        :enabled="socialEnabled"
      />
    </div>

    <!-- 去登录 -->
    <footer class="register-card__footer">
      <span class="register-card__footer-text">{{ t('auth.register.haveAccount') }}</span>
      <router-link to="/login" class="register-card__footer-link">
        {{ t('auth.register.goLogin') }}
      </router-link>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.register-card {
  width: 100%;
  max-width: 460px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  // D26-H2 紧凑模式:padding-block 缩小,降低首屏滚动条概率
  padding: brand.$spacing-6 brand.$spacing-8;

  @media (min-width: brand.$bp-laptop) {
    padding: brand.$spacing-6 brand.$spacing-8;
    box-shadow: none;
  }

  @media (max-width: 1023px) {
    box-shadow: var(--shadow-base);
  }

  @media (max-width: brand.$bp-tablet) {
    padding: brand.$spacing-5 brand.$spacing-4;
    box-shadow: none;
  }

  // D26-H2 EP form 间距压缩
  :deep(.el-form-item) {
    margin-block-end: brand.$spacing-3; // 22px → 12px
  }
  :deep(.el-form-item__label) {
    padding-block-end: 2px;
    line-height: 1.4;
    font-size: brand.$font-size-sm;
  }
  :deep(.el-input--large .el-input__wrapper) {
    padding-block: 0;
  }
  :deep(.el-input--large .el-input__inner) {
    height: 38px;       // 默认 40 缩到 38
    font-size: brand.$font-size-base;
  }

  &__header {
    margin-block-end: brand.$spacing-3;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-xl;
    font-weight: 700;
    color: var(--color-text-primary);
    line-height: brand.$line-height-tight;
  }

  &__subtitle {
    margin: brand.$spacing-1 0 0;
    color: var(--color-text-secondary);
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }

  &__teacher-entry {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: brand.$spacing-3;
    margin-block-end: brand.$spacing-3;
    padding: brand.$spacing-2 brand.$spacing-3;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-base);
    background: var(--color-primary-soft);
    color: var(--color-text-primary);
    text-decoration: none;
    transition: border-color 0.15s, box-shadow 0.15s;

    &:hover {
      border-color: var(--color-primary);
      box-shadow: var(--shadow-base);
    }
  }

  &__teacher-entry-copy {
    min-width: 0;
    color: var(--color-text-secondary);
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }

  &__teacher-entry-action {
    flex-shrink: 0;
    color: var(--color-primary-deep);
    font-size: brand.$font-size-sm;
    font-weight: 700;
    line-height: 1;
  }

  &__method-switch {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: brand.$spacing-1;
    padding: brand.$spacing-1;
    margin-block-end: brand.$spacing-3;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-base);
    background: var(--color-bg-page);
  }

  &__method {
    min-height: 36px;
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

  &__submit {
    width: 100%;
    min-height: 40px;
  }

  &__link {
    color: var(--color-info);
    text-decoration: underline;
  }

  &__footer {
    margin-block-start: brand.$spacing-3;
    text-align: center;
    font-size: brand.$font-size-sm;
    border-block-start: 1px solid var(--color-border);
    padding-block-start: brand.$spacing-3;
  }

  &__social {
    margin-block-start: brand.$spacing-3;
  }

  &__divider {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-block-start: brand.$spacing-4;
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

  :deep(.el-checkbox__label) {
    white-space: normal;
    line-height: brand.$line-height-base;
  }
}
</style>
