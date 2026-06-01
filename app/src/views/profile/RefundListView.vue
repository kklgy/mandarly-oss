<script setup>
/**
 * RefundListView.vue — /my/refunds — 我的退款列表
 *
 * 展示学生提交的退款工单历史
 * status badge: pending 黄 / approved 蓝 / refunded 绿 / rejected 灰 / failed 红
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getMyRefunds } from '@/api/payment'
import { fromUTC, getUserTimezone } from '@/utils/datetime'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const router = useRouter()
const { t, locale } = useI18n()

const refunds = ref([])
const loading = ref(true)
const errorMsg = ref('')
const tz = fromUTC ? getUserTimezone() : 'UTC'

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await getMyRefunds({ pageNo: 1, pageSize: 50 })
    refunds.value = (res?.list || res || [])
  } catch (e) {
    errorMsg.value = e?.message || t('common.error')
  } finally {
    loading.value = false
  }
}

function formatDate(utc) {
  if (!utc) return '—'
  try {
    const d = new Date(utc)
    if (locale.value === 'en') {
      return d.toLocaleDateString('en', { year: 'numeric', month: 'short', day: 'numeric', timeZone: tz })
    }
    return d.toLocaleDateString('zh-HK', { year: 'numeric', month: 'long', day: 'numeric', timeZone: tz })
  } catch {
    return utc
  }
}

function statusBadgeClass(status) {
  return {
    'rl-badge': true,
    'rl-badge--pending':  status === 'pending',
    'rl-badge--approved': status === 'approved',
    'rl-badge--refunded': status === 'refunded',
    'rl-badge--rejected': status === 'rejected',
    'rl-badge--failed':   status === 'failed'
  }
}

function statusLabel(status) {
  return t(`refund.status.${status}`)
}

function fmtUsd(val) {
  if (val == null) return '—'
  return `USD ${Number(val).toFixed(2)}`
}
</script>

<template>
  <div class="rl-page" :dir="$i18n.locale === 'ar' ? 'rtl' : 'ltr'">
    <div class="rl-topbar">
      <button class="rl-back" type="button" @click="router.push('/my/packages')">
        ← {{ t('myPackages.title') }}
      </button>
      <LanguageSwitcher />
    </div>

    <header class="rl-hero">
      <h1 class="rl-title">{{ t('refund.myList.title') }}</h1>
    </header>

    <!-- 加载 -->
    <div v-if="loading" class="rl-state">{{ t('common.loading') }}</div>

    <!-- 错误 -->
    <div v-else-if="errorMsg" class="rl-state rl-state--error">
      <p>{{ errorMsg }}</p>
      <button class="rl-retry" type="button" @click="load">{{ t('common.retry') }}</button>
    </div>

    <!-- 空态 -->
    <div v-else-if="refunds.length === 0" class="rl-empty">
      <p>{{ t('refund.myList.empty') }}</p>
      <button class="rl-cta" type="button" @click="router.push('/my/packages')">
        {{ t('myPackages.title') }}
      </button>
    </div>

    <!-- 退款列表 -->
    <ul v-else class="rl-list">
      <li
        v-for="r in refunds"
        :key="r.id"
        class="rl-card"
      >
        <header class="rl-card__head">
          <span class="rl-card__id">#{{ r.id }}</span>
          <span :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</span>
        </header>

        <dl class="rl-card__rows">
          <div class="rl-card__row">
            <dt>{{ t('refund.apply.reason.label') }}</dt>
            <dd class="rl-card__reason" dir="auto">{{ r.applyReason }}</dd>
          </div>
          <div class="rl-card__row">
            <dt>{{ t('payment.amount.usd') }}</dt>
            <dd>{{ fmtUsd(r.suggestedAmountUsd) }}</dd>
          </div>
          <div v-if="r.finalAmountUsd != null" class="rl-card__row">
            <dt>{{ t('payment.status.refunded') }}</dt>
            <dd>{{ fmtUsd(r.finalAmountUsd) }}</dd>
          </div>
          <div v-if="r.auditNote" class="rl-card__row">
            <dt>{{ t('refund.auditNote.label') }}</dt>
            <dd dir="auto">{{ r.auditNote }}</dd>
          </div>
          <div v-if="r.refundedAt" class="rl-card__row">
            <dt>{{ t('payment.status.refunded') }}</dt>
            <dd>{{ formatDate(r.refundedAt) }}</dd>
          </div>
          <div class="rl-card__row">
            <dt>{{ t('myPackages.card.purchasedAt', { date: '' }).replace(': ', '') }}</dt>
            <dd>{{ formatDate(r.createTime) }}</dd>
          </div>
        </dl>
      </li>
    </ul>
  </div>
</template>

<style lang="scss" scoped>
.rl-page {
  min-height: 100vh;
  background: brand.$color-bg-page;
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8 brand.$spacing-6;
  }
}

.rl-topbar {
  max-width: 960px;
  margin: 0 auto brand.$spacing-6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rl-back {
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

.rl-hero {
  max-width: 960px;
  margin: 0 auto brand.$spacing-6;
  text-align: center;
}

.rl-title {
  margin: 0;
  font-size: brand.$font-size-2xl;
  font-weight: 700;
  color: brand.$color-text-primary;
}

.rl-state {
  max-width: 960px;
  margin: 0 auto;
  text-align: center;
  padding: brand.$spacing-12 brand.$spacing-4;
  color: brand.$color-text-secondary;
  &--error { color: brand.$color-error; }
}

.rl-retry {
  margin-top: brand.$spacing-3;
  padding: brand.$spacing-2 brand.$spacing-5;
  background: brand.$brand-primary;
  color: brand.$color-text-inverse;
  border: 1.5px solid brand.$brand-primary;
  border-radius: brand.$radius-base;
  cursor: pointer;
}

.rl-empty {
  max-width: 500px;
  margin: brand.$spacing-12 auto;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-4;
  color: brand.$color-text-secondary;
}

.rl-cta {
  padding: brand.$spacing-3 brand.$spacing-6;
  background: brand.$brand-primary;
  color: brand.$color-text-inverse;
  border: 1.5px solid brand.$brand-primary;
  border-radius: brand.$radius-base;
  font: inherit;
  font-weight: 500;
  cursor: pointer;
}

.rl-list {
  max-width: 960px;
  margin: 0 auto;
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
}

.rl-card {
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-card;
  padding: brand.$spacing-4;
  box-shadow: brand.$shadow-base;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-3;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-5 brand.$spacing-6;
  }

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  &__id {
    font-size: brand.$font-size-sm;
    color: brand.$color-text-tertiary;
    font-family: brand.$font-family-mono;
  }

  &__rows {
    margin: 0;
    display: flex;
    flex-direction: column;
  }

  &__row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: brand.$spacing-4;
    padding: brand.$spacing-2 0;
    border-top: 1px solid brand.$color-divider;
    font-size: brand.$font-size-sm;
    margin: 0;

    &:first-child { border-top: none; }

    dt {
      color: brand.$color-text-secondary;
      flex-shrink: 0;
    }
    dd {
      margin: 0;
      color: brand.$color-text-primary;
      text-align: end;
    }
  }

  &__reason {
    font-size: brand.$font-size-sm;
    color: brand.$color-text-primary;
    word-break: break-word;
  }
}

// status badge
.rl-badge {
  flex-shrink: 0;
  padding: 2px brand.$spacing-2;
  border-radius: brand.$radius-sm;
  font-size: brand.$font-size-xs;
  font-weight: 600;
  white-space: nowrap;

  &--pending  { background: brand.$brand-primary-soft; color: brand.$brand-primary-deep; }
  &--approved { background: rgba(64, 158, 255, 0.15); color: #409eff; }
  &--refunded { background: rgba(82, 196, 26, 0.12);  color: #52c41a; }
  &--rejected { background: brand.$color-divider;       color: brand.$color-text-tertiary; }
  &--failed   { background: rgba(255, 77, 79, 0.10);   color: brand.$color-error; }
}
</style>
