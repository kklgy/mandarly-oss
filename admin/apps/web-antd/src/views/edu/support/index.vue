<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduSupportApi } from '#/api/edu/support';

import { onMounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { Card, Col, message, Row, Statistic, Tabs, Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteSupportContact,
  deleteSupportFaq,
  getSupportContactPage,
  getSupportFaqPage,
  getSupportInquiryPage,
  getSupportInquiryStats,
  getSupportTopUnmatched,
} from '#/api/edu/support';
import { $t } from '#/locales';

import {
  useContactColumns,
  useContactGridFormSchema,
  useFaqColumns,
  useFaqGridFormSchema,
  useInquiryColumns,
  useInquiryGridFormSchema,
} from './data';
import ContactForm from './modules/ContactForm.vue';
import FaqForm from './modules/FaqForm.vue';

const activeTab = ref('faq');
const stats = ref<EduSupportApi.InquiryStats>();
const topUnmatched = ref<EduSupportApi.TopUnmatched[]>([]);

const [FaqModal, faqModalApi] = useVbenModal({
  connectedComponent: FaqForm,
  destroyOnClose: true,
});
const [ContactModal, contactModalApi] = useVbenModal({
  connectedComponent: ContactForm,
  destroyOnClose: true,
});

function percent(value?: number | string) {
  const num = Number(value || 0) * 100;
  return `${num.toFixed(1)}%`;
}

async function refreshInquiryStats() {
  const [nextStats, nextTopUnmatched] = await Promise.all([
    getSupportInquiryStats(),
    getSupportTopUnmatched(8),
  ]);
  stats.value = nextStats;
  topUnmatched.value = nextTopUnmatched;
}

function refreshFaq() {
  faqGridApi.query();
}

function refreshContact() {
  contactGridApi.query();
}

function refreshInquiry() {
  inquiryGridApi.query();
  refreshInquiryStats();
}

function handleCreateFaq() {
  faqModalApi.setData(null).open();
}

function handleEditFaq(row: EduSupportApi.Faq) {
  faqModalApi.setData(row).open();
}

async function handleDeleteFaq(row: EduSupportApi.Faq) {
  const hideLoading = message.loading({ content: '正在删除 FAQ', duration: 0 });
  try {
    await deleteSupportFaq(row.id!);
    message.success('FAQ 已删除');
    refreshFaq();
  } finally {
    hideLoading();
  }
}

function handleCreateContact() {
  contactModalApi.setData(null).open();
}

function handleEditContact(row: EduSupportApi.Contact) {
  contactModalApi.setData(row).open();
}

async function handleDeleteContact(row: EduSupportApi.Contact) {
  const hideLoading = message.loading({ content: '正在删除联系方式', duration: 0 });
  try {
    await deleteSupportContact(row.id!);
    message.success('联系方式已删除');
    refreshContact();
  } finally {
    hideLoading();
  }
}

const [FaqGrid, faqGridApi] = useVbenVxeGrid({
  formOptions: { schema: useFaqGridFormSchema() },
  gridOptions: {
    columns: useFaqColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getSupportFaqPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<EduSupportApi.Faq>,
});

const [ContactGrid, contactGridApi] = useVbenVxeGrid({
  formOptions: { schema: useContactGridFormSchema() },
  gridOptions: {
    columns: useContactColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getSupportContactPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<EduSupportApi.Contact>,
});

const [InquiryGrid, inquiryGridApi] = useVbenVxeGrid({
  formOptions: { schema: useInquiryGridFormSchema() },
  gridOptions: {
    columns: useInquiryColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getSupportInquiryPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<EduSupportApi.Inquiry>,
});

onMounted(refreshInquiryStats);
</script>

<template>
  <Page auto-content-height>
    <FaqModal @success="refreshFaq" />
    <ContactModal @success="refreshContact" />

    <!-- Tabs 只做切换器,3 个 Grid 同级展开;Grid 嵌入 Tabs.TabPane 时 height:'auto' 算不准,见 D22 fix -->
    <Tabs v-model:active-key="activeTab" class="bg-card px-4 pt-2" type="line">
      <Tabs.TabPane key="faq" tab="FAQ 管理" />
      <Tabs.TabPane key="contact" tab="联系方式" />
      <Tabs.TabPane key="inquiry" tab="咨询日志" />
    </Tabs>

    <FaqGrid v-show="activeTab === 'faq'" table-title="FAQ 列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['FAQ']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['edu:support-faq:create'],
              onClick: handleCreateFaq,
            },
          ]"
        />
      </template>

      <template #keywords="{ row }">
        <div class="flex flex-wrap gap-1">
          <Tag v-for="keyword in row.keywords || []" :key="keyword">
            {{ keyword }}
          </Tag>
          <span v-if="!row.keywords?.length" class="text-gray-400">-</span>
        </div>
      </template>

      <template #faqStatus="{ row }">
        <Tag v-if="row.status === 'active'" color="green">启用</Tag>
        <Tag v-else>停用</Tag>
      </template>

      <template #faqActions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['edu:support-faq:update'],
              onClick: handleEditFaq.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['edu:support-faq:delete'],
              popConfirm: {
                title: '确认删除该 FAQ?',
                confirm: handleDeleteFaq.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </FaqGrid>

    <ContactGrid v-show="activeTab === 'contact'" table-title="私域联系方式">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['联系方式']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['edu:support-contact:create'],
              onClick: handleCreateContact,
            },
          ]"
        />
      </template>

      <template #contactStatus="{ row }">
        <Tag v-if="row.isActive" color="green">启用</Tag>
        <Tag v-else>停用</Tag>
      </template>

      <template #contactActions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['edu:support-contact:update'],
              onClick: handleEditContact.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['edu:support-contact:delete'],
              popConfirm: {
                title: '确认删除该联系方式?',
                confirm: handleDeleteContact.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </ContactGrid>

    <div v-show="activeTab === 'inquiry'">
      <Row :gutter="[16, 16]" class="mb-4">
        <Col :lg="6" :md="12" :xs="24">
          <Card size="small"><Statistic title="交互总量" :value="stats?.totalCount || 0" /></Card>
        </Col>
        <Col :lg="6" :md="12" :xs="24">
          <Card size="small">
            <Statistic title="提问数" :value="stats?.askCount || 0" />
            <div class="mt-1 text-xs text-gray-500">直接点客服 {{ stats?.directClickCount || 0 }}</div>
          </Card>
        </Col>
        <Col :lg="6" :md="12" :xs="24">
          <Card size="small">
            <Statistic title="FAQ 命中率" :value="percent(stats?.matchRate)" />
            <div class="mt-1 text-xs text-gray-500">未命中 {{ stats?.unmatchedCount || 0 }}</div>
          </Card>
        </Col>
        <Col :lg="6" :md="12" :xs="24">
          <Card size="small">
            <Statistic title="转人工点击率" :value="percent(stats?.clickRate)" />
            <div class="mt-1 text-xs text-gray-500">点击数 {{ stats?.clickedToHumanCount || 0 }}</div>
          </Card>
        </Col>
      </Row>

      <Card v-if="topUnmatched.length" size="small" title="未命中问题 Top 8" class="mb-4">
        <div class="flex flex-wrap gap-2">
          <Tag v-for="item in topUnmatched" :key="item.questionText" color="orange">
            {{ item.questionText }} × {{ item.count }}
          </Tag>
        </div>
      </Card>

      <InquiryGrid table-title="咨询日志">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '刷新指标',
                type: 'primary',
                icon: ACTION_ICON.REFRESH,
                auth: ['edu:support-inquiry:query'],
                onClick: refreshInquiry,
              },
            ]"
          />
        </template>

        <template #matchedFaq="{ row }">
          <Tag v-if="row.matchedFaqId" color="green">FAQ #{{ row.matchedFaqId }}</Tag>
          <Tag v-else color="orange">未命中</Tag>
        </template>

        <template #clickedToHuman="{ row }">
          <Tag v-if="row.clickedToHuman" color="blue">已点击</Tag>
          <Tag v-else>否</Tag>
        </template>
      </InquiryGrid>
    </div>
  </Page>
</template>
