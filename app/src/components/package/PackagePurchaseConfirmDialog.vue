<script setup>
/**
 * PackagePurchaseConfirmDialog.vue — § F 确认订单 Dialog(转化保护层)
 *
 * 设计稿: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § PackageListView § F
 * 命名:  PackagePurchaseConfirmDialog (避免与第 5 轮 OrderCancelDialog 混淆)
 *
 * 视觉:
 *   - PC: <el-dialog width="480px">
 *   - H5: 同 dialog, top: 5vh, width: 90vw(EP dialog 自带响应式)
 *
 * 实现:
 *   - 点"前往支付" → 调用方 emit('confirm') → 调用方调 payment.checkout({ packageId, currency })
 *     → 拿 checkoutUrl → window.location.href
 *   - loading 期间 disable 按钮 + 显示 spinner, 防止双击重复创建 session
 *
 * Props:
 *   - visible:   v-model
 *   - pkg:       套餐对象 (id, name, sessions, periodMonths/Days, price, pricePerSession, discountLabel)
 *   - currency:  'HKD'|'USD'|'CNY'
 *   - loading:   父级 checkout pending
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import PriceTag from '@/components/PriceTag.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  pkg: { type: Object, default: null },
  currency: { type: String, default: 'HKD' },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel'])

const { t, te } = useI18n()
const tt = (key, fb) => (te(key) ? t(key) : fb)

const labels = computed(() => ({
  title: tt('packages.confirm.title', '确认订单'),
  package: tt('packages.confirm.field.package', '套餐'),
  sessions: tt('packages.confirm.field.sessions', '课次'),
  duration: tt('packages.confirm.field.duration', '有效期'),
  currencyLabel: tt('packages.confirm.field.currency', '币种'),
  subtotal: tt('packages.confirm.field.subtotal', '小计'),
  discount: tt('packages.confirm.field.discount', '优惠'),
  total: tt('packages.confirm.field.total', '合计'),
  tip: tt('packages.confirm.tip', '点击"前往支付"将跳转 Stripe 安全支付页面'),
  refundTip: tt('packages.confirm.refundTip', '课前 24 小时取消可全额退款,详见服务条款 § 5'),
  cancel: tt('packages.confirm.cancel', '取消'),
  confirm: tt('packages.confirm.confirm', '前往支付'),
  confirming: tt('packages.confirm.confirming', '处理中…'),
  termsLink: tt('packages.confirm.termsLink', '服务条款'),
  noPlaceholder: '—',
  durationMonths: tt('packages.confirm.durationMonths', '{n} 个月(自购买日起)'),
  durationDays: tt('packages.confirm.durationDays', '{n} 天'),
  sessionsCount: tt('packages.confirm.sessionsCount', '约 {n} 节'),
  sessionsCountWeekly: tt('packages.confirm.sessionsCountWeekly', '约 {n} 节({weekly} 节/周)')
}))

const sessionsText = computed(() => {
  const p = props.pkg
  if (!p) return ''
  const total = p.sessions ?? p.totalCount
  if (!total) return ''
  if (p.weeklyCount) {
    return labels.value.sessionsCountWeekly
      .replace('{n}', total)
      .replace('{weekly}', p.weeklyCount)
  }
  return labels.value.sessionsCount.replace('{n}', total)
})

const durationText = computed(() => {
  const p = props.pkg
  if (!p) return ''
  if (p.periodMonths) return labels.value.durationMonths.replace('{n}', p.periodMonths)
  if (p.periodDays) return labels.value.durationDays.replace('{n}', p.periodDays)
  if (p.validityDays) return labels.value.durationDays.replace('{n}', p.validityDays)
  return labels.value.noPlaceholder
})

const discountText = computed(() => props.pkg?.discountLabel || labels.value.noPlaceholder)
const displayCurrency = computed(() => props.pkg?.currency || props.currency)

function handleClose() {
  if (props.loading) return
  emit('update:visible', false)
  emit('cancel')
}

function handleConfirm() {
  if (props.loading) return
  emit('confirm', { pkg: props.pkg, currency: props.currency })
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="labels.title"
    width="480px"
    align-center
    :close-on-click-modal="!loading"
    :close-on-press-escape="!loading"
    :show-close="!loading"
    class="pkg-confirm-dialog"
    @update:model-value="(v) => emit('update:visible', v)"
    @close="handleClose"
  >
    <div v-if="pkg" class="pkg-confirm-dialog__body">
      <dl class="pkg-confirm-dialog__rows">
        <div class="pkg-confirm-dialog__row">
          <dt>{{ labels.package }}</dt>
          <dd>{{ pkg.name }}</dd>
        </div>
        <div v-if="sessionsText" class="pkg-confirm-dialog__row">
          <dt>{{ labels.sessions }}</dt>
          <dd>{{ sessionsText }}</dd>
        </div>
        <div class="pkg-confirm-dialog__row">
          <dt>{{ labels.duration }}</dt>
          <dd>{{ durationText }}</dd>
        </div>
        <div class="pkg-confirm-dialog__row">
          <dt>{{ labels.currencyLabel }}</dt>
          <dd>{{ displayCurrency }}</dd>
        </div>
        <div class="pkg-confirm-dialog__row">
          <dt>{{ labels.subtotal }}</dt>
          <dd>
            <PriceTag :amount="pkg.price" :currency="displayCurrency" size="default" />
          </dd>
        </div>
        <div class="pkg-confirm-dialog__row">
          <dt>{{ labels.discount }}</dt>
          <dd>{{ discountText }}</dd>
        </div>
        <div class="pkg-confirm-dialog__row pkg-confirm-dialog__row--total">
          <dt>{{ labels.total }}</dt>
          <dd>
            <PriceTag :amount="pkg.price" :currency="displayCurrency" size="large" />
          </dd>
        </div>
      </dl>

      <p class="pkg-confirm-dialog__tip">
        <span aria-hidden="true">ⓘ </span>{{ labels.tip }}
      </p>

      <p class="pkg-confirm-dialog__refund">
        {{ labels.refundTip }}
        <router-link
          to="/legal/terms#refund"
          class="pkg-confirm-dialog__terms-link"
          target="_blank"
          rel="noopener"
        >
          {{ labels.termsLink }}
        </router-link>
      </p>
    </div>

    <template #footer>
      <div class="pkg-confirm-dialog__footer">
        <el-button :disabled="loading" @click="handleClose">
          {{ labels.cancel }}
        </el-button>
        <el-button
          type="primary"
          :loading="loading"
          :disabled="loading"
          @click="handleConfirm"
        >
          {{ loading ? labels.confirming : labels.confirm }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.pkg-confirm-dialog {
  &__body {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-4;
  }

  &__rows {
    margin: 0;
    display: flex;
    flex-direction: column;
  }

  &__row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-block: brand.$spacing-3;
    border-block-start: 1px solid brand.$color-divider;
    font-size: brand.$font-size-base;

    &:first-child {
      border-block-start: none;
    }

    dt {
      color: brand.$color-text-secondary;
      margin: 0;
    }

    dd {
      margin: 0;
      color: brand.$color-text-primary;
      font-weight: 500;
      text-align: end;
    }

    &--total {
      border-block-start: 1px solid brand.$color-border;
      padding-block-start: brand.$spacing-4;
      margin-block-start: brand.$spacing-2;

      dt {
        color: brand.$color-text-primary;
        font-weight: 600;
      }
    }
  }

  &__tip {
    margin: 0;
    padding: brand.$spacing-3 brand.$spacing-4;
    background: brand.$brand-primary-soft;
    border-radius: brand.$radius-base;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
    line-height: brand.$line-height-base;
  }

  &__refund {
    margin: 0;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-tertiary;
    line-height: brand.$line-height-base;
  }

  &__terms-link {
    color: brand.$brand-primary-deep;
    text-decoration: underline;

    &:hover {
      color: brand.$brand-primary;
    }
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: brand.$spacing-3;
  }
}

// EP dialog 在 H5 自适应宽度(480px > 90vw 时退化)
@media (max-width: brand.$bp-tablet) {
  :deep(.el-dialog) {
    width: 90vw !important;
    max-width: 480px;
  }
}
</style>
