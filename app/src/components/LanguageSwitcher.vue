<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ENABLED_LOCALES, setLocale } from '@/i18n'

const { locale } = useI18n()

const LOCALE_LABELS = {
  en: 'English',
  'zh-CN': '简体中文',
  'zh-TW': '繁體中文'
}

const options = ENABLED_LOCALES.map((value) => ({
  value,
  label: LOCALE_LABELS[value]
}))

const current = computed({
  get: () => locale.value,
  set: (val) => setLocale(val)
})
</script>

<template>
  <select v-model="current" class="lang-switcher" aria-label="Language">
    <option v-for="opt in options" :key="opt.value" :value="opt.value">
      {{ opt.label }}
    </option>
  </select>
</template>

<style lang="scss" scoped>
// D27:统一 ghost 文字下拉风(与 AppHeader 登录文字链 + 注册 pill 视觉等级一致)
// base 无 border + 透明 bg,hover 浅色 bg;不再有 ghost outline 状态
.lang-switcher {
  appearance: none;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  padding: 0 brand.$spacing-3;
  height: 32px;
  font-family: var(--font-family-base);
  font-size: 13px;
  font-weight: 500;
  color: rgba(20, 20, 19, 0.72);
  letter-spacing: -0.005em;
  cursor: pointer;
  outline: none;
  transition: color 0.15s, background 0.15s;

  &:hover {
    color: rgba(20, 20, 19, 0.95);
    background: var(--color-bg-strong);
  }

  &:focus-visible {
    box-shadow: var(--ring-focus);
  }
}
</style>
