<script lang="ts" setup>
import type { EduSupportApi } from '#/api/edu/support';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createSupportFaq,
  getSupportFaq,
  updateSupportFaq,
} from '#/api/edu/support';
import { $t } from '#/locales';

import { useFaqFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<EduSupportApi.Faq>();
const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['FAQ'])
    : $t('ui.actionTitle.create', ['FAQ']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useFaqFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const data = (await formApi.getValues()) as EduSupportApi.Faq;
    data.keywords = (data.keywords || [])
      .map((keyword) => String(keyword).trim())
      .filter(Boolean);
    try {
      await (formData.value?.id ? updateSupportFaq(data) : createSupportFaq(data));
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
    const data = modalApi.getData<EduSupportApi.Faq>();
    if (!data?.id) {
      formData.value = undefined;
      await formApi.setValues({
        category: 'other',
        locale: 'zh-CN',
        sort: 0,
        status: 'active',
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getSupportFaq(data.id);
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
