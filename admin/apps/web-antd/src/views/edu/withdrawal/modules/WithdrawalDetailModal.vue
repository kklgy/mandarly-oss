<script lang="ts" setup>
import type { EduWithdrawalApi } from '#/api/edu/withdrawal';

import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenModal } from '@vben/common-ui';

import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Descriptions,
  DescriptionsItem,
  Image,
  Tag,
  Timeline,
  TimelineItem,
} from 'ant-design-vue';

import { $t } from '#/locales';

import {
  getPayeeMethodLabel,
  getWithdrawalStatusLabel,
  WITHDRAWAL_STATUS_COLOR,
} from '../data';

/** spec §6.2 / §6.7 提现详情 modal
 *  - 完整字段(去敏)
 *  - 时间线:申请 → 审核 → 打款 / 失败
 *  - 「查看完整收款信息」按钮(权限 edu:withdrawal:reveal-payee)→ 父页面打开 PayeeInfoReveal
 *  - 「查看该教师收入明细」链跳 teacher-income(query: teacherId)
 *
 *  传入:{ withdrawal, onRevealPayee }
 */
interface ModalData {
  withdrawal: EduWithdrawalApi.Withdrawal;
  /** 触发 PayeeInfoReveal(由父页面持有) */
  onRevealPayee?: (row: EduWithdrawalApi.Withdrawal) => void;
}

const modalData = ref<ModalData>();
const router = useRouter();

const withdrawal = computed(() => modalData.value?.withdrawal);
const PAYEE_METHOD_LABEL = computed(() => getPayeeMethodLabel());
const WITHDRAWAL_STATUS_LABEL = computed(() => getWithdrawalStatusLabel());

function formatUtcLocal(utcStr?: null | string) {
  if (!utcStr) return '-';
  return new Date(utcStr).toLocaleString();
}

function handleRevealPayee() {
  if (!withdrawal.value || !modalData.value?.onRevealPayee) return;
  modalData.value.onRevealPayee(withdrawal.value);
}

function handleJumpTeacherIncome() {
  if (!withdrawal.value) return;
  router.push({
    path: '/edu/teacher-income',
    query: { teacherId: String(withdrawal.value.teacherId) },
  });
}

const [Modal, modalApi] = useVbenModal({
  title: $t('edu.withdrawal.detail.title'),
  showConfirmButton: false,
  cancelText: $t('edu.withdrawal.modal.cancel'),
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      modalData.value = undefined;
      return;
    }
    modalData.value = modalApi.getData<ModalData>();
  },
});
</script>

