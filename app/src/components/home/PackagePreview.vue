<script setup>
// =============================================================================
// <PackagePreview> — HomeView Pricing preview
//
// 2026-05-24: keep the home preview visually consistent with /packages.
// The home section now previews the same four pricing cards, badge rules,
// locked-price behavior, and state-based CTA language as PackageListView.
// =============================================================================
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  Calendar,
  CircleCheck,
  Lock,
  Medal,
  Star,
  UserFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { listPackages } from '@/api/package'
import { inferDefaultCurrency } from '@/components/package/CurrencySwitcher.vue'

const { t, te, locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const PLAN_KEYS = ['starter', 'standard', 'annualLite', 'premiumAnnual']

const packages = ref([])
const loaded = ref(false)
const errored = ref(false)

const tr = (key, fb, payload) => {
  if (te(key)) return t(key, payload || {})
  if (!payload) return fb
  return Object.entries(payload).reduce(
    (acc, [k, v]) => acc.replaceAll(`{${k}}`, v),
    fb
  )
}

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

const pricingCards = computed(() => {
  return packages.value.map((pkg, index) => {
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

const showSection = computed(() => loaded.value && !errored.value && pricingCards.value.length > 0)

function fallbackPackages() {
  const names = [
    tr('packages.pricing.cards.starter.name', 'Half-Year Plan · 1 lesson/week'),
    tr('packages.pricing.cards.standard.name', 'Half-Year Plan · 2 lessons/week'),
    tr('packages.pricing.cards.annualLite.name', 'Annual Plan · 1 lesson/week'),
    tr('packages.pricing.cards.premiumAnnual.name', 'Annual Plan · 2 lessons/week')
  ]
  const sessions = [26, 52, 52, 104]
  const months = [6, 6, 12, 12]

  return PLAN_KEYS.map((key, index) => ({
    id: `fallback-${key}`,
    name: names[index],
    sessions: sessions[index],
    periodMonths: months[index],
    price: null,
    currency: null,
    pricePerSession: null,
    priceVisible: false,
    sellingPoints: [],
    freeTrial: false
  }))
}

function normalizePackage(p) {
  return {
    ...p,
    sessions: p.sessions ?? p.totalCount,
    periodDays: p.periodDays ?? p.validityDays,
    freeTrial: p.freeTrial ?? p.isFreeTrial
  }
}

function normalizePreviewList(list) {
  const active = (list || [])
    .filter((p) => !p.isFreeTrial && p.isActive !== false)
    .map(normalizePackage)
  const ranked = active.filter((p) => Number(p.showOnListPriority) > 0)
  const source = ranked.length > 0
    ? [...ranked].sort((a, b) => Number(a.showOnListPriority) - Number(b.showOnListPriority))
    : active
  return source.slice(0, 4)
}

async function fetchData() {
  loaded.value = false
  errored.value = false
  const currency = inferDefaultCurrency(locale.value)
  try {
    const rows = await listPackages({ currency, locale: locale.value })
    packages.value = normalizePreviewList(rows)
    if (packages.value.length === 0 && import.meta.env.DEV) {
      packages.value = fallbackPackages()
    }
  } catch (_err) {
    if (import.meta.env.DEV) {
      packages.value = fallbackPackages()
    } else {
      errored.value = true
    }
  } finally {
    loaded.value = true
  }
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
  return !userStore.isLoggedIn || pkg?.priceVisible === false
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
  const currencyCode = pkg?.currency || inferDefaultCurrency(locale.value)
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
    return formatMoney(pkg.originalPricePerSession, pkg.currency || inferDefaultCurrency(locale.value))
  }
  if (pkg?.originalPrice != null && pkg?.pricePerSession == null) {
    return formatMoney(pkg.originalPrice, pkg.currency || inferDefaultCurrency(locale.value))
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

function goToPackages() {
  router.push('/packages')
}

function handleCardCta(card) {
  if (!userStore.isLoggedIn) {
    router.push('/register?redirect=/booking/free-trial')
    return
  }
  goToPackages()
}

function handlePriceLockCta(card) {
  if (priceLocked(card.pkg) && !userStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: '/packages' } })
    return
  }
  goToPackages()
}

const sectionRef = ref(null)
const visible = ref(false)
let io = null

onMounted(() => {
  fetchData()
  io = new IntersectionObserver(([entry]) => {
    if (entry.isIntersecting) {
      visible.value = true
      io.disconnect()
    }
  }, { threshold: 0.12 })
  if (sectionRef.value) io.observe(sectionRef.value)
})

watch(locale, () => {
  fetchData()
})

onBeforeUnmount(() => io?.disconnect())
</script>

<template>
  <section
    v-if="showSection"
    ref="sectionRef"
    class="package-preview"
    :class="{ 'is-section-visible': visible }"
  >
    <div class="package-preview__inner">
      <header class="package-preview__header">
        <div class="package-preview__eyebrow">
          <el-icon><Medal /></el-icon>
          <span>{{ tr('packages.pricing.eyebrow', 'Flexible Plans for Every Learner') }}</span>
        </div>

        <h2 class="package-preview__title">
          <span>{{ tr('packages.pricing.titlePrefix', 'Choose the Right Plan for Your') }}</span>
          {{ ' ' }}
          <strong>{{ tr('packages.pricing.titleHighlight', 'Chinese') }}</strong>
          {{ ' ' }}
          <span>{{ tr('packages.pricing.titleSuffix', 'Learning Goals') }}</span>
        </h2>

        <p v-if="pricingSubtitle" class="package-preview__sub">
          {{ pricingSubtitle }}
        </p>
        <p class="package-preview__lesson-format">
          <span>{{ lessonFormatParts.prefix }}</span>
          <strong>{{ lessonFormatParts.duration }}</strong>
          <span>{{ lessonFormatParts.middle1 }}</span>
          <strong>{{ lessonFormatParts.oneOnOne }}</strong>
          <span>{{ lessonFormatParts.middle2 }}</span>
          <strong>{{ lessonFormatParts.private }}</strong>
          <span>{{ lessonFormatParts.suffix }}</span>
        </p>

        <div class="package-preview__pills" aria-label="Pricing benefits">
          <span
            v-for="pill in heroPills"
            :key="pill.label"
            class="package-preview__pill"
          >
            <el-icon><component :is="pill.icon" /></el-icon>
            {{ pill.label }}
          </span>
        </div>
      </header>

      <div class="package-preview__grid">
        <article
          v-for="card in pricingCards"
          :key="card.pkg.id"
          class="pricing-card"
          :class="{
            'pricing-card--featured': card.featured,
            'pricing-card--value': card.bestValue,
            'is-locked': priceLocked(card.pkg)
          }"
          :style="{ '--card-idx': card.index }"
        >
          <span
            v-if="card.badge"
            class="pricing-card__badge"
            :class="card.bestValue ? 'pricing-card__badge--value' : 'pricing-card__badge--popular'"
          >
            <el-icon><Star /></el-icon>
            {{ card.badge }}
          </span>

          <header class="pricing-card__header">
            <h3 class="pricing-card__name">{{ card.title }}</h3>
            <p class="pricing-card__desc">{{ card.subtitle }}</p>
          </header>

          <div class="pricing-card__price" :class="{ 'pricing-card__price--locked': priceLocked(card.pkg) }">
            <button
              v-if="priceLocked(card.pkg)"
              type="button"
              class="pricing-card__lock"
              @click="handlePriceLockCta(card)"
            >
              <el-icon><Lock /></el-icon>
              <span>{{ tr('packages.pricing.priceLocked', 'Log in to view price') }}</span>
            </button>

            <template v-else>
              <div class="pricing-card__amount">
                <span>{{ priceDisplay(card.pkg) }}</span>
                <small v-if="priceUnit(card.pkg)">{{ priceUnit(card.pkg) }}</small>
              </div>
              <div
                v-if="originalPriceLabel(card.pkg) || card.pkg.discountLabel"
                class="pricing-card__saving"
              >
                <del v-if="originalPriceLabel(card.pkg)">{{ originalPriceLabel(card.pkg) }}</del>
                <span v-if="card.pkg.discountLabel">{{ card.pkg.discountLabel }}</span>
              </div>
            </template>
          </div>

          <p v-if="planMeta(card.pkg)" class="pricing-card__meta">
            {{ planMeta(card.pkg) }}
          </p>

          <ul class="pricing-card__points">
            <li
              v-for="point in card.points"
              :key="point"
            >
              <el-icon><CircleCheck /></el-icon>
              <span>{{ point }}</span>
            </li>
          </ul>

          <button
            class="pricing-card__cta"
            :class="{ 'pricing-card__cta--primary': card.featured || card.bestValue }"
            type="button"
            @click="handleCardCta(card)"
          >
            {{ pricingCtaLabel(card) }}
          </button>
        </article>
      </div>

      <div class="package-preview__view-all-row">
        <router-link to="/packages" class="package-preview__view-all">
          {{ t('home.package.viewAll') }}
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.package-preview {
  background:
    radial-gradient(circle at 50% 0%, rgba(brand.$brand-primary, 0.16), transparent 38%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), brand.$canvas-warm 100%);
  padding-block: brand.$spacing-12;
  padding-inline: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding-block: brand.$spacing-16;
    padding-inline: brand.$spacing-8;
  }
}

