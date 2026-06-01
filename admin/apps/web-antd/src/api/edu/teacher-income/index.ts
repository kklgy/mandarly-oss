import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduTeacherIncomeApi {
  /** 收入类型 */
  export type IncomeType = 'free_trial' | 'no_show_student' | 'normal' | 'refund_deduct';

  /** 教师收入流水(管理后台 RespVO) */
  export interface TeacherIncome {
    id: number;
    teacherId: number;
    teacherEmail?: string;
    teacherName?: string;
    courseOrderId?: number;
    refundId?: number;
    packageName?: string;
    /** 收入金额(USD),负数为扣回 */
    amountUsd: number | string;
    type: IncomeType;
    /** 结算时间(UTC ISO) */
    settledAt?: string;
    /** 创建时间(UTC ISO) */
    createTime?: string;
  }

  /** 教师余额详情 */
  export interface TeacherBalance {
    teacherId: number;
    frozenUsd: number | string;
    availableUsd: number | string;
    totalEarnedUsd: number | string;
    totalWithdrawnUsd: number | string;
    /** 最后重建时间(UTC ISO) */
    lastRebuildAt?: string;
  }

  /** 分页查询参数 */
  export interface PageReq extends PageParam {
    teacherId?: number;
    type?: IncomeType;
  }
}

/** 查询教师收入流水分页 */
export function getTeacherIncomePage(params: EduTeacherIncomeApi.PageReq) {
  return requestClient.get<PageResult<EduTeacherIncomeApi.TeacherIncome>>(
    '/edu/teacher-income/page',
    { params },
  );
}

/** 查询单教师余额详情 */
export function getTeacherBalance(teacherId: number) {
  return requestClient.get<EduTeacherIncomeApi.TeacherBalance>(
    `/edu/teacher-income/balance/${teacherId}`,
  );
}
