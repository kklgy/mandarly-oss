<script setup>
// =============================================================================
// AccountView.vue — /profile/account
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § /profile/account 详细规格
//
// 内容:
//   §A 基本资料(头像 / 昵称 / 邮箱(只读)/ 语言 / 时区 + 保存)
//   §B 账号绑定(邮箱 / 手机 / Google / Apple 绑定 / 解绑)
//   §C 修改密码(仅 isBound('email') === true 才显示)
//   §D 退出登录(danger 区,二次 confirm — 但本视图把按钮 emit 给 Layout 统一处理)
//
// 关键改进(plan §4 任务 1.5):
//   - 时区 select 改 Intl.supportedValuesOf('timeZone') 动态生成 + region 分组
//   - 旧浏览器(无 supportedValuesOf API)fallback 到 7 项硬编
//
// API 调用(主 agent 后续在 api/auth.js 加方法):
//   - 修改密码: POST /app-api/edu/auth/change-password
//     当前组件 try/catch + ElMessage 提示"功能即将上线"(待主 agent 接入)
// =============================================================================
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import * as authApi from '@/api/auth'
import request from '@/api/request'
import { ENABLED_LOCALES, normalizeEnabledLocale, setLocale } from '@/i18n'
import { setUserTimezone } from '@/utils/datetime'
import MobileWithCountryCode from '@/components/auth/MobileWithCountryCode.vue'
import VerifyCodeInput from '@/components/auth/VerifyCodeInput.vue'

// 头像上传约束(对齐 ProfileEditView 试讲视频:5MB / image/*)
const MAX_AVATAR_SIZE_MB = 5
const ACCEPTED_AVATAR_TYPES = 'image/png,image/jpeg,image/jpg,image/webp,image/gif'

const emit = defineEmits(['logout'])

const router = useRouter()
const { t, locale } = useI18n()
const userStore = useUserStore()

// 加载 / 保存态
const profileLoading = ref(false)
const saving = ref(false)
const changingPassword = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref(null)

// 基本资料 form
const editForm = reactive({
  nickname: '',
  locale: '',
  tz: ''
})

// 修改密码 form
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 绑定 Dialog 状态
const bindEmailDialog = ref(false)
const bindPhoneDialog = ref(false)
const bindEmailForm = reactive({ email: '', code: '' })
const bindPhoneForm = reactive({ phone: '', code: '' })
const bindingEmail = ref(false)
const bindingPhone = ref(false)

// 语言选项
const LOCALE_LABELS = {
  en: 'English',
  'zh-CN': '简体中文',
  'zh-TW': '繁體中文'
}

const LOCALE_OPTIONS = ENABLED_LOCALES.map((value) => ({
  value,
  label: LOCALE_LABELS[value]
}))

// 时区 fallback(Intl.supportedValuesOf 不可用时使用)
// 排序按字母顺序(Africa → America → Asia → Europe → UTC),与 REGION_ORDER 不同(REGION_ORDER 用于动态分组)
const TZ_FALLBACK = [
  { value: 'Africa/Accra', label: 'Accra (GMT+0)' }, // N2 加纳上线 2026-05-11(PRD-v4.3 第一批上线市场)
  { value: 'America/New_York', label: 'New York (EST/EDT)' },
  { value: 'Asia/Dubai', label: 'Dubai (GST, UTC+4)' },
  { value: 'Asia/Hong_Kong', label: 'Hong Kong (HKT, UTC+8)' },
  { value: 'Asia/Shanghai', label: '中国大陆 (CST, UTC+8)' },
  { value: 'Asia/Taipei', label: '台湾 (TST, UTC+8)' },
  { value: 'Europe/London', label: 'London (GMT/BST)' },
  { value: 'UTC', label: 'UTC' }
]

// region 分组排序
const REGION_ORDER = ['Asia', 'Europe', 'America', 'Africa', 'Australia', 'Pacific', 'Atlantic', 'Indian', 'Antarctica', 'Etc', 'UTC']

