<script setup>
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'

/**
 * Mock 模式专用占位课堂页(被 ClassroomView 的 iframe 嵌入)
 *
 * 真实凭证就位 + LCIC_MODE=real 后,后端 join-info 返回腾讯云域名,本页不再被命中。
 *
 * 仅本地 / dev e2e 测试用,展示订单上下文 + 提示「这是 mock 课堂」。
 */

const route = useRoute()
const { t } = useI18n()

const params = computed(() => ({
  classId: route.query.class || '—',
  role: route.query.role || '—',
  userId: route.query.userId || '—',
  token: route.query.token || '—'
}))
</script>

<template>
  <div class="cs-stub">
    <div class="cs-card">
      <h2 class="cs-title">{{ t('classroom.stub.title') }}</h2>
      <p class="cs-desc">{{ t('classroom.stub.desc') }}</p>

      <dl class="cs-rows">
        <div class="cs-row">
          <dt>{{ t('classroom.stub.classId') }}</dt>
          <dd>{{ params.classId }}</dd>
        </div>
        <div class="cs-row">
          <dt>{{ t('classroom.stub.role') }}</dt>
          <dd>{{ params.role }}</dd>
        </div>
        <div class="cs-row">
          <dt>{{ t('classroom.stub.userId') }}</dt>
          <dd>{{ params.userId }}</dd>
        </div>
        <div class="cs-row">
          <dt>{{ t('classroom.stub.token') }}</dt>
          <dd class="cs-mono">{{ params.token }}</dd>
        </div>
      </dl>

      <p class="cs-hint">{{ t('classroom.stub.hint') }}</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.cs-stub {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: brand.$spacing-6;
  background: brand.$color-bg-page;
  font-family: brand.$font-family-base;
}

.cs-card {
  max-width: 480px;
  width: 100%;
  background: brand.$color-bg-card;
  border-radius: brand.$radius-card;
  border: 1px dashed brand.$brand-primary;
  padding: brand.$spacing-6;
  box-shadow: brand.$shadow-base;
}

.cs-title {
  margin: 0 0 brand.$spacing-2;
  font-size: brand.$font-size-xl;
  color: brand.$brand-primary-deep;
}

.cs-desc {
  margin: 0 0 brand.$spacing-5;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  line-height: brand.$line-height-base;
}

.cs-rows {
  margin: 0 0 brand.$spacing-4;
}

.cs-row {
  display: flex;
  gap: brand.$spacing-3;
  padding: brand.$spacing-2 0;
  border-top: 1px solid brand.$color-divider;
  font-size: brand.$font-size-sm;

  &:first-child { border-top: none; }

  dt {
    width: 90px;
    color: brand.$color-text-tertiary;
  }
  dd {
    flex: 1;
    margin: 0;
    color: brand.$color-text-primary;
    word-break: break-all;
  }
}

.cs-mono {
  font-family: monospace;
  font-size: brand.$font-size-xs;
}

.cs-hint {
  margin: 0;
  padding: brand.$spacing-3;
  background: brand.$brand-primary-soft;
  border-radius: brand.$radius-base;
  font-size: brand.$font-size-xs;
  color: brand.$brand-primary-deep;
  line-height: brand.$line-height-base;
}
</style>
