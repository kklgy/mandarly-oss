<script setup>
// =============================================================================
// ReferralView.vue — /profile/referral
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § /profile/referral 详细规格
//
// 内容:
//   §A 3 数字卡(已邀请 / 已奖励 / 累计奖励)
//   §B 邀请链接 + 复制 + 分享(WhatsApp / LinkedIn / X)
//   §C 邀请记录 list(最近 20 条)
//
// API 调用(主 agent 后续在 api/payment.js 加方法):
//   - GET /app-api/edu/referral/me     已存在 → getMyReferralStats()
//                返 { referralCode, refereeCount, rewardedCount, totalRewardPackages }
//                需后端补 totalRewardAmount(累计奖励金额)
//   - GET /app-api/edu/referral/my-referees?pageSize=20  待加
//                返 { list: [{ nickname, registeredAt, status }] }
// =============================================================================
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Promotion, CopyDocument } from '@element-plus/icons-vue'
import { getMyReferralStats } from '@/api/payment'
import StatCard from '@/components/StatCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const errorMsg = ref('')
const stats = ref(null) // { referralCode, refereeCount, rewardedCount, totalRewardPackages, totalRewardAmount? }

const recordsLoading = ref(false)
const records = ref([]) // 邀请记录

const inviteLink = computed(() => {
  if (!stats.value?.referralCode) return ''
  // SITE_ORIGIN — 浏览器端读 location.origin
  const origin = typeof window !== 'undefined' ? window.location.origin : 'http://localhost:3001'
  return `${origin}/register?ref=${stats.value.referralCode}`
})

const totalReward = computed(() => {
  // 优先后端 totalRewardAmount(待补);否则用 totalRewardPackages * 单价占位
  // 当前用 rewardedCount 作为"奖励课次数"展示(P0)
  return stats.value?.rewardedCount || 0
})

onMounted(async () => {
  await loadStats()
  // 邀请记录 — API 待后端加,先 try/catch 静默
  loadRecords()
})

async function loadStats() {
  loading.value = true
  errorMsg.value = ''
  try {
    stats.value = await getMyReferralStats()
  } catch {
    errorMsg.value = t('common.error')
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    // 主 agent 后续在 api/payment.js 加 getMyReferees:
    //   GET /app-api/edu/referral/my-referees?pageSize=20
    //   返 { list: [{ nickname, registeredAt(ISO), status: 'rewarded' | 'pending' }] }
    // 当前 API 未就绪 — 静默置空,UI 显示 EmptyState
    records.value = []
  } catch {
    records.value = []
  } finally {
    recordsLoading.value = false
  }
}

async function copyToClipboard(text, successKey = 'profile.referral.copied') {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t(successKey))
  } catch {
    ElMessage.info(text)
  }
}

function copyCode() {
  copyToClipboard(stats.value?.referralCode, 'profile.referral.codeCopied')
}

function copyLink() {
  copyToClipboard(inviteLink.value, 'profile.referral.linkCopied')
}

// 分享
function shareWhatsApp() {
  const text = t('profile.referral.shareText', { link: inviteLink.value })
  const url = `https://wa.me/?text=${encodeURIComponent(text)}`
  window.open(url, '_blank', 'noopener,noreferrer')
}

function shareLinkedIn() {
  const url = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(inviteLink.value)}`
  window.open(url, '_blank', 'noopener,noreferrer')
}

function shareX() {
  const text = t('profile.referral.shareText', { link: inviteLink.value })
  const url = `https://twitter.com/intent/tweet?text=${encodeURIComponent(text)}`
  window.open(url, '_blank', 'noopener,noreferrer')
}

function formatDate(iso) {
  if (!iso) return ''
  try {
    return new Date(iso).toLocaleDateString()
  } catch {
    return iso
  }
}

function statusLabel(status) {
  const map = {
    rewarded: t('profile.referral.status.rewarded'),
    pending: t('profile.referral.status.pending')
  }
  return map[status] || status
}

function handleBack() {
  router.push('/profile')
}
</script>

