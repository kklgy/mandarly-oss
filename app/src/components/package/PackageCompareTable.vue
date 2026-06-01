<script setup>
/**
 * PackageCompareTable.vue — § D 完整套餐对比表
 *
 * 设计稿: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § PackageListView § D
 *
 * PC ≥ 768  → EP <el-table border> + 首列 sticky-left
 * H5 < 768  → 折叠 accordion + 展开后水平滚动(scroll-snap)
 *
 * Props:
 *   - packages: Array<PackageVO> — 完整套餐列表(主推 4 + 单课 + 任何额外)
 *   - currency: 'HKD'|'USD'|'CNY'
 *   - compact:  Boolean(true 时强制 H5 折叠形态, 用于父级控制)
 *
 * Events:
 *   - click-buy: { pkg } — 行内"购买"按钮点击
 */
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  packages: { type: Array, default: () => [] },
  currency: { type: String, default: 'HKD' },
  compact: { type: Boolean, default: false },
  priceVisible: { type: Boolean, default: true }
})

const emit = defineEmits(['click-buy'])

const { t, te } = useI18n()
const tt = (key, fb) => (te(key) ? t(key) : fb)

const expanded = ref(false)

const labels = computed(() => ({
  title: tt('packages.compare.title', '完整套餐对比'),
  expand: tt('packages.compare.expand', '展开对比表'),
  collapse: tt('packages.compare.collapse', '收起'),
  package: tt('packages.compare.col.package', '套餐'),
  totalSessions: tt('packages.compare.row.totalLessons', '总课次'),
  duration: tt('packages.compare.row.validity', '有效期'),
  pricePerLesson: tt('packages.compare.row.pricePerLesson', '单节价格'),
  totalPrice: tt('packages.compare.row.totalPrice', '总价'),
  discount: tt('packages.compare.row.discount', '折扣'),
  booking: tt('packages.compare.row.booking', '自由预约'),
  refund: tt('packages.compare.row.refund', '24h 退款'),
  upgrade: tt('packages.compare.row.upgrade', '升级 / 降级费'),
  action: tt('packages.compare.row.action', '操作'),
  buy: tt('packages.compare.buy', '立即购买'),
  loginToViewPrice: tt('packages.compare.loginToViewPrice', '登录查看价格'),
  loginAction: tt('packages.compare.loginAction', '登录 / 注册'),
  priceLockedNote: tt('packages.compare.priceLockedNote', '登录后查看各套餐完整价格,套餐权益仍可对比'),
  yes: tt('packages.compare.yes', '✓'),
  no: tt('packages.compare.no', '—'),
  free: tt('packages.compare.free', '免费'),
  durationMonths: tt('packages.compare.durationMonths', '{n} 个月'),
  durationDays: tt('packages.compare.durationDays', '{n} 天')
}))

const isPriceVisible = computed(() => props.priceVisible && props.packages.every((p) => p.priceVisible !== false))

const currencyPrefix = computed(() => {
  switch (props.currency) {
    case 'CNY': return '¥'
    case 'USD': return 'US$'
    case 'HKD':
    default: return 'HK$'
  }
})

function fmtMoney(amount) {
  if (amount == null || amount === '') return '—'
  const num = Number(amount)
  if (Number.isNaN(num)) return '—'
  if (num === 0) return labels.value.free
  try {
    const formatted = new Intl.NumberFormat('en-US', { style: 'decimal', maximumFractionDigits: 2 }).format(num)
    return `${currencyPrefix.value} ${formatted}`
  } catch (e) {
    return `${currencyPrefix.value} ${num}`
  }
}

function fmtDuration(pkg) {
  if (pkg.periodMonths) {
    return labels.value.durationMonths.replace('{n}', pkg.periodMonths)
  }
  if (pkg.periodDays) {
    return labels.value.durationDays.replace('{n}', pkg.periodDays)
  }
  if (pkg.validityDays) {
    return labels.value.durationDays.replace('{n}', pkg.validityDays)
  }
  return '—'
}

// 行定义 + cell 取值器(单一数据源, PC 表格 + H5 卡片复用)
const rows = computed(() => [
  { key: 'totalSessions', label: labels.value.totalSessions, getter: (p) => p.sessions ?? p.totalCount ?? '—' },
  { key: 'duration', label: labels.value.duration, getter: (p) => fmtDuration(p) },
  { key: 'pricePerLesson', label: labels.value.pricePerLesson, getter: (p) => fmtMoney(p.pricePerSession), locked: true },
  { key: 'totalPrice', label: labels.value.totalPrice, getter: (p) => fmtMoney(p.price), locked: true },
  { key: 'discount', label: labels.value.discount, getter: (p) => p.discountLabel || labels.value.no, isDiscount: true },
  { key: 'booking', label: labels.value.booking, getter: () => labels.value.yes },
  { key: 'refund', label: labels.value.refund, getter: () => labels.value.yes }
])

function onBuyClick(pkg) {
  emit('click-buy', { pkg })
}

function toggleExpand() {
  expanded.value = !expanded.value
}
</script>

