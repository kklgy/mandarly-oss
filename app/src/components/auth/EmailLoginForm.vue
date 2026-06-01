<script setup>
/**
 * EmailLoginForm.vue — 邮箱 + 密码登录表单
 *
 * Emits:
 *   submit({ email, password }) — 表单验证通过后触发
 */
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits(['submit'])
const { t } = useI18n()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const rules = {
  email: [
    { required: true, message: () => t('auth.login.email') + ' ' + t('common.error'), trigger: 'blur' },
    { type: 'email', message: () => t('auth.validation.emailInvalid'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: () => t('auth.error.passwordWeak'), trigger: 'blur' },
    { min: 8, message: () => t('auth.error.passwordWeak'), trigger: 'blur' }
  ]
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
    emit('submit', { email: form.email, password: form.password })
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
    class="email-login-form"
    label-position="top"
    @submit.prevent="handleSubmit"
  >
    <el-form-item :label="t('auth.login.email')" prop="email">
      <el-input
        v-model="form.email"
        type="email"
        :placeholder="t('auth.login.email')"
        autocomplete="email"
        size="large"
      />
    </el-form-item>

    <el-form-item :label="t('auth.login.password')" prop="password">
      <el-input
        v-model="form.password"
        type="password"
        :placeholder="t('auth.login.password')"
        autocomplete="current-password"
        show-password
        size="large"
      />
    </el-form-item>

    <el-form-item>
      <el-button
        type="primary"
        class="email-login-form__submit"
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
.email-login-form {
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
