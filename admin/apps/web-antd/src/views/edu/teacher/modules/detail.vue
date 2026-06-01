<script lang="ts" setup>
import type { EduTeacherProfileApi } from '#/api/edu/teacher-profile';
import type { EduTeacherQualificationApi } from '#/api/edu/teacher-qualification';

import { computed, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import {
  Button,
  Descriptions,
  DescriptionsItem,
  message,
  Space,
  Tag,
} from 'ant-design-vue';

import {
  auditTeacherProfile,
  getTeacherProfile,
} from '#/api/edu/teacher-profile';
import { getTeacherQualifications } from '#/api/edu/teacher-qualification';

import {
  ACCENT_LABEL,
  AUDIT_STATUS_COLOR,
  AUDIT_STATUS_LABEL,
  DOC_TYPE_LABEL,
} from '../data';
import RejectForm from './reject-form.vue';

const emit = defineEmits(['success']);
const profile = ref<EduTeacherProfileApi.TeacherProfile>();
const qualifications = ref<EduTeacherQualificationApi.TeacherQualification[]>(
  [],
);
const loading = ref(false);

const [RejectFormModal, rejectFormModalApi] = useVbenModal({
  connectedComponent: RejectForm,
  destroyOnClose: true,
});

const isPending = computed(() => profile.value?.auditStatus === 'pending');

const auditStatusText = computed(() => {
  const s = profile.value?.auditStatus;
  return s ? AUDIT_STATUS_LABEL[s] || s : '-';
});

const auditStatusColor = computed(() => {
  const s = profile.value?.auditStatus;
  return s ? AUDIT_STATUS_COLOR[s] || 'default' : 'default';
});

const accentText = computed(() => {
  const a = profile.value?.accent;
  return a ? ACCENT_LABEL[a] || a : '-';
});

/** 资质行的 docType 文案 */
function docTypeText(t: string) {
  return DOC_TYPE_LABEL[t] || t;
}

/** 资质行的审核状态文案 */
function qualAuditStatusText(s: string) {
  return AUDIT_STATUS_LABEL[s] || s;
}

/** 资质行的审核状态 tag color */
function qualAuditStatusColor(s: string) {
  return AUDIT_STATUS_COLOR[s] || 'default';
}

/** 通过审核 */
async function handleApprove() {
  if (!profile.value?.userId) return;
  await confirm('确认通过该教师审核?');
  drawerApi.lock();
  try {
    await auditTeacherProfile({
      userId: profile.value.userId,
      action: 'approve',
    });
    message.success('已通过');
    emit('success');
    await drawerApi.close();
  } finally {
    drawerApi.unlock();
  }
}

/** 打开驳回弹窗 */
function handleOpenReject() {
  if (!profile.value?.userId) return;
  rejectFormModalApi.setData(profile.value).open();
}

/** 驳回成功后,关闭抽屉并通知父刷新 */
function handleRejectSuccess() {
  emit('success');
  drawerApi.close();
}

const [Drawer, drawerApi] = useVbenDrawer({
  title: '教师审核详情',
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      profile.value = undefined;
      qualifications.value = [];
      return;
    }
    const data = drawerApi.getData<{ userId: number }>();
    if (!data?.userId) {
      return;
    }
    loading.value = true;
    try {
      profile.value = await getTeacherProfile(data.userId);
      // 并行不强制 — 拉 qualification 失败不影响 profile 展示
      try {
        qualifications.value = await getTeacherQualifications(data.userId);
      } catch {
        qualifications.value = [];
      }
    } finally {
      loading.value = false;
    }
  },
});
</script>

