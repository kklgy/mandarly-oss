<script setup>
// =============================================================================
// <TeacherSearchBar> — TeacherListView 顶部 sticky 搜索 orb
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § A 搜索 orb (Wave 4 / 第 6 轮)
//
// 行为:
//   - EP el-input size="large", pill 全圆角, 主色 focus ring
//   - debounce 400ms emit 'search' (父级最终发请求)
//   - 立即 emit 'update:modelValue' 同步 v-model (URL ?keyword= 同步)
//   - 清空按钮 EP clearable
//   - i18n: 调用方传 placeholder
//
// 严格约束:
//   1) token only - 0 硬编色 / px / 字号
//   2) RTL: el-input 内置自动镜像;icon 走 prefix slot
//   3) 不在 <style scoped> 写 @use '@/assets/styles/brand'(vite additionalData 已注入)
// =============================================================================
import { ref, watch, onBeforeUnmount } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  // debounce 毫秒;默认 400ms
  debounce: {
    type: Number,
    default: 400
  }
})

const emit = defineEmits(['update:modelValue', 'search'])

const local = ref(props.modelValue)

// 父级 URL 还原 / 重置时同步
watch(
  () => props.modelValue,
  (v) => {
    if (v !== local.value) local.value = v
  }
)

let timer = null

function onInput(value) {
  // EP el-input @input 已 emit 字符串值
  local.value = value
  emit('update:modelValue', value)
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    emit('search', value)
  }, props.debounce)
}

function onClear() {
  if (timer) clearTimeout(timer)
  local.value = ''
  emit('update:modelValue', '')
  emit('search', '')
}

function onEnter() {
  if (timer) clearTimeout(timer)
  emit('search', local.value)
}

onBeforeUnmount(() => {
  if (timer) clearTimeout(timer)
})
</script>

<template>
  <div class="teacher-search-bar">
    <el-input
      :model-value="local"
      :placeholder="placeholder"
      size="large"
      clearable
      class="teacher-search-bar__input"
      @input="onInput"
      @clear="onClear"
      @keyup.enter="onEnter"
    >
      <template #prefix>
        <el-icon class="teacher-search-bar__icon"><Search /></el-icon>
      </template>
    </el-input>
  </div>
</template>

<style scoped lang="scss">
.teacher-search-bar {
  width: 100%;

  &__input {
    width: 100%;

    // EP el-input 的 wrapper 圆角 pill + 主色 focus ring
    :deep(.el-input__wrapper) {
      border-radius: brand.$radius-full;
      padding-inline: brand.$spacing-5;
      box-shadow: 0 0 0 1px brand.$color-border inset;
      background: brand.$color-bg-card;
      transition: box-shadow 0.18s ease;
    }

    :deep(.el-input__wrapper:hover) {
      box-shadow: 0 0 0 1px brand.$brand-primary inset;
    }

    :deep(.el-input__wrapper.is-focus) {
      box-shadow:
        0 0 0 1px brand.$brand-primary inset,
        brand.$ring-focus;
    }

    :deep(.el-input__inner) {
      font-size: brand.$font-size-md;
      color: brand.$color-text-primary;

      &::placeholder {
        color: brand.$color-text-tertiary;
      }
    }
  }

  &__icon {
    color: brand.$color-text-tertiary;
    font-size: brand.$font-size-lg;
  }
}
</style>
