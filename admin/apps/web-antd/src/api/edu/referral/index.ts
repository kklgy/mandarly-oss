import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduReferralApi {
  /** 推荐记录状态 */
  export type ReferralStatus = 'bound' | 'rewarded' | 'voided';

  /** 推荐记录(管理后台 RespVO) */
  export interface ReferralRecord {
    id: number;
    referrerUserId?: number;
    referrerEmail?: string;
    refereeUserId?: number;
    refereeEmail?: string;
    referralCode?: string;
    paymentId?: number;
    /** 被推荐人折扣金额(USD) */
    refereeDiscountAmountUsd?: number | string;
    /** 推荐人奖励套餐 ID */
    referrerRewardPackageId?: number;
    status: ReferralStatus;
    /** 绑定时间(UTC ISO) */
    boundAt?: string;
    /** 奖励发放时间(UTC ISO) */
    rewardedAt?: string;
    /** 创建时间(UTC ISO) */
    createTime?: string;
  }

  /** 分页查询参数 */
  export interface PageReq extends PageParam {
    referrerUserId?: number;
    refereeUserId?: number;
    status?: ReferralStatus;
  }
}

/** 查询推荐记录分页 */
export function getReferralPage(params: EduReferralApi.PageReq) {
  return requestClient.get<PageResult<EduReferralApi.ReferralRecord>>(
    '/edu/referral/page',
    { params },
  );
}
