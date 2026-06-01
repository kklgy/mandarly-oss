package com.mandarly.boot.module.edu.dal.mysql.payment;

import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.dal.dataobject.payment.UserPaymentProfileDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper
public interface UserPaymentProfileMapper extends BaseMapperX<UserPaymentProfileDO> {

    default Optional<UserPaymentProfileDO> selectByUserAndChannel(Long userId, String channel) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapperX<UserPaymentProfileDO>()
                .eq(UserPaymentProfileDO::getUserId, userId)
                .eq(UserPaymentProfileDO::getChannel, channel)
                .last("LIMIT 1")));
    }
}
