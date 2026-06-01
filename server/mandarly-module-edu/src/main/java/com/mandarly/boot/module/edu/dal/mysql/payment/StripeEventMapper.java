package com.mandarly.boot.module.edu.dal.mysql.payment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mandarly.boot.module.edu.dal.dataobject.payment.StripeEventDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StripeEventMapper extends BaseMapper<StripeEventDO> {

    /**
     * INSERT IGNORE 去重写入,返回受影响行数(1=新事件,0=已处理过)
     */
    @Insert("INSERT IGNORE INTO stripe_event(id, type, payload, processed_at, result, created_at) " +
            "VALUES(#{id}, #{type}, #{payload}, NOW(), 'success', NOW())")
    int insertIgnore(@Param("id") String id,
                     @Param("type") String type,
                     @Param("payload") String payload);
}
