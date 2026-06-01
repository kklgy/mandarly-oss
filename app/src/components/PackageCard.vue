<script setup>
// =============================================================================
// <PackageCard> — 套餐卡片(Mandarly 共享, 3 variant)
//
// 契约: app/src/components/SHARED-API.md §2
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md §8.2
//
// variant:
//   - home           : Home § 4(3 张精华, 不显示 sellingPoints, 显示推荐 pill / 折扣徽章)
//   - list-major     : PackageList § C 主推 4 卡(完整版: 推荐 pill + 折扣 + 3 行卖点)
//   - list-secondary : 对比表 mini(P2 占位; 紧凑, 仅价格 + CTA)
//
// 严格约束:
//   1) i18n: 调用方传完整本地化字段 — name / sellingPoints / recommendationLabel
//      / discountLabel; 组件内部 t() 仅用于 CTA 文案兜底(白名单)
//   2) token only — 0 硬编码
//   3) RTL: 推荐 pill 顶部居中(translate 数学等价); 折扣徽章 inset-inline-end
//   4) sellingPoints / recommendationLabel / discountLabel 字段为 P1 后端字段,
//      ?? null 兜底, 字段未落时不渲染对应视觉
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import PriceTag from './PriceTag.vue'

const props = defineProps({
  pkg: {
    type: Object,
    required: true
  },
  variant: {
    type: String,
    default: 'list-major',
    validator: (v) => ['home', 'list-major', 'list-secondary'].includes(v)
  },
  isPurchased: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  priceVisible: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click-buy', 'click-claim-trial'])

const { t, te } = useI18n()
const t2 = (key, fallback) => (te(key) ? t(key) : fallback)

// 推荐 pill / 折扣 / 卖点字段统一兜底
const recommendationLabel = computed(() => props.pkg?.recommendationLabel ?? null)
const discountLabel = computed(() => props.pkg?.discountLabel ?? null)
const sellingPoints = computed(() => props.pkg?.sellingPoints ?? [])
const isFreeTrial = computed(() => Boolean(props.pkg?.freeTrial))
const priceLocked = computed(() => !isFreeTrial.value && (!props.priceVisible || props.pkg?.priceVisible === false))

const showSellingPoints = computed(
  () => props.variant === 'list-major' && sellingPoints.value.length > 0
)
const showRecommendationPill = computed(
  () => props.variant !== 'list-secondary' && !!recommendationLabel.value
)
const showDiscountBadge = computed(
  () => props.variant !== 'list-secondary' && !!discountLabel.value
)
const isRecommendedCard = computed(() => showRecommendationPill.value)

const ctaText = computed(() => {
  if (props.loading) {
    return t2('packageCard.cta.loading', t2('common.loading', '加载中…'))
  }
  if (isFreeTrial.value) {
    return t2('packageCard.cta.claimTrial', '立即领取')
  }
  if (priceLocked.value) {
    return t2('packageCard.cta.loginRegister', '登录 / 注册')
  }
  return t2('packageCard.cta.buy', '立即购买')
})

const purchasedTagText = computed(() => t2('packageCard.purchased', '已购'))

function tn(key, fallback, n) {
  return te(key) ? t(key, { n }) : fallback.replace('{n}', n)
}

function onCtaClick() {
  if (props.loading) return
  if (isFreeTrial.value) {
    emit('click-claim-trial', { pkg: props.pkg })
  } else {
    emit('click-buy', { pkg: props.pkg })
  }
}

const periodLabel = computed(() => {
  const pkg = props.pkg || {}
  if (pkg.periodMonths) {
    return tn('packageCard.period.months', '{n} 个月有效', pkg.periodMonths)
  }
  if (pkg.periodDays) {
    return tn('packageCard.period.days', '{n} 天有效', pkg.periodDays)
  }
  return ''
})

const sessionsLabel = computed(() => {
  const n = props.pkg?.sessions
  if (!n) return ''
  return tn('packageCard.sessions', '{n} 节', n)
})

const perSessionLabel = computed(() => {
  const pkg = props.pkg || {}
  if (pkg.pricePerSession == null) return ''
  return pkg.pricePerSession
})
</script>

<template>
  <article
    class="package-card"
    :class="[
      `package-card--${variant}`,
      isRecommendedCard ? 'package-card--recommended' : '',
      isPurchased ? 'is-purchased' : ''
    ]"
  >
    <!-- 推荐 pill 顶部居中 -->
    <span
      v-if="showRecommendationPill"
      class="package-card__pill-recommend"
    >
      {{ recommendationLabel }}
    </span>

    <!-- 折扣徽章 右上 -->
    <span
      v-if="showDiscountBadge"
      class="package-card__badge-discount"
    >
      {{ discountLabel }}
    </span>

    <!-- 已购 tag 左上(EP el-tag) -->
    <el-tag
      v-if="isPurchased"
      class="package-card__tag-purchased"
      size="small"
      type="warning"
      effect="plain"
    >
      {{ purchasedTagText }}
    </el-tag>

    <header class="package-card__head">
      <h3 class="package-card__name">{{ pkg.name }}</h3>
      <p v-if="sessionsLabel || periodLabel" class="package-card__meta">
        <span v-if="sessionsLabel">{{ sessionsLabel }}</span>
        <span v-if="sessionsLabel && periodLabel" aria-hidden="true"> · </span>
        <span v-if="periodLabel">{{ periodLabel }}</span>
      </p>
    </header>

    <div class="package-card__price">
      <button
        v-if="priceLocked"
        type="button"
        class="package-card__locked-trigger"
        @click="onCtaClick"
      >
        <PriceTag
          :amount="pkg.price"
          :currency="pkg.currency || 'HKD'"
          :locked-label="t2('packageCard.cta.loginToViewPrice', '登录查看价格')"
          :locked="priceLocked"
          size="large"
        />
      </button>
      <template v-else>
        <PriceTag
          :amount="pkg.price"
          :currency="pkg.currency || 'HKD'"
          :free="isFreeTrial"
          size="large"
        />
      </template>
      <p v-if="variant !== 'list-secondary' && perSessionLabel && !isFreeTrial && !priceLocked" class="package-card__per">
        <PriceTag
          :amount="perSessionLabel"
          :currency="pkg.currency || 'HKD'"
          :period="t2('packageCard.perSessionUnit', '/ 节')"
          size="default"
        />
      </p>
    </div>

    <ul
      v-if="showSellingPoints"
      class="package-card__points"
    >
      <li
        v-for="(point, idx) in sellingPoints"
        :key="idx"
        class="package-card__point"
      >
        <span class="package-card__point-check" aria-hidden="true">✓</span>
        <span>{{ point }}</span>
      </li>
    </ul>

    <footer v-if="!priceLocked" class="package-card__foot">
      <el-button
        :type="isRecommendedCard ? 'primary' : 'default'"
        :loading="loading"
        :disabled="loading"
        class="package-card__cta"
        @click="onCtaClick"
      >
        {{ ctaText }}
      </el-button>
    </footer>
  </article>
</template>

<style scoped lang="scss">

.package-card {
  position: relative;
  background: var(--color-bg-card);
  border-radius: brand.$radius-card;
  border: 1px solid brand.$color-border;
  padding: brand.$spacing-6 brand.$spacing-5;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
  box-shadow: brand.$shadow-sm;
  transition: box-shadow 0.18s ease, transform 0.18s ease;

  &:hover {
    box-shadow: brand.$shadow-md;
    transform: translateY(-2px);
  }

  &--recommended {
    border: 1.5px solid brand.$brand-primary;

    @media (max-width: brand.$bp-tablet) {
      transform: scale(1.02);

      &:hover {
        transform: scale(1.02) translateY(-2px);
      }
    }
  }

  &__pill-recommend {
    position: absolute;
    inset-block-start: -12px;
    inset-inline-start: 50%;
    transform: translateX(-50%);
    padding: brand.$spacing-1 brand.$spacing-4;
    background: brand.$brand-primary;
    color: brand.$color-text-inverse;
    border-radius: brand.$radius-pill;
    font-size: brand.$font-size-xs;
    font-weight: 600;
    white-space: nowrap;
    z-index: 1;
  }

  &__badge-discount {
    position: absolute;
    inset-block-start: brand.$spacing-3;
    inset-inline-end: brand.$spacing-3;
    padding: brand.$spacing-1 brand.$spacing-2;
    background: brand.$color-text-primary;
    color: brand.$color-text-inverse;
    border-radius: brand.$radius-base;
    font-size: brand.$font-size-xs;
    font-weight: 500;
    line-height: 1;
    z-index: 1;
  }

  &__tag-purchased {
    position: absolute;
    inset-block-start: brand.$spacing-3;
    inset-inline-start: brand.$spacing-3;
    z-index: 1;
  }

  &__head {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__name {
    margin: 0;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;
  }

  &__meta {
    margin: 0;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
  }

  &__price {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__locked-trigger {
    width: 100%;
    padding: 0;
    border: 0;
    background: transparent;
    cursor: pointer;
    font: inherit;
    text-align: inherit;

    :deep(.price-tag--locked) {
      width: 100%;
      min-height: 72px;
      justify-content: center;
    }
  }

  &__per {
    margin: 0;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
  }

  &__points {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__point {
    display: flex;
    align-items: flex-start;
    gap: brand.$spacing-2;
    font-size: brand.$font-size-base;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-base;
  }

  &__point-check {
    color: var(--color-success);
    font-weight: 700;
    flex-shrink: 0;
  }

  &__foot {
    margin-block-start: auto;
  }

  &__cta {
    width: 100%;
    height: 48px;
    font-size: brand.$font-size-base;
    font-weight: 600;
  }

  // ===========================
  // variant: home
  // ===========================
  &--home {
    padding: brand.$spacing-5 brand.$spacing-4;

    .package-card__cta {
      height: 44px;
    }
  }

  // ===========================
  // variant: list-major
  // ===========================
  &--list-major {
    // 默认样式即 list-major,无需额外覆盖
  }

  // ===========================
  // variant: list-secondary (P2)
  // ===========================
  &--list-secondary {
    padding: brand.$spacing-4 brand.$spacing-4;
    box-shadow: none;
    border: 1px solid brand.$color-border;
    gap: brand.$spacing-3;

    .package-card__name {
      font-size: brand.$font-size-base;
    }

    .package-card__cta {
      height: 36px;
      font-size: brand.$font-size-sm;
    }

    &:hover {
      box-shadow: brand.$shadow-sm;
      transform: none;
    }
  }
}
</style>
