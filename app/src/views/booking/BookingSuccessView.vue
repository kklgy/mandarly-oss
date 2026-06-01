<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getOrder } from '@/api/booking'
import { fromUTC, getUserTimezone } from '@/utils/datetime'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()

const order = ref(null)
const loading = ref(true)
const errorMsg = ref('')

const orderId = computed(() => Number(route.params.id))

onMounted(async () => {
  try {
    order.value = await getOrder(orderId.value, locale.value)
  } catch (e) {
    errorMsg.value = e?.message || t('common.error')
  } finally {
    loading.value = false
  }
})

const scheduledDate = computed(() => {
  if (!order.value?.scheduledAt) return ''
  return fromUTC(order.value.scheduledAt, getUserTimezone()).format(longDateFmt(locale.value))
})

function longDateFmt(loc) {
  if (loc === 'en') return 'dddd, MMM D, YYYY'
  if (loc === 'ar') return 'dddd D MMMM YYYY'
  return 'YYYY年M月D日 dddd'
}

const scheduledTime = computed(() => {
  if (!order.value?.scheduledAt) return ''
  const start = fromUTC(order.value.scheduledAt, getUserTimezone())
  const end = start.add(order.value.duration || 30, 'minute')
  return `${start.format('HH:mm')} - ${end.format('HH:mm')}`
})
</script>

<template>
  <div class="bs-page">
    <div v-if="loading" class="bs-state">{{ t('common.loading') }}</div>
    <div v-else-if="errorMsg" class="bs-state bs-state--error">{{ errorMsg }}</div>

    <template v-else-if="order">
      <header class="bs-hero">
        <div class="bs-hero__check">✓</div>
        <h1 class="bs-hero__title">{{ t('booking.success.title') }}</h1>
        <p class="bs-hero__subtitle">{{ t('booking.success.subtitle') }}</p>
      </header>

      <section class="bs-card">
        <h2 class="bs-card__title">{{ t('booking.success.orderTitle') }}</h2>
        <dl class="bs-rows">
          <div class="bs-row">
            <dt>{{ t('booking.success.teacher') }}</dt>
            <dd>{{ order.teacherNickname || `#${order.teacherId}` }}</dd>
          </div>
          <div class="bs-row">
            <dt>{{ t('booking.success.scheduledAt') }}</dt>
            <dd>
              <div>{{ scheduledDate }}</div>
              <div class="bs-row__time">{{ scheduledTime }}</div>
              <div class="bs-row__tz">{{ t('booking.success.tzNote', { tz: getUserTimezone() }) }}</div>
            </dd>
          </div>
          <div class="bs-row">
            <dt>{{ t('booking.success.duration') }}</dt>
            <dd>{{ t('booking.success.minutes', { n: order.duration || 30 }) }}</dd>
          </div>
          <div class="bs-row">
            <dt>{{ t('booking.success.package') }}</dt>
            <dd>
              {{ order.packageName || t('booking.success.packageFallback') }}
              <span v-if="order.isFreeTrial" class="bs-row__badge">
                {{ t('booking.dialog.freeTrialBadge') }}
              </span>
            </dd>
          </div>
          <div class="bs-row">
            <dt>{{ t('booking.success.status') }}</dt>
            <dd class="bs-row__status">{{ t(`booking.status.${order.status}`) }}</dd>
          </div>
        </dl>

        <div class="bs-note">
          <span class="bs-note__icon">✉</span>
          <span class="bs-note__text">{{ t('booking.success.emailNote') }}</span>
        </div>
      </section>

      <footer class="bs-footer">
        <button class="bs-cta-secondary" type="button" @click="router.push('/')">
          {{ t('booking.success.backHome') }}
        </button>
        <button class="bs-cta-primary" type="button" @click="router.push('/orders')">
          {{ t('booking.success.viewOrders') }}
        </button>
      </footer>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.bs-page {
  min-height: 100vh;
  background: brand.$color-bg-page;
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-12 brand.$spacing-6;
  }
}

