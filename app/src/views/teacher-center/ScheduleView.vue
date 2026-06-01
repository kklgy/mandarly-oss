<script setup>
/**
 * ScheduleView — 教师排课(M6 T1 / spec §5.1)
 *
 * PC ≥ 600px:WeekGrid 33×7 网格
 * H5 < 600px:WeekGridMobile 单日 + Vant swipe(B3 实现)
 *
 * 数据流:
 *   - getWeeklySchedule → 把 slot 区间 ({weekday, startTime, endTime}) 展开为 Set<"weekday-HH:MM">
 *   - getExceptions(from, to) → 本周例外列表(week 切换时按 from..to 拉)
 *   - 点击格子 → toggleWeeklySlot(乐观更新 + 失败回滚)
 *   - 长按格子 → 打开例外抽屉(添加 / 删除单次例外)
 *
 * 周切换:
 *   - weekStart 默认本周一(教师本地时区)
 *   - ← / → 前后切周;exceptions 重新拉取
 *
 * 时区:
 *   - 教师本地时区由后端 schedule.timezone 返回(默认浏览器时区)
 *   - 模板永远不受时区切换影响(数据库存的就是教师本地)
 */
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElDrawer, ElButton } from 'element-plus'
import { useBreakpoint } from '@/composables/useBreakpoint'
import dayjs from '@/utils/datetime'
import {
  getWeeklySchedule,
  toggleWeeklySlot,
  getExceptions,
  toggleException
} from '@/api/teacher-center/schedule'
import WeekGrid from '@/components/teacher-center/WeekGrid.vue'
import WeekGridMobile from '@/components/teacher-center/WeekGridMobile.vue'

const { t } = useI18n()
const { isPC } = useBreakpoint(600)

// ---------- 状态 ----------
const loading = ref(true)
const saving = ref(false)
const errorMsg = ref('')

const timezone = ref('')          // 教师本地时区(IANA)
const weeklySlots = ref(new Set()) // Set<"weekday-HH:MM"> — 模板可约
const exceptions = ref([])         // 本周例外

// 首次进入排课页且无任何 slot 时显示 onboarding;dismiss 后持久化
const ONBOARDING_KEY = 'mandarly_tc_schedule_onboarded'
const onboardingDismissed = ref(
  typeof localStorage !== 'undefined' && localStorage.getItem(ONBOARDING_KEY) === '1'
)
// errorMsg 也算"零数据"语义,不挡引导(教师该看到指引,API 错误另行重试)
const showOnboarding = computed(
  () => !loading.value && weeklySlots.value.size === 0 && !onboardingDismissed.value
)
function dismissOnboarding() {
  onboardingDismissed.value = true
  try { localStorage.setItem(ONBOARDING_KEY, '1') } catch (e) { /* SSR / 隐私模式 */ }
}

const TIME_PERIODS = [
  { key: 'all', startHour: 7, endHour: 24, i18n: 'teacherCenter.schedule.periodAll' },
  { key: 'morning', startHour: 7, endHour: 12, i18n: 'teacherCenter.schedule.periodMorning' },
  { key: 'afternoon', startHour: 12, endHour: 18, i18n: 'teacherCenter.schedule.periodAfternoon' },
  { key: 'evening', startHour: 18, endHour: 24, i18n: 'teacherCenter.schedule.periodEvening' }
]

function getMonday(date = dayjs()) {
  const d = dayjs(date)
  const day = d.day()
  return d.add(day === 0 ? -6 : 1 - day, 'day').format('YYYY-MM-DD')
}

function getDefaultPeriodKey() {
  const hour = dayjs().hour()
  if (hour < 12) return 'morning'
  if (hour < 18) return 'afternoon'
  return 'evening'
}

// 本周起点(周一,教师本地时区下的 YYYY-MM-DD)
const weekStart = ref(getMonday())
const weekEnd = computed(() => dayjs(weekStart.value).add(6, 'day').format('YYYY-MM-DD'))
const activePeriodKey = ref(getDefaultPeriodKey())
const activePeriod = computed(
  () => TIME_PERIODS.find((item) => item.key === activePeriodKey.value) || TIME_PERIODS[0]
)
const editMode = ref('template')

const EDIT_MODES = [
  {
    key: 'template',
    title: 'teacherCenter.schedule.modeTemplate',
    desc: 'teacherCenter.schedule.modeTemplateDesc'
  },
  {
    key: 'exception',
    title: 'teacherCenter.schedule.modeException',
    desc: 'teacherCenter.schedule.modeExceptionDesc'
  }
]

