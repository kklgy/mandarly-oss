<script setup>
/**
 * IframeErrorOverlay — 课堂 iframe 加载失败浮层
 *
 * 设计源:DESIGN-mandarly-v1.md § ClassroomView § iframe 错误重连(第 10 轮 P2-2)
 *
 * 状态机(由父级 ClassroomView 驱动):
 *   props.networkOnline = false        → 顶部"网络已断开"红条
 *   props.autoRetrying = true          → 中央显示倒计时 "{seconds} 秒后第 {n} 次自动重试..."
 *   props.exhausted = true (≥3 次失败) → 显示客服文案 + 手动重连主按钮
 *
 * 不直接 reload iframe,emit 给父级:
 *   - manual-retry  父级走 reloadIframe(重置计数 + iframeKey++)
 *   - leave         父级走 confirm 离开链路
 */

import { useI18n } from 'vue-i18n'
import { ElButton, ElIcon } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  /** 浏览器在线状态(navigator.onLine) */
  networkOnline: { type: Boolean, default: true },
  /** 当前自动重试到第几次(0 起步,3 即用尽) */
  retryCount: { type: Number, default: 0 },
  /** 自动重试上限 */
  maxAutoRetry: { type: Number, default: 3 },
  /** 是否正在自动重试倒计时阶段 */
  autoRetrying: { type: Boolean, default: false },
  /** 自动重试倒计时剩余秒数 */
  retryCountdown: { type: Number, default: 0 },
  /** 已耗尽自动重试 */
  exhausted: { type: Boolean, default: false }
})

const emit = defineEmits(['manual-retry', 'leave'])

const { t } = useI18n()

function onManualRetry() {
  emit('manual-retry')
}

function onLeave() {
  emit('leave')
}
</script>

<template>
  <div class="cl-error" role="alert" aria-live="assertive">
    <div v-if="!networkOnline" class="cl-error__net-banner">
      {{ t('classroom.network.offlineTip') }}
    </div>

    <div class="cl-error__card">
      <el-icon class="cl-error__icon">
        <WarningFilled />
      </el-icon>

      <h2 class="cl-error__title">
        {{ t('classroom.error.loadFailed') }}
      </h2>

      <p class="cl-error__hint">
        {{ t('classroom.error.retryHint') }}
      </p>

      <p
        v-if="autoRetrying && !exhausted"
        class="cl-error__countdown"
      >
        {{
          t('classroom.error.autoRetry', {
            seconds: retryCountdown,
            n: retryCount + 1
          })
        }}
      </p>

      <p v-if="exhausted" class="cl-error__support">
        {{ t('classroom.error.contactSupport') }}
      </p>

      <div class="cl-error__actions">
        <el-button
          type="primary"
          size="large"
          class="cl-error__retry"
          @click="onManualRetry"
        >
          {{ t('classroom.error.manualRetry') }}
        </el-button>
        <el-button
          plain
          size="large"
          class="cl-error__leave"
          @click="onLeave"
        >
          {{ t('classroom.back') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.cl-error {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  /* 课堂 iframe 失败黑底是 LCIC 视觉协议(全屏黑底),用半透明黑配 brand surface 卡 */
  background: rgba(0, 0, 0, 0.85);
  padding: var(--spacing-6, 24px);
  /* iOS 底部 home indicator 安全区 */
  padding-bottom: calc(var(--spacing-6, 24px) + env(safe-area-inset-bottom, 0px));
  z-index: 10;

  &__net-banner {
    position: absolute;
    top: 0;
    inset-inline: 0;
    padding: 8px 16px;
    text-align: center;
    background: var(--color-error);
    color: var(--color-text-inverse);
    font-size: 13px;
    font-weight: 500;
  }

  &__card {
    width: 100%;
    max-width: 420px;
    padding: var(--spacing-8, 32px) var(--spacing-6, 24px);
    background: var(--color-bg-card);
    border-radius: var(--radius-xl, 16px);
    box-shadow: var(--shadow-lg);
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  &__icon {
    font-size: 56px;
    color: var(--color-warning);
    margin-bottom: var(--spacing-4, 16px);
  }

  &__title {
    margin: 0 0 var(--spacing-2, 8px);
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text-primary);
    line-height: 1.4;
  }

  &__hint {
    margin: 0 0 var(--spacing-4, 16px);
    font-size: 14px;
    color: var(--color-text-secondary);
    line-height: 1.6;
  }

  &__countdown {
    margin: 0 0 var(--spacing-4, 16px);
    font-size: 13px;
    color: var(--color-primary-deep);
    font-weight: 500;
    padding: 6px 12px;
    background: var(--color-primary-soft);
    border-radius: var(--radius-pill);
  }

  &__support {
    margin: 0 0 var(--spacing-4, 16px);
    font-size: 12px;
    color: var(--color-text-tertiary);
    line-height: 1.6;
    word-break: break-word;
  }

  &__actions {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-3, 12px);
    width: 100%;
    margin-top: var(--spacing-2, 8px);
  }

  &__retry,
  &__leave {
    width: 100%;
  }

  /* H5 紧凑卡 */
  @media (max-width: 480px) {
    &__card {
      padding: var(--spacing-6, 24px) var(--spacing-4, 16px);
    }

    &__icon {
      font-size: 48px;
    }

    &__title {
      font-size: 16px;
    }
  }
}
</style>
