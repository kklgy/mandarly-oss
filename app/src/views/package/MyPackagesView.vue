<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import { listMyAllPackages } from '@/api/booking'
import { getMyRefunds } from '@/api/payment'
import { fromUTC, getUserTimezone } from '@/utils/datetime'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import RefundApplyModal from '@/views/profile/components/RefundApplyModal.vue'

const router = useRouter()
const { t, locale } = useI18n()

const TABS = ['all', 'active', 'expired', 'exhausted']
const activeTab = ref('all')

const allPackages = ref([])
const refunds = ref([])
const loading = ref(true)
const errorMsg = ref('')

const tz = computed(() => getUserTimezone())

const dateFmt = computed(() => {
  if (locale.value === 'en') return 'MMM D, YYYY'
  if (locale.value === 'ar') return 'D MMMM YYYY'
  return 'YYYY年M月D日'
})

const filteredPackages = computed(() => {
  if (activeTab.value === 'all') return allPackages.value
  return allPackages.value.filter((p) => p.status === activeTab.value)
})
const refundInFlightPaymentIds = computed(() => {
  return new Set(
    refunds.value
      .filter((r) => r.paymentId && ['pending', 'approved'].includes(r.status))
      .map((r) => r.paymentId)
  )
})

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [list, refundPage] = await Promise.all([
      listMyAllPackages(locale.value),
      getMyRefunds({ pageNo: 1, pageSize: 100 }).catch(() => ({ list: [] }))
    ])
    allPackages.value = list || []
    refunds.value = refundPage?.list || refundPage || []
  } catch (e) {
    errorMsg.value = e?.message || t('myPackages.loadFailed')
    allPackages.value = []
    refunds.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(utc) {
  if (!utc) return ''
  return fromUTC(utc, tz.value).format(dateFmt.value)
}

function statusBadgeClass(status) {
  return {
    'mp-badge': true,
    'mp-badge--active': status === 'active',
    'mp-badge--expired': status === 'expired',
    'mp-badge--exhausted': status === 'exhausted'
  }
}

function statusLabel(status) {
  return t(`myPackages.card.status${status[0].toUpperCase()}${status.slice(1)}`)
}

function sourceLabel(source) {
  // i18n 已配 key:purchase / free_trial / register_grant / admin_grant / referral_reward
  return t(`myPackages.source.${source || 'purchase'}`)
}

function isFreeSource(source) {
  return source === 'free_trial' || source === 'register_grant' || source === 'referral_reward'
}

// === Track 8B:退款申请 ===
const refundModal = ref(false)
const refundPaymentId = ref(null)

function openRefundModal(pkg) {
  refundPaymentId.value = pkg.paymentId || null
  if (!refundPaymentId.value) return
  if (refundInFlightPaymentIds.value.has(refundPaymentId.value)) return
  refundModal.value = true
}

function onRefundSuccess() {
  refundModal.value = false
  if (refundPaymentId.value) {
    refunds.value = [
      { id: `local-${refundPaymentId.value}`, paymentId: refundPaymentId.value, status: 'pending' },
      ...refunds.value
    ]
  }
  load()   // 刷新套餐状态
}
</script>

<template>
  <div class="mp-page">
    <div class="mp-topbar">
      <button class="mp-back" type="button" @click="router.push('/')">
        ← Mandarly
      </button>
      <LanguageSwitcher />
    </div>

    <header class="mp-hero">
      <h1 class="mp-title">{{ t('myPackages.title') }}</h1>
      <p class="mp-subtitle">{{ t('myPackages.subtitle') }}</p>
    </header>

    <nav class="mp-tabs" role="tablist">
      <button
        v-for="tab in TABS"
        :key="tab"
        type="button"
        role="tab"
        class="mp-tab"
        :class="{ 'mp-tab--active': activeTab === tab }"
        :aria-selected="activeTab === tab"
        @click="activeTab = tab"
      >
        {{ t(`myPackages.tabs.${tab}`) }}
      </button>
    </nav>

    <div v-if="loading" class="mp-state mp-state--loading" role="status" aria-live="polite">
      <el-skeleton v-for="idx in 3" :key="idx" animated class="mp-state__skeleton">
        <template #template>
          <el-skeleton-item variant="h3" style="width: 50%" />
          <el-skeleton-item variant="text" style="width: 90%" />
          <el-skeleton-item variant="text" style="width: 64%" />
        </template>
      </el-skeleton>
    </div>

    <EmptyState
      v-else-if="errorMsg"
      variant="error"
      :title="t('myPackages.loadFailed')"
      :description="errorMsg"
      compact
    >
      <template #action>
        <el-button type="primary" @click="load">{{ t('common.retry') }}</el-button>
      </template>
    </EmptyState>

    <EmptyState
      v-else-if="filteredPackages.length === 0"
      variant="no-data"
      :title="t(`myPackages.empty.${activeTab}`)"
      compact
    >
      <template v-if="activeTab === 'all' || activeTab === 'active'" #action>
        <el-button type="primary" @click="router.push('/teachers')">
          {{ t('myPackages.browseTeachers') }}
        </el-button>
      </template>
    </EmptyState>

    <ul v-else class="mp-list">
      <li
        v-for="pkg in filteredPackages"
        :key="pkg.id"
        class="mp-card"
        :class="{ 'mp-card--dim': pkg.status !== 'active' }"
      >
        <header class="mp-card__head">
          <div>
            <p class="mp-card__name">
              {{ pkg.name || `#${pkg.packageId}` }}
              <span v-if="pkg.isFreeTrial" class="mp-card__inline-badge">
                {{ t('myPackages.card.free') }}
              </span>
            </p>
            <p class="mp-card__source">{{ sourceLabel(pkg.source) }}</p>
          </div>
          <span :class="statusBadgeClass(pkg.status)">{{ statusLabel(pkg.status) }}</span>
        </header>

        <dl class="mp-card__rows">
          <div class="mp-card__row">
            <dt>{{ t('myPackages.card.remaining', { n: pkg.remaining }) }}</dt>
            <dd>{{ t('myPackages.card.total', { n: pkg.totalCount || 0 }) }}</dd>
          </div>
          <div v-if="pkg.weeklyCount" class="mp-card__row">
            <dt>{{ t('myPackages.card.weekly', { n: pkg.weeklyCount }) }}</dt>
            <dd>—</dd>
          </div>
          <div class="mp-card__row">
            <dt>{{ t('myPackages.card.expireAt', { date: formatDate(pkg.expireAt) }) }}</dt>
            <dd>{{ t('myPackages.card.purchasedAt', { date: formatDate(pkg.purchasedAt) }) }}</dd>
          </div>
          <div v-if="!isFreeSource(pkg.source) && pkg.price" class="mp-card__row">
            <dt>{{ pkg.currency }} {{ pkg.price }}</dt>
            <dd>—</dd>
          </div>
        </dl>

        <!-- Track 8B:申请退款(仅 active 套餐,且有 paymentId) -->
        <div v-if="pkg.status === 'active' && pkg.paymentId" class="mp-card__actions">
          <button
            class="mp-card__refund-btn"
            :class="{ 'is-disabled': refundInFlightPaymentIds.has(pkg.paymentId) }"
            type="button"
            :disabled="refundInFlightPaymentIds.has(pkg.paymentId)"
            @click="openRefundModal(pkg)"
          >
            {{ refundInFlightPaymentIds.has(pkg.paymentId) ? t('refund.apply.pendingButton') : t('refund.apply.button') }}
          </button>
        </div>
      </li>
    </ul>

    <!-- 退款申请弹窗 -->
    <RefundApplyModal
      v-if="refundModal && refundPaymentId"
      :payment-id="refundPaymentId"
      @success="onRefundSuccess"
      @close="refundModal = false"
    />
  </div>
</template>

<style lang="scss" scoped>
@use "sass:color";

.mp-page {
  min-height: 100vh;
  background: brand.$color-bg-page;
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8 brand.$spacing-6;
  }
}

