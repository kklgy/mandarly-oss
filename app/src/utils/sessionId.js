/**
 * 用户会话 ID(未登录场景)
 *
 * 用途:水平测试 / 客服咨询等 @PermitAll 接口需要一个稳定 ID 串联同一访客
 * 落库 localStorage,刷新页面 / 重启浏览器仍保留
 */

const STORAGE_KEY = 'mandarly_session_id'

function uuidv4() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export function getSessionId() {
  let id = localStorage.getItem(STORAGE_KEY)
  if (!id) {
    id = uuidv4()
    localStorage.setItem(STORAGE_KEY, id)
  }
  return id
}