<template>
  <Modal>
    <div v-if="withdrawal" class="space-y-4">
      <!-- 基础信息 -->
      <Descriptions
        :title="$t('edu.withdrawal.detail.basicInfoTitle')"
        :column="2"
        bordered
        size="small"
      >
        <DescriptionsItem :label="$t('edu.withdrawal.column.id')">
          {{ withdrawal.id }}
        </DescriptionsItem>
        <DescriptionsItem :label="$t('edu.withdrawal.column.status')">
          <Tag :color="WITHDRAWAL_STATUS_COLOR[withdrawal.status] || 'default'">
            {{ WITHDRAWAL_STATUS_LABEL[withdrawal.status] || withdrawal.status }}
          </Tag>
        </DescriptionsItem>
        <DescriptionsItem :label="$t('edu.withdrawal.column.teacher')">
          <span class="mr-2">{{ withdrawal.teacherId }}</span>
          <!-- 「查看收入明细」按钮 zh-TW 未覆盖,保留中文(辅助跳转链接) -->
          <Button
            type="link"
            size="small"
            v-access:code="['edu:teacher-income:query']"
            @click="handleJumpTeacherIncome"
          >
            <IconifyIcon icon="ant-design:link-outlined" class="mr-1" />
            查看收入明细
          </Button>
        </DescriptionsItem>
        <DescriptionsItem :label="$t('edu.withdrawal.column.amount')">
          <span class="text-base font-semibold text-blue-600">
            {{ withdrawal.currency || 'USD' }}
            ${{ Number(withdrawal.amount).toFixed(2) }}
          </span>
        </DescriptionsItem>
        <DescriptionsItem :label="$t('edu.withdrawal.column.payeeMethod')">
          {{
            PAYEE_METHOD_LABEL[withdrawal.payeeMethod] || withdrawal.payeeMethod
          }}
        </DescriptionsItem>
        <DescriptionsItem :label="$t('edu.withdrawal.column.payeeMasked')">
          <div class="flex items-center gap-2">
            <span>{{ withdrawal.payeeInfoMasked || '-' }}</span>
            <Button
              type="link"
              size="small"
              danger
              v-access:code="['edu:withdrawal:reveal-payee']"
              @click="handleRevealPayee"
            >
              <IconifyIcon icon="ant-design:eye-outlined" class="mr-1" />
              {{ $t('edu.withdrawal.action.revealPayee') }}
            </Button>
          </div>
        </DescriptionsItem>
      </Descriptions>

      <!-- 时间线 -->
      <div>
        <div class="mb-2 text-sm font-medium">
          {{ $t('edu.withdrawal.detail.timelineTitle') }}
        </div>
        <Timeline>
          <TimelineItem color="blue">
            <!-- 时间线节点文案 zh-TW 未覆盖,保留中文 -->
            <div class="text-sm font-medium">申请提交</div>
            <div class="text-xs text-gray-500">
              {{ formatUtcLocal(withdrawal.appliedAt) }}
              <span class="ml-2 text-gray-400">
                ({{ withdrawal.appliedAt }} UTC)
              </span>
            </div>
          </TimelineItem>

          <TimelineItem
            v-if="withdrawal.auditedAt"
            :color="
              withdrawal.status === 'rejected'
                ? 'red'
                : withdrawal.status === 'pending'
                  ? 'gray'
                  : 'green'
            "
          >
            <div class="text-sm font-medium">
              {{ withdrawal.status === 'rejected' ? '审核驳回' : '审核通过' }}
              <span class="ml-2 text-xs text-gray-400">
                by admin #{{ withdrawal.auditedBy }}
              </span>
            </div>
            <div class="text-xs text-gray-500">
              {{ formatUtcLocal(withdrawal.auditedAt) }}
            </div>
            <div
              v-if="withdrawal.rejectReason"
              class="mt-1 rounded bg-red-50 p-2 text-xs text-red-700"
            >
              {{ $t('edu.withdrawal.detail.rejectReasonLabel') }}:{{
                withdrawal.rejectReason
              }}
            </div>
          </TimelineItem>

          <TimelineItem
            v-if="withdrawal.status === 'paid' && withdrawal.paidAt"
            color="green"
          >
            <div class="text-sm font-medium">
              打款成功
              <span class="ml-2 text-xs text-gray-400">
                by admin #{{ withdrawal.paidBy }}
              </span>
            </div>
            <div class="text-xs text-gray-500">
              {{ formatUtcLocal(withdrawal.paidAt) }}
            </div>
            <div v-if="withdrawal.paidRemark" class="mt-1 text-xs text-gray-700">
              {{ $t('edu.withdrawal.detail.paidRemarkLabel') }}:{{
                withdrawal.paidRemark
              }}
            </div>
            <div v-if="withdrawal.paidProof" class="mt-2">
              <div class="mb-1 text-xs text-gray-500">
                {{ $t('edu.withdrawal.detail.paidProofLabel') }}
              </div>
              <Image :src="withdrawal.paidProof" :width="160" />
            </div>
          </TimelineItem>

          <TimelineItem
            v-if="withdrawal.status === 'failed' && withdrawal.paidAt"
            color="red"
          >
            <div class="text-sm font-medium">
              打款失败
              <span class="ml-2 text-xs text-gray-400">
                by admin #{{ withdrawal.paidBy }}
              </span>
            </div>
            <div class="text-xs text-gray-500">
              {{ formatUtcLocal(withdrawal.paidAt) }}
            </div>
            <div
              v-if="withdrawal.paidRemark"
              class="mt-1 rounded bg-red-50 p-2 text-xs text-red-700"
            >
              {{ $t('edu.withdrawal.detail.failReasonLabel') }}:{{
                withdrawal.paidRemark
              }}
            </div>
          </TimelineItem>
        </Timeline>
      </div>
    </div>

    <div v-else class="p-4 text-center text-gray-400">
      {{ $t('edu.withdrawal.modal.submitting') }}
    </div>
  </Modal>
</template>
