<script setup>
// =============================================================================
// <OrderCard> — 单订单卡(4 主状态 + 进入课堂 4 子态 + 24h 退款边界自动切色)
// =============================================================================
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md §"### MyOrdersView 拆分" → OrderCard 多状态规格
//
// 4 主状态(根据 order.status):
//   1) upcoming    — 大头像 + 教师名 + 课程时间 + OrderCountdown + 进入课堂 4 子态 + 取消按钮
//   2) finished    — 缩略 + 教师名 + 已完成时间 + 评价 CTA(_reviewState 三态)
//   3) cancelled   — 灰底 + 教师名 + 取消时间 + 退款状态 chip
//   4) refunding   — 退款状态 + Stripe refund_id chip + 三段进度
//
// "进入课堂" 4 子态(plan §D.5):
//   > 30 min     → 倒计时 chip(灰,不可点)
//   ≤ 30 min(开课前) → 主色 CTA "进入课堂"
//   课中(now > start && now < end + 5min) → 主色实底 "重返课堂"
//   课后(now > end + 5min, < end + 30min) → 灰 chip "等待结算"
//
// 24h 退款边界(plan §D.6):
//   - 课前 > 24h:取消按钮文案 "可全额退款" + info 蓝
//   - 课前 ≤ 24h:文案 "课时不退,仅取消" + warning 黄
//   - 跨 24h 边界自动切色(reactive — OrderCountdown 内部 ref tick 即可触发)
//
// 严格约束:
//   - 0 硬编品牌色 / px / 字号(token only)
//   - 0 v-html
//   - 25 分钟课时是 baseline,实际读 order.duration(不假设 25)
// =============================================================================
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import OrderCountdown from './OrderCountdown.vue'
import { fromUTC, getUserTimezone } from '@/utils/datetime'