// ---------- 拉数据 ----------
async function loadWeekly() {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await getWeeklySchedule()
    timezone.value = data?.timezone || Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'
    weeklySlots.value = expandSlotsToSet(data?.slots || [])
  } catch (e) {
    errorMsg.value = e?.message || t('teacherCenter.schedule.loadFailed')
    weeklySlots.value = new Set()
  } finally {
    loading.value = false
  }
}

async function loadExceptions() {
  try {
    const list = await getExceptions({ from: weekStart.value, to: weekEnd.value })
    exceptions.value = list || []
  } catch (e) {
    exceptions.value = []
  }
}

// 把区间型 slot 展开成 30-min 集合(集合元素 "weekday-HH:MM")
function expandSlotsToSet(slots) {
  const set = new Set()
  for (const s of slots) {
    if (s.weekday == null || !s.startTime || !s.endTime) continue
    const wd = Number(s.weekday)
    const [sH, sM] = String(s.startTime).split(':').map(Number)
    const [eH, eM] = String(s.endTime).split(':').map(Number)
    let curM = sH * 60 + sM
    const endM = eH * 60 + eM
    while (curM + 30 <= endM) {
      const h = Math.floor(curM / 60)
      const m = curM % 60
      set.add(`${wd}-${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`)
      curM += 30
    }
  }
  return set
}

// ---------- 周切换 ----------
const weekRangeLabel = computed(() => {
  return t('teacherCenter.schedule.weekRange', {
    from: dayjs(weekStart.value).format('M-D'),
    to: dayjs(weekEnd.value).format('M-D')
  })
})

function gotoPrevWeek() {
  weekStart.value = dayjs(weekStart.value).subtract(7, 'day').format('YYYY-MM-DD')
}
function gotoNextWeek() {
  weekStart.value = dayjs(weekStart.value).add(7, 'day').format('YYYY-MM-DD')
}
function gotoCurrentWeek() {
  weekStart.value = getMonday()
}

// 周切换时重拉例外
watch(weekStart, () => {
  loadExceptions()
})

