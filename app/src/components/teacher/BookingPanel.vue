<script setup>
// =============================================================================
// <BookingPanel> — TeacherDetail § E 预约面板(转化核心区)
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § TeacherDetailView 改造 § E
//
// 功能:
//   1) 价格 + 时长(/ 25 分钟)
//   2) <ScheduleSlotPicker> 时段选择(沿用现状组件)
//   3) "已选" 信息回显
//   4) "立即预约" 主 CTA
//   5) "课前 24h 取消可全额退款" 提示
//
// 注:
//   - 套餐余额展示 / 提交预约逻辑 在父级 TeacherDetailView 处理(更接近现状代码,
//     便于沿用 myPackages / submitBooking 流程)
//   - 父级通过 v-model:selected-slot 传 selectedSlot,本组件 emit 'select-slot'
//   - 父级通过 @click-book 处理预约链路(未登录 / 余额不足 / 正常)
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import ScheduleSlotPicker from '@/components/ScheduleSlotPicker.vue'
import PriceTag from '@/components/PriceTag.vue'
import { getTeacherPricingMode } from '@/utils/teacherPricing'

const props = defineProps({
  teacherId: {
    type: [String, Number],
    required: true
  },
  pricePerSession: {
    type: [Number, String],
    default: 250
  },
  currency: {
    type: String,
    default: 'HKD'
  },
  sessionMinutes: {
    type: Number,
    default: 25
  },
  selectedSlot: {
    type: Object,
    default: null
  },
  submitting: {
    type: Boolean,
    default: false
  },
  // 让 H5 sticky CTA 找到滚动锚
  panelId: {
    type: String,
    default: 'teacher-booking-panel'
  },
  // M6 B8:教师角色 — 双角色禁购,显示文案 + disable CTA
  teacherBlocked: {
    type: Boolean,
    default: false
  },
  priceVisible: {
    type: Boolean,
    default: true
  },
  ctaState: {
    type: String,
    default: 'book',
    validator: (v) => ['book', 'buy-package', 'other-teachers'].includes(v)
  }
})

const emit = defineEmits(['select-slot', 'availability-change', 'click-book', 'click-other-teachers'])

const { t, te } = useI18n()
const t2 = (key, fallback, payload) => (te(key) ? t(key, payload || {}) : fallback)

const periodLabel = computed(() => {
  const m = props.sessionMinutes
  return t2('teacherDetail.book.priceUnit', `/ ${m} 分钟`, { n: m, minutes: m })
    .replace('{n}', m)
    .replace('{minutes}', m)
})

const pricingMode = computed(() =>
  getTeacherPricingMode({
    isLoggedIn: props.priceVisible,
    isTeacher: props.teacherBlocked,
    pricePerSession: props.pricePerSession
  })
)

const pricingHint = computed(() => {
  if (pricingMode.value === 'package-credit') {
    return t2('teacherDetail.book.packageCreditPricing', '按套餐课次预约')
  }
  return t2('teacherDetail.book.loginToViewPackages', '登录后查看套餐')
})

const ctaText = computed(() => {
  if (props.teacherBlocked) {
    return t2('booking.teacherBlocked.shortHint', '教师账号不可购买')
  }
  if (props.submitting) {
    return t2('booking.dialog.submitting', '提交中…')
  }
  if (props.ctaState === 'other-teachers') {
    return t2('teacherDetail.book.viewOtherTeachers', '查看其他老师')
  }
  if (props.ctaState === 'buy-package') {
    return t2('teacherDetail.book.buyPackageForTeacher', '购买套餐(为本老师预约)')
  }
  return t2('teacherDetail.book.bookNow', '立即预约')
})

function onPickSlot(payload) {
  emit('select-slot', payload)
}

function onAvailabilityChange(payload) {
  emit('availability-change', payload)
}

function onBook() {
  if (props.ctaState === 'other-teachers') {
    emit('click-other-teachers')
    return
  }
  emit('click-book')
}
</script>

