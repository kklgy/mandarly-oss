<script setup>
// =============================================================================
// TeacherListView — 用户端 /teachers 老师列表(M5 Wave 4 Subagent E)
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § TeacherListView 改造(第 6 轮 P1-1)
//   - docs/frontend/ui-style-guide-v1.md(token / RTL / i18n / SEO)
//   - app/src/components/SHARED-API.md §1 TeacherCard variant=list / §4 EmptyState
//
// 整体布局:
//   AppHeader (来自 AppLayout)
//     ├─ TeacherSearchBar(顶部 sticky + pill + 主色 ring)
//     ├─ 操作栏:筛选按钮 / 排序下拉 / 共 X · 已加载 Y
//     ├─ TeacherCard 网格(PC 4 / Tablet 3 / Mobile 1, variant="list")
//     ├─ EmptyState variant="no-result"(0 命中 + 双按钮 清除筛选/浏览全部)
//     └─ "加载更多" 按钮(点击式;底部进度)
//
// URL ?keyword= ?accent= ?priceBuckets= ?available= ?minRating= ?expertise= ?tags= ?sort= ?pageNo=
// 同步 — 刷新还原全部 filter,router.replace 不 push
// =============================================================================
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter, useRoute } from 'vue-router'
import { useHead } from '@vueuse/head'
import { Filter } from '@element-plus/icons-vue'
import { listTeachers, countTeachers } from '@/api/teacher'
import { getFreeTrialStatus } from '@/api/package'
import { buildHead, jsonLd, SITE_ORIGIN, SITE_NAME } from '@/utils/seo'
import { getFreeTrialCtaTarget, shouldShowFreeTrialCta } from '@/utils/freeTrialCta'
import { useUserStore } from '@/stores/user'
import PageIntro from '@/components/PageIntro.vue'
import TeacherSearchBar from '@/components/teacher/TeacherSearchBar.vue'
import TeacherFilterDrawer from '@/components/teacher/TeacherFilterDrawer.vue'
import TeacherSortSelect from '@/components/teacher/TeacherSortSelect.vue'

const { t, te, locale } = useI18n()
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const t2 = (key, fallback) => (te(key) ? t(key) : fallback)
const templateText = (key, fallback, params) => (te(key) ? t(key, params) : fallback)

// =============================================================================
// 1. 选项 schema(i18n 调用方组装,从 i18n + 设计稿写死的 enum 列表)
// =============================================================================
const ACCENT_VALUES = ['mandarin_cn', 'mandarin_tw', 'cantonese']
const PRICE_BUCKET_VALUES = ['lt200', '200-500', '500-1000', 'gt1000']
const EXPERTISE_VALUES = ['business', 'daily', 'kids', 'HSK', 'speaking']
const TAG_VALUES = ['beginner', 'kids', 'hasVideo']
const SORT_VALUES = ['recommend', 'rating_desc', 'price_asc', 'price_desc', 'review_count_desc']
const PRICE_SORT_VALUES = ['price_asc', 'price_desc']
const canViewPrices = computed(() => userStore.isLoggedIn)

const accentOptions = computed(() =>
  ACCENT_VALUES.map((v) => ({
    value: v,
    label: t2(`teachers.filter.accent.${v}`, t2(`teacher.accent.${v}`, v))
  }))
)

const priceBucketOptions = computed(() => canViewPrices.value
  ? [
      { value: 'lt200', label: t2('teachers.filter.priceBuckets.lt200', '< HK$200') },
      { value: '200-500', label: t2('teachers.filter.priceBuckets.200_500', 'HK$200 - 500') },
      { value: '500-1000', label: t2('teachers.filter.priceBuckets.500_1000', 'HK$500 - 1000') },
      { value: 'gt1000', label: t2('teachers.filter.priceBuckets.gt1000', '> HK$1000') }
    ]
  : []
)

const expertiseOptions = computed(() =>
  EXPERTISE_VALUES.map((v) => ({
    value: v,
    label: t2(`teacher.expertise.${v}`, v)
  }))
)

const tagOptions = computed(() =>
  TAG_VALUES.map((v) => ({
    value: v,
    label: t2(`teachers.filter.tags.${v}`, v)
  }))
)

