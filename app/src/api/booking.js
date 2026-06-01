/**
 * 预约下单 API(对应 /app-api/edu/booking/*)
 *
 * M2.5 已切真 token,studentId 由后端从 token 取,不再走前端参数。
 * PRD: §4.3 S2 选时段 / S3 套餐 / S4 扣次预约 / S5 我的订单 + 取消
 */

import request from './request'

export function listMyPackages(locale) {
  return request.get('/edu/booking/student-packages', {
    params: { locale }
  })
}

/**
 * S8 我的套餐:全部套餐(active + expired + exhausted)
 * 含 source / status(三态) / purchasedAt 字段
 */
export function listMyAllPackages(locale) {
  return request.get('/edu/booking/student-packages/all', {
    params: { locale }
  })
}

export function createBooking({ teacherId, scheduledAt, studentPackageId }) {
  return request.post('/edu/booking/create', {
    teacherId,
    scheduledAt,
    studentPackageId
  })
}

export function getOrder(id, locale) {
  return request.get(`/edu/booking/order/${id}`, { params: { locale } })
}

/**
 * 我的订单分页(S5)
 * @param {Object} opts
 * @param {string} [opts.status]   单 status 或多 status 逗号分隔(upcoming,cancelled);留空表示全部
 * @param {number} [opts.pageNo=1]
 * @param {number} [opts.pageSize=10]
 * @param {string} [opts.locale]
 * @returns {{ total: number, list: Array }}
 */
export function listMyOrders({ status, pageNo = 1, pageSize = 10, locale } = {}) {
  return request.get('/edu/booking/my-orders', {
    params: { status, pageNo, pageSize, locale }
  })
}

/**
 * 学生取消订单(S5)
 * 后端按课前 24h 自动判断是否返还课次
 */
export function cancelOrder(id, reason) {
  return request.post(`/edu/booking/order/${id}/cancel`, { reason: reason || '' })
}

export function listTeacherScheduleExceptions(teacherId, from, to) {
  return request.get('/edu/teacher-schedule/exceptions', {
    params: { teacherId, from, to }
  })
}

/**
 * GET /app-api/edu/booking/counts
 * 全局徽章数据源(useBookingCountsStore 调用)。
 *
 * 返回: { upcomingCount, toReviewCount, refundingCount }
 *
 * Wave 3 后端落地前:404/500 兜底 0/0/0,不抛(避免 store 状态机被打断);
 * 落地后这层 catch 仅吞偶发网络错。
 */
export async function getBookingCounts() {
  try {
    const data = await request.get('/edu/booking/counts')
    return {
      upcomingCount: data?.upcomingCount ?? 0,
      toReviewCount: data?.toReviewCount ?? 0,
      refundingCount: data?.refundingCount ?? 0
    }
  } catch (e) {
    // Wave 3 后端未落时静默兜底;落地后这层 catch 仅吞偶发网络错
    if (import.meta.env.DEV) {
      console.debug('[bookingCounts] fetch failed, fallback 0/0/0', e)
    }
    return { upcomingCount: 0, toReviewCount: 0, refundingCount: 0 }
  }
}
