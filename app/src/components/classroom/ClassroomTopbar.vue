<script setup>
/**
 * ClassroomTopbar — 课堂页顶部工具栏
 *
 * 设计源:DESIGN-mandarly-v1.md § ClassroomView 课堂页改造(第 10 轮 P2-2)
 *
 * 职责:
 *   - 左:← 返回(emit 'leave',不擅自跳转,父级走 confirm)
 *   - 中:课堂 #order_id · 对方姓名 · 课程时长(H5 紧凑显示 #order_id)
 *   - 右:⚡ 网络连接状态徽章 + 🔄 手动重连按钮
 *
 * 高度:PC 48px / H5 40px(plan §H 精简)
 * iOS safe-area:padding-top: env(safe-area-inset-top, 0px)
 *
 * 不做差异化跳转(由父级 ClassroomView 决定 student → /review、teacher → /orders)
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElIcon, ElTooltip } from 'element-plus'
import { ArrowLeft, RefreshRight, Connection, WarnTriangleFilled } from '@element-plus/icons-vue'
import { useBreakpoint } from '@/composables/useBreakpoint'

const props = defineProps({
  orderId: { type: [String, Number], required: true },
  /** 订单详情:含 teacherNickname / studentNickname / duration / scheduledAt / status */
  orderInfo: { type: Object, default: null },
  /** 当前用户角色:'student' | 'teacher' | null */
  userRole: { type: String, default: 'student' },
  /** 网络在线状态(navigator.onLine) */
  isOnline: { type: Boolean, default: true },
  /** iframe 状态:loading / ok / error */
  iframeStatus: { type: String, default: 'loading' }
})

const emit = defineEmits(['leave', 'refresh'])

const { t } = useI18n()
const { isPC } = useBreakpoint()

const counterpartName = computed(() => {
  if (!props.orderInfo) return ''
  // 学生视角看老师,老师视角看学生(后端 API 仅注入对方 nickname,字段名 teacherNickname / studentNickname)
  if (props.userRole === 'teacher') {
    return props.orderInfo.studentNickname || ''
  }
  return props.orderInfo.teacherNickname || ''
})

const subtitle = computed(() => {
  const name = counterpartName.value
  const duration = props.orderInfo?.duration || 0
  if (!name && !duration) return ''
  const key =
    props.userRole === 'teacher'
      ? 'classroom.subtitle.teacher'
      : 'classroom.subtitle.student'
  return t(key, { nickname: name || '—', duration: duration || 30 })
})

const networkBadgeClass = computed(() => {
  if (!props.isOnline) return 'cl-topbar__net cl-topbar__net--offline'
  if (props.iframeStatus === 'error')
    return 'cl-topbar__net cl-topbar__net--warning'
  return 'cl-topbar__net cl-topbar__net--online'
})

const networkBadgeText = computed(() => {
  if (!props.isOnline) return t('classroom.network.offline')
  if (props.iframeStatus === 'error') return t('classroom.error.loadFailed')
  return t('classroom.network.online')
})

function onLeaveClick() {
  emit('leave')
}

function onRefreshClick() {
  emit('refresh')
}
</script>

<template>
  <header class="cl-topbar" :class="{ 'cl-topbar--h5': !isPC }">
    <button
      class="cl-topbar__leave"
      type="button"
      :aria-label="t('classroom.back')"
      @click="onLeaveClick"
    >
      <el-icon class="cl-topbar__leave-icon">
        <ArrowLeft />
      </el-icon>
      <span v-if="isPC" class="cl-topbar__leave-text">
        {{ t('classroom.back') }}
      </span>
    </button>

    <div class="cl-topbar__title">
      <template v-if="isPC">
        <span class="cl-topbar__title-id">
          {{ t('classroom.title') }} #{{ orderId }}
        </span>
        <span v-if="subtitle" class="cl-topbar__title-sub">
          · {{ subtitle }}
        </span>
      </template>
      <template v-else>
        <span class="cl-topbar__title-id">#{{ orderId }}</span>
      </template>
    </div>

    <div class="cl-topbar__actions">
      <el-tooltip
        :content="!isOnline ? t('classroom.network.offlineTip') : networkBadgeText"
        placement="bottom"
        :disabled="isPC && isOnline && iframeStatus !== 'error'"
      >
        <span :class="networkBadgeClass">
          <el-icon class="cl-topbar__net-icon">
            <WarnTriangleFilled v-if="!isOnline || iframeStatus === 'error'" />
            <Connection v-else />
          </el-icon>
          <span v-if="isPC" class="cl-topbar__net-text">
            {{ networkBadgeText }}
          </span>
        </span>
      </el-tooltip>

      <button
        class="cl-topbar__refresh"
        type="button"
        :aria-label="t('classroom.refresh')"
        :title="t('classroom.refresh')"
        @click="onRefreshClick"
      >
        <el-icon><RefreshRight /></el-icon>
      </button>
    </div>
  </header>
</template>

<style lang="scss" scoped>
.cl-topbar {
  flex-shrink: 0;
  height: 48px;
  /* iOS notch / Dynamic Island 安全区 */
  padding-top: env(safe-area-inset-top, 0px);
  padding-inline-start: calc(var(--spacing-4, 16px) + env(safe-area-inset-left, 0px));
  padding-inline-end: calc(var(--spacing-4, 16px) + env(safe-area-inset-right, 0px));
  display: flex;
  align-items: center;
  gap: var(--spacing-3, 12px);
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-primary);
  box-sizing: content-box;

  &--h5 {
    height: 40px;
    gap: var(--spacing-2, 8px);
    padding-inline-start: calc(12px + env(safe-area-inset-left, 0px));
    padding-inline-end: calc(12px + env(safe-area-inset-right, 0px));
  }

  &__leave {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    background: transparent;
    border: none;
    color: var(--color-primary-deep);
    font: inherit;
    font-weight: 500;
    cursor: pointer;
    padding: 6px 8px;
    border-radius: var(--radius-base);
    transition: background 0.15s ease;

    &:hover,
    &:focus-visible {
      background: var(--color-primary-soft);
      outline: none;
    }
  }

  &__leave-icon {
    font-size: 18px;
  }

  &__leave-text {
    font-size: 14px;
  }

  &__title {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: baseline;
    gap: 6px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    color: var(--color-text-primary);
  }

  &__title-id {
    font-weight: 600;
    font-size: 14px;
  }

  &__title-sub {
    color: var(--color-text-secondary);
    font-size: 13px;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__actions {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: var(--spacing-2, 8px);
  }

  &__net {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: var(--radius-pill);
    font-size: 12px;
    font-weight: 500;
    line-height: 1;

    &--online {
      color: var(--color-success);
      background: rgba(82, 196, 26, 0.08);
    }

    &--offline {
      color: var(--color-error);
      background: rgba(255, 77, 79, 0.08);
    }

    &--warning {
      color: var(--color-warning);
      background: rgba(250, 173, 20, 0.1);
    }
  }

  &__net-icon {
    font-size: 14px;
  }

  &__net-text {
    white-space: nowrap;
  }

  &__refresh {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background: transparent;
    border: none;
    border-radius: var(--radius-full);
    color: var(--color-text-secondary);
    cursor: pointer;
    transition:
      background 0.15s ease,
      color 0.15s ease;

    &:hover,
    &:focus-visible {
      background: var(--color-primary-soft);
      color: var(--color-primary-deep);
      outline: none;
    }
  }

  /* H5 紧凑刷新按钮 */
  &--h5 .cl-topbar__refresh {
    width: 28px;
    height: 28px;
  }
}
</style>
