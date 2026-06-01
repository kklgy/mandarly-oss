<script setup>
/**
 * App.vue — 用户端根组件
 *
 * 装配:
 *   - ElConfigProvider:动态 EP locale + z-index 2000(plan Wave 1.5.3)
 *   - VanConfigProvider:主题色 + RTL 跟随
 *   - 动态 layout(plan Wave 1.1):根据 route.meta.layout 切 AppLayout / AuthLayout / FullscreenLayout
 *
 * EP / Vant z-index 错开(plan § 11.7 修补 A1):
 *   - EP 默认 2000,显式声明
 *   - Vant overlay/popup 通过 CSS var 提到 2200,避免被 EP dialog 压底
 */
import { computed, ref, watch } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElConfigProvider } from 'element-plus'
import { ConfigProvider as VanConfigProvider } from 'vant'
import { isRTL } from '@/i18n'
import AppLayout from '@/layouts/AppLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import FullscreenLayout from '@/layouts/FullscreenLayout.vue'

const { locale } = useI18n()
const route = useRoute()
const rtl = computed(() => isRTL(locale.value))

// EP locale 动态 import(每个语言一个 chunk,避免一次加载 4 套)
// ar 若 EP 版本不带 ar.mjs,await 失败时 fallback en
const epLocaleLoaders = {
  'zh-CN': () => import('element-plus/dist/locale/zh-cn.mjs'),
  'zh-TW': () => import('element-plus/dist/locale/zh-tw.mjs'),
  'en':    () => import('element-plus/dist/locale/en.mjs'),
  'ar':    () => import('element-plus/dist/locale/ar.mjs')
}

const epLocale = ref(null)

async function loadEpLocale(lang) {
  const loader = epLocaleLoaders[lang] || epLocaleLoaders['en']
  try {
    const mod = await loader()
    epLocale.value = mod.default
  } catch (e) {
    if (import.meta.env.DEV) console.debug(`[App] EP locale ${lang} load failed, fallback en`, e)
    const fb = await epLocaleLoaders['en']()
    epLocale.value = fb.default
  }
}

// 初次加载 + 切语言时同步
watch(locale, (l) => loadEpLocale(l), { immediate: true })

// Vant 主题色变量(对齐 brand.scss CSS token)
const vantThemeVars = {
  primaryColor: 'var(--color-primary)',
  primaryColorBg: 'var(--color-primary-soft)',
  buttonPrimaryColor: 'var(--btn-primary-text)',
  buttonPrimaryBackground: 'var(--color-primary)',
  buttonPrimaryBorderColor: 'var(--color-primary)'
}

// 动态 layout 切换(plan Wave 1.1)
const layoutComponent = computed(() => {
  switch (route.meta.layout) {
    case 'auth': return AuthLayout
    case 'fullscreen': return FullscreenLayout
    default: return AppLayout  // 'app' 或未声明
  }
})
</script>

<template>
  <el-config-provider :locale="epLocale" :z-index="2000">
    <van-config-provider :theme-vars="vantThemeVars" :rtl="rtl">
      <component :is="layoutComponent">
        <RouterView />
      </component>
    </van-config-provider>
  </el-config-provider>
</template>

<style>
/* Vant overlay z-index 错开 EP(plan § 11.7 修补 A1)
   EP dialog z-index 默认 2000(已在 ElConfigProvider 显式声明),
   Vant popup/overlay 提到 2200,确保 EP dialog 上的 Vant ImagePreview / ActionSheet 在最上层 */
:root {
  --van-overlay-z-index: 2200;
  --van-popup-z-index: 2200;
}
</style>
