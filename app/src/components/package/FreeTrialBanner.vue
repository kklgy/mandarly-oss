<script setup>
/**
 * FreeTrialBanner.vue — § B 免费体验 banner
 *
 * 设计稿: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § PackageListView § B
 *
 * 显隐:
 *   - 未登录 → 默认显示, 点击跳 /register?redirect=/booking/free-trial
 *   - 登录 + claimed=false → 显示, 点击跳 /booking/free-trial
 *   - 登录 + claimed=true → 整体 v-if 隐藏(由调用方控制)
 *
 * 主色 ribbon:
 *   - background: brand.$brand-primary-soft
 *   - border-inline-start: 4px solid brand.$brand-primary
 *   - border-radius: brand.$radius-lg
 */
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  /** 已领取(true 时父级应 v-if 隐藏整组件; 此 prop 仅用于 disabled tooltip 兜底) */
  claimed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['claim'])

const router = useRouter()
const { t, te } = useI18n()
const tt = (key, fb) => (te(key) ? t(key) : fb)
const userStore = useUserStore()

const text = computed(() => tt('packages.banner.freeTrial.text', '注册即送 1 节免费体验课(25 分钟)'))
const subtext = computed(() => tt('packages.banner.freeTrial.subtext', '自由选老师 · 无需绑定信用卡'))
const ctaText = computed(() => {
  if (props.claimed) return tt('packages.banner.freeTrial.claimed', '您已领取过免费体验课')
  return tt('packages.banner.freeTrial.cta', '立即领取')
})

function handleClick() {
  if (props.claimed) return
  emit('claim')
  if (!userStore.isLoggedIn) {
    router.push('/register?redirect=/booking/free-trial')
  } else {
    router.push('/booking/free-trial')
  }
}
</script>

<template>
  <section
    class="free-trial-banner"
    :class="{ 'is-claimed': claimed }"
    role="region"
    :aria-label="text"
  >
    <div class="free-trial-banner__icon" aria-hidden="true">
      🎁
    </div>
    <div class="free-trial-banner__body">
      <p class="free-trial-banner__text">{{ text }}</p>
      <p class="free-trial-banner__subtext">{{ subtext }}</p>
    </div>
    <el-button
      type="primary"
      class="free-trial-banner__cta"
      :disabled="claimed"
      @click="handleClick"
    >
      {{ ctaText }}
    </el-button>
  </section>
</template>

<style scoped lang="scss">
.free-trial-banner {
  display: flex;
  align-items: center;
  gap: brand.$spacing-4;
  padding-block: brand.$spacing-4;
  padding-inline: brand.$spacing-5;
  background: brand.$brand-primary-soft;
  border-inline-start: 4px solid brand.$brand-primary;
  border-radius: brand.$radius-lg;
  margin-block-end: brand.$spacing-8;

  @media (max-width: brand.$bp-tablet) {
    flex-wrap: wrap;
    padding-block: brand.$spacing-3;
    padding-inline: brand.$spacing-4;
    margin-block-end: brand.$spacing-6;
    gap: brand.$spacing-3;
  }

  &__icon {
    font-size: brand.$font-size-2xl;
    flex-shrink: 0;
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__text {
    margin: 0;
    font-size: brand.$font-size-md;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__subtext {
    margin: brand.$spacing-1 0 0;
    font-size: brand.$font-size-base;
    color: brand.$color-text-secondary;
  }

  &__cta {
    flex-shrink: 0;

    @media (max-width: brand.$bp-tablet) {
      width: 100%;
    }
  }

  &.is-claimed {
    opacity: 0.7;
  }
}
</style>
