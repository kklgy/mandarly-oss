/**
 * authChannels.js — 4 渠道凭证就位状态(M2.5 §6.5 toggle)
 *
 * 应用启动时拉一次 `/app-api/system/auth/channels`,缓存在内存。
 * LoginView / RegisterView / TeacherRegisterView 据此 disable 不可用渠道。
 *
 * fetch 失败时所有 channel 默认 true(降级 = 现状,不阻塞用户尝试)。
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAuthChannels } from '@/api/auth'

export const useAuthChannelsStore = defineStore('authChannels', () => {
  const channels = ref({ email: true, sms: true, google: true, apple: true })
  const loaded = ref(false)
  const loading = ref(false)

  async function refresh() {
    if (loading.value) return
    loading.value = true
    try {
      const data = await getAuthChannels()
      if (data && typeof data === 'object') {
        channels.value = {
          email: data.email !== false,
          sms:   data.sms   !== false,
          google: data.google === true,
          apple:  data.apple  === true
        }
      }
      loaded.value = true
    } catch (e) {
      // 探测失败:保持默认全 true,让用户尝试(凭证未就位时后端仍能 fail-fast)
      // eslint-disable-next-line no-console
      console.warn('[authChannels] fetch failed, fall back to default all-on', e)
    } finally {
      loading.value = false
    }
  }

  /** 首次访问 LoginView/RegisterView 时按需 fetch(避免 App.vue 启动时无谓请求) */
  function ensureLoaded() {
    if (!loaded.value && !loading.value) {
      refresh()
    }
  }

  return { channels, loaded, loading, refresh, ensureLoaded }
})
