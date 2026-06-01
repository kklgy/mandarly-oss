<script setup>
// =============================================================================
// <TeacherFilterDrawer> — TeacherListView § C 高级筛选抽屉
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § C 高级筛选抽屉(第 6 轮)
//
// 6 维度筛选:
//   1) 口音        accent[]      多选 chip
//   2) 价格区间    priceBuckets[] 多选 checkbox (lt200 / 200-500 / 500-1000 / gt1000)
//   3) 今日可约    available     boolean (el-switch)
//   4) 最低评分    minRating     0-5 step 0.5 (el-slider)
//   5) 教学方向    expertise[]   多选 chip
//   6) 标签        tags[]        多选 chip (beginner / kids / hasVideo)
//
// 行为:
//   - v-model 一个 filters 对象;打开 drawer 时本地 clone, 抽屉内编辑不影响外部 state
//   - filters 改变 (本地) → emit 'preview' (debounce 300ms 由父调 count API)
//   - "重置" → 清空本地 + emit 'reset'
//   - "应用筛选 (N)" → emit 'apply' 把本地 state 推回父 (父再 commit URL + 刷列表)
//   - "应用筛选 (N)" 按钮文案带 matchCount, 由父级实时调 count API 喂回 prop
//   - matchCount === 0 时按钮 disabled + 切换文案 (DESIGN § C 应用筛选按钮)
//
// EP drawer placement="right" 自动 RTL 镜像。
//
// i18n 调用方负责;chip / checkbox label / drawer title / 按钮文案均为 prop 字符串
//
// 严格约束:
//   1) token only
//   2) RTL: logical properties / EP drawer 自动镜像
// =============================================================================
import { ref, watch, computed, onBeforeUnmount } from 'vue'

const props = defineProps({
  // v-model:visible
  visible: {
    type: Boolean,
    default: false
  },
  // 当前已应用的 filters(打开 drawer 时拷一份做本地编辑)
  filters: {
    type: Object,
    required: true
  },
  // 父级实时调 count 接口喂回的命中数
  matchCount: {
    type: Number,
    default: 0
  },
  // 父级是否在 loading count
  countLoading: {
    type: Boolean,
    default: false
  },
  // 选项 schema:i18n 调用方传入
  // {
  //   accentOptions: [{ value: 'mandarin_cn', label: '普通话(大陆)' }, ...],
  //   priceBucketOptions: [{ value: 'lt200', label: '< HK$200' }, ...],
  //   expertiseOptions: [{ value: 'business', label: '商务中文' }, ...],
  //   tagOptions: [{ value: 'beginner', label: '适合零基础' }, ...]
  // }
  optionSchema: {
    type: Object,
    required: true
  },
  // 文案
  texts: {
    type: Object,
    required: true
  }
})

const emit = defineEmits([
  'update:visible',
  'preview', // (localFilters) 父端 debounce 300 调 count
  'apply', // (localFilters) 父端 commit + 刷列表
  'reset' // 父端清 URL + 刷列表
])

// 本地 filters 拷贝(打开 drawer 时刷新一次)
const local = ref(cloneFilters(props.filters))

watch(
  () => props.visible,
  (v) => {
    if (v) {
      // 打开时拷一份,避免直接改外部 ref
      local.value = cloneFilters(props.filters)
    }
  }
)

// 价格区间 / 标签 / 教学方向 / 口音 是数组,要保证 reactive
function cloneFilters(src) {
  return {
    accent: Array.isArray(src.accent) ? [...src.accent] : [],
    priceBuckets: Array.isArray(src.priceBuckets) ? [...src.priceBuckets] : [],
    available: !!src.available,
    minRating: typeof src.minRating === 'number' ? src.minRating : 0,
    expertise: Array.isArray(src.expertise) ? [...src.expertise] : [],
    tags: Array.isArray(src.tags) ? [...src.tags] : []
  }
}

// debounce 300 emit preview
let timer = null
function schedulePreview() {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    emit('preview', cloneFilters(local.value))
  }, 300)
}

onBeforeUnmount(() => {
  if (timer) clearTimeout(timer)
})

// chip 多选切换
function toggleChip(field, value) {
  const arr = local.value[field]
  const i = arr.indexOf(value)
  if (i >= 0) arr.splice(i, 1)
  else arr.push(value)
  schedulePreview()
}

function isChipActive(field, value) {
  return (local.value[field] || []).includes(value)
}

function onPriceBucketsChange() {
  schedulePreview()
}

function onAvailableChange() {
  schedulePreview()
}

function onRatingChange() {
  schedulePreview()
}

function onReset() {
  local.value = {
    accent: [],
    priceBuckets: [],
    available: false,
    minRating: 0,
    expertise: [],
    tags: []
  }
  if (timer) clearTimeout(timer)
  emit('reset')
}

function onApply() {
  if (timer) clearTimeout(timer)
  emit('apply', cloneFilters(local.value))
}

function onClose() {
  emit('update:visible', false)
}

const drawerSize = computed(() => {
  // PC 520, H5 90vw — 由 EP drawer size 接受 px / %;返字符串自适配
  if (typeof window !== 'undefined' && window.innerWidth < 768) return '90%'
  return '520px'
})

// 应用按钮文案: matchCount 0 disabled
const applyDisabled = computed(() => props.matchCount === 0 && !props.countLoading)

// minRating 显示
const ratingDisplay = computed(() => {
  const r = local.value.minRating
  if (!r) return props.texts.ratingAny || '不限'
  return `≥ ${r.toFixed(1)}`
})
</script>

