<script setup>
// =============================================================================
// <OrderCancelDialog> — 取消订单确认弹窗(PC EP Dialog / H5 EP Drawer 80vh)
// =============================================================================
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md §"OrderCancelDialog 规格"
//
// 实现要点(plan §D.7):
//   - PC ≥ 768:<el-dialog width="480px"> 居中
//   - H5 < 768:<el-drawer placement="bottom" size="80vh" + 顶部 4px drag handle>
//   - 24h 边界文案自动切色(reactive,跟随 isMobile / countdown 同时刷新)
//   - 5 项常见取消原因 + "其它"(可选,plan 不强制 — 这里实现作为最佳体验)
//   - 提交后 close + ElMessage toast + emit('cancelled') 父级处理刷新
//   - 父级负责 store.fetch(true) 强刷角标
// =============================================================================
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { fromUTC, getUserTimezone } from '@/utils/datetime'
import { cancelOrder } from '@/api/booking'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  // 当前要取消的 order 对象;关闭时父级置 null,本组件保留最后一次 snapshot 用于动画结束前展示
  order: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'cancelled'])

const { t, locale } = useI18n()

// 24h 边界缓冲:UI 阈值比后端略保守 5 分钟,避免「弹窗显示将返还、点完确认时已不足 24h」的误导
const REFUND_BUFFER_HOURS = 24 + 5 / 60

// ---------- 响应式断点 ----------
// 不依赖 @vueuse/core,用 matchMedia 手写
const isMobile = ref(false)
let mql = null
function syncMql(e) {
  isMobile.value = e.matches
}
onMounted(() => {
  if (typeof window === 'undefined' || !window.matchMedia) return
  mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  if (mql.addEventListener) mql.addEventListener('change', syncMql)
  else mql.addListener(syncMql) // legacy Safari
})
onUnmounted(() => {
  if (!mql) return
  if (mql.removeEventListener) mql.removeEventListener('change', syncMql)
  else mql.removeListener(syncMql)
})

// ---------- 时间 ----------
const tz = computed(() => getUserTimezone())
const dateFmt = computed(() => {
  if (locale.value === 'en') return 'MMM D, YYYY · HH:mm'
  if (locale.value === 'ar') return 'D MMMM YYYY · HH:mm'
  return 'YYYY年M月D日 HH:mm'
})

// 现在时间 reactive — 30s tick 一次,够用于跨 24h 边界的状态切换
const nowMs = ref(Date.now())
let nowTimer = null
onMounted(() => {
  nowTimer = window.setInterval(() => {
    nowMs.value = Date.now()
  }, 30_000)
})
onUnmounted(() => {
  if (nowTimer) {
    window.clearInterval(nowTimer)
    nowTimer = null
  }
})

const remainingMs = computed(() => {
  if (!props.order?.scheduledAt) return 0
  return dayjs.utc(props.order.scheduledAt).valueOf() - nowMs.value
})
const remainingHours = computed(() => remainingMs.value / 3_600_000)

const policyState = computed(() => {
  if (!props.order) return 'unknown'
  if (remainingMs.value <= 0) return 'started'
  if (remainingHours.value >= REFUND_BUFFER_HOURS) return 'refund'
  return 'noRefund'
})

const canSubmit = computed(() => policyState.value !== 'started' && !submitting.value)

// "距上课 X 天 Y 小时" 文案(弹窗内不复用 OrderCountdown — 不需秒级刷新)
const distanceText = computed(() => {
  const ms = remainingMs.value
  if (ms <= 0) return t('orders.cancelDialog.alreadyStarted')
  const totalSec = Math.floor(ms / 1000)
  const days = Math.floor(totalSec / 86400)
  const hours = Math.floor((totalSec % 86400) / 3600)
  const minutes = Math.floor((totalSec % 3600) / 60)
  if (days >= 1) return t('orders.countdown.daysHours', { days, hours })
  if (hours >= 1) return t('orders.countdown.hoursMinutes', { hours, minutes })
  return t('orders.countdown.minutesSeconds', {
    minutes: String(minutes).padStart(2, '0'),
    seconds: '00'
  })
})

function formatScheduled(utcStr) {
  if (!utcStr) return ''
  return fromUTC(utcStr, tz.value).format(dateFmt.value)
}

// ---------- 取消原因(可选 select,5 常见 + 其它 + 备注) ----------
const REASON_OPTIONS = ['conflict', 'illness', 'travel', 'noShow', 'changedMind', 'other']
const reasonKey = ref('')
const reasonNote = ref('')

const submittedReason = computed(() => {
  if (!reasonKey.value) return ''
  if (reasonKey.value === 'other') return reasonNote.value.trim() || t('orders.cancelDialog.reasonOther')
  return t(`orders.cancelDialog.reason.${reasonKey.value}`) +
    (reasonNote.value.trim() ? ` — ${reasonNote.value.trim()}` : '')
})

// ---------- 提交 ----------
const submitting = ref(false)
const errorMsg = ref('')

