/**
 * package.js — 套餐 API(M5 Wave 4 § PackageListView)
 *
 * 公开套餐列表: /edu/package/list?currency=HKD/USD/CNY
 * 免费体验状态: /edu/booking/free-trial-status (登录态, claimed: bool)
 *
 * 注: 与 payment.js 中 listPackages 重复, 为后续清理迁出 payment.js 而设;
 *     本 wave 内两处共存, 主 agent 后续可在收尾合并。
 */
import request from './request'

/**
 * 公开套餐列表(未登录也可访问)
 *
 * @param {Object} [opts]
 * @param {'HKD'|'USD'|'CNY'} [opts.currency]  货币(可选, 默认按租户配置或 HKD)
 * @param {string} [opts.locale]               locale, 用于翻译 recommendationLabel/discountLabel/sellingPoints
 * @returns {Promise<Array>}
 */
export function listPackages({ currency, locale } = {}) {
  return request.get('/edu/package/list', {
    params: { currency, locale }
  })
}

/**
 * 免费体验领取状态(登录态)
 *
 * @returns {Promise<{ claimed: boolean }>}
 */
export function getFreeTrialStatus() {
  return request.get('/edu/booking/free-trial-status')
}
