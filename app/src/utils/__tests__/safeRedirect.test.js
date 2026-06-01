// 需运行: pnpm add -D vitest -w(主 agent 后续装,vitest 风格 describe/it/expect)
// 当前 app/ 未装 vitest,本测试文件按 vitest 约定写,装好后 `pnpm vitest run` 即可执行。

import { describe, it, expect } from 'vitest'
import { safeRedirect } from '../safeRedirect.js'

describe('safeRedirect — open redirect 防御', () => {
  // ===== 攻击向量(plan § 11.4 明文要求 4 个) =====

  describe('攻击向量:协议绝对 URL', () => {
    it('https://evil.com → fallback', () => {
      expect(safeRedirect('https://evil.com')).toBe('/')
    })

    it('http://evil.com/x → fallback', () => {
      expect(safeRedirect('http://evil.com/x')).toBe('/')
    })

    it('HTTPS://EVIL.COM(大写)→ fallback', () => {
      expect(safeRedirect('HTTPS://EVIL.COM')).toBe('/')
    })
  })

  describe('攻击向量:协议相对 URL', () => {
    it('//evil.com → fallback', () => {
      expect(safeRedirect('//evil.com')).toBe('/')
    })

    it('//evil.com/path?x=1 → fallback', () => {
      expect(safeRedirect('//evil.com/path?x=1')).toBe('/')
    })
  })

  describe('攻击向量:反斜杠绕过', () => {
    it('/\\evil.com → fallback', () => {
      expect(safeRedirect('/\\evil.com')).toBe('/')
    })

    it('/\\\\evil.com/path → fallback', () => {
      expect(safeRedirect('/\\\\evil.com/path')).toBe('/')
    })
  })

  describe('攻击向量:伪协议(XSS)', () => {
    it('javascript:alert(1) → fallback', () => {
      expect(safeRedirect('javascript:alert(1)')).toBe('/')
    })

    it('JavaScript:alert(1)(混合大小写)→ fallback', () => {
      expect(safeRedirect('JavaScript:alert(1)')).toBe('/')
    })

    it('data:text/html,<script>alert(1)</script> → fallback', () => {
      expect(safeRedirect('data:text/html,<script>alert(1)</script>')).toBe('/')
    })

    it('vbscript:msgbox(1) → fallback', () => {
      expect(safeRedirect('vbscript:msgbox(1)')).toBe('/')
    })

    it('file:///etc/passwd → fallback', () => {
      expect(safeRedirect('file:///etc/passwd')).toBe('/')
    })
  })

  describe('攻击向量:类型 / 空值', () => {
    it('null → fallback', () => {
      expect(safeRedirect(null)).toBe('/')
    })

    it('undefined → fallback', () => {
      expect(safeRedirect(undefined)).toBe('/')
    })

    it('空串 → fallback', () => {
      expect(safeRedirect('')).toBe('/')
    })

    it('数字 123 → fallback', () => {
      expect(safeRedirect(123)).toBe('/')
    })

    it('对象 → fallback', () => {
      expect(safeRedirect({ url: '/teacher/1' })).toBe('/')
    })

    it('数组 → fallback', () => {
      expect(safeRedirect(['/teacher/1'])).toBe('/')
    })
  })

  describe('攻击向量:不带 / 开头', () => {
    it('evil.com → fallback', () => {
      expect(safeRedirect('evil.com')).toBe('/')
    })

    it('teachers(裸路径)→ fallback', () => {
      expect(safeRedirect('teachers')).toBe('/')
    })
  })

  describe('攻击向量:不在白名单的 / 路径', () => {
    it('/admin/secret → fallback', () => {
      expect(safeRedirect('/admin/secret')).toBe('/')
    })

    it('/api/users → fallback', () => {
      expect(safeRedirect('/api/users')).toBe('/')
    })

    it('/teachersfoo(伪装匹配 /teachers)→ fallback', () => {
      // 不带尾斜杠 prefix 严格校验:/teachers 后必须是 ? # / 或结束
      expect(safeRedirect('/teachersfoo')).toBe('/')
    })

    it('/profilex(伪装匹配 /profile)→ fallback', () => {
      expect(safeRedirect('/profilex')).toBe('/')
    })
  })

  // ===== 合法路径(应原值返回) =====

  describe('合法路径:白名单内 — 带尾斜杠 prefix', () => {
    it('/teacher/ → 原值', () => {
      expect(safeRedirect('/teacher/')).toBe('/teacher/')
    })

    it('/teacher/123 → 原值', () => {
      expect(safeRedirect('/teacher/123')).toBe('/teacher/123')
    })

    it('/teacher/123?slot=2026-05-12-14-00 → 原值', () => {
      expect(safeRedirect('/teacher/123?slot=2026-05-12-14-00')).toBe(
        '/teacher/123?slot=2026-05-12-14-00'
      )
    })

    it('/legal/privacy → 原值', () => {
      expect(safeRedirect('/legal/privacy')).toBe('/legal/privacy')
    })

    it('/my/packages → 原值', () => {
      expect(safeRedirect('/my/packages')).toBe('/my/packages')
    })

    it('/my/refunds → 原值', () => {
      expect(safeRedirect('/my/refunds')).toBe('/my/refunds')
    })

    it('/booking/success/42 → 原值', () => {
      expect(safeRedirect('/booking/success/42')).toBe('/booking/success/42')
    })

    it('/payment/success?redirect=/teacher/123 → 原值', () => {
      // payment 是合法 prefix;query 里的嵌套 redirect 由调用方再次 safeRedirect
      expect(safeRedirect('/payment/success?redirect=/teacher/123')).toBe(
        '/payment/success?redirect=/teacher/123'
      )
    })

    it('/review/99 → 原值', () => {
      expect(safeRedirect('/review/99')).toBe('/review/99')
    })

    it('/classroom/77 → 原值', () => {
      expect(safeRedirect('/classroom/77')).toBe('/classroom/77')
    })
  })

  describe('合法路径:白名单内 — 不带尾斜杠 prefix', () => {
    it('/teachers → 原值', () => {
      expect(safeRedirect('/teachers')).toBe('/teachers')
    })

    it('/teachers?keyword=hello → 原值', () => {
      expect(safeRedirect('/teachers?keyword=hello')).toBe('/teachers?keyword=hello')
    })

    it('/teachers#anchor → 原值', () => {
      expect(safeRedirect('/teachers#anchor')).toBe('/teachers#anchor')
    })

    it('/orders → 原值', () => {
      expect(safeRedirect('/orders')).toBe('/orders')
    })

    it('/orders?tab=upcoming → 原值', () => {
      expect(safeRedirect('/orders?tab=upcoming')).toBe('/orders?tab=upcoming')
    })

    it('/profile → 原值', () => {
      expect(safeRedirect('/profile')).toBe('/profile')
    })

    it('/profile/account → 原值', () => {
      expect(safeRedirect('/profile/account')).toBe('/profile/account')
    })

    it('/packages → 原值', () => {
      expect(safeRedirect('/packages')).toBe('/packages')
    })

    it('/level-check → 原值', () => {
      expect(safeRedirect('/level-check')).toBe('/level-check')
    })

    it('/level-check/result/abc123 → 原值', () => {
      expect(safeRedirect('/level-check/result/abc123')).toBe('/level-check/result/abc123')
    })
  })

  // ===== 自定义 fallback =====

  describe('自定义 fallback', () => {
    it('safeRedirect("//evil", "/login") → /login', () => {
      expect(safeRedirect('//evil', '/login')).toBe('/login')
    })

    it('safeRedirect(null, "/home") → /home', () => {
      expect(safeRedirect(null, '/home')).toBe('/home')
    })

    it('safeRedirect("javascript:alert(1)", "/safe") → /safe', () => {
      expect(safeRedirect('javascript:alert(1)', '/safe')).toBe('/safe')
    })
  })

  // ===== 可选 router 参数:二次校验 =====

  describe('可选 router 参数:二次 router.resolve 校验', () => {
    // 模拟 router:matched 命中
    const okRouter = {
      resolve(target) {
        return { matched: [{ path: target }], name: 'TeacherDetail', path: target }
      }
    }

    // 模拟 router:matched 为空(NotFound 场景)
    const notFoundRouter = {
      resolve() {
        return { matched: [], name: 'NotFound' }
      }
    }

    // 模拟 router:resolve 抛错
    const throwingRouter = {
      resolve() {
        throw new Error('boom')
      }
    }

    it('白名单通过 + router.resolve 命中 → 原值', () => {
      expect(safeRedirect('/teacher/123', '/', okRouter)).toBe('/teacher/123')
    })

    it('白名单通过 + router.resolve matched 为空 → fallback', () => {
      expect(safeRedirect('/teacher/123', '/', notFoundRouter)).toBe('/')
    })

    it('白名单通过 + router.resolve 抛错 → fallback', () => {
      expect(safeRedirect('/teacher/123', '/', throwingRouter)).toBe('/')
    })

    it('攻击向量 + router 传入 → 仍 fallback(白名单优先拦截)', () => {
      expect(safeRedirect('https://evil.com', '/', okRouter)).toBe('/')
    })

    it('router 为非函数对象 → 跳过二次校验,仍按 prefix 通过', () => {
      expect(safeRedirect('/teacher/1', '/', { resolve: 'not-a-function' })).toBe('/teacher/1')
    })
  })
})
