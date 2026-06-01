package com.mandarly.boot.module.edu.dal.mysql.levelcheck;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckQuestionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LevelCheckQuestionMapper extends BaseMapperX<LevelCheckQuestionDO> {

    default PageResult<LevelCheckQuestionDO> selectPage(LevelCheckQuestionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<LevelCheckQuestionDO>()
                .eqIfPresent(LevelCheckQuestionDO::getQuestionCode, reqVO.getQuestionCode())
                .eqIfPresent(LevelCheckQuestionDO::getIsActive, reqVO.getIsActive())
                .orderByAsc(LevelCheckQuestionDO::getSort)
                .orderByAsc(LevelCheckQuestionDO::getId));
    }

    default List<LevelCheckQuestionDO> selectActiveOrderBySort() {
        return selectList(Wrappers.<LevelCheckQuestionDO>lambdaQuery()
                .eq(LevelCheckQuestionDO::getIsActive, true)
                .orderByAsc(LevelCheckQuestionDO::getSort)
                .orderByAsc(LevelCheckQuestionDO::getId));
    }

}
