package com.mandarly.boot.module.edu.service.classroom;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.MeetingDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.booking.MeetingMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.booking.OrderStatusEnum;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicCreateRoomRequest;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicJoinInfo;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicRoom;
import com.mandarly.boot.module.edu.service.income.event.OrderFinishedEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {

    /** stub 占位 lcicClassId 前缀;getJoinInfo 检测到该前缀会触发同步重试 */
    private static final String STUB_PREFIX = "stub-";

    @Resource
    private LcicClient lcicClient;

    @Resource
    private MeetingMapper meetingMapper;

    @Resource
    private CourseOrderMapper courseOrderMapper;

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public MeetingDO openRoomForOrder(Long orderId) {
        CourseOrderDO order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw ServiceExceptionUtil.exception0(404, "订单不存在 orderId=" + orderId);
        }
        UserDO student = eduUserMapper.selectById(order.getStudentId());
        UserDO teacher = eduUserMapper.selectById(order.getTeacherId());

        MeetingDO meeting = new MeetingDO();
        meeting.setOrderId(order.getId());
        meeting.setStatus("created");
        // @PermitAll 上下文兜底,与 BookingServiceImpl 同模式
        meeting.setCreator("anonymous");
        meeting.setUpdater("anonymous");

        try {
            LcicRoom room = lcicClient.createRoom(LcicCreateRoomRequest.builder()
                    .orderId(order.getId())
                    .teacherId(order.getTeacherId())
                    .studentId(order.getStudentId())
                    .teacherName(teacher != null ? teacher.getNickname() : null)
                    .studentName(student != null ? student.getNickname() : null)
                    .scheduledAt(order.getScheduledAt())
                    .durationMinutes(order.getDuration())
                    .build());
            meeting.setLcicClassId(room.getLcicClassId());
            meeting.setLcicRoomUrl(room.getLcicRoomUrl());
            log.info("[openRoomForOrder] orderId={} lcicClassId={}", order.getId(), room.getLcicClassId());
        } catch (Exception ex) {
            // 决策位 (b):LCIC 失败不回滚下单事务,改 stub 占位 + 异步重试
            String stubId = STUB_PREFIX + order.getId();
            meeting.setLcicClassId(stubId);
            meeting.setLcicRoomUrl(null);
            log.error("[openRoomForOrder] LCIC createRoom 失败 orderId={},改 stub 占位等异步重试", order.getId(), ex);
        }
        meetingMapper.insert(meeting);
        return meeting;
    }

    @Override
    public LcicJoinInfo getJoinInfo(Long orderId, Long userId) {
        CourseOrderDO order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw ServiceExceptionUtil.exception0(404, "订单不存在 orderId=" + orderId);
        }
        // 角色判定
        String role;
        if (userId.equals(order.getStudentId())) {
            role = "student";
        } else if (userId.equals(order.getTeacherId())) {
            role = "teacher";
        } else {
            throw ServiceExceptionUtil.exception0(403, "当前用户与该订单无关,无权进入课堂");
        }
        // 状态校验:仅 upcoming 可进
        if (!OrderStatusEnum.UPCOMING.getCode().equals(order.getStatus())) {
            throw ServiceExceptionUtil.exception0(409,
                    "订单当前状态 " + order.getStatus() + ",无法进入课堂");
        }

        MeetingDO meeting = meetingMapper.selectById(orderId);
        if (meeting == null) {
            throw ServiceExceptionUtil.exception0(500, "meeting 缺失,数据异常 orderId=" + orderId);
        }

        // stub 修复:若上次创单时 LCIC 失败留了 stub-,这里同步重试一次
        if (meeting.getLcicClassId() != null && meeting.getLcicClassId().startsWith(STUB_PREFIX)) {
            log.info("[getJoinInfo] 发现 stub meeting orderId={},尝试同步重试 createRoom", orderId);
            UserDO student = eduUserMapper.selectById(order.getStudentId());
            UserDO teacher = eduUserMapper.selectById(order.getTeacherId());
            try {
                LcicRoom room = lcicClient.createRoom(LcicCreateRoomRequest.builder()
                        .orderId(order.getId())
                        .teacherId(order.getTeacherId())
                        .studentId(order.getStudentId())
                        .teacherName(teacher != null ? teacher.getNickname() : null)
                        .studentName(student != null ? student.getNickname() : null)
                        .scheduledAt(order.getScheduledAt())
                        .durationMinutes(order.getDuration())
                        .build());
                meeting.setLcicClassId(room.getLcicClassId());
                meeting.setLcicRoomUrl(room.getLcicRoomUrl());
                meetingMapper.updateById(meeting);
            } catch (Exception ex) {
                log.error("[getJoinInfo] stub 修复失败 orderId={}", orderId, ex);
                throw ServiceExceptionUtil.exception0(502, "课堂房间不可用,请稍后再试");
            }
        }

        // 实时签发 join token
        LcicJoinInfo info;
        try {
            info = lcicClient.generateJoinInfo(meeting.getLcicClassId(), userId, role);
        } catch (Exception ex) {
            log.error("[getJoinInfo] generateJoinInfo 失败 orderId={} userId={} role={}", orderId, userId, role, ex);
            throw ServiceExceptionUtil.exception0(502, "课堂入场凭证签发失败,请稍后再试");
        }

        // 落最后一次 join URL + 过期时间(便于审计 / webhook 对账)
        if ("student".equals(role)) {
            meeting.setStudentJoinUrl(info.getJoinUrl());
            meeting.setStudentTokenExpiresAt(info.getExpiresAt());
        } else {
            meeting.setTeacherJoinUrl(info.getJoinUrl());
            meeting.setTeacherTokenExpiresAt(info.getExpiresAt());
        }
        meetingMapper.updateById(meeting);
        return info;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRoomStart(String lcicClassId) {
        MeetingDO meeting = meetingMapper.selectOne(MeetingDO::getLcicClassId, lcicClassId);
        if (meeting == null) {
            log.warn("[handleRoomStart] meeting 未找到 lcicClassId={},忽略", lcicClassId);
            return;
        }
        if ("ongoing".equals(meeting.getStatus()) || "ended".equals(meeting.getStatus())) {
            log.info("[handleRoomStart] meeting 状态已 {},幂等跳过 lcicClassId={}", meeting.getStatus(), lcicClassId);
            return;
        }
        meeting.setStatus("ongoing");
        meeting.setStartedAt(LocalDateTime.now());
        meetingMapper.updateById(meeting);
        log.info("[handleRoomStart] orderId={} lcicClassId={} → ongoing", meeting.getOrderId(), lcicClassId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRoomEnd(String lcicClassId) {
        MeetingDO meeting = meetingMapper.selectOne(MeetingDO::getLcicClassId, lcicClassId);
        if (meeting == null) {
            log.warn("[handleRoomEnd] meeting 未找到 lcicClassId={},忽略", lcicClassId);
            return;
        }
        if ("ended".equals(meeting.getStatus())) {
            log.info("[handleRoomEnd] meeting 已 ended,幂等跳过 lcicClassId={}", lcicClassId);
            return;
        }
        meeting.setStatus("ended");
        meeting.setEndedAt(LocalDateTime.now());
        meetingMapper.updateById(meeting);

        CourseOrderDO order = courseOrderMapper.selectById(meeting.getOrderId());
        if (order != null && OrderStatusEnum.UPCOMING.getCode().equals(order.getStatus())) {
            order.setStatus(OrderStatusEnum.FINISHED.getCode());
            courseOrderMapper.updateById(order);
            // AFTER_COMMIT 异步触发 settle;失败由 TeacherIncomeBackfillJob 兜底
            applicationEventPublisher.publishEvent(new OrderFinishedEvent(meeting.getOrderId()));
            log.info("[handleRoomEnd] orderId={} → finished, OrderFinishedEvent published", meeting.getOrderId());
        }
    }

}
