<script setup>
/**
 * LegalSidebar — 法务页 PC 左侧 sticky 锚点导航(Wave 5 第 12 轮)
 *
 * Props:
 *  - sectionKeys : String[] 章节 key 列表(如 ['intro','collect',...])
 *  - i18nNs      : String 章节标题 i18n 命名空间(如 'legal.privacy.sections')
 *  - activeKey   : String 当前激活章节(由 useActiveSection 提供)
 *  - showPrint   : Boolean 是否显示底部打印按钮(PC 用,H5 不需要)
 *  - title       : String 顶部页眉标题(可选,如"隐私政策")
 *  - icon        : String 顶部 icon emoji(可选,如 🔒)
 *
 * Events:
 *  - print: 用户点击打印按钮
 *  - select(key): 用户点击章节(父组件可同步 hash 与平滑滚动)
 */

import { useI18n } from 'vue-i18n'

defineProps({
  sectionKeys: { type: Array, required: true },
  i18nNs: { type: String, required: true },
  activeKey: { type: String, default: '' },
  showPrint: { type: Boolean, default: true },
  title: { type: String, default: '' },
  icon: { type: String, default: '' }
})

const emit = defineEmits(['print', 'select'])

const { t } = useI18n()

function onClick(e, key) {
  // 不阻止默认 anchor 行为(让浏览器原生 anchor 跳转 + scrollBehavior 平滑滚动 + top: 100 偏移)
  // 但通知父级,以便其他副作用(关 drawer 等)
  emit('select', key)
}
</script>

<template>
  <aside class="legal-sidebar" aria-label="document table of contents">
    <header v-if="title" class="legal-sidebar__header">
      <span v-if="icon" class="legal-sidebar__icon" aria-hidden="true">{{ icon }}</span>
      <span class="legal-sidebar__title">{{ title }}</span>
    </header>

    <nav class="legal-sidebar__nav" aria-label="legal sections">
      <ol class="legal-sidebar__list">
        <li
          v-for="(key, idx) in sectionKeys"
          :key="key"
          class="legal-sidebar__item"
          :class="{ 'is-active': activeKey === key }"
        >
          <a
            :href="`#section-${key}`"
            class="legal-sidebar__link"
            :aria-current="activeKey === key ? 'true' : null"
            @click="onClick($event, key)"
          >
            <span class="legal-sidebar__num">{{ idx + 1 }}.</span>
            <span class="legal-sidebar__label">{{ t(`${i18nNs}.${key}.heading`) }}</span>
          </a>
        </li>
      </ol>
    </nav>

    <footer v-if="showPrint" class="legal-sidebar__footer">
      <button type="button" class="legal-sidebar__print" @click="emit('print')">
        <span aria-hidden="true">🖨️</span>
        <span>{{ t('legal.print') }}</span>
      </button>
    </footer>
  </aside>
</template>

<style lang="scss" scoped>
.legal-sidebar {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-3;
  inline-size: 240px;
  padding: brand.$spacing-5 brand.$spacing-4;
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-lg;
}

.legal-sidebar__header {
  display: flex;
  align-items: center;
  gap: brand.$spacing-2;
  padding-block-end: brand.$spacing-3;
  border-block-end: 1px solid brand.$color-divider;
}

.legal-sidebar__icon {
  font-size: brand.$font-size-lg;
  line-height: 1;
}

.legal-sidebar__title {
  font-size: brand.$font-size-base;
  font-weight: 600;
  color: brand.$color-text-primary;
}

.legal-sidebar__nav {
  flex: 1;
}

.legal-sidebar__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-1;
}

.legal-sidebar__item {
  position: relative;
  border-radius: brand.$radius-base;
  transition: background 0.18s;

  &.is-active {
    background: brand.$brand-primary-soft;
  }

  &.is-active::before {
    content: '';
    position: absolute;
    inset-inline-start: 0;
    inset-block-start: brand.$spacing-2;
    inset-block-end: brand.$spacing-2;
    inline-size: 3px;
    background: brand.$brand-primary;
    border-radius: brand.$radius-sm;
  }
}

.legal-sidebar__link {
  display: flex;
  align-items: flex-start;
  gap: brand.$spacing-2;
  padding: brand.$spacing-2 brand.$spacing-3;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  text-decoration: none;
  line-height: brand.$line-height-base;
  border-radius: brand.$radius-base;
  transition: color 0.18s;

  &:hover {
    color: brand.$brand-primary-deep;
  }

  .is-active & {
    color: brand.$brand-primary-deep;
    font-weight: 500;
  }
}

.legal-sidebar__num {
  flex: 0 0 auto;
  font-variant-numeric: tabular-nums;
  color: brand.$color-text-tertiary;

  .is-active & {
    color: brand.$brand-primary-deep;
  }
}

.legal-sidebar__label {
  flex: 1;
  word-break: break-word;
}

.legal-sidebar__footer {
  padding-block-start: brand.$spacing-3;
  border-block-start: 1px solid brand.$color-divider;
}

.legal-sidebar__print {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;
  padding: brand.$spacing-2 brand.$spacing-3;
  background: transparent;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;

  &:hover {
    border-color: brand.$brand-primary;
    color: brand.$brand-primary-deep;
  }
}
</style>
