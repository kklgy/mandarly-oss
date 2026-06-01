<script setup>
/**
 * S6 视频课堂(LCIC iframe 嵌入,Wave 5 H 第 10 轮重写)
 *
 * 设计源:DESIGN-mandarly-v1.md § ClassroomView 课堂页改造(第 10 轮 P2-2)
 * Plan:M5-用户端UX大改造-plan.md Subagent H + § 11.9 真机测试矩阵
 *
 * 改造核心:
 *   1. 100vh / 100dvh CSS 多重声明(plan §11.9 iOS Safari ≥16.4 dvh / <16.4 vh fallback)
 *   2. 离开 confirm 双触发:
 *      - onBeforeRouteLeave(SPA 内导航) → ElMessageBox confirm
 *      - beforeunload(关 tab / 刷新)→ 浏览器原生默认警告
 *   3. iframe 加载失败自动 5s × 3 + 手动重连(:key 触发 Vue 重挂载)
 *   4. 课结束兜底 timer:lessonStart + duration + 5min,弹 confirm 让用户选
 *   5. navigator.onLine watchdog 网络断开徽章
 *   6. topbar PC 48px / H5 40px 精简,iOS safe-area 处理
 *
 * 真机测试矩阵(plan §11.9):
 *   iPhone SE3(375 无 notch)/ iPhone 15(393 home indicator)/ iPad mini 6(横屏 1133)
 *   iOS ≥ 16.4(MCP 模拟器与真机有差异,以真机为准)
 */

import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { getJoinInfo } from '@/api/classroom'
import { getOrder } from '@/api/booking'
import { useUserStore } from '@/stores/user'
import { useNetworkStatus } from '@/composables/useNetworkStatus'
import { fromUTC } from '@/utils/datetime'
import ClassroomTopbar from '@/components/classroom/ClassroomTopbar.vue'
import IframeErrorOverlay from '@/components/classroom/IframeErrorOverlay.vue'

const props = defineProps({
  /** 路由 props 注入(/classroom/:orderId,router 配 props: true) */
  orderId: { type: [String, Number], default: undefined }
})

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const userStore = useUserStore()
const { role } = storeToRefs(userStore)
const { isOnline } = useNetworkStatus()

const orderId = computed(() =>
  Number(props.orderId ?? route.params.orderId)
)

// ----- 课堂数据 -----
const orderInfo = ref(null)
const joinUrl = ref('')
const loading = ref(true)
const initialErrorMsg = ref('')

// ----- iframe 状态 -----
const iframeStatus = ref('loading')   // loading / ok / error
const iframeKey = ref(0)              // :key 变更触发 Vue 重挂载 iframe(URL 同时拼时间戳防缓存)
let iframeLoadWatchdog = null         // iframe load 监视:src 设置后 30s 还没 load 视为失败

// ----- 自动重连 -----
const MAX_AUTO_RETRY = 3
const AUTO_RETRY_INTERVAL_S = 5
const IFRAME_LOAD_TIMEOUT_MS = 30_000
const retryCount = ref(0)
const retryCountdown = ref(0)
const autoRetrying = ref(false)
let retryCountdownTimer = null

// ----- 课结束兜底 / 状态轮询 -----
const POLL_INTERVAL_MS = 5000
const FALLBACK_BUFFER_MS = 5 * 60_000
let pollTimer = null
let fallbackTimer = null

const TERMINAL_STATUSES = [
  'finished',
  'cancelled',
  'no_show_student',
  'no_show_teacher'
]

// ----- 计算 -----
const orderStatus = computed(() => orderInfo.value?.status || '')

const inLesson = computed(() => {
  // "上课中"判断:订单已加载、状态非终态、joinUrl 已就位
  return (
    !!orderInfo.value &&
    !TERMINAL_STATUSES.includes(orderStatus.value) &&
    !!joinUrl.value
  )
})

const exhaustedAutoRetry = computed(
  () => iframeStatus.value === 'error' && retryCount.value >= MAX_AUTO_RETRY
)

const iframeSrc = computed(() => {
  if (!joinUrl.value) return ''
  // 防缓存:每次重挂载附 ts 参数(iframeKey 变化触发 :key 重挂载)
  const sep = joinUrl.value.includes('?') ? '&' : '?'
  return `${joinUrl.value}${sep}_t=${iframeKey.value}`
})

const isStudent = computed(() => role.value !== 'teacher')

// ----- 生命周期 -----
onMounted(async () => {
  await loadJoinAndOrder()
  if (joinUrl.value && orderInfo.value) {
    startStatusPolling()
    setupFallbackTimer()
  }
  window.addEventListener('beforeunload', onBeforeUnload)
})

onBeforeUnmount(() => {
  cleanup()
})

