<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs, { getUserTimezone, fromUTC } from '@/utils/datetime'
import { listTeacherSchedules } from '@/api/teacher'
import { listTeacherScheduleExceptions } from '@/api/booking'

const props = defineProps({
  teacherId: { type: [String, Number], required: true },
  slotMinutes: { type: Number, default: 30 },
  daysAhead: { type: Number, default: 7 }
})

const emit = defineEmits(['select', 'availability-change'])

const { t, locale } = useI18n()

const loading = ref(true)
const errorMsg = ref('')
const weekly = ref([])
const exceptions = ref([])
const selectedKey = ref('')

const studentTz = computed(() => getUserTimezone())
const teacherTz = computed(() => weekly.value[0]?.timezone || studentTz.value)

const days = computed(() => buildDays({
  weekly: weekly.value,
  exceptions: exceptions.value,
  studentTz: studentTz.value,
  teacherTz: teacherTz.value,
  slotMinutes: props.slotMinutes,
  daysAhead: props.daysAhead
}))

const totalSlots = computed(() => days.value.reduce((acc, d) => acc + d.slots.length, 0))

onMounted(load)

watch(() => props.teacherId, load)
watch(
  [loading, errorMsg, totalSlots],
  () => {
    if (loading.value || errorMsg.value) return
    emit('availability-change', { totalSlots: totalSlots.value })
  },
  { immediate: true }
)

async function load() {
  loading.value = true
  errorMsg.value = ''
  selectedKey.value = ''
  try {
    const today = dayjs().format('YYYY-MM-DD')
    const to = dayjs().add(props.daysAhead + 1, 'day').format('YYYY-MM-DD')
    const [w, ex] = await Promise.all([
      listTeacherSchedules(props.teacherId),
      listTeacherScheduleExceptions(props.teacherId, today, to)
    ])
    weekly.value = (w || []).map(normalizeScheduleRow)
    exceptions.value = (ex || []).map(normalizeScheduleRow)
  } catch (e) {
    errorMsg.value = e?.message || t('common.error')
  } finally {
    loading.value = false
  }
}

// Spring Boot Jackson 默认把 LocalTime 序列化成 [hh, mm] / [hh, mm, ss] 数组。
// 这里统一回 "HH:mm:ss",避免 dayjs.tz parse 失败。
function normalizeScheduleRow(row) {
  return {
    ...row,
    startTime: toHmsString(row.startTime),
    endTime: toHmsString(row.endTime)
  }
}

function toHmsString(value) {
  if (value == null) return null
  if (Array.isArray(value)) {
    const [h = 0, m = 0, s = 0] = value
    return `${pad(h)}:${pad(m)}:${pad(s)}`
  }
  return value
}

function pad(n) {
  return String(n).padStart(2, '0')
}

function buildDays({ weekly, exceptions, studentTz, teacherTz, slotMinutes, daysAhead }) {
  const out = []
  if (!weekly || weekly.length === 0) {
    for (let i = 0; i < daysAhead; i++) {
      const studentDay = dayjs().tz(studentTz).add(i, 'day').format('YYYY-MM-DD')
      out.push({ dateKey: studentDay, slots: [] })
    }
    return out
  }

  const allSlotsUtc = []
  const horizon = daysAhead + 2
  const now = dayjs()

  for (let d = 0; d < horizon; d++) {
    const teacherDay = dayjs().tz(teacherTz).add(d, 'day').format('YYYY-MM-DD')
    const wholeDayClosed = exceptions.some(
      (e) =>
        normalizeDate(e.exceptionDate) === teacherDay &&
        e.type === 'closed' &&
        !e.startTime
    )
    if (wholeDayClosed) continue
    const wd = dayjs.tz(teacherDay, teacherTz).day()
    const partialClosed = exceptions.filter(
      (e) =>
        normalizeDate(e.exceptionDate) === teacherDay &&
        e.type === 'closed' &&
        e.startTime &&
        e.endTime
    )
    const matching = weekly.filter((s) => s.weekday === wd)
    for (const ws of matching) {
      pushRange(allSlotsUtc, ws.startTime, ws.endTime, teacherDay, ws.timezone || teacherTz, slotMinutes, partialClosed, now)
    }
  }

  for (const ex of exceptions) {
    if (ex.type === 'extra' && ex.startTime && ex.endTime) {
      const exDate = normalizeDate(ex.exceptionDate)
      pushRange(allSlotsUtc, ex.startTime, ex.endTime, exDate, ex.timezone || teacherTz, slotMinutes, [], now)
    }
  }

  const byDay = new Map()
  for (const s of allSlotsUtc) {
    const k = fromUTC(s.startUtc, studentTz).format('YYYY-MM-DD')
    if (!byDay.has(k)) byDay.set(k, [])
    byDay.get(k).push(s)
  }
  for (const arr of byDay.values()) arr.sort((a, b) => a.startUtc.localeCompare(b.startUtc))

  for (let i = 0; i < daysAhead; i++) {
    const studentDay = dayjs().tz(studentTz).add(i, 'day').format('YYYY-MM-DD')
    out.push({ dateKey: studentDay, slots: byDay.get(studentDay) || [] })
  }
  return out
}

