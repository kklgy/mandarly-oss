/**
 * token.js — localStorage 读写封装
 *
 * keys 与实现计划保持一致:
 *   mandarly_access_token
 *   mandarly_refresh_token
 *   mandarly_token_expires_at
 */

const KEY_ACCESS = 'mandarly_access_token'
const KEY_REFRESH = 'mandarly_refresh_token'
const KEY_EXPIRES = 'mandarly_token_expires_at'

export const tokenStorage = {
  load() {
    return {
      access: localStorage.getItem(KEY_ACCESS) || '',
      refresh: localStorage.getItem(KEY_REFRESH) || '',
      expiresAt: Number(localStorage.getItem(KEY_EXPIRES) || 0)
    }
  },
  save({ access, refresh, expiresAt }) {
    localStorage.setItem(KEY_ACCESS, access)
    localStorage.setItem(KEY_REFRESH, refresh)
    localStorage.setItem(KEY_EXPIRES, String(expiresAt))
  },
  clear() {
    localStorage.removeItem(KEY_ACCESS)
    localStorage.removeItem(KEY_REFRESH)
    localStorage.removeItem(KEY_EXPIRES)
  }
}

export const tokenKeys = { KEY_ACCESS, KEY_REFRESH, KEY_EXPIRES }