const sortOptions = computed(() => [
  { value: 'recommend', label: t2('teachers.sort.recommend', '推荐') },
  { value: 'rating_desc', label: t2('teachers.sort.rating', '评分高 → 低') },
  ...(canViewPrices.value
    ? [
        { value: 'price_asc', label: t2('teachers.sort.priceAsc', '价格低 → 高') },
        { value: 'price_desc', label: t2('teachers.sort.priceDesc', '价格高 → 低') }
      ]
    : []),
  { value: 'review_count_desc', label: t2('teachers.sort.reviewCount', '评价数多 → 少') }
])

const drawerTexts = computed(() => ({
  title: t2('teachers.filter.title', '筛选'),
  accentTitle: t2('teachers.filter.accent.title', '口音'),
  priceBucketsTitle: t2('teachers.filter.priceBuckets.title', '价格区间'),
  availableTitle: t2('teachers.filter.available', '今日可约'),
  minRatingTitle: t2('teachers.filter.minRating', '最低评分'),
  ratingAny: t2('teachers.filter.minRating.any', '不限'),
  expertiseTitle: t2('teachers.filter.expertise.title', '教学方向'),
  tagsTitle: t2('teachers.filter.tags.title', '标签'),
  reset: t2('teachers.filter.reset', '重置'),
  applyTemplate: templateText('teachers.filter.apply', '应用筛选({count} 位)', { count: '{count}' }),
  applyEmpty: t2('teachers.filter.applyEmpty', '无匹配老师')
}))

// =============================================================================
// 2. State
// =============================================================================
const PAGE_SIZE = 24

// 已应用的 filter(URL 同源)
const appliedFilters = ref(emptyFilters())
const keyword = ref('')
const sort = ref('recommend')

// 抽屉状态
const drawerVisible = ref(false)

// 抽屉内"实时命中数"(由抽屉 @preview 触发 count API)
const previewMatchCount = ref(0)
const previewLoading = ref(false)

// 列表数据
const teachers = ref([])
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const errorMsg = ref('')
const pageNo = ref(1)
const freeTrialClaimed = ref(false)

const showFreeTrialCta = computed(() =>
  shouldShowFreeTrialCta({
    isLoggedIn: userStore.isLoggedIn,
    claimed: freeTrialClaimed.value
  })
)
const teacherIntroDescription = computed(() =>
  showFreeTrialCta.value
    ? t2('teachers.intro.description', '不确定怎么选? 先免费体验 1 节,或 30 秒测水平。')
    : t2('teachers.intro.descriptionNoTrial', '不确定怎么选? 30 秒测水平,我们推荐合适老师。')
)
const teacherIntroPrimaryLabel = computed(() =>
  showFreeTrialCta.value
    ? t2('teachers.intro.freeTrial', '免费体验课')
    : t2('teachers.intro.levelCheck', '30 秒测水平')
)
const teacherIntroPrimaryTo = computed(() =>
  showFreeTrialCta.value ? getFreeTrialCtaTarget({ isLoggedIn: userStore.isLoggedIn }) : '/level-check'
)
const teacherIntroSecondaryLabel = computed(() =>
  showFreeTrialCta.value ? t2('teachers.intro.levelCheck', '30 秒测水平') : ''
)
const teacherIntroSecondaryTo = computed(() =>
  showFreeTrialCta.value ? '/level-check' : ''
)

function emptyFilters() {
  return {
    accent: [],
    priceBuckets: [],
    available: false,
    minRating: 0,
    expertise: [],
    tags: []
  }
}

// =============================================================================
// 3. URL ↔ state 同步
// =============================================================================
function readFiltersFromQuery() {
  const q = route.query
  keyword.value = typeof q.keyword === 'string' ? q.keyword : ''
  const querySort = SORT_VALUES.includes(q.sort) ? q.sort : 'recommend'
  sort.value = !canViewPrices.value && PRICE_SORT_VALUES.includes(querySort) ? 'recommend' : querySort
  appliedFilters.value = {
    accent: parseCsv(q.accent, ACCENT_VALUES),
    priceBuckets: canViewPrices.value ? parseCsv(q.priceBuckets, PRICE_BUCKET_VALUES) : [],
    available: q.available === 'true' || q.available === '1',
    minRating: parseRating(q.minRating),
    expertise: parseCsv(q.expertise, EXPERTISE_VALUES),
    tags: parseCsv(q.tags, TAG_VALUES)
  }
  pageNo.value = parseInt(q.pageNo, 10) || 1
}

