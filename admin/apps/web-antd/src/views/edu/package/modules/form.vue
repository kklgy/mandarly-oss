<script lang="ts" setup>
import type { EduPackageApi } from '#/api/edu/package';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createPackage,
  getPackage,
  updatePackage,
} from '#/api/edu/package';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<EduPackageApi.PackageInfo>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['套餐'])
    : $t('ui.actionTitle.create', ['套餐']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as EduPackageApi.PackageSaveReq;
    // price 强制保留 2 位小数,避免浮点精度丢失
    if (data.price !== null && data.price !== undefined && data.price !== '') {
      const num = typeof data.price === 'number'
        ? data.price
        : Number.parseFloat(String(data.price));
      if (!Number.isNaN(num)) {
        data.price = Number(num.toFixed(2));
      }
    }
    try {
      await (formData.value?.id ? updatePackage(data) : createPackage(data));
      // 关闭并提示
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
    // 加载数据
    const data = modalApi.getData<EduPackageApi.PackageInfo>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getPackage(data.id);
      // 设置到 values
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
