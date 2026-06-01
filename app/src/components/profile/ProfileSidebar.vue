<script setup>
// =============================================================================
// <ProfileSidebar> — PC 左侧菜单(/profile 双栏布局左栏)
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § ProfileView 信息架构拆分
//     (PC ≥ 1024 视图,左栏 240px sticky)
//
// 用途:
//   - PC 端(≥768) /profile/* 所有子页共用左侧菜单
//   - active 项主色 soft 底 + 主色文字 + 4px 主色左边线
//   - 头像区点击跳 /profile/account
//   - teacherStats 仅 role=teacher 显示
//
// 严格约束:
//   1) token only — 无硬编色 / px / 字号
//   2) RTL — logical properties(border-inline-start)
//   3) 路由用 <router-link>,active 由当前 route name 判断
// =============================================================================
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import {
  User,
  Promotion,
  Box,
  Calendar,
  RefreshLeft,
  DataLine,
  SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

defineEmits(['logout'])

const { t } = useI18n()
const route = useRoute()
const userStore = useUserStore()
const { profile } = storeToRefs(userStore)

const isTeacher = computed(() => profile.value?.role === 'teacher')

// 菜单项
const menuItems = computed(() => {
  const items = [
    {
      key: 'account',
      to: { name: 'ProfileAccount' },
      icon: User,
      label: t('profile.menu.account'),
      // ProfileAccount 是 path '' 和 'account' 的 name(后者) — 这里我们用 name 比较
      activeNames: ['ProfileAccount', 'Profile']
    },
    {
      key: 'referral',
      to: { name: 'ProfileReferral' },
      icon: Promotion,
      label: t('profile.menu.referral'),
      activeNames: ['ProfileReferral']
    },
    {
      key: 'my-packages',
      to: { name: 'MyPackages' },
      icon: Box,
      label: t('profile.menu.myPackages'),
      activeNames: ['MyPackages']
    },
    {
      key: 'my-orders',
      to: { name: 'MyOrders' },
      icon: Calendar,
      label: t('profile.menu.myOrders'),
      activeNames: ['MyOrders']
    },
    {
      key: 'my-refunds',
      to: { name: 'MyRefunds' },
      icon: RefreshLeft,
      label: t('profile.menu.myRefunds'),
      activeNames: ['MyRefunds']
    }
  ]
  if (isTeacher.value) {
    items.push({
      key: 'teacher-stats',
      to: { name: 'ProfileTeacherStats' },
      icon: DataLine,
      label: t('profile.menu.teacherStats'),
      activeNames: ['ProfileTeacherStats']
    })
  }
  return items
})

function isActive(item) {
  return item.activeNames.includes(route.name)
}
</script>

<template>
  <aside class="profile-sidebar" aria-label="profile-nav">
    <!-- 头像区(点击跳 /profile/account) -->
    <router-link :to="{ name: 'ProfileAccount' }" class="profile-sidebar__user">
      <div class="profile-sidebar__avatar" aria-hidden="true">
        <img
          v-if="profile?.avatarUrl"
          :src="profile.avatarUrl"
          :alt="profile?.nickname || ''"
          class="profile-sidebar__avatar-img"
        />
        <el-icon v-else class="profile-sidebar__avatar-fallback"><User /></el-icon>
      </div>
      <div class="profile-sidebar__user-meta">
        <p class="profile-sidebar__user-name" dir="auto">
          {{ profile?.nickname || t('profile.user.guest') }}
        </p>
        <p v-if="profile?.email" class="profile-sidebar__user-email">{{ profile.email }}</p>
      </div>
    </router-link>

    <hr class="profile-sidebar__divider" />

    <!-- 菜单 -->
    <nav class="profile-sidebar__nav">
      <router-link
        v-for="item in menuItems"
        :key="item.key"
        :to="item.to"
        class="profile-sidebar__item"
        :class="{ 'is-active': isActive(item) }"
      >
        <el-icon class="profile-sidebar__item-icon">
          <component :is="item.icon" />
        </el-icon>
        <span class="profile-sidebar__item-label">{{ item.label }}</span>
      </router-link>
    </nav>

    <hr class="profile-sidebar__divider" />

    <!-- 退出登录 -->
    <button
      type="button"
      class="profile-sidebar__item profile-sidebar__item--danger"
      @click="$emit('logout')"
    >
      <el-icon class="profile-sidebar__item-icon"><SwitchButton /></el-icon>
      <span class="profile-sidebar__item-label">{{ t('profile.menu.logout') }}</span>
    </button>
  </aside>
</template>

<style scoped lang="scss">

.profile-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: brand.$color-bg-card;
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-4;
  box-shadow: brand.$shadow-base;
  position: sticky;
  inset-block-start: 100px; // AppHeader 64 + 36 呼吸
  align-self: flex-start;

  &__user {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    padding: brand.$spacing-3;
    border-radius: brand.$radius-base;
    text-decoration: none;
    color: inherit;
    transition: background 0.18s ease;

    &:hover {
      background: brand.$brand-primary-soft;
    }
  }

  &__avatar {
    width: 56px;
    height: 56px;
    border-radius: brand.$radius-full;
    background: brand.$color-bg-strong;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
  }

  &__avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__avatar-fallback {
    font-size: brand.$font-size-2xl;
    color: brand.$color-text-tertiary;
  }

  &__user-meta {
    flex: 1;
    min-width: 0;
  }

  &__user-name {
    margin: 0;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: brand.$color-text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__user-email {
    margin: brand.$spacing-1 0 0;
    font-size: brand.$font-size-sm;
    color: brand.$color-text-tertiary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__divider {
    border: none;
    border-block-start: 1px solid brand.$color-border;
    margin: brand.$spacing-3 0;
  }

  &__nav {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-1;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    padding: brand.$spacing-3;
    border-radius: brand.$radius-base;
    text-decoration: none;
    color: brand.$color-text-primary;
    background: transparent;
    border: none;
    border-inline-start: 4px solid transparent;
    cursor: pointer;
    font: inherit;
    text-align: start;
    width: 100%;
    box-sizing: border-box;
    transition: background 0.18s ease, color 0.18s ease;

    &:hover {
      background: brand.$brand-primary-soft;
      color: brand.$brand-primary-deep;
    }

    &.is-active {
      background: brand.$brand-primary-soft;
      color: brand.$brand-primary-deep;
      border-inline-start-color: brand.$brand-primary;
      font-weight: 600;

      .profile-sidebar__item-icon {
        color: brand.$brand-primary-deep;
      }
    }

    &--danger {
      color: brand.$color-error;

      &:hover {
        background: brand.$color-error-soft;
        color: brand.$color-error;
      }
    }
  }

  &__item-icon {
    font-size: brand.$font-size-lg;
    color: brand.$color-text-secondary;
  }

  &__item-label {
    font-size: brand.$font-size-base;
    flex: 1;
  }
}
</style>