function parseCsv(v, whitelist) {
  if (typeof v !== 'string' || !v) return []
  return v
    .split(',')
    .map((s) => s.trim())
    .filter((s) => whitelist.includes(s))
}

function parseRating(v) {
  if (v === undefined || v === null || v === '') return 0
  const n = Number(v)
  if (Number.isNaN(n)) return 0
  if (n < 0) return 0
  if (n > 5) return 5
  return n
}

function buildQuery() {
  const f = appliedFilters.value
  const q = {}
  if (keyword.value) q.keyword = keyword.value
  if (f.accent.length) q.accent = f.accent.join(',')
  if (canViewPrices.value && f.priceBuckets.length) q.priceBuckets = f.priceBuckets.join(',')
  if (f.available) q.available = 'true'
  if (f.minRating > 0) q.minRating = String(f.minRating)
  if (f.expertise.length) q.expertise = f.expertise.join(',')
  if (f.tags.length) q.tags = f.tags.join(',')
  if (sort.value && sort.value !== 'recommend') q.sort = sort.value
  return q
}

function syncQuery() {
  const next = buildQuery()
  // 浅比较避免重复 replace
  const cur = route.query
  const keys = new Set([...Object.keys(cur), ...Object.keys(next)])
  let dirty = false
  for (const k of keys) {
    if (cur[k] !== next[k]) {
      dirty = true
      break
    }
  }
  if (dirty) {
    router.replace({ path: route.path, query: next })
  }
}

// =============================================================================
// 4. API params 构造
// =============================================================================
function buildApiParams(extra = {}) {
  const f = appliedFilters.value
  const params = {}
  if (keyword.value) params.keyword = keyword.value
  if (f.accent.length) params.accent = f.accent.join(',')
  if (canViewPrices.value && f.priceBuckets.length) params.priceBuckets = f.priceBuckets.join(',')
  if (f.available) params.available = true
  if (f.minRating > 0) params.minRating = f.minRating
  if (f.expertise.length) params.expertise = f.expertise.join(',')
  if (f.tags.length) params.tags = f.tags.join(',')
  if (sort.value) params.sort = sort.value
  return { ...params, ...extra }
}

// preview filter 用(抽屉本地态 + keyword + sort)
function buildPreviewParams(localFilters) {
  const params = {}
  if (keyword.value) params.keyword = keyword.value
  if (localFilters.accent.length) params.accent = localFilters.accent.join(',')
  if (canViewPrices.value && localFilters.priceBuckets.length)
    params.priceBuckets = localFilters.priceBuckets.join(',')
  if (localFilters.available) params.available = true
  if (localFilters.minRating > 0) params.minRating = localFilters.minRating
  if (localFilters.expertise.length) params.expertise = localFilters.expertise.join(',')
  if (localFilters.tags.length) params.tags = localFilters.tags.join(',')
  return params
}

