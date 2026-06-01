/**
 * 视频课堂 API(对应 /app-api/edu/classroom/*)
 *
 * PRD: §4.6 视频课堂(LCIC iframe 嵌入)。M2.5 已切真 token,userId 由后端从 token 取。
 */

import request from './request'

/**
 * 实时拉取课堂 join 信息
 * @param {number} orderId  course_order.id
 * @returns {Promise<{ joinUrl: string, token: string, expiresAt: string, role: 'student'|'teacher' }>}
 */
export function getJoinInfo(orderId) {
  return request.get(`/edu/classroom/${orderId}/join-info`)
}
