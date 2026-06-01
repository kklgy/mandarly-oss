/**
 * 课后评价 API(对应 /app-api/edu/review/*)
 *
 * PRD: §S7 评价 + §T6 教师评价管理 + S2 公开评价列表
 * 口径:仅 finished 才能评、24h 改窗、tag 上限 3、白名单 6 个。
 */

import request from './request'

/**
 * 学生首次写评价(Wave 5 第 11 轮支持 isAnonymous + customTags)
 *
 * @param {object} payload
 * @param {number|string} payload.orderId
 * @param {number} payload.rating 1-5
 * @param {string|null} payload.content 选填正文 ≤ 1024
 * @param {string[]} payload.tags 预设 tag code 列表(白名单 0-5 个)
 * @param {boolean} [payload.isAnonymous] 匿名(默认 false)
 * @param {string[]} [payload.customTags] 用户自定义 tag(0-3 条 × 8 字符)
 */
export function saveReview({ orderId, rating, content, tags, isAnonymous, customTags }) {
  return request.post('/edu/review/save', {
    orderId, rating, content, tags,
    isAnonymous: isAnonymous === true,
    customTags: customTags || []
  })
}

/**
 * 学生 72h 内修改评价(Wave 5:24h→72h)
 */
export function updateReview({ orderId, rating, content, tags, isAnonymous, customTags }) {
  return request.post('/edu/review/update', {
    orderId, rating, content, tags,
    isAnonymous: isAnonymous === true,
    customTags: customTags || []
  })
}

/**
 * 预设 tag 字典(Wave 5 第 11 轮 TagPicker 加载;返 tag code 数组)
 * 前端 i18n 解 review.tag.{code}
 */
export function getReviewTags() {
  return request.get('/edu/review/tags')
}

/**
 * 按订单查评价(写/改入口前置查;返 null 表示还没评)
 */
export function getReviewByOrder(orderId) {
  return request.get(`/edu/review/by-order/${orderId}`)
}

/**
 * 学生:我写过的评价分页
 */
export function listMyReviews({ pageNo = 1, pageSize = 10 } = {}) {
  return request.get('/edu/review/my-page', { params: { pageNo, pageSize } })
}

/**
 * 公开:教师评价分页(S2 教师详情)
 */
export function listTeacherReviews(teacherId, { pageNo = 1, pageSize = 10 } = {}) {
  return request.get(`/edu/review/teacher/${teacherId}/list`, {
    params: { pageNo, pageSize }
  })
}

/**
 * 公开:教师评分聚合 {avgRating, reviewCount, finishedOrderCount}
 */
export function getTeacherStat(teacherId) {
  return request.get(`/edu/review/teacher/${teacherId}/stat`)
}

/**
 * T6:教师查自己的评分聚合
 */
export function getMyTeacherStat() {
  return request.get('/edu/review/teacher/me/stat')
}
