<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduWithdrawalApi } from '#/api/edu/withdrawal';

import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';

import { Tabs, Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getWithdrawalPage } from '#/api/edu/withdrawal';
import { $t } from '#/locales';

import AuditModal from './modules/AuditModal.vue';
import MarkFailedModal from './modules/MarkFailedModal.vue';
import MarkPaidModal from './modules/MarkPaidModal.vue';
import PayeeInfoReveal from './modules/PayeeInfoReveal.vue';
import WithdrawalDetailModal from './modules/WithdrawalDetailModal.vue';
import {
  getPayeeMethodLabel,
  getWithdrawalStatusLabel,
  useGridColumns,
  useGridFormSchema,
  WITHDRAWAL_STATUS_COLOR,
} from './data';

const route = useRoute();

/** 当前 tab(默认进入「待审核」最高优先级)
 *  '' = 全部 */
const activeTab = ref<string>('pending');

/** i18n label map(computed 保证 locale 切换时响应) */
const PAYEE_METHOD_LABEL = computed(() => getPayeeMethodLabel());
const WITHDRAWAL_STATUS_LABEL = computed(() => getWithdrawalStatusLabel());

/** 审核 modal(通过 / 驳回) */
const [AuditModalCmp, auditModalApi] = useVbenModal({
  connectedComponent: AuditModal,
  destroyOnClose: true,
});

/** 标记打款 modal */
const [MarkPaidModalCmp, markPaidModalApi] = useVbenModal({
  connectedComponent: MarkPaidModal,
  destroyOnClose: true,
});

/** 标记打款失败 modal */
const [MarkFailedModalCmp, markFailedModalApi] = useVbenModal({
  connectedComponent: MarkFailedModal,
  destroyOnClose: true,
});

/** payee 明文 reveal 弹层(3 秒倒计时) */
const [PayeeRevealCmp, payeeRevealApi] = useVbenModal({
  connectedComponent: PayeeInfoReveal,
  destroyOnClose: true,
});

/** 详情 modal */
const [DetailModalCmp, detailModalApi] = useVbenModal({
  connectedComponent: WithdrawalDetailModal,
  destroyOnClose: true,
});

/** 刷新表格(modal 提交成功后回调) */
function handleRefresh() {
  gridApi.query();
}

/** 详情(任意状态可点) */
function handleViewDetail(row: EduWithdrawalApi.Withdrawal) {
  detailModalApi
    .setData({
      withdrawal: row,
      onRevealPayee: () => handleRevealPayee(row),
    })
    .open();
}

/** 通过 / 驳回 */
function handleAudit(row: EduWithdrawalApi.Withdrawal) {
  auditModalApi.setData({ withdrawal: row }).open();
}

/** 标记打款 */
function handleMarkPaid(row: EduWithdrawalApi.Withdrawal) {
  markPaidModalApi.setData({ withdrawal: row }).open();
}

/** 标记打款失败 */
function handleMarkFailed(row: EduWithdrawalApi.Withdrawal) {
  markFailedModalApi.setData({ withdrawal: row }).open();
}

/** 查看完整收款信息明文 */
function handleRevealPayee(row: EduWithdrawalApi.Withdrawal) {
  payeeRevealApi.setData({ withdrawal: row }).open();
}

/** tab 切换:更新 activeTab(闭包内被 query 函数读取)+ 触发查询 */
function handleTabChange(key: number | string) {
  activeTab.value = String(key);
  gridApi.query();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    /** 隐藏 status 字段,通过 tab 切换控制 */
    fieldMappingTime: [
      // 把 RangePicker 的 [from, to] 映射为后端的 appliedAtFrom / appliedAtTo
      ['appliedAtRange', ['appliedAtFrom', 'appliedAtTo']],
    ],
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const status =
            activeTab.value === '' ? undefined : activeTab.value;
          return await getWithdrawalPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            status: status as EduWithdrawalApi.WithdrawalStatus | undefined,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<EduWithdrawalApi.Withdrawal>,
});

