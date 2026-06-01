<script setup>
/**
 * PrivacyView — 隐私政策(Wave 5 第 12 轮锚点 + 进度条)
 *
 *  - 套 AppLayout(默认,router meta 无需改)
 *  - PC ≥ 768:左侧 sticky LegalSidebar 240px
 *  - H5 < 768:LegalTocDrawer 折叠目录(EP drawer placement=bottom 60vh)
 *  - 顶部 sticky LegalProgress(useScrollProgress)
 *  - IntersectionObserver active(useActiveSection,rootMargin -100px 0 -60% 0)
 *  - print stylesheet 隐藏 sidebar/progress/drawer
 *
 * 内容:沿用 i18n locales/legal-privacy/{en,zh-CN,zh-TW,ar}.js 11 章节(M2.5 已落 4 套)
 * 不修改既有法务措辞;只新增 lastUpdatedDate / lastUpdatedLabel / 等基础 i18n key。
 */

import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'
import { buildHead, jsonLd, SITE_ORIGIN, SITE_NAME } from '@/utils/seo'
import { useBreakpoint } from '@/composables/useBreakpoint'
import { useScrollProgress } from '@/composables/useScrollProgress'
import { useActiveSection } from '@/composables/useActiveSection'
import LegalSidebar from '@/components/legal/LegalSidebar.vue'
import LegalProgress from '@/components/legal/LegalProgress.vue'
import LegalTocDrawer from '@/components/legal/LegalTocDrawer.vue'

const { t, te, locale } = useI18n()
const { isPC } = useBreakpoint()

// 法务文案中需要插值的变量
const interpolations = computed(() => ({
  company: t('legal.privacy.companyName'),
  companyEn: t('legal.privacy.companyNameEn'),
  address: t('legal.privacy.companyAddress'),
  email: t('legal.privacy.contactEmail')
}))

const sectionKeys = [
  'intro',
  'collect',
  'use',
  'share',
  'cookies',
  'rights',
  'children',
  'crossBorder',
  'retention',
  'changes',
  'contact'
]

// 阅读进度 + active 章节
const { progress } = useScrollProgress()
const { activeId } = useActiveSection(sectionKeys, { idPrefix: 'section-' })

// H5 ToC drawer
const tocOpen = ref(false)

// 文档协议生效日期 — 修订时同步 4 套 i18n 的 legal.privacy.lastUpdatedDate 与 legal.privacy.lastUpdated
const PRIVACY_FALLBACK_DATE = '2026-05-09'

// 最后更新时间(优先 lastUpdatedDate;fallback 常量,法务页面不应展示当天日期)
const lastUpdatedDate = computed(() =>
  te('legal.privacy.lastUpdatedDate') ? t('legal.privacy.lastUpdatedDate') : PRIVACY_FALLBACK_DATE
)
const lastUpdatedText = computed(() => {
  if (te('legal.privacy.lastUpdatedLabel')) {
    return t('legal.privacy.lastUpdatedLabel', { date: lastUpdatedDate.value })
  }
  return t('legal.privacy.lastUpdated')
})

// SEO + JSON-LD(dateModified 读 lastUpdatedDate 单一真实源)
const headPayload = computed(() => {
  const base = buildHead({
    title: t('legal.privacy.seoTitle'),
    description: t('legal.privacy.seoDescription'),
    keywords: t('legal.privacy.seoKeywords'),
    path: '/legal/privacy',
    locale: locale.value,
    robots: 'index, follow'
  })
  return {
    ...base,
    script: [
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'WebPage',
        name: t('legal.privacy.title'),
        url: SITE_ORIGIN + '/legal/privacy',
        inLanguage: locale.value,
        description: t('legal.privacy.seoDescription'),
        dateModified: lastUpdatedDate.value,
        isPartOf: {
          '@type': 'Organization',
          name: SITE_NAME,
          url: SITE_ORIGIN + '/'
        }
      })
    ]
  }
})

useHead(headPayload)

function onSidebarSelect(key) {
  // 让浏览器默认 anchor 跳转(Wave 1 router scrollBehavior top: 100 接管平滑滚动)
  // 同步 hash(history.replaceState 避免触发新历史)
  if (typeof window !== 'undefined') {
    window.history.replaceState(null, '', `#section-${key}`)
  }
}

function onPrint() {
  if (typeof window !== 'undefined') window.print()
}
</script>

