<script setup>
// =============================================================================
// <TeacherSortSelect> — TeacherListView § D 排序下拉
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § D 排序下拉(第 6 轮)
//
// 5 项: 推荐(默认) / 评分高→低 / 价格低→高 / 价格高→低 / 评价数多→少
// (DESIGN 第 6 轮 § D 列出 5 项,任务 prompt 把"新教师"换成"评价数",采用 prompt 拍板)
//
// 严格约束:
//   1) i18n: 调用方传 label 数组 (key + label 对象)
//   2) token only
//   3) URL ?sort= 同步由父级负责
// =============================================================================
const props = defineProps({
  modelValue: {
    type: String,
    default: 'recommend'
  },
  // 选项数组 [{ value, label }],i18n 调用方组装
  options: {
    type: Array,
    required: true
  },
  // 整体显示在控件前的 label,如"排序:"
  prefixLabel: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

function onChange(value) {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<template>
  <div class="teacher-sort-select">
    <span v-if="prefixLabel" class="teacher-sort-select__label">{{ prefixLabel }}</span>
    <el-select
      :model-value="modelValue"
      size="default"
      class="teacher-sort-select__control"
      @change="onChange"
    >
      <el-option
        v-for="opt in options"
        :key="opt.value"
        :value="opt.value"
        :label="opt.label"
      />
    </el-select>
  </div>
</template>

<style scoped lang="scss">
.teacher-sort-select {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;

  &__label {
    font-size: brand.$font-size-base;
    color: brand.$color-text-secondary;
    white-space: nowrap;
  }

  &__control {
    width: 160px;

    :deep(.el-select__wrapper) {
      border-radius: brand.$radius-base;
    }

    :deep(.el-select__wrapper.is-focus) {
      box-shadow:
        0 0 0 1px brand.$brand-primary inset,
        brand.$ring-focus;
    }
  }
}
</style>