.mp-topbar {
  max-width: 960px;
  margin: 0 auto brand.$spacing-6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mp-back {
  background: transparent;
  border: none;
  color: brand.$brand-primary-deep;
  font: inherit;
  font-family: brand.$font-family-heading-en;
  font-style: italic;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  cursor: pointer;
  &:hover { color: brand.$brand-primary; }
}

.mp-hero {
  max-width: 960px;
  margin: 0 auto brand.$spacing-6;
  text-align: center;
}

.mp-title {
  margin: 0 0 brand.$spacing-2;
  font-size: brand.$font-size-2xl;
  font-weight: 700;
  color: brand.$color-text-primary;
}

.mp-subtitle {
  margin: 0;
  font-size: brand.$font-size-base;
  color: brand.$color-text-secondary;
}

.mp-tabs {
  max-width: 960px;
  margin: 0 auto brand.$spacing-6;
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  gap: brand.$spacing-4;
  border-bottom: 1px solid brand.$color-border;
  padding-bottom: brand.$spacing-1;
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }
}

.mp-tab {
  flex-shrink: 0;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  padding: brand.$spacing-2 brand.$spacing-1;
  margin-bottom: -1px;
  color: brand.$color-text-secondary;
  font: inherit;
  font-size: brand.$font-size-base;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  &:hover { color: brand.$color-text-primary; }
  &--active {
    color: brand.$color-text-primary;
    border-bottom-color: brand.$color-text-primary;
    font-weight: 600;
  }
}

