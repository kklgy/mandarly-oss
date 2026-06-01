import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduRefundApi {
  /** 退款工单状态 */
  export type RefundStatus =
    | 'approved'
    | 'failed'
    | 'pending'
    | 'refunded'
    | 'rejected';

  /** 退款工单(管理后台 RespVO) */
  export interface Refund {
    id: number;
    paymentId: number;
    userId: number;
    studentPackageId?: number;
    applyReason?: string;
    /** 系统建议退款金额(USD) */
    suggestedAmountUsd?: number | string;
    /** 最终退款金额(USD) */
    finalAmountUsd?: number | string;
    adjustReason?: string;
    channelRefundId?: string;
    status: RefundStatus;
    auditBy?: number;
    /** 审核时间(UTC ISO) */
    auditAt?: string;
    auditNote?: string;
    /** 退款到账时间(UTC ISO) */
    refundedAt?: string;
    teacherIncomeDeducted?: boolean;
    /** 创建时间(UTC ISO) */
    createTime?: string;
    updateTime?: string;
  }

  /** 分页查询参数 */
  export interface PageReq extends PageParam {
    userId?: number;
    paymentId?: number;
    status?: RefundStatus;
  }

  /** 审批通过退款 Request */
  export interface ApproveReq {
    finalAmountUsd: number | string;
    adjustReason?: string;
    auditNote?: string;
  }

  /** 拒绝退款 Request */
  export interface RejectReq {
    auditNote: string;
  }
}

/** 查询退款工单分页 */
export function getRefundPage(params: EduRefundApi.PageReq) {
  return requestClient.get<PageResult<EduRefundApi.Refund>>(
    '/edu/refund/page',
    { params },
  );
}

/** 查询退款工单详情 */
export function getRefund(id: number) {
  return requestClient.get<EduRefundApi.Refund>(`/edu/refund/${id}`);
}

/** 审批通过退款 */
export function approveRefund(id: number, data: EduRefundApi.ApproveReq) {
  return requestClient.post<boolean>(`/edu/refund/${id}/approve`, data);
}

/** 拒绝退款 */
export function rejectRefund(id: number, data: EduRefundApi.RejectReq) {
  return requestClient.post<boolean>(`/edu/refund/${id}/reject`, data);
}
