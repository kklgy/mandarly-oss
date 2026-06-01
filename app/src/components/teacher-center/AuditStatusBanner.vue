<script setup>
/**
 * AuditStatusBanner — 教师资料审核状态横幅(D19 Phase B / spec §B8)
 *
 * 教师中心顶部 banner,根据 teacher_user.audit_status 显示:
 *   - draft    → 米色 brand,文案“资料尚未提交,请完善档案与资质后提交审核”
 *   - pending  → 黄底 warning,文案“资料已提交,等待管理员审核”
 *   - rejected → 红底 danger,文案带 reason + “去补充”按钮(跳 /teacher-center/profile-edit)
 *   - approved → 不渲染(收起)
 *   - null     → 不渲染(B6/B7 完工前的旧用户兜底)
 *
 * Props 设计为通用,B6 ProfileEditView / B7 QualificationView 都可以复用:
 *   :status / :reason / @action(可选,默认内部 router.push)
 *
 * RTL 注意:
 *   - 用 border-inline-start 替代 border-left,ar 模式自动镜像
 *   - margin-inline 代替 margin-left/right
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter, useRoute } from 'vue-router'
import { EditPen, Warning, CircleCloseFilled } from '@element-plus/icons-vue'

const props = defineProps({
  // 'draft' | 'pending' | 'rejected' | 'approved' | null
  status: { type: String, default: null },
  reason: { type: String, default: '' }
})
const emit = defineEmits(['action'])

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

const visible = computed(() => ['draft', 'pending', 'rejected'].includes(props.status))

const variant = computed(() => {
  if (props.status === 'rejected') return 'rejected'
  if (props.status === 'pending') return 'pending'
  return 'draft'
})

const icon = computed(() => {
  if (props.status === 'rejected') return CircleCloseFilled
  if (props.status === 'pending') return Warning
  return EditPen
})

const message = computed(() => {
  if (props.status === 'draft') {
    return t('teacherCenter.audit.banner.draft')
  }
  if (props.status === 'pending') {
    return t('teacherCenter.audit.banner.pending')
  }
  if (props.status === 'rejected') {
    return t('teacherCenter.audit.banner.rejected', { reason: props.reason || '-' })
  }
  return ''
})

const actionLabel = computed(() => (
  props.status === 'draft'
    ? t('teacherCenter.audit.banner.draftAction')
    : t('teacherCenter.audit.banner.action')
))

const goEdit = () => {
  emit('action')
  if (route.name !== 'TeacherCenterProfileEdit' && route.path !== '/teacher-center/profile-edit') {
    router.push('/teacher-center/profile-edit')
  }
}
</script>

<template>
  <div
    v-if="visible"
    class="audit-banner"
    :class="`audit-banner--${variant}`"
    role="status"
    aria-live="polite"
  >
    <el-icon class="audit-banner__icon" aria-hidden="true">
      <component :is="icon" />
    </el-icon>
    <span class="audit-banner__text">{{ message }}</span>
    <el-button
      v-if="status === 'draft' || status === 'rejected'"
      type="primary"
      size="small"
      class="audit-banner__action"
      @click="goEdit"
    >
      {{ actionLabel }}
    </el-button>
  </div>
</template>

<style lang="scss" scoped>
.audit-banner {
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;
  padding: brand.$spacing-3 brand.$spacing-4;
  margin-block-end: brand.$spacing-4;
  border-radius: brand.$radius-base;
  border-inline-start: 4px solid transparent;  // RTL 自动镜像
  background: brand.$color-bg-card;
  font-size: brand.$font-size-base;
  line-height: brand.$line-height-base;

  &--pending {
    background: brand.$color-warning-soft;          // token 化的 warning 8% 底
    border-inline-start-color: brand.$color-warning;
  }

  &--draft {
    background: brand.$brand-primary-soft;
    border-inline-start-color: brand.$brand-primary-deep;
  }

  &--rejected {
    background: brand.$color-error-soft;            // 现成 token(rgba(255,77,79,0.08))
    border-inline-start-color: brand.$color-error;
  }

  &__icon {
    flex: 0 0 auto;
    font-size: brand.$font-size-lg;
    line-height: 1;

    .audit-banner--pending & {
      color: brand.$color-warning;
    }
    .audit-banner--draft & {
      color: brand.$brand-primary-deep;
    }
    .audit-banner--rejected & {
      color: brand.$color-error;
    }
  }

  &__text {
    flex: 1 1 auto;
    color: brand.$color-text-primary;
    word-break: break-word;
  }

  &__action {
    flex: 0 0 auto;
  }
}
</style>