// 时区分组(Intl.supportedValuesOf 动态)
const tzGroups = computed(() => {
  let zones = TZ_FALLBACK.map(z => z.value)
  let useDynamic = false
  if (typeof Intl !== 'undefined' && typeof Intl.supportedValuesOf === 'function') {
    try {
      zones = Intl.supportedValuesOf('timeZone')
      useDynamic = true
    } catch {
      zones = TZ_FALLBACK.map(z => z.value)
    }
  }

  // dynamic 模式按 region 分组
  if (useDynamic) {
    const groupMap = new Map()
    for (const z of zones) {
      const region = z.includes('/') ? z.split('/')[0] : 'UTC'
      if (!groupMap.has(region)) groupMap.set(region, [])
      groupMap.get(region).push({ value: z, label: z.replace(/_/g, ' ') })
    }
    // UTC 单条特殊处理
    if (!groupMap.has('UTC')) {
      groupMap.set('UTC', [{ value: 'UTC', label: 'UTC' }])
    }
    return REGION_ORDER
      .filter(r => groupMap.has(r))
      .map(r => ({
        label: r,
        options: groupMap.get(r).sort((a, b) => a.label.localeCompare(b.label))
      }))
  }
  // fallback 模式:无分组,单组返回
  return [{ label: 'Common', options: TZ_FALLBACK }]
})

const profile = computed(() => userStore.profile)
const oauthBindings = computed(() => userStore.oauthBindings || [])

// isBound — 邮箱绑定判断:user.email 非空 || oauthBindings 含 provider
function isBound(type) {
  if (type === 'email') return !!profile.value?.email
  if (type === 'phone') return !!profile.value?.phone
  return oauthBindings.value.some(b => b.provider === type)
}

function getBinding(type) {
  return oauthBindings.value.find(b => b.provider === type)
}

// 仅当邮箱绑定时显示修改密码区
const canChangePassword = computed(() => isBound('email'))

onMounted(async () => {
  profileLoading.value = true
  try {
    if (userStore.isLoggedIn && !profile.value) {
      await userStore.refreshProfile()
    }
    if (profile.value) {
      editForm.nickname = profile.value.nickname || ''
      editForm.locale = normalizeEnabledLocale(profile.value.locale || locale.value)
      editForm.tz = profile.value.timezone || Intl.DateTimeFormat().resolvedOptions().timeZone
    }
  } finally {
    profileLoading.value = false
  }
})

async function handleSave() {
  saving.value = true
  try {
    const nextLocale = normalizeEnabledLocale(editForm.locale)
    await authApi.updateProfile({
      nickname: editForm.nickname,
      locale: nextLocale,
      timezone: editForm.tz
    })
    editForm.locale = nextLocale
    setLocale(nextLocale)
    setUserTimezone(editForm.tz)
    await userStore.refreshProfile()
    ElMessage.success(t('profile.account.saved'))
  } catch {
    ElMessage.error(t('common.error'))
  } finally {
    saving.value = false
  }
}

// D20: 头像上传 — 触发 hidden file input → /infra/file/upload → updateProfile(avatarUrl)
//      后端 service 层 normalize 入 DB,GET /me 出参 presign,所以入参传原始 URL 即可
function triggerAvatarPicker() {
  if (uploadingAvatar.value) return
  avatarInputRef.value?.click()
}

