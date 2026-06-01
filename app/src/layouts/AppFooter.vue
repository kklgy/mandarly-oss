<script setup>
/**
 * AppFooter — 仅 PC ≥ 768 + 公开营销页(/, /teachers, /teacher/:id, /level-check, /legal/*)
 *
 * 装配条件由 AppLayout 控制(本组件不做视口判断)。
 * 4 列链接 + 公司主体 + 静态支付方式 + 社交占位
 *
 * 公司主体单一真实源:utils/company.js(plan Wave 1.5)— 禁止硬编码公司名/地址/版权
 */
import { computed, ref, onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  COMPANY_NAME_ZH,
  COMPANY_NAME_EN,
  COMPANY_CONTACT_EMAIL,
  getCopyrightLine,
  getCompanyAddress
} from '@/utils/company'
import { getFooterSocialLinks } from '@/config/socials'
import { useUserStore } from '@/stores/user'
import wechatSearchImg from '@/assets/social/wechat-search-style-a-transparent.png'

const paymentMethods = [
  { key: 'bank', name: 'Bank Transfer', mark: 'BANK', label: 'Transfer' },
  { key: 'visa', name: 'Visa', mark: 'VISA', label: 'Visa' },
  { key: 'mastercard', name: 'Mastercard', mark: 'Mastercard', label: 'Mastercard', symbol: 'mastercard' },
  { key: 'amex', name: 'American Express', mark: 'AMERICAN', label: 'EXPRESS' },
  { key: 'discover', name: 'Discover', mark: 'DISCOVER', label: 'Discover' },
  { key: 'paypal', name: 'PayPal', mark: 'PayPal', label: 'PayPal' },
  { key: 'alipay', name: 'Alipay', mark: 'Alipay', label: '支付宝' },
  { key: 'wechatPay', name: 'WeChat Pay', mark: 'WeChat', label: 'Pay' },
  { key: 'applePay', name: 'Apple Pay', mark: 'Apple', label: 'Pay' },
  { key: 'googlePay', name: 'Google Pay', mark: 'Google', label: 'Pay' }
]

// D27:微信 QR 点击放大 + "联系方式" 触发客服弹窗
const qrModalOpen = ref(false)

function openWechatQr() {
  qrModalOpen.value = true
}

function closeWechatQr() {
  qrModalOpen.value = false
}

function openSupportWidget() {
  window.dispatchEvent(new Event('mandarly:open-support'))
}

