/**
 * stores/user.js — 全局用户状态 (Pinia)
 *
 * 功能:
 *   - 从 localStorage 恢复 token (SSR-safe)
 *   - 4 渠道登录 / 2 注册 / logout
 *   - forceRefresh / refreshIfNeeded (5min 阈值)
 *   - refresh promise 去重,避免并发多次刷新
 *   - 跨 tab 同步:另一 tab 清除 access_token 时 reset 本 tab 状态
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tokenStorage, tokenKeys } from '@/utils/token'
import { clearUserTimezone, setUserTimezone } from '@/utils/datetime'
import * as authApi from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // ---- 初始化(从 localStorage 恢复) ----
  const initial = tokenStorage.load()
  const accessToken = ref(initial.access)
  const refreshTokenValue = ref(initial.refresh)
  const expiresAt = ref(initial.expiresAt)
  const userId = ref(null)
  const role = ref(null)
  const profile = ref(null)
  const oauthBindings = ref([])

  // ---- computed ----
  const isLoggedIn = computed(
    () => !!accessToken.value && Date.now() < expiresAt.value
  )

  // ---- 内部:持久化 token 到 localStorage ----
  function applyTokenPayload(payload) {
    accessToken.value = payload.accessToken
    refreshTokenValue.value = payload.refreshToken
    // 后端 expiresTime 为 ISO 字符串(LocalDateTime 序列化)
    if (payload.expiresTime) {
      expiresAt.value = new Date(payload.expiresTime).getTime()
    }
    if (payload.userId !== undefined) userId.value = payload.userId
    if (payload.role !== undefined) role.value = payload.role
    tokenStorage.save({
      access: accessToken.value,
      refresh: refreshTokenValue.value,
      expiresAt: expiresAt.value
    })
  }

  // ---- 内部:清除全部状态 ----
  function reset() {
    accessToken.value = ''
    refreshTokenValue.value = ''
    expiresAt.value = 0
    userId.value = null
    role.value = null
    profile.value = null
    oauthBindings.value = []
    tokenStorage.clear()
    clearUserTimezone()
    // M5 Wave 4 plan §11.13:切账号清理 mandarly_pkg_currency,前账号 HKD 不跟到新账号
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem('mandarly_pkg_currency')
        localStorage.removeItem('mandarly_pkg_currency_manual')
      }
    } catch (e) {
      // 忽略 SSR / 隐私模式异常
    }
  }

  // ---- 登录 ----
  async function loginByEmail(email, password) {
    const data = await authApi.loginByEmail(email, password)
    applyTokenPayload(data)
    await loadProfile()
  }

  async function loginByPhone(phone, code) {
    const data = await authApi.loginByPhone(phone, code)
    applyTokenPayload(data)
    await loadProfile()
  }

  async function loginBySocial(type, code, state, role = 'student') {
    const data = await authApi.loginBySocial(type, code, state, role)
    applyTokenPayload(data)
    await loadProfile()
  }

  // ---- 注册 ----
  async function registerByEmail(payload) {
    const data = await authApi.registerByEmail(payload)
    applyTokenPayload(data)
    await loadProfile()
  }

  async function registerByPhone(payload) {
    const data = await authApi.registerByPhone(payload)
    applyTokenPayload(data)
    await loadProfile()
  }

  // ---- 登出 ----
  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // ignore — 本地状态无论如何都要清除
    }
    reset()
  }

  // ---- token 刷新 ----
  let refreshPromise = null

  /** 判断是否需要刷新;若 token 距过期 < 5min 则主动 refresh */
  async function refreshIfNeeded() {
    if (!refreshTokenValue.value) return false
    const fiveMin = 5 * 60 * 1000
    if (Date.now() < expiresAt.value - fiveMin) return true
    return forceRefresh()
  }

  /** 强制刷新,并发调用时复用同一个 Promise */
  async function forceRefresh() {
    if (refreshPromise) return refreshPromise
    if (!refreshTokenValue.value) return false
    refreshPromise = (async () => {
      try {
        const data = await authApi.refreshToken(refreshTokenValue.value)
        applyTokenPayload({
          ...data,
          userId: userId.value,
          role: role.value
        })
        return true
      } catch (e) {
        reset()
        return false
      } finally {
        refreshPromise = null
      }
    })()
    return refreshPromise
  }

  // ---- 用户 profile ----
  async function loadProfile() {
    if (!isLoggedIn.value) return
    try {
      const data = await authApi.getProfile()
      // D19-A7: 兜底 teacherAuditStatus / teacherRejectReason,使 role=student 或
      // 旧后端响应也能安全访问 userStore.profile.teacherAuditStatus(默认 null)。
      profile.value = {
        teacherAuditStatus: null,
        teacherRejectReason: null,
        ...data
      }
      oauthBindings.value = data.oauthBindings || []
      userId.value = data.id
      role.value = data.role
      setUserTimezone(data.timezone)
    } catch (e) {
      // 忽略:调用方可自行重试;isLoggedIn 仍为 true
    }
  }

  async function refreshProfile() {
    return loadProfile()
  }

  // ---- 跨 tab 同步 ----
  if (typeof window !== 'undefined') {
    window.addEventListener('storage', (e) => {
      if (e.key === tokenKeys.KEY_ACCESS && !e.newValue) {
        reset()
      }
    })
  }

  return {
    // state
    accessToken,
    refreshToken: refreshTokenValue,
    expiresAt,
    userId,
    role,
    profile,
    oauthBindings,
    // computed
    isLoggedIn,
    // actions
    loginByEmail,
    loginByPhone,
    loginBySocial,
    registerByEmail,
    registerByPhone,
    logout,
    refreshIfNeeded,
    forceRefresh,
    loadProfile,
    refreshProfile
  }
})