function pushRange(out, startTime, endTime, dateStr, tz, durationMin, partials, now) {
  const dayStart = dayjs.tz(`${dateStr} ${startTime}`, tz)
  const dayEnd = dayjs.tz(`${dateStr} ${endTime}`, tz)
  let cursor = dayStart
  while (cursor.add(durationMin, 'minute').valueOf() <= dayEnd.valueOf()) {
    const slotStart = cursor
    const slotEnd = cursor.add(durationMin, 'minute')
    if (slotStart.isAfter(now)) {
      const overlap = partials.some((p) => {
        const ps = dayjs.tz(`${dateStr} ${p.startTime}`, tz)
        const pe = dayjs.tz(`${dateStr} ${p.endTime}`, tz)
        return slotStart.isBefore(pe) && slotEnd.isAfter(ps)
      })
      if (!overlap) {
        out.push({
          startUtc: slotStart.utc().toISOString(),
          endUtc: slotEnd.utc().toISOString()
        })
      }
    }
    cursor = slotEnd
  }
}

function normalizeDate(value) {
  if (!value) return ''
  if (typeof value === 'string') return value.slice(0, 10)
  return dayjs(value).format('YYYY-MM-DD')
}

function dateLabel(dateKey, idx) {
  const d = dayjs.tz(dateKey, studentTz.value)
  const wdKey = `booking.picker.weekday.${d.day()}`
  const wd = t(wdKey)
  const md = d.format(localeDateFormat(locale.value))
  const todayBadge = idx === 0 ? `(${t('booking.picker.today')})` : ''
  return { wd, md, todayBadge, isToday: idx === 0 }
}

function localeDateFormat(loc) {
  if (loc === 'en') return 'MMM D'
  if (loc === 'ar') return 'D MMM'
  return 'M月D日'
}

function timeLabel(startUtc) {
  return fromUTC(startUtc, studentTz.value).format('HH:mm')
}

function slotKey(dayKey, slot) {
  return `${dayKey}__${slot.startUtc}`
}

function onSelect(dayKey, slot) {
  const key = slotKey(dayKey, slot)
  selectedKey.value = key
  const startLocal = fromUTC(slot.startUtc, studentTz.value)
  const endLocal = fromUTC(slot.endUtc, studentTz.value)
  emit('select', {
    startUtc: slot.startUtc,
    endUtc: slot.endUtc,
    dateLabel: startLocal.format(longDateFormat(locale.value)),
    timeLabel: `${startLocal.format('HH:mm')} - ${endLocal.format('HH:mm')}`
  })
}

function longDateFormat(loc) {
  if (loc === 'en') return 'ddd, MMM D'
  if (loc === 'ar') return 'ddd D MMM'
  return 'M月D日 ddd'
}

defineExpose({ reload: load })
</script>