<template>
  <div class="legal-page">
    <LegalProgress :progress="progress" />

    <div class="legal-page__container">
      <aside v-if="isPC" class="legal-page__aside">
        <LegalSidebar
          :section-keys="sectionKeys"
          i18n-ns="legal.privacy.sections"
          :active-key="activeId"
          :title="t('legal.privacy.title')"
          icon="🔒"
          :show-print="true"
          @select="onSidebarSelect"
          @print="onPrint"
        />
      </aside>

      <main class="legal-page__main">
        <article class="legal-page__article" :lang="locale">
          <header class="legal-page__header">
            <h1 class="legal-page__title">{{ t('legal.privacy.title') }}</h1>
            <p class="legal-page__updated">{{ lastUpdatedText }}</p>

            <button
              v-if="!isPC"
              type="button"
              class="legal-page__toc-trigger"
              @click="tocOpen = true"
              :aria-label="t('legal.toc.title')"
            >
              <span aria-hidden="true">📜</span>
              <span>{{ t('legal.toc.title') }}</span>
              <span class="legal-page__toc-trigger-caret" aria-hidden="true">▾</span>
            </button>
          </header>

          <section
            v-for="key in sectionKeys"
            :key="key"
            :id="`section-${key}`"
            class="legal-page__section"
          >
            <h2 class="legal-page__h2">
              {{ t(`legal.privacy.sections.${key}.heading`) }}
            </h2>
            <p class="legal-page__body">
              {{ t(`legal.privacy.sections.${key}.body`, interpolations) }}
            </p>
          </section>

          <footer class="legal-page__footer">
            <p class="legal-page__legal">
              {{ t('legal.privacy.companyName') }}({{ t('legal.privacy.companyNameEn') }})
            </p>
            <p class="legal-page__legal">{{ t('legal.privacy.companyAddress') }}</p>
            <p class="legal-page__legal">
              <a :href="`mailto:${t('legal.privacy.contactEmail')}`" class="legal-page__email">
                {{ t('legal.privacy.contactEmail') }}
              </a>
            </p>
          </footer>
        </article>
      </main>
    </div>

    <LegalTocDrawer
      v-if="!isPC"
      v-model="tocOpen"
      :section-keys="sectionKeys"
      i18n-ns="legal.privacy.sections"
      :active-key="activeId"
      :title="t('legal.privacy.title')"
      icon="🔒"
      @select="onSidebarSelect"
    />
  </div>
</template>

<style lang="scss" scoped>
.legal-page {
  min-block-size: 100vh;
  background: brand.$color-bg-page;
}

.legal-page__container {
  max-inline-size: 1200px;
  margin: 0 auto;
  padding: brand.$spacing-6 brand.$spacing-4 brand.$spacing-12;
  display: flex;
  gap: brand.$spacing-8;
  align-items: flex-start;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8 brand.$spacing-6 brand.$spacing-16;
  }
}

.legal-page__aside {
  flex: 0 0 240px;
  position: sticky;
  inset-block-start: 100px;     // AppHeader 64 + progress 4 + 呼吸 32(配套 router scrollBehavior top: 100)
  align-self: flex-start;
}

.legal-page__main {
  flex: 1;
  min-inline-size: 0;
  display: flex;
  justify-content: center;
}

.legal-page__article {
  inline-size: 100%;
  max-inline-size: 800px;
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6 brand.$spacing-5;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-12 brand.$spacing-10;
  }
}

.legal-page__header {
  margin-block-end: brand.$spacing-8;
  padding-block-end: brand.$spacing-5;
  border-block-end: 1px solid brand.$color-divider;
}

.legal-page__title {
  margin: 0 0 brand.$spacing-3;
  font-family: brand.$font-family-heading-en;
  font-style: italic;
  font-size: brand.$font-size-2xl;
  font-weight: 700;
  color: brand.$brand-primary-deep;
  line-height: brand.$line-height-tight;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-3xl;
  }

  [lang='ar'] & {
    font-family: inherit;
    font-style: normal;
  }
}

.legal-page__updated {
  margin: 0 0 brand.$spacing-3;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-tertiary;
}

.legal-page__toc-trigger {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;
  padding: brand.$spacing-2 brand.$spacing-4;
  margin-block-start: brand.$spacing-3;
  background: brand.$brand-primary-soft;
  border: 1px solid brand.$brand-primary;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-sm;
  font-weight: 500;
  color: brand.$brand-primary-deep;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: brand.$brand-primary;
    color: brand.$color-text-inverse;
  }
}

.legal-page__toc-trigger-caret {
  margin-inline-start: auto;
}

.legal-page__section {
  margin-block-end: brand.$spacing-6;
  scroll-margin-block-start: 100px;   // anchor 跳转停留位置(配套 router scrollBehavior top: 100)
}

.legal-page__h2 {
  margin: 0 0 brand.$spacing-3;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  color: brand.$color-text-primary;
  line-height: brand.$line-height-tight;
  padding-inline-start: brand.$spacing-3;
  border-inline-start: 3px solid brand.$brand-primary;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-xl;
  }
}

.legal-page__body {
  margin: 0;
  font-size: brand.$font-size-md;
  line-height: 1.7;
  color: brand.$color-text-primary;
  text-align: start;
  word-break: break-word;
}

.legal-page__footer {
  margin-block-start: brand.$spacing-10;
  padding-block-start: brand.$spacing-5;
  border-block-start: 1px solid brand.$color-divider;
  text-align: start;
}

.legal-page__legal {
  margin: 0 0 brand.$spacing-1;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  line-height: brand.$line-height-base;
}

.legal-page__email {
  color: brand.$brand-primary-deep;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

// ---------- Print stylesheet ----------
@media print {
  .legal-page {
    background: #fff;
  }

  .legal-page__aside,
  .legal-page__toc-trigger {
    display: none !important;
  }

  .legal-page__container {
    padding: 0;
    max-inline-size: none;
    display: block;
  }

  .legal-page__article {
    border: none;
    box-shadow: none;
    padding: 0;
    max-inline-size: none;
  }

  .legal-page__section {
    page-break-inside: avoid;
    scroll-margin-block-start: 0;
  }

  .legal-page__h2 {
    page-break-after: avoid;
  }

  // 链接打印 href
  a[href^="http"]:after,
  a[href^="mailto:"]:after {
    content: ' (' attr(href) ')';
    font-size: 0.85em;
    color: #555;
  }
}
</style>
