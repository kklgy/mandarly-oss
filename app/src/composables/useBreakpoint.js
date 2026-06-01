/**
 * useBreakpoint — 响应式断点 composable
 *
 * Mandarly 用户端 PC / H5 切换分水岭固定 768px(brand.$bp-tablet)
 *  - 提供 reactive `isPC` / `isH5`,组件用 `v-if` 切换 PC / H5 视图,或让条件 prop 走数据流
 *  - SSR-safe(window 不存在时默认 isPC=true,避免水合闪烁)
 *  - 监听 resize 自动同步,onUnmounted 清理
 *
 * 用法:
 *   import { useBreakpoint } from '@/composables/useBreakpoint'
 *   const { isPC, isH5 } = useBreakpoint()
 *
 * 想自定义断点:
 *   useBreakpoint(1024)  // ≥1024 视为 PC
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'

export function useBreakpoint(threshold = 768) {
  const isPC = ref(
    typeof window !== 'undefined' ? window.innerWidth >= threshold : true
  )

  let listener = null

  onMounted(() => {
    listener = () => {
      isPC.value = window.innerWidth >= threshold
    }
    window.addEventListener('resize', listener, { passive: true })
  })

  onUnmounted(() => {
    if (listener) window.removeEventListener('resize', listener)
  })

  return {
    isPC,
    isH5: computed(() => !isPC.value)
  }
}

export default useBreakpoint
