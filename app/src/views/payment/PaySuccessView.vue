<script setup>
/**
 * PaySuccessView.vue — /pay/success — Stripe success_url 落地页
 *
 * 接收 paymentId query param → 每 2s 轮询 payment.status
 * paid  → toast 成功 + 跳 /my/packages
 * 最多 30 次(60s),超时显示"支付处理中"提示
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getPayment } from '@/api/payment'
import { useBookingCountsStore } from '@/stores/bookingCounts'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const MAX_POLLS = 30
const POLL_INTERVAL_MS = 2000

const status = ref('polling')   // polling | paid | timeout | failed
let timer = null
let pollCount = 0

const paymentId = ref(route.query.paymentId || route.query.payment_id || null)

onMounted(() => {
  if (!paymentId.value) {
    status.value = 'timeout'
    return
  }
  startPolling()
})

onUnmounted(() => {
  clearTimer()
})

function clearTimer() {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

async function poll() {
  if (pollCount >= MAX_POLLS) {
    status.value = 'timeout'
    return
  }
  pollCount++
  try {
    const payment = await getPayment(paymentId.value)
    if (payment.status === 'paid') {
      status.value = 'paid'
      ElMessage.success(t('payment.success.title'))
      // M5 Wave 4 plan §11.11:支付成功后强刷 booking counts(套餐已入账,跨 tab stale 兜底)
      try {
        useBookingCountsStore().fetch(true)
      } catch (e) {
        // 忽略 store 未就绪异常,不阻塞跳转
      }
      setTimeout(() => {
        router.push('/my/packages')
      }, 1200)
      return
    }
    if (payment.status === 'failed' || payment.status === 'expired') {
      status.value = 'failed'
      return
    }
    // pending → continue polling
    timer = setTimeout(poll, POLL_INTERVAL_MS)
  } catch {
    // 网络错误也继续轮询,不计入 MAX_POLLS
    timer = setTimeout(poll, POLL_INTERVAL_MS)
  }
}

function startPolling() {
  timer = setTimeout(poll, POLL_INTERVAL_MS)
}

function goMyPackages() {
  router.push('/my/packages')
}
</script>

<template>
  <div class="ps-page" :dir="$i18n.locale === 'ar' ? 'rtl' : 'ltr'">
    <div class="ps-card">
      <!-- 轮询中 -->
      <template v-if="status === 'polling'">
        <div class="ps-icon ps-icon--spin">⏳</div>
        <h1 class="ps-title">{{ t('payment.processing.title') }}</h1>
        <p class="ps-tip">{{ t('payment.processing.tip') }}</p>
      </template>

      <!-- 支付成功 -->
      <template v-else-if="status === 'paid'">
        <div class="ps-icon ps-icon--success">✓</div>
        <h1 class="ps-title ps-title--success">{{ t('payment.success.title') }}</h1>
        <p class="ps-tip">{{ t('payment.success.message') }}</p>
        <button class="ps-btn" type="button" @click="goMyPackages">
          {{ t('payment.viewMyPackages') }}
        </button>
      </template>

      <!-- 超时(处理中) -->
      <template v-else-if="status === 'timeout'">
        <div class="ps-icon">🕐</div>
        <h1 class="ps-title">{{ t('payment.expired.title') }}</h1>
        <p class="ps-tip">{{ t('payment.expired.message') }}</p>
        <button class="ps-btn" type="button" @click="goMyPackages">
          {{ t('payment.viewMyPackages') }}
        </button>
      </template>

      <!-- 支付失败 -->
      <template v-else-if="status === 'failed'">
        <div class="ps-icon ps-icon--failed">✕</div>
        <h1 class="ps-title ps-title--failed">{{ t('payment.status.failed') }}</h1>
        <p class="ps-tip">{{ t('payment.cancel.message') }}</p>
        <div class="ps-actions">
          <button class="ps-btn ps-btn--ghost" type="button" @click="router.push('/packages')">
            {{ t('payment.cancel.retry') }}
          </button>
          <button class="ps-btn" type="button" @click="goMyPackages">
            {{ t('payment.viewMyPackages') }}
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.ps-page {
  min-height: 100vh;
  background: brand.$color-bg-page;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: brand.$spacing-4;
}

.ps-card {
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

.ps-icon {
  font-size: 3rem;
  line-height: 1;

  &--spin {
    animation: spin 1.5s linear infinite;
  }
  &--success {
    width: 56px;
    height: 56px;
    background: brand.$color-success;
    color: brand.$color-text-inverse;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: brand.$font-size-xl;
    font-weight: 700;
  }
  &--failed {
    width: 56px;
    height: 56px;
    background: brand.$color-error;
    color: brand.$color-text-inverse;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: brand.$font-size-xl;
    font-weight: 700;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.ps-title {
  margin: 0;
  font-size: brand.$font-size-xl;
  font-weight: 700;
  color: brand.$color-text-primary;

  &--success { color: brand.$color-success; }
  &--failed  { color: brand.$color-error; }
}

.ps-tip {
  margin: 0;
  font-size: brand.$font-size-base;
  color: brand.$color-text-secondary;
  line-height: 1.6;
}

.ps-btn {
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
  min-width: 160px;

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

.ps-actions {
  display: flex;
  gap: brand.$spacing-3;
  flex-wrap: wrap;
  justify-content: center;
}
</style>
