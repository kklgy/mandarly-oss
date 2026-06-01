<script setup>
// =============================================================================
// <OrderCountdown> — 智能切换 interval 的倒计时组件
// =============================================================================
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md §"### MyOrdersView 拆分" → OrderCountdown
//
// 性能优化(plan §D.4):
//   - > 1h:setInterval 60s 更新
//   - ≤ 1h, > 0:setInterval 1s(显秒)
//   - 跨过 1h 边界时自动切频(只发生一次)
//   - ≤ 0:emit('expired') + 停止 timer
//   - watch targetTime 变化重新建 interval
//   - onUnmounted 清理
//
// 显示格式(i18n via $t):
//   > 24h:    {days} 天 {hours} 小时
//   1-24h:    {hours} 小时 {minutes} 分
//   ≤ 1h:     {minutes} 分 {seconds} 秒
//   ≤ 0:     不渲染 — 由父级根据 expired 事件接管
//
// role 区分(plan):
//   - startCountdown   → 距上课倒计时(主要使用,中性灰底)
//   - cancelDeadline   → 24h 取消窗口倒计时(取消弹窗内可复用,警示黄底)
// =============================================================================
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import { Clock } from '@element-plus/icons-vue'

dayjs.extend(utc)

const props = defineProps({
  // ISO UTC 字符串(后端 scheduledAt)
  targetTime: {
    type: String,
    required: true
  },
  role: {
    type: String,
    default: 'startCountdown',
    validator: (v) => ['startCountdown', 'cancelDeadline'].includes(v)
  }
})

const emit = defineEmits(['expired'])

const { t } = useI18n()

// 当前时间 ms(reactive — interval 更新触发 remaining 重算 → display 重算)
const nowMs = ref(Date.now())
let timerId = null
let currentIntervalMs = 0
let expiredEmitted = false

const targetMs = computed(() => dayjs.utc(props.targetTime).valueOf())
const remainingMs = computed(() => targetMs.value - nowMs.value)

const display = computed(() => {
  const ms = remainingMs.value
  if (ms <= 0) return ''
  const totalSec = Math.floor(ms / 1000)
  const days = Math.floor(totalSec / 86400)
  const hours = Math.floor((totalSec % 86400) / 3600)
  const minutes = Math.floor((totalSec % 3600) / 60)
  const seconds = totalSec % 60

  if (days >= 1) {
    return t('orders.countdown.daysHours', { days, hours })
  }
  if (hours >= 1) {
    return t('orders.countdown.hoursMinutes', { hours, minutes })
  }
  return t('orders.countdown.minutesSeconds', {
    minutes: String(minutes).padStart(2, '0'),
    seconds: String(seconds).padStart(2, '0')
  })
})

function startTimer(intervalMs) {
  stopTimer()
  currentIntervalMs = intervalMs
  timerId = window.setInterval(tick, intervalMs)
}

function stopTimer() {
  if (timerId !== null) {
    window.clearInterval(timerId)
    timerId = null
    currentIntervalMs = 0
  }
}

function tick() {
  nowMs.value = Date.now()
  const ms = remainingMs.value

  if (ms <= 0) {
    stopTimer()
    if (!expiredEmitted) {
      expiredEmitted = true
      emit('expired')
    }
    return
  }

  // 跨过 1h 边界时把 60s 切到 1s(只切换一次,避免每秒比对)
  const desired = ms > 60 * 60 * 1000 ? 60_000 : 1_000
  if (desired !== currentIntervalMs) {
    startTimer(desired)
  }
}

function rebuild() {
  stopTimer()
  expiredEmitted = false
  nowMs.value = Date.now()
  const ms = remainingMs.value
  if (ms <= 0) {
    if (!expiredEmitted) {
      expiredEmitted = true
      emit('expired')
    }
    return
  }
  const intervalMs = ms > 60 * 60 * 1000 ? 60_000 : 1_000
  startTimer(intervalMs)
}

onMounted(rebuild)
onUnmounted(stopTimer)

watch(() => props.targetTime, rebuild)

// 暴露给父级(OrderCard 用 remainingMs 判 24h 边界 + 30min 进入课堂边界)
defineExpose({ remainingMs })
</script>

<template>
  <span
    v-if="remainingMs > 0"
    class="order-countdown"
    :class="`order-countdown--${role}`"
    role="timer"
    :aria-label="display"
  >
    <el-icon class="order-countdown__icon"><Clock /></el-icon>
    <span class="order-countdown__text">{{ display }}</span>
  </span>
</template>

<style lang="scss" scoped>
.order-countdown {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-1;
  padding: brand.$spacing-1 brand.$spacing-2;
  border-radius: brand.$radius-pill;
  background: brand.$color-bg-strong;
  color: brand.$color-text-secondary;
  font-size: brand.$font-size-sm;
  line-height: brand.$line-height-tight;
  font-variant-numeric: tabular-nums;

  &--cancelDeadline {
    background: rgba(250, 173, 20, 0.12);
    color: brand.$color-warning;
  }
}

.order-countdown__icon {
  font-size: brand.$font-size-base;
}

.order-countdown__text {
  white-space: nowrap;
}
</style>