async function onAvatarChange(e) {
  const file = e.target?.files?.[0]
  if (!file) return
  // 清掉 input 让用户能再次选同一个文件
  e.target.value = ''

  if (!file.type?.startsWith('image/')) {
    ElMessage.warning(t('profile.account.avatar.invalidType'))
    return
  }
  if (file.size > MAX_AVATAR_SIZE_MB * 1024 * 1024) {
    ElMessage.warning(t('profile.account.avatar.tooLarge', { mb: MAX_AVATAR_SIZE_MB }))
    return
  }

  uploadingAvatar.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const url = await request.post('/infra/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    })
    if (!url) throw new Error(t('profile.account.avatar.uploadFailed'))
    await authApi.updateProfile({ avatarUrl: url })
    await userStore.refreshProfile()
    ElMessage.success(t('profile.account.avatar.uploadSuccess'))
  } catch (err) {
    ElMessage.error(err?.message || t('profile.account.avatar.uploadFailed'))
  } finally {
    uploadingAvatar.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning(t('profile.account.password.required'))
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning(t('profile.account.password.mismatch'))
    return
  }
  changingPassword.value = true
  try {
    // 主 agent 后续在 api/auth.js 加 changePassword 方法 + 后端 endpoint
    // 当前接口未实现 — 友好提示
    const changePasswordApi = Reflect.get(authApi, 'changePassword')
    if (typeof changePasswordApi === 'function') {
      await changePasswordApi({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success(t('profile.account.password.success'))
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.info(t('profile.account.password.comingSoon'))
    }
  } catch {
    ElMessage.error(t('common.error'))
  } finally {
    changingPassword.value = false
  }
}

// --- 绑定邮箱 ---
async function sendBindEmailCode() {
  if (!bindEmailForm.email) throw new Error('email required')
  await authApi.sendEmailCode(bindEmailForm.email, 'bind')
}

async function handleBindEmail() {
  bindingEmail.value = true
  try {
    await authApi.bindEmail({ email: bindEmailForm.email, code: bindEmailForm.code })
    await userStore.refreshProfile()
    bindEmailDialog.value = false
    ElMessage.success(t('profile.account.bindings.bindEmailSuccess'))
    Object.assign(bindEmailForm, { email: '', code: '' })
  } catch {
    ElMessage.error(t('common.error'))
  } finally {
    bindingEmail.value = false
  }
}

// --- 绑定手机 ---
async function sendBindPhoneCode() {
  if (!/^\+[0-9]{7,15}$/.test(bindPhoneForm.phone)) throw new Error(t('auth.phone.invalid'))
  await authApi.sendSmsCode(bindPhoneForm.phone, 'bind')
}

async function handleBindPhone() {
  bindingPhone.value = true
  try {
    await authApi.bindPhone({ phone: bindPhoneForm.phone, code: bindPhoneForm.code })
    await userStore.refreshProfile()
    bindPhoneDialog.value = false
    ElMessage.success(t('profile.account.bindings.bindPhoneSuccess'))
    Object.assign(bindPhoneForm, { phone: '', code: '' })
  } catch {
    ElMessage.error(t('common.error'))
  } finally {
    bindingPhone.value = false
  }
}

async function handleUnbind(type) {
  try {
    await ElMessageBox.confirm(
      t('profile.account.bindings.unbindConfirm'),
      t('profile.account.bindings.unbind'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
  } catch {
    return
  }
  try {
    await authApi.unbindSocial({ provider: type })
    await userStore.refreshProfile()
    ElMessage.success(t('profile.account.bindings.unbindSuccess'))
  } catch {
    ElMessage.error(t('common.error'))
  }
}

// 退出登录 — emit 给 Layout 统一处理(避免重复 confirm 逻辑)
function onClickLogout() {
  emit('logout')
}

// H5 子页返回 /profile 主入口
function handleBack() {
  router.push('/profile')
}
</script>

<template>
  <div class="account-view" v-loading="profileLoading">
    <!-- H5 子页返回(PC 不显示) -->
    <header class="account-view__topbar">
      <button class="account-view__back" type="button" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ t('profile.menu.account') }}</span>
      </button>
    </header>

    <!-- §A 基本资料 -->
    <section class="account-view__section">
      <h2 class="account-view__heading">{{ t('profile.account.section.basic') }}</h2>

      <div class="account-view__form">
        <!-- 头像 + 更换头像(P1 disabled) -->
        <div class="account-view__avatar-row">
          <div class="account-view__avatar" aria-hidden="true">
            <img
              v-if="profile?.avatarUrl"
              :src="profile.avatarUrl"
              :alt="profile?.nickname || ''"
              class="account-view__avatar-img"
            />
            <span v-else class="account-view__avatar-fallback">
              {{ (profile?.nickname || 'U').slice(0, 1).toUpperCase() }}
            </span>
          </div>
          <el-button :loading="uploadingAvatar" @click="triggerAvatarPicker">
            {{ uploadingAvatar
              ? t('profile.account.avatar.uploading')
              : t('profile.account.action.changeAvatar') }}
          </el-button>
          <input
            ref="avatarInputRef"
            type="file"
            :accept="ACCEPTED_AVATAR_TYPES"
            class="account-view__avatar-input"
            aria-hidden="true"
            tabindex="-1"
            @change="onAvatarChange"
          />
        </div>

        <!-- 昵称 -->
        <div class="account-view__row">
          <label class="account-view__label" for="acc-nickname">
            {{ t('profile.account.field.nickname') }}
          </label>
          <el-input
            id="acc-nickname"
            v-model="editForm.nickname"
            size="large"
            :placeholder="t('profile.account.field.nickname')"
            dir="auto"
          />
        </div>

        <!-- 邮箱(只读) -->
        <div class="account-view__row">
          <label class="account-view__label">
            {{ t('profile.account.field.email') }}
          </label>
          <div class="account-view__readonly">
            <span dir="ltr">{{ profile?.email || t('profile.account.bindings.notBound') }}</span>
            <span v-if="profile?.email" class="account-view__chip account-view__chip--success">
              {{ t('profile.account.bindings.primaryEmail') }}
            </span>
          </div>
        </div>

        <!-- 推荐码(只读) -->
        <div v-if="profile?.referralCode" class="account-view__row">
          <label class="account-view__label">
            {{ t('profile.account.field.referralCode') }}
          </label>
          <div class="account-view__readonly">
            <code class="account-view__code">{{ profile.referralCode }}</code>
          </div>
        </div>

        <!-- 语言 -->
        <div class="account-view__row">
          <label class="account-view__label" for="acc-locale">
            {{ t('profile.account.field.locale') }}
          </label>
          <el-select id="acc-locale" v-model="editForm.locale" size="large">
            <el-option
              v-for="opt in LOCALE_OPTIONS"
              :key="opt.value"
              :value="opt.value"
              :label="opt.label"
            />
          </el-select>
        </div>

        <!-- 时区(动态分组) -->
        <div class="account-view__row">
          <label class="account-view__label" for="acc-tz">
            {{ t('profile.account.field.timezone') }}
          </label>
          <el-select
            id="acc-tz"
            v-model="editForm.tz"
            size="large"
            filterable
            :placeholder="t('profile.account.field.timezone')"
          >
            <el-option-group
              v-for="group in tzGroups"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="opt in group.options"
                :key="opt.value"
                :value="opt.value"
                :label="opt.label"
              />
            </el-option-group>
          </el-select>
        </div>

        <div class="account-view__actions">
          <el-button
            type="primary"
            size="large"
            :loading="saving"
            @click="handleSave"
          >
            {{ t('profile.account.action.save') }}
          </el-button>
        </div>
      </div>
    </section>

    <!-- §B 账号绑定 -->
    <section class="account-view__section">
      <h2 class="account-view__heading">{{ t('profile.account.section.bindings') }}</h2>

      <ul class="account-view__bindings">
        <!-- 邮箱 -->
        <li class="account-view__binding">
          <div class="account-view__binding-info">
            <span class="account-view__binding-type">
              {{ t('profile.account.bindings.email') }}
            </span>
            <span v-if="profile?.email" class="account-view__binding-value" dir="ltr">
              {{ profile.email }}
            </span>
            <span v-else class="account-view__binding-empty">
              {{ t('profile.account.bindings.notBound') }}
            </span>
          </div>
          <span v-if="profile?.email" class="account-view__chip account-view__chip--success">
            {{ t('profile.account.bindings.bound') }}
          </span>
          <el-button
            v-else
            size="small"
            @click="bindEmailDialog = true"
          >
            {{ t('profile.account.bindings.bindNow') }}
          </el-button>
        </li>

        <!-- 手机 -->
        <li class="account-view__binding">
          <div class="account-view__binding-info">
            <span class="account-view__binding-type">
              {{ t('profile.account.bindings.phone') }}
            </span>
            <span v-if="profile?.phone" class="account-view__binding-value" dir="ltr">
              {{ profile.phone }}
            </span>
            <span v-else class="account-view__binding-empty">
              {{ t('profile.account.bindings.notBound') }}
            </span>
          </div>
          <span v-if="profile?.phone" class="account-view__chip account-view__chip--success">
            {{ t('profile.account.bindings.bound') }}
          </span>
          <el-button
            v-else
            size="small"
            @click="bindPhoneDialog = true"
          >
            {{ t('profile.account.bindings.bindNow') }}
          </el-button>
        </li>

        <!-- Google -->
        <li class="account-view__binding">
          <div class="account-view__binding-info">
            <span class="account-view__binding-type">
              {{ t('profile.account.bindings.google') }}
            </span>
            <template v-if="isBound('google')">
              <span class="account-view__binding-value" dir="ltr">
                {{ getBinding('google')?.oauthEmail }}
              </span>
            </template>
            <span v-else class="account-view__binding-empty">
              {{ t('profile.account.bindings.notBound') }}
            </span>
          </div>
          <el-button
            v-if="isBound('google')"
            size="small"
            type="danger"
            plain
            @click="handleUnbind('google')"
          >
            {{ t('profile.account.bindings.unbind') }}
          </el-button>
        </li>

        <!-- Apple -->
        <li class="account-view__binding">
          <div class="account-view__binding-info">
            <span class="account-view__binding-type">
              {{ t('profile.account.bindings.apple') }}
            </span>
            <template v-if="isBound('apple')">
              <span class="account-view__binding-value" dir="ltr">
                {{ getBinding('apple')?.oauthEmail }}
              </span>
            </template>
            <span v-else class="account-view__binding-empty">
              {{ t('profile.account.bindings.notBound') }}
            </span>
          </div>
          <el-button
            v-if="isBound('apple')"
            size="small"
            type="danger"
            plain
            @click="handleUnbind('apple')"
          >
            {{ t('profile.account.bindings.unbind') }}
          </el-button>
        </li>
      </ul>
    </section>

    <!-- §C 修改密码(仅 isBound('email')) -->
    <section v-if="canChangePassword" class="account-view__section">
      <h2 class="account-view__heading">{{ t('profile.account.section.password') }}</h2>

      <div class="account-view__form">
        <div class="account-view__row">
          <label class="account-view__label" for="pw-old">
            {{ t('profile.account.password.current') }}
          </label>
          <el-input
            id="pw-old"
            v-model="passwordForm.oldPassword"
            type="password"
            size="large"
            show-password
            autocomplete="current-password"
          />
        </div>
        <div class="account-view__row">
          <label class="account-view__label" for="pw-new">
            {{ t('profile.account.password.new') }}
          </label>
          <el-input
            id="pw-new"
            v-model="passwordForm.newPassword"
            type="password"
            size="large"
            show-password
            autocomplete="new-password"
          />
        </div>
        <div class="account-view__row">
          <label class="account-view__label" for="pw-confirm">
            {{ t('profile.account.password.confirm') }}
          </label>
          <el-input
            id="pw-confirm"
            v-model="passwordForm.confirmPassword"
            type="password"
            size="large"
            show-password
            autocomplete="new-password"
          />
        </div>
        <div class="account-view__actions">
          <el-button
            type="primary"
            size="large"
            :loading="changingPassword"
            @click="handleChangePassword"
          >
            {{ t('profile.account.password.action') }}
          </el-button>
        </div>
      </div>
    </section>

    <!-- §D 危险区 — 退出登录 -->
    <section class="account-view__section account-view__section--danger">
      <h2 class="account-view__heading">{{ t('profile.account.section.danger') }}</h2>
      <p class="account-view__danger-desc">{{ t('profile.account.logout.desc') }}</p>
      <el-button type="danger" plain size="large" @click="onClickLogout">
        {{ t('profile.account.logout.title') }}
      </el-button>
    </section>

    <!-- 绑定邮箱 Dialog -->
    <el-dialog
      v-model="bindEmailDialog"
      :title="t('profile.account.bindings.bindEmail')"
      width="420px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item :label="t('auth.login.email')">
          <el-input
            v-model="bindEmailForm.email"
            type="email"
            size="large"
            :placeholder="t('auth.login.email')"
          />
        </el-form-item>
        <el-form-item :label="t('auth.login.code')">
          <VerifyCodeInput
            v-model="bindEmailForm.code"
            :on-send="sendBindEmailCode"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindEmailDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="bindingEmail" @click="handleBindEmail">
          {{ t('profile.account.bindings.bindNow') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 绑定手机 Dialog -->
    <el-dialog
      v-model="bindPhoneDialog"
      :title="t('profile.account.bindings.bindPhone')"
      width="420px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item :label="t('auth.login.phone')">
          <MobileWithCountryCode
            v-model="bindPhoneForm.phone"
            :placeholder="t('auth.login.phone')"
          />
        </el-form-item>
        <el-form-item :label="t('auth.login.code')">
          <VerifyCodeInput
            v-model="bindPhoneForm.code"
            :on-send="sendBindPhoneCode"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindPhoneDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="bindingPhone" @click="handleBindPhone">
          {{ t('profile.account.bindings.bindNow') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">

.account-view {
  // PC 端在 ProfileLayout pc-content 内,主动控制内边距
  // H5 端独立子页 — 加 padding
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }
}

.account-view__topbar {
  margin-block-end: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    display: none; // PC 由 Layout 顶 page-title 接管
  }
}

.account-view__back {
  background: transparent;
  border: none;
  color: brand.$brand-primary-deep;
  font: inherit;
  font-size: brand.$font-size-md;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;

  &:hover { color: brand.$brand-primary; }
}

.account-view__section {
  background: brand.$color-bg-card;
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6;
  margin-block-end: brand.$spacing-5;
  box-shadow: brand.$shadow-base;

  &--danger {
    border: 1px solid rgba(255, 77, 79, 0.16); // brand.$color-error 边框 16% — M5 后补 token
  }
}

.account-view__heading {
  margin: 0 0 brand.$spacing-5;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  color: brand.$color-text-primary;
  padding-block-end: brand.$spacing-3;
  border-block-end: 1px solid brand.$color-border-light;
}

.account-view__form {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-5;
}

.account-view__row {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
}

.account-view__label {
  font-size: brand.$font-size-sm;
  font-weight: 500;
  color: brand.$color-text-secondary;
}

.account-view__readonly {
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;
  flex-wrap: wrap;
  font-size: brand.$font-size-base;
  color: brand.$color-text-primary;
  min-height: 40px;
}

.account-view__code {
  font-family: brand.$font-family-mono;
  background: brand.$color-bg-strong;
  padding: brand.$spacing-1 brand.$spacing-3;
  border-radius: brand.$radius-base;
  letter-spacing: 0.05em;
  color: brand.$brand-primary-deep;
  font-weight: 600;
}

.account-view__avatar-row {
  display: flex;
  align-items: center;
  gap: brand.$spacing-4;
}

.account-view__avatar {
  width: 80px;
  height: 80px;
  border-radius: brand.$radius-full;
  background: brand.$brand-primary-soft;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-2xl;
  font-weight: 600;
}

.account-view__avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

// D20: 头像 file input 隐藏(visually hidden 但仍可由 ref.click() 触发)
.account-view__avatar-input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}

.account-view__chip {
  display: inline-flex;
  align-items: center;
  padding: brand.$spacing-1 brand.$spacing-3;
  border-radius: brand.$radius-pill;
  font-size: brand.$font-size-xs;
  font-weight: 500;

  &--success {
    background: rgba(82, 196, 26, 0.12); // brand.$color-success 12%(token 占位)
    color: brand.$color-success;
  }
}

.account-view__actions {
  padding-block-start: brand.$spacing-2;
}

// 账号绑定列表
.account-view__bindings {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
}

.account-view__binding {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: brand.$spacing-4;
  padding: brand.$spacing-4 0;
  border-block-end: 1px solid brand.$color-border-light;

  &:last-child {
    border-block-end: none;
  }
}

.account-view__binding-info {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-1;
  flex: 1;
  min-width: 0;
}

.account-view__binding-type {
  font-size: brand.$font-size-base;
  font-weight: 500;
  color: brand.$color-text-primary;
}

.account-view__binding-value {
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-view__binding-empty {
  font-size: brand.$font-size-sm;
  color: brand.$color-text-tertiary;
}

// 危险区
.account-view__danger-desc {
  margin: 0 0 brand.$spacing-4;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  line-height: brand.$line-height-base;
}
</style>
