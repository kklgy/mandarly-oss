<script setup>
// =============================================================================
// <TeacherIntroVideo> — TeacherDetail § B 试讲视频(可选, null 时整节隐藏)
//
// 设计: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § TeacherDetailView 改造 § B
//
// 字段: teacher.introVideoUrl(VARCHAR(500) NULL, Wave 5 SQL patch 落地)
//        null 时本组件不渲染任何内容(包括标题), 避免视觉空洞
// =============================================================================
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { VideoPlay } from '@element-plus/icons-vue'

const props = defineProps({
  videoUrl: {
    type: String,
    default: ''
  },
  posterUrl: {
    type: String,
    default: ''
  }
})

const { t, te } = useI18n()
const t2 = (key, fallback) => (te(key) ? t(key) : fallback)

const dialogOpen = ref(false)

const hasVideo = computed(() => Boolean(props.videoUrl))

function open() {
  if (hasVideo.value) dialogOpen.value = true
}

function close() {
  dialogOpen.value = false
}
</script>

<template>
  <section v-if="hasVideo" class="td-video">
    <h2 class="td-video__title">{{ t2('teacherDetail.video.title', '试讲视频') }}</h2>

    <button
      type="button"
      class="td-video__poster"
      :aria-label="t2('teacherDetail.video.title', '试讲视频')"
      @click="open"
    >
      <img
        v-if="posterUrl"
        :src="posterUrl"
        alt=""
        class="td-video__poster-img"
      />
      <div v-else class="td-video__poster-placeholder" aria-hidden="true"></div>

      <span class="td-video__play">
        <el-icon><VideoPlay /></el-icon>
      </span>
    </button>

    <el-dialog
      v-model="dialogOpen"
      :title="t2('teacherDetail.video.title', '试讲视频')"
      width="800"
      align-center
      :show-close="true"
      destroy-on-close
      class="td-video__dialog"
      @close="close"
    >
      <video
        :src="videoUrl"
        controls
        playsinline
        class="td-video__player"
      ></video>
    </el-dialog>
  </section>
</template>

<style scoped lang="scss">

.td-video {
  background: var(--color-bg-card);
  border-radius: brand.$radius-lg;
  padding: brand.$spacing-6 brand.$spacing-5;
  box-shadow: brand.$shadow-sm;
  display: flex;
  flex-direction: column;
  gap: brand.$spacing-4;

  @media (min-width: brand.$bp-tablet) {
    padding: brand.$spacing-8;
  }

  &__title {
    margin: 0;
    font-size: brand.$font-size-lg;
    font-weight: 600;
    color: brand.$color-text-primary;
  }

  &__poster {
    position: relative;
    width: 100%;
    aspect-ratio: 16 / 9;
    border: none;
    border-radius: brand.$radius-card;
    overflow: hidden;
    cursor: pointer;
    padding: 0;
    background: brand.$color-bg-strong;
    box-shadow: brand.$shadow-base;
    transition: transform 0.18s ease, box-shadow 0.18s ease;

    &:hover {
      box-shadow: brand.$shadow-md;
      transform: translateY(-2px);
    }
  }

  &__poster-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &__poster-placeholder {
    width: 100%;
    height: 100%;
    background: brand.$brand-gradient;
    opacity: 0.6;
  }

  &__play {
    position: absolute;
    inset-block-start: 50%;
    inset-inline-start: 50%;
    transform: translate(-50%, -50%);
    width: 64px;
    height: 64px;
    border-radius: brand.$radius-full;
    background: brand.$brand-primary;
    color: brand.$color-text-inverse;
    display: grid;
    place-items: center;
    font-size: brand.$font-size-2xl;
    box-shadow: brand.$shadow-brand;
  }

  &__player {
    width: 100%;
    max-height: 70vh;
    display: block;
    border-radius: brand.$radius-base;
    background: brand.$color-text-primary;
  }
}
</style>
