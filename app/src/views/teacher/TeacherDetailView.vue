<script setup>
// =============================================================================
// TeacherDetailView — Mandarly 教师详情页(M5 Wave 2 Subagent B 重写)
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md
//   - § TeacherDetailView 改造(第 3 轮设计稿定版,2026-05-09)
//   - § 跨页流转图(redirect 链路)
//
// 布局:
//   - PC ≥ 1024:CSS grid 65% / 35% 双栏,右栏 sticky top: 96px(plan A4 修补:
//     必须用 grid + align-self: flex-start,不可用 el-row/el-col)
//   - PC < 1024:单列, BookingPanel 移到 TeacherHero 下方
//   - H5  < 1024:单列 + 底部 sticky CTA(滚动到 BookingPanel)
//
// 关键交互:
//   1) URL ?slot=YYYY-MM-DD-HH-mm 同步: 选中时段后 router.replace(query.slot=...)
//      —— 登录回跳后保留选择
//   2) 预约链路:
//      未登录       → /login?redirect={fullPath} (含 ?slot=)
//      余额不足     → ElMessageBox confirm "套餐课次不足,前往购买?"
//                     → /packages?redirect={fullPath}&fromBalance=1
//      余额够       → POST /booking/create → /booking/success/:id
//   3) 任何 router.replace/push 接 query.redirect / 路由参数 → 走 safeRedirect
//   4) 教师介绍 <p dir="auto"> 处理 RTL bidi
// =============================================================================
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useHead } from '@vueuse/head'
import { ElMessageBox } from 'element-plus'

import { getTeacher, listTeachers } from '@/api/teacher'
import { listMyPackages, createBooking } from '@/api/booking'
import { getTeacherStat, listTeacherReviews } from '@/api/review'
import { fromUTC, getUserTimezone } from '@/utils/datetime'
import { buildHead, jsonLd, SITE_ORIGIN, SITE_NAME } from '@/utils/seo'
import { safeRedirect } from '@/utils/safeRedirect'
import { getTeacherPricingMode } from '@/utils/teacherPricing'
import { useUserStore } from '@/stores/user'

import TeacherHero from '@/components/teacher/TeacherHero.vue'
import TeacherIntroVideo from '@/components/teacher/TeacherIntroVideo.vue'
import TeacherIntro from '@/components/teacher/TeacherIntro.vue'
import TeacherExpertise from '@/components/teacher/TeacherExpertise.vue'
import BookingPanel from '@/components/teacher/BookingPanel.vue'
import ReviewList from '@/components/teacher/ReviewList.vue'
import TeacherCard from '@/components/TeacherCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const REVIEW_PAGE_SIZE = 5

const route = useRoute()
const router = useRouter()
const { t, te, locale } = useI18n()
const userStore = useUserStore()

const t2 = (key, fallback, payload) => (te(key) ? t(key, payload || {}) : fallback)

const teacher = ref(null)
const loading = ref(true)
const errorMsg = ref('')

const userId = computed(() => Number(route.params.userId))

// ---- 时段选择 ----
const selectedSlot = ref(null) // { startUtc, endUtc, dateLabel, timeLabel }
const availableSlotCount = ref(null)

// ---- 评价 ----
const reviewStat = ref(null)
const reviewList = ref([])
const reviewPageNo = ref(1)
const reviewTotal = ref(0)
const reviewLoading = ref(false)

const hasMoreReviews = computed(() => reviewList.value.length < reviewTotal.value)

// ---- 相关老师 ----
const relatedTeachers = ref([])
const relatedLoading = ref(false)
const relatedLoadFailed = ref(false)

// ---- 学生套餐余额(仅用于 CTA 文案预判;提交前仍重新拉余额)----
const availableLessonCount = ref(null)

// ---- 预约提交 ----
const submitting = ref(false)
let detailLoadSeq = 0

// ---- 价格 / 时长(后端字段缺失时兜底)----
const pricePerSession = computed(() => {
  return teacher.value?.pricePerSession ?? null
})
const currency = computed(() => teacher.value?.currency || 'HKD')
const sessionMinutes = computed(() => teacher.value?.sessionMinutes || 25)

