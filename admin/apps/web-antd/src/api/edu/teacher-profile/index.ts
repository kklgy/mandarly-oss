import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduTeacherProfileApi {
  /** 教师档案审核状态 */
  export type AuditStatus = 'approved' | 'draft' | 'pending' | 'rejected';

  /** 口音 */
  export type Accent = 'hk' | 'mainland' | 'mixed' | 'taiwan';

  /** 审核动作 */
  export type AuditAction = 'approve' | 'reject';

  /** 教师档案 */
  export interface TeacherProfile {
    userId: number;
    phone?: string;
    intro?: string;
    auditStatus?: AuditStatus;
    rejectReason?: string;
    auditedAt?: string;
    auditedBy?: number;
    level?: string;
    expertise?: string[];
    accent?: Accent;
    languages?: string[];
    yearsExperience?: number;
    introVideoUrl?: string;
    introVideoSize?: number;
    introVideoUploadedAt?: string;
    createTime?: string;
  }

  /** 列表查询参数 */
  export interface PageReq extends PageParam {
    userId?: number;
    auditStatus?: AuditStatus;
    introKeyword?: string;
    accent?: Accent;
  }

  /** 审核请求体 */
  export interface AuditReq {
    userId: number;
    action: AuditAction;
    rejectReason?: string;
  }
}

/** 查询教师档案分页 */
export function getTeacherProfilePage(params: EduTeacherProfileApi.PageReq) {
  return requestClient.get<PageResult<EduTeacherProfileApi.TeacherProfile>>(
    '/edu/teacher-profile/page',
    { params },
  );
}

/** 查询教师档案详情 */
export function getTeacherProfile(userId: number) {
  return requestClient.get<EduTeacherProfileApi.TeacherProfile>(
    `/edu/teacher-profile/get?userId=${userId}`,
  );
}

/** 教师档案审核(通过/驳回) */
export function auditTeacherProfile(data: EduTeacherProfileApi.AuditReq) {
  return requestClient.put<boolean>('/edu/teacher-profile/audit', data);
}
