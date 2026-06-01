<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduUserApi } from '#/api/edu/user';

import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

import { confirm, Page } from '@vben/common-ui';

import { Descriptions, Drawer, message, Space, Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { freezeUser, getUser, getUserPage } from '#/api/edu/user';

import {
  AUDIT_STATUS_COLOR,
  AUDIT_STATUS_LABEL,
  ROLE_COLOR,
  ROLE_LABEL,
  STATUS_COLOR,
  STATUS_LABEL,
  useGridColumns,
  useGridFormSchema,
} from './data';

const router = useRouter();
const detailOpen = ref(false);
const detailLoading = ref(false);
const currentUser = ref<EduUserApi.User>();

const detailTitle = computed(() => {
  const user = currentUser.value;
  if (!user) return '用户详情';
  return `用户详情 #${user.id}`;
});

function handleRefresh() {
  gridApi.query();
}

async function handleDetail(row: EduUserApi.User) {
  detailOpen.value = true;
  detailLoading.value = true;
  try {
    currentUser.value = await getUser(row.id);
  } finally {
    detailLoading.value = false;
  }
}

async function handleFreeze(row: EduUserApi.User) {
  const isFrozen = row.status === 'frozen';
  const action = isFrozen ? 'unfreeze' : 'freeze';
  await confirm(`确认${isFrozen ? '解冻' : '冻结'}该用户?`);
  const hideLoading = message.loading({ content: '提交中...', duration: 0 });
  try {
    await freezeUser({
      userId: row.id,
      action,
      reason: isFrozen ? 'admin user management unfreeze' : 'admin user management freeze',
    });
    message.success(isFrozen ? '已解冻' : '已冻结');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

function goCourseOrders(row: EduUserApi.User) {
  const query =
    row.role === 'teacher' ? { teacherId: row.id } : { studentId: row.id };
  router.push({ name: 'EduCourseOrder', query });
}

function goPackage(row: EduUserApi.User) {
  router.push({ name: 'EduPackage', query: { userId: row.id } });
}

function goTeacherProfile(row: EduUserApi.User) {
  router.push({ name: 'EduTeacher', query: { userId: row.id } });
}

function renderFallback(value?: null | number | string) {
  return value === null || value === undefined || value === '' ? '-' : value;
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getUserPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
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
  } as VxeTableGridOptions<EduUserApi.User>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="C 端用户列表">
      <template #role="{ row }">
        <Tag :color="ROLE_COLOR[row.role] || 'default'">
          {{ ROLE_LABEL[row.role] || row.role }}
        </Tag>
      </template>

      <template #status="{ row }">
        <Tag :color="STATUS_COLOR[row.status] || 'default'">
          {{ STATUS_LABEL[row.status] || row.status }}
        </Tag>
      </template>

      <template #oauthSummary="{ row }">
        <Space v-if="row.oauthProviders?.length" :size="4" wrap>
          <Tag v-for="provider in row.oauthProviders" :key="provider">
            {{ provider }}
          </Tag>
        </Space>
        <span v-else class="text-gray-400">未绑定</span>
      </template>

      <template #teacherAuditStatus="{ row }">
        <Tag
          v-if="row.teacherAuditStatus"
          :color="AUDIT_STATUS_COLOR[row.teacherAuditStatus] || 'default'"
        >
          {{
            AUDIT_STATUS_LABEL[row.teacherAuditStatus] ||
            row.teacherAuditStatus
          }}
        </Tag>
        <span v-else class="text-gray-400">-</span>
      </template>

      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['edu:user:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: row.status === 'frozen' ? '解冻' : '冻结',
              type: 'link',
              danger: row.status !== 'frozen',
              auth: ['edu:user:freeze'],
              onClick: handleFreeze.bind(null, row),
            },
            {
              label: '订单',
              type: 'link',
              auth: ['edu:course-order:query'],
              onClick: goCourseOrders.bind(null, row),
            },
            {
              label: '套餐',
              type: 'link',
              ifShow: row.role === 'student',
              auth: ['edu:package:query'],
              onClick: goPackage.bind(null, row),
            },
            {
              label: '教师档案',
              type: 'link',
              ifShow: row.role === 'teacher',
              auth: ['edu:teacher-profile:query'],
              onClick: goTeacherProfile.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>

    <Drawer
      v-model:open="detailOpen"
      :title="detailTitle"
      width="720"
      :loading="detailLoading"
    >
      <Descriptions v-if="currentUser" bordered :column="2" size="small">
        <Descriptions.Item label="用户ID">
          {{ currentUser.id }}
        </Descriptions.Item>
        <Descriptions.Item label="角色">
          {{ ROLE_LABEL[currentUser.role] || currentUser.role }}
        </Descriptions.Item>
        <Descriptions.Item label="状态">
          {{ STATUS_LABEL[currentUser.status] || currentUser.status }}
        </Descriptions.Item>
        <Descriptions.Item label="昵称">
          {{ renderFallback(currentUser.nickname) }}
        </Descriptions.Item>
        <Descriptions.Item label="邮箱">
          {{ renderFallback(currentUser.email) }}
        </Descriptions.Item>
        <Descriptions.Item label="邮箱验证">
          {{ renderFallback(currentUser.emailVerifiedAt) }}
        </Descriptions.Item>
        <Descriptions.Item label="手机号">
          {{ renderFallback(currentUser.phone) }}
        </Descriptions.Item>
        <Descriptions.Item label="手机验证">
          {{ renderFallback(currentUser.phoneVerifiedAt) }}
        </Descriptions.Item>
        <Descriptions.Item label="语言">
          {{ renderFallback(currentUser.locale) }}
        </Descriptions.Item>
        <Descriptions.Item label="时区">
          {{ renderFallback(currentUser.timezone) }}
        </Descriptions.Item>
        <Descriptions.Item label="推荐码">
          {{ renderFallback(currentUser.referralCode) }}
        </Descriptions.Item>
        <Descriptions.Item label="推荐人">
          {{ renderFallback(currentUser.referredBy) }}
        </Descriptions.Item>
        <Descriptions.Item label="OAuth">
          {{ currentUser.oauthSummary || '未绑定' }}
        </Descriptions.Item>
        <Descriptions.Item label="最后登录">
          {{ renderFallback(currentUser.lastLoginAt) }}
        </Descriptions.Item>
        <Descriptions.Item label="最后登录 IP">
          {{ renderFallback(currentUser.lastLoginIp) }}
        </Descriptions.Item>
        <Descriptions.Item label="注册时间">
          {{ renderFallback(currentUser.createTime) }}
        </Descriptions.Item>
        <Descriptions.Item label="学习目标" :span="2">
          {{ renderFallback(currentUser.learningGoal) }}
        </Descriptions.Item>
        <Descriptions.Item
          v-if="currentUser.role === 'teacher'"
          label="教师档案"
          :span="2"
        >
          <Space wrap>
            <Tag
              v-if="currentUser.teacherAuditStatus"
              :color="
                AUDIT_STATUS_COLOR[currentUser.teacherAuditStatus] || 'default'
              "
            >
              {{
                AUDIT_STATUS_LABEL[currentUser.teacherAuditStatus] ||
                currentUser.teacherAuditStatus
              }}
            </Tag>
            <span>口音: {{ renderFallback(currentUser.teacherAccent) }}</span>
            <span>
              教龄: {{ renderFallback(currentUser.teacherYearsExperience) }}
            </span>
            <span>
              方向:
              {{
                currentUser.teacherExpertise?.length
                  ? currentUser.teacherExpertise.join(' / ')
                  : '-'
              }}
            </span>
          </Space>
        </Descriptions.Item>
      </Descriptions>
    </Drawer>
  </Page>
</template>
