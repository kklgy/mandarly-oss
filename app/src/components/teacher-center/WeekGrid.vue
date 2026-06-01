<script setup>
/**
 * WeekGrid — 教师排课 PC 周视图(33 行 × 7 列 30-min 网格)
 *
 * 设计源:
 *   - docs/product/M6-spec.md §5.1 T1 排课 PC ≥ 600px
 *   - docs/frontend/ui-style-guide-v1.md(token / RTL)
 *
 * 网格:
 *   - 列:Mon-Sun(spec §5.1 — 国内习惯周一开头;数据 weekday 0=Sun..6=Sat)
 *   - 行:07:00 - 23:00 共 33 个 30-min 段(7:00, 7:30, ..., 22:30, 23:00 = 33 行)
 *   - 单元格:点击 = 切换可约;长按 / 右键 = 触发例外抽屉
 *
 * 状态色:
 *   - 可约模板:主色实块 + 白色 ✓ icon(Google Calendar / Calendly pattern)
 *   - 不可约:白底
 *   - 本周例外(closed/extra)叠加左边框红/绿
 *
 * 防御:
 *   - 模板是周期性的,过去日期的格子点击也合法(改的是下一周及之后的模板),不做 disable
 *   - 父组件传入 `weekData`(slots 集合) / `exceptions`(本周例外)
 *
 * Props:
 *   - weeklySlots:Set<string> 形如 "1-09:00"(weekday-HH:MM)
 *   - exceptions: Array<{ id, exceptionDate, type, startTime, endTime }>
 *   - weekStart: dayjs YYYY-MM-DD 周一(教师本地时区)
 *   - timezone: 教师本地时区(IANA)
 *
 * Emits:
 *   - toggle({ dayOfWeek, hh, mm, available }) — 点击格子
 *   - open-exception-drawer({ date, hh, mm }) — 长按 / 右键单格
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from '@/utils/datetime'

const props = defineProps({
  /** Set of "weekday-HH:MM" strings,如 "1-09:00" 表示周一 09:00 这个 30-min 可约 */
  weeklySlots: {
    type: Object, // Set
    required: true
  },
  /** 本周例外列表(每条含 exceptionDate / startTime / endTime / type) */
  exceptions: {
    type: Array,
    default: () => []
  },
  /** 本周周一日期(YYYY-MM-DD,教师本地时区) */
  weekStart: {
    type: String,
    required: true
  },
  /** 教师本地时区(IANA) */
  timezone: {
    type: String,
    default: ''
  },
  editMode: {
    type: String,
    default: 'template',
    validator: (value) => ['template', 'exception'].includes(value)
  },
  startHour: {
    type: Number,
    default: 7
  },
  endHour: {
    type: Number,
    default: 24
  },
  /** 操作中(toggle 调用)— 全网格 disable 防止并发误点 */
  saving: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle', 'open-exception-drawer'])

const { t } = useI18n()

// ---------- 列:周一→周日(weekday 0=Sun..6=Sat;UI 显示 1..6,0 放最后) ----------
// UI weekday 序列:1=Mon, 2=Tue, ..., 6=Sat, 0=Sun
const WEEKDAY_ORDER = [1, 2, 3, 4, 5, 6, 0]

const dayHeaders = computed(() =>
  WEEKDAY_ORDER.map((wd, idx) => {
    const date = dayjs(props.weekStart).add(idx, 'day')
    return {
      weekday: wd,
      label: t(`teacherCenter.schedule.mobileDayDot`, { day: ['日', '一', '二', '三', '四', '五', '六'][wd] }),
      dateLabel: date.format('M-D'),
      dateYmd: date.format('YYYY-MM-DD'),
      isToday: date.isSame(dayjs(), 'day')
    }
  })
)

// ---------- 行:07:00 - 23:00 共 33 个 30-min 段 ----------
const ALL_ROWS = (() => {
  const rows = []
  for (let h = 7; h <= 23; h++) {
    rows.push({ hh: h, mm: 0, label: `${String(h).padStart(2, '0')}:00` })
    if (h < 23) {
      rows.push({ hh: h, mm: 30, label: `${String(h).padStart(2, '0')}:30` })
    }
  }
  return rows
})()

const visibleRows = computed(() =>
  ALL_ROWS.filter((row) => {
    const minute = row.hh * 60 + row.mm
    return minute >= props.startHour * 60 && minute < props.endHour * 60
  })
)

