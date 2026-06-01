<script setup>
// =============================================================================
// ProfileMenuMobile.vue — H5 < 768 主入口 menu list(类 iOS Settings)
//
// 设计源:
//   - docs/frontend/visual-reference/DESIGN-mandarly-v1.md § ProfileView H5 主入口
//
// 路由:仅在 /profile(空 path)且 H5 视口下渲染(由 ProfileLayout 控制)
//
// 入口:
//   §1 用户卡(头像 + 昵称 + 邮箱)
//   §2 账户 / 推荐战绩(profile 子域)
//   §3 我的套餐 / 我的订单 / 我的退款(独立路由,直达)
//   §4 教学统计(仅 role=teacher)
//   §5 退出登录(danger plain)
// =============================================================================
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import {
  User,
  Promotion,
  Box,
  Calendar,
  RefreshLeft,
  DataLine,
  ArrowRight,
  ArrowLeft as IconArrowLeft
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const emit = defineEmits(['logout'])

const { t, locale } = useI18n()
const userStore = useUserStore()
const { profile } = storeToRefs(userStore)

const isTeacher = computed(() => profile.value?.role === 'teacher')

// RTL 时 ▸ 改 ◂(列表 chevron 镜像)
const ChevronEnd = computed(() => (locale.value === 'ar' ? IconArrowLeft : ArrowRight))

const sections = computed(() => [
  {
    items: [
      {
        key: 'account',
        to: { name: 'ProfileAccount' },
        icon: User,
        label: t('profile.menu.account')
      },
      {
        key: 'referral',
        to: { name: 'ProfileReferral' },
        icon: Promotion,
        label: t('profile.menu.referral')
      }
    ]
  },
  {
    items: [
      {
        key: 'my-packages',
        to: { name: 'MyPackages' },
        icon: Box,
        label: t('profile.menu.myPackages')
      },
      {
        key: 'my-orders',
        to: { name: 'MyOrders' },
        icon: Calendar,
        label: t('profile.menu.myOrders')
      },
      {
        key: 'my-refunds',
        to: { name: 'MyRefunds' },
        icon: RefreshLeft,
        label: t('profile.menu.myRefunds')
      }
    ]
  },
  // 教师段(仅 role=teacher)
  ...(isTeacher.value
    ? [
        {
          items: [
            {
              key: 'teacher-stats',
              to: { name: 'ProfileTeacherStats' },
              icon: DataLine,
              label: t('profile.menu.teacherStats')
            }
          ]
        }
      ]
    : [])
])
</script>

<template>
  <div class="profile-menu-mobile">
    <!-- §1 顶部 page topbar(无返回箭头) — 与 H5 全局壳兼容,仅页面标题 -->
    <header class="profile-menu-mobile__topbar">
      <h1 class="profile-menu-mobile__title">{{ t('profile.title') }}</h1>
    </header>

    <!-- §2 用户卡 -->
    <section class="profile-menu-mobile__user-card">
      <div class="profile-menu-mobile__avatar" aria-hidden="true">
        <img
          v-if="profile?.avatar"
          :src="profile.avatar"
          :alt="profile?.nickname || ''"
          class="profile-menu-mobile__avatar-img"
        />
        <el-icon v-else class="profile-menu-mobile__avatar-fallback"><User /></el-icon>
      </div>
      <div class="profile-menu-mobile__user-meta">
        <p class="profile-menu-mobile__user-name" dir="auto">
          {{ profile?.nickname || t('profile.user.guest') }}
        </p>
        <p v-if="profile?.email" class="profile-menu-mobile__user-email">
          {{ profile.email }}
        </p>
      </div>
    </section>

    <!-- §3 入口 list 分段 -->
    <section
      v-for="(section, idx) in sections"
      :key="idx"
      class="profile-menu-mobile__section"
    >
      <ul class="profile-menu-mobile__list">
        <li v-for="item in section.items" :key="item.key" class="profile-menu-mobile__row-wrap">
          <router-link :to="item.to" class="profile-menu-mobile__row">
            <el-icon class="profile-menu-mobile__row-icon">
              <component :is="item.icon" />
            </el-icon>
            <span class="profile-menu-mobile__row-label">{{ item.label }}</span>
            <el-icon class="profile-menu-mobile__row-chevron" aria-hidden="true">
              <component :is="ChevronEnd" />
            </el-icon>
          </router-link>
        </li>
      </ul>
    </section>

    <!-- §4 退出登录 -->
    <div class="profile-menu-mobile__logout-wrap">
      <el-button
        type="danger"
        plain
        size="large"
        class="profile-menu-mobile__logout-btn"
        @click="emit('logout')"
      >
        {{ t('profile.menu.logout') }}
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">

.profile-menu-mobile {
  padding-block: brand.$spacing-3 brand.$spacing-12;
  padding-inline: brand.$spacing-4;
  min-height: 100vh;
  background: brand.$color-bg-page;
  box-sizing: border-box;

  &__topbar {
    padding-block: brand.$spacing-3;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-xl;
    font-weight: 700;
    color: brand.$color-text-primary;
  }

  &__user-card {
    display: flex;
    align-items: center;
    gap: brand.$spacing-4;
    padding: brand.$spacing-5;
    background: brand.$color-bg-card;
    border-radius: brand.$radius-lg;
    margin-block-end: brand.$spacing-4;
    box-shadow: brand.$shadow-base;
  }

  &__avatar {
    width: 64px;
    height: 64px;
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
    font-size: brand.$font-size-lg;
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

  &__section {
    margin-block-end: brand.$spacing-4;
    background: brand.$color-bg-card;
    border-radius: brand.$radius-lg;
    overflow: hidden;
    box-shadow: brand.$shadow-base;
  }

  &__list {
    margin: 0;
    padding: 0;
    list-style: none;
  }

  &__row-wrap {
    border-block-end: 1px solid brand.$color-border-light;

    &:last-child {
      border-block-end: none;
    }
  }

  &__row {
    display: flex;
    align-items: center;
    gap: brand.$spacing-3;
    padding: brand.$spacing-4 brand.$spacing-5;
    text-decoration: none;
    color: brand.$color-text-primary;

    &:active {
      background: brand.$brand-primary-soft;
    }
  }

  &__row-icon {
    font-size: brand.$font-size-xl;
    color: brand.$brand-primary-deep;
    flex-shrink: 0;
  }

  &__row-label {
    flex: 1;
    font-size: brand.$font-size-md;
  }

  &__row-chevron {
    font-size: brand.$font-size-base;
    color: brand.$color-text-tertiary;
    flex-shrink: 0;
  }

  &__logout-wrap {
    margin-block-start: brand.$spacing-8;
    display: flex;
    justify-content: center;
  }

  &__logout-btn {
    min-width: 200px;
  }
}
</style>
