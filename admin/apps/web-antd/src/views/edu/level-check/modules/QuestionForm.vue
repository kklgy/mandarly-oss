<script lang="ts" setup>
import type { EduLevelCheckApi } from '#/api/edu/level-check';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createLevelCheckQuestion,
  getLevelCheckQuestion,
  updateLevelCheckQuestion,
} from '#/api/edu/level-check';
import { $t } from '#/locales';

import { useQuestionFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<EduLevelCheckApi.Question>();
const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['题目'])
    : $t('ui.actionTitle.create', ['题目']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 130,
  },
  layout: 'horizontal',
  schema: useQuestionFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const data = (await formApi.getValues()) as EduLevelCheckApi.Question;
    try {
      await (formData.value?.id
        ? updateLevelCheckQuestion(data)
        : createLevelCheckQuestion(data));
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    const data = modalApi.getData<EduLevelCheckApi.Question>();
    if (!data?.id) {
      formData.value = undefined;
      await formApi.setValues({
        isActive: true,
        questionType: 'single_choice',
        sort: 0,
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getLevelCheckQuestion(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
