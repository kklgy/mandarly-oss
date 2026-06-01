/**
 * stores/bookingCounts.js — 全局预约数徽章
 *
 * 数据源单一:
 *   - AppTabBar 我的课红点(upcomingCount > 0)
 *   - MyOrders OrderTabs 各 tab 数字(upcoming/toReview/refunding)
 *   - Profile 入口角标(若需)
 *
 * 30s 缓存,visibilitychange 回前台 + 关键事件(取消/评价完成/支付成功)force fetch
 *
 * 后端 endpoint: GET /app-api/edu/booking/counts
 *   响应: { upcomingCount: int, toReviewCount: int, refundingCount: int }
 *   未实现时(Wave 3 落):API 层兜底返 { ...all 0 },store 不抛
 *
 * 调用时机(plan § 11.11 + DESIGN.md § 全局徽章 Pinia store):
 *   - 用户登录成功后:fetch(true)
 *   - 路由切换到 /orders /profile /:fetch() 走 30s 缓存
 *   - 取消订单 / 评价提交 / 支付成功:fetch(true)
 *   - visibilitychange 回前台:fetch(true)(跨 tab stale 兜底)
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getBookingCounts } from '@/api/booking'
import { useUserStore } from '@/stores/user'

const CACHE_TTL = 30_000 // 30s

export const useBookingCountsStore = defineStore('bookingCounts', () => {
  // ---- state ----
  const upcomingCount = ref(0)
  const toReviewCount = ref(0)
  const refundingCount = ref(0)
  const lastFetchAt = ref(0)
  const loading = ref(false)

  // ---- computed ----
  // 总未读数(若需统一徽角)
  const totalCount = computed(
    () => upcomingCount.value + toReviewCount.value + refundingCount.value
  )

  // ---- actions ----
  /**
   * 拉数。30s 内不重复,force=true 跳过缓存。
   * 未登录态直接置 0(不调 API,避免 401 噪音)。
   */
  async function fetch(force = false) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      reset()
      return
    }
    if (!force && Date.now() - lastFetchAt.value < CACHE_TTL) return

    loading.value = true
    try {
      const data = await getBookingCounts()
      upcomingCount.value = data?.upcomingCount ?? 0
      toReviewCount.value = data?.toReviewCount ?? 0
      refundingCount.value = data?.refundingCount ?? 0
      lastFetchAt.value = Date.now()
    } finally {
      loading.value = false
    }
  }

  function reset() {
    upcomingCount.value = 0
    toReviewCount.value = 0
    refundingCount.value = 0
    lastFetchAt.value = 0
  }

  // ---- 跨 tab / 回前台时强刷(plan § 11.11) ----
  // listener 在 store 单例内一次性注册,生命周期跟随 Pinia store,避免组件 mount 时重复绑定
  if (typeof window !== 'undefined') {
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        fetch(true)
      }
    })
  }

  return {
    // state
    upcomingCount,
    toReviewCount,
    refundingCount,
    lastFetchAt,
    loading,
    // computed
    totalCount,
    // actions
    fetch,
    reset
  }
})
