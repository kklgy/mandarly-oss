<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduLevelCheckApi } from '#/api/edu/level-check';

import { computed, onMounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { Card, Col, message, Row, Statistic, Tabs, Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteLevelCheckOption,
  deleteLevelCheckQuestion,
  getLevelCheckOptionList,
  getLevelCheckQuestionPage,
  getLevelCheckSubmissionPage,
  getLevelCheckSubmissionStats,
} from '#/api/edu/level-check';
import { $t } from '#/locales';

import {
  useOptionColumns,
  useQuestionColumns,
  useQuestionGridFormSchema,
  useSubmissionColumns,
  useSubmissionGridFormSchema,
} from './data';
import OptionForm from './modules/OptionForm.vue';
import QuestionForm from './modules/QuestionForm.vue';

const activeTab = ref('question');
const selectedQuestion = ref<EduLevelCheckApi.Question>();
const stats = ref<EduLevelCheckApi.SubmissionStats>();
const lastSubmissionParams = ref<EduLevelCheckApi.SubmissionPageReq>({
  pageNo: 1,
  pageSize: 10,
});

const selectedQuestionTitle = computed(() =>
  selectedQuestion.value
    ? `${selectedQuestion.value.questionCode} 的选项`
    : '选项配置',
);

const [QuestionModal, questionModalApi] = useVbenModal({
  connectedComponent: QuestionForm,
  destroyOnClose: true,
});

const [OptionModal, optionModalApi] = useVbenModal({
  connectedComponent: OptionForm,
  destroyOnClose: true,
});

function percent(value?: number | string) {
  return `${(Number(value || 0) * 100).toFixed(1)}%`;
}

function labelLevel(level?: string) {
  if (level === 'beginner') return 'Beginner';
  if (level === 'intermediate') return 'Intermediate';
  if (level === 'advanced') return 'Advanced';
  return level || '-';
}

function levelColor(level?: string) {
  if (level === 'beginner') return 'blue';
  if (level === 'intermediate') return 'orange';
  if (level === 'advanced') return 'green';
  return 'default';
}

function refreshQuestion() {
  questionGridApi.query();
}

function refreshOption() {
  optionGridApi.query();
}

async function refreshStats() {
  stats.value = await getLevelCheckSubmissionStats();
}

function refreshSubmission() {
  submissionGridApi.query();
  refreshStats();
}

function handleCreateQuestion() {
  questionModalApi.setData(null).open();
}

function handleEditQuestion(row: EduLevelCheckApi.Question) {
  questionModalApi.setData(row).open();
}

async function handleDeleteQuestion(row: EduLevelCheckApi.Question) {
  const hideLoading = message.loading({ content: '正在删除题目', duration: 0 });
  try {
    await deleteLevelCheckQuestion(row.id!);
    if (selectedQuestion.value?.id === row.id) {
      selectedQuestion.value = undefined;
      refreshOption();
    }
    message.success('题目已删除');
    refreshQuestion();
  } finally {
    hideLoading();
  }
}

function handleSelectQuestion(row: EduLevelCheckApi.Question) {
  selectedQuestion.value = row;
  refreshOption();
}

function handleCreateOption() {
  if (!selectedQuestion.value?.id) {
    message.warning('请先选择一道题目');
    return;
  }
  optionModalApi
    .setData({ questionId: selectedQuestion.value.id, sort: 0 })
    .open();
}

function handleEditOption(row: EduLevelCheckApi.Option) {
  optionModalApi.setData(row).open();
}

async function handleDeleteOption(row: EduLevelCheckApi.Option) {
  const hideLoading = message.loading({ content: '正在删除选项', duration: 0 });
  try {
    await deleteLevelCheckOption(row.id!);
    message.success('选项已删除');
    refreshOption();
  } finally {
    hideLoading();
  }
}

function csvEscape(value: unknown) {
  if (value === null || value === undefined) return '';
  const text = typeof value === 'string' ? value : JSON.stringify(value);
  return `"${text.replaceAll('"', '""')}"`;
}

async function exportSubmissions() {
  const page = await getLevelCheckSubmissionPage({
    ...lastSubmissionParams.value,
    pageNo: 1,
    pageSize: 1000,
  });
  const rows = page.list || [];
  const header = [
    'id',
    'createTime',
    'locale',
    'email',
    'inferredLevel',
    'recommendedTeacherIds',
    'recommendedPackageId',
    'isConverted',
    'answers',
  ];
  const csv = [
    header.join(','),
    ...rows.map((row) =>
      [
        row.id,
        row.createTime,
        row.locale,
        row.email,
        row.inferredLevel,
        row.recommendedTeacherIds,
        row.recommendedPackageId,
        row.isConverted,
        row.answers,
      ].map(csvEscape).join(','),
    ),
  ].join('\n');
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `level-check-submissions-${Date.now()}.csv`;
  link.click();
  URL.revokeObjectURL(url);
}

const [QuestionGrid, questionGridApi] = useVbenVxeGrid({
  formOptions: { schema: useQuestionGridFormSchema() },
  gridOptions: {
    columns: useQuestionColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getLevelCheckQuestionPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id', isCurrent: true, isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<EduLevelCheckApi.Question>,
});

const [OptionGrid, optionGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useOptionColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!selectedQuestion.value?.id) {
            return { list: [], total: 0 };
          }
          const list = await getLevelCheckOptionList(selectedQuestion.value.id);
          return { list, total: list.length };
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true },
  } as VxeTableGridOptions<EduLevelCheckApi.Option>,
});

