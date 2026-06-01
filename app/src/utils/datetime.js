/**
 * Mandarly 时间工具
 *
 * 设计原则(对齐项目级 CLAUDE.md):
 * - 后端 / 数据库统一 UTC 存储
 * - 前端展示按"用户时区"转换(取自 user.timezone,默认浏览器时区)
 * - 教师可约时段(teacher_schedule)按教师本地时区,需要用 timezone 字段单独换算
 *
 * 全局约定:所有从后端拿到的 datetime 都是 UTC ISO string
 *           所有发给后端的 datetime 都转成 UTC ISO string
 *
 * 用法:
 *   import { fromUTC, toUTC, formatLocal, getUserTimezone } from '@/utils/datetime'
 */

import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import relativeTime from 'dayjs/plugin/relativeTime'
import customParseFormat from 'dayjs/plugin/customParseFormat'
import localizedFormat from 'dayjs/plugin/localizedFormat'
import 'dayjs/locale/en'
import 'dayjs/locale/zh-cn'
import 'dayjs/locale/zh-tw'
import 'dayjs/locale/ar'

dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.extend(relativeTime)
dayjs.extend(customParseFormat)
dayjs.extend(localizedFormat)

const TZ_STORAGE_KEY = 'mandarly_user_timezone'
const LOCALE_TO_DAYJS = {
  en: 'en',
  'zh-CN': 'zh-cn',
  'zh-TW': 'zh-tw',
  ar: 'ar'
}

/**
 * 获取用户时区(优先级:用户配置 → 浏览器检测 → UTC 兜底)
 * @returns {string} IANA 时区名,如 "Asia/Hong_Kong"
 */
export function getUserTimezone() {
  try {
    const saved = localStorage.getItem(TZ_STORAGE_KEY)
    if (saved) return saved
  } catch {
    // ignore SSR / privacy mode
  }
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'
  } catch {
    return 'UTC'
  }
}

/**
 * 设置用户时区(用户中心改时区时调用)
 */
export function setUserTimezone(tz) {
  if (!tz) return
  try {
    localStorage.setItem(TZ_STORAGE_KEY, tz)
  } catch {
    // ignore SSR / privacy mode
  }
}

/**
 * 清除本地用户时区缓存(退出 / 切账号时调用)
 */
export function clearUserTimezone() {
  try {
    localStorage.removeItem(TZ_STORAGE_KEY)
  } catch {
    // ignore SSR / privacy mode
  }
}

/**
 * 同步 dayjs 全局 locale(在 i18n 切换语言时调用)
 */
export function syncDayjsLocale(appLocale) {
  const dayjsLocale = LOCALE_TO_DAYJS[appLocale] || 'en'
  dayjs.locale(dayjsLocale)
}

/**
 * UTC ISO string → 用户时区下的 dayjs 对象
 * @param {string} utcIsoString 如 "2026-05-15T10:00:00Z"
 * @param {string} [tz] 默认用户时区
 * @returns {dayjs.Dayjs}
 */
export function fromUTC(utcIsoString, tz = getUserTimezone()) {
  return dayjs.utc(utcIsoString).tz(tz)
}

/**
 * 本地时间 → UTC ISO string(发给后端)
 * @param {string|Date|dayjs.Dayjs} localTime
 * @param {string} [tz] 默认用户时区
 * @returns {string} 形如 "2026-05-15T10:00:00.000Z"
 */
export function toUTC(localTime, tz = getUserTimezone()) {
  if (typeof localTime === 'string') {
    return dayjs.tz(localTime, tz).utc().toISOString()
  }
  return dayjs(localTime).tz(tz, true).utc().toISOString()
}

/**
 * 格式化展示(自动用用户时区 + 当前 dayjs locale)
 * @param {string} utcIsoString
 * @param {string} [format] dayjs format string,默认 "LLL"(本地化日期时间)
 * @param {string} [tz] 默认用户时区
 * @returns {string}
 */
export function formatLocal(utcIsoString, format = 'LLL', tz = getUserTimezone()) {
  if (!utcIsoString) return ''
  return fromUTC(utcIsoString, tz).format(format)
}

/**
 * 相对时间("5 分钟前"/"in 2 hours")
 * @param {string} utcIsoString
 * @returns {string}
 */
export function relative(utcIsoString) {
  if (!utcIsoString) return ''
  return dayjs.utc(utcIsoString).fromNow()
}

/**
 * 教师可约时段计算辅助:把"教师本地时区下的 weekday + time" 转成
 * "学生时区下的具体某天某时刻 dayjs 对象"。
 *
 * @param {object} slot { weekday, startTime, endTime, timezone } — teacher_schedule 行
 * @param {string|dayjs.Dayjs} referenceDate 学生侧选择的某个日期(本周内)
 * @param {string} [studentTz]
 * @returns {{ start: dayjs.Dayjs, end: dayjs.Dayjs }}
 */
export function teacherSlotToStudent(slot, referenceDate, studentTz = getUserTimezone()) {
  const teacherTz = slot.timezone
  const refInTeacher = dayjs(referenceDate).tz(teacherTz)
  const targetWeekday = slot.weekday
  const diff = (targetWeekday - refInTeacher.day() + 7) % 7
  const dateInTeacher = refInTeacher.add(diff, 'day').format('YYYY-MM-DD')

  const start = dayjs.tz(`${dateInTeacher} ${slot.startTime}`, teacherTz).tz(studentTz)
  const end = dayjs.tz(`${dateInTeacher} ${slot.endTime}`, teacherTz).tz(studentTz)
  return { start, end }
}

/**
 * 是否在课前 5 分钟到课程结束之间(用于"进入课堂"按钮高亮)
 */
export function isClassJoinable(scheduledAtUTC, durationMinutes = 30) {
  const start = dayjs.utc(scheduledAtUTC)
  const end = start.add(durationMinutes, 'minute')
  const joinFrom = start.subtract(5, 'minute')
  const now = dayjs.utc()
  return now.isAfter(joinFrom) && now.isBefore(end)
}

/**
 * 是否符合"课前 24h+"取消条件(用于退款判定)
 */
export function isOver24hBefore(scheduledAtUTC) {
  return dayjs.utc().add(24, 'hour').isBefore(dayjs.utc(scheduledAtUTC))
}

export default dayjs
