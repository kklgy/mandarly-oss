<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close, Message, Service } from '@element-plus/icons-vue'
import { bootstrapSupport, markSupportContactClick } from '@/api/support'

const props = defineProps({
  hasTabbar: {
    type: Boolean,
    default: false
  }
})

const { t, locale } = useI18n()

const SESSION_STORAGE_KEY = 'mandarly_support_session_id'

const open = ref(false)
const bootstrapped = ref(false)
const loading = ref(false)
const contacts = ref([])
const sessionId = ref('')

const localeValue = computed(() => locale.value || 'en')

const fallbackContacts = computed(() => [
  {
    id: 'fallback-wechat',
    channelType: 'wechat',
    displayText: t('support.channel.wechat'),
    linkUrl: 'wechat:I-KYC_ZJF-LOVE'
  },
  {
    id: 'fallback-whatsapp',
    channelType: 'whatsapp',
    displayText: t('support.channel.whatsapp'),
    linkUrl: ''
  },
  {
    id: 'fallback-email',
    channelType: 'email',
    displayText: t('support.channel.email'),
    linkUrl: 'mailto:support@mandarly.com'
  }
])

const channelOrder = {
  wechat: 0,
  whatsapp: 1,
  email: 2,
  other: 3
}

const visibleContacts = computed(() => {
  const source = contacts.value.length ? mergeFallbackChannels(contacts.value) : fallbackContacts.value
  // D27:同一渠道类型只保留第一条(去除 prod DB 历史多条 Email/Email support 重复条目)
  const seen = new Set()
  const deduped = []
  for (const contact of source) {
    const channel = normalizeChannel(contact)
    if (seen.has(channel)) continue
    seen.add(channel)
    deduped.push(contact)
  }
  return deduped.sort((a, b) => {
    const left = channelOrder[normalizeChannel(a)] ?? channelOrder.other
    const right = channelOrder[normalizeChannel(b)] ?? channelOrder.other
    return left - right
  })
})

function mergeFallbackChannels(source) {
  const seen = new Set(source.map((contact) => normalizeChannel(contact)))
  const missing = fallbackContacts.value.filter((contact) => !seen.has(normalizeChannel(contact)))
  return [...source, ...missing]
}

/**
 * 归一化 contact 渠道类型。
 * 优先看 channelType 字段(wechat / whatsapp / email / mail);
 * 兜底按 linkUrl 协议判断 — prod DB 历史条目可能有 channelType='other' 但 linkUrl=mailto:xxx,
 * 这种 case 必须归到 email,否则 fallback email 会被再次补进来出现重复。
 */
function normalizeChannel(contact) {
  if (!contact) return 'other'
  const raw = String(contact.channelType || '').toLowerCase()
  if (raw.includes('wechat')) return 'wechat'
  if (raw.includes('whatsapp')) return 'whatsapp'
  if (raw.includes('email') || raw.includes('mail')) return 'email'
  // 协议兜底
  const link = String(contact.linkUrl || '').toLowerCase().trim()
  if (link.startsWith('mailto:')) return 'email'
  if (link.startsWith('whatsapp:') || link.includes('wa.me/') || link.includes('whatsapp.com')) return 'whatsapp'
  if (link.startsWith('wechat:')) return 'wechat'
  return 'other'
}

function inferMarket() {
  if (localeValue.value === 'zh-TW') return 'HK'
  if (localeValue.value === 'ar') return 'AE'
  return 'DEFAULT'
}

function ensureSessionId() {
  if (sessionId.value) return sessionId.value
  try {
    const existing = window.localStorage?.getItem(SESSION_STORAGE_KEY)
    if (existing) {
      sessionId.value = existing
      return existing
    }
  } catch (e) {
    /* localStorage 可能在隐私模式下抛错,降级到内存 */
  }
  const fresh = generateSessionId()
  try {
    window.localStorage?.setItem(SESSION_STORAGE_KEY, fresh)
  } catch (e) { /* ignore */ }
  sessionId.value = fresh
  return fresh
}

function generateSessionId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `s_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 10)}`
}

/**
 * 解析微信号:支持 wechat:I-KYC_ZJF-LOVE / WeChat: id / 大小写不敏感 / 冒号后空格容错。
 */
function parseWechatId(linkUrl) {
  if (!linkUrl) return ''
  const match = /^wechat\s*:\s*(.+)$/i.exec(String(linkUrl).trim())
  return match ? match[1].trim() : ''
}

/**
 * 同步路径优先 clipboard API,失败降级 execCommand fallback。
 * 必须在 user gesture 同步栈内调用,否则 iOS / 微信内浏览器会拒绝。
 */