// =============================================================================
// 5. 数据加载
// =============================================================================
async function loadFirstPage() {
  loading.value = true
  errorMsg.value = ''
  pageNo.value = 1
  try {
    const params = buildApiParams({ pageNo: 1, pageSize: PAGE_SIZE })
    const resp = await listTeachers(params)
    const { list, count } = normalizeListResp(resp)
    teachers.value = list
    total.value = count
  } catch (e) {
    errorMsg.value = t2('teachers.loadFailed', '老师列表加载失败,请重试。')
    teachers.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadFreeTrialStatus() {
  if (!userStore.isLoggedIn) {
    freeTrialClaimed.value = false
    return
  }
  try {
    const data = await getFreeTrialStatus()
    freeTrialClaimed.value = !!data?.claimed
  } catch {
    freeTrialClaimed.value = false
  }
}

async function loadMore() {
  if (loadingMore.value) return
  if (teachers.value.length >= total.value) return
  loadingMore.value = true
  try {
    const next = pageNo.value + 1
    const params = buildApiParams({ pageNo: next, pageSize: PAGE_SIZE })
    const resp = await listTeachers(params)
    const { list, count } = normalizeListResp(resp)
    if (list.length) {
      teachers.value = [...teachers.value, ...list]
    }
    total.value = count > 0 ? count : total.value
    pageNo.value = next
  } catch (e) {
    // 静默,保留已加载的项
  } finally {
    loadingMore.value = false
  }
}

// 兼容 list 接口可能返:
//   1) 数组直接              → list = resp, total 用 list.length(降级)
//   2) PageResult { list, total }
//   3) { records, total }    (MyBatis-Plus 风格)
function normalizeListResp(resp) {
  if (!resp) return { list: [], count: 0 }
  if (Array.isArray(resp)) return { list: resp, count: resp.length }
  if (Array.isArray(resp.list)) return { list: resp.list, count: Number(resp.total) || resp.list.length }
  if (Array.isArray(resp.records)) return { list: resp.records, count: Number(resp.total) || resp.records.length }
  return { list: [], count: 0 }
}

// =============================================================================
// 6. 抽屉 / 排序 / 搜索 交互
// =============================================================================
async function onPreviewFilters(localFilters) {
  previewLoading.value = true
  try {
    const params = buildPreviewParams(localFilters)
    const resp = await countTeachers(params)
    previewMatchCount.value = extractCount(resp, localFilters)
  } catch (e) {
    // 接口不可用时降级:走 list 拿 length(避免阻塞)
    try {
      const params = buildPreviewParams(localFilters)
      const resp = await listTeachers({ ...params, pageNo: 1, pageSize: 1 })
      previewMatchCount.value = normalizeListResp(resp).count
    } catch (ee) {
      previewMatchCount.value = 0
    }
  } finally {
    previewLoading.value = false
  }
}

function extractCount(resp, localFilters) {
  if (!resp) return 0
  if (typeof resp.total === 'number') return resp.total
  if (typeof resp.count === 'number') return resp.count
  if (typeof resp === 'number') return resp
  return 0
}

function onApplyFilters(localFilters) {
  appliedFilters.value = { ...localFilters }
  drawerVisible.value = false
  syncQuery()
  loadFirstPage()
}

function onResetFilters() {
  appliedFilters.value = emptyFilters()
  // 抽屉自身 reset 后保持打开,让用户继续编辑;但本场景按 plan 关闭后立即刷
  drawerVisible.value = false
  syncQuery()
  loadFirstPage()
}

function onSortChange(value) {
  sort.value = value
  syncQuery()
  loadFirstPage()
}

function onSearch(value) {
  keyword.value = value
  syncQuery()
  loadFirstPage()
}

function openDrawer() {
  drawerVisible.value = true
  // 进入抽屉时先以"已应用"态预算一次 count
  previewMatchCount.value = total.value
}

// "EmptyState" 双按钮
function clearAllFilters() {
  keyword.value = ''
  appliedFilters.value = emptyFilters()
  // sort 保留
  syncQuery()
  loadFirstPage()
}

function goLevelCheck() {
  router.push('/level-check')
}

// =============================================================================
// 7. 衍生(进度 / chip 装饰)
// =============================================================================

// D25: 头部 stats 聚合(不调新 API,从已加载 list 中计算)
const listStats = computed(() => {
  const list = teachers.value
  if (!list.length) return null
  // 语言种类:accent 可能是 string 或 string[]
  const langSet = new Set()
  list.forEach((t) => {
    const accentArr = Array.isArray(t.accent)
      ? t.accent
      : t.accent ? [t.accent] : []
    accentArr.forEach((a) => langSet.add(a))
  })
  // 平均评分
  const rated = list.filter((t) => {
    const r = Number(t.avgRating)
    return Number.isFinite(r) && r > 0
  })
  const avgRating = rated.length
    ? (rated.reduce((s, t) => s + Number(t.avgRating), 0) / rated.length).toFixed(1)
    : null
  return {
    teacherCount: total.value || list.length,
    langCount: langSet.size,
    avgRating
  }
})

const remaining = computed(() => Math.max(total.value - teachers.value.length, 0))
const allLoaded = computed(() => total.value > 0 && teachers.value.length >= total.value)

// 已激活的 filter 数(用于"筛选"按钮的角标)
const activeFilterCount = computed(() => {
  const f = appliedFilters.value
  let n = 0
  if (f.accent.length) n++
  if (canViewPrices.value && f.priceBuckets.length) n++
  if (f.available) n++
  if (f.minRating > 0) n++
  if (f.expertise.length) n++
  if (f.tags.length) n++
  return n
})

const summaryText = computed(() => {
  return templateText('teachers.list.summary', '共 {total} · 已加载 {loaded}', {
    total: total.value,
    loaded: teachers.value.length
  })
    .replace('{total}', total.value)
    .replace('{loaded}', teachers.value.length)
})

const loadMoreText = computed(() => {
  if (allLoaded.value) {
    return t2('teachers.list.allLoaded', '已全部加载')
  }
  return templateText('teachers.list.loadMore', '加载更多({remaining} 位)', {
    remaining: remaining.value
  }).replace('{remaining}', remaining.value)
})

// =============================================================================
// 8. TeacherCard 数据装饰(SHARED-API.md §1 调用方负责)
// =============================================================================
const decoratedTeachers = computed(() => {
  const periodText = t2('teacherCard.priceUnit', '/ 25 分钟')
  return teachers.value.map((raw) => decorateOne(raw, periodText))
})

function decorateOne(raw, periodText) {
  // accent 后端可能返 String(单值)或 List<String>;两种都兼容
  const accentArr = Array.isArray(raw.accent)
    ? raw.accent
    : raw.accent
      ? [raw.accent]
      : []
  const expertiseArr = Array.isArray(raw.expertise) ? raw.expertise : []
  const badgesArr = Array.isArray(raw.badges) ? raw.badges : []
  const avgRating = Number(raw.avgRating)
  const finishedOrderCount = Number(raw.finishedOrderCount || 0)
  const reviewCount = Number(raw.reviewCount || 0)
  return {
    ...raw,
    avatar: raw.avatar || raw.avatarUrl || '',
    accent: accentArr,
    expertise: expertiseArr,
    badges: badgesArr,
    hasIntroVideo: raw.hasIntroVideo ?? Boolean(raw.introVideoUrl),
    teacherTimezone: raw.teacherTimezone || raw.timezone || '',
    displayAccent: accentArr
      .map((code) => t2(`teacher.accent.${code}`, code))
      .filter(Boolean)
      .join(' · '),
    displayExpertise: expertiseArr.map((code) => t2(`teacher.expertise.${code}`, code)),
    period: periodText,
    avgRating: Number.isFinite(avgRating) ? avgRating : undefined,
    reviewCount,
    finishedOrderCount
  }
}

// =============================================================================
// 9. SEO head(沿用现状)
// =============================================================================
const headPayload = computed(() => {
  const base = buildHead({
    title: t2('seo.teachers.title', t2('teachers.title', 'Mandarly Tutors')),
    description: t2('seo.teachers.description', ''),
    keywords: t2('seo.teachers.keywords', ''),
    path: '/teachers',
    locale: locale.value
  })
  const items = teachers.value.slice(0, 20).map((tt, i) => ({
    '@type': 'ListItem',
    position: i + 1,
    url: `${SITE_ORIGIN}/teacher/${tt.userId}`,
    item: {
      '@type': 'Person',
      '@id': `${SITE_ORIGIN}/teacher/${tt.userId}`,
      name: tt.nickname || `Tutor #${tt.userId}`,
      jobTitle: 'Mandarin Tutor',
      description: tt.intro || undefined,
      knowsAbout:
        Array.isArray(tt.expertise) && tt.expertise.length ? tt.expertise : undefined,
      worksFor: { '@type': 'EducationalOrganization', name: SITE_NAME }
    }
  }))
  return {
    ...base,
    script: [
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'ItemList',
        name: t2('seo.teachers.title', t2('teachers.title', 'Mandarly Tutors')),
        numberOfItems: items.length,
        itemListElement: items
      })
    ]
  }
})