async function submit() {
  if (!canSubmit.value || !props.order) return
  submitting.value = true
  errorMsg.value = ''
  try {
    await cancelOrder(props.order.id, submittedReason.value)
    ElMessage.success(t('orders.cancelDialog.success'))
    emit('cancelled', props.order)
    close()
  } catch (e) {
    errorMsg.value = e?.message || t('orders.cancelDialog.error')
  } finally {
    submitting.value = false
  }
}

function close() {
  if (submitting.value) return
  emit('update:modelValue', false)
}

// 切换 order(打开新弹窗)时清空表单
watch(() => props.order?.id, () => {
  reasonKey.value = ''
  reasonNote.value = ''
  errorMsg.value = ''
})

// ---------- 同一段表单 markup,PC / H5 通过外壳决定挂在 dialog 还是 drawer ----------
// 不抽 SFC 子组件以减少文件数;两段 template 内容相同。
</script>

<template>
  <!-- ============ PC ≥ 768 : el-dialog 480px 居中 ============ -->
  <el-dialog
    v-if="!isMobile"
    :model-value="modelValue"
    :title="t('orders.cancelDialog.title')"
    :width="480"
    :close-on-click-modal="!submitting"
    :close-on-press-escape="!submitting"
    :show-close="!submitting"
    align-center
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-if="order" class="cancel-dialog__body">
      <section class="cancel-dialog__summary">
        <div class="cancel-dialog__sumrow">
          <span class="cancel-dialog__sumlabel">{{ t('orders.cancelDialog.summary.teacher') }}</span>
          <span>{{ order.teacherNickname || `#${order.teacherId}` }}</span>
        </div>
        <div class="cancel-dialog__sumrow">
          <span class="cancel-dialog__sumlabel">{{ t('orders.cancelDialog.summary.time') }}</span>
          <span>{{ formatScheduled(order.scheduledAt) }}</span>
        </div>
        <div class="cancel-dialog__sumrow">
          <span class="cancel-dialog__sumlabel">{{ t('orders.cancelDialog.summary.distance') }}</span>
          <span>{{ distanceText }}</span>
        </div>
      </section>

      <p
        v-if="policyState === 'refund'"
        class="cancel-dialog__callout cancel-dialog__callout--ok"
      >
        {{ t('orders.cancelDialog.countdown24Plus') }}
      </p>
      <p
        v-else-if="policyState === 'noRefund'"
        class="cancel-dialog__callout cancel-dialog__callout--warn"
      >
        {{ t('orders.cancelDialog.countdown24Minus') }}
      </p>
      <p
        v-else-if="policyState === 'started'"
        class="cancel-dialog__callout cancel-dialog__callout--err"
      >
        {{ t('orders.cancelDialog.alreadyStarted') }}
      </p>

      <div v-if="canSubmit || policyState === 'noRefund'" class="cancel-dialog__form">
        <label class="cancel-dialog__label">
          {{ t('orders.cancelDialog.reasonLabel') }}
        </label>
        <el-select
          v-model="reasonKey"
          :placeholder="t('orders.cancelDialog.reasonPlaceholder')"
          :disabled="submitting"
          class="cancel-dialog__select"
        >
          <el-option
            v-for="key in REASON_OPTIONS"
            :key="key"
            :value="key"
            :label="key === 'other'
              ? t('orders.cancelDialog.reasonOther')
              : t(`orders.cancelDialog.reason.${key}`)"
          />
        </el-select>
        <el-input
          v-if="reasonKey"
          v-model="reasonNote"
          type="textarea"
          :rows="3"
          :maxlength="200"
          show-word-limit
          :placeholder="t('orders.cancelDialog.notePlaceholder')"
          :disabled="submitting"
          class="cancel-dialog__textarea"
        />
      </div>

      <p v-if="errorMsg" class="cancel-dialog__error">{{ errorMsg }}</p>
    </div>

    <template #footer>
      <el-button :disabled="submitting" @click="close">
        {{ t('orders.cancelDialog.cancel') }}
      </el-button>
      <el-button
        type="danger"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="submit"
      >
        {{ submitting
          ? t('orders.cancelDialog.confirming')
          : t('orders.cancelDialog.confirm') }}
      </el-button>
    </template>
  </el-dialog>

  <!-- ============ H5 < 768 : el-drawer placement=bottom 80vh ============ -->
  <el-drawer
    v-else
    :model-value="modelValue"
    direction="btt"
    :size="'80vh'"
    :title="t('orders.cancelDialog.title')"
    :close-on-click-modal="!submitting"
    :close-on-press-escape="!submitting"
    :show-close="!submitting"
    :with-header="true"
    class="cancel-dialog__drawer"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <!-- 顶部 drag handle(EP drawer 默认无,自加) -->
    <div class="cancel-dialog__handle" aria-hidden="true" />

    <div v-if="order" class="cancel-dialog__body">
      <section class="cancel-dialog__summary">
        <div class="cancel-dialog__sumrow">
          <span class="cancel-dialog__sumlabel">{{ t('orders.cancelDialog.summary.teacher') }}</span>
          <span>{{ order.teacherNickname || `#${order.teacherId}` }}</span>
        </div>
        <div class="cancel-dialog__sumrow">
          <span class="cancel-dialog__sumlabel">{{ t('orders.cancelDialog.summary.time') }}</span>
          <span>{{ formatScheduled(order.scheduledAt) }}</span>
        </div>
        <div class="cancel-dialog__sumrow">
          <span class="cancel-dialog__sumlabel">{{ t('orders.cancelDialog.summary.distance') }}</span>
          <span>{{ distanceText }}</span>
        </div>
      </section>

      <p
        v-if="policyState === 'refund'"
        class="cancel-dialog__callout cancel-dialog__callout--ok"
      >
        {{ t('orders.cancelDialog.countdown24Plus') }}
      </p>
      <p
        v-else-if="policyState === 'noRefund'"
        class="cancel-dialog__callout cancel-dialog__callout--warn"
      >
        {{ t('orders.cancelDialog.countdown24Minus') }}
      </p>
      <p
        v-else-if="policyState === 'started'"
        class="cancel-dialog__callout cancel-dialog__callout--err"
      >
        {{ t('orders.cancelDialog.alreadyStarted') }}
      </p>

      <div v-if="canSubmit || policyState === 'noRefund'" class="cancel-dialog__form">
        <label class="cancel-dialog__label">
          {{ t('orders.cancelDialog.reasonLabel') }}
        </label>
        <el-select
          v-model="reasonKey"
          :placeholder="t('orders.cancelDialog.reasonPlaceholder')"
          :disabled="submitting"
          class="cancel-dialog__select"
        >
          <el-option
            v-for="key in REASON_OPTIONS"
            :key="key"
            :value="key"
            :label="key === 'other'
              ? t('orders.cancelDialog.reasonOther')
              : t(`orders.cancelDialog.reason.${key}`)"
          />
        </el-select>
        <el-input
          v-if="reasonKey"
          v-model="reasonNote"
          type="textarea"
          :rows="3"
          :maxlength="200"
          show-word-limit
          :placeholder="t('orders.cancelDialog.notePlaceholder')"
          :disabled="submitting"
          class="cancel-dialog__textarea"
        />
      </div>

      <p v-if="errorMsg" class="cancel-dialog__error">{{ errorMsg }}</p>
    </div>

    <template #footer>
      <div class="cancel-dialog__drawer-foot">
        <el-button :disabled="submitting" class="cancel-dialog__drawer-btn" @click="close">
          {{ t('orders.cancelDialog.cancel') }}
        </el-button>
        <el-button
          type="danger"
          :loading="submitting"
          :disabled="!canSubmit"
          class="cancel-dialog__drawer-btn"
          @click="submit"
        >
          {{ submitting
            ? t('orders.cancelDialog.confirming')
            : t('orders.cancelDialog.confirm') }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style lang="scss" scoped>
.cancel-dialog__body {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
}

.cancel-dialog__summary {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
  background: brand.$color-bg-page;
  padding: brand.$spacing-3 brand.$spacing-4;
  border-radius: brand.$radius-base;
}

.cancel-dialog__sumrow {
  display: flex;
  justify-content: space-between;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-primary;
}

.cancel-dialog__sumlabel {
  color: brand.$color-text-tertiary;
}

.cancel-dialog__callout {
  margin: 0;
  padding: brand.$spacing-3 brand.$spacing-4;
  border-radius: brand.$radius-base;
  font-size: brand.$font-size-sm;
  line-height: brand.$line-height-base;

  &--ok {
    background: rgba(82, 196, 26, 0.10);
    color: brand.$color-success;
  }
  &--warn {
    background: rgba(250, 173, 20, 0.12);
    color: brand.$color-warning;
  }
  &--err {
    background: rgba(255, 77, 79, 0.10);
    color: brand.$color-error;
  }
}

.cancel-dialog__form {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
}

.cancel-dialog__label {
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
}

.cancel-dialog__select {
  width: 100%;
}

.cancel-dialog__textarea {
  width: 100%;
}

.cancel-dialog__error {
  margin: 0;
  font-size: brand.$font-size-sm;
  color: brand.$color-error;
}

// ---------- H5 drawer 增强 ----------
.cancel-dialog__drawer {
  // EP drawer header 让位给 drag handle:padding-top 给点空间
  :deep(.el-drawer__header) {
    margin-block-end: brand.$spacing-3;
    padding-block-start: brand.$spacing-2;
  }
}

.cancel-dialog__handle {
  width: 40px;
  height: 4px;
  border-radius: brand.$radius-pill;
  background: brand.$color-border-strong;
  margin: brand.$spacing-2 auto brand.$spacing-1;
  opacity: 0.6;
}

.cancel-dialog__drawer-foot {
  display: flex;
  gap: brand.$spacing-3;
}

.cancel-dialog__drawer-btn {
  flex: 1;
}
</style>
