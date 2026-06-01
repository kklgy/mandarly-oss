package com.mandarly.boot.module.edu.service.classroom.dto;

import lombok.Builder;
import lombok.Data;

/**
 * LCIC 课堂房间创建结果
 */
@Data
@Builder
public class LcicRoom {

    /** LCIC 房间 id;mock 模式形如 "mock-{orderId}",real 模式由 LCIC 返回 */
    private String lcicClassId;

    /** 房间基础 URL(不含用户 token);student / teacher 进课堂时另外拼 token */
    private String lcicRoomUrl;

}
