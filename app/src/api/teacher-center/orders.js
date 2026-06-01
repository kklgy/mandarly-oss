/**
 * 教师中心 - 订单 API(对应 /app-api/edu/teacher-center/orders/*)
 *
 * 后端 controller:AppTeacherOrderController(M6 §3.7 / §5.2 T2)
 * - teacherId 由后端从 token 取
 * - status 可逗号分隔(upcoming,finished),留空表示全部
 */

import request from '@/api/request'

/**
 * 教师订单分页
 * @param {object} [opts]
 * @param {string} [opts.status] 单 status 或多 status 逗号分隔(留空表示全部)
 * @param {number} [opts.pageNo=1]
 * @param {number} [opts.pageSize=10]
 * @returns {Promise<{ total, list: Array<AppTeacherOrderRespVO> }>}
 */
export function listTeacherOrders({ status, pageNo = 1, pageSize = 10 } = {}) {
  return request.get('/edu/teacher-center/orders/list', {
    params: { status, pageNo, pageSize }
  })
}

/**
 * 订单详情(教师视角)
 * @param {number} id course_order.id
 */
export function getTeacherOrder(id) {
  return request.get(`/edu/teacher-center/orders/${id}`)
}
