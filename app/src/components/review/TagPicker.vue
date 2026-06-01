<script setup>
/**
 * TagPicker — 评价标签多选 + 自定义(Wave 5 第 11 轮)
 *
 * Props:
 *  - presetTags  : String[] 预设 tag code 列表(从 GET /edu/review/tags 加载;父组件传)
 *  - tagsValue   : String[] v-model:tags 已选预设 tag(白名单)
 *  - customValue : String[] v-model:custom 已选自定义 tag(0-3 条)
 *  - maxTags     : Number 预设 tag 上限(默认 5)
 *  - maxCustom   : Number 自定义 tag 上限(默认 3)
 *  - customMaxLen: Number 单条自定义 tag 字符上限(默认 8)
 *  - disabled    : Boolean 整体禁用(评价过期场景)
 *
 * Events:
 *  - update:tags(arr)
 *  - update:custom(arr)
 */

import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  presetTags: { type: Array, default: () => [] },
  tagsValue: { type: Array, default: () => [] },
  customValue: { type: Array, default: () => [] },
  maxTags: { type: Number, default: 5 },
  maxCustom: { type: Number, default: 3 },
  customMaxLen: { type: Number, default: 8 },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:tags', 'update:custom'])

const { t, te } = useI18n()

const showCustomInput = ref(false)
const customDraft = ref('')
const inputRef = ref(null)

const customTagsAtLimit = computed(() => props.customValue.length >= props.maxCustom)
const presetTagsAtLimit = computed(() => props.tagsValue.length >= props.maxTags)
const draftLen = computed(() => Array.from(customDraft.value.trim()).length)
const draftValid = computed(() => draftLen.value > 0 && draftLen.value <= props.customMaxLen)

function tagLabel(code) {
  // 优先 i18n review.tag.{code};若 i18n 未配,直接展示 code(防 i18n 滞后)
  const key = `review.tag.${code}`
  return te(key) ? t(key) : code
}

function isPresetSelected(code) {
  return props.tagsValue.includes(code)
}

function togglePreset(code) {
  if (props.disabled) return
  const next = [...props.tagsValue]
  const idx = next.indexOf(code)
  if (idx >= 0) {
    next.splice(idx, 1)
  } else {
    if (next.length >= props.maxTags) return
    next.push(code)
  }
  emit('update:tags', next)
}

function openCustomInput() {
  if (props.disabled || customTagsAtLimit.value) return
  showCustomInput.value = true
  customDraft.value = ''
  // 等 DOM 更新后聚焦
  requestAnimationFrame(() => {
    if (inputRef.value) inputRef.value.focus()
  })
}

function cancelCustomInput() {
  showCustomInput.value = false
  customDraft.value = ''
}

function commitCustom() {
  if (props.disabled) return
  const trimmed = customDraft.value.trim()
  if (!trimmed) return
  // 防长度超限(用户在 input 内 paste 长文本)
  if (Array.from(trimmed).length > props.customMaxLen) return
  if (props.customValue.includes(trimmed)) {
    cancelCustomInput()
    return
  }
  if (customTagsAtLimit.value) return
  emit('update:custom', [...props.customValue, trimmed])
  cancelCustomInput()
}

function removeCustom(tag) {
  if (props.disabled) return
  emit('update:custom', props.customValue.filter((t) => t !== tag))
}
</script>