// ---------- 切换单元格 ----------
async function onToggle({ dayOfWeek, hh, mm, available }) {
  if (saving.value) return
  saving.value = true
  const key = `${dayOfWeek}-${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  // 乐观更新
  const nextSet = new Set(weeklySlots.value)
  if (available) nextSet.add(key)
  else nextSet.delete(key)
  weeklySlots.value = nextSet

  try {
    await toggleWeeklySlot({
      dayOfWeek,
      hh,
      mm,
      available,
      timezone: timezone.value
    })
    ElMessage.success(t('teacherCenter.schedule.toggleSuccess'))
  } catch (e) {
    // 回滚
    const rollback = new Set(weeklySlots.value)
    if (available) rollback.delete(key)
    else rollback.add(key)
    weeklySlots.value = rollback
    ElMessage.error(e?.message || t('teacherCenter.schedule.toggleFailed'))
  } finally {
    saving.value = false
  }
}

// ---------- 例外抽屉 ----------
const drawerVisible = ref(false)
const drawerTarget = ref(null) // { date, hh, mm }

function onOpenExceptionDrawer(payload) {
  drawerTarget.value = payload
  drawerVisible.value = true
}

const drawerExceptions = computed(() => {
  if (!drawerTarget.value) return exceptions.value
  // 抽屉只展示当天例外(更聚焦)
  return exceptions.value.filter((ex) => {
    const d = typeof ex.exceptionDate === 'string'
      ? ex.exceptionDate.slice(0, 10)
      : dayjs(ex.exceptionDate).format('YYYY-MM-DD')
    return d === drawerTarget.value.date
  })
})

async function addException(action) {
  if (!drawerTarget.value || saving.value) return
  saving.value = true
  try {
    await toggleException({
      date: drawerTarget.value.date,
      hh: drawerTarget.value.hh,
      mm: drawerTarget.value.mm,
      action,
      timezone: timezone.value
    })
    await loadExceptions()
    ElMessage.success(t('teacherCenter.schedule.exceptionAddSuccess'))
    drawerVisible.value = false
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.schedule.toggleFailed'))
  } finally {
    saving.value = false
  }
}

async function removeException(ex) {
  if (saving.value) return
  saving.value = true
  try {
    const dateYmd = typeof ex.exceptionDate === 'string'
      ? ex.exceptionDate.slice(0, 10)
      : dayjs(ex.exceptionDate).format('YYYY-MM-DD')
    const [h, m] = String(ex.startTime).split(':').map(Number)
    await toggleException({
      date: dateYmd,
      hh: h,
      mm: m,
      action: 'remove',
      timezone: timezone.value
    })
    await loadExceptions()
    ElMessage.success(t('teacherCenter.schedule.exceptionRemoveSuccess'))
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.schedule.toggleFailed'))
  } finally {
    saving.value = false
  }
}

// ---------- 生命周期 ----------
onMounted(async () => {
  await loadWeekly()
  await loadExceptions()
})
</script>

<template>
  <div class="tc-schedule">
    <!-- 顶部周切换 + 操作 -->
    <header class="tc-schedule__header">
      <div class="tc-schedule__title-row">
        <h1 class="tc-schedule__title">{{ t('teacherCenter.schedule.title') }}</h1>
        <p class="tc-schedule__subtitle">{{ t('teacherCenter.schedule.subtitle') }}</p>
      </div>

      <div class="tc-schedule__toolbar">
        <div class="tc-schedule__week-switch">
          <button type="button" class="tc-schedule__week-btn" @click="gotoPrevWeek">
            ← {{ t('teacherCenter.schedule.prevWeek') }}
          </button>
          <span class="tc-schedule__week-range">{{ weekRangeLabel }}</span>
          <button type="button" class="tc-schedule__week-btn" @click="gotoNextWeek">
            {{ t('teacherCenter.schedule.nextWeek') }} →
          </button>
          <button
            type="button"
            class="tc-schedule__week-btn tc-schedule__week-btn--ghost"
            @click="gotoCurrentWeek"
          >
            {{ t('teacherCenter.schedule.currentWeek') }}
          </button>
        </div>

        <div class="tc-schedule__actions">
          <span v-if="timezone" class="tc-schedule__tz">
            {{ t('teacherCenter.schedule.timezoneLabel', { tz: timezone }) }}
          </span>
        </div>
      </div>

      <div class="tc-schedule__modes" role="radiogroup" :aria-label="t('teacherCenter.schedule.modeLabel')">
        <button
          v-for="mode in EDIT_MODES"
          :key="mode.key"
          type="button"
          class="tc-schedule__mode-btn"
          :class="{ 'is-active': editMode === mode.key }"
          :aria-checked="editMode === mode.key"
          role="radio"
          @click="editMode = mode.key"
        >
          <span class="tc-schedule__mode-title">{{ t(mode.title) }}</span>
          <span class="tc-schedule__mode-desc">{{ t(mode.desc) }}</span>
        </button>
      </div>

      <div class="tc-schedule__periods" role="group" :aria-label="t('teacherCenter.schedule.periodLabel')">
        <span class="tc-schedule__period-label">{{ t('teacherCenter.schedule.periodLabel') }}</span>
        <button
          v-for="period in TIME_PERIODS"
          :key="period.key"
          type="button"
          class="tc-schedule__period-btn"
          :class="{ 'is-active': activePeriodKey === period.key }"
          :aria-pressed="activePeriodKey === period.key"
          @click="activePeriodKey = period.key"
        >
          {{ t(period.i18n) }}
        </button>
      </div>
    </header>

    <!-- 首次进入引导(无任何可约时段时显示,dismiss 后持久化不再出现)-->
    <aside v-if="showOnboarding" class="tc-schedule__onboarding" role="note">
      <div class="tc-schedule__onboarding-body">
        <h2 class="tc-schedule__onboarding-title">{{ t('teacherCenter.schedule.onboarding.title') }}</h2>
        <ul class="tc-schedule__onboarding-steps">
          <li>{{ t('teacherCenter.schedule.onboarding.step1') }}</li>
          <li>{{ t('teacherCenter.schedule.onboarding.step2') }}</li>
          <li>{{ t('teacherCenter.schedule.onboarding.step3') }}</li>
        </ul>
      </div>
      <el-button type="primary" plain size="small" @click="dismissOnboarding">
        {{ t('teacherCenter.schedule.onboarding.dismiss') }}
      </el-button>
    </aside>

    <!-- 加载 / 错误 -->
    <div v-if="loading" class="tc-schedule__state">
      {{ t('common.loading', { defaultValue: '加载中…' }) }}
    </div>
    <div v-else-if="errorMsg" class="tc-schedule__state tc-schedule__state--error">
      <p>{{ errorMsg }}</p>
      <el-button size="small" @click="loadWeekly">
        {{ t('common.retry', { defaultValue: '重试' }) }}
      </el-button>
    </div>

    <!-- 网格(PC / H5 分支) -->
    <template v-else>
      <WeekGrid
        v-if="isPC"
        :weekly-slots="weeklySlots"
        :exceptions="exceptions"
        :week-start="weekStart"
        :timezone="timezone"
        :edit-mode="editMode"
        :start-hour="activePeriod.startHour"
        :end-hour="activePeriod.endHour"
        :saving="saving"
        @toggle="onToggle"
        @open-exception-drawer="onOpenExceptionDrawer"
      />
      <WeekGridMobile
        v-else
        :weekly-slots="weeklySlots"
        :exceptions="exceptions"
        :week-start="weekStart"
        :timezone="timezone"
        :edit-mode="editMode"
        :start-hour="activePeriod.startHour"
        :end-hour="activePeriod.endHour"
        :saving="saving"
        @toggle="onToggle"
        @open-exception-drawer="onOpenExceptionDrawer"
      />
    </template>

    <!-- 例外抽屉(单元格长按 / 右键 触发) -->
    <el-drawer
      v-model="drawerVisible"
      :title="t('teacherCenter.schedule.exceptionDrawerTitle')"
      direction="rtl"
      size="380px"
      :append-to-body="true"
    >
      <div v-if="drawerTarget" class="tc-schedule__drawer">
        <p class="tc-schedule__drawer-info">
          <strong>{{ t('teacherCenter.schedule.exceptionDate') }}:</strong> {{ drawerTarget.date }}
          &nbsp;
          <strong>{{ t('teacherCenter.schedule.exceptionTime') }}:</strong>
          {{ String(drawerTarget.hh).padStart(2, '0') }}:{{ String(drawerTarget.mm).padStart(2, '0') }}
        </p>

        <div class="tc-schedule__drawer-actions">
          <el-button type="warning" plain @click="addException('closed')">
            {{ t('teacherCenter.schedule.exceptionActionBlockOnce') }}
          </el-button>
          <el-button type="success" plain @click="addException('extra')">
            {{ t('teacherCenter.schedule.exceptionActionMakeOnce') }}
          </el-button>
        </div>

        <h3 class="tc-schedule__drawer-title">{{ t('teacherCenter.schedule.exceptionDrawerTitle') }} · {{ drawerTarget.date }}</h3>
        <p v-if="drawerExceptions.length === 0" class="tc-schedule__drawer-empty">
          {{ t('teacherCenter.schedule.exceptionEmpty') }}
        </p>
        <ul v-else class="tc-schedule__drawer-list">
          <li
            v-for="ex in drawerExceptions"
            :key="ex.id"
            class="tc-schedule__drawer-item"
          >
            <span>
              {{ String(ex.startTime).slice(0, 5) }}–{{ String(ex.endTime).slice(0, 5) }}
              <span
                class="tc-schedule__drawer-badge"
                :class="`is-${ex.type}`"
              >
                {{ ex.type === 'closed'
                  ? t('teacherCenter.schedule.exceptionActionBlockOnce')
                  : t('teacherCenter.schedule.exceptionActionMakeOnce') }}
              </span>
            </span>
            <el-button link type="danger" size="small" @click="removeException(ex)">
              {{ t('common.delete', { defaultValue: '删除' }) }}
            </el-button>
          </li>
        </ul>
      </div>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.tc-schedule {
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }

  &__header {
    margin-block-end: brand.$spacing-4;
  }

  &__onboarding {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
    padding: brand.$spacing-4 brand.$spacing-5;
    margin-block-end: brand.$spacing-4;
    background: brand.$brand-primary-soft;
    border: 1px solid brand.$brand-primary;
    border-radius: brand.$radius-lg;

    @media (min-width: brand.$bp-tablet) {
      flex-direction: row;
      align-items: flex-start;
      justify-content: space-between;
      gap: brand.$spacing-4;
    }
  }

  &__onboarding-body {
    flex: 1;
    min-width: 0;
  }

  &__onboarding-title {
    margin: 0 0 brand.$spacing-2;
    font-size: brand.$font-size-base;
    font-weight: 600;
    color: brand.$brand-primary-deep;
  }

  &__onboarding-steps {
    margin: 0;
    padding-inline-start: 0;
    list-style: none;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;

    li + li {
      margin-block-start: brand.$spacing-1;
    }
  }

  &__title-row {
    margin-block-end: brand.$spacing-4;
  }

  &__title {
    margin: 0 0 brand.$spacing-1;
    font-size: brand.$font-size-2xl;
    font-weight: 700;
    color: brand.$color-text-primary;
    line-height: brand.$line-height-tight;
  }

  &__subtitle {
    margin: 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
  }

  &__toolbar {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    gap: brand.$spacing-3;
  }

  &__week-switch {
    display: flex;
    align-items: center;
    gap: brand.$spacing-2;
    flex-wrap: wrap;
  }

  &__week-btn {
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-base;
    padding: brand.$spacing-1 brand.$spacing-3;
    font-size: brand.$font-size-sm;
    cursor: pointer;
    color: brand.$color-text-primary;
    transition: background 0.15s, border-color 0.15s;

    &:hover {
      background: brand.$brand-primary-soft;
      border-color: brand.$brand-primary;
      color: brand.$brand-primary-deep;
    }

    &--ghost {
      background: transparent;
      border-color: transparent;
      color: brand.$color-text-secondary;

      &:hover {
        background: brand.$color-bg-strong;
      }
    }
  }

  &__week-range {
    font-weight: 600;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-base;
    min-width: 9em;
    text-align: center;
  }

  &__periods {
    display: flex;
    align-items: center;
    gap: brand.$spacing-2;
    flex-wrap: wrap;
    margin-block-start: brand.$spacing-3;
    padding: brand.$spacing-2;
    border: 1px solid brand.$color-border-light;
    border-radius: brand.$radius-lg;
    background: brand.$color-bg-card;
  }

  &__modes {
    display: grid;
    grid-template-columns: 1fr;
    gap: brand.$spacing-2;
    margin-block-start: brand.$spacing-3;

    @media (min-width: brand.$bp-tablet) {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  &__mode-btn {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
    min-height: 56px;
    padding: brand.$spacing-3 brand.$spacing-4;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    background: brand.$color-bg-card;
    color: brand.$color-text-secondary;
    cursor: pointer;
    text-align: start;
    transition: background 0.15s, border-color 0.15s, color 0.15s;

    &:hover {
      background: brand.$color-bg-strong;
      color: brand.$color-text-primary;
    }

    &.is-active {
      background: brand.$brand-primary-soft;
      border-color: brand.$brand-primary;
      color: brand.$brand-primary-deep;
    }
  }

  &__mode-title {
    font-size: brand.$font-size-base;
    font-weight: 700;
    color: brand.$color-text-primary;
  }

  &__mode-btn.is-active &__mode-title {
    color: brand.$brand-primary-deep;
  }

  &__mode-desc {
    font-size: brand.$font-size-xs;
    line-height: brand.$line-height-base;
  }

  &__period-label {
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
    padding-inline: brand.$spacing-1;
  }

  &__period-btn {
    border: 1px solid transparent;
    border-radius: brand.$radius-base;
    background: transparent;
    color: brand.$color-text-secondary;
    cursor: pointer;
    font-size: brand.$font-size-sm;
    min-height: 32px;
    padding: brand.$spacing-1 brand.$spacing-3;
    transition: background 0.15s, border-color 0.15s, color 0.15s;

    &:hover {
      background: brand.$color-bg-strong;
      color: brand.$color-text-primary;
    }

    &.is-active {
      background: brand.$brand-primary-soft;
      border-color: brand.$brand-primary;
      color: brand.$brand-primary-deep;
      font-weight: 600;
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
  }

  &__tz {
    font-size: brand.$font-size-sm;
    color: brand.$color-text-tertiary;
  }

  &__state {
    text-align: center;
    padding: brand.$spacing-12 brand.$spacing-4;
    color: brand.$color-text-secondary;

    &--error {
      color: brand.$color-error;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: brand.$spacing-3;
    }
  }

  &__drawer {
    padding: 0 brand.$spacing-3;
  }

  &__drawer-info {
    padding: brand.$spacing-3;
    background: brand.$color-bg-page;
    border-radius: brand.$radius-base;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    margin: 0 0 brand.$spacing-4;
  }

  &__drawer-actions {
    display: flex;
    gap: brand.$spacing-2;
    margin-block-end: brand.$spacing-6;
  }

  &__drawer-title {
    margin: 0 0 brand.$spacing-3;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__drawer-empty {
    color: brand.$color-text-tertiary;
    font-size: brand.$font-size-sm;
    padding: brand.$spacing-4 0;
    text-align: center;
  }

  &__drawer-list {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__drawer-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: brand.$spacing-2 brand.$spacing-3;
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-base;
    font-size: brand.$font-size-sm;
  }

  &__drawer-badge {
    display: inline-block;
    margin-inline-start: brand.$spacing-2;
    padding: 1px brand.$spacing-2;
    border-radius: brand.$radius-sm;
    font-size: brand.$font-size-xs;
    font-weight: 600;

    &.is-closed {
      background: rgba(255, 77, 79, 0.12);
      color: brand.$color-error;
    }

    &.is-extra {
      background: rgba(82, 196, 26, 0.16);
      color: brand.$color-success;
    }
  }
}
</style>
