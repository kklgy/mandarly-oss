/**
 * 教师中心 - 工作台聚合 API(对应 /app-api/edu/teacher-center/dashboard/*)
 *
 * 后端 controller:AppTeacherDashboardController(D16 P2)
 */

import request from '@/api/request'

/**
 * 工作台 4 卡 + 评价聚合
 * @returns {Promise<{ weeklyClassCount, monthlyIncomeUsd, pendingSettleUsd, totalEarnedUsd, availableUsd, ratingAvg, ratingCount }>}
 */
export function getTeacherDashboardSummary() {
  return request.get('/edu/teacher-center/dashboard/summary')
}
