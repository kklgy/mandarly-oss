/**
 * 教师中心 - 资质材料 API(对应 /app-api/edu/teacher-center/qualification/*)
 *
 * 后端 controller:AppTeacherQualificationController(D19 Phase B / B5)
 *
 * 端点说明:
 *   - GET    /list           列本人资质材料(不暴露 docUrl,只展示文件名 + 状态)
 *   - POST   /upload         记录已上传至 COS 的资质材料(前端先调 /infra/file/upload 拿 docUrl)
 *   - DELETE /{id}           删除本人的资质材料(后端做 owner check)
 *
 * 字段口径(对齐 AppTeacherQualificationRespVO):
 *   id / docType / docFilename / auditStatus / rejectReason / createTime
 *
 * docType 取值(对齐 TeacherQualificationDocTypeEnum):
 *   id_card / passport / teaching_cert / experience_proof
 */

import request from '@/api/request'

/**
 * 列本人资质材料
 * @returns {Promise<Array<{
 *   id:number,
 *   docType:'id_card'|'passport'|'teaching_cert'|'experience_proof',
 *   docFilename:string|null,
 *   auditStatus:'pending'|'approved'|'rejected',
 *   rejectReason:string|null,
 *   createTime:string|null
 * }>>}
 */
export function listMyQualifications() {
  return request.get('/edu/teacher-center/qualification/list')
}

/**
 * 记录一份已上传至 COS 的资质材料
 *
 * @param {object} data
 * @param {string} data.docType        id_card / passport / teaching_cert / experience_proof
 * @param {string} data.docUrl         COS 完整访问 URL(由 /infra/file/upload 返回)
 * @param {string} [data.docFilename]  原始文件名(展示用)
 * @returns {Promise<number>} 新记录 id
 */
export function uploadQualification(data) {
  return request.post('/edu/teacher-center/qualification/upload', data)
}

/**
 * 删除本人的一份资质材料
 *
 * @param {number} id
 * @returns {Promise<boolean>}
 */
export function deleteQualification(id) {
  return request.delete(`/edu/teacher-center/qualification/${id}`)
}
