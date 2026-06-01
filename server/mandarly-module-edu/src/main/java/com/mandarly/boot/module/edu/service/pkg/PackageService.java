package com.mandarly.boot.module.edu.service.pkg;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.pkg.vo.PackagePageReqVO;
import com.mandarly.boot.module.edu.controller.admin.pkg.vo.PackageSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;

import java.util.List;

/**
 * 套餐定义 Service 接口
 *
 * <p>一期纯 CRUD,无状态机;约束:全表最多 1 条 is_free_trial=true AND is_active=true。
 */
public interface PackageService {

    PageResult<PackageDO> getPackagePage(PackagePageReqVO reqVO);

    PackageDO getPackage(Long id);

    Long createPackage(PackageSaveReqVO reqVO);

    void updatePackage(PackageSaveReqVO reqVO);

    /**
     * 逻辑删除;Service 层不做"是否被 student_package 引用"的强校验,
     * 因为下架(is_active=false)是更合理的下架手段;此处仅简单标记 deleted=1。
     */
    void deletePackage(Long id);

    /** 公开套餐列表(is_active=1 且非 free_trial,按 sort 升序),用户端展示用 */
    List<PackageDO> getPublicPackageList();

    /**
     * 公开套餐列表 + 按币种过滤(Wave 4 P1, 2026-05-10)。
     *
     * <p>实施约定:套餐表当前每行只有 1 个 currency 字段(单币种存价),
     *   传 currency 时返回该币种的套餐;传 null 返回全部(默认 HKD 主货币行)。
     *   后续若改为多币种价(price_hkd / price_usd / price_cny 三字段)再扩。
     *
     * @param currency null 或 "HKD"/"USD"/"CNY"
     */
    List<PackageDO> getPublicPackageList(String currency);

}
