import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduPaymentApi {
  /** 支付状态 */
  export type PaymentStatus =
    | 'cancelled'
    | 'expired'
    | 'failed'
    | 'paid'
    | 'partial_refunded'
    | 'pending'
    | 'refunded';

  /** 支付订单(管理后台 RespVO,含完整 Stripe 字段) */
  export interface Payment {
    id: number;
    userId: number;
    packageId: number;
    studentPackageId?: number;
    channel?: string;
    channelSessionId?: string;
    channelPaymentIntentId?: string;
    channelChargeId?: string;
    paymentMethodType?: string;
    /** 请求金额(USD) BigDecimal */
    amountRequest?: number | string;
    currencyRequest?: string;
    /** 实际付款金额(原币种) */
    amountPaid?: number | string;
    currencyPaid?: string;
    /** 结算金额(USD) */
    amountSettledUsd?: number | string;
    /** 折扣金额(USD) */
    discountAmountUsd?: number | string;
    referrerUserId?: number;
    status: PaymentStatus;
    /** 支付时间(UTC ISO) */
    paidAt?: string;
    /** 过期时间(UTC ISO) */
    expiredAt?: string;
    /** 创建时间(UTC ISO) */
    createTime?: string;
    updateTime?: string;
  }

  /** 分页查询参数 */
  export interface PageReq extends PageParam {
    userId?: number;
    packageId?: number;
    status?: PaymentStatus;
    createTimeFrom?: string;
    createTimeTo?: string;
  }
}

/** 查询支付订单分页 */
export function getPaymentPage(params: EduPaymentApi.PageReq) {
  return requestClient.get<PageResult<EduPaymentApi.Payment>>(
    '/edu/payment/page',
    { params },
  );
}

/** 查询支付订单详情 */
export function getPayment(id: number) {
  return requestClient.get<EduPaymentApi.Payment>(`/edu/payment/${id}`);
}
