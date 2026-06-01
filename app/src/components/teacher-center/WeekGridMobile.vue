<script setup>
/**
 * WeekGridMobile — 教师排课 H5 单日视图(M6 T1 / spec §5.1 H5 < 600px)
 *
 * B3 实现:Vant swipe + 7 圆点切换星期;单日时段列表(竖向)。
 * B2 占位:暂用最简列表,B3 用 Vant <van-swipe> 替换。
 *
 * Props / Emits 与 WeekGrid 对齐(便于父组件无感切换)。
 */
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from '@/utils/datetime'

const props = defineProps({
  weeklySlots: { type: Object, required: true },
  exceptions: { type: Array, default: () => [] },
  weekStart: { type: String, required: true },
  timezone: { type: String, default: '' },
  editMode: {
    type: String,
    default: 'template',
    validator: (value) => ['template', 'exception'].includes(value)
  },
  startHour: { type: Number, default: 7 },
  endHour: { type: Number, default: 24 },
  saving: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle', 'open-exception-drawer'])

const { t } = useI18n()

// 当前选中的星期偏移(0=本周一,1=本周二,..., 6=本周日)
const dayIndex = ref(dayjs().diff(dayjs(props.weekStart), 'day'))
if (dayIndex.value < 0 || dayIndex.value > 6) dayIndex.value = 0

// UI weekday 序列(对齐 WeekGrid)
const WEEKDAY_ORDER = [1, 2, 3, 4, 5, 6, 0]

const days = computed(() =>
  WEEKDAY_ORDER.map((wd, idx) => {
    const date = dayjs(props.weekStart).add(idx, 'day')
    return {
      weekday: wd,
      idx,
      label: ['日', '一', '二', '三', '四', '五', '六'][wd],
      dateLabel: date.format('M-D'),
      dateYmd: date.format('YYYY-MM-DD'),
      isToday: date.isSame(dayjs(), 'day')
    }
  })
)

const currentDay = computed(() => days.value[dayIndex.value])

// 33 行 30-min 时段
const ALL_ROWS = (() => {
  const rows = []
  for (let h = 7; h <= 23; h++) {
    rows.push({ hh: h, mm: 0, label: `${String(h).padStart(2, '0')}:00` })
    if (h < 23) rows.push({ hh: h, mm: 30, label: `${String(h).padStart(2, '0')}:30` })
  }
  return rows
})()

const visibleRows = computed(() =>
  ALL_ROWS.filter((row) => {
    const minute = row.hh * 60 + row.mm
    return minute >= props.startHour * 60 && minute < props.endHour * 60
  })
)

const exceptionMap = computed(() => {
  const map = new Map()
  for (const ex of props.exceptions || []) {
    if (!ex.exceptionDate || !ex.startTime) continue
    const dateYmd = typeof ex.exceptionDate === 'string'
      ? ex.exceptionDate.slice(0, 10)
      : dayjs(ex.exceptionDate).format('YYYY-MM-DD')
    const startHHMM = String(ex.startTime).slice(0, 5)
    const endHHMM = ex.endTime ? String(ex.endTime).slice(0, 5) : startHHMM
    const [sH, sM] = startHHMM.split(':').map(Number)
    const [eH, eM] = endHHMM.split(':').map(Number)
    let curM = sH * 60 + sM
    const endM = Math.max(eH * 60 + eM, curM + 30)
    while (curM + 30 <= endM) {
      const h = Math.floor(curM / 60)
      const m = curM % 60
      const k = `${dateYmd}-${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
      if (!map.has(k)) map.set(k, { type: ex.type, id: ex.id })
      curM += 30
    }
  }
  return map
})

function cellInfo(row) {
  const day = currentDay.value
  const hhmm = `${String(row.hh).padStart(2, '0')}:${String(row.mm).padStart(2, '0')}`
  const baseAvailable = props.weeklySlots.has(`${day.weekday}-${hhmm}`)
  const ex = exceptionMap.value.get(`${day.dateYmd}-${hhmm}`)
  return { baseAvailable, ex }
}

function onCellTap(row) {
  if (props.saving) return
  if (props.editMode === 'exception') {
    onCellLongPress(row)
    return
  }
  const hhmm = `${String(row.hh).padStart(2, '0')}:${String(row.mm).padStart(2, '0')}`
  const baseAvailable = props.weeklySlots.has(`${currentDay.value.weekday}-${hhmm}`)
  emit('toggle', {
    dayOfWeek: currentDay.value.weekday,
    hh: row.hh,
    mm: row.mm,
    available: !baseAvailable
  })
}

function onCellLongPress(row) {
  if (props.saving) return
  emit('open-exception-drawer', {
    date: currentDay.value.dateYmd,
    hh: row.hh,
    mm: row.mm
  })
}

// 简单长按检测(B3 用 Vant 替换时改 vant 内置 touch)
const longPressTimers = new Map()
function bindLongPress(row, e) {
  e.stopPropagation()
  const id = `${row.hh}-${row.mm}`
  const timer = window.setTimeout(() => {
    onCellLongPress(row)
    longPressTimers.delete(id)
  }, 500)
  longPressTimers.set(id, timer)
}
function cancelLongPress(row) {
  const id = `${row.hh}-${row.mm}`
  if (longPressTimers.has(id)) {
    window.clearTimeout(longPressTimers.get(id))
    longPressTimers.delete(id)
  }
}
</script>

<template>
  <div
    class="week-mobile"
    :class="{ 'is-exception-mode': editMode === 'exception' }"
    :aria-busy="saving"
  >
    <!-- 星期 chips(7 圆点) -->
    <div class="week-mobile__days" role="tablist">
      <button
        v-for="(d, idx) in days"
        :key="d.weekday"
        type="button"
        class="week-mobile__day-chip"
        :class="{
          'is-active': idx === dayIndex,
          'is-today': d.isToday
        }"
        role="tab"
        :aria-selected="idx === dayIndex"
        @click="dayIndex = idx"
      >
        <span class="week-mobile__day-label">{{ d.label }}</span>
        <span class="week-mobile__day-date">{{ d.dateLabel }}</span>
      </button>
    </div>

    <!-- 当前日时段列表 -->
    <ul class="week-mobile__list">
      <li
        v-for="row in visibleRows"
        :key="`${row.hh}-${row.mm}`"
        class="week-mobile__row"
      >
        <span class="week-mobile__time">{{ row.label }}</span>
        <button
          type="button"
          class="week-mobile__cell"
          :class="{
            'is-base-available': cellInfo(row).baseAvailable,
            'is-exception-closed': cellInfo(row).ex?.type === 'closed',
            'is-exception-extra': cellInfo(row).ex?.type === 'extra'
          }"
          :disabled="saving"
          @click="onCellTap(row)"
          @touchstart.passive="(e) => bindLongPress(row, e)"
          @touchend="cancelLongPress(row)"
          @touchcancel="cancelLongPress(row)"
          @contextmenu.prevent="onCellLongPress(row)"
        >
          {{ cellInfo(row).baseAvailable
            ? t('teacherCenter.schedule.slotWeeklyAvailable')
            : t('teacherCenter.schedule.slotUnavailable') }}
          <span v-if="cellInfo(row).ex?.type === 'closed'" class="week-mobile__ex-badge">
            {{ t('teacherCenter.schedule.slotClosedThisWeek') }}
          </span>
          <span v-else-if="cellInfo(row).ex?.type === 'extra'" class="week-mobile__ex-badge">
            {{ t('teacherCenter.schedule.slotOpenThisWeek') }}
          </span>
        </button>
      </li>
    </ul>
  </div>
</template>

<style lang="scss" scoped>
.week-mobile {
  &__days {
    display: flex;
    gap: brand.$spacing-1;
    overflow-x: auto;
    padding: brand.$spacing-2 0;
    margin-block-end: brand.$spacing-3;
    -webkit-overflow-scrolling: touch;

    &::-webkit-scrollbar { display: none; }
  }

  &__day-chip {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    padding: brand.$spacing-2 brand.$spacing-3;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-base;
    background: brand.$color-bg-card;
    cursor: pointer;
    color: brand.$color-text-secondary;
    transition: background 0.15s, border-color 0.15s;
    min-width: 48px;

    &.is-active {
      background: brand.$brand-primary-soft;
      border-color: brand.$brand-primary;
      color: brand.$brand-primary-deep;
      font-weight: 600;
    }

    &.is-today:not(.is-active) {
      border-color: brand.$brand-primary;
    }
  }

  &__day-label {
    font-size: brand.$font-size-sm;
  }

  &__day-date {
    font-size: brand.$font-size-xs;
    color: brand.$color-text-tertiary;
  }

  &__list {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-1;
  }

  &__row {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    padding: brand.$spacing-2 brand.$spacing-3;
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border-light;
    border-radius: brand.$radius-base;
  }

  &__time {
    flex-shrink: 0;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-tertiary;
    font-variant-numeric: tabular-nums;
    width: 48px;
  }

  &__cell {
    flex: 1;
    padding: brand.$spacing-2 brand.$spacing-3;
    background: brand.$color-bg-strong;
    border: 1px solid transparent;
    border-radius: brand.$radius-sm;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
    cursor: pointer;
    text-align: start;
    transition: background 0.15s;
    min-height: 36px;

    &.is-base-available {
      background: brand.$brand-primary;
      color: brand.$color-text-inverse;
      font-weight: 700;
    }

    &.is-exception-closed {
      background: rgba(255, 77, 79, 0.12);
      color: brand.$color-error;
      font-weight: 600;
      border-inline-start: 3px solid brand.$color-error;
    }

    &.is-exception-extra {
      background: rgba(82, 196, 26, 0.16);
      color: brand.$color-success;
      font-weight: 600;
      border-inline-start: 3px solid brand.$color-success;
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &.is-exception-mode {
    .week-mobile__cell {
      &.is-base-available {
        background: brand.$brand-primary-soft;
        color: brand.$brand-primary-deep;
      }

      &.is-exception-closed {
        background: rgba(255, 77, 79, 0.12);
        color: brand.$color-error;
      }

      &.is-exception-extra {
        background: rgba(82, 196, 26, 0.16);
        color: brand.$color-success;
      }
    }
  }

  &__ex-badge {
    margin-inline-start: brand.$spacing-2;
    font-size: brand.$font-size-xs;
    opacity: 0.7;
  }
}
</style>
