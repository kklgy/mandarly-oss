import request from './request'

export function bootstrapSupport(params) {
  return request.get('/edu/support/bootstrap', { params: params || {} })
}

export function askSupport(payload) {
  return request.post('/edu/support/ask', payload)
}

/**
 * 转人工点击埋点。Phase 1 SupportChatWidget 没有 ask 流,直接渠道点击 logId 留空,
 * 后端走纯点击日志 insert 路径,sessionId 必填。
 */
export function markSupportContactClick(payload) {
  return request.post('/edu/support/contact-click', payload)
}