// ---- 评价对象装饰(本地化时间)----
const decoratedReviews = computed(() => {
  return reviewList.value.map((r) => ({
    ...r,
    createdLabel: formatReviewTime(r.lastEditedAt || r.createTime)
  }))
})

// =============================================================================
// 数据加载
// =============================================================================
onMounted(async () => {
  await loadTeacherDetail()
})

function resetTeacherDetailState() {
  teacher.value = null
  errorMsg.value = ''
  selectedSlot.value = null
  availableSlotCount.value = null
  reviewStat.value = null
  reviewList.value = []
  reviewPageNo.value = 1
  reviewTotal.value = 0
  relatedTeachers.value = []
  relatedLoadFailed.value = false
  availableLessonCount.value = null
  submitting.value = false
}

async function loadTeacherDetail() {
  const requestSeq = ++detailLoadSeq
  const targetUserId = userId.value
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await getTeacher(targetUserId)
    if (requestSeq !== detailLoadSeq) return
    teacher.value = data
    if (!teacher.value) {
      errorMsg.value = t2('teacher.notFound', '老师不存在')
    } else {
      // URL ?slot= 同步: 还原 selectedSlot(只是 dateLabel/timeLabel 占位,
      // 用户点 ScheduleSlotPicker 后会 emit 完整 payload 覆盖)
      restoreSelectedSlotFromQuery()
    }
  } catch (e) {
    if (requestSeq !== detailLoadSeq) return
    errorMsg.value = t2('common.error', '加载失败')
  } finally {
    if (requestSeq === detailLoadSeq) loading.value = false
  }
  if (requestSeq !== detailLoadSeq) return
  loadReviewStat()
  loadReviewPage(1)
  loadRelatedTeachers()
  loadMyPackageBalance()
}

async function loadReviewStat() {
  try {
    reviewStat.value = await getTeacherStat(userId.value)
  } catch {
    reviewStat.value = null
  }
}

async function loadReviewPage(pageNo) {
  reviewLoading.value = true
  try {
    const page = await listTeacherReviews(userId.value, {
      pageNo,
      pageSize: REVIEW_PAGE_SIZE
    })
    if (pageNo === 1) {
      reviewList.value = page?.list || []
    } else {
      reviewList.value = [...reviewList.value, ...(page?.list || [])]
    }
    reviewPageNo.value = pageNo
    reviewTotal.value = page?.total || 0
  } catch {
    if (pageNo === 1) reviewList.value = []
  } finally {
    reviewLoading.value = false
  }
}

async function loadRelatedTeachers() {
  relatedLoading.value = true
  relatedLoadFailed.value = false
  try {
    const resp = await listTeachers({
      pageNo: 1,
      pageSize: 5,
      sort: 'rating_desc'
    })
    const { list } = normalizeTeacherListResp(resp)
    relatedTeachers.value = list
      .filter((item) => Number(item.userId) !== Number(userId.value))
      .slice(0, 3)
      .map(decorateRelatedTeacher)
  } catch {
    relatedTeachers.value = []
    relatedLoadFailed.value = true
  } finally {
    relatedLoading.value = false
  }
}

async function loadMyPackageBalance() {
  if (!userStore.isLoggedIn || userStore.profile?.role === 'teacher') {
    availableLessonCount.value = null
    return
  }
  try {
    const list = (await listMyPackages(locale.value)) || []
    availableLessonCount.value = list.reduce((sum, item) => {
      return sum + Math.max(0, Number(item.remaining || 0))
    }, 0)
  } catch {
    availableLessonCount.value = null
  }
}

function normalizeTeacherListResp(resp) {
  if (!resp) return { list: [], count: 0 }
  if (Array.isArray(resp)) return { list: resp, count: resp.length }
  if (Array.isArray(resp.list)) return { list: resp.list, count: Number(resp.total) || resp.list.length }
  if (Array.isArray(resp.records)) return { list: resp.records, count: Number(resp.total) || resp.records.length }
  return { list: [], count: 0 }
}

