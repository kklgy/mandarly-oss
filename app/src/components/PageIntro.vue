<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  primaryLabel: {
    type: String,
    default: ''
  },
  primaryTo: {
    type: String,
    default: ''
  },
  secondaryLabel: {
    type: String,
    default: ''
  },
  secondaryTo: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <section class="page-intro" aria-labelledby="page-intro-title">
    <div class="page-intro__copy">
      <p v-if="eyebrow" class="page-intro__eyebrow">{{ eyebrow }}</p>
      <h1 id="page-intro-title" class="page-intro__title">{{ title }}</h1>
      <p v-if="description" class="page-intro__description">{{ description }}</p>
    </div>

    <div v-if="primaryLabel || secondaryLabel" class="page-intro__actions">
      <router-link
        v-if="primaryLabel && primaryTo"
        class="page-intro__action page-intro__action--primary"
        :to="primaryTo"
      >
        {{ primaryLabel }}
      </router-link>
      <router-link
        v-if="secondaryLabel && secondaryTo"
        class="page-intro__action page-intro__action--secondary"
        :to="secondaryTo"
      >
        {{ secondaryLabel }}
      </router-link>
    </div>
  </section>
</template>

<style scoped lang="scss">
.page-intro {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: brand.$spacing-4;
  padding: brand.$spacing-4;
  border: 1px solid color-mix(in srgb, var(--color-primary) 24%, transparent);
  border-radius: brand.$radius-card;
  background: var(--color-primary-soft);
  margin-block-end: brand.$spacing-5;

  @media (max-width: 767px) {
    align-items: stretch;
    flex-direction: column;
  }

  &__copy {
    min-width: 0;
  }

  &__eyebrow {
    margin: 0 0 brand.$spacing-1;
    color: brand.$brand-primary-deep;
    font-size: brand.$font-size-xs;
    font-weight: 700;
  }

  &__title {
    margin: 0;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-xl;
    font-weight: 700;
    line-height: brand.$line-height-tight;
  }

  &__description {
    margin: brand.$spacing-2 0 0;
    color: brand.$color-text-secondary;
    font-size: brand.$font-size-base;
    line-height: brand.$line-height-base;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: brand.$spacing-2;
    flex-wrap: wrap;
  }

  &__action {
    min-height: 40px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding-inline: brand.$spacing-4;
    border-radius: brand.$radius-pill;
    color: brand.$color-text-primary;
    font-size: brand.$font-size-sm;
    font-weight: 600;
    text-decoration: none;
    white-space: nowrap;
    transition: box-shadow 0.18s ease, transform 0.18s ease;

    &:focus-visible {
      outline: none;
      box-shadow: brand.$ring-focus;
    }

    &:hover {
      transform: translateY(-1px);
    }

    &--primary {
      background: brand.$brand-primary;
      border: 1px solid brand.$brand-primary;
    }

    &--secondary {
      background: brand.$color-bg-card;
      border: 1px solid brand.$color-border;
    }
  }
}
</style>
