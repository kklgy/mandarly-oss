import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import zhTW from './locales/zh-TW'
import en from './locales/en'
import ar from './locales/ar'
import { syncDayjsLocale } from '@/utils/datetime'

const STORAGE_KEY = 'mandarly_locale'
const RTL_LOCALES = new Set(['ar', 'he', 'fa', 'ur'])
export const PACKAGED_LOCALES = ['en', 'zh-CN', 'zh-TW', 'ar']
export const ENABLED_LOCALES = ['en', 'zh-CN', 'zh-TW']
export const DEFAULT_LOCALE = 'en'

export function normalizeEnabledLocale(locale) {
  return ENABLED_LOCALES.includes(locale) ? locale : DEFAULT_LOCALE
}

function detectLocale() {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (ENABLED_LOCALES.includes(saved)) return saved
  if (PACKAGED_LOCALES.includes(saved)) {
    localStorage.removeItem(STORAGE_KEY)
  }
  return DEFAULT_LOCALE
}

function applyDirection(locale) {
  const dir = RTL_LOCALES.has(locale) ? 'rtl' : 'ltr'
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('dir', dir)
    document.documentElement.setAttribute('lang', locale)
  }
}

const initialLocale = detectLocale()

const i18n = createI18n({
  legacy: false,
  locale: initialLocale,
  fallbackLocale: ['en', 'zh-CN'],
  messages: {
    'zh-CN': zhCN,
    'zh-TW': zhTW,
    'en': en,
    'ar': ar
  }
})

// 初始挂载时同步 dir + dayjs locale
applyDirection(initialLocale)
syncDayjsLocale(initialLocale)

/**
 * 切换语言(用户中心 / 顶栏切语言时调用)
 * 同步:vue-i18n locale + dayjs locale + <html dir> + localStorage
 */
export function setLocale(locale) {
  if (!ENABLED_LOCALES.includes(locale)) return
  i18n.global.locale.value = locale
  localStorage.setItem(STORAGE_KEY, locale)
  applyDirection(locale)
  syncDayjsLocale(locale)
}

export function isRTL(locale = i18n.global.locale.value) {
  return RTL_LOCALES.has(locale)
}

export default i18n