.package-preview__inner {
  max-width: 1240px;
  margin-inline: auto;
}

.package-preview__header {
  text-align: center;
  margin-block-end: brand.$spacing-10;
}

.package-preview__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;
  margin-block-end: brand.$spacing-3;
  padding: brand.$spacing-1 brand.$spacing-3;
  border-radius: brand.$radius-full;
  background: rgba(brand.$brand-primary, 0.12);
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-xs;
  font-weight: 700;
}

.package-preview__title {
  margin: 0;
  font-family: brand.$font-family-heading-en;
  font-size: clamp(32px, 5vw, 48px);
  line-height: brand.$line-height-tight;
  color: brand.$ink;

  strong {
    color: brand.$brand-primary-deep;
    font-style: italic;
  }

  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
    font-weight: 700;
    letter-spacing: 0;

    strong {
      font-style: normal;
    }
  }
}

.package-preview__sub {
  max-width: 680px;
  margin: brand.$spacing-3 auto 0;
  font-size: brand.$font-size-md;
  color: brand.$muted;
  line-height: brand.$lh-body-loose;
}

.package-preview__lesson-format {
  max-width: 760px;
  margin: brand.$spacing-2 auto 0;
  color: brand.$ink;
  font-size: brand.$font-size-sm;
  font-weight: 700;
  line-height: brand.$lh-body-loose;

  strong {
    color: brand.$brand-primary-deep;
    font-weight: 900;
  }
}

