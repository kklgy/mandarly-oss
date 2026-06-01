/**
 * useActiveSection — IntersectionObserver active section composable(Wave 5 第 12 轮 Legal 页用)
 *
 * 接收 sectionIds 数组(对应 DOM 上 id="section-xxx" 中的 xxx),
 * 返回 reactive activeId,跟随滚动自动更新到当前最显眼的章节。
 *
 *  - rootMargin '-100px 0px -60% 0px':顶部留 100px(过 AppHeader 64 + progress 4 + 呼吸 32),
 *    底部 60% 不算可见,保证 active 准确锁定阅读位置
 *  - threshold [0, 0.25, 0.5, 0.75, 1]:多 stop 平滑过渡
 *  - SSR-safe(无 window 时直接返第一个 id)
 *  - DOM 元素延迟挂载场景下 onMounted 内查找,observe 失败的 id 静默跳过
 *  - onUnmounted disconnect 防泄露
 *
 * 用法:
 *   import { useActiveSection } from '@/composables/useActiveSection'
 *   const sectionIds = ['intro', 'collect', ...]
 *   const { activeId } = useActiveSection(sectionIds, { idPrefix: 'section-' })
 *
 * 注:配套 Wave 1 router scrollBehavior `top: 100`,锚点跳转后顶部留白与 rootMargin 匹配。
 */

import { ref, onMounted, onUnmounted } from 'vue'

export function useActiveSection(sectionIds = [], options = {}) {
  const {
    idPrefix = 'section-',
    rootMargin = '-100px 0px -60% 0px',
    threshold = [0, 0.25, 0.5, 0.75, 1]
  } = options

  const activeId = ref(sectionIds[0] || '')
  let observer = null

  onMounted(() => {
    if (typeof window === 'undefined' || typeof IntersectionObserver === 'undefined') return
    if (!Array.isArray(sectionIds) || sectionIds.length === 0) return

    observer = new IntersectionObserver(
      (entries) => {
        // 取所有当前 intersecting 中可见比例最高的一项作为 active
        const visible = entries
          .filter((e) => e.isIntersecting)
          .sort((a, b) => b.intersectionRatio - a.intersectionRatio)
        if (visible.length > 0) {
          const id = visible[0].target.id
          activeId.value = id.startsWith(idPrefix) ? id.slice(idPrefix.length) : id
        }
      },
      { rootMargin, threshold }
    )

    sectionIds.forEach((id) => {
      const el = document.getElementById(`${idPrefix}${id}`)
      if (el) observer.observe(el)
    })
  })

  onUnmounted(() => {
    if (observer) {
      observer.disconnect()
      observer = null
    }
  })

  return { activeId }
}

export default useActiveSection
