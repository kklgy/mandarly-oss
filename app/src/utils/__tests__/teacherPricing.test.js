import { describe, expect, it } from 'vitest'
import { getTeacherPricingMode, hasTeacherSessionPrice } from '../teacherPricing.js'

describe('teacherPricing', () => {
  it('有老师单价且学生已登录时展示价格', () => {
    expect(getTeacherPricingMode({ isLoggedIn: true, isTeacher: false, pricePerSession: 250 })).toBe('price')
  })

  it('有老师单价但未登录时锁价', () => {
    expect(getTeacherPricingMode({ isLoggedIn: false, isTeacher: false, pricePerSession: 250 })).toBe('locked')
  })

  it('无老师单价且学生已登录时显示按套餐课次预约', () => {
    expect(hasTeacherSessionPrice(null)).toBe(false)
    expect(getTeacherPricingMode({ isLoggedIn: true, isTeacher: false, pricePerSession: null })).toBe('package-credit')
  })

  it('无老师单价且未登录时提示登录后查看套餐', () => {
    expect(getTeacherPricingMode({ isLoggedIn: false, isTeacher: false, pricePerSession: null })).toBe('login-package')
  })
})
