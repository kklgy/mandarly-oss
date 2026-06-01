/**
 * Axios 实例 — app 端统一请求封装
 *
 * - baseURL 固定 /app-api,dev 走 vite proxy,prod 走 nginx 后端转发
 * - 自动注入 tenant-id 头(若依多租户必需)
 * - 统一 CommonResult<T> 解包:成功返回 data 字段,失败抛 Error
 * - 请求拦截:从 localStorage 注入 Authorization Bearer token
 * - 响应拦截:HTTP 401 / CommonResult code=401 → 自动 refresh + retry once
 *            refresh 失败 → 跳转 /login?redirect=<current>
 */

import axios from 'axios'

const TENANT_ID = '1'

const request = axios.create({
  baseURL: '/app-api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'tenant-id': TENANT_ID
  }
})

// ---------- 请求拦截:注入 Authorization 头 ----------
request.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const accessToken = localStorage.getItem('mandarly_access_token')
    if (accessToken) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${accessToken}`
    }
  }
  return config
})

// ---------- 401 处理:refresh + retry ----------
let isRefreshing = false
const queue = []

/**
 * 动态 import user store,避免循环依赖:
 *   request.js → user.js → auth.js → request.js
 */
async function tryRefresh() {
  const { useUserStore } = await import('@/stores/user')
  const userStore = useUserStore()
  return userStore.forceRefresh()
}

async function goToLogin() {
  if (typeof window === 'undefined') return
  // 避免循环跳转
  if (window.location.pathname === '/login') return
  const current = window.location.pathname + window.location.search
  window.location.href = `/login?redirect=${encodeURIComponent(current)}`
}

async function handle401(config) {
  // 已重试过 — 直接失败
  if (config._retry) {
    await goToLogin()
    return Promise.reject(new Error('认证已过期'))
  }
  config._retry = true

  if (isRefreshing) {
    // 等待正在进行的 refresh 完成后重试
    return new Promise((resolve, reject) => {
      queue.push({ resolve, reject, config })
    })
  }

  isRefreshing = true
  try {
    const ok = await tryRefresh()
    if (ok) {
      // flush 等待队列
      queue.forEach(({ resolve, config: c }) => resolve(request(c)))
      queue.length = 0
      return request(config)
    }
    queue.forEach(({ reject }) => reject(new Error('刷新失败')))
    queue.length = 0
    await goToLogin()
    return Promise.reject(new Error('认证已过期'))
  } finally {
    isRefreshing = false
  }
}

// ---------- 响应拦截:CommonResult 解包 + 401 处理 ----------
request.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) return body.data
      // 若依在 token 过期时返回 code=401
      if (body.code === 401) {
        return handle401(resp.config)
      }
      const err = new Error(body.msg || `请求失败 (code=${body.code})`)
      err.code = body.code
      err.payload = body
      return Promise.reject(err)
    }
    return body
  },
  async (err) => {
    if (err.response && err.response.status === 401) {
      return handle401(err.config)
    }
    return Promise.reject(err)
  }
)

export default request