const [SubmissionGrid, submissionGridApi] = useVbenVxeGrid({
  formOptions: { schema: useSubmissionGridFormSchema() },
  gridOptions: {
    columns: useSubmissionColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          lastSubmissionParams.value = {
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          };
          return await getLevelCheckSubmissionPage(lastSubmissionParams.value);
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<EduLevelCheckApi.Submission>,
});

onMounted(refreshStats);
</script>

<template>
  <Page auto-content-height>
    <QuestionModal @success="refreshQuestion" />
    <OptionModal @success="refreshOption" />

    <Tabs v-model:active-key="activeTab">
      <Tabs.TabPane key="question" tab="题库配置">
        <QuestionGrid table-title="题目列表">
          <template #toolbar-tools>
            <TableAction
              :actions="[
                {
                  label: $t('ui.actionTitle.create', ['题目']),
                  type: 'primary',
                  icon: ACTION_ICON.ADD,
                  auth: ['edu:level-check:create'],
                  onClick: handleCreateQuestion,
                },
              ]"
            />
          </template>

          <template #active="{ row }">
            <Tag v-if="row.isActive" color="green">启用</Tag>
            <Tag v-else>停用</Tag>
          </template>

          <template #questionActions="{ row }">
            <TableAction
              :actions="[
                {
                  label: '选项',
                  type: 'link',
                  icon: ACTION_ICON.VIEW,
                  auth: ['edu:level-check:query'],
                  onClick: handleSelectQuestion.bind(null, row),
                },
                {
                  label: $t('common.edit'),
                  type: 'link',
                  icon: ACTION_ICON.EDIT,
                  auth: ['edu:level-check:update'],
                  onClick: handleEditQuestion.bind(null, row),
                },
                {
                  label: $t('common.delete'),
                  type: 'link',
                  danger: true,
                  icon: ACTION_ICON.DELETE,
                  auth: ['edu:level-check:delete'],
                  popConfirm: {
                    title: '确认删除该题目及其选项?',
                    confirm: handleDeleteQuestion.bind(null, row),
                  },
                },
              ]"
            />
          </template>
        </QuestionGrid>

        <div class="mt-4">
          <OptionGrid :table-title="selectedQuestionTitle">
            <template #toolbar-tools>
              <TableAction
                :actions="[
                  {
                    label: $t('ui.actionTitle.create', ['选项']),
                    type: 'primary',
                    icon: ACTION_ICON.ADD,
                    auth: ['edu:level-check:create'],
                    onClick: handleCreateOption,
                  },
                ]"
              />
            </template>

            <template #level="{ row }">
              <Tag v-if="row.inferredLevel" :color="levelColor(row.inferredLevel)">
                {{ labelLevel(row.inferredLevel) }}
              </Tag>
              <span v-else class="text-gray-400">-</span>
            </template>

            <template #expertise="{ row }">
              <div class="flex flex-wrap gap-1">
                <Tag v-for="tag in row.matchExpertise || []" :key="tag">
                  {{ tag }}
                </Tag>
                <span v-if="!row.matchExpertise?.length" class="text-gray-400">-</span>
              </div>
            </template>

            <template #scoreRules="{ row }">
              <code class="text-xs">
                {{ row.scoreRules?.length ? JSON.stringify(row.scoreRules) : '-' }}
              </code>
            </template>

            <template #optionActions="{ row }">
              <TableAction
                :actions="[
                  {
                    label: $t('common.edit'),
                    type: 'link',
                    icon: ACTION_ICON.EDIT,
                    auth: ['edu:level-check:update'],
                    onClick: handleEditOption.bind(null, row),
                  },
                  {
                    label: $t('common.delete'),
                    type: 'link',
                    danger: true,
                    icon: ACTION_ICON.DELETE,
                    auth: ['edu:level-check:delete'],
                    popConfirm: {
                      title: '确认删除该选项?',
                      confirm: handleDeleteOption.bind(null, row),
                    },
                  },
                ]"
              />
            </template>
          </OptionGrid>
        </div>
      </Tabs.TabPane>

      <Tabs.TabPane key="submission" tab="答卷看板">
        <Row :gutter="16" class="mb-4">
          <Col :lg="6" :sm="12" :xs="24">
            <Card size="small">
              <Statistic title="答卷数" :value="stats?.totalCount || 0" />
            </Card>
          </Col>
          <Col :lg="6" :sm="12" :xs="24">
            <Card size="small">
              <Statistic title="已下单" :value="stats?.convertedCount || 0" />
            </Card>
          </Col>
          <Col :lg="6" :sm="12" :xs="24">
            <Card size="small">
              <Statistic title="未下单" :value="stats?.unconvertedCount || 0" />
            </Card>
          </Col>
          <Col :lg="6" :sm="12" :xs="24">
            <Card size="small">
              <Statistic title="转化率" :value="percent(stats?.conversionRate)" />
            </Card>
          </Col>
        </Row>

        <Card class="mb-4" size="small" title="等级分布">
          <div class="flex flex-wrap gap-2">
            <Tag
              v-for="item in stats?.levelStats || []"
              :key="item.inferredLevel"
              :color="levelColor(item.inferredLevel)"
            >
              {{ labelLevel(item.inferredLevel) }} · {{ item.count }} ·
              {{ percent(item.rate) }}
            </Tag>
            <span v-if="!stats?.levelStats?.length" class="text-gray-400">
              暂无答卷数据
            </span>
          </div>
        </Card>

        <SubmissionGrid table-title="答卷记录">
          <template #toolbar-tools>
            <TableAction
              :actions="[
                {
                  label: '导出 CSV',
                  type: 'primary',
                  icon: ACTION_ICON.DOWNLOAD,
                  auth: ['edu:level-check:export'],
                  onClick: exportSubmissions,
                },
                {
                  label: '刷新指标',
                  icon: ACTION_ICON.REFRESH,
                  auth: ['edu:level-check:query'],
                  onClick: refreshSubmission,
                },
              ]"
            />
          </template>

          <template #submissionLevel="{ row }">
            <Tag :color="levelColor(row.inferredLevel)">
              {{ labelLevel(row.inferredLevel) }}
            </Tag>
          </template>

          <template #teacherIds="{ row }">
            <div class="flex flex-wrap gap-1">
              <Tag v-for="id in row.recommendedTeacherIds || []" :key="id">
                #{{ id }}
              </Tag>
              <span v-if="!row.recommendedTeacherIds?.length" class="text-gray-400">
                -
              </span>
            </div>
          </template>

          <template #converted="{ row }">
            <Tag v-if="row.isConverted" color="green">已下单</Tag>
            <Tag v-else>未下单</Tag>
          </template>

          <template #answers="{ row }">
            <code class="text-xs">{{ JSON.stringify(row.answers || []) }}</code>
          </template>
        </SubmissionGrid>
      </Tabs.TabPane>
    </Tabs>
  </Page>
</template>
