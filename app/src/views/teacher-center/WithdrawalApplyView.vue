<script setup>
/**
 * WithdrawalApplyView — 申请提现(M6 T4 / spec §5.4)
 *
 * ⭐ 安全敏感细节(spec §5.4):
 *   1. 不预填历史 payee_info(只读余额,不读 last apply)
 *   2. payeeInfo 不放 store / localStorage(只活在本组件 ref)
 *   3. 提交按钮 disable 直到:勾选确认 + 金额合法 + payee 非空 + 未提交中
 *   4. 提交成功后 immediately payeeInfo.value = '',跳 /teacher-center/withdrawal
 *   5. onBeforeUnmount 兜底清空(用户中途离开页面)
 *   6. 前端不缓存 payee 任何内容(刷新 + 离开 + 返回 都要重新输入)
 *
 * 数据流:
 *   - 进入页:拉 balance → 显示余额 + 设置 max
 *   - 提交:applyWithdrawal({ amount, payeeMethod, payeeInfo }) → 成功跳 list
 *   - 后端校验:Redisson 锁 + balance 事务 + min/max 二次校验
 *
 * 错误码映射:
 *   - balanceInsufficient / withdrawalPending / withdrawalBelowMin / withdrawalAboveBalance
 */
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElButton, ElCheckbox } from 'element-plus'
import { getTeacherBalance } from '@/api/teacher-center/income'
import { applyWithdrawal } from '@/api/teacher-center/withdrawal'
import PayeeMethodPicker from '@/components/teacher-center/PayeeMethodPicker.vue'
import AmountInput from '@/components/teacher-center/AmountInput.vue'

const { t } = useI18n()
const router = useRouter()

// ---------- 余额 ----------
const balance = ref({ availableUsd: 0, pendingWithdrawUsd: 0, currency: 'USD' })
const balanceLoading = ref(true)
const balanceError = ref('')

// 最低提现金额(后端默认 100 USD;前端兜底,后端会再校验)
const MIN_WITHDRAW_AMOUNT = 100

async function loadBalance() {
  balanceLoading.value = true
  balanceError.value = ''
  try {
    const data = await getTeacherBalance()
    balance.value = {
      availableUsd: Number(data?.availableUsd ?? 0),
      pendingWithdrawUsd: Number(data?.pendingWithdrawUsd ?? 0),
      currency: data?.currency || 'USD'
    }
    // pending > 0:已有进行中,直接跳列表 + toast
    if (balance.value.pendingWithdrawUsd > 0) {
      ElMessage.warning(t('teacherCenter.errors.withdrawalPending'))
      router.replace('/teacher-center/withdrawal')
    }
  } catch (e) {
    balanceError.value = e?.message || t('teacherCenter.income.loadFailed')
  } finally {
    balanceLoading.value = false
  }
}

// ---------- 表单(payeeInfo 只活在组件 ref 中) ----------
const amount = ref('')
const payeeMethod = ref('')
const payeeInfo = ref('')
const bankPayeeInfo = ref({
  bankName: '',
  cardNo: '',
  holderName: ''
})
const confirmed = ref(false)
const submitting = ref(false)
const amountValid = ref(false)

// payeeInfo hint / placeholder 按 method 切换
const payeePlaceholder = computed(() => {
  const map = {
    wechat: 'teacherCenter.withdrawal.apply.payeeInfoPlaceholderWechat',
    alipay: 'teacherCenter.withdrawal.apply.payeeInfoPlaceholderAlipay',
    paypal: 'teacherCenter.withdrawal.apply.payeeInfoPlaceholderPaypal',
    bank_card: 'teacherCenter.withdrawal.apply.payeeInfoPlaceholderBankCard',
    other: 'teacherCenter.withdrawal.apply.payeeInfoPlaceholderOther'
  }
  return payeeMethod.value ? t(map[payeeMethod.value]) : ''
})

const payeeHint = computed(() => {
  const map = {
    wechat: 'teacherCenter.withdrawal.apply.payeeInfoHintWechat',
    alipay: 'teacherCenter.withdrawal.apply.payeeInfoHintAlipay',
    paypal: 'teacherCenter.withdrawal.apply.payeeInfoHintPaypal',
    bank_card: 'teacherCenter.withdrawal.apply.payeeInfoHintBankCard',
    other: 'teacherCenter.withdrawal.apply.payeeInfoHintOther'
  }
  return payeeMethod.value ? t(map[payeeMethod.value]) : ''
})

