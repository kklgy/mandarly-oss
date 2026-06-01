package com.mandarly.boot.module.edu.dal.mysql.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.FaqDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FaqMapper extends BaseMapperX<FaqDO> {

    default PageResult<FaqDO> selectPage(SupportFaqPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FaqDO>()
                .eqIfPresent(FaqDO::getCategory, reqVO.getCategory())
                .eqIfPresent(FaqDO::getLocale, reqVO.getLocale())
                .eqIfPresent(FaqDO::getStatus, reqVO.getStatus())
                .likeIfPresent(FaqDO::getQuestion, reqVO.getQuestion())
                .orderByAsc(FaqDO::getLocale)
                .orderByAsc(FaqDO::getCategory)
                .orderByAsc(FaqDO::getSort)
                .orderByDesc(FaqDO::getId));
    }

    default List<FaqDO> selectActiveByLocales(List<String> locales) {
        return selectList(Wrappers.<FaqDO>lambdaQuery()
                .in(FaqDO::getLocale, locales)
                .eq(FaqDO::getStatus, "active")
                .orderByAsc(FaqDO::getSort)
                .orderByAsc(FaqDO::getId));
    }

    @Update("UPDATE faq SET match_count = match_count + 1 WHERE id = #{id} AND deleted = 0")
    void incrementMatchCount(@Param("id") Long id);
}