.package-preview__pills {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: brand.$spacing-3;
  margin-block-start: brand.$spacing-6;
}

.package-preview__pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: brand.$spacing-2;
  min-height: 42px;
  padding: 0 brand.$spacing-5;
  border: 1px solid brand.$hairline;
  border-radius: brand.$radius-full;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: brand.$shadow-v2-xs;
  color: brand.$ink;
  font-size: brand.$font-size-sm;
  font-weight: 700;

  .el-icon {
    color: brand.$brand-primary-deep;
  }
}

.package-preview__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-5;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  @media (min-width: brand.$bp-laptop) {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    align-items: stretch;
  }
}

.pricing-card {
  position: relative;
  display: flex;
  min-height: 420px;
  flex-direction: column;
  padding: brand.$spacing-6;
  border: 1px solid brand.$hairline;
  border-radius: brand.$radius-xl;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: brand.$shadow-v2-md;
  transition:
    transform brand.$duration-base brand.$ease-out,
    box-shadow brand.$duration-base brand.$ease-out;

  &:hover {
    transform: translateY(-3px);
    box-shadow: brand.$shadow-v2-lg;
    transition-delay: 0ms;
  }

  &--featured {
    border-color: rgba(brand.$brand-primary-deep, 0.34);
    box-shadow: brand.$shadow-v2-lg, 0 0 0 1px rgba(brand.$brand-primary-deep, 0.12);
  }

  &--value {
    border-color: rgba(brand.$brand-primary, 0.72);
  }
}

.pricing-card__badge {
  position: absolute;
  inset-block-start: -14px;
  inset-inline-end: brand.$spacing-5;
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-1;
  min-height: 30px;
  padding: 0 brand.$spacing-3;
  border-radius: brand.$radius-base;
  font-size: brand.$font-size-xs;
  font-weight: 800;
  box-shadow: brand.$shadow-v2-sm;
  white-space: nowrap;

  &--popular {
    background: brand.$brand-gradient-cta;
    color: brand.$color-text-inverse;
  }

  &--value {
    background: brand.$brand-primary;
    color: brand.$ink;
  }
}

.pricing-card__header {
  margin-block-end: brand.$spacing-6;
}

