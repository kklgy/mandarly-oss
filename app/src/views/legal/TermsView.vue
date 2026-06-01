<script setup>
/**
 * TermsView — 服务条款(Wave 5 第 12 轮锚点 + 进度条)
 *
 * 与 PrivacyView 镜像装配:
 *  - 套 AppLayout(默认)
 *  - PC 左 sticky LegalSidebar 240px
 *  - H5 LegalTocDrawer
 *  - 顶部 sticky LegalProgress
 *  - useScrollProgress + useActiveSection
 *
 * 内容:沿用 i18n locales/legal-terms/{en,zh-CN,zh-TW,ar}.js 14 章节(M2.5 已落 4 套)
 * 退款规则在既有 paymentRefund 章节中(第 5 章 5.3 项),anchor 用 #section-paymentRefund.
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

const SECTION_KEYS = [
  'intro',
  'accountRules',
  'serviceContent',
  'userObligations',
  'paymentRefund',
  'teacherIncome',
  'referral',
  'intellectualProperty',
  'liabilityLimit',
  'termination',
  'governingLaw',
  'changes',
  'finalInterpretation',
  'contact'
]

const { progress } = useScrollProgress()
const { activeId } = useActiveSection(SECTION_KEYS, { idPrefix: 'section-' })

const tocOpen = ref(false)

// 最后更新(优先 lastUpdatedDate)
const lastUpdatedDate = computed(() =>
  te('legal.terms.lastUpdatedDate') ? t('legal.terms.lastUpdatedDate') : '2026-05-09'
)
const lastUpdatedText = computed(() =>
  te('legal.terms.lastUpdatedLabel')
    ? t('legal.terms.lastUpdatedLabel', { date: lastUpdatedDate.value })
    : t('legal.terms.lastUpdated')
)

const headPayload = computed(() => {
  const base = buildHead({
    title: t('legal.terms.seoTitle'),
    description: t('legal.terms.seoDescription'),
    keywords: t('legal.terms.seoKeywords'),
    path: '/legal/terms',
    locale: locale.value,
    robots: 'index, follow'
  })
  return {
    ...base,
    script: [
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'WebPage',
        name: base.title,
        url: SITE_ORIGIN + '/legal/terms',
        inLanguage: locale.value,
        description: t('legal.terms.seoDescription'),
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
          :section-keys="SECTION_KEYS"
          i18n-ns="legal.terms.sections"
          :active-key="activeId"
          :title="t('legal.terms.title')"
          icon="📜"
          :show-print="true"
          @select="onSidebarSelect"
          @print="onPrint"
        />
      </aside>

      <main class="legal-page__main">
        <article class="legal-page__article" :lang="locale">
          <header class="legal-page__header">
            <h1 class="legal-page__title">{{ t('legal.terms.title') }}</h1>
            <p class="legal-page__updated">{{ lastUpdatedText }}</p>
            <p v-if="te('legal.terms.effectiveDate')" class="legal-page__updated">
              {{ t('legal.terms.effectiveDate') }}
            </p>

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
            v-for="key in SECTION_KEYS"
            :key="key"
            :id="`section-${key}`"
            class="legal-page__section"
            :aria-labelledby="`section-${key}-h2`"
          >
            <h2 :id="`section-${key}-h2`" class="legal-page__h2">
              {{ t(`legal.terms.sections.${key}.heading`) }}
            </h2>
            <p class="legal-page__body">
              {{ t(`legal.terms.sections.${key}.body`) }}
            </p>
          </section>

          <footer class="legal-page__footer">
            <p class="legal-page__legal">{{ t('legal.terms.companyName') }}</p>
            <p v-if="te('legal.terms.companyNameEn')" class="legal-page__legal">
              {{ t('legal.terms.companyNameEn') }}
            </p>
            <p class="legal-page__legal">{{ t('legal.terms.companyAddress') }}</p>
            <p class="legal-page__legal">
              <a :href="`mailto:${t('legal.terms.contactEmail')}`" class="legal-page__email">
                {{ t('legal.terms.contactEmail') }}
              </a>
            </p>
          </footer>
        </article>
      </main>
    </div>

    <LegalTocDrawer
      v-if="!isPC"
      v-model="tocOpen"
      :section-keys="SECTION_KEYS"
      i18n-ns="legal.terms.sections"
      :active-key="activeId"
      :title="t('legal.terms.title')"
      icon="📜"
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
  inset-block-start: 100px;
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
  border-radius: brand.$radius-xl;
  padding: brand.$spacing-6 brand.$spacing-5;
  box-shadow: brand.$shadow-base;

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
  margin: 0 0 brand.$spacing-2;
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
  margin-block-end: brand.$spacing-8;
  scroll-margin-block-start: 100px;

  &:last-of-type {
    margin-block-end: 0;
  }
}

.legal-page__h2 {
  margin: 0 0 brand.$spacing-3;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  color: brand.$color-text-primary;
  line-height: brand.$line-height-base;
  padding-inline-start: brand.$spacing-3;
  border-inline-start: 3px solid brand.$brand-primary;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-xl;
  }
}

.legal-page__body {
  margin: 0;
  font-size: brand.$font-size-md;
  color: brand.$color-text-secondary;
  line-height: 1.7;
  white-space: pre-line;
  word-wrap: break-word;
  overflow-wrap: anywhere;
}

.legal-page__footer {
  margin-block-start: brand.$spacing-12;
  padding-block-start: brand.$spacing-6;
  border-block-start: 1px solid brand.$color-border;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-tertiary;
  line-height: brand.$line-height-base;
}

.legal-page__legal {
  margin: 0 0 brand.$spacing-1;
}

.legal-page__email {
  color: brand.$brand-primary-deep;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

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

  a[href^="http"]:after,
  a[href^="mailto:"]:after {
    content: ' (' attr(href) ')';
    font-size: 0.85em;
    color: #555;
  }
}
</style>
