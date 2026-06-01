<script lang="ts" setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';

const userStore = useUserStore();
const router = useRouter();

const nickname = computed(() => userStore.userInfo?.nickname || '管理员');

const quickEntries = [
  {
    description: '教师入驻资料与审核状态',
    title: '教师审核',
    to: '/edu/teacher',
  },
  {
    description: '套餐、订单、支付与退款',
    title: '课程交易',
    to: '/edu/course-order',
  },
  {
    description: '收入明细与提现审核',
    title: '教师结算',
    to: '/edu/withdrawal',
  },
  {
    description: '邮件、短信与站内信记录',
    title: '消息中心',
    to: '/system/messages/mail/mail-log',
  },
];

function openEntry(to: string) {
  router.push(to).catch(() => {});
}
</script>

<template>
  <div class="space-y-5 p-5">
    <section class="rounded-lg border border-border bg-card p-5 shadow-sm">
      <div class="flex flex-col gap-4 md:flex-row md:items-center">
        <img
          :src="userStore.userInfo?.avatar || preferences.app.defaultAvatar"
          alt="Mandarly Admin"
          class="size-16 rounded-full"
        />
        <div class="min-w-0 flex-1">
          <h1 class="text-xl font-semibold text-foreground">
            早安，{{ nickname }}
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Mandarly 运营后台已就绪。
          </p>
        </div>
      </div>
    </section>

    <section class="rounded-lg border border-border bg-card p-5 shadow-sm">
      <div class="mb-4 flex items-center justify-between gap-3">
        <h2 class="text-base font-semibold text-foreground">快捷入口</h2>
      </div>
      <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <button
          v-for="entry in quickEntries"
          :key="entry.title"
          class="rounded-lg border border-border bg-background p-4 text-left transition-colors hover:border-[#ffb627] hover:bg-[#ffb627]/10"
          type="button"
          @click="openEntry(entry.to)"
        >
          <span class="block text-sm font-semibold text-foreground">
            {{ entry.title }}
          </span>
          <span class="mt-2 block text-xs leading-5 text-muted-foreground">
            {{ entry.description }}
          </span>
        </button>
      </div>
    </section>

    <section class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_360px]">
      <div class="rounded-lg border border-border bg-card p-5 shadow-sm">
        <h2 class="text-base font-semibold text-foreground">今日待办</h2>
        <div
          class="mt-6 flex min-h-40 flex-col items-center justify-center rounded-lg border border-dashed border-border text-center"
        >
          <div class="text-sm font-medium text-foreground">暂无待处理事项</div>
          <div class="mt-2 text-xs text-muted-foreground">
            新的教师审核、退款或提现申请会在这里出现。
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-border bg-card p-5 shadow-sm">
        <h2 class="text-base font-semibold text-foreground">运营提示</h2>
        <div class="mt-4 space-y-3 text-sm leading-6 text-muted-foreground">
          <p>优先处理教师审核、退款审核和提现审核。</p>
          <p>邮件、短信异常请从消息中心查看发送记录。</p>
        </div>
      </div>
    </section>
  </div>
</template>
