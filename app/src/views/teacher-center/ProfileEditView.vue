<script setup>
/**
 * ProfileEditView — 教师"我的档案"(D19 Phase B / B6)
 *
 * 教师本人维护自我档案 + 介绍视频 + 触发再次提交审核。
 *
 * 数据流(对齐 B4 AppTeacherProfileController):
 *   - onMounted → GET /me 拉档案 + auditStatus + qualificationCount
 *   - 保存按钮 → PUT /me(partial,不改 audit_status)
 *   - 提交审核按钮 → POST /submit-audit(auditStatus === 'draft' / 'rejected' 显示)
 *
 * 视频上传:
 *   - 走 /infra/file/upload(AppFileController),COS Mandarly bucket
 *   - 三道前端校验:format(mp4)+ size(≤50MB)+ duration(≤60s,用 <video> metadata 读)
 *   - 后端会再做大小 / mime 兜底,前端校验只为更快反馈
 *
 * 顶部 banner 复用 B8 AuditStatusBanner(pending 黄 / rejected 红 / approved 收起)。
 *
 * 文案中性化(memory: feedback_user_facing_copy_neutral):
 *   - 避免"被驳回 / 不支持"等内部术语,统一"需要补充 / 暂未上传"等中性提示。
 */
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import * as authApi from '@/api/auth'
import {
  getMyTeacherProfile,
  updateMyTeacherProfile,
  submitForAudit
} from '@/api/teacher-center/profile'
import { useUserStore } from '@/stores/user'
import AuditStatusBanner from '@/components/teacher-center/AuditStatusBanner.vue'

const { t } = useI18n()
const userStore = useUserStore()

// 枚举(与后端 / teachers 列表 filter 对齐;不再独立维护一份)
const EXPERTISE_OPTIONS = ['business', 'daily', 'kids', 'HSK', 'speaking']
const LANGUAGE_OPTIONS = ['zh', 'en', 'ar', 'yue', 'ja', 'ko']
const ACCENT_OPTIONS = ['mandarin_cn', 'mandarin_tw', 'cantonese', 'mixed']

// 上传约束
const MAX_VIDEO_SIZE_MB = 50
const MAX_VIDEO_SIZE_BYTES = MAX_VIDEO_SIZE_MB * 1024 * 1024
const MAX_VIDEO_DURATION_SEC = 60
const ALLOWED_VIDEO_MIME = ['video/mp4']
const INTRO_MAX_LEN = 1024
const MAX_AVATAR_SIZE_MB = 5
const MAX_AVATAR_SIZE_BYTES = MAX_AVATAR_SIZE_MB * 1024 * 1024
const ACCEPTED_AVATAR_TYPES = 'image/png,image/jpeg,image/jpg,image/webp'

// ---------- state ----------
const loading = ref(true)
const saving = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref(null)
const loadError = ref('')

const profile = ref({
  userId: null,
  auditStatus: null,
  rejectReason: '',
  auditedAt: null,
  qualificationCount: 0
})

const form = ref({
  intro: '',
  expertise: [],
  languages: [],
  accent: '',
  yearsExperience: 0,
  introVideoUrl: ''
})

// ---------- computed ----------
const canSubmitForAudit = computed(() => ['draft', 'rejected'].includes(profile.value.auditStatus))

const currentAvatarUrl = computed(() => userStore.profile?.avatarUrl || '')

const avatarFallback = computed(() => {
  const name = userStore.profile?.nickname || userStore.profile?.email || 'T'
  return String(name).slice(0, 1).toUpperCase()
})

const auditedAtDisplay = computed(() => {
  if (!profile.value.auditedAt) return ''
  try {
    const d = new Date(profile.value.auditedAt)
    return d.toLocaleString()
  } catch (e) {
    return profile.value.auditedAt
  }
})

