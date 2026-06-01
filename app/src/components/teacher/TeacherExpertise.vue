<script setup>
// =============================================================================
// <TeacherExpertise> — TeacherDetail § D 教学专长 chip 列表
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § TeacherDetailView 改造 § D
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  expertise: {
    type: Array,
    default: () => []
  }
})

const { t, te } = useI18n()
const t2 = (key, fallback) => (te(key) ? t(key) : fallback)

const hasItems = computed(() => Array.isArray(props.expertise) && props.expertise.length > 0)
const displayExpertise = computed(() => {
  if (!hasItems.value) return []
  return props.expertise.map((code) => t2(`teacher.expertise.${code}`, code))
})
</script>

<template>
  <section v-if="hasItems" class="td-expertise">
    <h2 class="td-expertise__title">{{ t2('teacherDetail.expertise.title', '教学专长') }}</h2>
    <ul class="td-expertise__list">
      <li
        v-for="(chip, idx) in displayExpertise"
        :key="idx"
        class="td-expertise__chip"
      >
        {{ chip }}
      </li>
    </ul>
  </section>
</template>

<style scoped lang="scss">

.td-expertise {
  background: var(--color-bg-card);
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6 brand.$spacing-5;
  box-shadow: brand.$shadow-sm;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__list {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
  }

  &__chip {
    padding: brand.$spacing-1 brand.$spacing-4;
    background: brand.$brand-primary-soft;
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-sm;
    font-weight: 500;
    border-radius: brand.$radius-pill;
    border: 1px solid transparent;
    transition: border-color 0.15s ease;

    &:hover {
      border-color: brand.$brand-primary;
    }
  }
}
</style>
