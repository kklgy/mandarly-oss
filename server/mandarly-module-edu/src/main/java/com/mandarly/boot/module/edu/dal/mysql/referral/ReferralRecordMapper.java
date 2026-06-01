package com.mandarly.boot.module.edu.dal.mysql.referral;

import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.dal.dataobject.referral.ReferralRecordDO;
import com.mandarly.boot.module.edu.enums.referral.ReferralStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper
public interface ReferralRecordMapper extends BaseMapperX<ReferralRecordDO> {

    default Optional<ReferralRecordDO> selectByReferee(Long refereeUserId) {
        return Optional.ofNullable(selectOne(ReferralRecordDO::getRefereeUserId, refereeUserId));
    }

    default int countByReferrer(Long referrerUserId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapperX<ReferralRecordDO>()
                .eq(ReferralRecordDO::getReferrerUserId, referrerUserId)
                .eq(ReferralRecordDO::getStatus, ReferralStatusEnum.REWARDED.getCode())));
    }
}