// ---------- load ----------
async function loadProfile() {
  loading.value = true
  loadError.value = ''
  try {
    if (userStore.isLoggedIn && !userStore.profile && typeof userStore.refreshProfile === 'function') {
      await userStore.refreshProfile()
    }
    const data = await getMyTeacherProfile()
    profile.value = {
      userId: data?.userId ?? null,
      auditStatus: data?.auditStatus ?? null,
      rejectReason: data?.rejectReason ?? '',
      auditedAt: data?.auditedAt ?? null,
      qualificationCount: Number(data?.qualificationCount ?? 0)
    }
    form.value = {
      intro: data?.intro ?? '',
      expertise: Array.isArray(data?.expertise) ? [...data.expertise] : [],
      languages: Array.isArray(data?.languages) ? [...data.languages] : [],
      accent: data?.accent ?? '',
      yearsExperience: Number.isFinite(Number(data?.yearsExperience))
        ? Number(data.yearsExperience)
        : 0,
      introVideoUrl: data?.introVideoUrl ?? ''
    }
  } catch (e) {
    loadError.value = e?.message || t('teacherCenter.profile.messages.loadFailed')
  } finally {
    loading.value = false
  }
}

// ---------- avatar upload ----------
function triggerAvatarPicker() {
  if (uploadingAvatar.value) return
  avatarInputRef.value?.click()
}

async function onAvatarChange(e) {
  const file = e.target?.files?.[0]
  if (!file) return
  e.target.value = ''

  if (!file.type?.startsWith('image/')) {
    ElMessage.warning(t('teacherCenter.profile.messages.avatarFormatInvalid'))
    return
  }
  if (file.size > MAX_AVATAR_SIZE_BYTES) {
    ElMessage.warning(t('teacherCenter.profile.messages.avatarTooLarge', { mb: MAX_AVATAR_SIZE_MB }))
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
    if (!url) throw new Error(t('teacherCenter.profile.messages.avatarUploadFailed'))
    await authApi.updateProfile({ avatarUrl: url })
    if (typeof userStore.refreshProfile === 'function') {
      await userStore.refreshProfile()
    }
    ElMessage.success(t('teacherCenter.profile.messages.avatarUploadSuccess'))
  } catch (e2) {
    ElMessage.error(e2?.message || t('teacherCenter.profile.messages.avatarUploadFailed'))
  } finally {
    uploadingAvatar.value = false
  }
}

// ---------- save ----------
async function handleSave() {
  if (saving.value) return
  saving.value = true
  try {
    await updateMyTeacherProfile({
      intro: form.value.intro?.trim() || null,
      expertise: form.value.expertise,
      languages: form.value.languages,
      accent: form.value.accent || null,
      yearsExperience: form.value.yearsExperience,
      introVideoUrl: form.value.introVideoUrl || null
    })
    ElMessage.success(t('teacherCenter.profile.messages.saveSuccess'))
    // 同步 userStore.profile.teacherAuditStatus(banner 跟着刷新);保存不会改状态
    // 但 banner 在 reject → 保存后教师习惯再点提交审核,这里先 refresh 一次保持一致
    if (typeof userStore.refreshProfile === 'function') {
      userStore.refreshProfile().catch(() => {})
    }
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.profile.messages.saveFailed'))
  } finally {
    saving.value = false
  }
}

// ---------- submit-audit ----------
async function handleSubmitForAudit() {
  if (!canSubmitForAudit.value || submitting.value) return
  try {
    await ElMessageBox.confirm(
      t('teacherCenter.profile.messages.submitAuditConfirm'),
      t('teacherCenter.profile.actions.submitAudit'),
      {
        confirmButtonText: t('teacherCenter.profile.actions.submitAuditConfirm'),
        cancelButtonText: t('common.cancel', { defaultValue: '取消' }),
        type: 'info'
      }
    )
  } catch (e) {
    // 用户取消
    return
  }
  submitting.value = true
  try {
    await submitForAudit()
    ElMessage.success(t('teacherCenter.profile.messages.submitAuditSuccess'))
    // 状态会转 pending → 重新拉,banner 同步切换
    await loadProfile()
    if (typeof userStore.refreshProfile === 'function') {
      userStore.refreshProfile().catch(() => {})
    }
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.profile.messages.submitAuditFailed'))
  } finally {
    submitting.value = false
  }
}

// ---------- video upload ----------
/**
 * 读 video 文件 duration(秒);失败回 -1 让上层走错误分支
 */
function readVideoDuration(file) {
  return new Promise((resolve) => {
    const url = URL.createObjectURL(file)
    const video = document.createElement('video')
    video.preload = 'metadata'
    let settled = false
    const cleanup = () => {
      URL.revokeObjectURL(url)
      video.removeAttribute('src')
      video.load()
    }
    video.onloadedmetadata = () => {
      if (settled) return
      settled = true
      const duration = Number.isFinite(video.duration) ? video.duration : -1
      cleanup()
      resolve(duration)
    }
    video.onerror = () => {
      if (settled) return
      settled = true
      cleanup()
      resolve(-1)
    }
    video.src = url
  })
}

