package com.mandarly.boot.module.edu.dal.mysql.pkg;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.pkg.vo.PackagePageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PackageMapper extends BaseMapperX<PackageDO> {

    default PageResult<PackageDO> selectPage(PackagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PackageDO>()
                .likeIfPresent(PackageDO::getNameI18nCode, reqVO.getNameI18nCode())
                .eqIfPresent(PackageDO::getCurrency, reqVO.getCurrency())
                .eqIfPresent(PackageDO::getIsFreeTrial, reqVO.getIsFreeTrial())
                .eqIfPresent(PackageDO::getIsActive, reqVO.getIsActive())
                .orderByAsc(PackageDO::getSort)
                .orderByDesc(PackageDO::getId));
    }

}
