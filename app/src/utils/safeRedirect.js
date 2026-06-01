/**
 * safeRedirect — open redirect 防御工具
 *
 * 背景:
 *   登录 / 注册 / 重置密码 / 第三方回调 / 支付取消等页面会从 `?redirect=`
 *   query 取 next URL 后 router.replace。若不校验白名单,攻击者可构造
 *   /login?redirect=https://evil.com 跳第三方钓鱼站,本工具收口防御。
 *
 * 攻击向量回顾(全部应 fallback):
 *   - 'https://evil.com'         → 协议绝对 URL,跳第三方钓鱼
 *   - 'http://evil.com/x'        → 同上
 *   - '//evil.com'               → 协议相对 URL,浏览器自动补当前协议
 *   - '/\\evil.com'              → 反斜杠绕过(部分浏览器解析为 //evil.com)
 *   - 'javascript:alert(1)'      → XSS(直接执行脚本)
 *   - 'data:text/html,<script>'  → data URL,等价 XSS
 *   - 'vbscript:msgbox(1)'       → IE 老攻击向量,补全防御
 *   - 'file:///etc/passwd'       → 本地文件协议
 *   - 不带 / 开头的相对路径      → 浏览器按当前路径拼接,语义不可控
 *   - 不在白名单的 / 路径        → 例如 /admin/secret,Mandarly 用户端无此页
 *
 * 使用:
 *   import { safeRedirect } from '@/utils/safeRedirect'
 *
 *   // 基础用法(LoginView 这类 setup 内有 useRouter 的场景,推荐传 router)
 *   const router = useRouter()
 *   const target = safeRedirect(route.query.redirect, '/', router)
 *   router.replace(target)
 *
 *   // 简化用法(SocialCallbackView 这类回调环境复杂,允许跳过 router.resolve)
 *   const target = safeRedirect(route.query.redirect, '/')
 *   router.replace(target)
 */

// 白名单前缀:严格对应 app/src/router/index.js 的合法用户端路径
// 带尾斜杠的:必须 startsWith(prefix)(保证 prefix 后是路径段,不是 //evil.com)
// 不带尾斜杠的:精确等于 / 或 prefix + '?' / '#' / '/' 开头
const ALLOW_PREFIXES = [
  '/teacher/',     // /teacher/:userId
  '/teachers',     // /teachers (列表页,无尾斜杠)
  '/packages',     // /packages (套餐列表)
  '/orders',       // /orders (我的订单)
  '/profile',      // /profile (个人中心)
  '/level-check',  // /level-check + /level-check/result/:id
  '/legal/',       // /legal/privacy / /legal/terms
  '/my/',          // /my/packages / /my/refunds
  '/booking/',     // /booking/success/:id
  '/payment/',     // /payment/success / /payment/cancel
  '/review/',      // /review/:orderId
  '/classroom/'    // /classroom/:orderId
]

// 严格拒绝模式(优先级最高,白名单匹配前先过这层)
const DENY_PATTERNS = [
  /^https?:/i,    // http:// / https:// — 协议绝对 URL
  /^\/\//,        // //evil.com — 协议相对 URL
  /^\/\\/,        // /\evil.com — 反斜杠绕过
  /^javascript:/i, // javascript: — XSS
  /^data:/i,      // data: — XSS
  /^vbscript:/i,  // vbscript: — IE 老攻击向量
  /^file:/i       // file:// — 本地文件协议
]

/**
 * 校验并返回一个安全的内部跳转目标。
 *
 * @param {*} target   待校验的 redirect 值(可能来自 route.query,任意类型)
 * @param {string} [fallback='/']  校验失败时的兜底路径(主 agent 调用方决定)
 * @param {object|null} [router=null]  可选 vue-router 实例。传入则做二次
 *                                     router.resolve 校验,确保命中具体路由;
 *                                     不传则只走 prefix 白名单(已足够安全)。
 * @returns {string}   安全的目标路径,或 fallback
 */
export function safeRedirect(target, fallback = '/', router = null) {
  // 1. 类型 / 空值兜底:非字符串、空串、null、undefined、数字 等一律 fallback
  if (!target || typeof target !== 'string') return fallback

  // 2. 严格拒绝模式(协议、协议相对、反斜杠、伪协议)
  for (const pat of DENY_PATTERNS) {
    if (pat.test(target)) return fallback
  }

  // 3. 必须 / 开头(纯相对路径如 'evil.com' 一律拒绝)
  if (!target.startsWith('/')) return fallback

  // 4. 白名单前缀匹配
  //    - 带尾斜杠的 prefix(如 '/teacher/'):startsWith(prefix) 即可
  //      —— prefix 后必有路径段,不会出现 '/teacher//evil.com' 通过(协议相对已被 DENY 拦截)
  //    - 不带尾斜杠的 prefix(如 '/teachers' '/orders' '/profile' '/level-check'):
  //      必须精确等于 prefix,或 prefix 后跟 '?' / '#' / '/'
  //      —— 防止 '/teachersfoo' 这类伪装匹配
  const matched = ALLOW_PREFIXES.some((prefix) => {
    if (prefix.endsWith('/')) {
      return target.startsWith(prefix)
    }
    if (target === prefix) return true
    if (target.startsWith(prefix + '?')) return true
    if (target.startsWith(prefix + '#')) return true
    if (target.startsWith(prefix + '/')) return true
    return false
  })

  if (!matched) return fallback

  // 5. 二次校验(可选):若传入 router,用 router.resolve 校验是否命中具体路由
  //    NotFound / matched 为空 时回退,避免白名单"前缀匹配但实际无路由"
  if (router && typeof router.resolve === 'function') {
    try {
      const resolved = router.resolve(target)
      if (!resolved || !resolved.matched || resolved.matched.length === 0) {
        return fallback
      }
      if (resolved.name === 'NotFound') {
        return fallback
      }
    } catch {
      return fallback
    }
  }

  return target
}

export default safeRedirect