const isBankCard = computed(() => payeeMethod.value === 'bank_card')

const bankPayeeComplete = computed(() => {
  return (
    bankPayeeInfo.value.bankName.trim().length > 0 &&
    bankPayeeInfo.value.cardNo.trim().length > 0 &&
    bankPayeeInfo.value.holderName.trim().length > 0
  )
})

const normalizedPayeeInfo = computed(() => {
  if (!isBankCard.value) {
    return payeeInfo.value.trim()
  }
  return [
    `${t('teacherCenter.withdrawal.apply.bankNameLabel')}:${bankPayeeInfo.value.bankName.trim()}`,
    `${t('teacherCenter.withdrawal.apply.bankCardNoLabel')}:${bankPayeeInfo.value.cardNo.trim()}`,
    `${t('teacherCenter.withdrawal.apply.bankHolderNameLabel')}:${bankPayeeInfo.value.holderName.trim()}`
  ].join('\n')
})

// 提交按钮 disable 条件(严格 4 条 — spec §5.4)
const canSubmit = computed(() => {
  return (
    !submitting.value &&
    amountValid.value &&
    payeeMethod.value &&
    (isBankCard.value ? bankPayeeComplete.value : payeeInfo.value.trim().length > 0) &&
    confirmed.value
  )
})

function clearPayeeInfo() {
  payeeInfo.value = ''
  bankPayeeInfo.value = {
    bankName: '',
    cardNo: '',
    holderName: ''
  }
}

watch(payeeMethod, () => {
  clearPayeeInfo()
  confirmed.value = false
})

function onAmountValidate({ valid }) {
  amountValid.value = valid
}