.mp-state {
  max-width: 960px;
  margin: 0 auto;
  width: 100%;

  &--loading {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
    padding: brand.$spacing-4;
  }

  &__skeleton {
    padding: brand.$spacing-5;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    background: var(--color-bg-card);
  }
}

.mp-empty {
  max-width: 720px;
  margin: 0 auto;
  background: brand.$brand-primary-soft;
  border-radius: brand.$radius-card;
  padding: brand.$spacing-12 brand.$spacing-6;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
  align-items: center;
}

.mp-empty__text {
  margin: 0;
  font-size: brand.$font-size-md;
  color: brand.$color-text-primary;
}

.mp-empty__cta {
  padding: brand.$spacing-3 brand.$spacing-6;
  background: brand.$brand-primary;
  color: brand.$color-text-inverse;
  border: 1.5px solid brand.$brand-primary;
  border-radius: brand.$radius-base;
  font: inherit;
  font-weight: 500;
  cursor: pointer;
  &:hover {
    background: brand.$brand-primary-deep;
    border-color: brand.$brand-primary-deep;
  }
}

.mp-list {
  max-width: 960px;
  margin: 0 auto;
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
}

.mp-card {
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-card;
  padding: brand.$spacing-4;
  box-shadow: brand.$shadow-base;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-3;
  transition: opacity 0.18s;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-6;
  }

  &--dim { opacity: 0.65; }
}

.mp-card__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: brand.$spacing-3;
}

.mp-card__name {
  margin: 0;
  font-size: brand.$font-size-md;
  font-weight: 600;
  color: brand.$color-text-primary;
}

.mp-card__source {
  margin: 2px 0 0;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}

.mp-card__inline-badge {
  margin-inline-start: brand.$spacing-2;
  font-size: brand.$font-size-xs;
  background: brand.$color-success;
  color: brand.$color-text-inverse;
  padding: 2px brand.$spacing-2;
  border-radius: brand.$radius-sm;
  font-weight: 500;
}

.mp-badge {
  flex-shrink: 0;
  padding: 2px brand.$spacing-2;
  border-radius: brand.$radius-sm;
  font-size: brand.$font-size-xs;
  font-weight: 600;
  white-space: nowrap;

  &--active {
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
  }
  &--expired {
    background: brand.$color-divider;
    color: brand.$color-text-tertiary;
  }
  &--exhausted {
    background: var(--color-error-soft);
    color: brand.$color-error;
  }
}

// Track 8B:退款按钮
.mp-card__actions {
  padding: brand.$spacing-3 brand.$spacing-4 brand.$spacing-4;
  border-top: 1px solid brand.$color-divider;
  margin-top: brand.$spacing-2;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-3 brand.$spacing-6 brand.$spacing-5;
  }
}

.mp-card__refund-btn {
  background: transparent;
  border: none;
  color: brand.$color-error;
  font: inherit;
  font-size: brand.$font-size-sm;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
  text-underline-offset: 2px;

  &:hover {
    color: color.adjust(brand.$color-error, $lightness: -10%);
  }

  &.is-disabled,
  &:disabled {
    color: brand.$color-text-tertiary;
    cursor: not-allowed;
    text-decoration: none;
  }
}

.mp-card__rows {
  margin: 0;
  display: flex;
  flex-direction: column;
}

.mp-card__row {
  display: flex;
  justify-content: space-between;
  gap: brand.$spacing-4;
  padding: brand.$spacing-2 0;
  border-top: 1px solid brand.$color-divider;
  font-size: brand.$font-size-sm;
  margin: 0;

  &:first-child { border-top: none; }

  dt {
    color: brand.$color-text-primary;
    font-weight: 500;
  }
  dd {
    margin: 0;
    color: brand.$color-text-tertiary;
  }
}
</style>