<template>
  <Drawer class="w-2/5">
    <RejectFormModal @success="handleRejectSuccess" />

    <div v-if="loading" class="p-4 text-center text-gray-400">加载中...</div>

    <div v-else-if="profile" class="space-y-4">
      <!-- 自我介绍视频 -->
      <div v-if="profile.introVideoUrl">
        <div class="mb-2 font-medium">自我介绍视频</div>
        <video
          :src="profile.introVideoUrl"
          controls
          class="w-full max-h-72 rounded bg-black"
        />
        <div class="mt-1 text-xs text-gray-400">
          <span v-if="profile.introVideoSize">
            大小: {{ (profile.introVideoSize / 1024 / 1024).toFixed(2) }} MB
          </span>
          <span v-if="profile.introVideoUploadedAt" class="ml-2">
            上传时间: {{ formatDateTime(profile.introVideoUploadedAt) }}
          </span>
        </div>
      </div>

      <Descriptions :column="2" bordered size="small">
        <DescriptionsItem label="用户ID">
          {{ profile.userId }}
        </DescriptionsItem>
        <DescriptionsItem label="手机号">
          <span dir="ltr">{{ profile.phone || '-' }}</span>
        </DescriptionsItem>
        <DescriptionsItem label="审核状态">
          <Tag :color="auditStatusColor">{{ auditStatusText }}</Tag>
        </DescriptionsItem>
        <DescriptionsItem label="级别">
          {{ profile.level || '-' }}
        </DescriptionsItem>
        <DescriptionsItem label="口音">
          {{ accentText }}
        </DescriptionsItem>
        <DescriptionsItem label="教龄(年)">
          {{ profile.yearsExperience ?? '-' }}
        </DescriptionsItem>
        <DescriptionsItem label="创建时间">
          {{ profile.createTime ? formatDateTime(profile.createTime) : '-' }}
        </DescriptionsItem>
        <DescriptionsItem label="擅长领域" :span="2">
          <Space :size="[4, 4]" wrap>
            <Tag
              v-for="item in profile.expertise || []"
              :key="item"
              color="blue"
            >
              {{ item }}
            </Tag>
            <span
              v-if="!profile.expertise || profile.expertise.length === 0"
              class="text-gray-400"
            >
              -
            </span>
          </Space>
        </DescriptionsItem>
        <DescriptionsItem label="教学语言" :span="2">
          <Space :size="[4, 4]" wrap>
            <Tag
              v-for="item in profile.languages || []"
              :key="item"
              color="cyan"
            >
              {{ item }}
            </Tag>
            <span
              v-if="!profile.languages || profile.languages.length === 0"
              class="text-gray-400"
            >
              -
            </span>
          </Space>
        </DescriptionsItem>
        <DescriptionsItem label="自我介绍" :span="2">
          <div class="whitespace-pre-wrap">{{ profile.intro || '-' }}</div>
        </DescriptionsItem>
        <DescriptionsItem
          v-if="profile.auditStatus === 'rejected' && profile.rejectReason"
          label="驳回原因"
          :span="2"
        >
          <div class="whitespace-pre-wrap text-red-500">
            {{ profile.rejectReason }}
          </div>
        </DescriptionsItem>
        <DescriptionsItem
          v-if="profile.auditedAt"
          label="审核时间"
        >
          {{ formatDateTime(profile.auditedAt) }}
        </DescriptionsItem>
        <DescriptionsItem v-if="profile.auditedBy" label="审核人ID">
          {{ profile.auditedBy }}
        </DescriptionsItem>
      </Descriptions>

      <!-- 资质材料(15min 签名 URL 预览) -->
      <Descriptions
        title="资质材料"
        :column="1"
        bordered
        size="small"
      >
        <DescriptionsItem v-if="qualifications.length === 0" label="状态">
          <span class="text-gray-400">暂无</span>
        </DescriptionsItem>
        <DescriptionsItem
          v-for="q in qualifications"
          :key="q.id"
          :label="docTypeText(q.docType)"
        >
          <Space :size="[8, 4]" wrap>
            <a
              :href="q.docUrl"
              target="_blank"
              rel="noopener noreferrer"
            >
              {{ q.docFilename || '(无文件名)' }}
            </a>
            <Tag :color="qualAuditStatusColor(q.auditStatus)">
              {{ qualAuditStatusText(q.auditStatus) }}
            </Tag>
            <span
              v-if="q.auditStatus === 'rejected' && q.rejectReason"
              class="text-red-500"
            >
              原因: {{ q.rejectReason }}
            </span>
            <span class="text-xs text-gray-400">
              上传: {{ formatDateTime(q.createTime) }}
            </span>
          </Space>
        </DescriptionsItem>
      </Descriptions>
    </div>

    <div v-else class="p-4 text-center text-gray-400">暂无数据</div>

    <template #footer>
      <Space v-if="isPending">
        <Button danger @click="handleOpenReject">驳回</Button>
        <Button type="primary" @click="handleApprove">通过</Button>
      </Space>
      <Button v-else @click="drawerApi.close()">关闭</Button>
    </template>
  </Drawer>
</template>
