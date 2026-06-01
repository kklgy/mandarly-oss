package com.mandarly.boot.module.edu.dal.mysql.support;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportInquiryLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SupportInquiryLogMapper extends BaseMapperX<SupportInquiryLogDO> {

    default PageResult<SupportInquiryLogDO> selectPage(SupportInquiryPageReqVO reqVO) {
        LambdaQueryWrapperX<SupportInquiryLogDO> wrapper = new LambdaQueryWrapperX<SupportInquiryLogDO>()
                .eqIfPresent(SupportInquiryLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SupportInquiryLogDO::getSessionId, reqVO.getSessionId())
                .eqIfPresent(SupportInquiryLogDO::getLocale, reqVO.getLocale())
                .eqIfPresent(SupportInquiryLogDO::getMarket, reqVO.getMarket())
                .likeIfPresent(SupportInquiryLogDO::getQuestionText, reqVO.getQuestionKeyword())
                .eqIfPresent(SupportInquiryLogDO::getClickedToHuman, reqVO.getClickedToHuman());
        if (Boolean.TRUE.equals(reqVO.getMatched())) {
            wrapper.isNotNull(SupportInquiryLogDO::getMatchedFaqId);
        } else if (Boolean.FALSE.equals(reqVO.getMatched())) {
            wrapper.isNull(SupportInquiryLogDO::getMatchedFaqId);
        }
        return selectPage(reqVO, wrapper.orderByDesc(SupportInquiryLogDO::getId));
    }
}
