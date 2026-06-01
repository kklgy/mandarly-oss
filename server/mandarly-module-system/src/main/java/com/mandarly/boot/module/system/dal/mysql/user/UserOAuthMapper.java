package com.mandarly.boot.module.system.dal.mysql.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.system.dal.dataobject.user.UserOAuthDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserOAuthMapper extends BaseMapperX<UserOAuthDO> {

    default UserOAuthDO selectActiveByProviderAndUid(String provider, String oauthUid) {
        return selectOne(Wrappers.<UserOAuthDO>lambdaQuery()
            .eq(UserOAuthDO::getProvider, provider)
            .eq(UserOAuthDO::getOauthUid, oauthUid)
            .isNull(UserOAuthDO::getUnboundAt));
    }

    default UserOAuthDO selectAnyByProviderAndUid(String provider, String oauthUid) {
        return selectOne(Wrappers.<UserOAuthDO>lambdaQuery()
            .eq(UserOAuthDO::getProvider, provider)
            .eq(UserOAuthDO::getOauthUid, oauthUid));
    }

    default List<UserOAuthDO> selectActiveByUserId(Long userId) {
        return selectList(Wrappers.<UserOAuthDO>lambdaQuery()
            .eq(UserOAuthDO::getUserId, userId)
            .isNull(UserOAuthDO::getUnboundAt));
    }

    default UserOAuthDO selectActiveByUserAndProvider(Long userId, String provider) {
        return selectOne(Wrappers.<UserOAuthDO>lambdaQuery()
            .eq(UserOAuthDO::getUserId, userId)
            .eq(UserOAuthDO::getProvider, provider)
            .isNull(UserOAuthDO::getUnboundAt));
    }
}
