package com.mandarly.boot.module.edu.dal.mysql.income;

import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface TeacherBalanceMapper extends BaseMapperX<TeacherBalanceDO> {

    /**
     * 乐观锁 UPDATE — 命中 (teacher_id, version) 才生效;返回 0 行说明版本被并发改了,Service 层抛 ServiceException
     *
     * <p>delta 含义:增量(正数=加,负数=减);所有 4 个字段任一可为 0(无变动)
     */
    @Update("UPDATE teacher_balance SET " +
            "  frozen_t7_usd = frozen_t7_usd + #{deltaFrozenT7}, " +
            "  available_usd = available_usd + #{deltaAvailable}, " +
            "  pending_withdraw_usd = pending_withdraw_usd + #{deltaPending}, " +
            "  total_earned_usd = total_earned_usd + #{deltaTotalEarned}, " +
            "  total_withdrawn_usd = total_withdrawn_usd + #{deltaTotalWithdrawn}, " +
            "  version = version + 1, " +
            "  update_time = NOW() " +
            "WHERE teacher_id = #{teacherId} AND version = #{version} AND deleted = 0")
    int updateBalanceWithOptimisticLock(@Param("teacherId") Long teacherId,
                                        @Param("version") Integer version,
                                        @Param("deltaFrozenT7") BigDecimal deltaFrozenT7,
                                        @Param("deltaAvailable") BigDecimal deltaAvailable,
                                        @Param("deltaPending") BigDecimal deltaPending,
                                        @Param("deltaTotalEarned") BigDecimal deltaTotalEarned,
                                        @Param("deltaTotalWithdrawn") BigDecimal deltaTotalWithdrawn);
}
