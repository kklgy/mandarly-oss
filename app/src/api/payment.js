/**
 * payment.js — M4 支付/退款/推荐统计 API
 */
import request from './request'

// ======================== Payment ========================

/**
 * 发起套餐购买(创建 Stripe Checkout Session)
 * @param {{ packageId: number, referralCode?: string }} data
 * @returns {{ paymentId: number, checkoutUrl: string, discountAmountUsd: number }}
 */
export const checkout = (data) => request.post('/edu/payment/checkout', data)

/**
 * 查询支付单详情(用于 success 页轮询)
 * @param {number} id
 */
export const getPayment = (id) => request.get(`/edu/payment/${id}`)

/**
 * 我的支付分页列表
 */
export const getMyPaymentPage = (params) =>
  request.get('/edu/payment/page', { params })

// ======================== Refund ========================

/**
 * 学生申请退款
 * @param {{ paymentId: number, reason: string }} data
 */
export const applyRefund = (data) => request.post('/edu/refund', data)

/**
 * 我的退款分页列表
 */
export const getMyRefunds = (params) =>
  request.get('/edu/refund/page', { params })

/**
 * 查询单条退款详情
 */
export const getMyRefund = (id) => request.get(`/edu/refund/${id}`)

// ======================== Referral ========================

/**
 * 我的推荐战绩统计
 * @returns {{ referralCode: string, refereeCount: number, rewardedCount: number, totalRewardPackages: number }}
 */
export const getMyReferralStats = () => request.get('/edu/referral/me')

// ======================== Package(公开套餐列表) ========================

/**
 * 公开套餐列表(未登录也可访问)
 */
export const listPackages = () => request.get('/edu/package/list')