function pickVideoFile() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = ALLOWED_VIDEO_MIME.join(',')
  input.onchange = async () => {
    const file = input.files && input.files[0]
    if (!file) return
    await uploadVideo(file)
  }
  input.click()
}

async function uploadVideo(file) {
  if (uploading.value) return

  // 1) format
  if (!ALLOWED_VIDEO_MIME.includes(file.type)) {
    ElMessage.error(t('teacherCenter.profile.messages.videoFormatInvalid'))
    return
  }
  // 2) size
  if (file.size > MAX_VIDEO_SIZE_BYTES) {
    ElMessage.error(
      t('teacherCenter.profile.messages.videoTooLarge', { mb: MAX_VIDEO_SIZE_MB })
    )
    return
  }
  // 3) duration
  const duration = await readVideoDuration(file)
  if (duration < 0) {
    ElMessage.error(t('teacherCenter.profile.messages.videoUnreadable'))
    return
  }
  if (duration > MAX_VIDEO_DURATION_SEC + 0.5) {
    ElMessage.error(
      t('teacherCenter.profile.messages.videoTooLong', { sec: MAX_VIDEO_DURATION_SEC })
    )
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    // 走统一 request(baseURL=/app-api,自动注入 token + tenant-id,401 自动 refresh)
    // FormData 时显式覆盖 Content-Type 让 axios + 浏览器自填 multipart boundary
    const url = await request.post('/infra/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    })
    if (!url) {
      throw new Error(t('teacherCenter.profile.messages.videoUploadFailed'))
    }
    form.value.introVideoUrl = url
    ElMessage.success(t('teacherCenter.profile.messages.videoUploadSuccess'))
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.profile.messages.videoUploadFailed'))
  } finally {
    uploading.value = false
  }
}

function clearVideo() {
  form.value.introVideoUrl = ''
}

onMounted(() => loadProfile())
</script>

<template>
  <div class="tc-profile">
    <!-- 顶部审核状态横幅(复用 B8) -->
    <AuditStatusBanner
      :status="profile.auditStatus"
      :reason="profile.rejectReason || ''"
    />

    <header class="tc-profile__header">
      <h1 class="tc-profile__title">{{ t('teacherCenter.profile.title') }}</h1>
      <p class="tc-profile__subtitle">{{ t('teacherCenter.profile.subtitle') }}</p>
    </header>

    <div v-if="loading" class="tc-profile__state">
      {{ t('common.loading', { defaultValue: '加载中…' }) }}
    </div>

    <div v-else-if="loadError" class="tc-profile__state tc-profile__state--error">
      <p>{{ loadError }}</p>
      <el-button size="small" @click="loadProfile">
        {{ t('common.retry', { defaultValue: '重试' }) }}
      </el-button>
    </div>

    <form v-else class="tc-profile__form" @submit.prevent="handleSave">
      <!-- 公开头像 / 老师卡片主图 -->
      <div class="tc-profile__field">
        <label class="tc-profile__label">
          {{ t('teacherCenter.profile.avatar.label') }}
        </label>
        <div class="tc-profile__avatar-row">
          <div class="tc-profile__avatar-preview" aria-hidden="true">
            <img
              v-if="currentAvatarUrl"
              :src="currentAvatarUrl"
              :alt="t('teacherCenter.profile.avatar.label')"
              class="tc-profile__avatar-img"
            />
            <span v-else class="tc-profile__avatar-fallback">{{ avatarFallback }}</span>
          </div>
          <div class="tc-profile__avatar-copy">
            <el-button
              :loading="uploadingAvatar"
              :disabled="saving || uploading"
              @click="triggerAvatarPicker"
            >
              {{ uploadingAvatar
                ? t('teacherCenter.profile.avatar.uploading')
                : t('teacherCenter.profile.avatar.upload') }}
            </el-button>
            <p class="tc-profile__hint">
              {{ t('teacherCenter.profile.avatar.hint', { mb: MAX_AVATAR_SIZE_MB }) }}
            </p>
          </div>
          <input
            ref="avatarInputRef"
            type="file"
            :accept="ACCEPTED_AVATAR_TYPES"
            class="tc-profile__avatar-input"
            aria-hidden="true"
            tabindex="-1"
            @change="onAvatarChange"
          />
        </div>
      </div>

      <!-- 自我介绍 -->
      <div class="tc-profile__field">
        <label class="tc-profile__label" for="tc-profile-intro">
          {{ t('teacherCenter.profile.intro.label') }}
        </label>
        <el-input
          id="tc-profile-intro"
          v-model="form.intro"
          type="textarea"
          :rows="6"
          :maxlength="INTRO_MAX_LEN"
          show-word-limit
          :placeholder="t('teacherCenter.profile.intro.placeholder')"
          :disabled="saving"
        />
        <p class="tc-profile__hint">{{ t('teacherCenter.profile.intro.hint') }}</p>
      </div>

      <!-- 教学方向 -->
      <div class="tc-profile__field">
        <label class="tc-profile__label">
          {{ t('teacherCenter.profile.expertise.label') }}
        </label>
        <el-checkbox-group v-model="form.expertise" :disabled="saving">
          <el-checkbox
            v-for="opt in EXPERTISE_OPTIONS"
            :key="opt"
            :label="opt"
            :value="opt"
          >
            {{ t(`teacher.expertise.${opt}`) }}
          </el-checkbox>
        </el-checkbox-group>
        <p class="tc-profile__hint">{{ t('teacherCenter.profile.expertise.hint') }}</p>
      </div>

      <!-- 语言 -->
      <div class="tc-profile__field">
        <label class="tc-profile__label" for="tc-profile-languages">
          {{ t('teacherCenter.profile.languages.label') }}
        </label>
        <el-select
          id="tc-profile-languages"
          v-model="form.languages"
          multiple
          collapse-tags
          collapse-tags-tooltip
          :placeholder="t('teacherCenter.profile.languages.placeholder')"
          :disabled="saving"
          style="width: 100%"
        >
          <el-option
            v-for="opt in LANGUAGE_OPTIONS"
            :key="opt"
            :label="t(`teacherCenter.profile.languageOptions.${opt}`)"
            :value="opt"
          />
        </el-select>
      </div>

      <!-- 口音 -->
      <div class="tc-profile__field">
        <label class="tc-profile__label">
          {{ t('teacherCenter.profile.accent.label') }}
        </label>
        <el-radio-group v-model="form.accent" :disabled="saving">
          <el-radio
            v-for="opt in ACCENT_OPTIONS"
            :key="opt"
            :label="opt"
            :value="opt"
          >
            {{ t(`teacherCenter.profile.accentOptions.${opt}`) }}
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 教龄 -->
      <div class="tc-profile__field">
        <label class="tc-profile__label" for="tc-profile-years">
          {{ t('teacherCenter.profile.yearsExperience.label') }}
        </label>
        <el-input-number
          id="tc-profile-years"
          v-model="form.yearsExperience"
          :min="0"
          :max="60"
          :step="1"
          :disabled="saving"
          controls-position="right"
        />
        <p class="tc-profile__hint">{{ t('teacherCenter.profile.yearsExperience.hint') }}</p>
      </div>

      <!-- 介绍视频 -->
      <div class="tc-profile__field">
        <label class="tc-profile__label">
          {{ t('teacherCenter.profile.introVideo.label') }}
        </label>

        <div v-if="form.introVideoUrl" class="tc-profile__video-preview">
          <video
            :src="form.introVideoUrl"
            controls
            preload="metadata"
            class="tc-profile__video"
          ></video>
          <div class="tc-profile__video-actions">
            <el-button
              size="small"
              :loading="uploading"
              :disabled="saving"
              @click="pickVideoFile"
            >
              {{ t('teacherCenter.profile.introVideo.replace') }}
            </el-button>
            <el-button
              size="small"
              :disabled="saving || uploading"
              @click="clearVideo"
            >
              {{ t('teacherCenter.profile.introVideo.remove') }}
            </el-button>
          </div>
        </div>

        <div v-else class="tc-profile__video-empty">
          <el-button
            type="primary"
            plain
            :loading="uploading"
            :disabled="saving"
            @click="pickVideoFile"
          >
            {{ t('teacherCenter.profile.introVideo.upload') }}
          </el-button>
          <p class="tc-profile__hint">
            {{ t('teacherCenter.profile.introVideo.hint', {
              mb: MAX_VIDEO_SIZE_MB,
              sec: MAX_VIDEO_DURATION_SEC
            }) }}
          </p>
        </div>
      </div>

      <!-- 审核信息(只读元数据) -->
      <div v-if="auditedAtDisplay || profile.qualificationCount > 0" class="tc-profile__meta">
        <div v-if="profile.qualificationCount >= 0" class="tc-profile__meta-row">
          <span class="tc-profile__meta-label">
            {{ t('teacherCenter.profile.meta.qualificationCount') }}
          </span>
          <strong>{{ profile.qualificationCount }}</strong>
        </div>
        <div v-if="auditedAtDisplay" class="tc-profile__meta-row">
          <span class="tc-profile__meta-label">
            {{ t('teacherCenter.profile.meta.auditedAt') }}
          </span>
          <span>{{ auditedAtDisplay }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="tc-profile__actions">
        <el-button
          v-if="canSubmitForAudit"
          :disabled="saving || uploadingAvatar"
          :loading="submitting"
          @click="handleSubmitForAudit"
        >
          {{ t('teacherCenter.profile.actions.submitAudit') }}
        </el-button>
        <el-button
          type="primary"
          :loading="saving"
          :disabled="uploading || uploadingAvatar"
          native-type="submit"
        >
          {{ saving
            ? t('teacherCenter.profile.actions.saving')
            : t('teacherCenter.profile.actions.save') }}
        </el-button>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
.tc-profile {
  max-width: 720px;
  margin: 0 auto;
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }

  &__header {
    margin-block-end: brand.$spacing-6;
  }

  &__title {
    margin: 0 0 brand.$spacing-1;
    font-size: brand.$font-size-2xl;
    font-weight: 700;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;
  }

  &__subtitle {
    margin: 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
  }

  &__state {
    text-align: center;
    padding: brand.$spacing-12 brand.$spacing-4;
    color: brand.$color-text-secondary;

    &--error {
      color: brand.$color-error;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: brand.$spacing-3;
    }
  }

  &__form {
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    padding: brand.$spacing-6 brand.$spacing-5;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-6;

    @media (min-width: brand.$bp-tablet) {
      padding: brand.$spacing-8;
    }
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__label {
    font-size: brand.$font-size-base;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__hint {
    margin: 0;
    color: brand.$color-text-tertiary;
    font-size: brand.$font-size-sm;
  }

  &__avatar-row {
    display: flex;
    align-items: center;
    gap: brand.$spacing-4;
    padding: brand.$spacing-4;
    border: 1px solid brand.$color-border-light;
    border-radius: brand.$radius-lg;
    background: brand.$color-bg-page;

    @media (max-width: brand.$bp-tablet) {
      align-items: flex-start;
    }
  }

  &__avatar-preview {
    flex: 0 0 96px;
    width: 96px;
    height: 96px;
    border-radius: brand.$radius-lg;
    overflow: hidden;
    background: brand.$brand-gradient;
    box-shadow: brand.$shadow-sm;
  }

  &__avatar-img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__avatar-fallback {
    width: 100%;
    height: 100%;
    display: grid;
    place-items: center;
    color: brand.$btn-primary-text;
    font-size: brand.$font-size-2xl;
    font-weight: 700;
  }

  &__avatar-copy {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: brand.$spacing-2;
    min-width: 0;
  }

  &__avatar-input {
    display: none;
  }

  &__video-preview {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
  }

  &__video {
    width: 100%;
    max-width: 360px;
    border-radius: brand.$radius-base;
    background: #000;
  }

  &__video-actions {
    display: flex;
    gap: brand.$spacing-2;
    flex-wrap: wrap;
  }

  &__video-empty {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
    align-items: flex-start;
  }

  &__meta {
    background: brand.$color-bg-page;
    border-radius: brand.$radius-base;
    padding: brand.$spacing-3 brand.$spacing-4;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__meta-row {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-primary;
  }

  &__meta-label {
    color: brand.$color-text-secondary;
    margin-inline-end: brand.$spacing-3;
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: brand.$spacing-3;
    border-block-start: 1px solid brand.$color-border-light;
    padding-block-start: brand.$spacing-4;

    @media (max-width: brand.$bp-tablet) {
      flex-direction: column-reverse;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
