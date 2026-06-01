package com.mandarly.boot.module.edu.dal.mysql.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.user.EduUserOauthDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper
public interface UserOauthMapper extends BaseMapperX<EduUserOauthDO> {

    default List<EduUserOauthDO> selectActiveByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(Wrappers.<EduUserOauthDO>lambdaQuery()
                .in(EduUserOauthDO::getUserId, userIds)
                .isNull(EduUserOauthDO::getUnboundAt)
                .orderByAsc(EduUserOauthDO::getProvider));
    }

}
