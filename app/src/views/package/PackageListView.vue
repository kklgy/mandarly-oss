<script setup>
/**
 * PackageListView.vue — /packages — public Pricing page
 *
 * 目标:
 *   - 未登录可访问页面与导航,价格区保持毛玻璃锁定
 *   - 登录后显示完整套餐价格并可发起 Stripe checkout
 *   - CTA 一期按登录态统一输出,不使用卡片静态营销文案
 *   - 删除旧大表格,改为国际化教育平台风格 pricing cards
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'
import { ElMessage } from 'element-plus'
import {
  Aim,
  Calendar,
  CircleCheck,
  Histogram,
  Lock,
  Medal,
  Star,
  TrendCharts,
  UserFilled
} from '@element-plus/icons-vue'
import { listPackages, getFreeTrialStatus } from '@/api/package'
import { getTeacher } from '@/api/teacher'
import { checkout } from '@/api/payment'
import { safeRedirect } from '@/utils/safeRedirect'
import { buildHead, jsonLd, SITE_ORIGIN, SITE_NAME } from '@/utils/seo'
import { useUserStore } from '@/stores/user'
import CurrencySwitcher, { inferDefaultCurrency } from '@/components/package/CurrencySwitcher.vue'
import PackagePurchaseConfirmDialog from '@/components/package/PackagePurchaseConfirmDialog.vue'

const router = useRouter()
const route = useRoute()
const { t, te, locale } = useI18n()
const userStore = useUserStore()

const tr = (key, fb, payload) => {
  if (te(key)) return t(key, payload || {})
  if (!payload) return fb
  return Object.entries(payload).reduce(
    (acc, [k, v]) => acc.replaceAll(`{${k}}`, v),
    fb
  )
}

const PLAN_KEYS = ['starter', 'standard', 'annualLite', 'premiumAnnual']

const currency = ref(inferDefaultCurrency(locale.value))
const packages = ref([])
const loading = ref(true)
const errorMsg = ref('')
const freeTrialClaimed = ref(false)
const teacherContext = ref(null)
const dialogVisible = ref(false)
const dialogPkg = ref(null)
const checkoutPending = ref(null)

const canViewPrices = computed(() => userStore.isLoggedIn)
const isTeacherEntry = computed(() => route.query.from === 'teacher' && route.query.teacherId)
const teacherReturnTarget = computed(() => {
  const fallback = route.query.teacherId ? `/teacher/${route.query.teacherId}` : '/teachers'
  return safeRedirect(route.query.redirect, fallback, router)
})
const teacherBannerName = computed(() => {
  return teacherContext.value?.nickname || teacherContext.value?.name || ''
})
const teacherBannerTitle = computed(() => {
  const name = teacherBannerName.value
  if (name) {
    return tr('packages.teacherEntry.titleWithName', 'Choose a package for {name}', { name })
  }
  return tr('packages.teacherEntry.title', 'Choose a package for this tutor')
})

const majorPackages = computed(() => {
  const list = packages.value
  const ranked = list.filter((p) => Number(p.showOnListPriority) > 0)
  if (ranked.length > 0) {
    return [...ranked].sort(
      (a, b) => Number(a.showOnListPriority) - Number(b.showOnListPriority)
    )
  }
  return list.slice(0, 4)
})

const pricingCards = computed(() => {
  return majorPackages.value.map((pkg, index) => {
    const key = PLAN_KEYS[index] || PLAN_KEYS[PLAN_KEYS.length - 1]
    return {
      pkg,
      index,
      key,
      featured: index === 1,
      bestValue: index === 3,
      badge: badgeLabel(index),
      title: tr(`packages.pricing.cards.${key}.name`, pkg.name || ''),
      ctaName: tr(`packages.pricing.cards.${key}.ctaName`, tr(`packages.pricing.cards.${key}.name`, pkg.name || '')),
      subtitle: tr(`packages.pricing.cards.${key}.subtitle`, ''),
      points: packagePoints(pkg, key)
    }
  })
})

const heroPills = computed(() => [
  { icon: UserFilled, label: tr('packages.pricing.pills.tutors', 'Native-speaking tutors') },
  { icon: Calendar, label: tr('packages.pricing.pills.scheduling', 'Flexible Scheduling') },
  { icon: Star, label: tr('packages.pricing.pills.personalized', 'Personalized Learning') },
  { icon: Lock, label: tr('packages.pricing.pills.secure', 'Safe, secure & trusted') }
])

const pricingSubtitle = computed(() => tr(
  'packages.pricing.subtitle',
  'Flexible learning plans for kids, beginners, and global learners.'
))

const lessonFormatParts = computed(() => ({
  prefix: tr('packages.pricing.lessonFormatParts.prefix', 'All plans include '),
  duration: tr('packages.pricing.lessonFormatParts.duration', '30-minute'),
  middle1: tr('packages.pricing.lessonFormatParts.middle1', ' '),
  oneOnOne: tr('packages.pricing.lessonFormatParts.oneOnOne', '1-on-1'),
  middle2: tr('packages.pricing.lessonFormatParts.middle2', ' '),
  private: tr('packages.pricing.lessonFormatParts.private', 'private'),
  suffix: tr('packages.pricing.lessonFormatParts.suffix', ' Mandarin lessons with native-speaking tutors.')
}))

const benefitItems = computed(() => [
  {
    icon: UserFilled,
    title: tr('packages.pricing.why.native.title', 'Native Tutors'),
    desc: tr('packages.pricing.why.native.desc', 'Carefully selected and certified Mandarin teachers.')
  },
  {
    icon: Calendar,
    title: tr('packages.pricing.why.schedule.title', 'Flexible Scheduling'),
    desc: tr('packages.pricing.why.schedule.desc', 'Book lessons anytime, across all time zones.')
  },
  {
    icon: Aim,
    title: tr('packages.pricing.why.personalized.title', 'Personalized Learning'),
    desc: tr('packages.pricing.why.personalized.desc', 'Lessons tailored to your goals and learning style.')
  },
  {
    icon: Histogram,
    title: tr('packages.pricing.why.progress.title', 'Structured Progress'),
    desc: tr('packages.pricing.why.progress.desc', 'Track improvement and achieve your goals step by step.')
  }
])

const testimonialItems = computed(() => [
  {
    name: tr('packages.pricing.testimonials.emily.name', 'Emily, California'),
    quote: tr('packages.pricing.testimonials.emily.quote', 'My daughter finally enjoys learning Chinese!')
  },
  {
    name: tr('packages.pricing.testimonials.kevin.name', 'Kevin, Singapore'),
    quote: tr('packages.pricing.testimonials.kevin.quote', 'Flexible, professional, and very engaging lessons.')
  },
  {
    name: tr('packages.pricing.testimonials.sophie.name', 'Sophie, London'),
    quote: tr('packages.pricing.testimonials.sophie.quote', 'Great teachers and a clear learning path. Highly recommend!')
  }
])

const hasUnavailableCurrency = computed(
  () => !loading.value && !errorMsg.value && packages.value.length === 0 && currency.value !== 'HKD'
)
const unavailableCurrencyLabel = computed(() => {
  if (currency.value === 'USD') return tr('packages.currency.usd', 'USD')
  if (currency.value === 'CNY') return tr('packages.currency.cny', 'CNY')
  return currency.value
})
const unavailableCurrencyMessage = computed(() => tr(
  'packages.empty.currencyUnavailable',
  'No {currency} packages are currently available. You can view HKD packages instead',
  { currency: unavailableCurrencyLabel.value }
))

const headPayload = computed(() => {
  const base = buildHead({
    title: tr('seo.packages.title', 'Pricing · Flexible Mandarin Lesson Plans · Mandarly'),
    description: tr(
      'seo.packages.description',
      'Choose flexible Mandarin lesson plans for kids, beginners, and global learners. Learn 1-on-1 with native tutors on Mandarly.'
    ),
    keywords: tr(
      'seo.packages.keywords',
      'Mandarin pricing, Chinese lesson packages, online Mandarin lessons, 1-on-1 Chinese tutoring'
    ),
    path: '/packages',
    locale: locale.value
  })
  return {
    ...base,
    script: [
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'OfferCatalog',
        name: tr('seo.packages.catalogName', 'Mandarly lesson plans'),
        url: `${SITE_ORIGIN}/packages`,
        itemListElement: majorPackages.value.map((pkg, index) => ({
          '@type': 'Offer',
          position: index + 1,
          name: pkg.name,
          priceCurrency: pkg.currency || currency.value,
          availability: 'https://schema.org/InStock',
          seller: {
            '@type': 'Organization',
            name: SITE_NAME
          }
        }))
      })
    ]
  }
})

useHead(headPayload)

onMounted(async () => {
  await Promise.all([loadPackages(), loadFreeTrialStatus(), loadTeacherContext()])
})

watch(locale, () => {
  currency.value = inferDefaultCurrency(locale.value)
  loadPackages()
})

async function loadTeacherContext() {
  if (!isTeacherEntry.value) return
  try {
    teacherContext.value = await getTeacher(route.query.teacherId)
  } catch {
    teacherContext.value = null
  }
}

async function loadPackages() {
  loading.value = true
  errorMsg.value = ''
  try {
    const list = await listPackages({
      currency: currency.value,
      locale: locale.value
    })
    packages.value = (list || [])
      .filter((p) => !p.isFreeTrial && p.isActive !== false)
      .map(normalizePackage)
  } catch (e) {
    errorMsg.value = e?.message || tr('packages.error.loadFailed', 'Failed to load packages, please retry')
  } finally {
    loading.value = false
  }
}

async function loadFreeTrialStatus() {
  if (!userStore.isLoggedIn) {
    freeTrialClaimed.value = false
    return
  }
  try {
    const data = await getFreeTrialStatus()
    freeTrialClaimed.value = !!data?.claimed
  } catch (e) {
    freeTrialClaimed.value = false
    if (import.meta.env.DEV) {
      console.debug('[free-trial-status] fetch failed', e)
    }
  }
}

function normalizePackage(p) {
  return {
    ...p,
    sessions: p.sessions ?? p.totalCount,
    periodDays: p.periodDays ?? p.validityDays,
    freeTrial: p.freeTrial ?? p.isFreeTrial
  }
}

function onCurrencyChange() {
  loadPackages()
}

function switchToHkd() {
  currency.value = 'HKD'
  try {
    localStorage.setItem('mandarly_pkg_currency', 'HKD')
    localStorage.setItem('mandarly_pkg_currency_manual', '1')
  } catch (e) {
    // ignore storage failures
  }
  loadPackages()
}

function badgeLabel(index) {
  if (index === 1) return tr('packages.pricing.badge.mostPopular', 'Most Popular')
  if (index === 3) return tr('packages.pricing.badge.bestValue', 'Best Value')
  return ''
}

function packagePoints(pkg, key) {
  const list = Array.isArray(pkg?.sellingPoints) ? pkg.sellingPoints.filter(Boolean) : []
  if (list.length > 0) return list.slice(0, 4)
  return [1, 2, 3, 4]
    .map((idx) => tr(`packages.pricing.cards.${key}.point${idx}`, ''))
    .filter(Boolean)
}

function priceLocked(pkg) {
  return !canViewPrices.value || pkg?.priceVisible === false
}

function formatMoney(amount, code) {
  const value = Number(amount)
  const symbol = code === 'USD' ? 'US$' : code === 'CNY' ? '¥' : code === 'HKD' ? 'HK$' : `${code || ''} `
  if (!Number.isFinite(value)) return symbol.trim()
  return `${symbol}${new Intl.NumberFormat(locale.value, {
    minimumFractionDigits: value % 1 === 0 ? 0 : 2,
    maximumFractionDigits: 2
  }).format(value)}`
}

function priceDisplay(pkg) {
  const currencyCode = pkg?.currency || currency.value || 'HKD'
  const amount = pkg?.pricePerSession ?? pkg?.price
  return formatMoney(amount, currencyCode)
}

function priceUnit(pkg) {
  if (pkg?.pricePerSession == null) return ''
  return tr('packages.pricing.perLesson', '/ lesson')
}

function planMeta(pkg) {
  const parts = []
  if (pkg?.sessions) {
    parts.push(tr('packages.pricing.lessonsCount', '{n} lessons', { n: pkg.sessions }))
  }
  const period = periodLabel(pkg)
  if (period) parts.push(period)
  return parts.join(' • ')
}

function periodLabel(pkg) {
  if (pkg?.periodMonths) {
    return tr('packages.pricing.durationMonths', '{n} months', { n: pkg.periodMonths })
  }
  if (pkg?.periodDays) {
    return tr('packages.pricing.durationDays', '{n} days', { n: pkg.periodDays })
  }
  return ''
}

function originalPriceLabel(pkg) {
  if (pkg?.originalPricePerSession != null) {
    return formatMoney(pkg.originalPricePerSession, pkg.currency || currency.value)
  }
  if (pkg?.originalPrice != null && pkg?.pricePerSession == null) {
    return formatMoney(pkg.originalPrice, pkg.currency || currency.value)
  }
  return ''
}

function pricingCtaLabel(card) {
  if (!userStore.isLoggedIn) {
    return tr('packages.pricing.ctaLoggedOut', 'Book Free Trial')
  }
  return tr('packages.pricing.ctaLoggedInPlan', 'Choose {plan}', {
    plan: card?.ctaName || tr('packages.pricing.ctaDefaultPlan', 'Plan')
  })
}

function openCardCta(card) {
  if (!userStore.isLoggedIn) {
    router.push('/register?redirect=/booking/free-trial')
    return
  }
  openConfirmDialog({ pkg: card.pkg })
}

function openConfirmDialog({ pkg }) {
  if (userStore.profile?.role === 'teacher') {
    ElMessage.warning(tr('booking.teacherBlocked.tooltip', 'Teacher accounts cannot buy lessons. Please switch to a student account.'))
    return
  }
  if (!userStore.isLoggedIn) {
    const target = route.fullPath || '/packages'
    router.push(`/login?redirect=${encodeURIComponent(target)}`)
    return
  }
  dialogPkg.value = pkg
  dialogVisible.value = true
}

async function handleConfirm({ pkg, currency: cur }) {
  if (!pkg || checkoutPending.value) return
  checkoutPending.value = pkg.id
  try {
    const data = await checkout({ packageId: pkg.id, currency: pkg.currency || cur })
    if (data?.discountAmountUsd && Number(data.discountAmountUsd) > 0) {
      ElMessage.success(tr('referral.discount.applied', 'Referral discount applied'))
    }
    if (data?.checkoutUrl) {
      window.location.href = data.checkoutUrl
    } else {
      throw new Error('missing checkoutUrl')
    }
  } catch (e) {
    ElMessage.error(e?.message || tr('packages.error.checkoutFailed', 'Failed to start payment, please retry'))
    checkoutPending.value = null
    dialogVisible.value = false
  }
}
</script>

<template>
  <main class="pkg-page" :dir="$i18n.locale === 'ar' ? 'rtl' : 'ltr'">
    <section class="pkg-hero">
      <div class="pkg-hero__eyebrow">
        <el-icon><Medal /></el-icon>
        <span>{{ tr('packages.pricing.eyebrow', 'Flexible Plans for Every Learner') }}</span>
      </div>

      <h1 class="pkg-hero__title">
        <span>{{ tr('packages.pricing.titlePrefix', 'Choose the Right Plan for Your') }}</span>
        {{ ' ' }}
        <strong>{{ tr('packages.pricing.titleHighlight', 'Chinese') }}</strong>
        {{ ' ' }}
        <span>{{ tr('packages.pricing.titleSuffix', 'Learning Goals') }}</span>
      </h1>

      <p v-if="pricingSubtitle" class="pkg-hero__subtitle">
        {{ pricingSubtitle }}
      </p>
      <p class="pkg-hero__lesson-format">
        <span>{{ lessonFormatParts.prefix }}</span>
        <strong>{{ lessonFormatParts.duration }}</strong>
        <span>{{ lessonFormatParts.middle1 }}</span>
        <strong>{{ lessonFormatParts.oneOnOne }}</strong>
        <span>{{ lessonFormatParts.middle2 }}</span>
        <strong>{{ lessonFormatParts.private }}</strong>
        <span>{{ lessonFormatParts.suffix }}</span>
      </p>

      <div class="pkg-hero__tools">
        <div class="pkg-hero__pills" aria-label="Plan benefits">
          <span
            v-for="pill in heroPills"
            :key="pill.label"
            class="pkg-hero__pill"
          >
            <el-icon><component :is="pill.icon" /></el-icon>
            {{ pill.label }}
          </span>
        </div>

        <CurrencySwitcher
          v-if="canViewPrices"
          v-model="currency"
          class="pkg-hero__currency"
          @change="onCurrencyChange"
        />
      </div>
    </section>

    <section
      v-if="isTeacherEntry"
      class="pkg-teacher-banner"
      aria-live="polite"
    >
      <div class="pkg-teacher-banner__copy">
        <p class="pkg-teacher-banner__eyebrow">
          {{ tr('packages.teacherEntry.eyebrow', 'Continue booking') }}
        </p>
        <h2 class="pkg-teacher-banner__title">{{ teacherBannerTitle }}</h2>
        <p class="pkg-teacher-banner__desc">
          {{ tr('packages.teacherEntry.desc', 'After buying lessons, return to the tutor detail page and finish the 1-on-1 Mandarin booking you started.') }}
        </p>
      </div>
      <router-link
        class="pkg-teacher-banner__link"
        :to="teacherReturnTarget"
      >
        {{ tr('packages.teacherEntry.backToTeacher', 'Back to tutor') }}
      </router-link>
    </section>

    <section v-if="loading" class="pkg-state pkg-state--loading" role="status" aria-live="polite">
      <el-skeleton
        v-for="idx in 4"
        :key="idx"
        animated
        class="pkg-state__skeleton"
      >
        <template #template>
          <el-skeleton-item variant="h3" style="width: 62%" />
          <el-skeleton-item variant="text" style="width: 78%" />
          <el-skeleton-item variant="text" style="width: 48%" />
          <el-skeleton-item variant="button" style="width: 100%" />
        </template>
      </el-skeleton>
    </section>

    <EmptyState
      v-else-if="errorMsg"
      variant="error"
      :title="tr('packages.error.loadFailed', 'Failed to load packages, please retry')"
      :description="errorMsg"
    >
      <template #action>
        <el-button type="primary" @click="loadPackages">
          {{ tr('common.retry', 'Retry') }}
        </el-button>
      </template>
    </EmptyState>

    <template v-else>
      <section v-if="pricingCards.length > 0" class="pkg-pricing" aria-label="Pricing plans">
        <article
          v-for="card in pricingCards"
          :key="card.pkg.id"
          class="pkg-card"
          :class="{
            'pkg-card--featured': card.featured,
            'pkg-card--value': card.bestValue,
            'is-locked': priceLocked(card.pkg)
          }"
        >
          <span
            v-if="card.badge"
            class="pkg-card__badge"
            :class="card.bestValue ? 'pkg-card__badge--value' : 'pkg-card__badge--popular'"
          >
            <el-icon><Star /></el-icon>
            {{ card.badge }}
          </span>

          <header class="pkg-card__header">
            <h2 class="pkg-card__name">{{ card.title }}</h2>
            <p class="pkg-card__desc">{{ card.subtitle }}</p>
          </header>

          <div class="pkg-card__price" :class="{ 'pkg-card__price--locked': priceLocked(card.pkg) }">
            <button
              v-if="priceLocked(card.pkg)"
              type="button"
              class="pkg-card__lock"
              @click="openConfirmDialog({ pkg: card.pkg })"
            >
              <el-icon><Lock /></el-icon>
              <span>{{ tr('packages.pricing.priceLocked', 'Log in to view price') }}</span>
            </button>

            <template v-else>
              <div class="pkg-card__amount">
                <span>{{ priceDisplay(card.pkg) }}</span>
                <small v-if="priceUnit(card.pkg)">{{ priceUnit(card.pkg) }}</small>
              </div>
              <div
                v-if="originalPriceLabel(card.pkg) || card.pkg.discountLabel"
                class="pkg-card__saving"
              >
                <del v-if="originalPriceLabel(card.pkg)">{{ originalPriceLabel(card.pkg) }}</del>
                <span v-if="card.pkg.discountLabel">{{ card.pkg.discountLabel }}</span>
              </div>
            </template>
          </div>

          <p v-if="planMeta(card.pkg)" class="pkg-card__meta">
            {{ planMeta(card.pkg) }}
          </p>

          <ul class="pkg-card__points">
            <li
              v-for="point in card.points"
              :key="point"
            >
              <el-icon><CircleCheck /></el-icon>
              <span>{{ point }}</span>
            </li>
          </ul>

          <button
            class="pkg-card__cta"
            :class="{ 'pkg-card__cta--primary': card.featured || card.bestValue }"
            type="button"
            :disabled="checkoutPending === card.pkg.id"
            @click="openCardCta(card)"
          >
            <span v-if="checkoutPending === card.pkg.id">
              {{ tr('packageCard.cta.loading', tr('common.loading', 'Loading...')) }}
            </span>
            <span v-else>{{ pricingCtaLabel(card) }}</span>
          </button>
        </article>
      </section>

      <div v-if="packages.length === 0" class="pkg-empty">
        <p v-if="hasUnavailableCurrency">
          {{ unavailableCurrencyMessage }}
        </p>
        <p v-else>{{ tr('packages.empty.title', 'No packages available, please contact support') }}</p>
        <el-button
          v-if="hasUnavailableCurrency"
          type="primary"
          @click="switchToHkd"
        >
          {{ tr('packages.empty.viewHkd', 'View HKD packages') }}
        </el-button>
      </div>

      <section v-if="packages.length > 0" class="pkg-benefits">
        <h2 class="pkg-benefits__title">
          <span>{{ tr('packages.pricing.why.titlePrefix', 'Why Choose') }}</span>
          <strong>{{ tr('packages.pricing.why.titleHighlight', 'Mandarly?') }}</strong>
        </h2>

        <div class="pkg-benefits__grid">
          <article
            v-for="item in benefitItems"
            :key="item.title"
            class="pkg-benefit"
          >
            <span class="pkg-benefit__icon">
              <el-icon><component :is="item.icon" /></el-icon>
            </span>
            <div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </div>
          </article>
        </div>
      </section>

      <section v-if="packages.length > 0" class="pkg-testimonials">
        <h2>{{ tr('packages.pricing.testimonials.title', 'Loved by Learners Worldwide') }}</h2>
        <div class="pkg-testimonials__stars" aria-hidden="true">★★★★★</div>
        <div class="pkg-testimonials__grid">
          <article
            v-for="item in testimonialItems"
            :key="item.name"
            class="pkg-testimonial"
          >
            <div class="pkg-testimonial__avatar">
              {{ item.name.slice(0, 1) }}
            </div>
            <div>
              <p>“{{ item.quote }}”</p>
              <span>{{ item.name }}</span>
            </div>
          </article>
        </div>
      </section>

      <section v-if="packages.length > 0" class="pkg-rules">
        <div class="pkg-rules__head">
          <TrendCharts />
          <h2>{{ tr('packages.rules.title', 'Package rules') }}</h2>
        </div>
        <ul class="pkg-rules__list">
          <li>{{ tr('packages.rules.multiPackage', 'You may hold multiple packages; bookings consume the earliest-expiring lessons first') }}</li>
          <li>{{ tr('packages.rules.expiry', 'Unused lessons expire at the end of the validity period') }}</li>
          <li>{{ tr('packages.rules.refund', 'Cancellations 24h+ before lesson are fully refunded; see Terms § 5') }}</li>
          <li>{{ tr('packages.rules.currency', 'Packages are charged in their listed currency') }}</li>
        </ul>
      </section>
    </template>

    <PackagePurchaseConfirmDialog
      v-model:visible="dialogVisible"
      :pkg="dialogPkg"
      :currency="currency"
      :loading="checkoutPending != null"
      @confirm="handleConfirm"
    />
  </main>
</template>

<style lang="scss" scoped>
.pkg-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 50% 0%, rgba(255, 182, 39, 0.16), rgba(255, 255, 255, 0) 38%),
    linear-gradient(180deg, rgba(255, 246, 232, 0.82) 0%, var(--color-bg-page) 42%, var(--color-bg-page) 100%);
  padding: brand.$spacing-8 brand.$spacing-4 brand.$spacing-16;

  @media (min-width: brand.$bp-tablet) {
    padding-block-start: brand.$spacing-10;
    padding-inline: brand.$spacing-6;
  }
}

.pkg-hero {
  max-width: 1120px;
  margin: 0 auto brand.$spacing-8;
  text-align: center;

  &__eyebrow {
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-2;
    min-height: 28px;
    padding-inline: brand.$spacing-4;
    border-radius: brand.$radius-full;
    background: rgba(255, 182, 39, 0.16);
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-xs;
    font-weight: 700;
  }

  &__title {
    margin: brand.$spacing-4 0 brand.$spacing-2;
    color: brand.$color-text-primary;
    font-family: brand.$font-family-heading-en;
    font-size: 34px;
    font-weight: 800;
    line-height: 1.08;
    letter-spacing: 0;

    @media (min-width: brand.$bp-tablet) {
      font-size: 48px;
    }

    @media (min-width: brand.$bp-desktop) {
      font-size: 58px;
    }

    strong {
      display: inline-block;
      margin-inline: 0.16em;
      color: brand.$brand-primary-deep;
      font-style: italic;
    }
  }

  &__subtitle {
    max-width: 720px;
    margin: 0 auto;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-md;
    line-height: brand.$line-height-base;
  }

  &__lesson-format {
    max-width: 760px;
    margin: brand.$spacing-2 auto 0;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    font-weight: 700;
    line-height: brand.$line-height-base;

    strong {
      color: brand.$brand-primary-deep;
      font-weight: 900;
    }
  }

  &__tools {
    margin-block-start: brand.$spacing-5;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: brand.$spacing-3;
  }

  &__pills {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: brand.$spacing-3;
  }

  &__pill {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: brand.$spacing-2;
    min-height: 44px;
    min-width: 196px;
    padding-inline: brand.$spacing-4;
    border: 1px solid rgba(15, 23, 42, 0.08);
    border-radius: brand.$radius-full;
    background: rgba(255, 255, 255, 0.76);
    box-shadow: brand.$shadow-sm;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    font-weight: 700;
    backdrop-filter: blur(12px);

    .el-icon {
      color: brand.$brand-primary-deep;
      font-size: 18px;
    }
  }

  &__currency {
    margin-block-start: brand.$spacing-1;
  }
}

.pkg-teacher-banner,
.pkg-benefits,
.pkg-rules {
  max-width: 1120px;
  margin-inline: auto;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: brand.$radius-lg;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: brand.$shadow-sm;
  backdrop-filter: blur(14px);
}

.pkg-teacher-banner {
  margin-block-end: brand.$spacing-6;
  padding: brand.$spacing-5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: brand.$spacing-4;

  @media (max-width: brand.$bp-tablet) {
    align-items: stretch;
    flex-direction: column;
  }

  &__eyebrow {
    margin: 0 0 brand.$spacing-1;
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-sm;
    font-weight: 700;
  }

  &__title {
    margin: 0;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-xl;
    font-weight: 800;
    line-height: brand.$line-height-tight;
  }

  &__desc {
    margin: brand.$spacing-2 0 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-base;
    line-height: brand.$line-height-base;
  }

  &__link {
    flex: 0 0 auto;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 42px;
    padding-inline: brand.$spacing-5;
    border-radius: brand.$radius-base;
    background: brand.$brand-primary;
    color: brand.$btn-primary-text;
    text-decoration: none;
    font-size: brand.$font-size-sm;
    font-weight: 700;

    &:hover {
      background: brand.$brand-primary-deep;
    }
  }
}

.pkg-state {
  width: 100%;
  max-width: 1120px;
  margin: brand.$spacing-8 auto;

  &--loading {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: brand.$spacing-4;

    @media (max-width: brand.$bp-laptop) {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    @media (max-width: brand.$bp-tablet) {
      grid-template-columns: 1fr;
    }
  }

  &__skeleton {
    min-height: 300px;
    padding: brand.$spacing-6;
    border: 1px solid rgba(15, 23, 42, 0.08);
    border-radius: brand.$radius-lg;
    background: rgba(255, 255, 255, 0.78);
  }
}

.pkg-pricing {
  max-width: 1120px;
  margin: 0 auto brand.$spacing-6;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: brand.$spacing-4;

  @media (max-width: brand.$bp-desktop) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  @media (max-width: brand.$bp-tablet) {
    grid-template-columns: 1fr;
  }
}

.pkg-card {
  position: relative;
  min-height: 360px;
  padding: brand.$spacing-8 brand.$spacing-5 brand.$spacing-5;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: brand.$radius-lg;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: brand.$shadow-sm;
  backdrop-filter: blur(14px);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: brand.$shadow-card-hover;
  }

  &--featured {
    border-color: rgba(255, 103, 31, 0.36);
    box-shadow: 0 20px 46px rgba(255, 103, 31, 0.16);
  }

  &--value {
    border-color: rgba(255, 182, 39, 0.7);
    box-shadow: 0 20px 46px rgba(255, 182, 39, 0.18);
  }

  &__badge {
    position: absolute;
    top: -10px;
    inset-inline-end: brand.$spacing-5;
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-1;
    min-height: 28px;
    padding-inline: brand.$spacing-3;
    border-radius: brand.$radius-base;
    color: var(--color-bg-card);
    font-size: brand.$font-size-xs;
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 0;
    box-shadow: brand.$shadow-sm;
  }

  &__badge--popular {
    background: brand.$brand-primary-deep;
  }

  &__badge--value {
    background: brand.$brand-primary;
    color: brand.$color-text-primary;
  }

  &__header {
    min-height: 64px;
  }

  &__name {
    margin: 0 0 brand.$spacing-1;
    color: brand.$color-text-primary;
    font-family: brand.$font-family-heading-en;
    font-size: brand.$font-size-xl;
    line-height: brand.$line-height-tight;
    font-weight: 800;
  }

  &__desc {
    margin: 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }

  &__price {
    min-height: 68px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: brand.$spacing-2;
  }

  &__amount {
    display: flex;
    align-items: baseline;
    flex-wrap: wrap;
    gap: brand.$spacing-1;
    color: brand.$brand-primary-deep;

    span {
      font-family: brand.$font-family-heading-en;
      font-size: 36px;
      font-weight: 900;
      line-height: 1;
    }

    small {
      color: brand.$color-text-secondary;
      font-size: brand.$font-size-sm;
      font-weight: 600;
    }
  }

  &__saving {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-xs;

    span {
      padding: 2px brand.$spacing-2;
      border-radius: brand.$radius-full;
      background: rgba(255, 103, 31, 0.12);
      color: brand.$brand-primary-deep;
      font-weight: 700;
    }
  }

  &__lock {
    width: 100%;
    min-height: 58px;
    border: 1px solid rgba(15, 23, 42, 0.08);
    border-radius: brand.$radius-base;
    background: rgba(255, 255, 255, 0.48);
    color: brand.$color-text-secondary;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: brand.$spacing-2;
    font-size: brand.$font-size-sm;
    font-weight: 800;
    cursor: pointer;
    backdrop-filter: blur(12px);

    &:hover {
      border-color: brand.$brand-primary;
      color: brand.$brand-primary-deep;
    }
  }

  &.is-locked {
    .pkg-card__meta,
    .pkg-card__points {
      filter: blur(2.2px);
      opacity: 0.62;
      user-select: none;
      pointer-events: none;
    }
  }

  &__meta {
    margin: 0;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    font-weight: 700;
  }

  &__points {
    list-style: none;
    margin: 0;
    padding: 0;
    display: grid;
    gap: brand.$spacing-3;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;

    li {
      display: flex;
      gap: brand.$spacing-2;
      align-items: flex-start;
    }

    .el-icon {
      flex: 0 0 auto;
      margin-block-start: 2px;
      color: brand.$brand-primary-deep;
      font-size: 15px;
    }
  }

  &__cta {
    width: 100%;
    min-height: 44px;
    margin-block-start: auto;
    border: 1px solid brand.$brand-primary-deep;
    border-radius: brand.$radius-base;
    background: var(--color-bg-card);
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-sm;
    font-weight: 800;
    cursor: pointer;
    transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;

    &:hover {
      background: rgba(255, 103, 31, 0.08);
      transform: translateY(-1px);
    }

    &:disabled {
      opacity: 0.65;
      cursor: wait;
    }
  }

  &__cta--primary {
    border-color: transparent;
    background: brand.$brand-primary-deep;
    color: var(--color-bg-card);

    &:hover {
      background: brand.$brand-primary;
      color: brand.$color-text-primary;
    }
  }
}

.pkg-empty {
  max-width: 720px;
  margin: brand.$spacing-10 auto;
  padding: brand.$spacing-8;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: brand.$radius-lg;
  background: rgba(255, 255, 255, 0.84);
  text-align: center;
  color: brand.$color-text-secondary;
}

.pkg-benefits {
  margin-block: brand.$spacing-6;
  padding: brand.$spacing-6;
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr);
  gap: brand.$spacing-6;

  @media (max-width: brand.$bp-laptop) {
    grid-template-columns: 1fr;
  }

  &__title {
    margin: 0;
    align-self: center;
    color: brand.$color-text-primary;
    font-family: brand.$font-family-heading-en;
    font-size: brand.$font-size-2xl;
    line-height: brand.$line-height-tight;
    font-weight: 800;

    strong {
      display: block;
      color: brand.$brand-primary-deep;
      font-style: italic;
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: brand.$spacing-4;

    @media (max-width: brand.$bp-desktop) {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    @media (max-width: brand.$bp-tablet) {
      grid-template-columns: 1fr;
    }
  }
}

.pkg-benefit {
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr);
  gap: brand.$spacing-3;
  align-items: center;

  &__icon {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 103, 31, 0.1);
    color: brand.$brand-primary-deep;
    font-size: 26px;
  }

  h3 {
    margin: 0 0 brand.$spacing-1;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-tight;
    font-weight: 800;
  }

  p {
    margin: 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-xs;
    line-height: brand.$line-height-base;
  }
}

.pkg-testimonials {
  max-width: 980px;
  margin: brand.$spacing-8 auto 0;
  text-align: center;

  h2 {
    margin: 0;
    color: brand.$color-text-primary;
    font-family: brand.$font-family-heading-en;
    font-size: brand.$font-size-xl;
    font-weight: 800;
  }

  &__stars {
    margin-block: brand.$spacing-2 brand.$spacing-4;
    color: brand.$brand-primary;
    letter-spacing: 0;
    font-size: brand.$font-size-md;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: brand.$spacing-4;

    @media (max-width: brand.$bp-tablet) {
      grid-template-columns: 1fr;
    }
  }
}

.pkg-testimonial {
  min-height: 94px;
  padding: brand.$spacing-4;
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: brand.$spacing-3;
  align-items: center;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: brand.$radius-lg;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: brand.$shadow-sm;
  text-align: start;

  &__avatar {
    width: 52px;
    height: 52px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
    font-weight: 900;
  }

  p {
    margin: 0 0 brand.$spacing-1;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }

  span {
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-xs;
    font-weight: 700;
  }
}

.pkg-rules {
  margin-block-start: brand.$spacing-8;
  padding: brand.$spacing-5 brand.$spacing-6;

  &__head {
    display: flex;
    align-items: center;
    gap: brand.$spacing-2;
    color: brand.$brand-primary-deep;

    svg {
      width: 20px;
      height: 20px;
    }

    h2 {
      margin: 0;
      color: brand.$color-text-primary;
      font-size: brand.$font-size-lg;
      font-weight: 800;
    }
  }

  &__list {
    margin: brand.$spacing-3 0 0;
    padding-inline-start: brand.$spacing-5;
    color: brand.$color-text-secondary;
    display: grid;
    gap: brand.$spacing-2;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }
}

@media (max-width: brand.$bp-tablet) {
  .pkg-page {
    padding-block-start: brand.$spacing-6;
  }

  .pkg-hero {
    margin-block-end: brand.$spacing-6;

    &__subtitle {
      font-size: brand.$font-size-base;
    }

    &__pill {
      width: 100%;
      min-width: 0;
    }
  }

  .pkg-card {
    min-height: auto;
    padding: brand.$spacing-6 brand.$spacing-4 brand.$spacing-4;

    &__amount span {
      font-size: 32px;
    }
  }

  .pkg-benefits {
    padding: brand.$spacing-5;
  }
}
</style>
