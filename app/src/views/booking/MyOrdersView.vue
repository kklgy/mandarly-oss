<script setup>
// MyOrdersView — 我的订单 shell(Wave 3 第 5 轮设计稿落地)
// 设计源:docs/frontend/visual-reference/DESIGN-mandarly-v1.md §"### MyOrdersView 拆分"
// 职责:tab 状态 / URL 同步 / listMyOrders 拉数据 / 取消弹窗触发 / EmptyState
// 业务逻辑下沉到 OrderTabs / OrderCard / OrderCountdown / OrderCancelDialog 4 子组件
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { listMyOrders } from '@/api/booking'
import { getReviewByOrder } from '@/api/review'
import { useBookingCountsStore } from '@/stores/bookingCounts'
import EmptyState from '@/components/EmptyState.vue'
import OrderTabs from '@/components/booking/OrderTabs.vue'
import OrderCard from '@/components/booking/OrderCard.vue'
import OrderCancelDialog from '@/components/booking/OrderCancelDialog.vue'
import dayjs from 'dayjs'

const TABS = ['upcoming', 'toReview', 'finished', 'cancelled', 'refunding', 'all']
const REVIEW_EDIT_WINDOW_HOURS = 24

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const bookingCounts = useBookingCountsStore()

// ---------- tab 状态 + URL 同步 ----------
const activeTab = ref(
  TABS.includes(route.query.tab) ? route.query.tab : 'upcoming'
)

watch(activeTab, (tab) => {
  router.replace({ query: { ...route.query, tab } })
  load()
})

// 路径外部跳进来(?tab=xxx)同步
watch(() => route.query.tab, (q) => {
  if (q && TABS.includes(q) && q !== activeTab.value) {
    activeTab.value = q
  }
})

// ---------- 角标 counts(传给 OrderTabs) ----------
const counts = computed(() => ({
  upcoming: bookingCounts.upcomingCount,
  toReview: bookingCounts.toReviewCount,
  refunding: bookingCounts.refundingCount
}))

// ---------- 列表数据 ----------
const orders = ref([])
const loading = ref(true)
const errorMsg = ref('')

async function load() {
  errorMsg.value = ''
  // refunding tab — 后端口径:M5 Stripe webhook 推动状态机,M5 阶段一般为 0
  // 但 status='refunding' / 'refunded' 后端已有,直接按 status 过滤
  loading.value = true
  try {
    // toReview 等价于 finished 但未评(前端过滤);其它 tab 按 status 过滤
    const status = activeTab.value === 'all' ? ''
      : activeTab.value === 'toReview' ? 'finished'
      : activeTab.value === 'refunding' ? 'refunding,refunded'
      : activeTab.value
    const page = await listMyOrders({
      status,
      pageNo: 1,
      pageSize: 50,
      locale: locale.value
    })
    let list = page?.list || []
    // 富集 finished 订单的评价状态(toReview / finished / all 三个 tab 用)
    const needReview = list.some((o) => o.status === 'finished')
    if (needReview) {
      list = await enrichReviewState(list)
    }
    if (activeTab.value === 'toReview') {
      list = list.filter((o) => o._reviewState === 'none')
    }
    orders.value = list
  } catch (e) {
    errorMsg.value = e?.message || t('orders.loadFailed')
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function enrichReviewState(list) {
  const finished = list.filter((o) => o.status === 'finished')
  const states = await Promise.all(
    finished.map((o) => getReviewByOrder(o.id).catch(() => null))
  )
  const stateById = {}
  finished.forEach((o, i) => {
    const r = states[i]
    if (!r) {
      stateById[o.id] = { state: 'none' }
      return
    }
    const editLeft = dayjs(r.createTime).add(REVIEW_EDIT_WINDOW_HOURS, 'hour').diff(dayjs(), 'hour', true)
    stateById[o.id] = {
      state: editLeft > 0 ? 'exists' : 'exists_expired',
      rating: r.rating,
      editLeftHours: Math.max(0, editLeft)
    }
  })
  return list.map((o) => {
    if (o.status !== 'finished') return o
    const meta = stateById[o.id] || { state: 'none' }
    return { ...o, _reviewState: meta.state, _reviewRating: meta.rating, _reviewEditLeft: meta.editLeftHours }
  })
}

// ---------- 取消订单弹窗 ----------
const cancelDialogVisible = ref(false)
const orderToCancel = ref(null)

function openCancel(order) {
  orderToCancel.value = order
  cancelDialogVisible.value = true
}

async function onCancelled() {
  // 取消成功:刷新列表 + 强刷角标 store(plan §11.11 跨 tab stale 兜底)
  await load()
  bookingCounts.fetch(true)
}

// ---------- 生命周期 ----------
onMounted(() => {
  load()
  // 进入 /orders 走 30s 缓存(plan §11.11);store 内 visibilitychange 已自动绑定
  bookingCounts.fetch()
})
</script>

<template>
  <div class="my-orders">
    <OrderTabs v-model="activeTab" :counts="counts" />

    <main class="my-orders__list">
      <p v-if="loading" class="my-orders__state">{{ t('common.loading') }}</p>

      <div v-else-if="errorMsg" class="my-orders__state my-orders__state--error">
        <p>{{ errorMsg }}</p>
        <el-button size="default" @click="load">{{ t('common.retry') }}</el-button>
      </div>

      <EmptyState
        v-else-if="!orders.length"
        variant="no-data"
        :title="t(`orders.empty.${activeTab}.title`)"
        :description="t(`orders.empty.${activeTab}.desc`)"
        :action-text="activeTab === 'upcoming' || activeTab === 'all'
          ? t('orders.empty.browseTeachers')
          : ''"
        :action-link="activeTab === 'upcoming' || activeTab === 'all'
          ? '/teachers'
          : ''"
      />

      <ul v-else class="my-orders__cards">
        <li v-for="order in orders" :key="order.id">
          <OrderCard :order="order" @cancel="openCancel" />
        </li>
      </ul>
    </main>

    <OrderCancelDialog
      v-model="cancelDialogVisible"
      :order="orderToCancel"
      @cancelled="onCancelled"
    />
  </div>
</template>

<style lang="scss" scoped>
.my-orders {
  min-height: 100vh;
  background: brand.$color-bg-page;
}

.my-orders__list {
  max-width: 960px;
  margin: 0 auto;
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8 brand.$spacing-6;
  }
}

.my-orders__state {
  text-align: center;
  padding: brand.$spacing-12 brand.$spacing-4;
  color: brand.$color-text-secondary;

  &--error {
    color: brand.$color-error;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: brand.$spacing-3;
  }
}

.my-orders__cards {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;
}
</style>
