import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduPackageApi {
  /** 套餐信息(后端 PackageRespVO) */
  export interface PackageInfo {
    id?: number;
    nameI18nCode: string;
    weeklyCount?: number | null;
    totalCount: number;
    validityDays: number;
    /** BigDecimal,前端用 number 表达,提交时会序列化为字符串 */
    price: number | string;
    currency?: string;
    isFreeTrial?: boolean;
    isActive?: boolean;
    sort?: number;
    descriptionI18nCode?: string;
    createTime?: Date;
  }

  /** 套餐保存入参(后端 PackageSaveReqVO) */
  export interface PackageSaveReq {
    id?: number;
    nameI18nCode: string;
    weeklyCount?: number | null;
    totalCount: number;
    validityDays: number;
    price: number | string;
    currency?: string;
    isFreeTrial?: boolean;
    isActive?: boolean;
    sort?: number;
    descriptionI18nCode?: string;
  }

  /** 列表查询参数 */
  export interface PackagePageReq extends PageParam {
    nameI18nCode?: string;
    currency?: string;
    isFreeTrial?: boolean;
    isActive?: boolean;
  }
}

/** 查询套餐分页 */
export function getPackagePage(params: EduPackageApi.PackagePageReq) {
  return requestClient.get<PageResult<EduPackageApi.PackageInfo>>(
    '/edu/package/page',
    { params },
  );
}

/** 查询套餐详情 */
export function getPackage(id: number) {
  return requestClient.get<EduPackageApi.PackageInfo>(
    `/edu/package/get?id=${id}`,
  );
}

/** 新增套餐 */
export function createPackage(data: EduPackageApi.PackageSaveReq) {
  return requestClient.post<number>('/edu/package/create', data);
}

/** 修改套餐 */
export function updatePackage(data: EduPackageApi.PackageSaveReq) {
  return requestClient.put<boolean>('/edu/package/update', data);
}

/** 删除套餐(逻辑删除) */
export function deletePackage(id: number) {
  return requestClient.delete<boolean>(`/edu/package/delete?id=${id}`);
}
