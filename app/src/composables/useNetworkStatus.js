/**
 * useNetworkStatus — 浏览器网络在线状态 composable
 *
 * 监听 window `online` / `offline` 事件,提供响应式 `isOnline`。
 * 用于 ClassroomView 课堂页 topbar 连接状态徽章 + IframeErrorOverlay 文案差异化。
 *
 * 限制:`navigator.onLine` 仅检测**设备级别**网络(WiFi / 4G 是否连通),
 * 无法检测远程服务(如腾讯云 LCIC)是否可达。但用户视角"我是不是断网了"
 * 通常是设备 WiFi/4G 问题,这个 API 已够用。
 *
 * 用法:
 *   import { useNetworkStatus } from '@/composables/useNetworkStatus'
 *   const { isOnline } = useNetworkStatus()
 *
 * SSR-safe(navigator 不存在时默认 true,避免水合闪烁)
 */

import { ref, onMounted, onUnmounted } from 'vue'

export function useNetworkStatus() {
  const isOnline = ref(
    typeof navigator !== 'undefined' && typeof navigator.onLine === 'boolean'
      ? navigator.onLine
      : true
  )

  function onOnline() {
    isOnline.value = true
  }

  function onOffline() {
    isOnline.value = false
  }

  onMounted(() => {
    if (typeof window === 'undefined') return
    window.addEventListener('online', onOnline)
    window.addEventListener('offline', onOffline)
  })

  onUnmounted(() => {
    if (typeof window === 'undefined') return
    window.removeEventListener('online', onOnline)
    window.removeEventListener('offline', onOffline)
  })

  return { isOnline }
}

export default useNetworkStatus
