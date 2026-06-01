package com.mandarly.boot.module.system.dal.mysql.user;

import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppUserMapper extends BaseMapperX<AppUserDO> {

    default AppUserDO selectByEmail(String email) {
        return selectOne(AppUserDO::getEmail, email);
    }

    default AppUserDO selectByPhone(String phone) {
        return selectOne(AppUserDO::getPhone, phone);
    }

    default AppUserDO selectByReferralCode(String code) {
        return selectOne(AppUserDO::getReferralCode, code);
    }
}
