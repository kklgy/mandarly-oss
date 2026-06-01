package com.mandarly.boot.module.edu.dal.mysql.levelcheck;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckOptionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface LevelCheckOptionMapper extends BaseMapperX<LevelCheckOptionDO> {

    default List<LevelCheckOptionDO> selectListByQuestionId(Long questionId) {
        return selectList(Wrappers.<LevelCheckOptionDO>lambdaQuery()
                .eq(LevelCheckOptionDO::getQuestionId, questionId)
                .orderByAsc(LevelCheckOptionDO::getSort)
                .orderByAsc(LevelCheckOptionDO::getId));
    }

    default List<LevelCheckOptionDO> selectListByQuestionIds(Collection<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return List.of();
        }
        return selectList(Wrappers.<LevelCheckOptionDO>lambdaQuery()
                .in(LevelCheckOptionDO::getQuestionId, questionIds)
                .orderByAsc(LevelCheckOptionDO::getSort)
                .orderByAsc(LevelCheckOptionDO::getId));
    }

}
