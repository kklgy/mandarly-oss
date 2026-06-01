/**
 * Mandarly 公司主体信息 — 前端唯一真实源
 *
 * 用途:
 *   AppFooter / Legal(隐私 / 条款 / 关于) / 邮件模板 等所有需要展示
 *   公司主体(中英阿名 / 注册地址 / 版权行 / 联系邮箱)的位置,
 *   统一从本文件 import。**禁止**在组件 / 页面里硬编码主体字符串。
 *
 * 单一真实源:
 *   docs/compliance/company-entity.md
 *
 * 变更纪律:
 *   1. 改值先改 docs/compliance/company-entity.md
 *   2. 然后同步本文件
 *   3. 顺手 grep 引用方(AppFooter / Legal / 邮件模板)看是否需要联动
 *
 * 对外联系邮箱:
 *   以 docs/compliance/stripe-qualification-package.md 记录的 Stripe 客服邮箱为准。
 */

// ─── 主体名(常量)──────────────────────────────────────────────────────────

export const COMPANY_NAME_ZH = '曼德勵科技有限公司'
export const COMPANY_NAME_EN = 'MANDARLY TECHNOLOGY LIMITED'
// 阿语主体名:取自 docs/compliance/company-entity.md § 2.1 阿语版权行,
// 去掉 "© {YEAR}" 前缀和 "جميع الحقوق محفوظة." 后缀,只保留主体名。
export const COMPANY_NAME_AR = 'مَنْدَرلِي للتكنولوجيا المحدودة'
// 繁中沿用简中(香港主体登记中文名同形)
export const COMPANY_NAME_ZH_TW = '曼德勵科技有限公司'

// ─── 注册地址(常量)────────────────────────────────────────────────────────

export const COMPANY_ADDR_EN = 'RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong'
export const COMPANY_ADDR_ZH = '香港九龙观塘伟业街 143 号 1 楼 131 室'

// 通用展示用注册地(Footer 单行版权后缀等场景)
export const COMPANY_REGION = 'Hong Kong'

// ─── 联系方式(常量)────────────────────────────────────────────────────────

export const COMPANY_CONTACT_EMAIL = 'support@mandarly.com'

// ─── 法务页 last updated ───────────────────────────────────────────────────
// 主体下证日期作为 v1 起算;后续每次法务条款实质性修订,改这两个常量即可

export const LAST_UPDATED_PRIVACY = '2026-05-07'
export const LAST_UPDATED_TERMS = '2026-05-07'

// ─── locale 归一化(内部工具)──────────────────────────────────────────────

/**
 * 把 vue-i18n locale 归一到本文件支持的 4 类:
 *   'zh-CN' | 'zh-TW' | 'ar' | 'en'
 * 未识别一律 fallback 'en'
 */
function normalizeLocale(locale) {
  if (!locale || typeof locale !== 'string') return 'en'
  const lower = locale.toLowerCase()
  if (lower === 'zh-tw' || lower === 'zh-hk' || lower === 'zh-hant') return 'zh-TW'
  if (lower === 'zh' || lower === 'zh-cn' || lower === 'zh-hans') return 'zh-CN'
  if (lower === 'ar' || lower.startsWith('ar-')) return 'ar'
  if (lower === 'en' || lower.startsWith('en-')) return 'en'
  return 'en'
}

// ─── 4 语言版权行 helper ───────────────────────────────────────────────────

/**
 * 返回当前 locale 的版权字符串,严格遵循
 * docs/compliance/company-entity.md § 2.1 法务文案首选格式。
 *
 * @param {string} locale  vue-i18n locale,如 'zh-CN' / 'zh-TW' / 'en' / 'ar'
 * @param {number} [year]  年份,默认取当前年(本地时区,Footer 跨年自动滚动)
 * @returns {string}
 */
export function getCopyrightLine(locale, year = new Date().getFullYear()) {
  const norm = normalizeLocale(locale)
  switch (norm) {
    case 'zh-CN':
    case 'zh-TW':
      return `© ${year} 曼德勵科技有限公司 保留所有权利。`
    case 'ar':
      return `© ${year} مَنْدَرلِي للتكنولوجيا المحدودة. جميع الحقوق محفوظة.`
    case 'en':
    default:
      return `© ${year} MANDARLY TECHNOLOGY LIMITED. All rights reserved.`
  }
}

// ─── 主体地址 helper ───────────────────────────────────────────────────────

/**
 * 返回当前 locale 适用的注册地址。
 *   zh-CN / zh-TW → 中文标准化地址
 *   en / ar / 其它 → 英文原版地址
 *
 * 注意:同一页面不同位置不混用中英(docs/compliance/company-entity.md § 2.2)。
 *
 * @param {string} locale
 * @returns {string}
 */
export function getCompanyAddress(locale) {
  const norm = normalizeLocale(locale)
  if (norm === 'zh-CN' || norm === 'zh-TW') return COMPANY_ADDR_ZH
  return COMPANY_ADDR_EN
}

// ─── 主体名 helper ─────────────────────────────────────────────────────────

/**
 * 返回当前 locale 对应的主体名(纯主体名,不含版权符号 / 年份 / "保留所有权利")。
 *
 * @param {string} locale
 * @returns {string}
 */
export function getCompanyName(locale) {
  const norm = normalizeLocale(locale)
  switch (norm) {
    case 'zh-CN':
      return COMPANY_NAME_ZH
    case 'zh-TW':
      return COMPANY_NAME_ZH_TW
    case 'ar':
      return COMPANY_NAME_AR
    case 'en':
    default:
      return COMPANY_NAME_EN
  }
}