<template>
  <div class="referral-view" v-loading="loading">
    <!-- H5 子页返回(PC 不显示) -->
    <header class="referral-view__topbar">
      <button class="referral-view__back" type="button" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ t('profile.menu.referral') }}</span>
      </button>
    </header>

    <!-- 错误态 -->
    <div v-if="errorMsg" class="referral-view__error">
      <EmptyState
        variant="error"
        :title="t('common.networkError.title')"
        :description="errorMsg"
      >
        <template #action>
          <el-button type="primary" @click="loadStats">{{ t('common.retry') }}</el-button>
        </template>
      </EmptyState>
    </div>

    <template v-else>
      <!-- §A 3 数字卡 -->
      <section class="referral-view__section">
        <h2 class="referral-view__heading">{{ t('profile.referral.title') }}</h2>
        <div class="referral-view__stats">
          <StatCard
            color="primary"
            :value="stats?.refereeCount ?? 0"
            :label="t('profile.referral.stat.invited')"
          />
          <StatCard
            color="primary"
            :value="stats?.rewardedCount ?? 0"
            :label="t('profile.referral.stat.rewarded')"
          />
          <StatCard
            color="primary"
            :value="totalReward"
            suffix="节"
            :label="t('profile.referral.stat.totalReward')"
          />
        </div>
      </section>

      <!-- §B 邀请链接 + 分享 -->
      <section v-if="stats?.referralCode" class="referral-view__section">
        <h2 class="referral-view__heading">{{ t('profile.referral.linkLabel') }}</h2>

        <!-- 推荐码 -->
        <div class="referral-view__code-row">
          <span class="referral-view__code-label">{{ t('profile.referral.codeLabel') }}</span>
          <code class="referral-view__code">{{ stats.referralCode }}</code>
          <el-button size="small" @click="copyCode">
            <el-icon><CopyDocument /></el-icon>
            <span class="referral-view__btn-text">{{ t('profile.referral.copy') }}</span>
          </el-button>
        </div>

        <!-- 邀请链接 -->
        <div class="referral-view__link-row">
          <input
            class="referral-view__link-input"
            :value="inviteLink"
            readonly
            dir="ltr"
            :aria-label="t('profile.referral.linkLabel')"
          />
          <el-button type="primary" @click="copyLink">
            {{ t('profile.referral.copy') }}
          </el-button>
        </div>

        <!-- 分享 -->
        <div class="referral-view__share-row">
          <span class="referral-view__share-label">{{ t('profile.referral.shareLabel') }}</span>
          <el-button @click="shareWhatsApp">WhatsApp</el-button>
          <el-button @click="shareLinkedIn">LinkedIn</el-button>
          <el-button @click="shareX">X</el-button>
        </div>
      </section>

      <!-- §C 邀请记录 -->
      <section class="referral-view__section">
        <h2 class="referral-view__heading">{{ t('profile.referral.recordsTitle') }}</h2>

        <div v-if="recordsLoading" class="referral-view__records-loading">
          {{ t('common.loading') }}
        </div>

        <EmptyState
          v-else-if="records.length === 0"
          variant="no-data"
          :icon="Promotion"
          :title="t('profile.referral.empty.title')"
          :description="t('profile.referral.empty.desc')"
          compact
        >
          <template #action>
            <el-button type="primary" @click="copyLink">
              {{ t('profile.referral.empty.action') }}
            </el-button>
          </template>
        </EmptyState>

        <ul v-else class="referral-view__records">
          <li
            v-for="(r, idx) in records"
            :key="idx"
            class="referral-view__record"
          >
            <div class="referral-view__record-info">
              <span class="referral-view__record-name" dir="auto">{{ r.nickname }}</span>
              <span class="referral-view__record-date">
                {{ formatDate(r.registeredAt) }}
              </span>
            </div>
            <span
              class="referral-view__record-status"
              :class="`referral-view__record-status--${r.status}`"
            >
              {{ statusLabel(r.status) }}
            </span>
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">

.referral-view {
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }
}

.referral-view__topbar {
  margin-block-end: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    display: none;
  }
}

.referral-view__back {
  background: transparent;
  border: none;
  color: brand.$brand-primary-deep;
  font: inherit;
  font-size: brand.$font-size-md;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: brand.$spacing-2;

  &:hover { color: brand.$brand-primary; }
}

.referral-view__error {
  padding: brand.$spacing-6 0;
}

.referral-view__section {
  background: brand.$color-bg-card;
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6;
  margin-block-end: brand.$spacing-5;
  box-shadow: brand.$shadow-base;
}

.referral-view__heading {
  margin: 0 0 brand.$spacing-5;
  font-size: brand.$font-size-lg;
  font-weight: 600;
  color: brand.$color-text-primary;
}

// 3 数字卡 grid
.referral-view__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: brand.$spacing-3;

  @media (max-width: brand.$bp-mobile) {
    grid-template-columns: 1fr;
  }
}

// 推荐码行
.referral-view__code-row {
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;
  flex-wrap: wrap;
  margin-block-end: brand.$spacing-4;
}

.referral-view__code-label {
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
}

.referral-view__code {
  font-family: brand.$font-family-mono;
  background: brand.$brand-primary-soft;
  color: brand.$brand-primary-deep;
  padding: brand.$spacing-2 brand.$spacing-4;
  border-radius: brand.$radius-base;
  font-size: brand.$font-size-md;
  font-weight: 600;
  letter-spacing: 0.06em;
}

// 链接行
.referral-view__link-row {
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;
  margin-block-end: brand.$spacing-4;
}

.referral-view__link-input {
  flex: 1;
  min-width: 0;
  padding: brand.$spacing-2 brand.$spacing-3;
  border: 1px solid brand.$color-border;
  border-radius: brand.$radius-base;
  font: inherit;
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  background: brand.$color-bg-strong;

  &:focus {
    outline: none;
    border-color: brand.$brand-primary;
  }
}

// 分享行
.referral-view__share-row {
  display: flex;
  align-items: center;
  gap: brand.$spacing-2;
  flex-wrap: wrap;
}

.referral-view__share-label {
  font-size: brand.$font-size-sm;
  color: brand.$color-text-secondary;
  margin-inline-end: brand.$spacing-2;
}

.referral-view__btn-text {
  margin-inline-start: brand.$spacing-1;
}

// 邀请记录
.referral-view__records {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
}

.referral-view__records-loading {
  padding: brand.$spacing-4;
  text-align: center;
  color: brand.$color-text-tertiary;
  font-size: brand.$font-size-sm;
}

.referral-view__record {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: brand.$spacing-3;
  padding: brand.$spacing-3 0;
  border-block-end: 1px solid brand.$color-border-light;

  &:last-child {
    border-block-end: none;
  }
}

.referral-view__record-info {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-1;
  flex: 1;
  min-width: 0;
}

.referral-view__record-name {
  font-size: brand.$font-size-base;
  font-weight: 500;
  color: brand.$color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.referral-view__record-date {
  font-size: brand.$font-size-xs;
  color: brand.$color-text-tertiary;
}

.referral-view__record-status {
  font-size: brand.$font-size-xs;
  font-weight: 500;
  padding: brand.$spacing-1 brand.$spacing-3;
  border-radius: brand.$radius-pill;

  &--rewarded {
    background: rgba(82, 196, 26, 0.12);
    color: brand.$color-success;
  }

  &--pending {
    background: rgba(250, 173, 20, 0.12);
    color: brand.$color-warning;
  }
}
</style>
