/**
 * 教师中心 - 排课 API(对应 /app-api/edu/teacher-center/schedule/*)
 *
 * 后端 controller:AppTeacherScheduleController(M6 §3.7 / §5.1 T1)
 * - teacherId 由后端从 token 取(@ss.hasRole('teacher')),前端不传
 * - weekday 0=Sun..6=Sat;mm 0 / 30(30-min 网格)
 */

import request from '@/api/request'
import { normalizeScheduleExceptionRows, normalizeScheduleRows } from '@/utils/scheduleTime'

/**
 * 周排课模板
 * @returns {Promise<{ teacherId, timezone, slots: Array<{ id, weekday, startTime, endTime }> }>}
 */
export async function getWeeklySchedule() {
  const data = await request.get('/edu/teacher-center/schedule/weekly')
  return {
    ...data,
    slots: normalizeScheduleRows(data?.slots || [])
  }
}

/**
 * 切换周模板单格子(30-min)
 * @param {object} payload
 * @param {number} payload.dayOfWeek 0=Sun..6=Sat
 * @param {number} payload.hh 0-23
 * @param {number} payload.mm 0 / 30
 * @param {boolean} payload.available true=新增 / false=撤销
 * @param {string} [payload.timezone] available=true 时必填(IANA)
 * @returns {Promise<number>} 操作后的 slot id
 */
export function toggleWeeklySlot({ dayOfWeek, hh, mm, available, timezone }) {
  return request.post('/edu/teacher-center/schedule/toggle', {
    dayOfWeek,
    hh,
    mm,
    available,
    timezone
  })
}

/**
 * 查例外列表(可选日期范围,留空返回全部)
 * @param {object} [opts]
 * @param {string} [opts.from] YYYY-MM-DD
 * @param {string} [opts.to] YYYY-MM-DD
 * @returns {Promise<Array<{ id, teacherId, exceptionDate, type, startTime, endTime, timezone, reason, createTime }>>}
 */
export async function getExceptions({ from, to } = {}) {
  const rows = await request.get('/edu/teacher-center/schedule/exceptions', {
    params: { from, to }
  })
  return normalizeScheduleExceptionRows(rows || [])
}

/**
 * 单次例外操作
 * @param {object} payload
 * @param {string} payload.date YYYY-MM-DD(教师本地时区)
 * @param {number} payload.hh 0-23
 * @param {number} payload.mm 0 / 30
 * @param {string} payload.action 'closed' | 'extra' | 'remove'
 * @param {string} [payload.timezone] closed/extra 时必填
 * @param {string} [payload.reason] 备注(可选)
 * @returns {Promise<number>} 操作 id
 */
export function toggleException({ date, hh, mm, action, timezone, reason }) {
  return request.post('/edu/teacher-center/schedule/exceptions', {
    date,
    hh,
    mm,
    action,
    timezone,
    reason
  })
}