function decorateRelatedTeacher(raw) {
  const accentArr = Array.isArray(raw.accent)
    ? raw.accent
    : raw.accent
      ? [raw.accent]
      : []
  const expertiseArr = Array.isArray(raw.expertise) ? raw.expertise : []
  const avgRating = Number(raw.avgRating)
  return {
    ...raw,
    avatar: raw.avatar || raw.avatarUrl || '',
    accent: accentArr,
    expertise: expertiseArr,
    badges: Array.isArray(raw.badges) ? raw.badges : [],
    hasIntroVideo: raw.hasIntroVideo ?? Boolean(raw.introVideoUrl),
    teacherTimezone: raw.teacherTimezone || raw.timezone || '',
    displayAccent: accentArr
      .map((code) => t2(`teacher.accent.${code}`, code))
      .filter(Boolean)
      .join(' · '),
    displayExpertise: expertiseArr.map((code) => t2(`teacher.expertise.${code}`, code)),
    avgRating: Number.isFinite(avgRating) ? avgRating : undefined,
    reviewCount: Number(raw.reviewCount || 0),
    finishedOrderCount: Number(raw.finishedOrderCount || 0)
  }
}

function formatReviewTime(iso) {
  if (!iso) return ''
  return fromUTC(iso, getUserTimezone()).format(
    locale.value === 'en' ? 'MMM D, YYYY' : 'YYYY-MM-DD'
  )
}

// =============================================================================
// SEO head
// =============================================================================
const headPayload = computed(() => {
  const teacherName = teacher.value?.nickname?.trim()
  const introBrief = (teacher.value?.intro || '').trim().slice(0, 80)
  const title = teacherName
    ? t('seo.teacherDetail.titleTemplate', { name: teacherName })
    : t('seo.teacherDetail.titleFallback')
  const description = teacherName
    ? t('seo.teacherDetail.descriptionTemplate', { name: teacherName, intro: introBrief })
    : t('seo.teacherDetail.descriptionFallback')

  const base = buildHead({
    title,
    description,
    path: `/teacher/${userId.value}`,
    ogType: 'profile',
    locale: locale.value
  })

  const scripts = []
  if (teacher.value) {
    const personId = `${SITE_ORIGIN}/teacher/${userId.value}`
    const person = {
      '@context': 'https://schema.org',
      '@type': 'Person',
      '@id': personId,
      name: teacherName || `Tutor #${userId.value}`,
      jobTitle: 'Mandarin Tutor',
      description: teacher.value.intro || undefined,
      knowsLanguage: teacher.value.languages?.length ? teacher.value.languages : undefined,
      knowsAbout: teacher.value.expertise?.length ? teacher.value.expertise : undefined,
      worksFor: {
        '@type': 'EducationalOrganization',
        name: SITE_NAME,
        url: SITE_ORIGIN + '/'
      }
    }
    if (reviewStat.value?.reviewCount > 0) {
      person.aggregateRating = {
        '@type': 'AggregateRating',
        ratingValue: Number(reviewStat.value.avgRating).toFixed(1),
        reviewCount: reviewStat.value.reviewCount,
        bestRating: '5',
        worstRating: '1'
      }
    }
    scripts.push(jsonLd(person))

    scripts.push(
      jsonLd({
        '@context': 'https://schema.org',
        '@type': 'Course',
        name: teacherName ? `1-on-1 Mandarin with ${teacherName}` : '1-on-1 Mandarin Lesson',
        description: teacher.value.intro || t('seo.teacherDetail.descriptionFallback'),
        provider: {
          '@type': 'EducationalOrganization',
          name: SITE_NAME,
          url: SITE_ORIGIN + '/'
        },
        instructor: { '@id': personId },
        inLanguage: ['zh-CN', 'en'],
        educationalLevel: 'Beginner to Advanced',
        teaches: teacher.value.expertise?.length
          ? teacher.value.expertise.join(', ')
          : 'Mandarin Chinese',
        hasCourseInstance: {
          '@type': 'CourseInstance',
          courseMode: 'Online',
          courseWorkload: 'PT25M'
        }
      })
    )

    if (reviewList.value.length > 0) {
      scripts.push(
        jsonLd({
          '@context': 'https://schema.org',
          '@type': 'ItemList',
          itemListElement: reviewList.value.slice(0, 10).map((r, i) => ({
            '@type': 'Review',
            position: i + 1,
            author: { '@type': 'Person', name: r.studentDisplayName || 'Student' },
            datePublished: r.lastEditedAt || r.createTime,
            reviewBody: r.content || undefined,
            reviewRating: {
              '@type': 'Rating',
              ratingValue: r.rating,
              bestRating: '5',
              worstRating: '1'
            },
            itemReviewed: { '@id': personId }
          }))
        })
      )
    }
  }

  return { ...base, script: scripts }
})

