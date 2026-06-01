<script setup>
/**
 * PayCancelView.vue — /pay/cancel — Stripe cancel_url 落地页
 *
 * 提供友好文案 + 重试 / 返回套餐列表 双按钮
 */
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <div class="pc-page" :dir="$i18n.locale === 'ar' ? 'rtl' : 'ltr'">
    <div class="pc-card">
      <div class="pc-icon">✕</div>
      <h1 class="pc-title">{{ t('payment.cancel.title') }}</h1>
      <p class="pc-tip">{{ t('payment.cancel.message') }}</p>
      <div class="pc-actions">
        <button class="pc-btn pc-btn--ghost" type="button" @click="router.push('/packages')">
          {{ t('payment.cancel.retry') }}
        </button>
        <button class="pc-btn" type="button" @click="router.push('/my/packages')">
          {{ t('payment.cancel.back') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.pc-page {
  min-height: 100vh;
  background: brand.$color-bg-page;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: brand.$spacing-4;
}

.pc-card {
  max-width: 480px;
  width: 100%;
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-card;
  box-shadow: brand.$shadow-base;
  padding: brand.$spacing-10 brand.$spacing-8;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-4;

  @media (max-width: brand.$bp-tablet) {
    padding: brand.$spacing-8 brand.$spacing-4;
  }
}

.pc-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 77, 79, 0.10);
  color: brand.$color-error;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: brand.$font-size-xl;
  font-weight: 700;
}

.pc-title {
  margin: 0;
  font-size: brand.$font-size-xl;
  font-weight: 700;
  color: brand.$color-text-primary;
}

.pc-tip {
  margin: 0;
  font-size: brand.$font-size-base;
  color: brand.$color-text-secondary;
  line-height: 1.6;
}

.pc-actions {
  display: flex;
  gap: brand.$spacing-3;
  flex-wrap: wrap;
  justify-content: center;
}

.pc-btn {
  padding: brand.$spacing-3 brand.$spacing-8;
  background: brand.$brand-primary;
  color: brand.$color-text-inverse;
  border: 1.5px solid brand.$brand-primary;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-base;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  min-width: 140px;

  &:hover { background: brand.$brand-primary-deep; }

  &--ghost {
    background: transparent;
    color: brand.$brand-primary-deep;
    border-color: brand.$brand-primary;

    &:hover {
      background: brand.$brand-primary-soft;
    }
  }
}
</style>
