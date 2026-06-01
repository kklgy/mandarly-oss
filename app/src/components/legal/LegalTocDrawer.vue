<script setup>
/**
 * LegalTocDrawer — H5 折叠 ToC 抽屉(Wave 5 第 12 轮)
 *
 *  - EP el-drawer placement="bottom" size="60vh"
 *  - 内嵌 LegalSidebar(showPrint=false,H5 用浏览器原生 Cmd+P)
 *  - 章节点击关 drawer + select 事件冒泡
 *
 * Props:
 *  - modelValue (v-model) : Boolean drawer 显示
 *  - sectionKeys / i18nNs / activeKey / title / icon : 透传给 LegalSidebar
 *
 * Events:
 *  - update:modelValue
 *  - select(key)
 */

import { ElDrawer } from 'element-plus'
import { useI18n } from 'vue-i18n'
import LegalSidebar from './LegalSidebar.vue'

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  sectionKeys: { type: Array, required: true },
  i18nNs: { type: String, required: true },
  activeKey: { type: String, default: '' },
  title: { type: String, default: '' },
  icon: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'select'])

const { t } = useI18n()

function onSelect(key) {
  emit('select', key)
  emit('update:modelValue', false)  // 选完关 drawer
}
</script>

<template>
  <ElDrawer
    :model-value="modelValue"
    direction="btt"
    size="60vh"
    :with-header="false"
    :show-close="false"
    class="legal-toc-drawer"
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <div class="legal-toc-drawer__inner">
      <header class="legal-toc-drawer__header">
        <h3 class="legal-toc-drawer__title">{{ t('legal.toc.title') }}</h3>
        <button type="button" class="legal-toc-drawer__close" @click="emit('update:modelValue', false)" :aria-label="t('legal.toc.close')">×</button>
      </header>
      <LegalSidebar
        :section-keys="sectionKeys"
        :i18n-ns="i18nNs"
        :active-key="activeKey"
        :title="title"
        :icon="icon"
        :show-print="false"
        class="legal-toc-drawer__sidebar"
        @select="onSelect"
      />
    </div>
  </ElDrawer>
</template>

<style lang="scss" scoped>
.legal-toc-drawer__inner {
  display: flex;
  flex-direction: column;
  block-size: 100%;
}

.legal-toc-drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: brand.$spacing-3 brand.$spacing-4;
  border-block-end: 1px solid brand.$color-divider;
}

.legal-toc-drawer__title {
  margin: 0;
  font-size: brand.$font-size-base;
  font-weight: 600;
  color: brand.$color-text-primary;
}

.legal-toc-drawer__close {
  background: transparent;
  border: none;
  font-size: brand.$font-size-2xl;
  line-height: 1;
  color: brand.$color-text-tertiary;
  cursor: pointer;
  padding: brand.$spacing-1 brand.$spacing-2;

  &:hover {
    color: brand.$color-text-primary;
  }
}

.legal-toc-drawer__sidebar {
  flex: 1;
  overflow-y: auto;
  border: none;
  border-radius: 0;
  padding-block: brand.$spacing-3;
}

@media print {
  .legal-toc-drawer {
    display: none !important;
  }
}
</style>
