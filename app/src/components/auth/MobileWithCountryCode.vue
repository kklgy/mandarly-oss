<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  buildE164Phone,
  defaultPhoneCountry,
  digitsOnly,
  getCountryByIso,
  parseE164Phone,
  PHONE_COUNTRIES
} from '@/utils/phoneCountries'

defineOptions({ name: 'MobileWithCountryCode' })

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])
const { t, locale } = useI18n()

const countryIso = ref(defaultPhoneCountry(locale.value).iso)
const nationalNumber = ref('')
let syncingFromModel = false

const inputPlaceholder = computed(() => props.placeholder || t('auth.phone.nationalPlaceholder'))

function applyModelValue(value) {
  syncingFromModel = true
  const parsed = parseE164Phone(value)
  if (parsed) {
    countryIso.value = parsed.country.iso
    nationalNumber.value = parsed.nationalNumber
  } else {
    countryIso.value = defaultPhoneCountry(locale.value).iso
    nationalNumber.value = ''
  }
  syncingFromModel = false
}

function emitValue() {
  if (syncingFromModel) return
  emit('update:modelValue', buildE164Phone(countryIso.value, nationalNumber.value))
}

function handleNationalInput(value) {
  const raw = String(value || '').trim()
  if (raw.startsWith('+') || raw.startsWith('00')) {
    const normalized = raw.startsWith('00') ? `+${raw.slice(2)}` : raw
    const parsed = parseE164Phone(normalized)
    if (parsed) {
      countryIso.value = parsed.country.iso
      nationalNumber.value = parsed.nationalNumber
      return
    }
  }
  nationalNumber.value = digitsOnly(raw)
}

watch(() => props.modelValue, applyModelValue, { immediate: true })

watch(locale, (nextLocale) => {
  if (!props.modelValue && !nationalNumber.value) {
    countryIso.value = defaultPhoneCountry(nextLocale).iso
  }
})

watch([countryIso, nationalNumber], emitValue)
</script>

<template>
  <div class="mobile-with-country">
    <el-select
      v-model="countryIso"
      class="mobile-with-country__select"
      filterable
      size="large"
      :aria-label="t('auth.phone.countryCode')"
    >
      <el-option
        v-for="country in PHONE_COUNTRIES"
        :key="country.iso"
        :label="`+${country.dialCode}`"
        :value="country.iso"
      >
        <span class="mobile-with-country__option-name">{{ country.name }}</span>
        <span class="mobile-with-country__option-code" dir="ltr">+{{ country.dialCode }}</span>
      </el-option>
    </el-select>
    <el-input
      :model-value="nationalNumber"
      class="mobile-with-country__input"
      type="tel"
      size="large"
      autocomplete="tel-national"
      inputmode="numeric"
      :placeholder="inputPlaceholder"
      @input="handleNationalInput"
    />
  </div>
</template>

<style lang="scss" scoped>
.mobile-with-country {
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr);
  gap: 0;
  width: 100%;

  @media (max-width: brand.$bp-mobile) {
    grid-template-columns: 96px minmax(0, 1fr);
  }

  &__select,
  &__input {
    min-width: 0;
  }

  :deep(.mobile-with-country__select .el-select__wrapper) {
    min-height: 44px;
    border-start-end-radius: 0;
    border-end-end-radius: 0;
    box-shadow: 0 0 0 1px var(--color-border) inset;
    color: var(--color-text-primary);
    font-weight: 700;
    font-variant-numeric: tabular-nums;
  }

  :deep(.mobile-with-country__input .el-input__wrapper) {
    min-height: 44px;
    border-start-start-radius: 0;
    border-end-start-radius: 0;
    box-shadow: 0 0 0 1px var(--color-border) inset;
  }

  :deep(.mobile-with-country__input .el-input__inner),
  :deep(.mobile-with-country__select .el-select__placeholder),
  :deep(.mobile-with-country__select .el-select__selected-item) {
    font-size: brand.$font-size-md;
  }

  :deep(.mobile-with-country__select .el-select__wrapper.is-focused),
  :deep(.mobile-with-country__input .el-input__wrapper.is-focus) {
    position: relative;
    z-index: 1;
    box-shadow: 0 0 0 1px var(--color-primary) inset, var(--ring-focus);
  }

  :deep(.el-select__placeholder) {
    color: var(--color-text-primary);
  }

  :deep(.el-select__selected-item) {
    direction: ltr;
    unicode-bidi: isolate;
   }

  &__option-name {
    display: inline-block;
    max-width: 170px;
    overflow: hidden;
    text-overflow: ellipsis;
    vertical-align: bottom;
    white-space: nowrap;
  }

  &__option-code {
    float: right;
    margin-inline-start: 12px;
    color: var(--color-text-secondary);
    font-variant-numeric: tabular-nums;
  }
}
</style>