function copyText(text) {
  // execCommand fallback 同步路径,先准备好 textarea
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.top = '0'
  ta.style.left = '0'
  ta.style.opacity = '0'
  ta.setAttribute('readonly', '')
  document.body.appendChild(ta)
  ta.focus()
  ta.select()
  ta.setSelectionRange(0, text.length)

  let execOk = false
  try {
    execOk = document.execCommand('copy')
  } catch (e) {
    execOk = false
  }
  document.body.removeChild(ta)

  if (execOk) {
    return Promise.resolve(true)
  }

  // execCommand 失败,再尝试现代 clipboard API
  if (navigator.clipboard && typeof navigator.clipboard.writeText === 'function') {
    return navigator.clipboard.writeText(text).then(() => true).catch(() => false)
  }
  return Promise.resolve(false)
}

async function ensureBootstrap() {
  if (bootstrapped.value || loading.value) return
  loading.value = true
  try {
    const data = await bootstrapSupport({
      locale: localeValue.value,
      market: inferMarket()
    })
    contacts.value = data?.contacts || []
  } catch (e) {
    contacts.value = []
  } finally {
    bootstrapped.value = true
    loading.value = false
  }
}

async function toggleOpen() {
  open.value = !open.value
  if (open.value) await ensureBootstrap()
}

async function openWidget() {
  open.value = true
  await ensureBootstrap()
}

function trackContactClick(contact) {
  if (!contact?.id || typeof contact.id !== 'number') return
  const sid = ensureSessionId()
  markSupportContactClick({
    contactId: contact.id,
    sessionId: sid,
    locale: localeValue.value,
    market: inferMarket()
  }).catch(() => {
    // 点击埋点失败不阻断用户联系真人客服
  })
}

/**
 * 点击渠道按钮:必须在 click handler 同步栈内立刻处理跳转/复制,
 * 否则 iOS Safari / 微信内浏览器会丢失 user activation,
 * clipboard 拒绝、window.open 被弹窗拦截。
 * 埋点放最后 fire-and-forget,不 await。
 */
function openContact(contact) {
  const linkUrl = String(contact?.linkUrl || '').trim()

  if (!linkUrl) {
    ElMessage.warning(t('support.channel.pending'))
    trackContactClick(contact)
    return
  }

  const wechatId = parseWechatId(linkUrl)
  if (wechatId) {
    copyText(wechatId).then((ok) => {
      if (ok) {
        ElMessage.success(t('support.wechatCopied'))
      } else {
        ElMessage({
          type: 'warning',
          message: t('support.wechatCopyFailed', { id: wechatId }),
          duration: 5000,
          showClose: true
        })
      }
    })
    trackContactClick(contact)
    return
  }

  if (/^mailto:/i.test(linkUrl) || /^tel:/i.test(linkUrl)) {
    // iOS Safari 对 mailto/tel 走 location.href 比 window.open 更可靠
    window.location.href = linkUrl
    trackContactClick(contact)
    return
  }

  const opened = window.open(linkUrl, '_blank', 'noopener,noreferrer')
  if (!opened) {
    ElMessage.warning(t('support.openFailed'))
  }
  trackContactClick(contact)
}

onMounted(() => {
  ensureSessionId()
  window.addEventListener('mandarly:open-support', openWidget)
})

onBeforeUnmount(() => {
  window.removeEventListener('mandarly:open-support', openWidget)
})

function getContactSubtext(contact) {
  const wechatId = parseWechatId(contact?.linkUrl)
  if (wechatId) return wechatId
  return t(`support.channel.${normalizeChannel(contact)}`)
}
</script>

<template>
  <div class="support-chat" :class="{ 'support-chat--tabbar': props.hasTabbar, 'is-open': open }">
    <transition name="support-chat-panel">
      <section v-if="open" class="support-chat__panel" :aria-label="t('support.title')">
        <header class="support-chat__header">
          <div class="support-chat__title-row">
            <span class="support-chat__avatar" aria-hidden="true">
              <el-icon><Service /></el-icon>
            </span>
            <div class="support-chat__copy">
              <h2 class="support-chat__title">{{ t('support.title') }}</h2>
              <p class="support-chat__subtitle">{{ t('support.subtitle') }}</p>
            </div>
          </div>
          <button type="button" class="support-chat__close" :aria-label="t('support.close')" @click="open = false">
            <el-icon><Close /></el-icon>
          </button>
        </header>

        <div class="support-chat__body" :aria-busy="loading">
          <p class="support-chat__hint">{{ t('support.directHint') }}</p>
          <div class="support-chat__channels">
            <button
              v-for="contact in visibleContacts"
              :key="contact.id"
              type="button"
              class="support-chat__channel"
              @click="openContact(contact)"
            >
              <span class="support-chat__channel-icon" aria-hidden="true">
                <el-icon><Message /></el-icon>
              </span>
              <span class="support-chat__channel-copy">
                <span class="support-chat__channel-title">{{ contact.displayText }}</span>
                <span class="support-chat__channel-meta">{{ getContactSubtext(contact) }}</span>
              </span>
            </button>
          </div>
        </div>
      </section>
    </transition>

    <button type="button" class="support-chat__fab" :aria-label="t('support.open')" @click="toggleOpen">
      <el-icon><ChatDotRound /></el-icon>
    </button>
  </div>
