/**
 * 教师中心 - 我的档案 API(对应 /app-api/edu/teacher-center/profile/*)
 *
 * 后端 controller:AppTeacherProfileController(D19 Phase B / B4)
 *
 * 端点说明:
 *   - GET    /me           返回档案字段 + auditStatus + rejectReason + qualificationCount
 *   - PUT    /me           partial update;不修改 audit_status
 *   - POST   /submit-audit  rejected → pending 状态机(仅 rejected 状态可调)
 *
 * 字段口径(对齐 AppTeacherProfileMeRespVO):
 *   userId / intro / expertise / accent / languages /
 *   yearsExperience / introVideoUrl /
 *   auditStatus / rejectReason / auditedAt / qualificationCount
 */

import request from '@/api/request'

/**
 * 拉取本人教师档案 + 审核状态 + 资质数量
 * @returns {Promise<{
 *   userId:number,
 *   intro:string|null,
 *   expertise:string[]|null,
 *   accent:string|null,
 *   languages:string[]|null,
 *   yearsExperience:number|null,
 *   introVideoUrl:string|null,
 *   auditStatus:'pending'|'approved'|'rejected'|null,
 *   rejectReason:string|null,
 *   auditedAt:string|null,
 *   qualificationCount:number
 * }>}
 */
export function getMyTeacherProfile() {
  return request.get('/edu/teacher-center/profile/me')
}

/**
 * 更新本人教师档案(partial,null 字段不动)
 *
 * @param {object} data
 * @param {string} [data.intro]
 * @param {string[]} [data.expertise]
 * @param {string} [data.accent]
 * @param {string[]} [data.languages]
 * @param {number} [data.yearsExperience]
 * @param {string} [data.introVideoUrl]
 * @returns {Promise<boolean>}
 */
export function updateMyTeacherProfile(data) {
  return request.put('/edu/teacher-center/profile/me', data)
}

/**
 * 提交审核(rejected → pending);其他状态后端会拒绝
 * @returns {Promise<boolean>}
 */
export function submitForAudit() {
  return request.post('/edu/teacher-center/profile/submit-audit')
}
