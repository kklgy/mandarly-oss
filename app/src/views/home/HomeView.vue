<script setup>
// =============================================================================
// HomeView — 8 节产品级落地页(D25 视觉浓度升级版,2026-05-21)
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § HomeView 改造(2026-05-09 第 2 轮定版)
//   - docs/progress/D25-UI视觉浓度升级.md(2026-05-21 视觉浓度升级,加 § 0 / § 5)
//
// 8 节:
//   § 1 HeroSection           深色多色渐变 + 大字 + 双 pill CTA + AI 真人配图(D25 浓化)
//   § 0 StatsBar              4 宫格数字统计带(D25 新增)
//   § 2 FeatureCards          图文交错 4 张(母语 / 零基础 / 灵活 / 专业认证师资,D25 浓化)
//   § 3 RecommendedTeachers   公开可见推荐教师(D25 解除登录闸门,对齐 D20 价格锁定路线)
//   § 4 PackagePreview        渐变背景 + 推荐徽章(D25 浓化,价格锁定沿用 D20)
//   § 5 TestimonialCards      3 张学员见证示例(D25 补做 — 文案对冲策略)
//   § 6 HomeFAQ               米色底 + EP collapse + FAQPage JSON-LD(沿用)
//   § 7 BottomCTABanner       多色渐变 + 双 pill CTA(D25 浓化)
//
// SEO:
//   - <title> / <meta description> / hreflang 由 useHead + buildHead 拼
//   - Organization + WebSite JSON-LD 保留
//   - FAQPage JSON-LD 由 HomeFAQ.vue 内部 useHead 注入(混合模式)
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'
import { buildHead, jsonLd, SITE_ORIGIN, SITE_NAME, DEFAULT_OG_IMAGE } from '@/utils/seo'
import HeroSection from '@/components/home/HeroSection.vue'
import StatsBar from '@/components/home/StatsBar.vue'
import HowItWorks from '@/components/home/HowItWorks.vue'
import CurriculumPath from '@/components/home/CurriculumPath.vue'
import FeatureCards from '@/components/home/FeatureCards.vue'
import RecommendedTeachers from '@/components/home/RecommendedTeachers.vue'
import PackagePreview from '@/components/home/PackagePreview.vue'
import TestimonialCards from '@/components/home/TestimonialCards.vue'
import HomeFAQ from '@/components/home/HomeFAQ.vue'
import BottomCTABanner from '@/components/home/BottomCTABanner.vue'

const { t, locale } = useI18n()

const headPayload = computed(() => {
  const base = buildHead({
    title: t('seo.home.title'),
    description: t('seo.home.description'),
    keywords: t('seo.home.keywords'),
    path: '/',
    locale: locale.value
  })
  return {
    ...base,
    script: [
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'Organization',
        name: SITE_NAME,
        alternateName: '曼德勵',
        url: SITE_ORIGIN + '/',
        logo: SITE_ORIGIN + '/logo-256.png',
        image: DEFAULT_OG_IMAGE,
        description: t('seo.home.description'),
        sameAs: []
      }),
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'WebSite',
        name: SITE_NAME,
        url: SITE_ORIGIN + '/',
        inLanguage: ['zh-CN', 'zh-TW', 'en', 'ar']
      }),
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'VideoObject',
        name: t('home.hero.video.title'),
        description: t('home.hero.video.caption'),
        thumbnailUrl: DEFAULT_OG_IMAGE,
        uploadDate: '2026-05-26',
        contentUrl: 'https://mandarly-demo-bucket.cos.ap-hongkong.myqcloud.com/marketing/home/mandarly-class-demo-20260526.mp4',
        embedUrl: SITE_ORIGIN + '/'
      })
    ]
  }
})

useHead(headPayload)
</script>

<template>
  <div class="home-view">
    <HeroSection />
    <StatsBar />
    <HowItWorks />
    <div id="curriculum-preview"><CurriculumPath /></div>
    <div id="features"><FeatureCards /></div>
    <div id="teachers-section"><RecommendedTeachers /></div>
    <div id="packages-section"><PackagePreview /></div>
    <div id="stories"><TestimonialCards /></div>
    <div id="faq"><HomeFAQ /></div>
    <BottomCTABanner />
  </div>
</template>

<style lang="scss" scoped>
.home-view {
  // 各 section 自带底色与上下 padding,本壳不加内边距
  background: var(--color-bg-page);

  // D26:section anchor 滚动留白(避开 sticky header 64px + 16px buffer)
  & > div[id] {
    scroll-margin-block-start: 80px;
  }
}
</style>
