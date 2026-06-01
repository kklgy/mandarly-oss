<script lang="ts" setup>
import type { EduTeacherProfileApi } from '#/api/edu/teacher-profile';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { auditTeacherProfile } from '#/api/edu/teacher-profile';

const emit = defineEmits(['success']);
const formData = ref<EduTeacherProfileApi.TeacherProfile>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: [
    {
      fieldName: 'rejectReason',
      label: '驳回原因',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入驳回原因',
        rows: 4,
        maxlength: 500,
        showCount: true,
      },
      rules: 'required',
    },
  ],
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    if (!formData.value?.userId) {
      message.error('缺少教师 userId,无法提交');
      return;
    }
    modalApi.lock();
    try {
      const values = await formApi.getValues<{ rejectReason: string }>();
      await auditTeacherProfile({
        userId: formData.value.userId,
        action: 'reject',
        rejectReason: values.rejectReason,
      });
      await modalApi.close();
      emit('success');
      message.success('已驳回');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      await formApi.resetForm();
      return;
    }
    const data = modalApi.getData<EduTeacherProfileApi.TeacherProfile>();
    if (data) {
      formData.value = data;
    }
  },
});
</script>

<template>
  <Modal title="驳回教师审核">
    <Form class="mx-4" />
  </Modal>
</template>
