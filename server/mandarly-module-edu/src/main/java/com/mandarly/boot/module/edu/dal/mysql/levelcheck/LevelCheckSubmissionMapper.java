package com.mandarly.boot.module.edu.dal.mysql.levelcheck;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckSubmissionPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckSubmissionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LevelCheckSubmissionMapper extends BaseMapperX<LevelCheckSubmissionDO> {

    default PageResult<LevelCheckSubmissionDO> selectPage(LevelCheckSubmissionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<LevelCheckSubmissionDO>()
                .eqIfPresent(LevelCheckSubmissionDO::getLocale, reqVO.getLocale())
                .eqIfPresent(LevelCheckSubmissionDO::getInferredLevel, reqVO.getInferredLevel())
                .eqIfPresent(LevelCheckSubmissionDO::getIsConverted, reqVO.getIsConverted())
                .geIfPresent(LevelCheckSubmissionDO::getCreateTime, reqVO.getCreateTimeFrom())
                .leIfPresent(LevelCheckSubmissionDO::getCreateTime, reqVO.getCreateTimeTo())
                .orderByDesc(LevelCheckSubmissionDO::getCreateTime));
    }

}
