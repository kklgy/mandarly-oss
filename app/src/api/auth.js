/**
 * auth.js — 认证相关 API
 *
 * 17 个函数:
 *   4 渠道登录 (email / phone / apple / social)
 *   2 注册 (email / phone)
 *   2 验证码 (email / sms)
 *   refresh / logout / reset-password
 *   redirect URL + 2 social bind/unbind
 *   getProfile / updateProfile / bindEmail / bindPhone
 */

import request from './request'
import i18n from '@/i18n'

// 当前 i18n locale,后端按 startsWith('zh') 路由 ZH/EN 模板
function currentLocale() {
  return i18n.global.locale.value || 'en'
}

// ---------- 登录 ----------

export const loginByEmail = (email, password) =>
  request.post('/system/auth/login', { email, password })

export const loginByPhone = (phone, code) =>
  request.post('/system/auth/sms-login', { phone, code, locale: currentLocale() })

export const loginByApple = (code, state) =>
  request.post('/system/auth/social-login', { type: 'apple', code, state })

// D19-A8: role 默认 student;教师注册场景调用方传 'teacher'(后端 D19-A2 反射建 draft profile)
export const loginBySocial = (type, code, state, role = 'student') =>
  request.post('/system/auth/social-login', { type, code, state, role })

// ---------- 注册 ----------

export const registerByEmail = (payload) =>
  request.post('/system/auth/register', payload)

export const registerByPhone = (payload) =>
  request.post('/system/auth/sms-register', payload)

// ---------- 验证码 ----------

export const sendEmailCode = (email, scene) =>
  request.post('/system/auth/send-email-code', { email, scene, locale: currentLocale() })

export const sendSmsCode = (phone, scene) =>
  request.post('/system/auth/send-sms-code', { phone, scene, locale: currentLocale() })

// ---------- token / 密码 ----------

export const refreshToken = (refreshTokenStr) =>
  request.post('/system/auth/refresh-token', { refreshToken: refreshTokenStr })

export const logout = () =>
  request.post('/system/auth/logout')

export const resetPassword = (payload) =>
  request.post('/system/auth/reset-password', payload)

// ---------- 三方 OAuth ----------

export const getSocialRedirectUrl = (type) =>
  request.get('/system/auth/social-redirect-url', { params: { type } })

// ---------- 渠道探测(M2.5 §6.5 toggle) ----------

export const getAuthChannels = () =>
  request.get('/system/auth/channels')

export const bindSocial = (payload) =>
  request.post('/system/auth/bind-social', payload)

export const unbindSocial = (payload) =>
  request.post('/system/auth/unbind-social', payload)

// ---------- 用户中心 ----------

export const getProfile = () =>
  request.get('/system/user/me')

export const updateProfile = (payload) =>
  request.put('/system/user/me', payload)

export const bindEmail = (payload) =>
  request.post('/system/user/bind-email', payload)

export const bindPhone = (payload) =>
  request.post('/system/user/bind-phone', payload)
