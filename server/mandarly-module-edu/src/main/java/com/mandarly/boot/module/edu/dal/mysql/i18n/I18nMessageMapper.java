package com.mandarly.boot.module.edu.dal.mysql.i18n;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.i18n.I18nMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface I18nMessageMapper extends BaseMapperX<I18nMessageDO> {

    default List<I18nMessageDO> selectByCodes(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(Wrappers.<I18nMessageDO>lambdaQuery()
                .in(I18nMessageDO::getCode, codes));
    }

    default I18nMessageDO selectByCodeAndLocale(String code, String locale) {
        return selectOne(Wrappers.<I18nMessageDO>lambdaQuery()
                .eq(I18nMessageDO::getCode, code)
                .eq(I18nMessageDO::getLocale, locale)
                .last("LIMIT 1"));
    }

}