<template>
  <div class="tag-picker" :class="{ 'is-disabled': disabled }">
    <!-- 预设 tag chips -->
    <div class="tag-picker__chips">
      <button
        v-for="code in presetTags"
        :key="code"
        type="button"
        class="tag-picker__chip"
        :class="{
          'is-on': isPresetSelected(code),
          'is-disabled': !isPresetSelected(code) && (disabled || presetTagsAtLimit)
        }"
        :disabled="disabled || (!isPresetSelected(code) && presetTagsAtLimit)"
        :aria-pressed="isPresetSelected(code)"
        @click="togglePreset(code)"
      >
        {{ tagLabel(code) }}
      </button>

      <!-- 自定义 tag chips(已添加,带删除) -->
      <span
        v-for="tag in customValue"
        :key="'custom-' + tag"
        class="tag-picker__chip is-on is-custom"
      >
        {{ tag }}
        <button
          type="button"
          class="tag-picker__chip-close"
          :disabled="disabled"
          :aria-label="t('review.tags.remove', { tag })"
          @click="removeCustom(tag)"
        >×</button>
      </span>

      <!-- + 添加按钮 / input -->
      <button
        v-if="!showCustomInput"
        type="button"
        class="tag-picker__add"
        :disabled="disabled || customTagsAtLimit"
        @click="openCustomInput"
      >+ {{ t('review.tags.addCustom') }}</button>

      <span v-else class="tag-picker__input-wrap">
        <input
          ref="inputRef"
          v-model="customDraft"
          class="tag-picker__input"
          type="text"
          :maxlength="customMaxLen * 2"
          :placeholder="t('review.tags.customPlaceholder', { n: customMaxLen })"
          :disabled="disabled"
          @keydown.enter.prevent="commitCustom"
          @keydown.esc.prevent="cancelCustomInput"
          @blur="commitCustom"
        />
        <span class="tag-picker__counter" :class="{ 'is-over': draftLen > customMaxLen }">
          {{ draftLen }}/{{ customMaxLen }}
        </span>
      </span>
    </div>

    <p class="tag-picker__hint">
      <template v-if="customTagsAtLimit">
        {{ t('review.tags.customLimitReached', { n: maxCustom }) }}
      </template>
      <template v-else>
        {{ t('review.tags.customLimit', { n: maxCustom, len: customMaxLen }) }}
      </template>
    </p>
  </div>
</template>

<style lang="scss" scoped>
.tag-picker {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-3;
}

.tag-picker__chips {
  display: flex;
  flex-wrap: wrap;
  gap: brand.$spacing-2;
}

.tag-picker__chip {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-1;
  padding: brand.$spacing-2 brand.$spacing-4;
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-pill;
  font: inherit;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-primary;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  user-select: none;

  &:hover:not(:disabled):not(.is-disabled):not(.is-on) {
    border-color: brand.$brand-primary;
    color: brand.$brand-primary-deep;
  }

  &.is-on {
    background: brand.$brand-primary;
    border-color: brand.$brand-primary;
    color: brand.$color-text-inverse;
    font-weight: 500;
  }

  &.is-custom {
    background: brand.$brand-primary-soft;
    border-color: brand.$brand-primary;
    color: brand.$brand-primary-deep;
  }

  &:disabled,
  &.is-disabled {
    cursor: not-allowed;
    opacity: 0.55;
  }
}

.tag-picker__chip-close {
  background: transparent;
  border: none;
  color: inherit;
  font-size: brand.$font-size-base;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  margin-inline-start: brand.$spacing-1;

  &:hover {
    color: brand.$color-error;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.4;
  }
}

.tag-picker__add {
  padding: brand.$spacing-2 brand.$spacing-4;
  background: transparent;
  border: 1px dashed brand.$color-border;
  border-radius: brand.$radius-pill;
  font: inherit;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;

  &:hover:not(:disabled) {
    border-color: brand.$brand-primary;
    color: brand.$brand-primary-deep;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }
}

.tag-picker__input-wrap {
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;
  padding: 0 brand.$spacing-3;
  border: 1px solid brand.$brand-primary;
  border-radius: brand.$radius-pill;
  background: brand.$color-bg-card;
  box-shadow: brand.$ring-focus;
}

.tag-picker__input {
  border: none;
  outline: none;
  background: transparent;
  padding: brand.$spacing-2 0;
  font: inherit;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-primary;
  inline-size: 8em;

  &::placeholder {
    color: brand.$color-text-tertiary;
  }
}

.tag-picker__counter {
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
  font-variant-numeric: tabular-nums;

  &.is-over {
    color: brand.$color-error;
  }
}

.tag-picker__hint {
  margin: 0;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}
</style>
