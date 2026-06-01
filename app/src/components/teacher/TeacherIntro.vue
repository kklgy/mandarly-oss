<script setup>
// =============================================================================
// <TeacherIntro> — TeacherDetail § C 自我介绍
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § TeacherDetailView 改造 § C
//
// 关键:
//   1) 默认 4 行(PC) / 5 行(H5) clamp; 超出显示"展开"
//   2) <p dir="auto"> 处理 RTL bidi(教师中英混排, plan A4 已知坑)
//   3) "用户生成内容不翻译" — 直接 {{ }} 渲染原文
// =============================================================================
import { ref, computed, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const props = defineProps({
  intro: {
    type: String,
    default: ''
  }
})

const { t, te } = useI18n()
const t2 = (key, fallback) => (te(key) ? t(key) : fallback)

const expanded = ref(false)
const overflowing = ref(false)
const textRef = ref(null)

const hasIntro = computed(() => Boolean(props.intro && props.intro.trim()))

onMounted(async () => {
  await nextTick()
  checkOverflow()
})

function checkOverflow() {
  const el = textRef.value
  if (!el) return
  // 用 scrollHeight 比 clientHeight 判断是否溢出
  overflowing.value = el.scrollHeight - el.clientHeight > 1
}

function toggle() {
  expanded.value = !expanded.value
}
</script>

<template>
  <section v-if="hasIntro" class="td-intro">
    <h2 class="td-intro__title">{{ t2('teacherDetail.intro.title', '关于我') }}</h2>

    <p
      ref="textRef"
      class="td-intro__text"
      :class="{ 'td-intro__text--clamped': !expanded }"
      dir="auto"
    >{{ intro }}</p>

    <button
      v-if="overflowing || expanded"
      type="button"
      class="td-intro__toggle"
      @click="toggle"
    >
      <span>{{ expanded ? t2('teacherDetail.intro.less', '收起') : t2('teacherDetail.intro.more', '展开') }}</span>
      <el-icon>
        <component :is="expanded ? ArrowUp : ArrowDown" />
      </el-icon>
    </button>
  </section>
</template>

<style scoped lang="scss">

.td-intro {
  background: var(--color-bg-card);
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6 brand.$spacing-5;
  box-shadow: brand.$shadow-sm;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-3;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__text {
    margin: 0;
    font-size: brand.$font-size-base;
    line-height: brand.$line-height-loose;
    color: brand.$color-text-body;
    white-space: pre-wrap;
    word-break: break-word;
    max-width: 720px;

    &--clamped {
      display: -webkit-box;
      -webkit-box-orient: vertical;
      overflow: hidden;
      // H5 5 行
      -webkit-line-clamp: 5;

      @media (min-width: brand.$bp-tablet) {
        -webkit-line-clamp: 4;
      }
    }
  }

  &__toggle {
    align-self: flex-start;
    background: transparent;
    border: none;
    color: brand.$brand-primary-deep;
    font: inherit;
    font-size: brand.$font-size-sm;
    font-weight: 500;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-1;
    padding: brand.$spacing-2 0;

    &:hover {
      color: brand.$brand-primary;
    }
  }
}
</style>