useHead(headPayload)

// =============================================================================
// 时段选择(URL 同步)
// =============================================================================
function onSelectSlot(payload) {
  selectedSlot.value = payload
  // 同步到 URL ?slot=YYYY-MM-DD-HH-mm(用 startUtc 转 ISO 截取前 16 位 + 减号)
  if (payload?.startUtc) {
    const slotKey = payload.startUtc
      .slice(0, 16)
      .replace(/T/, '-')
      .replace(/:/g, '-')
    router.replace({ query: { ...route.query, slot: slotKey } })
  }
}

function onAvailabilityChange(payload) {
  availableSlotCount.value = Number(payload?.totalSlots || 0)
}

function restoreSelectedSlotFromQuery() {
  // 仅占位:真实 dateLabel/timeLabel 由 ScheduleSlotPicker 用户点击后 emit
  // (登录回跳时,picker 加载 schedules 后会回到默认未选;query.slot 仅为视觉提示)
  // 这里不做强制回选 — 避免与 picker 内部 selectedKey 状态冲突
}

// =============================================================================
// 预约链路(redirect 防御 + 余额校验)
// =============================================================================
async function onClickBook() {
  if (!selectedSlot.value || submitting.value) return

  // ---- M6 B8:教师角色拦截(双角色禁购,spec §4.6)----
  if (userStore.profile?.role === 'teacher') {
    ElMessageBox.alert(
      t2('booking.teacherBlocked.message', '教师账号不可购买课程'),
      t2('booking.teacherBlocked.title', '提示'),
      { confirmButtonText: t2('common.ok', '确定'), type: 'warning' }
    )
    return
  }

  // ---- 1) 未登录 → /login?redirect={fullPath} ----
  if (!userStore.isLoggedIn) {
    const target = route.fullPath
    // safeRedirect 校验当前 fullPath 自身(防止恶意构造的入站 URL)
    const safeTarget = safeRedirect(target, '/teachers', router)
    router.push({ path: '/login', query: { redirect: safeTarget } })
    return
  }

  // ---- 2) 已登录 → 拉套餐余额 → 余额不足弹引导 / 余额够走预约 ----
  let myPackages = []
  try {
    myPackages = (await listMyPackages(locale.value)) || []
  } catch (e) {
    ElMessageBox.alert(
      e?.message || t2('common.error', '加载失败'),
      t2('booking.dialog.title', '预约确认'),
      { confirmButtonText: t2('common.ok', '确定') }
    )
    return
  }

  const validPackage = myPackages.find((p) => p.remaining > 0)
  availableLessonCount.value = myPackages.reduce((sum, item) => {
    return sum + Math.max(0, Number(item.remaining || 0))
  }, 0)

  if (!validPackage) {
    // ---- 余额不足 → confirm → /packages?from=teacher&teacherId=X ----
    try {
      await ElMessageBox.confirm(
        t2(
          'teacherDetail.balance.insufficient',
          '您的套餐课次不足,前往购买?'
        ),
        t2('booking.dialog.title', '预约确认'),
        {
          confirmButtonText: t2('teacherDetail.balance.goPackages', '前往购买'),
          cancelButtonText: t2('common.cancel', '取消'),
          type: 'warning'
        }
      )
      const safeTarget = safeRedirect(route.fullPath, '/teachers', router)
      router.push({
        path: '/packages',
        query: {
          redirect: safeTarget,
          from: 'teacher',
          teacherId: userId.value
        }
      })
    } catch {
      // 用户取消 — 不操作
    }
    return
  }

  // ---- 3) 正常预约 ----
  submitting.value = true
  try {
    const orderId = await createBooking({
      teacherId: userId.value,
      scheduledAt: new Date(selectedSlot.value.startUtc).getTime(),
      studentPackageId: validPackage.id
    })
    router.push(`/booking/success/${orderId}`)
  } catch (e) {
    ElMessageBox.alert(
      e?.message || t2('common.error', '加载失败'),
      t2('booking.dialog.title', '预约确认'),
      { confirmButtonText: t2('common.ok', '确定') }
    )
  } finally {
    submitting.value = false
  }
}

