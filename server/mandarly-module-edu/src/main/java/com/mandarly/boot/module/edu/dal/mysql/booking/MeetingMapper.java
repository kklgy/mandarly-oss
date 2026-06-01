package com.mandarly.boot.module.edu.dal.mysql.booking;

import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.module.edu.dal.dataobject.booking.MeetingDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeetingMapper extends BaseMapperX<MeetingDO> {

}
