import request from './request'

/**
 * 教师列表(支持搜索 / 筛选 / 排序 / 分页)
 *
 * @param {Object} [params]
 * @param {string} [params.keyword]      模糊匹配 nickname / intro
 * @param {string} [params.accent]       逗号分隔多选,如 'mandarin_cn,cantonese'
 * @param {string} [params.priceBuckets] 逗号分隔,如 'lt200,200-500'
 * @param {boolean} [params.available]   true 仅今日可约
 * @param {number} [params.minRating]    avg_rating >=
 * @param {string} [params.expertise]    逗号分隔,如 'business,hsk'
 * @param {string} [params.tags]         逗号分隔,如 'beginner,kids'
 * @param {string} [params.sort]         recommend / rating_desc / price_asc / price_desc / review_count_desc
 * @param {number} [params.pageNo]       默认 1
 * @param {number} [params.pageSize]     默认 24
 */
export function listTeachers(params) {
  return request.get('/edu/teacher/list', { params: params || {} })
}

/**
 * 教师总数(同 list 的 filter 但只返 { total })
 */
export function countTeachers(params) {
  return request.get('/edu/teacher/count', { params: params || {} })
}

export function getTeacher(userId) {
  return request.get('/edu/teacher/get', { params: { userId } })
}

export function listTeacherSchedules(teacherId) {
  return request.get('/edu/teacher-schedule/list', { params: { teacherId } })
}
