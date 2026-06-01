<script setup>
// =============================================================================
// <HomeFAQ> — HomeView § 6 FAQ
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § HomeView 改造 → § 6 FAQ(6 题折叠 + JSON-LD)
//
// 视觉:米色底,padding-block 64px / 48px
//   6 条 FAQ,EP <el-collapse accordion>,默认全闭合
//
// JSON-LD:FAQPage schema(SEO + GEO 大头),通过 useHead 注入 <script type="application/ld+json">
//
// 文案 i18n:home.faq.{title|q1|q2|q3|q4|q5|q6}.{q|a}
//
// 严格约束:
//   1) 0 v-html(plan §11.8 — XSS 防御)— el-collapse-item 文本内容用插值
//   2) JSON-LD 用 useHead 而非 v-html,避免 XSS
//   3) token only
// =============================================================================
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'

const { t } = useI18n()

const FAQ_KEYS = ['q1', 'q2', 'q3', 'q4', 'q5', 'q6']

const activeNames = ref([]) // 默认全闭合(accordion 模式仍接收数组)

const faqList = computed(() =>
  FAQ_KEYS.map((k) => ({
    key: k,
    q: t(`home.faq.${k}.q`),
    a: t(`home.faq.${k}.a`)
  }))
)

// FAQPage JSON-LD —— 通过 useHead 注入,避免 v-html XSS
useHead(
  computed(() => ({
    script: [
      {
        type: 'application/ld+json',
        // vue-meta / @vueuse/head 会把 children 序列化到 script innerText
        children: JSON.stringify({
          '@context': 'https://schema.org',
          '@type': 'FAQPage',
          mainEntity: faqList.value.map((item) => ({
            '@type': 'Question',
            name: item.q,
            acceptedAnswer: {
              '@type': 'Answer',
              text: item.a
            }
          }))
        })
      }
    ]
  }))
)
</script>

<template>
  <section class="home-faq">
    <div class="home-faq__inner">
      <h2 class="home-faq__title">{{ t('home.faq.title') }}</h2>

      <el-collapse v-model="activeNames" accordion class="home-faq__collapse">
        <el-collapse-item
          v-for="item in faqList"
          :key="item.key"
          :name="item.key"
          :title="item.q"
        >
          <p class="home-faq__answer">{{ item.a }}</p>
        </el-collapse-item>
      </el-collapse>
    </div>
  </section>
</template>

<style scoped lang="scss">

.home-faq {
  background: var(--color-primary-soft);
  padding-block: brand.$spacing-12;
  padding-inline: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding-block: brand.$spacing-16;
    padding-inline: brand.$spacing-8;
  }
}

.home-faq__inner {
  max-width: 800px;
  margin-inline: auto;
}

.home-faq__title {
  margin: 0 0 brand.$spacing-10;
  text-align: center;
  font-size: brand.$font-size-xl;
  font-weight: 600;
  color: brand.$color-text-primary;
  line-height: brand.$line-height-tight;

  @media (min-width: brand.$bp-tablet) {
    font-size: brand.$font-size-2xl;
  }
}

.home-faq__collapse {
  --el-collapse-border-color: #{brand.$color-border};
  --el-collapse-header-bg-color: transparent;
  --el-collapse-content-bg-color: transparent;
  --el-collapse-header-text-color: #{brand.$color-text-primary};
  --el-collapse-header-font-size: #{brand.$font-size-md};
  --el-collapse-content-font-size: #{brand.$font-size-base};

  background: brand.$color-bg-card;
  border-radius: brand.$radius-lg;
  padding-inline: brand.$spacing-6;

  :deep(.el-collapse-item__header) {
    font-weight: 500;
    line-height: brand.$line-height-base;
    padding-block: brand.$spacing-4;
  }

  :deep(.el-collapse-item__content) {
    padding-block-end: brand.$spacing-5;
  }
}

.home-faq__answer {
  margin: 0;
  font-size: brand.$font-size-base;
  color: brand.$color-text-body;
  line-height: brand.$line-height-loose;
}
</style>
