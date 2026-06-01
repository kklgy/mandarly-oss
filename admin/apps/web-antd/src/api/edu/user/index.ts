import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduUserApi {
  export type UserRole = 'student' | 'teacher';

  export type UserStatus = 'active' | 'frozen' | 'pending_verification';

  export type FreezeAction = 'freeze' | 'unfreeze';

  export interface User {
    id: number;
    role: UserRole;
    email?: string;
    emailVerifiedAt?: string;
    phone?: string;
    phoneVerifiedAt?: string;
    nickname?: string;
    avatarUrl?: string;
    locale?: string;
    timezone?: string;
    status: UserStatus;
    referralCode?: string;
    referredBy?: number;
    learningGoal?: string;
    lastLoginAt?: string;
    lastLoginIp?: string;
    createTime?: string;
    oauthProviders?: string[];
    oauthSummary?: string;
    teacherAuditStatus?: string;
    teacherAccent?: string;
    teacherYearsExperience?: number;
    teacherExpertise?: string[];
  }

  export interface PageReq extends PageParam {
    role?: UserRole;
    status?: UserStatus;
    nickname?: string;
    email?: string;
    phone?: string;
    locale?: string;
    keyword?: string;
  }

  export interface FreezeReq {
    userId: number;
    action: FreezeAction;
    reason?: string;
  }
}

export function getUserPage(params: EduUserApi.PageReq) {
  return requestClient.get<PageResult<EduUserApi.User>>('/edu/user/page', {
    params,
  });
}

export function getUser(userId: number) {
  return requestClient.get<EduUserApi.User>(`/edu/user/get?userId=${userId}`);
}

export function freezeUser(data: EduUserApi.FreezeReq) {
  return requestClient.put<boolean>('/edu/user/freeze', data);
}
