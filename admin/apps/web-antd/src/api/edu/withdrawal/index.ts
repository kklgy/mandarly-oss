import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

/**
 * 教师提现审核 admin API
 *
 * 对应后端 {@link com.mandarly.boot.module.edu.controller.admin.withdrawal.TeacherWithdrawalController}
 * 实际路径前缀 `/admin-api/edu/withdrawal/...`(/admin-api 由 VITE_GLOB_API_URL 注入)
 *
 * 权限 perm key(spec §6.4):
 * - edu:withdrawal:query
 * - edu:withdrawal:audit
 * - edu:withdrawal:pay
 * - edu:withdrawal:reveal-payee
 */
export namespace EduWithdrawalApi {
  /** 提现申请状态机 */
  export type WithdrawalStatus =
    | 'approved'
    | 'failed'
    | 'paid'
    | 'pending'
    | 'rejected';

  /** 收款方式 */
  export type PayeeMethod =
    | 'alipay'
    | 'bank_card'
    | 'other'
    | 'paypal'
    | 'wechat';

  /** 提现申请(管理后台 RespVO,payee 脱敏) */
  export interface Withdrawal {
    id: number;
    teacherId: number;
    /** 金额(USD) */
    amount: number | string;
    currency: string;
    payeeMethod: PayeeMethod;
    /** 收款信息脱敏后展示(后 4 位) */
    payeeInfoMasked?: string;
    status: WithdrawalStatus;
    /** 申请时间(UTC ISO) */
    appliedAt?: string;
    auditedBy?: number;
    /** 审核时间(UTC ISO) */
    auditedAt?: string;
    rejectReason?: string;
    paidBy?: number;
    /** 打款时间(UTC ISO) */
    paidAt?: string;
    paidProof?: string;
    paidRemark?: string;
  }

  /** 分页查询参数 */
  export interface PageReq extends PageParam {
    status?: WithdrawalStatus;
    teacherId?: number;
    teacherKeyword?: string;
    amountMin?: number | string;
    amountMax?: number | string;
    /** UTC ISO,例如 2026-05-01T00:00:00 */
    appliedAtFrom?: string;
    /** UTC ISO,例如 2026-06-01T00:00:00 */
    appliedAtTo?: string;
  }

  /** 审核 Request */
  export interface AuditReq {
    approved: boolean;
    rejectReason?: string;
  }

  /** 标记打款 Request */
  export interface MarkPaidReq {
    paidProof?: string;
    paidRemark?: string;
  }

  /** 标记打款失败 Request */
  export interface MarkFailedReq {
    failReason: string;
  }
}

/** 查询提现申请分页(payee 脱敏) */
export function getWithdrawalPage(params: EduWithdrawalApi.PageReq) {
  return requestClient.get<PageResult<EduWithdrawalApi.Withdrawal>>(
    '/edu/withdrawal/page',
    { params },
  );
}

/** 查询提现申请详情(payee 脱敏) */
export function getWithdrawal(id: number) {
  return requestClient.get<EduWithdrawalApi.Withdrawal>(
    `/edu/withdrawal/${id}`,
  );
}

/** 审核(通过 / 驳回) */
export function auditWithdrawal(id: number, data: EduWithdrawalApi.AuditReq) {
  return requestClient.post<boolean>(`/edu/withdrawal/${id}/audit`, data);
}

/** 标记打款成功(approved → paid) */
export function markWithdrawalPaid(
  id: number,
  data: EduWithdrawalApi.MarkPaidReq,
) {
  return requestClient.post<boolean>(`/edu/withdrawal/${id}/mark-paid`, data);
}

/** 标记打款失败(approved → failed,余额回退) */
export function markWithdrawalFailed(
  id: number,
  data: EduWithdrawalApi.MarkFailedReq,
) {
  return requestClient.post<boolean>(`/edu/withdrawal/${id}/mark-failed`, data);
}

/**
 * 查看完整收款信息明文(二次确认 + 后端一次性审计日志)
 *
 * 安全约束(spec §6.5):
 * - 仅 perm `edu:withdrawal:reveal-payee` 可调
 * - 调用即写 audit log(后端实现)
 * - 前端拿到后必须 3 秒倒计时自动遮罩
 */
export function revealWithdrawalPayee(id: number) {
  return requestClient.get<string>(`/edu/withdrawal/${id}/reveal-payee`);
}
