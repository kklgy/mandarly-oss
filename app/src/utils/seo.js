/**
 * SEO 工具:统一构造 useHead({ title, meta, link, script }) 入参
 *
 * 与 docs/frontend/seo-geo-checklist.md 配套:零成本 meta + canonical + og + JSON-LD
 * 一期 SPA 同 URL,hreflang 全部回到 https://mandarly.com{path}(不加 /zh /en 前缀)
 * URL 多语言重构留 backlog
 */

export const SITE_ORIGIN = 'https://mandarly.com'
export const SITE_NAME = 'Mandarly'
export const DEFAULT_OG_IMAGE = `${SITE_ORIGIN}/cover-mandarly-1500x500.png`

const LOCALE_TO_OG = {
  'zh-CN': 'zh_CN',
  'zh-TW': 'zh_TW',
  'en': 'en_US'
}

export function ogLocale(locale) {
  return LOCALE_TO_OG[locale] || 'en_US'
}

export function canonicalOf(path) {
  if (!path) return SITE_ORIGIN + '/'
  return SITE_ORIGIN + (path.startsWith('/') ? path : '/' + path)
}

/**
 * 构造一份 useHead({ ... }) 入参,合并:
 *   - <title>(字符串)
 *   - description / keywords / robots
 *   - canonical link
 *   - og:title / og:description / og:image / og:url / og:type / og:locale
 *   - twitter:card / twitter:title / twitter:description / twitter:image
 *   - <html lang>(通过 htmlAttrs)
 *
 * 调用方再单独追加 JSON-LD(script[type=application/ld+json])。
 */
export function buildHead({
  title,
  description,
  keywords,
  path = '/',
  ogImage = DEFAULT_OG_IMAGE,
  ogType = 'website',
  locale = 'en',
  robots = 'index, follow'
}) {
  const url = canonicalOf(path)
  const meta = [
    { name: 'description', content: description },
    { name: 'robots', content: robots },
    { property: 'og:title', content: title },
    { property: 'og:description', content: description },
    { property: 'og:image', content: ogImage },
    { property: 'og:url', content: url },
    { property: 'og:type', content: ogType },
    { property: 'og:site_name', content: SITE_NAME },
    { property: 'og:locale', content: ogLocale(locale) },
    { name: 'twitter:card', content: 'summary_large_image' },
    { name: 'twitter:title', content: title },
    { name: 'twitter:description', content: description },
    { name: 'twitter:image', content: ogImage }
  ]
  if (keywords) {
    meta.push({ name: 'keywords', content: keywords })
  }
  return {
    title,
    htmlAttrs: { lang: locale },
    meta,
    link: [{ rel: 'canonical', href: url }]
  }
}

/**
 * JSON-LD <script> entry 工厂,直接挂到 useHead({ script: [jsonLd(obj)] })
 */
export function jsonLd(obj) {
  return {
    type: 'application/ld+json',
    children: JSON.stringify(obj)
  }
}