// ---------- 例外索引:dateYmd-HH:MM → { type, id } ----------
const exceptionMap = computed(() => {
  const map = new Map()
  for (const ex of props.exceptions || []) {
    if (!ex.exceptionDate || !ex.startTime) continue
    // exceptionDate 后端返回 LocalDate("YYYY-MM-DD" 字符串)/ LocalTime("HH:mm:ss")
    const dateYmd = typeof ex.exceptionDate === 'string'
      ? ex.exceptionDate.slice(0, 10)
      : dayjs(ex.exceptionDate).format('YYYY-MM-DD')
    const startHHMM = String(ex.startTime).slice(0, 5)
    map.set(`${dateYmd}-${startHHMM}`, { type: ex.type, id: ex.id })
    // 区间型例外(end - start > 30min)展开
    if (ex.endTime) {
      const endHHMM = String(ex.endTime).slice(0, 5)
      const [sH, sM] = startHHMM.split(':').map(Number)
      const [eH, eM] = endHHMM.split(':').map(Number)
      let curM = sH * 60 + sM
      const endM = eH * 60 + eM
      while (curM + 30 <= endM) {
        const h = Math.floor(curM / 60)
        const m = curM % 60
        const k = `${dateYmd}-${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
        if (!map.has(k)) map.set(k, { type: ex.type, id: ex.id })
        curM += 30
      }
    }
  }
  return map
})

// ---------- 单元格状态计算 ----------
function cellStatus(weekday, hh, mm, dateYmd) {
  const hhmm = `${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  const baseAvailable = props.weeklySlots.has(`${weekday}-${hhmm}`)
  const ex = exceptionMap.value.get(`${dateYmd}-${hhmm}`)
  return {
    baseAvailable,
    isException: !!ex,
    exceptionType: ex?.type || null,
    // 实际可约 = 模板可约 XOR 例外
    effectiveAvailable: ex
      ? (ex.type === 'extra' ? true : false)
      : baseAvailable
  }
}

// ---------- 事件 ----------
let longPressTimer = null
let longPressFired = false

function onCellMouseDown(weekday, hh, mm, dateYmd) {
  if (props.saving) return
  longPressFired = false
  longPressTimer = window.setTimeout(() => {
    longPressFired = true
    emit('open-exception-drawer', { date: dateYmd, hh, mm })
  }, 600)
}

function onCellMouseUp() {
  if (longPressTimer) {
    window.clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

function onCellMouseLeave() {
  onCellMouseUp()
}

function onCellClick(weekday, hh, mm, dateYmd) {
  if (props.saving) return
  if (longPressFired) {
    longPressFired = false
    return
  }
  if (props.editMode === 'exception') {
    emit('open-exception-drawer', { date: dateYmd, hh, mm })
    return
  }
  const hhmm = `${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  const baseAvailable = props.weeklySlots.has(`${weekday}-${hhmm}`)
  emit('toggle', {
    dayOfWeek: weekday,
    hh,
    mm,
    available: !baseAvailable
  })
}

function onCellContextMenu(e, weekday, hh, mm, dateYmd) {
  e.preventDefault()
  if (props.saving) return
  emit('open-exception-drawer', { date: dateYmd, hh, mm })
}
</script>

<template>
  <div
    class="week-grid"
    :class="{ 'is-exception-mode': editMode === 'exception' }"
    :aria-busy="saving"
  >
    <!-- 表头(列):时间 + 7 天 -->
    <div class="week-grid__head">
      <div class="week-grid__corner" aria-hidden="true"></div>
      <div
        v-for="day in dayHeaders"
        :key="day.weekday"
        class="week-grid__day-head"
        :class="{ 'is-today': day.isToday }"
      >
        <span class="week-grid__day-name">{{ day.label }}</span>
        <span class="week-grid__day-date">{{ day.dateLabel }}</span>
      </div>
    </div>

    <!-- 网格主体 -->
    <div class="week-grid__body">
      <template v-for="row in visibleRows" :key="`${row.hh}-${row.mm}`">
        <!-- 时间标签列 -->
        <div class="week-grid__time-label">{{ row.label }}</div>

        <!-- 7 个格子 -->
        <button
          v-for="day in dayHeaders"
          :key="`${day.weekday}-${row.hh}-${row.mm}`"
          type="button"
          class="week-grid__cell"
          :class="{
            'is-base-available': cellStatus(day.weekday, row.hh, row.mm, day.dateYmd).baseAvailable,
            'is-effective-available': cellStatus(day.weekday, row.hh, row.mm, day.dateYmd).effectiveAvailable,
            'is-exception': cellStatus(day.weekday, row.hh, row.mm, day.dateYmd).isException,
            'is-exception-closed': cellStatus(day.weekday, row.hh, row.mm, day.dateYmd).exceptionType === 'closed',
            'is-exception-extra': cellStatus(day.weekday, row.hh, row.mm, day.dateYmd).exceptionType === 'extra'
          }"
          :disabled="saving"
          :aria-label="`${day.label} ${row.label}`"
          @click="onCellClick(day.weekday, row.hh, row.mm, day.dateYmd)"
          @mousedown="onCellMouseDown(day.weekday, row.hh, row.mm, day.dateYmd)"
          @mouseup="onCellMouseUp"
          @mouseleave="onCellMouseLeave"
          @contextmenu="(e) => onCellContextMenu(e, day.weekday, row.hh, row.mm, day.dateYmd)"
        ></button>
      </template>
    </div>

    <!-- 图例 -->
    <div class="week-grid__legend">
      <span class="week-grid__legend-item">
        <span class="week-grid__legend-swatch is-base-available"></span>
        {{ t('teacherCenter.schedule.legendWeeklyAvailable') }}
      </span>
      <span class="week-grid__legend-item">
        <span class="week-grid__legend-swatch is-exception-closed"></span>
        {{ t('teacherCenter.schedule.legendExceptionClosed') }}
      </span>
      <span class="week-grid__legend-item">
        <span class="week-grid__legend-swatch is-exception-extra"></span>
        {{ t('teacherCenter.schedule.legendExceptionExtra') }}
      </span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.week-grid {
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-lg;
  overflow: hidden;
  user-select: none;

  &__head {
    display: grid;
    grid-template-columns: 56px repeat(7, 1fr);
    background: brand.$color-bg-strong;
    border-block-end: 1px solid brand.$color-border;
  }

  &__corner {
    border-inline-end: 1px solid brand.$color-border;
  }

  &__day-head {
    padding: brand.$spacing-2 brand.$spacing-1;
    text-align: center;
    border-inline-end: 1px solid brand.$color-border;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;

    &:last-child {
      border-inline-end: none;
    }

    &.is-today {
      background: brand.$brand-primary-soft;
      color: brand.$brand-primary-deep;
      font-weight: 600;
    }
  }

  &__day-name {
    font-weight: 500;
  }

  &__day-date {
    font-size: brand.$font-size-xs;
  }

  &__body {
    display: grid;
    grid-template-columns: 56px repeat(7, 1fr);
  }

  &__time-label {
    padding: brand.$spacing-1 brand.$spacing-2;
    font-size: brand.$font-size-xs;
    color: brand.$color-text-tertiary;
    text-align: end;
    border-inline-end: 1px solid brand.$color-border;
    border-block-end: 1px solid brand.$color-border-light;
    background: brand.$color-bg-strong;
    line-height: 24px;
  }

  &__cell {
    height: 26px;
    background: brand.$color-bg-card;
    border: none;
    border-inline-end: 1px solid brand.$color-border-light;
    border-block-end: 1px solid brand.$color-border-light;
    cursor: pointer;
    transition: background 0.1s;
    padding: 0;

    &:last-child {
      border-inline-end: none;
    }

    &:hover:not(:disabled) {
      background: brand.$color-bg-page;
    }

    &.is-base-available {
      background: brand.$brand-primary;
      position: relative;

      &::after {
        content: '✓';
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        color: brand.$color-text-inverse;
        font-size: brand.$font-size-sm;
        font-weight: 700;
        line-height: 1;
      }

      &:hover:not(:disabled) {
        background: brand.$brand-primary-deep;
      }
    }

    &.is-exception {
      position: relative;
    }

    &.is-exception-closed {
      &::before {
        content: '';
        position: absolute;
        inset-inline-start: 0;
        inset-block: 0;
        width: 3px;
        background: brand.$color-error;
      }
    }

    &.is-exception-extra {
      &::before {
        content: '';
        position: absolute;
        inset-inline-start: 0;
        inset-block: 0;
        width: 3px;
        background: brand.$color-success;
      }
    }

    &:not(.is-base-available).is-exception-closed {
      background: rgba(255, 77, 79, 0.12);
    }

    &:not(.is-base-available).is-exception-extra {
      background: rgba(82, 196, 26, 0.16);
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.6;
    }
  }

  &.is-exception-mode {
    .week-grid__cell {
      &.is-base-available {
        background: brand.$brand-primary-soft;

        &::after {
          color: brand.$brand-primary-deep;
        }
      }

      &.is-exception-closed {
        background: rgba(255, 77, 79, 0.12);

        &::after {
          content: none;
        }
      }

      &.is-exception-extra {
        background: rgba(82, 196, 26, 0.16);

        &::after {
          content: '✓';
          color: brand.$color-success;
        }
      }
    }
  }

  &__legend {
    display: flex;
    gap: brand.$spacing-4;
    padding: brand.$spacing-3 brand.$spacing-4;
    border-block-start: 1px solid brand.$color-border-light;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
  }

  &__legend-item {
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-2;
  }

  &__legend-swatch {
    display: inline-block;
    width: 16px;
    height: 16px;
    border-radius: brand.$radius-sm;

    &.is-base-available {
      background: brand.$brand-primary;
      position: relative;

      &::after {
        content: '✓';
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        color: brand.$color-text-inverse;
        font-size: 11px;
        font-weight: 700;
        line-height: 1;
      }
    }

    &.is-exception-closed {
      background: rgba(255, 77, 79, 0.12);
      border-inline-start: 3px solid brand.$color-error;
    }

    &.is-exception-extra {
      background: rgba(82, 196, 26, 0.16);
      border-inline-start: 3px solid brand.$color-success;
    }
  }
}
</style>
