package com.mandarly.boot.module.edu.service.pkg;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.pkg.vo.PackagePageReqVO;
import com.mandarly.boot.module.edu.controller.admin.pkg.vo.PackageSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 套餐定义 Service 实现
 */
@Service
@Slf4j
public class PackageServiceImpl implements PackageService {

    @Resource
    private PackageMapper packageMapper;

    @Override
    public PageResult<PackageDO> getPackagePage(PackagePageReqVO reqVO) {
        return packageMapper.selectPage(reqVO);
    }

    @Override
    public PackageDO getPackage(Long id) {
        return packageMapper.selectById(id);
    }

    @Override
    public Long createPackage(PackageSaveReqVO reqVO) {
        PackageDO entity = BeanUtils.toBean(reqVO, PackageDO.class);
        applyDefaults(entity);
        validateBusinessRules(entity, null);
        ensureSingleActiveFreeTrial(entity, null);
        packageMapper.insert(entity);
        log.info("[createPackage] id={} name={} freeTrial={} active={}",
                entity.getId(), entity.getNameI18nCode(), entity.getIsFreeTrial(), entity.getIsActive());
        return entity.getId();
    }

    @Override
    public void updatePackage(PackageSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "更新时 id 必填");
        }
        PackageDO existing = packageMapper.selectById(reqVO.getId());
        if (existing == null) {
            throw ServiceExceptionUtil.exception0(404, "套餐不存在");
        }
        PackageDO entity = BeanUtils.toBean(reqVO, PackageDO.class);
        applyDefaults(entity);
        validateBusinessRules(entity, existing.getId());
        ensureSingleActiveFreeTrial(entity, existing.getId());
        packageMapper.updateById(entity);
    }

    @Override
    public void deletePackage(Long id) {
        if (packageMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception0(404, "套餐不存在");
        }
        packageMapper.deleteById(id);
    }

    @Override
    public List<PackageDO> getPublicPackageList() {
        return getPublicPackageList(null);
    }

    @Override
    public List<PackageDO> getPublicPackageList(String currency) {
        LambdaQueryWrapperX<PackageDO> wrapper = new LambdaQueryWrapperX<PackageDO>()
                .eq(PackageDO::getIsActive, true)
                .eq(PackageDO::getIsFreeTrial, false);
        if (currency != null && !currency.isBlank()) {
            wrapper.eq(PackageDO::getCurrency, currency.toUpperCase());
        }
        return packageMapper.selectList(wrapper
                .orderByAsc(PackageDO::getSort)
                .orderByDesc(PackageDO::getId));
    }

    /**
     * 字段默认值兜底(可选字段在 SaveReqVO 上不强制)。
     */
    private void applyDefaults(PackageDO entity) {
        if (entity.getCurrency() == null || entity.getCurrency().isBlank()) {
            entity.setCurrency("HKD");
        }
        if (entity.getIsFreeTrial() == null) {
            entity.setIsFreeTrial(false);
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getPrice() == null) {
            entity.setPrice(BigDecimal.ZERO);
        }
    }

    /**
     * 业务规则校验:免费体验课 price 必须为 0,且 weeklyCount 应为 NULL(语义上不限制每周节奏)。
     */
    private void validateBusinessRules(PackageDO entity, Long updatingId) {
        if (Boolean.TRUE.equals(entity.getIsFreeTrial())) {
            if (entity.getPrice().compareTo(BigDecimal.ZERO) != 0) {
                throw ServiceExceptionUtil.exception0(400, "免费体验套餐 price 必须为 0");
            }
            if (entity.getWeeklyCount() != null) {
                throw ServiceExceptionUtil.exception0(400, "免费体验套餐 weeklyCount 必须为空");
            }
        }
    }

    /**
     * 全表最多 1 条 is_free_trial=true AND is_active=true(PRD §S3 决策:免费体验课唯一)。
     */
    private void ensureSingleActiveFreeTrial(PackageDO entity, Long updatingId) {
        if (!Boolean.TRUE.equals(entity.getIsFreeTrial()) || !Boolean.TRUE.equals(entity.getIsActive())) {
            return;
        }
        PackageDO existing = packageMapper.selectOne(Wrappers.<PackageDO>lambdaQuery()
                .eq(PackageDO::getIsFreeTrial, true)
                .eq(PackageDO::getIsActive, true)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), updatingId)) {
            throw ServiceExceptionUtil.exception0(409,
                    "已存在启用中的免费体验套餐(id=" + existing.getId() + "),请先停用旧的");
        }
    }

}