<template>
  <section class="pkg-compare" aria-labelledby="pkg-compare-title">
    <h2 id="pkg-compare-title" class="pkg-compare__title">
      {{ labels.title }}
    </h2>
    <p v-if="!isPriceVisible" class="pkg-compare__locked-note">
      {{ labels.priceLockedNote }}
    </p>

    <!-- PC 表格 -->
    <div class="pkg-compare__table-wrap" :class="{ 'is-mobile-only': compact }">
      <el-table
        :data="rows"
        border
        :show-header="true"
        class="pkg-compare__table"
      >
        <el-table-column
          prop="label"
          :label="labels.package"
          fixed="left"
          width="140"
        >
          <template #default="{ row }">
            <span class="pkg-compare__row-label">{{ row.label }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-for="pkg in packages"
          :key="pkg.id"
          :label="pkg.name"
          align="center"
          min-width="120"
        >
          <template #default="{ row }">
            <span
              :class="{
                'pkg-compare__cell-discount': row.isDiscount && pkg.discountLabel
              }"
            >
              <span v-if="row.locked && !isPriceVisible" class="pkg-compare__muted">
                {{ labels.loginToViewPrice }}
              </span>
              <template v-else>{{ row.getter(pkg) }}</template>
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 操作行 (单独, 不混在 row 里, EP table 单元格放 button 易布局碎裂) -->
      <div class="pkg-compare__actions">
        <div class="pkg-compare__actions-label">{{ labels.action }}</div>
        <div class="pkg-compare__actions-list">
          <el-button
            v-for="pkg in packages"
            :key="pkg.id"
            type="primary"
            size="small"
            class="pkg-compare__buy"
            @click="onBuyClick(pkg)"
          >
            {{ isPriceVisible ? labels.buy : labels.loginAction }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- H5 折叠 accordion: 横滚迷你卡片(每个套餐 1 列)-->
    <div class="pkg-compare__mobile">
      <button
        type="button"
        class="pkg-compare__mobile-toggle"
        :aria-expanded="expanded"
        @click="toggleExpand"
      >
        <span>{{ labels.expand }}</span>
        <span class="pkg-compare__mobile-toggle-icon" aria-hidden="true">{{ expanded ? '▴' : '▾' }}</span>
      </button>
      <div v-if="expanded" class="pkg-compare__mobile-list">
        <article
          v-for="pkg in packages"
          :key="pkg.id"
          class="pkg-compare__mobile-card"
        >
          <h3 class="pkg-compare__mobile-name">{{ pkg.name }}</h3>
          <dl class="pkg-compare__mobile-rows">
            <div
              v-for="row in rows"
              :key="row.key"
              class="pkg-compare__mobile-row"
            >
              <dt>{{ row.label }}</dt>
              <dd
                :class="{
                  'pkg-compare__cell-discount': row.isDiscount && pkg.discountLabel
                }"
              >
                <span v-if="row.locked && !isPriceVisible" class="pkg-compare__muted">
                  {{ labels.loginToViewPrice }}
                </span>
                <template v-else>{{ row.getter(pkg) }}</template>
              </dd>
            </div>
          </dl>
          <el-button
            type="primary"
            class="pkg-compare__mobile-buy"
            @click="onBuyClick(pkg)"
          >
            {{ isPriceVisible ? labels.buy : labels.loginAction }}
          </el-button>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.pkg-compare {
  margin-block-start: brand.$spacing-10;

  &__title {
    margin: 0 0 brand.$spacing-5;
    font-size: brand.$font-size-xl;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__locked-note {
    margin: 0 0 brand.$spacing-4;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
  }

  &__table-wrap {
    display: block;

    @media (max-width: brand.$bp-tablet) {
      display: none;
    }

    &.is-mobile-only {
      display: none;
    }
  }

  &__row-label {
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__cell-discount {
    color: brand.$brand-primary-deep;
    font-weight: 600;
  }

  &__locked {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 96px;
    padding: brand.$spacing-1 brand.$spacing-2;
    border-radius: brand.$radius-base;
    border: 1px solid brand.$color-border;
    background: rgba(255, 255, 255, 0.72);
    backdrop-filter: blur(8px);
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-sm;
    font-weight: 600;
  }

  &__muted {
    color: brand.$color-text-tertiary;
  }

  &__actions {
    display: grid;
    grid-template-columns: 140px 1fr;
    align-items: center;
    border-block-end: 1px solid brand.$color-border;
    border-inline: 1px solid brand.$color-border;
    background: brand.$color-bg-card;
    padding-block: brand.$spacing-3;
  }

  &__actions-label {
    padding-inline-start: brand.$spacing-4;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__actions-list {
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-3;
    justify-content: space-around;
    padding-inline: brand.$spacing-3;
  }

  &__buy {
    min-width: 84px;
  }

  // ---- H5 ----
  &__mobile {
    display: none;

    @media (max-width: brand.$bp-tablet) {
      display: block;
    }
  }

  &__mobile-toggle {
    display: flex;
    width: 100%;
    align-items: center;
    justify-content: space-between;
    padding-block: brand.$spacing-4;
    padding-inline: brand.$spacing-4;
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    font: inherit;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: brand.$color-text-primary;
    cursor: pointer;
  }

  &__mobile-toggle-icon {
    color: brand.$color-text-secondary;
  }

  &__mobile-list {
    margin-block-start: brand.$spacing-3;
    display: flex;
    overflow-x: auto;
    gap: brand.$spacing-3;
    scroll-snap-type: x mandatory;
    padding-block-end: brand.$spacing-2;

    -webkit-overflow-scrolling: touch;
  }

  &__mobile-card {
    flex: 0 0 80%;
    scroll-snap-align: start;
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    padding: brand.$spacing-4;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
  }

  &__mobile-name {
    margin: 0;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__mobile-rows {
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__mobile-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: brand.$font-size-sm;
    border-block-start: 1px solid brand.$color-divider;
    padding-block-start: brand.$spacing-2;

    &:first-child {
      border-block-start: none;
      padding-block-start: 0;
    }

    dt {
      color: brand.$color-text-secondary;
    }

    dd {
      margin: 0;
      color: brand.$color-text-primary;
      font-weight: 500;
      text-align: end;
    }
  }

  &__mobile-buy {
    width: 100%;
  }
}
</style>