// =============================================================================
// H5 sticky CTA — 滚到 BookingPanel
// =============================================================================
function scrollToBookingPanel() {
  const el = document.getElementById('teacher-booking-panel')
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function onStickyCtaClick() {
  if (bookingCtaState.value === 'other-teachers') {
    router.push('/teachers')
    return
  }
  if (!selectedSlot.value) {
    scrollToBookingPanel()
    return
  }
  onClickBook()
}

// 数据 ready 后再 nextTick 一下让 dom 计算稳定
watch(loading, async (v) => {
  if (!v) await nextTick()
})

watch(
  () => route.params.userId,
  async (nextUserId, previousUserId) => {
    if (nextUserId === previousUserId) return
    resetTeacherDetailState()
    await loadTeacherDetail()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
)

watch(
  () => userStore.isLoggedIn,
  async () => {
    // 登录态切换只影响价格 / 预约资格;教师公开资料、评价和可约时段保持可见。
    await loadTeacherDetail()
  }
)

// M6 B8:教师角色判定 — 用于禁用「立即预约」按钮 + sticky CTA + 文案提示
const isTeacher = computed(() => userStore.profile?.role === 'teacher')
const canViewPrices = computed(() => userStore.isLoggedIn && !isTeacher.value)
const teacherPricingMode = computed(() =>
  getTeacherPricingMode({
    isLoggedIn: userStore.isLoggedIn,
    isTeacher: isTeacher.value,
    pricePerSession: pricePerSession.value
  })
)
const h5PriceText = computed(() => {
  if (teacherPricingMode.value === 'price') {
    const prefix = currency.value === 'HKD' ? 'HK$' : currency.value === 'USD' ? 'US$' : currency.value === 'CNY' ? '¥' : currency.value
    return `${prefix} ${pricePerSession.value}`
  }
  if (teacherPricingMode.value === 'package-credit') {
    return t2('teacherDetail.book.packageCreditPricing', '按套餐课次预约')
  }
  if (teacherPricingMode.value === 'login-package') {
    return t2('teacherDetail.book.loginToViewPackages', '登录后查看套餐')
  }
  return t2('common.price.loginToView', '登录后查看价格')
})

const bookingCtaState = computed(() => {
  if (availableSlotCount.value === 0) return 'other-teachers'
  if (
    selectedSlot.value &&
    userStore.isLoggedIn &&
    availableLessonCount.value === 0
  ) {
    return 'buy-package'
  }
  return 'book'
})

const stickyCtaText = computed(() => {
  if (isTeacher.value) return t2('booking.teacherBlocked.shortHint', '教师账号不可购买')
  if (submitting.value) return t2('booking.dialog.submitting', '提交中…')
  if (bookingCtaState.value === 'other-teachers') {
    return t2('teacherDetail.book.viewOtherTeachers', '查看其他老师')
  }
  if (!selectedSlot.value) {
    return t2('teacherDetail.book.emptyHint', '请先选择时段')
  }
  if (bookingCtaState.value === 'buy-package') {
    return t2('teacherDetail.book.buyPackageForTeacher', '购买套餐(为本老师预约)')
  }
  return t2('teacherDetail.book.bookNow', '立即预约')
})

function goTeachers() {
  router.push('/teachers')
}
</script>

<template>
  <div class="teacher-detail">
    <div class="teacher-detail__topbar">
      <router-link class="teacher-detail__back" to="/teachers">
        ← {{ t2('teacher.backToList', '返回列表') }}
      </router-link>
    </div>

    <div v-if="loading" class="teacher-detail__state">{{ t2('common.loading', '加载中…') }}</div>
    <div
      v-else-if="errorMsg"
      class="teacher-detail__state teacher-detail__state--error"
    >{{ errorMsg }}</div>

    <template v-else-if="teacher">
      <div class="teacher-detail__layout">
        <!-- 左主体 -->
        <div class="teacher-detail__main">
          <TeacherHero :teacher="teacher" :review-stat="reviewStat" />

          <TeacherIntroVideo
            v-if="teacher.introVideoUrl"
            :video-url="teacher.introVideoUrl"
            :poster-url="teacher.introVideoPoster || ''"
          />

          <TeacherIntro :intro="teacher.intro || ''" />

          <TeacherExpertise :expertise="teacher.expertise || []" />

          <ReviewList
            :reviews="decoratedReviews"
            :total="reviewTotal"
            :avg-rating="reviewStat?.avgRating"
            :review-count="reviewStat?.reviewCount || 0"
            :loading="reviewLoading"
            :has-more="hasMoreReviews"
            @load-more="loadReviewPage(reviewPageNo + 1)"
          />

          <section class="teacher-detail__related">
            <div class="teacher-detail__related-head">
              <h2 class="teacher-detail__related-title">
                {{ t2('teacherDetail.related.title', '相关老师') }}
              </h2>
              <router-link class="teacher-detail__related-link" to="/teachers">
                {{ t2('teacherDetail.related.viewAll', '查看全部') }}
              </router-link>
            </div>
            <div v-if="relatedLoading" class="teacher-detail__related-state">
              {{ t2('common.loading', '加载中…') }}
            </div>
            <div v-else-if="relatedTeachers.length > 0" class="teacher-detail__related-grid">
              <TeacherCard
                v-for="item in relatedTeachers"
                :key="item.userId"
                :teacher="item"
                variant="compact"
              />
            </div>
            <EmptyState
              v-else
              variant="no-result"
              :title="relatedLoadFailed ? t2('teacherDetail.related.loadFailed', '相关老师暂时加载失败') : t2('teacherDetail.related.emptyTitle', '暂时没有更多相关老师')"
              :description="t2('teacherDetail.related.emptyDesc', '可以回到老师列表,按时间、口音或学习目标重新筛选。')"
              :action-text="t2('teacherDetail.related.emptyAction', '浏览全部老师')"
              action-link="/teachers"
              compact
            />
          </section>
        </div>

        <!-- 右侧栏(PC sticky) -->
        <aside class="teacher-detail__sidebar">
          <BookingPanel
            :teacher-id="userId"
            :price-per-session="pricePerSession"
            :currency="currency"
            :session-minutes="sessionMinutes"
            :selected-slot="selectedSlot"
            :submitting="submitting"
            :teacher-blocked="isTeacher"
            :price-visible="canViewPrices"
            :cta-state="bookingCtaState"
            panel-id="teacher-booking-panel"
            @select-slot="onSelectSlot"
            @availability-change="onAvailabilityChange"
            @click-book="onClickBook"
            @click-other-teachers="goTeachers"
          />
        </aside>
      </div>

      <!-- H5 sticky bottom CTA -->
      <div class="teacher-detail__h5-cta">
        <div class="teacher-detail__h5-cta-summary">
          <span class="teacher-detail__h5-cta-price">
            {{ h5PriceText }}
          </span>
          <span
            v-if="reviewStat?.avgRating"
            class="teacher-detail__h5-cta-rating"
            aria-hidden="true"
          >
            ★ {{ Number(reviewStat.avgRating).toFixed(1) }}
          </span>
        </div>
        <button
          type="button"
          class="teacher-detail__h5-cta-btn"
          :disabled="submitting || isTeacher || (bookingCtaState !== 'other-teachers' && !selectedSlot)"
          :title="isTeacher ? t2('booking.teacherBlocked.tooltip', '教师账号不可购买,请切换学生账号') : ''"
          @click="onStickyCtaClick"
        >
          {{ stickyCtaText }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">

.teacher-detail {
  // 父容器禁止 overflow: hidden(plan A4) — sticky 失效坑
  min-height: 100vh;
  background: brand.$color-bg-page;
  padding-block: brand.$spacing-4 brand.$spacing-12;
  padding-inline: brand.$spacing-4;
  // H5 底部 sticky CTA 占位(64px + safe-area)
  padding-block-end: calc(64px + brand.$spacing-12 + env(safe-area-inset-bottom));

  @media (min-width: brand.$bp-laptop) {
    padding-block: brand.$spacing-6 brand.$spacing-12;
    padding-inline: brand.$spacing-6;
    padding-block-end: brand.$spacing-12;
  }
}

.teacher-detail__topbar {
  max-width: 1280px;
  margin: 0 auto brand.$spacing-4;
}

.teacher-detail__back {
  display: inline-block;
  background: transparent;
  border: none;
  color: brand.$color-text-secondary;
  font: inherit;
  font-size: brand.$font-size-sm;
  padding: brand.$spacing-2 0;
  text-decoration: none;
  cursor: pointer;

  &:hover {
    color: brand.$brand-primary-deep;
  }
}

.teacher-detail__state {
  text-align: center;
  padding: brand.$spacing-12 brand.$spacing-4;
  color: brand.$color-text-secondary;

  &--error {
    color: brand.$color-error;
  }
}

// =============================================================================
// 布局: PC ≥ 1024 双栏 / 单列
// 注意 plan A4: CSS grid 65/35, 父级禁 overflow: hidden,
//            sidebar align-self: flex-start 才能 sticky
// =============================================================================
.teacher-detail__layout {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-6;

  @media (min-width: brand.$bp-laptop) {
    grid-template-columns: 65% 35%;
    gap: brand.$spacing-8;
  }
}

.teacher-detail__main {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-6;
  min-width: 0;
}

.teacher-detail__sidebar {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-6;
  min-width: 0;

  @media (min-width: brand.$bp-laptop) {
    position: sticky;
    inset-block-start: 96px; // AppHeader 64 + 呼吸 32
    align-self: flex-start;  // plan A4: 关键, 让 sticky 在 grid item 工作
    max-height: calc(100vh - 96px - #{brand.$spacing-6});
    overflow-y: auto;
  }

}

.teacher-detail__related {
  background: var(--color-bg-card);
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6 brand.$spacing-5;
  box-shadow: brand.$shadow-sm;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8;
  }
}

.teacher-detail__related-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: brand.$spacing-4;
  margin-block-end: brand.$spacing-4;
}

.teacher-detail__related-title {
  margin: 0;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  color: brand.$color-text-primary;
}

.teacher-detail__related-link {
  flex: 0 0 auto;
  font-size: brand.$font-size-sm;
  color: brand.$brand-primary-deep;
  text-decoration: none;

  &:hover {
    color: brand.$brand-primary;
  }
}

.teacher-detail__related-state {
  padding-block: brand.$spacing-8;
  text-align: center;
  color: brand.$color-text-secondary;
  font-size: brand.$font-size-base;
}

.teacher-detail__related-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

// =============================================================================
// H5 sticky bottom CTA(< 1024 显示)
// =============================================================================
.teacher-detail__h5-cta {
  position: fixed;
  inset-block-end: 0;
  inset-inline-start: 0;
  inset-inline-end: 0;
  z-index: 10;
  background: var(--color-bg-card);
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.08);
  padding: brand.$spacing-3 brand.$spacing-4;
  padding-block-end: calc(brand.$spacing-3 + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;

  @media (min-width: brand.$bp-laptop) {
    display: none;
  }
}

.teacher-detail__h5-cta-summary {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.teacher-detail__h5-cta-price {
  font-size: brand.$font-size-md;
  font-weight: 600;
  color: brand.$brand-primary-deep;
}

.teacher-detail__h5-cta-rating {
  font-size: brand.$font-size-sm;
  color: brand.$brand-primary;
}

.teacher-detail__h5-cta-btn {
  flex-shrink: 0;
  appearance: none;
  height: 48px;
  padding: 0 brand.$spacing-6;
  background: brand.$brand-gradient;
  color: brand.$color-text-inverse;
  border: none;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-base;
  font-weight: 600;
  cursor: pointer;
  box-shadow: brand.$shadow-brand;
  transition: opacity 0.15s ease, transform 0.05s ease;

  &:disabled {
    background: brand.$brand-primary-disabled;
    box-shadow: none;
    cursor: not-allowed;
  }

  &:not(:disabled):active {
    transform: scale(0.98);
  }

  &:not(:disabled):hover {
    opacity: 0.92;
  }
}
</style>