.pricing-card__name {
  margin: 0;
  color: brand.$ink;
  font-family: brand.$font-display;
  font-size: brand.$font-size-title-lg;
  font-weight: 700;
  line-height: brand.$line-height-tight;

  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
  }
}

.pricing-card__desc {
  margin: brand.$spacing-2 0 0;
  color: brand.$muted;
  font-size: brand.$font-size-sm;
  line-height: brand.$line-height-base;
}

.pricing-card__price {
  min-height: 78px;
  margin-block-end: brand.$spacing-3;
}

.pricing-card__lock {
  width: 100%;
  min-height: 66px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: brand.$spacing-2;
  border: 1px solid brand.$hairline;
  border-radius: brand.$radius-base;
  background: rgba(255, 255, 255, 0.86);
  color: brand.$body;
  font: inherit;
  font-weight: 700;
  box-shadow: inset 0 0 24px rgba(brand.$brand-primary, 0.08);
  cursor: pointer;

  &:hover {
    border-color: rgba(brand.$brand-primary-deep, 0.35);
    color: brand.$brand-primary-deep;
  }

  &:focus-visible {
    outline: none;
    box-shadow: brand.$ring-focus;
  }
}

.pricing-card__amount {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: brand.$spacing-2;
  color: brand.$brand-primary-deep;

  span {
    font-size: clamp(30px, 4vw, 42px);
    font-weight: 800;
    line-height: 1;
  }

  small {
    color: brand.$body;
    font-size: brand.$font-size-sm;
    font-weight: 600;
  }
}

.pricing-card__saving {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: brand.$spacing-2;
  margin-block-start: brand.$spacing-2;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-xs;

  del {
    color: brand.$muted;
  }

  span {
    padding: 2px brand.$spacing-2;
    border-radius: brand.$radius-full;
    background: rgba(brand.$brand-primary, 0.14);
  }
}

.pricing-card__meta {
  margin: 0 0 brand.$spacing-5;
  color: brand.$body;
  font-size: brand.$font-size-sm;
  line-height: brand.$line-height-base;
}

.pricing-card__points {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: brand.$spacing-3;
  margin: 0 0 brand.$spacing-6;
  padding: 0;
  list-style: none;

  li {
    display: flex;
    align-items: flex-start;
    gap: brand.$spacing-2;
    color: brand.$body;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }

  .el-icon {
    margin-block-start: 2px;
    color: brand.$brand-primary-deep;
    flex-shrink: 0;
  }
}

.pricing-card__cta {
  width: 100%;
  min-height: 48px;
  padding: 0 brand.$spacing-4;
  border: 1.5px solid brand.$brand-primary-deep;
  border-radius: brand.$radius-base;
  background: transparent;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-sm;
  font-weight: 800;
  cursor: pointer;
  transition:
    transform brand.$duration-fast brand.$ease-out,
    background brand.$duration-fast ease,
    color brand.$duration-fast ease,
    box-shadow brand.$duration-fast ease;

  &:hover {
    transform: translateY(-2px);
    background: rgba(brand.$brand-primary, 0.1);
  }

  &:focus-visible {
    outline: none;
    box-shadow: brand.$ring-focus;
  }

  &--primary {
    border-color: transparent;
    background: brand.$brand-gradient-cta;
    color: brand.$color-text-inverse;
    box-shadow: brand.$shadow-v2-sm;

    &:hover {
      background: brand.$brand-gradient-cta;
      color: brand.$color-text-inverse;
      box-shadow: brand.$shadow-glow-primary;
    }
  }
}

.package-preview__view-all-row {
  margin-block-start: brand.$spacing-8;
  text-align: center;
}

.package-preview__view-all {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-1;
  color: brand.$brand-primary-deep;
  font-size: brand.$font-size-base;
  font-weight: 700;
  text-decoration: none;

  &:hover {
    color: brand.$brand-primary;
  }

  [dir='rtl'] & .el-icon {
    transform: scaleX(-1);
  }
}

@media (max-width: #{brand.$bp-mobile}) {
  .package-preview {
    padding-inline: brand.$spacing-3;
  }

  .package-preview__pills {
    align-items: stretch;
    flex-direction: column;
  }

  .package-preview__pill {
    width: 100%;
  }

  .pricing-card {
    min-height: auto;
    padding: brand.$spacing-5;
  }
}

@media (prefers-reduced-motion: reduce) {
  .pricing-card {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
}
</style>
