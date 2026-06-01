<script setup>
/**
 * PhoneLoginForm.vue — 手机号 + 验证码登录表单
 *
 * Emits:
 *   submit({ phone, code }) — 表单验证通过后触发
 */
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import MobileWithCountryCode from './MobileWithCountryCode.vue'
import VerifyCodeInput from './VerifyCodeInput.vue'
import * as authApi from '@/api/auth'

const emit = defineEmits(['submit'])
const { t } = useI18n()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  phone: '',
  code: ''
})

const rules = {
  phone: [
    { required: true, message: () => t('auth.phone.required'), trigger: 'blur' },
    { pattern: /^\+[0-9]{7,15}$/, message: () => t('auth.phone.invalid'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: () => t('auth.validation.codeRequired'), trigger: 'blur' },
    { len: 6, message: () => t('auth.validation.codeLength'), trigger: 'blur' }
  ]
}

async function sendCode() {
  if (!/^\+[0-9]{7,15}$/.test(form.phone)) {
    throw new Error(t('auth.phone.invalid'))
  }
  await authApi.sendSmsCode(form.phone, 'login')
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    emit('submit', { phone: form.phone, code: form.code })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    class="phone-login-form"
    label-position="top"
    @submit.prevent="handleSubmit"
  >
    <el-form-item :label="t('auth.login.phone')" prop="phone">
      <MobileWithCountryCode
        v-model="form.phone"
        :placeholder="t('auth.login.phone')"
      />
    </el-form-item>

    <el-form-item :label="t('auth.login.code')" prop="code">
      <VerifyCodeInput
        v-model="form.code"
        :on-send="sendCode"
        :placeholder="t('auth.login.code')"
      />
    </el-form-item>

    <el-form-item>
      <el-button
        type="primary"
        class="phone-login-form__submit"
        size="large"
        native-type="submit"
        :loading="submitting"
      >
        {{ t('auth.login.submit') }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<style lang="scss" scoped>
.phone-login-form {
  width: 100%;

  &__submit {
    width: 100%;
    min-height: 44px;
  }

  :deep(.el-input__inner) {
    font-size: brand.$font-size-md;
  }
}
</style>