<template>
  <section
    :id="panelId"
    class="td-booking"
  >
    <div class="td-booking__price">
      <PriceTag
        v-if="pricingMode === 'price' || pricingMode === 'locked'"
        :amount="pricePerSession"
        :currency="currency"
        :period="periodLabel"
        :locked="pricingMode === 'locked'"
        size="large"
      />
      <span
        v-else
        class="td-booking__price-hint"
        :class="{ 'td-booking__price-hint--locked': pricingMode === 'login-package' }"
      >
        {{ pricingHint }}
      </span>
    </div>

    <div class="td-booking__divider" aria-hidden="true"></div>

    <h2 class="td-booking__title">{{ t2('teacherDetail.book.selectSlot', '选择上课时段') }}</h2>

    <div class="td-booking__picker">
      <ScheduleSlotPicker
        :teacher-id="teacherId"
        @select="onPickSlot"
        @availability-change="onAvailabilityChange"
      />
    </div>

    <div v-if="selectedSlot" class="td-booking__selected">
      <span class="td-booking__selected-label">{{ t2('teacherDetail.book.selectedSlot', '已选') }}:</span>
      <span class="td-booking__selected-date">{{ selectedSlot.dateLabel }}</span>
      <span class="td-booking__selected-time">{{ selectedSlot.timeLabel }}</span>
    </div>

    <button
      type="button"
      class="td-booking__cta"
      :disabled="(ctaState !== 'other-teachers' && !selectedSlot) || submitting || teacherBlocked"
      :title="teacherBlocked ? t2('booking.teacherBlocked.tooltip', '教师账号不可购买,请切换学生账号') : ''"
      @click="onBook"
    >
      {{ selectedSlot || ctaState === 'other-teachers' ? ctaText : t2('teacherDetail.book.emptyHint', '请先选择时段') }}
    </button>

    <p class="td-booking__refund">
      <span aria-hidden="true">ⓘ</span>
      {{ t2('teacherDetail.book.refundTip', '课前 24 小时取消可全额退款') }}
    </p>
  </section>
</template>

<style scoped lang="scss">

.td-booking {
  background: var(--color-bg-card);
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6 brand.$spacing-5;
  box-shadow: brand.$shadow-card-hover; // D25: 右栏粘性卡阴影浓化
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
  border: 1px solid brand.$color-border-light;

  @media (min-width: brand.$bp-laptop) {
    padding: brand.$spacing-6;
  }

  &__price {
    display: flex;
    align-items: baseline;
  }

  &__price-hint {
    min-height: 40px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding-inline: brand.$spacing-4;
    border: 1px solid color-mix(in srgb, var(--color-primary) 22%, transparent);
    border-radius: brand.$radius-base;
    background: var(--color-primary-soft);
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-sm;
    font-weight: 700;

    &--locked {
      border-color: brand.$color-border;
      background: brand.$color-bg-strong;
      color: brand.$color-text-secondary;
    }
  }

  &__divider {
    height: 1px;
    background: brand.$color-divider;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-base;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__picker {
    // 紧凑版 picker (右栏空间窄)
    :deep(.ssp__grid) {
      gap: brand.$spacing-1;
    }

    @media (min-width: brand.$bp-laptop) {
      :deep(.ssp__grid) {
        grid-auto-flow: column;
        grid-template-columns: repeat(7, 1fr);
        overflow-x: auto;
      }
    }
  }

  &__selected {
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
    align-items: center;
    background: brand.$brand-primary-soft;
    border-radius: brand.$radius-base;
    padding: brand.$spacing-3 brand.$spacing-4;
    font-size: brand.$font-size-sm;
  }

  &__selected-label {
    color: brand.$color-text-tertiary;
  }

  &__selected-date {
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__selected-time {
    color: brand.$brand-primary-deep;
    font-weight: 600;
  }

  &__cta {
    appearance: none;
    width: 100%;
    height: 48px;
    padding: 0 brand.$spacing-6;
    background: brand.$brand-gradient;
    color: brand.$color-text-inverse;
    border: none;
    border-radius: brand.$radius-base;
    font: inherit;
    font-size: brand.$font-size-md;
    font-weight: 600;
    cursor: pointer;
    box-shadow: brand.$shadow-brand;
    transition: opacity 0.15s ease, transform 0.05s ease;

    &:disabled {
      background: brand.$brand-primary-disabled;
      box-shadow: none;
      cursor: not-allowed;
    }

    &:not(:disabled):hover {
      opacity: 0.92;
    }

    &:not(:disabled):active {
      transform: scale(0.99);
    }
  }

  &__refund {
    margin: 0;
    font-size: brand.$font-size-xs;
    color: brand.$color-text-tertiary;
    text-align: center;
  }
}
</style>