const props = defineProps({
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['cancel', 'review'])

const router = useRouter()
const { t, locale } = useI18n()

// ---------- 时区 + 时间格式 ----------
const tz = computed(() => getUserTimezone())

const dateFmt = computed(() => {
  if (locale.value === 'en') return 'MMM D, YYYY · HH:mm'
  if (locale.value === 'ar') return 'D MMMM YYYY · HH:mm'
  return 'YYYY年M月D日 HH:mm'
})

function formatScheduled(utcStr) {
  if (!utcStr) return ''
  return fromUTC(utcStr, tz.value).format(dateFmt.value)
}

// ---------- 当前时间 reactive(每 30s 刷新一次,用于 24h 边界 / 进入课堂 4 态) ----------
// OrderCountdown 内部已有更高频率的 timer 触发 remainingMs;但 OrderCard 自己也需要一个独立
// reactive 时钟,因为非 upcoming 状态没有 OrderCountdown 渲染,无法借助其触发。
const nowTick = ref(Date.now())
let cardTimer = null
onMounted(() => {
  cardTimer = window.setInterval(() => {
    nowTick.value = Date.now()
  }, 30_000)
})
onUnmounted(() => {
  if (cardTimer) {
    window.clearInterval(cardTimer)
    cardTimer = null
  }
})

// ---------- 头像首字母 ----------
function avatarLetter(name) {
  return (name || '?').slice(0, 1).toUpperCase()
}

// ---------- 距上课 ms / hours(reactive) ----------
const startMs = computed(() => {
  if (!props.order?.scheduledAt) return 0
  return dayjs.utc(props.order.scheduledAt).valueOf()
})
const remainingMs = computed(() => startMs.value - nowTick.value)
const remainingHours = computed(() => remainingMs.value / 3_600_000)
const durationMin = computed(() => props.order?.duration || 30)

// ---------- 进入课堂 4 态(plan §D.5) ----------
const enterClassState = computed(() => {
  if (props.order.status !== 'upcoming') return null
  const ms = remainingMs.value
  const durationMs = durationMin.value * 60_000
  // > 30 min → countdown(不可点)
  if (ms > 30 * 60_000) return 'tooEarly'
  // ≤ 30 min, > 0 → primary CTA "进入课堂"
  if (ms > 0) return 'enter'
  // 课中:now > start && now < end + 5min
  if (ms <= 0 && ms > -(durationMs + 5 * 60_000)) return 'returnToClass'
  // 课后未结算:now > end + 5min && now < end + 30min
  if (ms <= -(durationMs + 5 * 60_000) && ms > -(durationMs + 30 * 60_000)) return 'waitingSettle'
  // 再之后(> 30min 未结算)— 视为 stale,显示 "等待结算" 持续直到后端推 finished
  return 'waitingSettle'
})

const enterClassButtonText = computed(() => {
  switch (enterClassState.value) {
    case 'tooEarly':
      // 显示倒计时由 OrderCountdown 接管,这里给 fallback 文案(不渲染按钮)
      return ''
    case 'enter':
      return t('orders.card.action.enterClass')
    case 'returnToClass':
      return t('orders.card.action.returnClass')
    case 'waitingSettle':
      return t('orders.card.status.finishedSettling')
    default:
      return ''
  }
})

const enterClassClickable = computed(() =>
  enterClassState.value === 'enter' || enterClassState.value === 'returnToClass'
)

function onEnterClass() {
  if (!enterClassClickable.value) return
  router.push(`/classroom/${props.order.id}`)
}

// ---------- 24h 退款边界(plan §D.6) ----------
// > 24h:info 蓝 + "可全额退款" / ≤ 24h(>0):warning 黄 + "仅取消课时不退"
const refundWindowState = computed(() => {
  if (props.order.status !== 'upcoming') return null
  if (remainingHours.value > 24) return 'refund'
  if (remainingHours.value > 0) return 'noRefund'
  return null
})

const refundWindowText = computed(() => {
  if (refundWindowState.value === 'refund') return t('orders.refundWindow.over24h')
  if (refundWindowState.value === 'noRefund') return t('orders.refundWindow.under24h')
  return ''
})

// ---------- 状态徽章颜色映射 ----------
const statusBadge = computed(() => {
  const s = props.order.status
  return {
    label: t(`orders.card.status.${s}`),
    cls: {
      'order-card__badge': true,
      [`order-card__badge--${s}`]: true
    }
  }
})

// ---------- finished 子态 → 评价按钮 ----------
const reviewState = computed(() => props.order._reviewState || 'none')

function onWriteReview() {
  emit('review', props.order)
  router.push(`/review/${props.order.id}`)
}

// ---------- cancel 触发(交父级处理弹窗) ----------
function onCancelClick() {
  emit('cancel', props.order)
}

// ---------- refunding 三段式进度 ----------
// M4 后端预留:order.refundStatus ∈ ['applied','reviewing','refunded'],缺省 'applied'
const refundStage = computed(() => props.order.refundStatus || 'applied')
const refundStages = ['applied', 'reviewing', 'refunded']
const refundStageIndex = computed(() => refundStages.indexOf(refundStage.value))
</script>

<template>
  <article
    class="order-card"
    :class="`order-card--${order.status}`"
    :aria-label="`#${order.id}`"
  >
    <!-- ---------- header: 头像 + 教师 + 状态 badge ---------- -->
    <header class="order-card__head">
      <div class="order-card__teacher">
        <div class="order-card__avatar" aria-hidden="true">
          {{ avatarLetter(order.teacherNickname) }}
        </div>
        <div class="order-card__teacher-meta">
          <p class="order-card__name">
            {{ order.teacherNickname || `#${order.teacherId}` }}
          </p>
          <p class="order-card__id">#{{ order.id }}</p>
        </div>
      </div>
      <span :class="statusBadge.cls">{{ statusBadge.label }}</span>
    </header>

    <!-- ---------- body: 课程信息 ---------- -->
    <dl class="order-card__rows">
      <div class="order-card__row">
        <dt>{{ t('orders.card.scheduledAt') }}</dt>
        <dd>
          <div>{{ formatScheduled(order.scheduledAt) }}</div>
          <div class="order-card__tz">
            {{ t('orders.card.tzSuffix', { tz }) }}
            · {{ t('orders.card.duration', { n: durationMin }) }}
          </div>
        </dd>
      </div>
      <div class="order-card__row">
        <dt>{{ t('orders.card.package') }}</dt>
        <dd>
          {{ order.packageName || t('orders.card.packageFallback') }}
          <span v-if="order.isFreeTrial" class="order-card__inline-badge">
            {{ t('orders.card.freeTrialBadge') }}
          </span>
        </dd>
      </div>

      <!-- cancelled 子卡 -->
      <div v-if="order.status === 'cancelled'" class="order-card__row">
        <dt>{{ t('orders.card.cancelReason') }}</dt>
        <dd>
          <div class="order-card__reason">{{ order.cancelReason || '—' }}</div>
          <div
            class="order-card__refund-flag"
            :class="order.isRefundedClass
              ? 'order-card__refund-flag--ok'
              : 'order-card__refund-flag--no'"
          >
            {{ order.isRefundedClass
              ? t('orders.card.refundedClass')
              : t('orders.card.notRefundedClass') }}
          </div>
        </dd>
      </div>

      <!-- refunding 三段进度 -->
      <div v-if="order.status === 'refunding' || order.status === 'refunded'" class="order-card__row">
        <dt>{{ t('orders.card.refundProgress') }}</dt>
        <dd>
          <ol class="order-card__refund-progress" aria-label="refund progress">
            <li
              v-for="(stage, i) in refundStages"
              :key="stage"
              class="order-card__refund-step"
              :class="{
                'order-card__refund-step--done': i <= refundStageIndex,
                'order-card__refund-step--current': i === refundStageIndex
              }"
            >
              <span class="order-card__refund-dot" />
              <span class="order-card__refund-label">{{ t(`orders.refundProgress.${stage}`) }}</span>
            </li>
          </ol>
          <div v-if="order.channelRefundId" class="order-card__refund-meta">
            {{ t('orders.card.channelRefundId') }}: <code>{{ order.channelRefundId }}</code>
          </div>
        </dd>
      </div>
    </dl>

    <!-- ---------- upcoming 提示区:倒计时 + 24h 退款窗口 ---------- -->
    <div v-if="order.status === 'upcoming'" class="order-card__hint">
      <OrderCountdown
        v-if="enterClassState === 'tooEarly'"
        :target-time="order.scheduledAt"
        role="startCountdown"
      />
      <span
        v-if="refundWindowState === 'refund'"
        class="order-card__refund-window order-card__refund-window--info"
      >
        {{ refundWindowText }}
      </span>
      <span
        v-else-if="refundWindowState === 'noRefund'"
        class="order-card__refund-window order-card__refund-window--warn"
      >
        {{ refundWindowText }}
      </span>
    </div>

    <!-- ---------- footer: 操作按钮 ---------- -->
    <!-- upcoming -->
    <footer v-if="order.status === 'upcoming'" class="order-card__foot">
      <el-button
        plain
        type="danger"
        size="default"
        @click="onCancelClick"
      >
        {{ t('orders.card.action.cancel') }}
      </el-button>
      <el-button
        v-if="enterClassState === 'enter' || enterClassState === 'returnToClass'"
        type="primary"
        size="default"
        @click="onEnterClass"
      >
        {{ enterClassButtonText }}
      </el-button>
      <el-button
        v-else-if="enterClassState === 'waitingSettle'"
        disabled
        size="default"
      >
        {{ t('orders.card.status.finishedSettling') }}
      </el-button>
    </footer>

    <!-- finished -->
    <footer v-else-if="order.status === 'finished'" class="order-card__foot">
      <el-button
        v-if="reviewState === 'none'"
        type="primary"
        size="default"
        @click="onWriteReview"
      >
        {{ t('orders.card.action.review') }}
      </el-button>
      <el-button
        v-else-if="reviewState === 'exists'"
        plain
        size="default"
        @click="onWriteReview"
      >
        ★ {{ order._reviewRating || '' }} · {{ t('orders.card.action.editReview') }}
      </el-button>
      <el-button v-else disabled size="default">
        ★ {{ order._reviewRating || '' }} · {{ t('orders.card.status.reviewExpired') }}
      </el-button>
    </footer>
  </article>
