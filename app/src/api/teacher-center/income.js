/**
 * 教师中心 - 收入 API(对应 /app-api/edu/teacher-center/income/*)
 *
 * 后端 controller:AppTeacherIncomeController(M6 §3.7 / §5.3 T3)
 */

import request from '@/api/request'

/**
 * 教师余额(5 字段)
 * @returns {Promise<{ availableUsd, frozenT7Usd, pendingWithdrawUsd, totalEarnedUsd, totalWithdrawnUsd, currency }>}
 */
export function getTeacherBalance() {
  return request.get('/edu/teacher-center/income/balance')
}

/**
 * 收入流水分页
 * @param {object} [opts]
 * @param {string} [opts.from] UTC ISO datetime
 * @param {string} [opts.to] UTC ISO datetime
 * @param {string} [opts.type] normal/free_trial/no_show_teacher/no_show_student/refund_deduct/manual_adjust
 * @param {string} [opts.status] frozen/available/reverted
 * @param {number} [opts.pageNo=1]
 * @param {number} [opts.pageSize=10]
 */
export function listTeacherIncome({ from, to, type, status, pageNo = 1, pageSize = 10 } = {}) {
  return request.get('/edu/teacher-center/income/list', {
    params: { from, to, type, status, pageNo, pageSize }
  })
}
