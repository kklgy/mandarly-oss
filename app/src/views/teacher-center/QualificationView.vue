<script setup>
/**
 * QualificationView — 教师"资质材料"(D19 Phase B / B7)
 *
 * 教师本人上传 / 浏览 / 删除资质材料,admin 端在 D19-A 教师审核台逐条审核。
 *
 * 数据流(对齐 B5 AppTeacherQualificationController):
 *   - onMounted → GET /list 拉本人资质材料
 *   - 上传:先 POST /infra/file/upload(multipart)拿 docUrl
 *           → POST /upload(JSON:docType/docUrl/docFilename)落 DB
 *           → 成功后 reload list
 *   - 删除:ElMessageBox.confirm → DELETE /{id} → reload list
 *
 * 顶部 banner 复用 B8 AuditStatusBanner,数据来源:
 *   - userStore.profile.teacherAuditStatus / teacherRejectReason(A7 已暴露)
 *   - 避免重复调 GET /me;Profile 页保存后会 refreshProfile,banner 会自动同步
 *
 * 列表 RespVO 故意不带 docUrl(B5 决策):私有 bucket 教师本人复看
 * 也需要签名 URL,简化设计只展示文件名 + 状态,不做在线预览。
 *
 * 上传约束(与后端兼容):
 *   - format: jpg / png / pdf
 *   - size: ≤ 10 MB
 *   - 后端会再做 mime / size 兜底,前端校验只是更快反馈
 *
 * 文案中性化(memory: feedback_user_facing_copy_neutral):
 *   - "审核中 / 已通过 / 未通过"取代"驳回 / 不通过"等内部术语
 */
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import request from '@/api/request'
import {
  listMyQualifications,
  uploadQualification,
  deleteQualification
} from '@/api/teacher-center/qualification'
import { useUserStore } from '@/stores/user'
import AuditStatusBanner from '@/components/teacher-center/AuditStatusBanner.vue'

const { t } = useI18n()
const userStore = useUserStore()
const { profile: userProfile } = storeToRefs(userStore)

// 材料类型枚举(与后端 TeacherQualificationDocTypeEnum 对齐;passport 作为历史兼容,新上传不主动展示)
const DOC_TYPE_OPTIONS = ['id_card', 'degree_cert', 'teaching_cert', 'english_cert', 'experience_proof']

// 上传约束
const MAX_FILE_SIZE_MB = 10
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024
const ALLOWED_MIME = ['image/jpeg', 'image/png', 'application/pdf']
const ALLOWED_ACCEPT = '.jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf'

// ---------- state ----------
const loading = ref(true)
const uploading = ref(false)
const deletingId = ref(null)
const loadError = ref('')
const list = ref([])

// 上传区:用户先选 docType 再选文件,docType 缺失阻止上传
const selectedDocType = ref('')

// ---------- computed ----------
const auditStatus = computed(() => userProfile.value?.teacherAuditStatus || null)
const rejectReason = computed(() => userProfile.value?.teacherRejectReason || '')

function docTypeLabel(docType) {
  if (!docType) return ''
  // i18n key 必须与 DOC_TYPE_OPTIONS 中的 snake_case 值对齐
  const camel = {
    id_card: 'idCard',
    passport: 'passport',
    degree_cert: 'degreeCert',
    teaching_cert: 'teachingCert',
    english_cert: 'englishCert',
    experience_proof: 'experienceProof'
  }[docType]
  return camel
    ? t(`teacherCenter.qualification.docType.${camel}`)
    : docType
}

function auditStatusLabel(status) {
  if (!status) return ''
  return t(`teacherCenter.qualification.list.auditStatus.${status}`, {
    defaultValue: status
  })
}

/** 映射到 el-tag type:pending=warning / approved=success / rejected=danger */
function auditStatusTagType(status) {
  if (status === 'approved') return 'success'
  if (status === 'rejected') return 'danger'
  return 'warning'
}

function formatCreateTime(raw) {
  if (!raw) return ''
  try {
    const d = new Date(raw)
    if (Number.isNaN(d.getTime())) return raw
    return d.toLocaleString()
  } catch (e) {
    return raw
  }
}

// ---------- load ----------
async function loadList() {
  loading.value = true
  loadError.value = ''
  try {
    const data = await listMyQualifications()
    list.value = Array.isArray(data) ? data : []
  } catch (e) {
    loadError.value = e?.message || t('teacherCenter.qualification.messages.loadFailed')
  } finally {
    loading.value = false
  }
}

