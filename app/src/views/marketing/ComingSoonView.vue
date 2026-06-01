<script setup>
/**
 * ComingSoonView — 占位页(教材 / 资源中心 等待内容填充时使用)
 *
 * 通过 route.meta.pageKey 决定渲染哪份文案:
 *   - 'textbook'  → comingSoon.textbook.{title,desc}
 *   - 'resources' → comingSoon.resources.{title,desc}
 *
 * 视觉契约:DESIGN-mandarly-v2.md(暖底 + 大标题 + 主色 pill CTA)
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'
import { buildHead } from '@/utils/seo'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()

const pageKey = computed(() => route.meta?.pageKey || 'textbook')
const title = computed(() => t(`comingSoon.${pageKey.value}.title`))
const desc = computed(() => t(`comingSoon.${pageKey.value}.desc`))

useHead(computed(() => buildHead({
  title: `${title.value} · Mandarly`,
  description: desc.value,
  path: route.path,
  locale: locale.value
})))

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="coming-soon">
    <div class="coming-soon__inner">
      <span class="coming-soon__badge">{{ t('comingSoon.badge') }}</span>
      <h1 class="coming-soon__title">{{ title }}</h1>
      <p class="coming-soon__desc">{{ desc }}</p>
      <button type="button" class="coming-soon__cta" @click="goHome">
        {{ t('comingSoon.cta.backHome') }}
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.coming-soon {
  min-height: calc(100vh - 64px);
  background:
    radial-gradient(80% 60% at 20% 0%, rgba(172, 114, 44, 0.06), transparent 60%),
    radial-gradient(60% 50% at 80% 30%, rgba(26, 54, 96, 0.05), transparent 60%),
    brand.$canvas-warm;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: brand.$spacing-12 brand.$spacing-6;
}

.coming-soon__inner {
  max-width: 640px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-5;
}

.coming-soon__badge {
  display: inline-block;
  font-size: brand.$font-size-xs;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: brand.$brand-primary-deep;
  background: brand.$brand-primary-soft;
  border-radius: brand.$radius-full;
  padding: 6px 14px;
}

.coming-soon__title {
  margin: 0;
  font-family: brand.$font-display;
  font-size: brand.$font-size-display-md;
  font-weight: brand.$font-weight-display;
  line-height: brand.$lh-display-sm;
  color: brand.$ink;

  &:lang(zh-CN),
  &:lang(zh-TW),
  &:lang(ar) {
    font-family: brand.$font-sans;
    font-weight: 700;
    letter-spacing: 0;
  }
}

.coming-soon__desc {
  margin: 0;
  font-size: brand.$font-size-md;
  color: brand.$body;
  line-height: brand.$lh-body-loose;
  max-width: 480px;
}

.coming-soon__cta {
  margin-block-start: brand.$spacing-2;
  border: none;
  background: brand.$brand-primary;
  color: #fff;
  font-size: brand.$font-size-base;
  font-weight: 600;
  padding: 12px 28px;
  border-radius: brand.$radius-full;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;

  &:hover {
    background: brand.$brand-primary-deep;
    transform: translateY(-1px);
  }
}
</style>
