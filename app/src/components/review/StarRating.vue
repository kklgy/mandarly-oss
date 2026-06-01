<script setup>
/**
 * StarRating — 自封装五星评分(Wave 5 第 11 轮 plan A5)
 *
 * 为什么不用 <el-rate>:EP rate 在 RTL 模式下半星 hover 用全局 clientX vs window.innerWidth
 * 计算,RTL 翻转后偏移错位(plan A5 已记 EP RTL 半星 bug)。本组件用按钮自身相对坐标
 * (offsetX vs button.clientWidth)计算,RTL 模式 LTR 模式逻辑一致,容器 flex 由 dir 自然反向。
 *
 * Props:
 *  - modelValue : Number 0-5 当前评分(支持 0.5 半星)
 *  - size       : 'small' | 'default' | 'large'(40 / 48 / 56px,H5 Step1 用 large)
 *  - readonly   : Boolean 只读(展示)
 *  - allowHalf  : Boolean 是否支持半星(默认 false,review 主流程整星即可)
 *  - texts      : String[] 长度 5,索引 0=1星文案 ... 4=5星文案;为空不显示文案
 *
 * Events:
 *  - update:modelValue(n)
 *  - change(n)         user 主动选完(用于触发 bounce 动效)
 */

import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: { type: Number, default: 0 },
  size: { type: String, default: 'default' },
  readonly: { type: Boolean, default: false },
  allowHalf: { type: Boolean, default: false },
  texts: { type: Array, default: () => [] },
  ariaLabel: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'change'])

const hoverValue = ref(0)
const justPicked = ref(false)  // 选完触发整组 bounce 动效

const displayValue = computed(() => hoverValue.value || props.modelValue || 0)

const ratingText = computed(() => {
  const v = Math.ceil(displayValue.value)
  if (v < 1 || v > 5) return ''
  return props.texts[v - 1] || ''
})

function getStarFill(i) {
  // i: 1..5  返回 'full' | 'half' | 'empty'
  const v = displayValue.value
  if (v >= i) return 'full'
  if (props.allowHalf && v >= i - 0.5) return 'half'
  return 'empty'
}

/**
 * hover 半星检测(关键防御:用按钮自身 offsetX,不读全局 clientX)
 * RTL 容器 flex-direction 自然反向,但 button.offsetX 是元素内本地坐标,逻辑不变
 */
function onHover(e, i) {
  if (props.readonly) return
  if (!props.allowHalf) {
    hoverValue.value = i
    return
  }
  const rect = e.currentTarget.getBoundingClientRect()
  // 注:RTL 模式 getBoundingClientRect.left 仍是物理左边;offsetX 是事件相对元素左边
  // 我们在 RTL 下要让"星的左半"代表"该星的整星",为此用 dir 判断翻转
  const isRTL = e.currentTarget.closest('[dir="rtl"]') !== null
  const offsetX = e.clientX - rect.left
  const ratio = offsetX / rect.width
  // LTR: ratio < 0.5 → i-0.5  / RTL: ratio > 0.5 → i-0.5
  const isHalf = isRTL ? ratio > 0.5 : ratio < 0.5
  hoverValue.value = isHalf ? i - 0.5 : i
}

function onLeave() {
  hoverValue.value = 0
}

function onClick(e, i) {
  if (props.readonly) return
  let val = i
  if (props.allowHalf) {
    onHover(e, i)
    val = hoverValue.value || i
  }
  emit('update:modelValue', val)
  emit('change', val)
  // 触发 bounce
  justPicked.value = false
  // 微任务里再 set true,确保 transition 重启
  requestAnimationFrame(() => {
    justPicked.value = true
    setTimeout(() => { justPicked.value = false }, 320)
  })
}

function onKeydown(e, i) {
  if (props.readonly) return
  if (e.key === 'ArrowLeft') {
    e.preventDefault()
    const v = Math.max(0, Math.floor((props.modelValue || 0) - 1))
    emit('update:modelValue', v)
    emit('change', v)
  } else if (e.key === 'ArrowRight') {
    e.preventDefault()
    const v = Math.min(5, Math.floor((props.modelValue || 0) + 1) || i)
    emit('update:modelValue', v)
    emit('change', v)
  }
}
</script>

<template>
  <div
    class="star-rating"
    :class="['star-rating--' + size, { 'is-readonly': readonly, 'is-bouncing': justPicked }]"
    role="slider"
    :aria-label="ariaLabel"
    :aria-valuemin="0"
    :aria-valuemax="5"
    :aria-valuenow="modelValue"
    :aria-readonly="readonly"
  >
    <div class="star-rating__row" @mouseleave="onLeave">
      <button
        v-for="i in 5"
        :key="i"
        type="button"
        class="star-rating__star"
        :class="['is-' + getStarFill(i)]"
        :tabindex="readonly ? -1 : 0"
        :aria-label="`${i}`"
        @click="onClick($event, i)"
        @mousemove="onHover($event, i)"
        @keydown="onKeydown($event, i)"
      >
        <span class="star-rating__icon" aria-hidden="true">
          <span class="star-rating__base">★</span>
          <span class="star-rating__fill" :style="{ width: getStarFill(i) === 'full' ? '100%' : getStarFill(i) === 'half' ? '50%' : '0%' }">★</span>
        </span>
      </button>
    </div>
    <p v-if="ratingText" class="star-rating__text">{{ ratingText }}</p>
  </div>
</template>

<style lang="scss" scoped>
.star-rating {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-2;
}

.star-rating__row {
  display: inline-flex;
  gap: brand.$spacing-2;
  transition: transform 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);

  .is-bouncing & {
    animation: star-bounce 0.32s ease-out;
  }
}

@keyframes star-bounce {
  0% { transform: scale(1); }
  40% { transform: scale(1.12); }
  100% { transform: scale(1); }
}

.star-rating__star {
  background: transparent;
  border: none;
  padding: brand.$spacing-1;
  cursor: pointer;
  line-height: 1;
  transition: transform 0.18s ease;
  border-radius: brand.$radius-sm;

  &:hover:not(:disabled) {
    transform: scale(1.18);
  }

  &:focus-visible {
    outline: none;
    box-shadow: brand.$ring-focus;
  }

  .is-readonly & {
    cursor: default;

    &:hover {
      transform: none;
    }
  }
}

.star-rating__icon {
  position: relative;
  display: inline-block;
  font-size: 32px;
  line-height: 1;
}

.star-rating--small .star-rating__icon { font-size: 24px; }
.star-rating--default .star-rating__icon { font-size: 32px; }
.star-rating--large .star-rating__icon { font-size: 48px; }

.star-rating__base {
  color: brand.$color-border;
  display: inline-block;
}

.star-rating__fill {
  position: absolute;
  inset-block-start: 0;
  inset-inline-start: 0;
  overflow: hidden;
  color: brand.$brand-primary;
  white-space: nowrap;
  // RTL 容器下 inset-inline-start 自动从右侧起算,fill 与 base 对齐;无需额外处理
  transition: width 0.12s ease;
}

.star-rating__text {
  margin: 0;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  font-weight: 500;
  min-height: 1.4em;
}

.star-rating--large .star-rating__text {
  font-size: brand.$font-size-md;
  color: brand.$color-text-primary;
  font-weight: 600;
}
</style>