useHead(headPayload)

// =============================================================================
// 10. 生命周期
// =============================================================================
onMounted(async () => {
  readFiltersFromQuery()
  await nextTick()
  await Promise.all([loadFreeTrialStatus(), loadFirstPage()])
})

// 监听 route.query 变化(返回 / 前进时刷新)
watch(
  () => route.query,
  (q) => {
    // 防止自循环:syncQuery 触发的 watch 不重新 load
    const prev = JSON.stringify(buildQuery())
    if (JSON.stringify(q) === prev) return
    readFiltersFromQuery()
    loadFirstPage()
  }
)

watch(
  () => userStore.isLoggedIn,
  async () => {
    // 登录态切换影响免费体验 CTA、价格筛选 / 价格排序是否可用;老师公开资料仍保持可见。
    await loadFreeTrialStatus()
    readFiltersFromQuery()
    syncQuery()
    await loadFirstPage()
  }
)
</script>

<template>
  <div class="teacher-list">
    <PageIntro
      :eyebrow="t2('teachers.intro.eyebrow', 'Find your tutor')"
      :title="t2('teachers.title', '浏览老师')"
      :description="teacherIntroDescription"
      :primary-label="teacherIntroPrimaryLabel"
      :primary-to="teacherIntroPrimaryTo"
      :secondary-label="teacherIntroSecondaryLabel"
      :secondary-to="teacherIntroSecondaryTo"
    />

    <!-- D25: 头部 stats 一行(从已加载 list 聚合,不调新 API) -->
    <div v-if="listStats" class="teacher-list__stats-bar">
      <span class="teacher-list__stats-item">
        {{ templateText('teachers.stats.teachers', '{n} 位教师', { n: listStats.teacherCount }).replace('{n}', listStats.teacherCount) }}
      </span>
      <span class="teacher-list__stats-sep" aria-hidden="true">·</span>
      <span v-if="listStats.langCount" class="teacher-list__stats-item">
        {{ templateText('teachers.stats.languages', '{n} 种语言', { n: listStats.langCount }).replace('{n}', listStats.langCount) }}
      </span>
      <span v-if="listStats.langCount" class="teacher-list__stats-sep" aria-hidden="true">·</span>
      <span v-if="listStats.avgRating" class="teacher-list__stats-item">
        ★ {{ templateText('teachers.stats.avgRating', '平均评分 {r}', { r: listStats.avgRating }).replace('{r}', listStats.avgRating) }}
      </span>
    </div>

    <!-- 顶部 sticky 搜索 orb -->
    <div class="teacher-list__search-wrap">
      <TeacherSearchBar
        :model-value="keyword"
        :placeholder="t2('teachers.search.placeholder', '搜索老师名 / 标签')"
        @update:model-value="keyword = $event"
        @search="onSearch"
      />
    </div>

    <!-- 操作栏:筛选 / 排序 / 数量 -->
    <div class="teacher-list__toolbar">
      <el-button
        class="teacher-list__filter-btn"
        :icon="Filter"
        @click="openDrawer"
      >
        {{ t2('teachers.filter.title', '筛选') }}
        <span
          v-if="activeFilterCount > 0"
          class="teacher-list__filter-badge"
        >
          {{ activeFilterCount }}
        </span>
      </el-button>

      <TeacherSortSelect
        v-model="sort"
        :options="sortOptions"
        :prefix-label="t2('teachers.sort.label', '排序')"
        @change="onSortChange"
      />

      <span class="teacher-list__summary">{{ summaryText }}</span>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="teacher-list__grid" aria-busy="true">
      <el-skeleton
        v-for="i in 6"
        :key="i"
        animated
        class="teacher-list__skeleton"
      >
        <template #template>
          <el-skeleton-item variant="image" class="teacher-list__skeleton-media" />
          <div class="teacher-list__skeleton-body">
            <el-skeleton-item variant="h3" style="width: 60%" />
            <el-skeleton-item variant="text" style="width: 86%" />
            <el-skeleton-item variant="text" style="width: 48%" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <!-- 错误 -->
    <div v-else-if="errorMsg" class="teacher-list__state teacher-list__state--error">
      <p>{{ errorMsg }}</p>
      <el-button type="primary" @click="loadFirstPage">
        {{ t2('common.retry', '重试') }}
      </el-button>
    </div>

    <!-- 空态 -->
    <EmptyState
      v-else-if="teachers.length === 0"
      variant="no-result"
      :title="t2('teachers.empty.noResult.title', '没有找到匹配的老师')"
      :description="t2('teachers.empty.noResult.desc', '试试放宽筛选条件,或浏览所有老师')"
    >
      <template #action>
        <el-button @click="clearAllFilters">
          {{ t2('teachers.empty.noResult.clearFilters', '清除筛选') }}
        </el-button>
        <el-button type="primary" @click="goLevelCheck">
          {{ t2('teachers.empty.noResult.levelCheck', '做水平测试') }}
        </el-button>
      </template>
    </EmptyState>

    <!-- 主网格 -->
    <template v-else>
      <div class="teacher-list__grid">
        <TeacherCard
          v-for="t in decoratedTeachers"
          :key="t.userId"
          :teacher="t"
          variant="list"
        />
      </div>

      <!-- 加载更多 / 已全部加载 -->
      <div class="teacher-list__more">
        <el-button
          class="teacher-list__more-btn"
          :loading="loadingMore"
          :disabled="allLoaded"
          @click="loadMore"
        >
          {{ loadingMore ? t2('teachers.list.loadingMore', '加载中...') : loadMoreText }}
        </el-button>
      </div>
    </template>

    <!-- 抽屉 -->
    <TeacherFilterDrawer
      v-model:visible="drawerVisible"
      :filters="appliedFilters"
      :match-count="previewMatchCount"
      :count-loading="previewLoading"
      :option-schema="{
        accentOptions,
        priceBucketOptions,
        expertiseOptions,
        tagOptions
      }"
      :texts="drawerTexts"
      @preview="onPreviewFilters"
      @apply="onApplyFilters"
      @reset="onResetFilters"
    />
  </div>
