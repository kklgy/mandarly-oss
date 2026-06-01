/**
 * useScrollProgress — 阅读进度 composable(Wave 5 第 12 轮 Legal 页用)
 *
 * 监听 window scroll,返回 0-100 的滚动百分比 ref.
 *  - SSR-safe(无 window 时默认 0)
 *  - passive 监听,不阻塞滚动
 *  - onMounted 立即计算一次,避免初始 0% 闪烁
 *  - onUnmounted 自动清理
 *
 * 用法:
 *   import { useScrollProgress } from '@/composables/useScrollProgress'
 *   const { progress } = useScrollProgress()
 *   // <div :style="{ width: progress + '%' }" />
 *
 * 注:进度条容器自行处理 v-show progress < 1 隐藏(避免顶部就有进度感)。
 */

import { ref, onMounted, onUnmounted } from 'vue'

const EPSILON = 0.5  // 进度变化 < 0.5% 不触发响应式更新

export function useScrollProgress() {
  const progress = ref(0)
  let rafId = 0

  function compute() {
    rafId = 0
    if (typeof window === 'undefined') return
    const scrolled = window.scrollY || document.documentElement.scrollTop || 0
    const max = (document.documentElement.scrollHeight || 0) - (window.innerHeight || 0)
    const next = max > 0 ? Math.min(100, Math.max(0, (scrolled / max) * 100)) : 0
    if (Math.abs(next - progress.value) >= EPSILON) progress.value = next
  }

  // rAF 节流:scroll/resize 高频事件合并到下一帧,避免 reactive 风暴
  function update() {
    if (rafId) return
    rafId = requestAnimationFrame(compute)
  }

  onMounted(() => {
    compute()
    window.addEventListener('scroll', update, { passive: true })
    window.addEventListener('resize', update, { passive: true })
  })

  onUnmounted(() => {
    if (typeof window === 'undefined') return
    if (rafId) cancelAnimationFrame(rafId)
    window.removeEventListener('scroll', update)
    window.removeEventListener('resize', update)
  })

  return { progress, update }
}

export default useScrollProgress