// ESC 关闭 modal
function onKeydown(e) {
  if (e.key === 'Escape' && qrModalOpen.value) qrModalOpen.value = false
}
watch(qrModalOpen, (open) => {
  if (open) {
    window.addEventListener('keydown', onKeydown)
    document.documentElement.style.overflow = 'hidden'
  } else {
    window.removeEventListener('keydown', onKeydown)
    document.documentElement.style.overflow = ''
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
  document.documentElement.style.overflow = ''
})

const { t, locale } = useI18n()
const userStore = useUserStore()

const copyrightLine = computed(() => getCopyrightLine(locale.value))
const companyAddress = computed(() => getCompanyAddress(locale.value))
const socialLinks = computed(() => getFooterSocialLinks().filter((item) => item.key !== 'wechat'))
const serviceLinks = computed(() => {
  const links = []
  if (userStore.isLoggedIn) {
    links.push(
      { to: '/teachers', label: t('header.nav.teachers') },
      { to: '/packages', label: t('header.nav.packages') }
    )
  }
  links.push({ to: '/level-check', label: t('header.nav.levelCheck') })
  return links
})
</script>

<template>
  <footer class="app-footer" role="contentinfo">
    <div class="app-footer__inner">
      <div class="app-footer__top">
        <!-- 品牌 + 公司主体 -->
        <div class="app-footer__brand">
          <div class="app-footer__brand-logo">
            <img src="/logo-256.png?v=v20260526-mark-only" alt="Mandarly" class="app-footer__logo" @error="(e) => e.target.style.display='none'" />
            <span class="app-footer__brand-name">Mandarly</span>
          </div>
          <div class="app-footer__entity" aria-label="Mandarly company entity">
            <span>{{ COMPANY_NAME_ZH }}</span>
            <span class="app-footer__entity-en">{{ COMPANY_NAME_EN }}</span>
          </div>
        </div>

        <!-- 4 列链接 -->
        <div class="app-footer__columns">
          <section class="app-footer__col">
            <h3 class="app-footer__col-title">{{ t('footer.section.about') }}</h3>
            <ul>
              <!-- TODO M5.5:/legal/about /jobs 占位页未做,P0 先链 # -->
              <li><a href="#" class="app-footer__link is-disabled" aria-disabled="true">{{ t('footer.about.intro') }}</a></li>
              <li><router-link to="/teacher/register" class="app-footer__link">{{ t('footer.about.teacherJoin') }}</router-link></li>
              <!-- D27:联系方式 → 触发客服弹窗 mandarly:open-support event -->
              <li><button type="button" class="app-footer__link app-footer__link--btn" @click="openSupportWidget">{{ t('footer.about.contact') }}</button></li>
            </ul>
          </section>

          <section class="app-footer__col">
            <h3 class="app-footer__col-title">{{ t('footer.section.service') }}</h3>
            <ul>
              <li v-for="item in serviceLinks" :key="item.to">
                <router-link :to="item.to" class="app-footer__link">{{ item.label }}</router-link>
              </li>
            </ul>
          </section>

          <section class="app-footer__col">
            <h3 class="app-footer__col-title">{{ t('footer.section.legal') }}</h3>
            <ul>
              <li><router-link to="/legal/privacy" class="app-footer__link">{{ t('footer.legal.privacy') }}</router-link></li>
              <li><router-link to="/legal/terms" class="app-footer__link">{{ t('footer.legal.terms') }}</router-link></li>
            </ul>
          </section>

          <section class="app-footer__col">
            <h3 class="app-footer__col-title">{{ t('footer.section.contact') }}</h3>
            <ul>
              <li>
                <a :href="`mailto:${COMPANY_CONTACT_EMAIL}`" class="app-footer__link">
                  {{ COMPANY_CONTACT_EMAIL }}
                </a>
              </li>
              <li class="app-footer__address">{{ companyAddress }}</li>
            </ul>
          </section>
        </div>
      </div>

      <div class="app-footer__engagement">
        <section class="app-footer__payments" :aria-label="t('footer.section.payment')">
          <h3 class="app-footer__col-title">{{ t('footer.section.payment') }}</h3>
          <ul class="app-footer__payment-grid">
            <li
              v-for="item in paymentMethods"
              :key="item.key"
              class="app-footer__payment-card"
              :class="`app-footer__payment-card--${item.key}`"
              :aria-label="item.name"
            >
              <span class="app-footer__payment-mark" aria-hidden="true">
                <span v-if="item.symbol === 'mastercard'" class="app-footer__payment-circles">
                  <i></i>
                  <i></i>
                </span>
                <span v-else>{{ item.mark }}</span>
              </span>
              <span class="app-footer__payment-label">{{ item.label }}</span>
            </li>
          </ul>
        </section>

        <section class="app-footer__follow-panel">
          <h3 class="app-footer__col-title">{{ t('footer.section.follow') }}</h3>
          <div class="app-footer__follow-row">
            <ul class="app-footer__social">
              <li v-for="item in socialLinks" :key="item.key">
                <a
                  v-if="item.url"
                  :href="item.url"
                  class="app-footer__social-link"
                  target="_blank"
                  rel="noopener noreferrer"
                  :aria-label="t('footer.social.followOn', { name: item.displayName })"
                  :title="item.displayName"
                >
                  <svg
                    v-if="item.iconPath"
                    class="app-footer__social-icon"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                    focusable="false"
                  >
                    <path :d="item.iconPath" />
                  </svg>
                  <span v-else class="app-footer__social-text">{{ item.shortLabel }}</span>
                </a>
                <button
                  v-else-if="item.key === 'wechat'"
                  type="button"
                  class="app-footer__social-link app-footer__social-link--button"
                  :aria-label="t('footer.social.wechatZoomHint')"
                  :title="item.displayName"
                  @click="openWechatQr"
                >
                  <svg
                    class="app-footer__social-icon"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                    focusable="false"
                  >
                    <path :d="item.iconPath" />
                  </svg>
                </button>
                <span
                  v-else
                  class="app-footer__social-link app-footer__social-link--placeholder"
                  role="img"
                  :aria-label="item.displayName"
                  :title="item.displayName"
                >
                  <svg
                    v-if="item.iconPath"
                    class="app-footer__social-icon"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                    focusable="false"
                  >
                    <path :d="item.iconPath" />
                  </svg>
                  <span v-else class="app-footer__social-text">{{ item.shortLabel }}</span>
                </span>
              </li>
            </ul>
            <!-- D27:点击 QR 放大查看(用户反馈缩略图看不清) -->
            <figure class="app-footer__wechat" :aria-label="t('footer.social.wechatScan')">
              <button
                type="button"
                class="app-footer__wechat-btn"
                :aria-label="t('footer.social.wechatZoomHint')"
                @click="openWechatQr"
              >
                <img
                  :src="wechatSearchImg"
                  alt="Mandarly WeChat search and QR"
                  class="app-footer__wechat-img"
                  loading="lazy"
                />
              </button>
            </figure>
          </div>
        </section>
      </div>
    </div>

    <!-- 分隔线 + 版权行(D27:语言切换统一到顶栏,Footer 不再重复展示) -->
    <div class="app-footer__legal">
      <div class="app-footer__legal-inner">
        <p class="app-footer__copyright">{{ copyrightLine }}</p>
      </div>
    </div>

    <!-- D27:微信 QR 大图 modal — 全屏遮罩 + 居中放大 -->
    <Teleport to="body">
      <transition name="wechat-modal-fade">
        <div
          v-if="qrModalOpen"
          class="wechat-modal"
          role="dialog"
          aria-modal="true"
          :aria-label="t('footer.social.wechatScan')"
          @click.self="closeWechatQr"
        >
          <div class="wechat-modal__panel">
            <button
              type="button"
              class="wechat-modal__close"
              :aria-label="t('common.close')"
              @click="closeWechatQr"
            >×</button>
            <img
              :src="wechatSearchImg"
              alt="Mandarly WeChat search and QR"
              class="wechat-modal__img"
              loading="eager"
            />
          </div>
        </div>
      </transition>
    </Teleport>
  </footer>
</template>

<style lang="scss" scoped>
.app-footer {
  background: brand.$mesh-cream-soft;
  border-block-start: 1px solid brand.$hairline;
  color: brand.$ink;
  margin-block-start: brand.$spacing-16;

  &__inner {
    max-width: 1280px;
    margin: 0 auto;
    padding: brand.$spacing-16 brand.$spacing-6 brand.$spacing-12;
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-10;
  }

  &__top {
    display: grid;
    grid-template-columns: minmax(240px, 0.85fr) minmax(0, 2.4fr);
    gap: brand.$spacing-12;
    align-items: start;

    @media (max-width: brand.$bp-laptop) {
      grid-template-columns: 1fr;
      gap: brand.$spacing-8;
    }
  }

  &__brand {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-3;
  }

  &__brand-logo {
    display: inline-flex;
    align-items: center;
    gap: brand.$spacing-2;
  }

  &__logo {
    height: 48px;
    width: auto;
  }

  &__brand-name {
    font-family: var(--font-family-heading-en);
    font-style: italic;
    font-size: brand.$font-size-2xl;
    font-weight: 600;
    color: brand.$ink;
    direction: ltr;
    unicode-bidi: isolate;
    letter-spacing: 0;
  }

  &__entity {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-1;
    color: brand.$muted;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-tight;
  }

  &__entity-en {
    direction: ltr;
    unicode-bidi: isolate;
    letter-spacing: 0;
  }

  &__columns {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: brand.$spacing-8;
    align-items: start;

    @media (max-width: brand.$bp-laptop) {
      grid-template-columns: repeat(2, 1fr);
      gap: brand.$spacing-6;
    }

    @media (max-width: brand.$bp-mobile) {
      grid-template-columns: 1fr;
    }
  }

  &__col {
    ul {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: brand.$spacing-2;
    }
  }

  &__col-title {
    margin: 0 0 brand.$spacing-3;
    font-size: brand.$font-size-base;
    font-weight: 600;
    color: brand.$ink;
    line-height: brand.$line-height-tight;
  }

  &__link {
    color: brand.$muted;
    font-size: brand.$font-size-sm;
    text-decoration: none;
    transition: color 0.15s;

    &:hover {
      color: brand.$brand-primary-deep;
    }

    &.is-disabled {
      color: brand.$muted-soft;
      cursor: not-allowed;
      pointer-events: none;
    }
  }

  &__address {
    color: brand.$muted;
    font-size: brand.$font-size-sm;
    line-height: brand.$line-height-base;
  }

  &__engagement {
    border-block-start: 1px solid brand.$hairline;
    padding-block-start: brand.$spacing-8;
    display: grid;
    grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.75fr);
    gap: brand.$spacing-10;
    align-items: start;

    @media (max-width: brand.$bp-laptop) {
      grid-template-columns: 1fr;
      gap: brand.$spacing-8;
    }
  }

  &__payment-grid {
    list-style: none;
    padding: 0;
    margin: 0;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: brand.$spacing-3;

    @media (max-width: brand.$bp-laptop) {
      grid-template-columns: repeat(auto-fit, minmax(116px, 1fr));
    }

    @media (max-width: brand.$bp-mobile) {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  &__payment-card {
    min-height: 64px;
    padding: brand.$spacing-3 brand.$spacing-2;
    border-radius: brand.$radius-base;
    background: brand.$color-bg-card;
    border: 1px solid brand.$hairline-soft;
    box-shadow: brand.$shadow-v2-sm;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: brand.$spacing-1;
    text-align: center;
  }

  &__payment-mark {
    min-height: 20px;
    color: #1f2937;
    font-size: brand.$font-size-base;
    font-weight: 800;
    line-height: brand.$line-height-tight;
    letter-spacing: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  &__payment-label {
    color: #4b5563;
    font-size: brand.$font-size-xs;
    font-weight: 600;
    line-height: brand.$line-height-tight;
    letter-spacing: 0;
  }

  &__payment-circles {
    position: relative;
    display: block;
    width: 42px;
    height: 24px;

    i {
      position: absolute;
      top: 0;
      width: 24px;
      height: 24px;
      border-radius: brand.$radius-full;
    }

    i:first-child {
      left: 0;
      background: #eb001b;
    }

    i:last-child {
      right: 0;
      background: #f79e1b;
      mix-blend-mode: multiply;
    }
  }

  &__payment-card--visa &__payment-mark {
    color: #1a4aa2;
    font-size: brand.$font-size-lg;
    font-style: italic;
  }

  &__payment-card--amex &__payment-mark {
    color: #1f6fb2;
    font-size: brand.$font-size-xs;
  }

  &__payment-card--discover &__payment-mark {
    color: #111827;
  }

  &__payment-card--paypal &__payment-mark {
    color: #1c5fba;
    font-size: brand.$font-size-lg;
  }

  &__payment-card--alipay &__payment-mark {
    color: #1677ff;
    font-size: brand.$font-size-lg;
  }

  &__payment-card--wechatPay &__payment-mark {
    color: #21a655;
  }

  &__payment-card--applePay &__payment-mark {
    color: #111827;
  }

  &__payment-card--googlePay &__payment-mark {
    color: #4285f4;
  }

  &__payment-card--bank &__payment-mark {
    color: #17335c;
  }

  &__follow-panel {
    min-width: 0;
  }

  &__follow-row {
    display: flex;
    flex-direction: column;
    gap: brand.$spacing-5;
    align-items: stretch;

    @media (max-width: brand.$bp-mobile) {
      gap: brand.$spacing-4;
    }
  }

  &__social {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-wrap: wrap;
    gap: brand.$spacing-3;
  }

  &__social-link {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border-radius: brand.$radius-lg;
    border: 1px solid brand.$hairline;
    background: rgba(255, 255, 255, 0.78);
    color: brand.$muted;
    text-decoration: none;
    transition: background-color 0.15s, border-color 0.15s, color 0.15s, transform 0.15s;

    &--button {
      padding: 0;
      font: inherit;
      cursor: pointer;
    }

    &:not(.app-footer__social-link--placeholder):hover,
    &:not(.app-footer__social-link--placeholder):focus-visible {
      background: brand.$brand-primary-soft;
      border-color: brand.$brand-primary-disabled;
      color: brand.$brand-primary-deep;
      transform: translateY(-1px);
    }

    &:focus-visible {
      outline: none;
      box-shadow: brand.$ring-focus;
    }

    &--placeholder {
      cursor: default;
    }
  }

  &__social-icon {
    width: 20px;
    height: 20px;
    fill: currentColor;
  }

  &__social-text {
    font-size: 11px;
    font-weight: 700;
    line-height: 1;
    letter-spacing: 0;
  }

  &__legal {
    border-block-start: 1px solid brand.$hairline;
    background: rgba(255, 255, 255, 0.72);
  }

  &__legal-inner {
    max-width: 1280px;
    margin: 0 auto;
    padding: brand.$spacing-5 brand.$spacing-6;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: brand.$spacing-4;
    flex-wrap: wrap;
  }

  &__copyright {
    margin: 0;
    color: brand.$muted-soft;
    font-size: brand.$font-size-xs;
    line-height: brand.$line-height-base;
    text-align: center;
  }

  &__wechat {
    margin: 0;
    width: min(360px, 100%);
    align-self: flex-end;

    @media (max-width: brand.$bp-mobile) {
      align-self: flex-start;
    }
  }

  &__wechat-btn {
    border: none;
    padding: 0;
    background: transparent;
    cursor: zoom-in;
    border-radius: brand.$radius-base;
    transition: transform 0.18s ease, box-shadow 0.18s ease;

    &:hover {
      transform: scale(1.03);
    }

    &:focus-visible {
      outline: none;
      box-shadow: brand.$ring-focus;
    }
  }

  &__wechat-img {
    display: block;
    width: min(360px, 100%);
    height: auto;
  }

  // D27:链接按钮形态(联系方式)— 与 <a> 视觉等同,但语义是 button(触发客服弹窗)
  &__link--btn {
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    font: inherit;
    text-align: start;
  }
}

// D27:微信 QR 放大 modal — Teleport 到 body,在 footer scope 外
.wechat-modal {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.wechat-modal__panel {
  position: relative;
  background: brand.$color-bg-card;
  border-radius: brand.$radius-xl;
  padding: brand.$spacing-8;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: brand.$spacing-4;
  max-width: min(680px, calc(100vw - 48px));
  box-shadow: brand.$shadow-lg;
}

.wechat-modal__close {
  position: absolute;
  top: 8px;
  inset-inline-end: 8px;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: brand.$muted;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  border-radius: brand.$radius-full;
  transition: background 0.15s, color 0.15s;

  &:hover {
    background: brand.$brand-primary-soft;
    color: brand.$ink;
  }

  &:focus-visible {
    outline: none;
    box-shadow: brand.$ring-focus;
  }
}

.wechat-modal__img {
  display: block;
  width: min(560px, calc(100vw - 112px));
  height: auto;
  border-radius: brand.$radius-base;
}

.wechat-modal-fade-enter-active,
.wechat-modal-fade-leave-active {
  transition: opacity 0.18s ease;

  .wechat-modal__panel {
    transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
}

.wechat-modal-fade-enter-from,
.wechat-modal-fade-leave-to {
  opacity: 0;

  .wechat-modal__panel {
    transform: scale(0.92);
  }
}
</style>
