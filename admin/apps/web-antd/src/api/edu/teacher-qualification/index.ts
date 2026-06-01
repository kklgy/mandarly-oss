import { requestClient } from '#/api/request';

/**
 * 管理后台 - 教师资质材料 API(D19 Phase B / B11)。
 *
 * 对应后端 {@code AdminTeacherQualificationController#list}:
 *   GET /admin-api/edu/teacher-qualification/list?userId=<teacherId>
 *
 * docUrl 字段由后端替换为 15min TTL 的腾讯云 COS 预签名 URL,前端
 * `<a target="_blank">` 直接打开即可在浏览器预览证件,无需额外鉴权。
 *
 * 权限点 `edu:teacher-qualification:query`(menu id 3045,B5 SQL patch
 * 20260518_130000 已挂在「教师审核」菜单下)。
 */
export namespace EduTeacherQualificationApi {
  /** 文件类型 */
  export type DocType =
    | 'experience_proof'
    | 'id_card'
    | 'passport'
    | 'teaching_cert';

  /** 资质审核状态 */
  export type AuditStatus = 'approved' | 'pending' | 'rejected';

  /** 教师资质材料 */
  export interface TeacherQualification {
    id: number;
    userId: number;
    docType: DocType | string;
    /** 15min 预签名 URL(admin 在线预览,不下载) */
    docUrl: string;
    docFilename?: string;
    auditStatus: AuditStatus | string;
    rejectReason?: string;
    auditedAt?: string;
    auditedBy?: number;
    createTime: string;
  }
}

/** 查指定教师的资质材料列表(docUrl 已是 15min 预签名 URL) */
export function getTeacherQualifications(userId: number) {
  return requestClient.get<EduTeacherQualificationApi.TeacherQualification[]>(
    '/edu/teacher-qualification/list',
    { params: { userId } },
  );
}