</template>

<style lang="scss" scoped>
.order-card {
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-card;
  padding: brand.$spacing-4;
  box-shadow: brand.$shadow-base;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-3;
  transition: box-shadow 0.18s ease, border-color 0.18s ease;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-6;
  }

  &:hover {
    box-shadow: brand.$shadow-md;
  }

  // 4 主状态视觉差异
  &--cancelled {
    background: brand.$color-bg-page;
    opacity: 0.92;
  }
  &--refunding,
  &--refunded {
    border-color: brand.$brand-primary-soft;
  }
}

// ---------- header ----------
.order-card__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: brand.$spacing-3;
}

.order-card__teacher {
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;
  min-width: 0;
}

.order-card__avatar {
  width: 56px;
  height: 56px;
  border-radius: brand.$radius-full;
  background: brand.$brand-gradient;
  color: brand.$color-text-inverse;
  font-size: brand.$font-size-xl;
  font-weight: 700;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.order-card--finished .order-card__avatar,
.order-card--cancelled .order-card__avatar {
  width: 44px;
  height: 44px;
  font-size: brand.$font-size-lg;
}

.order-card__teacher-meta { min-width: 0; }

.order-card__name {
  margin: 0;
  font-size: brand.$font-size-md;
  font-weight: 600;
  color: brand.$color-text-primary;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  max-width: 240px;
}

.order-card__id {
  margin: 2px 0 0;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}

// ---------- 状态徽章(主色 / 中性 / 警示 / 错误) ----------
.order-card__badge {
  flex-shrink: 0;
  padding: 2px brand.$spacing-2;
  border-radius: brand.$radius-pill;
  font-size: brand.$font-size-xs;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;

  &--upcoming {
    background: brand.$brand-primary;
    color: brand.$color-text-inverse;
  }
  &--finished {
    background: rgba(82, 196, 26, 0.12);
    color: brand.$color-success;
  }
  &--cancelled {
    background: brand.$color-divider;
    color: brand.$color-text-tertiary;
  }
  &--refunding,
  &--refunded {
    background: rgba(250, 173, 20, 0.14);
    color: brand.$color-warning;
  }
  &--no_show_student,
  &--no_show_teacher,
  &--abnormal {
    background: rgba(255, 77, 79, 0.12);
    color: brand.$color-error;
  }
}

// ---------- rows ----------
.order-card__rows {
  margin: 0;
  display: flex;
  flex-direction: column;
}

.order-card__row {
  display: flex;
  gap: brand.$spacing-4;
  padding: brand.$spacing-2 0;
  border-block-start: 1px solid brand.$color-divider;
  font-size: brand.$font-size-sm;
  margin: 0;

  &:first-child { border-block-start: none; }

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

.order-card__tz {
  margin-block-start: 2px;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}

.order-card__inline-badge {
  margin-inline-start: brand.$spacing-2;
  font-size: brand.$font-size-xs;
  background: brand.$color-success;
  color: brand.$color-text-inverse;
  padding: 2px brand.$spacing-2;
  border-radius: brand.$radius-sm;
  font-weight: 500;
}

.order-card__reason {
  color: brand.$color-text-body;
  font-size: brand.$font-size-sm;
}

.order-card__refund-flag {
  margin-block-start: 4px;
  display: inline-block;
  padding: 2px brand.$spacing-2;
  border-radius: brand.$radius-sm;
  font-size: brand.$font-size-xs;
  font-weight: 500;

  &--ok {
    background: rgba(82, 196, 26, 0.12);
    color: brand.$color-success;
  }
  &--no {
    background: rgba(255, 77, 79, 0.10);
    color: brand.$color-error;
  }
}

// ---------- refunding 三段进度条 ----------
.order-card__refund-progress {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  gap: brand.$spacing-2;
}

.order-card__refund-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-1;
  flex: 1;
  position: relative;

  & + & {
    &::before {
      content: '';
      position: absolute;
      inset-inline-start: -50%;
      inset-block-start: 6px;
      width: 100%;
      height: 2px;
      background: brand.$color-border;
      z-index: 0;
    }
  }

  &--done + .order-card__refund-step::before,
  &--done::before {
    background: brand.$brand-primary;
  }
}

.order-card__refund-dot {
  width: 14px;
  height: 14px;
  border-radius: brand.$radius-full;
  background: brand.$color-border;
  border: 2px solid brand.$color-bg-card;
  position: relative;
  z-index: 1;

  .order-card__refund-step--done & {
    background: brand.$brand-primary;
  }
  .order-card__refund-step--current & {
    box-shadow: brand.$ring-focus;
  }
}

.order-card__refund-label {
  font-size: brand.$font-size-xs;
  color: brand.$color-text-secondary;

  .order-card__refund-step--current & {
    color: brand.$brand-primary-deep;
    font-weight: 600;
  }
}

.order-card__refund-meta {
  margin-block-start: brand.$spacing-2;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;

  code {
    font-family: brand.$font-family-mono;
    background: brand.$color-bg-strong;
    padding: 1px brand.$spacing-1;
    border-radius: brand.$radius-sm;
  }
}

// ---------- 提示区 ----------
.order-card__hint {
  display: flex;
  flex-wrap: wrap;
  gap: brand.$spacing-2;
  align-items: center;
}

.order-card__refund-window {
  display: inline-flex;
  align-items: center;
  padding: brand.$spacing-1 brand.$spacing-3;
  border-radius: brand.$radius-pill;
  font-size: brand.$font-size-sm;
  line-height: brand.$line-height-tight;

  &--info {
    background: rgba(24, 144, 255, 0.10);
    color: brand.$color-info;
  }
  &--warn {
    background: rgba(250, 173, 20, 0.14);
    color: brand.$color-warning;
  }
}

// ---------- footer ----------
.order-card__foot {
  display: flex;
  gap: brand.$spacing-2;
  justify-content: flex-end;
  flex-wrap: wrap;
  border-block-start: 1px solid brand.$color-divider;
  padding-block-start: brand.$spacing-3;
}
</style>
