<script lang="ts" setup>
import type { EduLevelCheckApi } from '#/api/edu/level-check';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createLevelCheckOption,
  updateLevelCheckOption,
} from '#/api/edu/level-check';
import { $t } from '#/locales';

import { useOptionFormSchema } from '../data';

type OptionFormValues = EduLevelCheckApi.Option & {
  scoreRulesText?: string;
};

const emit = defineEmits(['success']);
const formData = ref<EduLevelCheckApi.Option>();
const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['选项'])
    : $t('ui.actionTitle.create', ['选项']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 130,
  },
  layout: 'horizontal',
  schema: useOptionFormSchema(),
  showDefaultActions: false,
});

function parseScoreRules(value?: string) {
  if (!value || !value.trim()) {
    return undefined;
  }
  const parsed = JSON.parse(value);
  if (!Array.isArray(parsed)) {
    throw new Error('软约束 JSON 必须是数组');
  }
  return parsed;
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const values = (await formApi.getValues()) as OptionFormValues;
    try {
      values.scoreRules = parseScoreRules(values.scoreRulesText);
      delete values.scoreRulesText;
      values.matchExpertise = (values.matchExpertise || [])
        .map((item) => String(item).trim())
        .filter(Boolean);
      await (formData.value?.id
        ? updateLevelCheckOption(values)
        : createLevelCheckOption(values));
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } catch (error) {
      if (error instanceof SyntaxError || error instanceof Error) {
        message.error(error.message || '软约束 JSON 格式不正确');
      }
      throw error;
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    const data = modalApi.getData<EduLevelCheckApi.Option>();
    formData.value = data?.id ? data : undefined;
    await formApi.setValues({
      ...data,
      scoreRulesText: data?.scoreRules?.length
        ? JSON.stringify(data.scoreRules, null, 2)
        : '',
      sort: data?.sort ?? 0,
    });
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
