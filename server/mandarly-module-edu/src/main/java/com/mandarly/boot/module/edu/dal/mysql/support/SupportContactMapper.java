package com.mandarly.boot.module.edu.dal.mysql.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportContactPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportContactDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SupportContactMapper extends BaseMapperX<SupportContactDO> {

    default PageResult<SupportContactDO> selectPage(SupportContactPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SupportContactDO>()
                .eqIfPresent(SupportContactDO::getMarket, reqVO.getMarket())
                .eqIfPresent(SupportContactDO::getChannelType, reqVO.getChannelType())
                .likeIfPresent(SupportContactDO::getDisplayText, reqVO.getDisplayText())
                .eqIfPresent(SupportContactDO::getIsActive, reqVO.getIsActive())
                .orderByAsc(SupportContactDO::getMarket)
                .orderByAsc(SupportContactDO::getSort)
                .orderByDesc(SupportContactDO::getId));
    }

    default List<SupportContactDO> selectActiveByMarket(String market) {
        return selectList(Wrappers.<SupportContactDO>lambdaQuery()
                .eq(SupportContactDO::getMarket, market)
                .eq(SupportContactDO::getIsActive, true)
                .orderByAsc(SupportContactDO::getSort)
                .orderByAsc(SupportContactDO::getId));
    }
}
