<script setup>
/**
 * AuthTrustSignals — 鉴权页左栏信任信号(P2-1)
 *
 * 显示 3 项小标:认证老师数 / 评分 / 国家学员数
 * 文案走 i18n authHero.trust.*(支持 {rating} {count} 插值)
 *
 * 来源: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § Auth 双栏氛围版
 */
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const items = [
  { key: 'rating', icon: '★', value: { rating: '4.9' } },
  { key: 'teachers', icon: '✓', value: { count: '600' } },
  { key: 'countries', icon: '⌖', value: { count: '20' } }
]
</script>

<template>
  <ul class="auth-trust" role="list">
    <li v-for="item in items" :key="item.key" class="auth-trust__item">
      <span class="auth-trust__icon" aria-hidden="true">{{ item.icon }}</span>
      <span class="auth-trust__text">{{ t(`authHero.trust.${item.key}`, item.value) }}</span>
    </li>
  </ul>
</template>

<style lang="scss" scoped>
.auth-trust {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;

  &__item {
    display: flex;
    align-items: center;
    gap: brand.$spacing-2;
    font-size: brand.$font-size-xs;
    color: var(--color-text-secondary);
    line-height: brand.$line-height-base;
  }

  &__icon {
    display: inline-grid;
    place-items: center;
    width: 20px;
    height: 20px;
    border-radius: var(--radius-full);
    background: var(--color-primary-soft);
    color: var(--color-primary-deep);
    font-size: 11px;
    font-weight: 700;
    flex-shrink: 0;
  }
}
</style>
