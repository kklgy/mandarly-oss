/**
 * Normalize Spring/Jackson date-time shapes used by schedule APIs.
 *
 * Production may serialize LocalTime as [hour, minute, second] while some
 * tests/dev paths serialize it as "HH:mm:ss". Schedule views expect strings.
 */

export function toHmsString(value) {
  if (value == null) return null
  if (Array.isArray(value)) {
    const [h = 0, m = 0, s = 0] = value
    return `${pad(h)}:${pad(m)}:${pad(s)}`
  }
  if (typeof value === 'string') {
    const [h = '0', m = '0', s = '0'] = value.split(':')
    return `${pad(h)}:${pad(m)}:${pad(s)}`
  }
  return String(value)
}

export function toYmdString(value) {
  if (value == null) return null
  if (Array.isArray(value)) {
    const [y, m = 1, d = 1] = value
    return `${String(y)}-${pad(m)}-${pad(d)}`
  }
  if (typeof value === 'string') return value.slice(0, 10)
  return String(value)
}

export function normalizeScheduleRow(row) {
  if (!row) return row
  return {
    ...row,
    startTime: toHmsString(row.startTime),
    endTime: toHmsString(row.endTime)
  }
}

export function normalizeScheduleExceptionRow(row) {
  if (!row) return row
  return {
    ...normalizeScheduleRow(row),
    exceptionDate: toYmdString(row.exceptionDate)
  }
}

export function normalizeScheduleRows(rows) {
  return (rows || []).map(normalizeScheduleRow)
}

export function normalizeScheduleExceptionRows(rows) {
  return (rows || []).map(normalizeScheduleExceptionRow)
}

function pad(value) {
  return String(value).padStart(2, '0')
}
