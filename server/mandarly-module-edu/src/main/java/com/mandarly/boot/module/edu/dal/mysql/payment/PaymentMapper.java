package com.mandarly.boot.module.edu.dal.mysql.payment;

import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;
import com.mandarly.boot.module.edu.enums.payment.PaymentStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PaymentMapper extends BaseMapperX<PaymentDO> {

    default Optional<PaymentDO> selectBySessionId(String sessionId) {
        return Optional.ofNullable(selectOne(PaymentDO::getChannelSessionId, sessionId));
    }

    default Optional<PaymentDO> selectByChargeId(String chargeId) {
        return Optional.ofNullable(selectOne(PaymentDO::getChannelChargeId, chargeId));
    }

    default Optional<PaymentDO> selectByPaymentIntentId(String paymentIntentId) {
        return Optional.ofNullable(selectOne(PaymentDO::getChannelPaymentIntentId, paymentIntentId));
    }

    default Optional<PaymentDO> selectRecentPendingByUserAndPackage(
            Long userId, Long packageId, LocalDateTime sinceTime) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapperX<PaymentDO>()
                .eq(PaymentDO::getUserId, userId)
                .eq(PaymentDO::getPackageId, packageId)
                .eq(PaymentDO::getStatus, PaymentStatusEnum.PENDING.getCode())
                .ge(PaymentDO::getCreateTime, sinceTime)
                .orderByDesc(PaymentDO::getCreateTime)
                .last("LIMIT 1")));
    }

    default boolean existsPaidByUser(Long userId) {
        return selectCount(new LambdaQueryWrapperX<PaymentDO>()
                .eq(PaymentDO::getUserId, userId)
                .eq(PaymentDO::getStatus, PaymentStatusEnum.PAID.getCode())) > 0;
    }
}