</template>

<style scoped lang="scss">
.teacher-list {
  max-width: 1280px;
  margin: 0 auto;
  padding-block: brand.$spacing-6;
  padding-inline: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding-block: brand.$spacing-10;
    padding-inline: brand.$spacing-6;
  }

  // D25: 头部 stats 一行
  &__stats-bar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: brand.$spacing-2;
    margin-block-end: brand.$spacing-4;
    font-size: brand.$font-size-base;
    color: brand.$color-text-secondary;
  }

  &__stats-item {
    // 平均评分含 ★ 用主色
    &:last-child {
      color: brand.$brand-primary-deep;
      font-weight: 500;
    }
  }

  &__stats-sep {
    color: brand.$color-border-strong;
    user-select: none;
  }

  &__search-wrap {
    position: sticky;
    inset-block-start: 0;
    z-index: brand.$z-sticky;
    background: brand.$color-bg-page;
    padding-block: brand.$spacing-3;
    margin-block-end: brand.$spacing-4;
  }

  &__toolbar {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    flex-wrap: wrap;
    margin-block-end: brand.$spacing-5;
  }

  &__filter-btn {
    position: relative;
    height: 40px;
    border-radius: brand.$radius-base;
  }

  &__filter-badge {
    display: inline-grid;
    place-items: center;
    min-width: 20px;
    height: 20px;
    padding-inline: brand.$spacing-1;
    margin-inline-start: brand.$spacing-2;
    background: brand.$brand-primary;
    color: brand.$btn-primary-text;
    border-radius: brand.$radius-full;
    font-size: brand.$font-size-xs;
    font-weight: 600;
    line-height: 1;
  }

  &__summary {
    margin-inline-start: auto;
    font-size: brand.$font-size-base;
    color: brand.$brand-primary-deep;
    font-weight: 500;
  }

  &__state {
    text-align: center;
    padding-block: brand.$spacing-12;
    padding-inline: brand.$spacing-4;
    color: brand.$color-text-secondary;

    &--error {
      color: brand.$color-error;
    }
  }

  &__grid {
    display: grid;
    gap: brand.$spacing-4;
    grid-template-columns: 1fr;

    @media (min-width: brand.$bp-tablet) {
      grid-template-columns: repeat(2, 1fr);
      gap: brand.$spacing-5;
    }

    @media (min-width: brand.$bp-laptop) {
      grid-template-columns: repeat(3, 1fr);
    }

    @media (min-width: brand.$bp-desktop) {
      grid-template-columns: repeat(3, 1fr);
      gap: brand.$spacing-6;
    }
  }

  &__skeleton {
    background: brand.$color-bg-card;
    border-radius: brand.$radius-xl; // D25: card(14px) → xl(16px)
    overflow: hidden;
    box-shadow: brand.$shadow-base;
  }

  &__skeleton-media {
    width: 100%;
    height: auto;
    aspect-ratio: 4 / 3;
  }

  &__skeleton-body {
    padding: brand.$spacing-4;
    display: grid;
    gap: brand.$spacing-3;
  }

  &__more {
    margin-block-start: brand.$spacing-8;
    display: flex;
    justify-content: center;
  }

  &__more-btn {
    min-width: 240px;
    height: 44px;
    font-size: brand.$font-size-md;
    font-weight: 500;
    border-radius: brand.$radius-base;
  }
}
</style>
