package com.mandarly.boot.module.edu.dal.mysql.payment;

import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.dal.dataobject.payment.RefundDO;
import com.mandarly.boot.module.edu.enums.payment.RefundStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface RefundMapper extends BaseMapperX<RefundDO> {

    default Optional<RefundDO> selectActivePendingByPayment(Long paymentId) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapperX<RefundDO>()
                .eq(RefundDO::getPaymentId, paymentId)
                .in(RefundDO::getStatus, List.of(RefundStatusEnum.PENDING.getCode(), RefundStatusEnum.APPROVED.getCode()))
                .last("LIMIT 1")));
    }

    /**
     * 查询孤儿退款:status=approved + channel_refund_id IS NULL + create_time < cutoff
     *
     * @param cutoff 时间截止点(now - 1h)
     * @return 孤儿退款列表
     */
    default List<RefundDO> selectOrphanApproved(LocalDateTime cutoff) {
        return selectList(new LambdaQueryWrapperX<RefundDO>()
                .eq(RefundDO::getStatus, RefundStatusEnum.APPROVED.getCode())
                .isNull(RefundDO::getChannelRefundId)
                .lt(RefundDO::getCreateTime, cutoff));
    }
}