/** 支持反向链:teacher-income 的「查看全部提现」按 query.teacherId 进入时,
 *  自动:① tab 切到「全部」② 表单 teacherId 字段填充 ③ 触发查询。
 */
onMounted(() => {
  const teacherIdRaw = route.query.teacherId;
  if (teacherIdRaw) {
    const tid = Number(teacherIdRaw);
    if (Number.isFinite(tid) && tid > 0) {
      activeTab.value = '';
      gridApi.formApi.setValues({ teacherId: tid });
      gridApi.query();
    }
  }
});
</script>

<template>
  <Page auto-content-height>
    <AuditModalCmp @success="handleRefresh" />
    <MarkPaidModalCmp @success="handleRefresh" />
    <MarkFailedModalCmp @success="handleRefresh" />
    <PayeeRevealCmp />
    <DetailModalCmp />

    <!-- 5 tab + 全部 -->
    <Tabs
      :active-key="activeTab"
      type="line"
      class="bg-card px-4 pt-2"
      @change="handleTabChange"
    >
      <Tabs.TabPane key="" :tab="$t('edu.withdrawal.tabs.all')" />
      <Tabs.TabPane key="pending" :tab="$t('edu.withdrawal.tabs.pending')" />
      <Tabs.TabPane key="approved" :tab="$t('edu.withdrawal.tabs.approved')" />
      <Tabs.TabPane key="paid" :tab="$t('edu.withdrawal.tabs.paid')" />
      <Tabs.TabPane key="rejected" :tab="$t('edu.withdrawal.tabs.rejected')" />
      <Tabs.TabPane key="failed" :tab="$t('edu.withdrawal.tabs.failed')" />
    </Tabs>

    <Grid :table-title="$t('edu.withdrawal.pageTitle')">
      <!-- 金额(USD) -->
      <template #amount="{ row }">
        <span class="font-semibold text-blue-600">
          ${{ Number(row.amount).toFixed(2) }}
        </span>
      </template>

      <!-- 收款方式 -->
      <template #payeeMethod="{ row }">
        <Tag color="default">
          {{ PAYEE_METHOD_LABEL[row.payeeMethod] || row.payeeMethod }}
        </Tag>
      </template>

      <!-- 状态徽章 -->
      <template #status="{ row }">
        <Tag :color="WITHDRAWAL_STATUS_COLOR[row.status] || 'default'">
          {{ WITHDRAWAL_STATUS_LABEL[row.status] || row.status }}
        </Tag>
      </template>

      <!-- 申请时间:UTC + 本地双行 -->
      <template #appliedAt="{ row }">
        <div v-if="row.appliedAt">
          <div>{{ new Date(row.appliedAt).toLocaleString() }}</div>
          <div class="text-xs text-gray-400">{{ row.appliedAt }} (UTC)</div>
        </div>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 操作列:按 status 显示不同按钮 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('edu.withdrawal.action.viewDetail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['edu:withdrawal:query'],
              onClick: handleViewDetail.bind(null, row),
            },
            {
              label: $t('edu.withdrawal.action.approve'),
              type: 'link',
              ifShow: row.status === 'pending',
              auth: ['edu:withdrawal:audit'],
              onClick: handleAudit.bind(null, row),
            },
            {
              label: $t('edu.withdrawal.action.reject'),
              type: 'link',
              danger: true,
              ifShow: row.status === 'pending',
              auth: ['edu:withdrawal:audit'],
              onClick: handleAudit.bind(null, row),
            },
            {
              label: $t('edu.withdrawal.action.markPaid'),
              type: 'link',
              ifShow: row.status === 'approved',
              auth: ['edu:withdrawal:pay'],
              onClick: handleMarkPaid.bind(null, row),
            },
            {
              label: $t('edu.withdrawal.action.markFailed'),
              type: 'link',
              danger: true,
              ifShow: row.status === 'approved',
              auth: ['edu:withdrawal:pay'],
              onClick: handleMarkFailed.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
