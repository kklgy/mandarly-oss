import { describe, expect, it } from 'vitest'
import { getFreeTrialCtaTarget, shouldShowFreeTrialCta } from '../freeTrialCta.js'

describe('freeTrialCta', () => {
  it('游客仍展示免费体验入口并跳注册', () => {
    expect(shouldShowFreeTrialCta({ isLoggedIn: false, claimed: false })).toBe(true)
    expect(getFreeTrialCtaTarget({ isLoggedIn: false })).toBe('/register?redirect=/booking/free-trial')
  })

  it('已登录但未领取时展示免费体验入口并跳预约免费体验', () => {
    expect(shouldShowFreeTrialCta({ isLoggedIn: true, claimed: false })).toBe(true)
    expect(getFreeTrialCtaTarget({ isLoggedIn: true })).toBe('/booking/free-trial')
  })

  it('已登录且已领取过免费体验时隐藏入口', () => {
    expect(shouldShowFreeTrialCta({ isLoggedIn: true, claimed: true })).toBe(false)
  })
})
