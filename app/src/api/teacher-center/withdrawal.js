/**
 * 教师中心 - 提现 API(对应 /app-api/edu/teacher-center/withdrawal/*)
 *
 * 后端 controller:AppTeacherWithdrawalController(M6 §3.7 / §5.4 T4 + §5.5 T5)
 * - apply:payeeInfo 明文 POST,后端透明加密(AesEncryptTypeHandler)
 * - list / detail:payeeInfoMasked 仅后 4 位,完整明文走 admin reveal
 */

import request from '@/api/request'

/**
 * 申请提现
 * @param {object} payload
 * @param {string|number} payload.amount 必填,USD
 * @param {string} payload.payeeInfo 明文(后端透明加密)
 * @param {string} payload.payeeMethod wechat | alipay | paypal | bank_card | other
 * @returns {Promise<number>} teacher_withdrawal.id
 */
export function applyWithdrawal(payload) {
  return request.post('/edu/teacher-center/withdrawal/apply', payload)
}

/**
 * 提现历史分页(payee 脱敏)
 * @param {object} [opts]
 * @param {string} [opts.status] pending/approved/paid/rejected/failed
 * @param {number} [opts.pageNo=1]
 * @param {number} [opts.pageSize=10]
 */
export function listWithdrawals({ status, pageNo = 1, pageSize = 10 } = {}) {
  return request.get('/edu/teacher-center/withdrawal/list', {
    params: { status, pageNo, pageSize }
  })
}

/**
 * 提现详情(payee 脱敏)
 */
export function getWithdrawal(id) {
  return request.get(`/edu/teacher-center/withdrawal/${id}`)
}