// ---------- upload ----------
function pickFile() {
  if (!selectedDocType.value) {
    ElMessage.warning(t('teacherCenter.qualification.upload.noDocType'))
    return
  }
  if (uploading.value) return
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = ALLOWED_ACCEPT
  input.onchange = async () => {
    const file = input.files && input.files[0]
    if (!file) return
    await handleUpload(file)
  }
  input.click()
}

async function handleUpload(file) {
  // 1) format(浏览器没填 type 时退回扩展名检查)
  const fileType = file.type || ''
  const ext = (file.name || '').split('.').pop()?.toLowerCase() || ''
  const formatOk =
    ALLOWED_MIME.includes(fileType) ||
    ['jpg', 'jpeg', 'png', 'pdf'].includes(ext)
  if (!formatOk) {
    ElMessage.error(t('teacherCenter.qualification.upload.errorFormat'))
    return
  }
  // 2) size
  if (file.size > MAX_FILE_SIZE_BYTES) {
    ElMessage.error(t('teacherCenter.qualification.upload.errorSize', { mb: MAX_FILE_SIZE_MB }))
    return
  }

  uploading.value = true
  try {
    // 第一步:上传到 COS 拿 docUrl
    const formData = new FormData()
    formData.append('file', file)
    const docUrl = await request.post('/infra/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    })
    if (!docUrl) {
      throw new Error(t('teacherCenter.qualification.upload.failed'))
    }

    // 第二步:把 (docType + docUrl + docFilename) 提交给后端落 DB
    await uploadQualification({
      docType: selectedDocType.value,
      docUrl,
      docFilename: file.name
    })
    ElMessage.success(t('teacherCenter.qualification.upload.success'))
    // 上传完保留 docType 选择,方便连续上传多份同类材料
    await loadList()
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.qualification.upload.failed'))
  } finally {
    uploading.value = false
  }
}

// ---------- delete ----------
async function handleDelete(row) {
  if (!row?.id || deletingId.value) return
  try {
    await ElMessageBox.confirm(
      t('teacherCenter.qualification.list.deleteConfirm'),
      t('teacherCenter.qualification.list.delete'),
      {
        confirmButtonText: t('teacherCenter.qualification.list.delete'),
        cancelButtonText: t('common.cancel', { defaultValue: '取消' }),
        type: 'warning'
      }
    )
  } catch (e) {
    return // 用户取消
  }
  deletingId.value = row.id
  try {
    await deleteQualification(row.id)
    ElMessage.success(t('teacherCenter.qualification.list.deleteSuccess'))
    await loadList()
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.qualification.list.deleteFailed'))
  } finally {
    deletingId.value = null
  }
}

onMounted(() => loadList())
</script>

<template>
  <div class="tc-qual">
    <!-- 顶部审核状态横幅(复用 B8) -->
    <AuditStatusBanner :status="auditStatus" :reason="rejectReason" />

    <header class="tc-qual__header">
      <h1 class="tc-qual__title">{{ t('teacherCenter.qualification.title') }}</h1>
      <p class="tc-qual__subtitle">{{ t('teacherCenter.qualification.subtitle') }}</p>
      <p class="tc-qual__required">{{ t('teacherCenter.qualification.requiredHint') }}</p>
    </header>

    <!-- 上传卡 -->
    <section class="tc-qual__card">
      <h2 class="tc-qual__card-title">
        {{ t('teacherCenter.qualification.upload.sectionTitle') }}
      </h2>
      <div class="tc-qual__upload">
        <div class="tc-qual__upload-field">
          <label class="tc-qual__label" for="tc-qual-doctype">
            {{ t('teacherCenter.qualification.docType.label') }}
          </label>
          <el-select
            id="tc-qual-doctype"
            v-model="selectedDocType"
            :placeholder="t('teacherCenter.qualification.docType.placeholder')"
            :disabled="uploading"
            style="width: 100%"
          >
            <el-option
              v-for="opt in DOC_TYPE_OPTIONS"
              :key="opt"
              :label="docTypeLabel(opt)"
              :value="opt"
            />
          </el-select>
        </div>
        <div class="tc-qual__upload-action">
          <el-button
            type="primary"
            plain
            :loading="uploading"
            @click="pickFile"
          >
            {{ t('teacherCenter.qualification.upload.button') }}
          </el-button>
          <p class="tc-qual__hint">
            {{ t('teacherCenter.qualification.upload.hint', { mb: MAX_FILE_SIZE_MB }) }}
          </p>
        </div>
      </div>
    </section>

    <!-- 列表卡 -->
    <section class="tc-qual__card">
      <h2 class="tc-qual__card-title">
        {{ t('teacherCenter.qualification.list.sectionTitle') }}
      </h2>

      <div v-if="loading" class="tc-qual__state">
        {{ t('common.loading', { defaultValue: '加载中…' }) }}
      </div>

      <div v-else-if="loadError" class="tc-qual__state tc-qual__state--error">
        <p>{{ loadError }}</p>
        <el-button size="small" @click="loadList">
          {{ t('common.retry', { defaultValue: '重试' }) }}
        </el-button>
      </div>

      <div v-else-if="list.length === 0" class="tc-qual__state">
        {{ t('teacherCenter.qualification.list.empty') }}
      </div>

      <ul v-else class="tc-qual__list">
        <li
          v-for="row in list"
          :key="row.id"
          class="tc-qual__row"
        >
          <div class="tc-qual__row-main">
            <div class="tc-qual__row-line">
              <span class="tc-qual__row-doctype">{{ docTypeLabel(row.docType) }}</span>
              <el-tag
                size="small"
                :type="auditStatusTagType(row.auditStatus)"
                effect="light"
              >
                {{ auditStatusLabel(row.auditStatus) }}
              </el-tag>
            </div>
            <p
              class="tc-qual__row-filename"
              :title="row.docFilename || ''"
            >
              {{ row.docFilename || '-' }}
            </p>
            <p
              v-if="row.auditStatus === 'rejected' && row.rejectReason"
              class="tc-qual__row-reason"
            >
              {{ t('teacherCenter.qualification.list.rejectReasonPrefix') }}{{ row.rejectReason }}
            </p>
            <p v-if="row.createTime" class="tc-qual__row-time">
              {{ formatCreateTime(row.createTime) }}
            </p>
          </div>
          <div class="tc-qual__row-action">
            <el-button
              size="small"
              :icon="Delete"
              :loading="deletingId === row.id"
              :disabled="uploading"
              @click="handleDelete(row)"
            >
              {{ t('teacherCenter.qualification.list.delete') }}
            </el-button>
          </div>
        </li>
      </ul>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.tc-qual {
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

  &__required {
    margin: brand.$spacing-2 0 0;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }

  &__card {
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    padding: brand.$spacing-5;
    margin-block-end: brand.$spacing-5;

    @media (min-width: brand.$bp-tablet) {
      padding: brand.$spacing-6 brand.$spacing-8;
    }
  }

  &__card-title {
    margin: 0 0 brand.$spacing-4;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__upload {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-4;

    @media (min-width: brand.$bp-tablet) {
      flex-direction: row;
      align-items: flex-start;
    }
  }

  &__upload-field {
    flex: 1 1 auto;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__upload-action {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
    align-items: flex-start;

    @media (min-width: brand.$bp-tablet) {
      padding-block-start: 26px; // 对齐 select 行(label 高度补偿)
    }
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

  &__state {
    text-align: center;
    padding: brand.$spacing-8 brand.$spacing-4;
    color: brand.$color-text-secondary;

    &--error {
      color: brand.$color-error;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: brand.$spacing-3;
    }
  }

  &__list {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
  }

  &__row {
    display: flex;
    align-items: flex-start;
    gap: brand.$spacing-3;
    padding: brand.$spacing-4;
    background: brand.$color-bg-page;
    border-radius: brand.$radius-base;
    border: 1px solid brand.$color-border-light;

    @media (max-width: brand.$bp-tablet) {
      flex-direction: column;
      align-items: stretch;
    }
  }

  &__row-main {
    flex: 1 1 auto;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-1;
  }

  &__row-line {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
  }

  &__row-doctype {
    font-size: brand.$font-size-base;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__row-filename {
    margin: 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
    word-break: break-all;
  }

  &__row-reason {
    margin: 0;
    color: brand.$color-error;
    font-size: brand.$font-size-sm;
    word-break: break-word;
  }

  &__row-time {
    margin: 0;
    color: brand.$color-text-tertiary;
    font-size: brand.$font-size-xs;
  }

  &__row-action {
    flex: 0 0 auto;
    display: flex;
    align-items: center;

    @media (max-width: brand.$bp-tablet) {
      justify-content: flex-end;
    }
  }
}
</style>
