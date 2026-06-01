package com.mandarly.boot.module.edu.service.classroom;

import com.mandarly.boot.module.edu.dal.dataobject.booking.MeetingDO;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicJoinInfo;

/**
 * 课堂业务封装(LCIC 适配 + meeting 表写入 + 进入课堂 token 实时签发)。
 *
 * <p>对应 PRD-v4 §4.6 视频课堂。M3 视频联调阶段实现。
 */
public interface ClassroomService {

    /**
     * 为订单开课堂房间并落 meeting 表(创建模式)。
     *
     * <p>失败语义:LCIC 调用失败时**不抛异常**,改用 stub 占位 + meeting.status=create_failed,
     *    便于异步重试 + 不反噬下单事务。
     *
     * <p>调用链:{@code BookingServiceImpl.createOrder} 创单成功后调用。
     *
     * @return 落库后的 MeetingDO(含真实或 stub 的 lcic_class_id)
     */
    MeetingDO openRoomForOrder(Long orderId);

    /**
     * 实时为指定 user 生成 join URL + token。
     *
     * <p>角色判定:userId 等于 order.studentId → student;等于 order.teacherId → teacher;否则 403。
     *
     * <p>状态校验:order 必须 upcoming(unfinished)状态;cancelled/finished 拒绝。
     *
     * <p>异常恢复:若 meeting.status=create_failed(stub),先尝试重试 createRoom;失败则 502。
     */
    LcicJoinInfo getJoinInfo(Long orderId, Long userId);

    /**
     * LCIC webhook room_start 事件处理:meeting.status=ongoing + started_at=now。
     *
     * <p>幂等:已 ongoing/ended 的 meeting 直接跳过。
     */
    void handleRoomStart(String lcicClassId);

    /**
     * LCIC webhook room_end 事件处理:meeting.status=ended + ended_at=now,course_order.status=finished。
     *
     * <p>幂等:已 ended 的 meeting / 已 finished 的订单跳过。
     *
     * <p>结算触发:M6 income 子域接入后在此触发,本期暂不实现。
     */
    void handleRoomEnd(String lcicClassId);

}
