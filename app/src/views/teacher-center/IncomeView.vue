<script setup>
/**
 * IncomeView — 教师收入(M6 T3 / spec §5.3)
 *
 * 顶部 4 卡片(spec §5.3):
 *   - 累计入账 totalEarnedUsd
 *   - 可提现 availableUsd(带「申请提现 →」按钮 — variant primary)
 *   - 在途 T+7 frozenT7Usd(hint 显示「近期上课入账,T+7 自动解冻」)
 *   - 申请中冻结 pendingWithdrawUsd
 *
 * 中部图表(P1)— 本次先 placeholder,后置
 *
 * 底部明细分页:
 *   筛选 — 开始/结束日期 + 收入类型 select
 *   列 — 课程时间 / 学生(courseOrderId) / 类型 / 课时费 / 状态 / 解冻时间
 *
 * 「申请提现」CTA — pendingWithdrawUsd > 0 时 disable + tooltip
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ElButton, ElPagination, ElTable, ElTableColumn,
  ElDatePicker, ElSelect, ElOption, ElTooltip, ElMessage
} from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import IncomeStatCard from '@/components/teacher-center/IncomeStatCard.vue'
import dayjs from '@/utils/datetime'
import { fromUTC, getUserTimezone, toUTC } from '@/utils/datetime'
import { getTeacherBalance, listTeacherIncome } from '@/api/teacher-center/income'

const { t, locale } = useI18n()
const router = useRouter()

// ---------- 余额 ----------
const balance = ref({
  availableUsd: 0,
  frozenT7Usd: 0,
  pendingWithdrawUsd: 0,
  totalEarnedUsd: 0,
  totalWithdrawnUsd: 0,
  currency: 'USD'
})
const balanceLoading = ref(true)

async function loadBalance() {
  balanceLoading.value = true
  try {
    const data = await getTeacherBalance()
    balance.value = {
      availableUsd: data?.availableUsd ?? 0,
      frozenT7Usd: data?.frozenT7Usd ?? 0,
      pendingWithdrawUsd: data?.pendingWithdrawUsd ?? 0,
      totalEarnedUsd: data?.totalEarnedUsd ?? 0,
      totalWithdrawnUsd: data?.totalWithdrawnUsd ?? 0,
      currency: data?.currency || 'USD'
    }
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.income.loadFailed'))
  } finally {
    balanceLoading.value = false
  }
}

// 「申请提现」按钮态
const canApplyWithdraw = computed(() => {
  return Number(balance.value.availableUsd) > 0 && Number(balance.value.pendingWithdrawUsd) === 0
})

const withdrawDisabledReason = computed(() => {
  if (Number(balance.value.pendingWithdrawUsd) > 0) {
    return t('teacherCenter.errors.withdrawalPending')
  }
  if (Number(balance.value.availableUsd) <= 0) {
    return t('teacherCenter.errors.balanceInsufficient')
  }
  return ''
})

function onApplyWithdraw() {
  if (!canApplyWithdraw.value) return
  router.push('/teacher-center/withdrawal/apply')
}

// ---------- 明细列表 ----------
const list = ref([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)
const listLoading = ref(true)

const filterDateRange = ref([])
const filterType = ref('')

const tz = computed(() => getUserTimezone())

const dateFmt = computed(() => {
  if (locale.value === 'en') return 'MMM D, YYYY HH:mm'
  if (locale.value === 'ar') return 'D MMM YYYY HH:mm'
  return 'YYYY-MM-DD HH:mm'
})

function formatUTC(utcStr) {
  if (!utcStr) return ''
  return fromUTC(utcStr, tz.value).format(dateFmt.value)
}

const typeOptions = computed(() => [
  { value: '', label: t('teacherCenter.income.filterTypeAll') },
  { value: 'normal', label: t('teacherCenter.income.filterTypeClass') },
  { value: 'free_trial', label: 'Free trial' },
  { value: 'no_show_teacher', label: 'No-show (teacher)' },
  { value: 'no_show_student', label: 'No-show (student)' },
  { value: 'refund_deduct', label: 'Refund deduct' },
  { value: 'manual_adjust', label: t('teacherCenter.income.filterTypeAdjust') }
])

const statusLabelMap = computed(() => ({
  frozen: t('teacherCenter.income.statusFrozen'),
  available: t('teacherCenter.income.statusAvailable'),
  reverted: t('teacherCenter.income.statusWithdrawn')
}))

function statusClass(status) {
  return {
    frozen: 'is-frozen',
    available: 'is-available',
    reverted: 'is-reverted'
  }[status] || ''
}

function typeLabel(type) {
  const found = typeOptions.value.find((opt) => opt.value === type)
  return found ? found.label : type
}

async function loadList() {
  listLoading.value = true
  try {
    const [from, to] = filterDateRange.value || []
    const page = await listTeacherIncome({
      from: from ? toUTC(dayjs(from).startOf('day').toISOString(), tz.value) : undefined,
      to: to ? toUTC(dayjs(to).endOf('day').toISOString(), tz.value) : undefined,
      type: filterType.value || undefined,
      pageNo: pageNo.value,
      pageSize: pageSize.value
    })
    list.value = page?.list || []
    total.value = page?.total || 0
  } catch (e) {
    ElMessage.error(e?.message || t('teacherCenter.income.loadFailed'))
    list.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

function onFilterChange() {
  pageNo.value = 1
  loadList()
}

function onPageChange(p) {
  pageNo.value = p
  loadList()
}

onMounted(() => {
  loadBalance()
  loadList()
})

// 「最近解冻 {date}」hint — 取 list 中 status=frozen 最早 frozenUntil(若已加载)
const nextUnfreezeHint = computed(() => {
  const frozen = list.value
    .filter((it) => it.status === 'frozen' && it.frozenUntil)
    .sort((a, b) => dayjs(a.frozenUntil).valueOf() - dayjs(b.frozenUntil).valueOf())
  if (frozen.length === 0) return ''
  return t('teacherCenter.income.nextUnfreezeLabel', {
    date: fromUTC(frozen[0].frozenUntil, tz.value).format('YYYY-MM-DD')
  })
})
</script>

<template>
  <div class="tc-income">
    <header class="tc-income__header">
      <h1 class="tc-income__title">{{ t('teacherCenter.income.title') }}</h1>
      <p class="tc-income__subtitle">{{ t('teacherCenter.income.subtitle') }}</p>
    </header>

    <!-- ===== 4 卡片 ===== -->
    <section v-if="!balanceLoading" class="tc-income__stats" aria-label="balance">
      <IncomeStatCard
        :label="t('teacherCenter.income.totalEarnedLabel')"
        :amount="balance.totalEarnedUsd"
        variant="default"
      />

      <IncomeStatCard
        :label="t('teacherCenter.income.availableLabel')"
        :amount="balance.availableUsd"
        :hint="t('teacherCenter.income.availableHint')"
        variant="primary"
      >
        <template #action>
          <el-tooltip
            v-if="!canApplyWithdraw && withdrawDisabledReason"
            :content="withdrawDisabledReason"
            placement="top"
          >
            <el-button
              size="small"
              class="tc-income__withdraw-btn"
              disabled
            >
              {{ t('teacherCenter.income.applyWithdrawalBtn') }}
            </el-button>
          </el-tooltip>
          <el-button
            v-else
            size="small"
            class="tc-income__withdraw-btn"
            @click="onApplyWithdraw"
          >
            {{ t('teacherCenter.income.applyWithdrawalBtn') }}
          </el-button>
        </template>
      </IncomeStatCard>

      <IncomeStatCard
        :label="t('teacherCenter.income.frozenLabel')"
        :amount="balance.frozenT7Usd"
        :hint="nextUnfreezeHint || t('teacherCenter.income.frozenHint')"
        variant="warning"
      />

      <IncomeStatCard
        :label="t('teacherCenter.income.pendingWithdrawalLabel')"
        :amount="balance.pendingWithdrawUsd"
        :hint="t('teacherCenter.income.pendingWithdrawalHint')"
        variant="info"
      />
    </section>

    <div v-else class="tc-income__state">
      {{ t('common.loading', { defaultValue: '加载中…' }) }}
    </div>

    <!-- ===== 明细表 ===== -->
    <section class="tc-income__detail">
      <header class="tc-income__detail-head">
        <h2 class="tc-income__detail-title">{{ t('teacherCenter.income.detailTitle') }}</h2>

        <div class="tc-income__filters">
          <el-date-picker
            v-model="filterDateRange"
            type="daterange"
            :range-separator="locale === 'en' ? 'to' : '至'"
            :start-placeholder="t('teacherCenter.income.filterFrom')"
            :end-placeholder="t('teacherCenter.income.filterTo')"
            value-format="YYYY-MM-DD"
            size="default"
            @change="onFilterChange"
          />
          <el-select
            v-model="filterType"
            :placeholder="t('teacherCenter.income.filterType')"
            size="default"
            style="width: 160px"
            @change="onFilterChange"
          >
            <el-option
              v-for="opt in typeOptions"
              :key="opt.value"
              :value="opt.value"
              :label="opt.label"
            />
          </el-select>
        </div>
      </header>

      <div v-if="listLoading" class="tc-income__state">
        {{ t('common.loading', { defaultValue: '加载中…' }) }}
      </div>

      <EmptyState
        v-else-if="!list.length"
        variant="no-data"
        :title="t('teacherCenter.income.empty')"
        description=""
        compact
      />

      <template v-else>
        <!-- PC 表格 -->
        <el-table :data="list" stripe class="tc-income__table">
          <el-table-column
            :label="t('teacherCenter.income.column.classTime')"
            min-width="160"
          >
            <template #default="{ row }">
              {{ formatUTC(row.settledAt) }}
            </template>
          </el-table-column>

          <el-table-column
            :label="t('teacherCenter.income.column.type')"
            width="140"
          >
            <template #default="{ row }">{{ typeLabel(row.type) }}</template>
          </el-table-column>

          <el-table-column
            :label="t('teacherCenter.income.column.fee')"
            width="120"
            align="right"
          >
            <template #default="{ row }">
              <strong>US$ {{ Number(row.amountUsd).toFixed(2) }}</strong>
            </template>
          </el-table-column>

          <el-table-column
            :label="t('teacherCenter.income.column.status')"
            width="100"
          >
            <template #default="{ row }">
              <span class="tc-income__status" :class="statusClass(row.status)">
                {{ statusLabelMap[row.status] || row.status }}
              </span>
            </template>
          </el-table-column>

          <el-table-column
            :label="t('teacherCenter.income.column.unfreezeAt')"
            min-width="160"
          >
            <template #default="{ row }">
              <span v-if="row.status === 'frozen'">
                {{ formatUTC(row.frozenUntil) }}
              </span>
              <span v-else class="tc-income__muted">—</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="tc-income__pagination">
          <el-pagination
            background
            layout="prev, pager, next, total"
            :total="total"
            :page-size="pageSize"
            :current-page="pageNo"
            @current-change="onPageChange"
          />
        </div>
      </template>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.tc-income {
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }

  &__header {
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

  &__stats {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: brand.$spacing-3;
    margin-block-end: brand.$spacing-6;

    @media (min-width: brand.$bp-laptop) {
      grid-template-columns: repeat(4, 1fr);
      gap: brand.$spacing-4;
    }
  }

  &__withdraw-btn {
    background: brand.$color-bg-card;
    color: brand.$brand-primary-deep;
    border-color: brand.$color-bg-card;
    font-weight: 600;

    &:hover:not(:disabled) {
      background: brand.$brand-primary-soft;
      color: brand.$brand-primary-deep;
      border-color: brand.$brand-primary-soft;
    }
  }

  &__state {
    text-align: center;
    padding: brand.$spacing-8 brand.$spacing-4;
    color: brand.$color-text-secondary;
  }

  &__detail {
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    padding: brand.$spacing-4;

    @media (min-width: brand.$bp-tablet) {
      padding: brand.$spacing-6;
    }
  }

  &__detail-head {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    gap: brand.$spacing-3;
    margin-block-end: brand.$spacing-4;
  }

  &__detail-title {
    margin: 0;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__filters {
    display: flex;
    gap: brand.$spacing-2;
    flex-wrap: wrap;
  }

  &__table {
    margin-block-end: brand.$spacing-3;
  }

  &__status {
    padding: 2px brand.$spacing-2;
    border-radius: brand.$radius-sm;
    font-size: brand.$font-size-xs;
    font-weight: 600;

    &.is-frozen {
      background: rgba(250, 173, 20, 0.12);
      color: brand.$color-warning;
    }

    &.is-available {
      background: rgba(82, 196, 26, 0.12);
      color: brand.$color-success;
    }

    &.is-reverted {
      background: brand.$color-bg-strong;
      color: brand.$color-text-tertiary;
    }
  }

  &__muted {
    color: brand.$color-text-tertiary;
  }

  &__pagination {
    display: flex;
    justify-content: center;
    margin-block-start: brand.$spacing-4;
  }
}
</style>
