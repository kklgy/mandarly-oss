<script lang="ts" setup>
import { $t } from '@vben/locales';

import { useVbenModal } from '@vben-core/popup-ui';

import { useMagicKeys, whenever } from '@vueuse/core';

defineOptions({
  name: 'Help',
});

const keys = useMagicKeys();
whenever(keys['Alt+KeyH']!, () => {
  modalApi.open();
});

const [Modal, modalApi] = useVbenModal({
  draggable: true,
  overlayBlur: 5,
  footer: false,
  onCancel() {
    modalApi.close();
  },
});
</script>
<template>
  <Modal class="w-1/3" :title="$t('ui.widgets.qa')">
    <div class="space-y-3 p-2 text-sm leading-6 text-muted-foreground">
      <p class="text-base font-semibold text-foreground">Mandarly 管理后台</p>
      <p>如遇账号、权限、消息发送或业务数据问题，请联系 Mandarly 运营负责人。</p>
      <p>生产排障请优先查看消息中心、审计日志与对应业务记录。</p>
    </div>
  </Modal>
</template>
