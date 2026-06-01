<script lang="ts" setup>
import type { EduSupportApi } from '#/api/edu/support';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createSupportContact,
  getSupportContact,
  updateSupportContact,
} from '#/api/edu/support';
import { $t } from '#/locales';

import { useContactFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<EduSupportApi.Contact>();
const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['联系方式'])
    : $t('ui.actionTitle.create', ['联系方式']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useContactFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const data = (await formApi.getValues()) as EduSupportApi.Contact;
    try {
      await (formData.value?.id
        ? updateSupportContact(data)
        : createSupportContact(data));
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
    const data = modalApi.getData<EduSupportApi.Contact>();
    if (!data?.id) {
      formData.value = undefined;
      await formApi.setValues({
        channelType: 'other',
        isActive: true,
        market: 'DEFAULT',
        sort: 100,
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getSupportContact(data.id);
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
