<script setup>
/**
 * WithdrawalView — 提现记录(M6 T5 / spec §5.5)
 *
 * PC 表格 / H5 卡片(响应式同源)
 * 5 状态徽章:pending(琥珀) / approved(蓝) / paid(绿) / rejected(灰) / failed(红)
 *
 * 详情抽屉:
 *   - 时间线(申请 / 审核 / 打款 / 失败)
 *   - 驳回原因 / 失败原因
 *   - failed 时显示「重新申请」按钮 → 跳 /teacher-center/withdrawal/apply
 *   - 收款信息后 4 位脱敏 + hint「完整信息已加密保存」
 *
 * 顶部 CTA「申请提现」— 走 income 页判定逻辑(pending 时 disable)
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ElButton, ElDrawer, ElSelect, ElOption, ElPagination,
  ElTable, ElTableColumn, ElTimeline, ElTimelineItem,
  ElMessage
} from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import { useBreakpoint } from '@/composables/useBreakpoint'
import { fromUTC, getUserTimezone } from '@/utils/datetime'
import { listWithdrawals, getWithdrawal } from '@/api/teacher-center/withdrawal'

const { t, locale } = useI18n()
const router = useRouter()
const { isPC } = useBreakpoint(768)

// ---------- 列表 ----------
const list = ref([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)
const loading = ref(true)
const errorMsg = ref('')

const filterStatus = ref('')

const STATUS_OPTIONS = computed(() => [
  { value: '', label: t('teacherCenter.withdrawal.list.filterStatusAll') },
  { value: 'pending', label: t('teacherCenter.withdrawal.list.statusPending') },
  { value: 'approved', label: t('teacherCenter.withdrawal.list.statusApproved') },
  { value: 'paid', label: t('teacherCenter.withdrawal.list.statusPaid') },
  { value: 'rejected', label: t('teacherCenter.withdrawal.list.statusRejected') },
  { value: 'failed', label: t('teacherCenter.withdrawal.list.statusFailed') }
])

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

const statusLabelMap = computed(() => ({
  pending: t('teacherCenter.withdrawal.list.statusPending'),
  approved: t('teacherCenter.withdrawal.list.statusApproved'),
  paid: t('teacherCenter.withdrawal.list.statusPaid'),
  rejected: t('teacherCenter.withdrawal.list.statusRejected'),
  failed: t('teacherCenter.withdrawal.list.statusFailed')
}))

const methodLabelMap = computed(() => ({
  wechat: t('teacherCenter.withdrawal.apply.payeeMethodWechat'),
  alipay: t('teacherCenter.withdrawal.apply.payeeMethodAlipay'),
  paypal: t('teacherCenter.withdrawal.apply.payeeMethodPaypal'),
  bank_card: t('teacherCenter.withdrawal.apply.payeeMethodBankCard'),
  other: t('teacherCenter.withdrawal.apply.payeeMethodOther')
}))

function statusClass(status) {
  return `is-${status}`
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const page = await listWithdrawals({
      status: filterStatus.value || undefined,
      pageNo: pageNo.value,
      pageSize: pageSize.value
    })
    list.value = page?.list || []
    total.value = page?.total || 0
  } catch (e) {
    errorMsg.value = e?.message || t('teacherCenter.withdrawal.list.loadFailed')
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onStatusChange() {
  pageNo.value = 1
  load()
}

function onPageChange(p) {
  pageNo.value = p
  load()
}

// ---------- 详情抽屉 ----------
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailItem = ref(null)

async function openDetail(item) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    // 拉单条最新详情(状态可能已变,列表是缓存)
    detailItem.value = await getWithdrawal(item.id)
  } catch (e) {
    // 拉不到时降级用列表里的
    detailItem.value = item
    ElMessage.warning(e?.message || t('teacherCenter.withdrawal.list.loadFailed'))
  } finally {
    detailLoading.value = false
  }
}

function onReapply() {
  // 跳申请页(payeeInfo / amount 都不预填,完全重新输入 — 符合安全约定)
  detailVisible.value = false
  router.push('/teacher-center/withdrawal/apply')
}

// ---------- 顶部「申请提现」CTA ----------
const applyDisabled = computed(() => {
  // 简化判定:列表已加载,顶部有 pending/approved 任一,则不让重复申请(后端也会拒)
  return list.value.some((it) => it.status === 'pending' || it.status === 'approved')
})

function onClickApply() {
  if (applyDisabled.value) {
    ElMessage.warning(t('teacherCenter.errors.withdrawalPending'))
    return
  }
  router.push('/teacher-center/withdrawal/apply')
}

onMounted(() => load())
</script>

<template>
  <div class="tc-withdraw">
    <header class="tc-withdraw__header">
      <div>
        <h1 class="tc-withdraw__title">{{ t('teacherCenter.withdrawal.list.title') }}</h1>
        <p class="tc-withdraw__subtitle">{{ t('teacherCenter.withdrawal.list.subtitle') }}</p>
      </div>
      <el-button type="primary" @click="onClickApply">
        {{ t('teacherCenter.withdrawal.list.applyBtn') }}
      </el-button>
    </header>

    <!-- 筛选 -->
    <div class="tc-withdraw__filters">
      <el-select
        v-model="filterStatus"
        :placeholder="t('teacherCenter.withdrawal.list.filterStatus')"
        style="width: 180px"
        @change="onStatusChange"
      >
        <el-option
          v-for="opt in STATUS_OPTIONS"
          :key="opt.value"
          :value="opt.value"
          :label="opt.label"
        />
      </el-select>
    </div>

    <!-- loading / error / empty / list -->
    <div v-if="loading" class="tc-withdraw__state">
      {{ t('common.loading', { defaultValue: '加载中…' }) }}
    </div>

    <div v-else-if="errorMsg" class="tc-withdraw__state tc-withdraw__state--error">
      <p>{{ errorMsg }}</p>
      <el-button size="small" @click="load">
        {{ t('common.retry', { defaultValue: '重试' }) }}
      </el-button>
    </div>

    <EmptyState
      v-else-if="!list.length"
      variant="no-data"
      :title="t('teacherCenter.withdrawal.list.empty')"
      :description="''"
      :action-text="t('teacherCenter.withdrawal.list.applyBtn')"
      action-link="/teacher-center/withdrawal/apply"
    />

    <template v-else>
      <!-- PC:表格 -->
      <el-table
        v-if="isPC"
        :data="list"
        stripe
        class="tc-withdraw__table"
      >
        <el-table-column
          :label="t('teacherCenter.withdrawal.list.column.appliedAt')"
          min-width="160"
        >
          <template #default="{ row }">{{ formatUTC(row.appliedAt) }}</template>
        </el-table-column>

        <el-table-column
          :label="t('teacherCenter.withdrawal.list.column.amount')"
          width="120"
          align="right"
        >
          <template #default="{ row }">
            <strong>US$ {{ Number(row.amount).toFixed(2) }}</strong>
          </template>
        </el-table-column>

        <el-table-column
          :label="t('teacherCenter.withdrawal.list.column.method')"
          width="120"
        >
          <template #default="{ row }">{{ methodLabelMap[row.payeeMethod] || row.payeeMethod }}</template>
        </el-table-column>

        <el-table-column
          :label="t('teacherCenter.withdrawal.list.column.payeeMasked')"
          min-width="160"
        >
          <template #default="{ row }">
            <code class="tc-withdraw__masked">{{ row.payeeInfoMasked }}</code>
          </template>
        </el-table-column>

        <el-table-column
          :label="t('teacherCenter.withdrawal.list.column.status')"
          width="140"
        >
          <template #default="{ row }">
            <span class="tc-withdraw__badge" :class="statusClass(row.status)">
              {{ statusLabelMap[row.status] || row.status }}
            </span>
          </template>
        </el-table-column>

        <el-table-column
          :label="t('teacherCenter.withdrawal.list.column.action')"
          width="100"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button link size="small" @click="openDetail(row)">
              {{ t('teacherCenter.withdrawal.list.viewDetailBtn') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- H5:卡片 -->
      <ul v-else class="tc-withdraw__cards">
        <li
          v-for="item in list"
          :key="item.id"
          class="tc-withdraw__card"
          @click="openDetail(item)"
        >
          <div class="tc-withdraw__card-head">
            <span class="tc-withdraw__badge" :class="statusClass(item.status)">
              {{ statusLabelMap[item.status] || item.status }}
            </span>
            <strong class="tc-withdraw__card-amount">
              US$ {{ Number(item.amount).toFixed(2) }}
            </strong>
          </div>
          <div class="tc-withdraw__card-row">
            <span class="tc-withdraw__card-label">{{ t('teacherCenter.withdrawal.list.column.method') }}</span>
            <span>{{ methodLabelMap[item.payeeMethod] || item.payeeMethod }}</span>
          </div>
          <div class="tc-withdraw__card-row">
            <span class="tc-withdraw__card-label">{{ t('teacherCenter.withdrawal.list.column.payeeMasked') }}</span>
            <code class="tc-withdraw__masked">{{ item.payeeInfoMasked }}</code>
          </div>
          <div class="tc-withdraw__card-row">
            <span class="tc-withdraw__card-label">{{ t('teacherCenter.withdrawal.list.column.appliedAt') }}</span>
            <span>{{ formatUTC(item.appliedAt) }}</span>
          </div>
        </li>
      </ul>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="tc-withdraw__pagination">
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

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      :title="t('teacherCenter.withdrawal.list.detailTitle')"
      direction="rtl"
      size="420px"
    >
      <div v-if="detailLoading" class="tc-withdraw__state">
        {{ t('common.loading', { defaultValue: '加载中…' }) }}
      </div>

      <div v-else-if="detailItem" class="tc-withdraw__detail">
        <!-- 摘要 -->
        <div class="tc-withdraw__detail-summary">
          <span class="tc-withdraw__badge" :class="statusClass(detailItem.status)">
            {{ statusLabelMap[detailItem.status] || detailItem.status }}
          </span>
          <h3 class="tc-withdraw__detail-amount">
            US$ {{ Number(detailItem.amount).toFixed(2) }}
          </h3>
          <p class="tc-withdraw__detail-method">
            {{ methodLabelMap[detailItem.payeeMethod] || detailItem.payeeMethod }} ·
            <code class="tc-withdraw__masked">{{ detailItem.payeeInfoMasked }}</code>
          </p>
          <p class="tc-withdraw__payee-hint">
            ⓘ {{ t('teacherCenter.withdrawal.list.payeeMaskedHint') }}
          </p>
        </div>

        <!-- 时间线 -->
        <h4 class="tc-withdraw__detail-section">{{ t('teacherCenter.withdrawal.list.timelineTitle') }}</h4>
        <el-timeline>
          <el-timeline-item
            v-if="detailItem.appliedAt"
            :timestamp="formatUTC(detailItem.appliedAt)"
            type="primary"
          >
            {{ t('teacherCenter.withdrawal.list.timelineApplied') }}
          </el-timeline-item>

          <el-timeline-item
            v-if="detailItem.auditedAt && detailItem.status !== 'rejected'"
            :timestamp="formatUTC(detailItem.auditedAt)"
            type="success"
          >
            {{ t('teacherCenter.withdrawal.list.timelineApproved') }}
          </el-timeline-item>

          <el-timeline-item
            v-if="detailItem.auditedAt && detailItem.status === 'rejected'"
            :timestamp="formatUTC(detailItem.auditedAt)"
            type="danger"
          >
            {{ t('teacherCenter.withdrawal.list.timelineRejected') }}
            <p v-if="detailItem.rejectReason" class="tc-withdraw__detail-reason">
              <strong>{{ t('teacherCenter.withdrawal.list.rejectReasonLabel') }}:</strong>
              {{ detailItem.rejectReason }}
            </p>
          </el-timeline-item>

          <el-timeline-item
            v-if="detailItem.paidAt && detailItem.status === 'paid'"
            :timestamp="formatUTC(detailItem.paidAt)"
            type="success"
          >
            {{ t('teacherCenter.withdrawal.list.timelinePaid') }}
            <p v-if="detailItem.paidRemark" class="tc-withdraw__detail-reason">
              <strong>{{ t('teacherCenter.withdrawal.list.paidRemarkLabel') }}:</strong>
              {{ detailItem.paidRemark }}
            </p>
          </el-timeline-item>

          <el-timeline-item
            v-if="detailItem.status === 'failed'"
            :timestamp="formatUTC(detailItem.paidAt || detailItem.auditedAt)"
            type="danger"
          >
            {{ t('teacherCenter.withdrawal.list.timelineFailed') }}
            <p v-if="detailItem.paidRemark" class="tc-withdraw__detail-reason">
              <strong>{{ t('teacherCenter.withdrawal.list.failReasonLabel') }}:</strong>
              {{ detailItem.paidRemark }}
            </p>
          </el-timeline-item>
        </el-timeline>

        <!-- failed → 重新申请 -->
        <div v-if="detailItem.status === 'failed'" class="tc-withdraw__detail-actions">
          <el-button type="primary" @click="onReapply">
            {{ t('teacherCenter.withdrawal.list.reapplyBtn') }}
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.tc-withdraw {
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: brand.$spacing-3;
    margin-block-end: brand.$spacing-4;

    @media (max-width: brand.$bp-tablet) {
      flex-direction: column;
      align-items: stretch;
    }
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

  &__filters {
    margin-block-end: brand.$spacing-4;
  }

  &__state {
    text-align: center;
    padding: brand.$spacing-8 brand.$spacing-4;
    color: brand.$color-text-secondary;

    &--error {
      color: brand.$color-error;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: brand.$spacing-3;
    }
  }

  &__table {
    margin-block-end: brand.$spacing-4;
  }

  &__masked {
    font-family: brand.$font-family-mono;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-secondary;
    background: brand.$color-bg-strong;
    padding: 1px brand.$spacing-2;
    border-radius: brand.$radius-sm;
  }

  &__badge {
    display: inline-block;
    padding: 2px brand.$spacing-2;
    border-radius: brand.$radius-sm;
    font-size: brand.$font-size-xs;
    font-weight: 600;

    &.is-pending {
      background: rgba(250, 173, 20, 0.12);
      color: brand.$color-warning;
    }

    &.is-approved {
      background: rgba(24, 144, 255, 0.12);
      color: brand.$color-info;
    }

    &.is-paid {
      background: rgba(82, 196, 26, 0.12);
      color: brand.$color-success;
    }

    &.is-rejected {
      background: brand.$color-bg-strong;
      color: brand.$color-text-tertiary;
    }

    &.is-failed {
      background: rgba(255, 77, 79, 0.12);
      color: brand.$color-error;
    }
  }

  &__cards {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
  }

  &__card {
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    padding: brand.$spacing-4;
    cursor: pointer;
    box-shadow: brand.$shadow-sm;
    transition: box-shadow 0.15s;

    &:hover {
      box-shadow: brand.$shadow-md;
    }
  }

  &__card-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-block-end: brand.$spacing-3;
  }

  &__card-amount {
    font-size: brand.$font-size-lg;
    color: brand.$color-text-primary;
    font-variant-numeric: tabular-nums;
  }

  &__card-row {
    display: flex;
    justify-content: space-between;
    gap: brand.$spacing-3;
    font-size: brand.$font-size-sm;
    padding: brand.$spacing-1 0;
  }

  &__card-label {
    color: brand.$color-text-tertiary;
  }

  &__pagination {
    display: flex;
    justify-content: center;
    margin-block-start: brand.$spacing-4;
  }

  &__detail {
    padding: 0 brand.$spacing-3;
  }

  &__detail-summary {
    background: brand.$color-bg-page;
    border-radius: brand.$radius-base;
    padding: brand.$spacing-4;
    margin-block-end: brand.$spacing-6;
    text-align: center;
  }

  &__detail-amount {
    margin: brand.$spacing-2 0;
    font-size: brand.$font-size-2xl;
    color: brand.$color-text-primary;
    font-variant-numeric: tabular-nums;
  }

  &__detail-method {
    margin: 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
  }

  &__payee-hint {
    margin: brand.$spacing-2 0 0;
    color: brand.$color-text-tertiary;
    font-size: brand.$font-size-xs;
  }

  &__detail-section {
    margin: 0 0 brand.$spacing-3;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__detail-reason {
    margin: brand.$spacing-1 0 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
  }

  &__detail-actions {
    margin-block-start: brand.$spacing-6;
    display: flex;
    justify-content: center;
  }
}
</style>