<template>
  <div class="ssp">
    <div v-if="loading" class="ssp__state">{{ t('common.loading') }}</div>
    <div v-else-if="errorMsg" class="ssp__state ssp__state--error">{{ errorMsg }}</div>

    <template v-else>
      <p class="ssp__hint">
        {{ t('booking.picker.tzHint', { tz: studentTz }) }}
      </p>

      <div v-if="totalSlots === 0" class="ssp__empty">
        {{ t('booking.picker.empty') }}
      </div>

      <div v-else class="ssp__grid">
        <div v-for="(d, idx) in days" :key="d.dateKey" class="ssp__col" :class="{ 'ssp__col--today': idx === 0 }">
          <header class="ssp__col-head">
            <span class="ssp__col-wd">{{ dateLabel(d.dateKey, idx).wd }}</span>
            <span class="ssp__col-date">{{ dateLabel(d.dateKey, idx).md }}</span>
            <span v-if="idx === 0" class="ssp__col-today-tag">{{ t('booking.picker.today') }}</span>
          </header>
          <div class="ssp__col-slots">
            <button
              v-for="slot in d.slots"
              :key="slotKey(d.dateKey, slot)"
              type="button"
              class="ssp__slot"
              :class="{ 'ssp__slot--active': selectedKey === slotKey(d.dateKey, slot) }"
              @click="onSelect(d.dateKey, slot)"
            >
              {{ timeLabel(slot.startUtc) }}
            </button>
            <span v-if="d.slots.length === 0" class="ssp__col-empty">—</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.ssp {
  width: 100%;
}

.ssp__hint {
  margin: 0 0 brand.$spacing-3;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-tertiary;
}

.ssp__state {
  text-align: center;
  padding: brand.$spacing-8 0;
  color: brand.$color-text-secondary;

  &--error {
    color: brand.$color-error;
  }
}

.ssp__empty {
  text-align: center;
  padding: brand.$spacing-8 0;
  color: brand.$color-text-tertiary;
  font-size: brand.$font-size-sm;
  background: brand.$color-bg-strong;
  border-radius: brand.$radius-base;
}

.ssp__grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: brand.$spacing-2;

  @media (max-width: 767px) {
    grid-auto-flow: column;
    grid-template-columns: repeat(7, 110px);
    grid-auto-rows: auto;
    overflow-x: auto;
    padding-bottom: brand.$spacing-2;
    -webkit-overflow-scrolling: touch;
  }
}

.ssp__col {
  background: brand.$color-bg-card;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-base;
  padding: brand.$spacing-3 brand.$spacing-2;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;

  &--today {
    border-color: brand.$brand-primary;
    background: brand.$brand-primary-soft;
  }
}

.ssp__col-head {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding-bottom: brand.$spacing-2;
  border-bottom: 1px solid brand.$color-divider;
}

.ssp__col-wd {
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  font-weight: 500;
}

.ssp__col-date {
  font-size: brand.$font-size-base;
  color: brand.$color-text-primary;
  font-weight: 600;
}

.ssp__col-today-tag {
  font-size: brand.$font-size-xs;
  color: brand.$brand-primary-deep;
}

.ssp__col-slots {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
}

.ssp__slot {
  appearance: none;
  border: 1px solid brand.$color-border;
  background: brand.$color-bg-card;
  border-radius: brand.$radius-base;
  padding: brand.$spacing-2 brand.$spacing-3;
  font: inherit;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-primary;
  cursor: pointer;
  text-align: center;
  transition: background 0.15s, border-color 0.15s, color 0.15s, transform 0.05s;

  &:hover {
    background: brand.$brand-primary-soft;
    border-color: brand.$brand-primary;
    color: brand.$brand-primary-deep;
  }

  &:active {
    transform: scale(0.98);
  }

  &--active {
    background: brand.$brand-primary;
    border-color: brand.$brand-primary;
    color: brand.$color-text-inverse;

    &:hover {
      background: brand.$brand-primary-deep;
      border-color: brand.$brand-primary-deep;
      color: brand.$color-text-inverse;
    }
  }
}

.ssp__col-empty {
  text-align: center;
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
  padding: brand.$spacing-2 0;
}
</style>