// ---------- 提交 ----------
async function onSubmit() {
  if (!canSubmit.value) return
  submitting.value = true
  // 临时变量后立刻清空内存的 payee(防御 — 若网络/异常打断,内存里不留残留)
  const payload = {
    amount: Number(amount.value),
    payeeMethod: payeeMethod.value,
    payeeInfo: normalizedPayeeInfo.value
  }
  // ↓ 注意:这里不能在 await 前清空 payeeInfo,否则后端报错重试场景内存里就没数据了
  // 提交后再清(成功 or 失败都清,失败时让用户重新输入)
  try {
    await applyWithdrawal(payload)
    // 成功 — 立即清空,跳列表
    clearPayeeInfo()
    amount.value = ''
    payeeMethod.value = ''
    confirmed.value = false
    ElMessage.success(t('teacherCenter.withdrawal.apply.submitSuccess'))
    router.replace('/teacher-center/withdrawal')
  } catch (e) {
    // 失败 — 清空 payeeInfo + confirmed(强制用户重输,安全 baseline)
    clearPayeeInfo()
    confirmed.value = false
    const code = e?.code || e?.payload?.code
    let msg = e?.message || t('teacherCenter.withdrawal.apply.submitFailed')
    // 后端业务错误码可能透传
    if (code === 'EDU_BALANCE_INSUFFICIENT') {
      msg = t('teacherCenter.errors.balanceInsufficient')
    } else if (code === 'EDU_WITHDRAWAL_PENDING_EXISTS') {
      msg = t('teacherCenter.errors.withdrawalPending')
    } else if (code === 'EDU_WITHDRAWAL_BELOW_MIN') {
      msg = t('teacherCenter.errors.withdrawalBelowMin', { min: MIN_WITHDRAW_AMOUNT })
    } else if (code === 'EDU_WITHDRAWAL_ABOVE_BALANCE') {
      msg = t('teacherCenter.errors.withdrawalAboveBalance')
    }
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}

function onCancel() {
  // 取消时清空敏感字段
  clearPayeeInfo()
  amount.value = ''
  confirmed.value = false
  router.back()
}

// ---------- 生命周期 ----------
onMounted(() => loadBalance())

// 离开页面兜底清空(防御:即便 setup 内 ref 已被 GC,显式清空一次更稳)
onBeforeUnmount(() => {
  clearPayeeInfo()
  amount.value = ''
})
</script>

<template>
  <div class="tc-withdraw-apply">
    <header class="tc-withdraw-apply__header">
      <h1 class="tc-withdraw-apply__title">{{ t('teacherCenter.withdrawal.apply.title') }}</h1>
      <p class="tc-withdraw-apply__subtitle">{{ t('teacherCenter.withdrawal.apply.subtitle') }}</p>
    </header>

    <div v-if="balanceLoading" class="tc-withdraw-apply__state">
      {{ t('common.loading', { defaultValue: '加载中…' }) }}
    </div>

    <div v-else-if="balanceError" class="tc-withdraw-apply__state tc-withdraw-apply__state--error">
      <p>{{ balanceError }}</p>
      <el-button size="small" @click="loadBalance">
        {{ t('common.retry', { defaultValue: '重试' }) }}
      </el-button>
    </div>

    <form v-else class="tc-withdraw-apply__form" @submit.prevent="onSubmit">
      <!-- 余额提示 -->
      <div class="tc-withdraw-apply__balance">
        <span class="tc-withdraw-apply__balance-label">{{ t('teacherCenter.withdrawal.apply.balanceLabel') }}</span>
        <strong class="tc-withdraw-apply__balance-value">
          US$ {{ balance.availableUsd.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}
        </strong>
      </div>

      <!-- 提现金额 -->
      <div class="tc-withdraw-apply__field">
        <label class="tc-withdraw-apply__label">{{ t('teacherCenter.withdrawal.apply.amountLabel') }}</label>
        <AmountInput
          v-model="amount"
          :min="MIN_WITHDRAW_AMOUNT"
          :max="balance.availableUsd"
          :placeholder="t('teacherCenter.withdrawal.apply.amountPlaceholder')"
          :disabled="submitting"
          :unit="balance.currency"
          @validate="onAmountValidate"
        />
        <p class="tc-withdraw-apply__hint">
          {{ t('teacherCenter.withdrawal.apply.amountHint', { min: MIN_WITHDRAW_AMOUNT }) }}
        </p>
      </div>

      <!-- 收款方式 -->
      <div class="tc-withdraw-apply__field">
        <label class="tc-withdraw-apply__label">{{ t('teacherCenter.withdrawal.apply.payeeMethodLabel') }}</label>
        <PayeeMethodPicker v-model="payeeMethod" :disabled="submitting" />
      </div>

      <!-- 收款信息(method 选了才显示) -->
      <div v-if="payeeMethod" class="tc-withdraw-apply__field">
        <label class="tc-withdraw-apply__label">{{ t('teacherCenter.withdrawal.apply.payeeInfoLabel') }}</label>
        <div v-if="isBankCard" class="tc-withdraw-apply__bank-fields">
          <label class="tc-withdraw-apply__bank-row">
            <span class="tc-withdraw-apply__bank-label">
              {{ t('teacherCenter.withdrawal.apply.bankNameLabel') }}
            </span>
            <input
              v-model="bankPayeeInfo.bankName"
              class="tc-withdraw-apply__input"
              type="text"
              :placeholder="t('teacherCenter.withdrawal.apply.bankNamePlaceholder')"
              :disabled="submitting"
              autocomplete="off"
              autocorrect="off"
              autocapitalize="off"
              spellcheck="false"
            />
          </label>
          <label class="tc-withdraw-apply__bank-row">
            <span class="tc-withdraw-apply__bank-label">
              {{ t('teacherCenter.withdrawal.apply.bankCardNoLabel') }}
            </span>
            <input
              v-model="bankPayeeInfo.cardNo"
              class="tc-withdraw-apply__input"
              type="text"
              inputmode="numeric"
              :placeholder="t('teacherCenter.withdrawal.apply.bankCardNoPlaceholder')"
              :disabled="submitting"
              autocomplete="off"
              autocorrect="off"
              autocapitalize="off"
              spellcheck="false"
            />
          </label>
          <label class="tc-withdraw-apply__bank-row">
            <span class="tc-withdraw-apply__bank-label">
              {{ t('teacherCenter.withdrawal.apply.bankHolderNameLabel') }}
            </span>
            <input
              v-model="bankPayeeInfo.holderName"
              class="tc-withdraw-apply__input"
              type="text"
              :placeholder="t('teacherCenter.withdrawal.apply.bankHolderNamePlaceholder')"
              :disabled="submitting"
              autocomplete="off"
              autocorrect="off"
              autocapitalize="off"
              spellcheck="false"
            />
          </label>
        </div>
        <textarea
          v-else
          v-model="payeeInfo"
          class="tc-withdraw-apply__textarea"
          :placeholder="payeePlaceholder"
          :disabled="submitting"
          rows="3"
          autocomplete="off"
          autocorrect="off"
          autocapitalize="off"
          spellcheck="false"
        ></textarea>
        <p class="tc-withdraw-apply__hint">{{ payeeHint }}</p>
        <p class="tc-withdraw-apply__security-note">
          ⓘ {{ t('teacherCenter.withdrawal.apply.payeeInfoSecurityNote') }}
        </p>
      </div>

      <!-- 确认勾选 -->
      <div class="tc-withdraw-apply__confirm">
        <el-checkbox v-model="confirmed" :disabled="submitting">
          {{ t('teacherCenter.withdrawal.apply.confirmCheckbox') }}
        </el-checkbox>
      </div>

      <!-- 按钮区 -->
      <div class="tc-withdraw-apply__actions">
        <el-button :disabled="submitting" @click="onCancel">
          {{ t('teacherCenter.withdrawal.apply.cancelBtn') }}
        </el-button>
        <el-button
          type="primary"
          :disabled="!canSubmit"
          :loading="submitting"
          native-type="submit"
        >
          {{ submitting
            ? t('teacherCenter.withdrawal.apply.submitting')
            : t('teacherCenter.withdrawal.apply.submitBtn') }}
        </el-button>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
.tc-withdraw-apply {
  max-width: 600px;
  margin: 0 auto;
  padding: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: 0;
  }

  &__header {
    margin-block-end: brand.$spacing-6;
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

  &__form {
    background: brand.$color-bg-card;
    border: 1px solid brand.$color-border;
    border-radius: brand.$radius-lg;
    padding: brand.$spacing-6 brand.$spacing-5;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-5;

    @media (min-width: brand.$bp-tablet) {
      padding: brand.$spacing-8;
    }
  }

  &__balance {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    padding: brand.$spacing-4;
    background: brand.$brand-primary-soft;
    border-radius: brand.$radius-base;
  }

  &__balance-label {
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
  }

  &__balance-value {
    font-size: brand.$font-size-xl;
    font-weight: 700;
    color: brand.$brand-primary-deep;
    font-variant-numeric: tabular-nums;
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__label {
    font-size: brand.$font-size-base;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__textarea {
    width: 100%;
    border: 1.5px solid brand.$color-border;
    border-radius: brand.$radius-base;
    padding: brand.$spacing-3 brand.$spacing-4;
    font: inherit;
    font-size: brand.$font-size-base;
    color: brand.$color-text-primary;
    background: brand.$color-bg-card;
    resize: vertical;
    transition: border-color 0.15s, box-shadow 0.15s;
    box-sizing: border-box;

    // iOS Safari 防自动 zoom
    @media (max-width: brand.$bp-tablet) {
      font-size: 16px;
    }

    &:focus {
      outline: none;
      border-color: brand.$brand-primary;
      box-shadow: brand.$ring-focus;
    }

    &:disabled {
      background: brand.$color-bg-page;
      color: brand.$color-text-tertiary;
      cursor: not-allowed;
    }
  }

  &__bank-fields {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
  }

  &__bank-row {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-2;
  }

  &__bank-label {
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-sm;
    font-weight: 600;
  }

  &__input {
    width: 100%;
    min-height: 48px;
    border: 1.5px solid brand.$color-border;
    border-radius: brand.$radius-base;
    padding: 0 brand.$spacing-4;
    font: inherit;
    font-size: brand.$font-size-base;
    color: brand.$color-text-primary;
    background: brand.$color-bg-card;
    transition: border-color 0.15s, box-shadow 0.15s;
    box-sizing: border-box;

    @media (max-width: brand.$bp-tablet) {
      font-size: 16px;
    }

    &:focus {
      outline: none;
      border-color: brand.$brand-primary;
      box-shadow: brand.$ring-focus;
    }

    &:disabled {
      background: brand.$color-bg-page;
      color: brand.$color-text-tertiary;
      cursor: not-allowed;
    }
  }

  &__hint {
    margin: 0;
    color: brand.$color-text-tertiary;
    font-size: brand.$font-size-sm;
  }

  &__security-note {
    margin: 0;
    color: brand.$color-warning;
    font-size: brand.$font-size-xs;
    line-height: brand.$line-height-base;
  }

  &__confirm {
    padding: brand.$spacing-3;
    background: brand.$color-bg-page;
    border-radius: brand.$radius-base;

    :deep(.el-checkbox__label) {
      white-space: normal;
      line-height: brand.$line-height-base;
      font-size: brand.$font-size-sm;
      color: brand.$color-text-primary;
    }
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: brand.$spacing-3;
    border-block-start: 1px solid brand.$color-border-light;
    padding-block-start: brand.$spacing-4;

    @media (max-width: brand.$bp-tablet) {
      flex-direction: column-reverse;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