.bs-state {
  text-align: center;
  padding: brand.$spacing-12 brand.$spacing-4;
  color: brand.$color-text-secondary;

  &--error {
    color: brand.$color-error;
  }
}

.bs-hero {
  max-width: 640px;
  margin: 0 auto brand.$spacing-6;
  padding: brand.$spacing-8 brand.$spacing-4;
  background: brand.$brand-primary-soft;
  border-radius: brand.$radius-xl;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-3;
}

.bs-hero__check {
  width: 64px;
  height: 64px;
  border-radius: brand.$radius-full;
  background: brand.$color-success;
  color: brand.$color-text-inverse;
  font-size: brand.$font-size-3xl;
  font-weight: 700;
  display: grid;
  place-items: center;
  box-shadow: brand.$shadow-md;
}

.bs-hero__title {
  margin: 0;
  font-size: brand.$font-size-2xl;
  font-weight: 700;
  color: brand.$color-text-primary;
}

.bs-hero__subtitle {
  margin: 0;
  font-size: brand.$font-size-base;
  color: brand.$color-text-secondary;
}

.bs-card {
  max-width: 640px;
  margin: 0 auto brand.$spacing-6;
  background: brand.$color-bg-card;
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6 brand.$spacing-4;
  box-shadow: brand.$shadow-sm;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8;
  }
}

.bs-card__title {
  margin: 0;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  color: brand.$color-text-primary;
}

.bs-rows {
  margin: 0;
  display: flex;
  flex-direction: column;
}

.bs-row {
  display: flex;
  gap: brand.$spacing-4;
  padding: brand.$spacing-3 0;
  border-bottom: 1px solid brand.$color-divider;
  font-size: brand.$font-size-sm;
  margin: 0;

  &:last-child {
    border-bottom: none;
  }

  dt {
    flex-shrink: 0;
    width: 90px;
    color: brand.$color-text-tertiary;
  }

  dd {
    flex: 1;
    margin: 0;
    color: brand.$color-text-primary;
  }
}

.bs-row__time {
  margin-top: 2px;
  color: brand.$brand-primary-deep;
  font-weight: 600;
}

.bs-row__tz {
  margin-top: 2px;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}

.bs-row__badge {
  margin-inline-start: brand.$spacing-2;
  font-size: brand.$font-size-xs;
  background: brand.$color-success;
  color: brand.$color-text-inverse;
  padding: 2px brand.$spacing-2;
  border-radius: brand.$radius-sm;
  font-weight: 500;
}

.bs-row__status {
  color: brand.$color-success;
  font-weight: 600;
  text-transform: capitalize;
}

.bs-note {
  display: flex;
  gap: brand.$spacing-2;
  align-items: flex-start;
  background: brand.$color-bg-page;
  padding: brand.$spacing-3 brand.$spacing-4;
  border-radius: brand.$radius-base;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
}

.bs-note__icon {
  flex-shrink: 0;
  font-size: brand.$font-size-md;
  color: brand.$brand-primary-deep;
}

.bs-note__text {
  line-height: brand.$line-height-base;
}

.bs-footer {
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  gap: brand.$spacing-3;
  flex-direction: column;

  @media (min-width: brand.$bp-tablet) {
    flex-direction: row;
  }
}

.bs-cta-secondary {
  flex: 1;
  padding: brand.$spacing-3 brand.$spacing-4;
  background: brand.$color-bg-card;
  color: brand.$color-text-secondary;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-base;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;

  &:hover:not(:disabled) {
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
    border-color: brand.$brand-primary;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.bs-cta-primary {
  flex: 1;
  padding: brand.$spacing-3 brand.$spacing-4;
  background: brand.$brand-primary;
  color: brand.$color-text-inverse;
  border: 1.5px solid brand.$brand-primary;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-base;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s;

  &:hover {
    background: brand.$brand-primary-deep;
    border-color: brand.$brand-primary-deep;
    box-shadow: brand.$shadow-brand;
  }
}
</style>