// SPA 路由跳转 confirm
onBeforeRouteLeave(async () => {
  if (!inLesson.value) return true
  try {
    await ElMessageBox.confirm(
      t('classroom.leaveConfirm.message'),
      t('classroom.leaveConfirm.title'),
      {
        confirmButtonText: t('classroom.leaveConfirm.ok'),
        cancelButtonText: t('classroom.leaveConfirm.cancel'),
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    return true
  } catch {
    return false
  }
})

// 关 tab / 刷新 confirm(浏览器默认警告)
function onBeforeUnload(e) {
  if (!inLesson.value) return
  e.preventDefault()
  e.returnValue = ''
}

// 网络从离线恢复时,如当前是错误态,自动尝试 1 次手动重连
watch(isOnline, (online) => {
  if (online && iframeStatus.value === 'error' && exhaustedAutoRetry.value) {
    manualRetry()
  }
})

// ----- 加载 -----
async function loadJoinAndOrder() {
  loading.value = true
  try {
    const [join, order] = await Promise.all([
      getJoinInfo(orderId.value),
      getOrder(orderId.value, locale.value).catch(() => null)
    ])
    orderInfo.value = order || null
    joinUrl.value = join?.joinUrl || ''
    if (!joinUrl.value) {
      initialErrorMsg.value = t('classroom.error.noJoinUrl')
      iframeStatus.value = 'error'
      retryCount.value = MAX_AUTO_RETRY // 凭证缺失不自动重连,直接走手动 + 客服文案
    } else {
      iframeStatus.value = 'loading'
      armIframeLoadWatchdog()
    }
  } catch (e) {
    initialErrorMsg.value = e?.message || t('classroom.error.loadFailed')
    iframeStatus.value = 'error'
  } finally {
    loading.value = false
  }
}

// ----- iframe 事件 -----
function onIframeLoad() {
  // cross-origin iframe load 事件在腾讯云域名也会触发(只是无法读 contentWindow)
  iframeStatus.value = 'ok'
  clearIframeLoadWatchdog()
  // load 成功重置 retryCount(允许后续再次出错时重新 3 次自动)
  retryCount.value = 0
  cancelAutoRetryCountdown()
}

function onIframeError() {
  triggerIframeError()
}

function armIframeLoadWatchdog() {
  clearIframeLoadWatchdog()
  iframeLoadWatchdog = setTimeout(() => {
    if (iframeStatus.value === 'loading') {
      // 30s 还没 load,视为失败
      triggerIframeError()
    }
  }, IFRAME_LOAD_TIMEOUT_MS)
}

function clearIframeLoadWatchdog() {
  if (iframeLoadWatchdog) {
    clearTimeout(iframeLoadWatchdog)
    iframeLoadWatchdog = null
  }
}

function triggerIframeError() {
  iframeStatus.value = 'error'
  clearIframeLoadWatchdog()
  if (retryCount.value < MAX_AUTO_RETRY) {
    startAutoRetryCountdown()
  } else {
    autoRetrying.value = false
  }
}

function startAutoRetryCountdown() {
  cancelAutoRetryCountdown()
  autoRetrying.value = true
  retryCountdown.value = AUTO_RETRY_INTERVAL_S
  retryCountdownTimer = setInterval(() => {
    retryCountdown.value--
    if (retryCountdown.value <= 0) {
      cancelAutoRetryCountdown()
      retryCount.value++
      reloadIframe()
    }
  }, 1000)
}

function cancelAutoRetryCountdown() {
  if (retryCountdownTimer) {
    clearInterval(retryCountdownTimer)
    retryCountdownTimer = null
  }
  autoRetrying.value = false
  retryCountdown.value = 0
}

function reloadIframe() {
  if (!joinUrl.value) return
  iframeStatus.value = 'loading'
  iframeKey.value++ // 触发 Vue 重挂载 iframe + URL 时间戳变化防缓存
  armIframeLoadWatchdog()
}

function manualRetry() {
  cancelAutoRetryCountdown()
  retryCount.value = 0
  // 凭证缺失型错误重新拉 join
  if (!joinUrl.value) {
    loadJoinAndOrder().then(() => {
      if (joinUrl.value) {
        startStatusPolling()
        setupFallbackTimer()
      }
    })
    return
  }
  reloadIframe()
}

// ----- 课结束兜底 / 状态轮询 -----
function startStatusPolling() {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(checkStatus, POLL_INTERVAL_MS)
}

async function checkStatus() {
  try {
    const o = await getOrder(orderId.value, locale.value)
    if (!o) return
    orderInfo.value = o
    if (o.status === 'finished') {
      clearAllTimers()
      router.replace(
        isStudent.value ? `/review/${orderId.value}` : '/orders'
      )
    } else if (
      o.status === 'cancelled' ||
      o.status === 'no_show_student' ||
      o.status === 'no_show_teacher'
    ) {
      clearAllTimers()
      router.replace('/orders')
    }
  } catch {
    // 轮询失败容忍,等下一周期
  }
}

function setupFallbackTimer() {
  if (!orderInfo.value?.scheduledAt) return
  const startMs = fromUTC(orderInfo.value.scheduledAt).valueOf()
  if (Number.isNaN(startMs)) return
  const durationMin = Number(orderInfo.value.duration) || 30
  const endMs = startMs + durationMin * 60_000 + FALLBACK_BUFFER_MS
  const delay = endMs - Date.now()
  if (delay <= 0) {
    // 已经超时(用户进得晚或刷新),直接走 fallback
    onFallbackTriggered()
    return
  }
  if (fallbackTimer) clearTimeout(fallbackTimer)
  fallbackTimer = setTimeout(onFallbackTriggered, delay)
}

async function onFallbackTriggered() {
  if (TERMINAL_STATUSES.includes(orderStatus.value)) return
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  try {
    await ElMessageBox.confirm(
      t('classroom.fallback.message'),
      t('classroom.fallback.title'),
      {
        confirmButtonText: isStudent.value
          ? t('classroom.fallback.toReview')
          : t('classroom.fallback.toOrders'),
        cancelButtonText: t('classroom.fallback.continue'),
        type: 'warning'
      }
    )
    router.replace(
      isStudent.value ? `/review/${orderId.value}` : '/orders'
    )
  } catch {
    // 用户选择继续上课,重启轮询(不再设兜底)
    startStatusPolling()
  }
}

// ----- topbar 事件 -----
function onTopbarLeave() {
  // 与 onBeforeRouteLeave 走同一 confirm 路径(由 router.push 触发),减少二次重复弹窗
  router.push('/orders')
}

function onTopbarRefresh() {
  manualRetry()
}

// ----- cleanup -----
function clearAllTimers() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  if (fallbackTimer) {
    clearTimeout(fallbackTimer)
    fallbackTimer = null
  }
  cancelAutoRetryCountdown()
  clearIframeLoadWatchdog()
}

function cleanup() {
  clearAllTimers()
  window.removeEventListener('beforeunload', onBeforeUnload)
}
</script>

<template>
  <div
    class="classroom"
    :class="{ 'classroom--offline': !isOnline }"
  >
    <ClassroomTopbar
      :order-id="orderId"
      :order-info="orderInfo"
      :user-role="role || 'student'"
      :is-online="isOnline"
      :iframe-status="iframeStatus"
      @leave="onTopbarLeave"
      @refresh="onTopbarRefresh"
    />

    <main class="classroom__main">
      <div v-if="loading" class="classroom__state">
        {{ t('classroom.loading') }}
      </div>

      <iframe
        v-else-if="joinUrl"
        :key="iframeKey"
        id="lcic-iframe"
        name="lcic-iframe"
        class="classroom__iframe"
        :src="iframeSrc"
        :title="t('classroom.title')"
        allow="camera; microphone; fullscreen; display-capture; autoplay"
        @load="onIframeLoad"
        @error="onIframeError"
      />

      <IframeErrorOverlay
        v-if="iframeStatus === 'error'"
        :network-online="isOnline"
        :retry-count="retryCount"
        :max-auto-retry="MAX_AUTO_RETRY"
        :auto-retrying="autoRetrying"
        :retry-countdown="retryCountdown"
        :exhausted="exhaustedAutoRetry"
        @manual-retry="manualRetry"
        @leave="onTopbarLeave"
      />
    </main>
  </div>
</template>

<style lang="scss" scoped>
$cl-topbar-h-pc: 48px;
$cl-topbar-h-h5: 40px;

.classroom {
  /* 100dvh 多重声明(plan §11.9 + DESIGN.md 第 10 轮)
     1) 100vh:旧 Safari < 16.4 fallback
     2) 100dvh:Safari 16.4+ / Chrome 108+ / Firefox 101+ 自动覆盖,移动端 URL bar 收起不闪
     3) -webkit-fill-available:更老 iOS Safari 兜底
  */
  width: 100vw;
  height: 100vh;
  height: -webkit-fill-available;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  background: #000; /* LCIC 全屏黑底视觉协议 */
  overflow: hidden;

  /* iOS pre-16.4 兜底(@supports not 在不支持 dvh 的浏览器命中) */
  @supports not (height: 100dvh) {
    height: calc(100vh - env(safe-area-inset-bottom, 0px));
  }

  &__main {
    position: relative;
    flex: 1;
    min-height: 0;
    overflow: hidden;
    /* iOS home indicator 安全区(主体不被遮)— 由 iframe 内部 LCIC SDK 不一定知道 safe-area,父级填这层 */
    padding-bottom: env(safe-area-inset-bottom, 0px);
    background: #000;
  }

  &__iframe {
    width: 100%;
    height: 100%;
    border: 0;
    display: block;
    background: #000;
  }

  &__state {
    position: absolute;
    inset: 0;
    display: grid;
    place-items: center;
    text-align: center;
    color: var(--color-text-inverse);
    font-size: 14px;
    background: #000;
  }
}
</style>
