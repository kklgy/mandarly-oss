<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { EduPackageApi } from '#/api/edu/package';

import { Page, useVbenModal } from '@vben/common-ui';

import { message, Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deletePackage, getPackagePage } from '#/api/edu/package';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建套餐 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑套餐 */
function handleEdit(row: EduPackageApi.PackageInfo) {
  formModalApi.setData(row).open();
}

/** 删除套餐 */
async function handleDelete(row: EduPackageApi.PackageInfo) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.nameI18nCode]),
    duration: 0,
  });
  try {
    await deletePackage(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.nameI18nCode]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getPackagePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<EduPackageApi.PackageInfo>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="套餐列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['套餐']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['edu:package:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>

      <template #isFreeTrial="{ row }">
        <Tag v-if="row.isFreeTrial" color="orange">免费体验</Tag>
        <Tag v-else>否</Tag>
      </template>

      <template #isActive="{ row }">
        <Tag v-if="row.isActive" color="green">启用</Tag>
        <Tag v-else color="default">停用</Tag>
      </template>

      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['edu:package:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['edu:package:delete'],
              popConfirm: {
                title: '确认删除该套餐?(逻辑删除,可恢复)',
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