<template>
  <el-drawer
    :model-value="visible"
    :title="texts.title"
    :size="drawerSize"
    :before-close="onClose"
    direction="rtl"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="teacher-filter-drawer__body">
      <!-- 1. 口音 -->
      <section v-if="optionSchema.priceBucketOptions?.length" class="teacher-filter-drawer__group">
        <h4 class="teacher-filter-drawer__title">{{ texts.accentTitle }}</h4>
        <ul class="teacher-filter-drawer__chips">
          <li v-for="opt in optionSchema.accentOptions" :key="opt.value">
            <button
              type="button"
              class="teacher-filter-drawer__chip"
              :class="{ 'is-active': isChipActive('accent', opt.value) }"
              @click="toggleChip('accent', opt.value)"
            >
              {{ opt.label }}
            </button>
          </li>
        </ul>
      </section>

      <!-- 2. 价格区间 -->
      <section class="teacher-filter-drawer__group">
        <h4 class="teacher-filter-drawer__title">{{ texts.priceBucketsTitle }}</h4>
        <el-checkbox-group
          v-model="local.priceBuckets"
          class="teacher-filter-drawer__checkboxes"
          @change="onPriceBucketsChange"
        >
          <el-checkbox
            v-for="opt in optionSchema.priceBucketOptions"
            :key="opt.value"
            :value="opt.value"
            :label="opt.label"
          >
            {{ opt.label }}
          </el-checkbox>
        </el-checkbox-group>
      </section>

      <!-- 3. 今日可约 -->
      <section class="teacher-filter-drawer__group teacher-filter-drawer__group--row">
        <h4 class="teacher-filter-drawer__title">{{ texts.availableTitle }}</h4>
        <el-switch
          v-model="local.available"
          @change="onAvailableChange"
        />
      </section>

      <!-- 4. 最低评分 -->
      <section class="teacher-filter-drawer__group">
        <div class="teacher-filter-drawer__title-row">
          <h4 class="teacher-filter-drawer__title">{{ texts.minRatingTitle }}</h4>
          <span class="teacher-filter-drawer__rating-value">{{ ratingDisplay }}</span>
        </div>
        <el-slider
          v-model="local.minRating"
          :min="0"
          :max="5"
          :step="0.5"
          :show-tooltip="false"
          @change="onRatingChange"
        />
      </section>

      <!-- 5. 教学方向 -->
      <section class="teacher-filter-drawer__group">
        <h4 class="teacher-filter-drawer__title">{{ texts.expertiseTitle }}</h4>
        <ul class="teacher-filter-drawer__chips">
          <li v-for="opt in optionSchema.expertiseOptions" :key="opt.value">
            <button
              type="button"
              class="teacher-filter-drawer__chip"
              :class="{ 'is-active': isChipActive('expertise', opt.value) }"
              @click="toggleChip('expertise', opt.value)"
            >
              {{ opt.label }}
            </button>
          </li>
        </ul>
      </section>

      <!-- 6. 教师标签 -->
      <section class="teacher-filter-drawer__group">
        <h4 class="teacher-filter-drawer__title">{{ texts.tagsTitle }}</h4>
        <ul class="teacher-filter-drawer__chips">
          <li v-for="opt in optionSchema.tagOptions" :key="opt.value">
            <button
              type="button"
              class="teacher-filter-drawer__chip"
              :class="{ 'is-active': isChipActive('tags', opt.value) }"
              @click="toggleChip('tags', opt.value)"
            >
              {{ opt.label }}
            </button>
          </li>
        </ul>
      </section>
    </div>

    <template #footer>
      <div class="teacher-filter-drawer__footer">
        <el-button
          class="teacher-filter-drawer__reset"
          @click="onReset"
        >
          {{ texts.reset }}
        </el-button>
        <el-button
          type="primary"
          class="teacher-filter-drawer__apply"
          :disabled="applyDisabled"
          :loading="countLoading"
          @click="onApply"
        >
          <template v-if="matchCount === 0 && !countLoading">
            {{ texts.applyEmpty }}
          </template>
          <template v-else>
            {{ texts.applyTemplate.replace('{count}', matchCount) }}
          </template>
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.teacher-filter-drawer {
  &__body {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-6;
    padding-block-end: brand.$spacing-4;
  }

  &__group {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;

    &--row {
      flex-direction: row;
      align-items: center;
      justify-content: space-between;
      gap: brand.$spacing-4;
    }
  }

  &__title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: brand.$spacing-3;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__rating-value {
    font-size: brand.$font-size-base;
    color: brand.$brand-primary-deep;
    font-weight: 600;
  }

  &__chips {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
  }

  &__chip {
    padding: brand.$spacing-2 brand.$spacing-4;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-pill;
    background: brand.$color-bg-card;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    cursor: pointer;
    transition:
      border-color 0.18s ease,
      background 0.18s ease,
      color 0.18s ease;

    &:hover {
      border-color: brand.$brand-primary;
      color: brand.$brand-primary-deep;
    }

    &.is-active {
      background: brand.$brand-primary;
      border-color: brand.$brand-primary;
      color: brand.$color-text-inverse;
      font-weight: 600;
    }
  }

  &__checkboxes {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: brand.$spacing-3;
    padding-block: brand.$spacing-3;
  }

  &__reset {
    flex: 0 0 auto;
  }

  &__apply {
    flex: 1 1 auto;
    height: 44px;
    font-size: brand.$font-size-md;
    font-weight: 600;
  }
}
</style>