</template>

<style scoped lang="scss">
.support-chat {
  position: fixed;
  inset-inline-end: brand.$spacing-6;
  inset-block-end: calc(#{brand.$spacing-6} + env(safe-area-inset-bottom, 0px));
  z-index: 1100;
  padding-block-end: env(safe-area-inset-bottom, 0px);

  &--tabbar {
    inset-block-end: calc(72px + #{brand.$spacing-4} + env(safe-area-inset-bottom, 0px));
  }
}

.support-chat__fab {
  width: 56px;
  min-width: 56px;
  height: 56px;
  border: none;
  border-radius: brand.$radius-full;
  background: var(--color-primary);
  color: var(--btn-primary-text);
  box-shadow: var(--shadow-brand);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: brand.$font-size-2xl;
  cursor: pointer;
}

.support-chat__panel {
  position: absolute;
  inset-inline-end: 0;
  inset-block-end: calc(56px + #{brand.$spacing-4});
  width: min(360px, calc(100vw - #{brand.$spacing-6}));
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: brand.$radius-xl;
  box-shadow: brand.$shadow-lg;
  overflow: hidden;
}

.support-chat__header {
  padding: brand.$spacing-4;
  border-block-end: 1px solid var(--color-border-light);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: brand.$spacing-3;
}

.support-chat__title-row {
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;
  min-width: 0;
}

.support-chat__avatar,
.support-chat__channel-icon {
  border-radius: brand.$radius-full;
  background: var(--color-primary-soft);
  color: var(--color-primary-deep);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.support-chat__avatar {
  width: 40px;
  height: 40px;
  font-size: brand.$font-size-xl;
}

.support-chat__copy {
  min-width: 0;
}

.support-chat__title {
  margin: 0;
  color: var(--color-text-primary);
  font-size: brand.$font-size-md;
  font-weight: 700;
  line-height: brand.$line-height-tight;
}

.support-chat__subtitle,
.support-chat__hint,
.support-chat__channel-meta {
  color: var(--color-text-secondary);
  line-height: brand.$line-height-base;
}

.support-chat__subtitle {
  margin: brand.$spacing-1 0 0;
  font-size: brand.$font-size-xs;
}

.support-chat__close {
  width: 44px;
  min-width: 44px;
  height: 44px;
  border: none;
  border-radius: brand.$radius-full;
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;

  &:hover {
    background: var(--color-bg-strong);
    color: var(--color-text-primary);
  }
}

.support-chat__body {
  padding: brand.$spacing-4;
}

.support-chat__hint {
  margin: 0 0 brand.$spacing-4;
  font-size: brand.$font-size-sm;
}

.support-chat__channels {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-2;
}

.support-chat__channel {
  min-height: 56px;
  border: 1px solid var(--color-border);
  border-radius: brand.$radius-base;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: brand.$spacing-3;
  padding: brand.$spacing-3;
  text-align: start;
  cursor: pointer;

  &:hover {
    border-color: var(--color-primary);
    background: var(--color-primary-soft);
  }
}

.support-chat__channel-icon {
  width: 36px;
  height: 36px;
  font-size: brand.$font-size-lg;
}

.support-chat__channel-copy {
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-1;
  min-width: 0;
}

.support-chat__channel-title,
.support-chat__channel-meta {
  overflow-wrap: anywhere;
}

.support-chat__channel-title {
  font-size: brand.$font-size-sm;
  font-weight: 700;
  line-height: brand.$line-height-tight;
}

.support-chat__channel-meta {
  font-size: brand.$font-size-xs;
  // iOS / 微信内浏览器即使 clipboard 失败,用户也能长按选中微信号手动复制
  user-select: text;
  -webkit-user-select: text;
}

.support-chat-panel-enter-active,
.support-chat-panel-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.support-chat-panel-enter-from,
.support-chat-panel-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: #{brand.$bp-tablet - 1px}) {
  .support-chat {
    inset-inline-end: brand.$spacing-4;
    inset-block-end: calc(#{brand.$spacing-4} + env(safe-area-inset-bottom, 0px));
  }

  .support-chat--tabbar {
    inset-block-end: calc(72px + #{brand.$spacing-4} + env(safe-area-inset-bottom, 0px));
  }

  .support-chat__panel {
    width: min(360px, calc(100vw - #{brand.$spacing-6}));
    max-width: calc(100vw - #{brand.$spacing-6});
  }
}
</style>
