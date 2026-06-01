package com.mandarly.boot.module.edu.service.booking;

import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.MeetingDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.booking.MeetingMapper;
import com.mandarly.boot.module.edu.service.income.event.OrderFinishedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * D28 BookingServiceImpl#sweepOverdueUpcoming 分流单测
 *
 * <p>覆盖 6 个 meeting 状态分支 + 边界:
 * <ol>
 *   <li>meeting == null → abnormal/meeting_missing</li>
 *   <li>meeting.status=ended → finished + publish OrderFinishedEvent</li>
 *   <li>meeting.status=ongoing → abnormal/meeting_ongoing_overdue</li>
 *   <li>meeting.status=created + lcic_class_id=stub-* → abnormal/lcic_init_failed</li>
 *   <li>meeting.status=created + 真实 lcicClassId → abnormal/lcic_no_attendance</li>
 *   <li>meeting.status=cancelled → abnormal/meeting_cancelled_orphan</li>
 *   <li>空列表 → scanned=0,不动 mapper</li>
 *   <li>单条异常不影响后续条目</li>
 * </ol>
 */
class BookingServiceImplSweepOverdueTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private CourseOrderMapper courseOrderMapper;

    @Mock
    private MeetingMapper meetingMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CourseOrderDO buildOrder(Long id) {
        CourseOrderDO o = new CourseOrderDO();
        o.setId(id);
        o.setStatus("upcoming");
        o.setScheduledAt(LocalDateTime.now().minusHours(2));
        o.setDuration(30);
        return o;
    }

    private MeetingDO buildMeeting(Long orderId, String status, String lcicClassId) {
        MeetingDO m = new MeetingDO();
        m.setOrderId(orderId);
        m.setStatus(status);
        m.setLcicClassId(lcicClassId);
        return m;
    }

    @Test
    void emptyScan_noUpdate_returnsScanned0() {
        when(courseOrderMapper.selectOverdueUpcoming(any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of());

        String summary = bookingService.sweepOverdueUpcoming();

        assertThat(summary).isEqualTo("scanned=0");
        verify(courseOrderMapper, never()).updateById(any(CourseOrderDO.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void meetingMissing_marksAbnormalMeetingMissing() {
        CourseOrderDO o = buildOrder(101L);
        when(courseOrderMapper.selectOverdueUpcoming(any(), anyInt())).thenReturn(List.of(o));
        when(meetingMapper.selectById(101L)).thenReturn(null);

        String summary = bookingService.sweepOverdueUpcoming();

        ArgumentCaptor<CourseOrderDO> cap = ArgumentCaptor.forClass(CourseOrderDO.class);
        verify(courseOrderMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("abnormal");
        assertThat(cap.getValue().getAbnormalReason()).isEqualTo("meeting_missing");
        assertThat(summary).contains("abnormal=1", "meeting_missing=1");
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void meetingEnded_marksFinishedAndPublishesEvent() {
        CourseOrderDO o = buildOrder(102L);
        LocalDateTime endedAt = LocalDateTime.now().minusMinutes(20);
        MeetingDO m = buildMeeting(102L, "ended", "lcic-real-123");
        m.setEndedAt(endedAt);
        when(courseOrderMapper.selectOverdueUpcoming(any(), anyInt())).thenReturn(List.of(o));
        when(meetingMapper.selectById(102L)).thenReturn(m);

        String summary = bookingService.sweepOverdueUpcoming();

        ArgumentCaptor<CourseOrderDO> cap = ArgumentCaptor.forClass(CourseOrderDO.class);
        verify(courseOrderMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("finished");
        assertThat(cap.getValue().getFinishedAt()).isEqualTo(endedAt);
        ArgumentCaptor<OrderFinishedEvent> evt = ArgumentCaptor.forClass(OrderFinishedEvent.class);
        verify(applicationEventPublisher).publishEvent(evt.capture());
        assertThat(evt.getValue().orderId()).isEqualTo(102L);
        assertThat(summary).contains("finished=1");
    }

    @Test
    void meetingOngoing_marksAbnormalOngoingOverdue() {
        CourseOrderDO o = buildOrder(103L);
        MeetingDO m = buildMeeting(103L, "ongoing", "lcic-real-456");
        when(courseOrderMapper.selectOverdueUpcoming(any(), anyInt())).thenReturn(List.of(o));
        when(meetingMapper.selectById(103L)).thenReturn(m);

        String summary = bookingService.sweepOverdueUpcoming();

        ArgumentCaptor<CourseOrderDO> cap = ArgumentCaptor.forClass(CourseOrderDO.class);
        verify(courseOrderMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("abnormal");
        assertThat(cap.getValue().getAbnormalReason()).isEqualTo("meeting_ongoing_overdue");
        assertThat(summary).contains("meeting_ongoing_overdue=1");
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void meetingCreatedStub_marksAbnormalLcicInitFailed() {
        CourseOrderDO o = buildOrder(104L);
        MeetingDO m = buildMeeting(104L, "created", "stub-104");
        when(courseOrderMapper.selectOverdueUpcoming(any(), anyInt())).thenReturn(List.of(o));
        when(meetingMapper.selectById(104L)).thenReturn(m);

        String summary = bookingService.sweepOverdueUpcoming();

        ArgumentCaptor<CourseOrderDO> cap = ArgumentCaptor.forClass(CourseOrderDO.class);
        verify(courseOrderMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("abnormal");
        assertThat(cap.getValue().getAbnormalReason()).isEqualTo("lcic_init_failed");
        assertThat(summary).contains("lcic_init_failed=1");
    }

    @Test
    void meetingCreatedReal_marksAbnormalLcicNoAttendance() {
        CourseOrderDO o = buildOrder(105L);
        MeetingDO m = buildMeeting(105L, "created", "lcic-real-789");
        when(courseOrderMapper.selectOverdueUpcoming(any(), anyInt())).thenReturn(List.of(o));
        when(meetingMapper.selectById(105L)).thenReturn(m);

        String summary = bookingService.sweepOverdueUpcoming();

        ArgumentCaptor<CourseOrderDO> cap = ArgumentCaptor.forClass(CourseOrderDO.class);
        verify(courseOrderMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("abnormal");
        assertThat(cap.getValue().getAbnormalReason()).isEqualTo("lcic_no_attendance");
        assertThat(summary).contains("lcic_no_attendance=1");
    }

    @Test
    void meetingCancelled_marksAbnormalOrphan() {
        CourseOrderDO o = buildOrder(106L);
        MeetingDO m = buildMeeting(106L, "cancelled", "lcic-real-999");
        when(courseOrderMapper.selectOverdueUpcoming(any(), anyInt())).thenReturn(List.of(o));
        when(meetingMapper.selectById(106L)).thenReturn(m);

        String summary = bookingService.sweepOverdueUpcoming();

        ArgumentCaptor<CourseOrderDO> cap = ArgumentCaptor.forClass(CourseOrderDO.class);
        verify(courseOrderMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("abnormal");
        assertThat(cap.getValue().getAbnormalReason()).isEqualTo("meeting_cancelled_orphan");
        assertThat(summary).contains("meeting_cancelled_orphan=1");
    }

    /** 单条异常不阻塞后续:第一条抛,第二条仍要被处理。 */
    @Test
    void singleFailure_doesNotBlockOthers() {
        CourseOrderDO bad = buildOrder(201L);
        CourseOrderDO good = buildOrder(202L);
        when(courseOrderMapper.selectOverdueUpcoming(any(), anyInt())).thenReturn(List.of(bad, good));
        when(meetingMapper.selectById(201L)).thenThrow(new RuntimeException("simulated"));
        when(meetingMapper.selectById(202L))
                .thenReturn(buildMeeting(202L, "created", "stub-202"));

        String summary = bookingService.sweepOverdueUpcoming();

        // 202 被正常处理
        verify(courseOrderMapper).updateById(any(CourseOrderDO.class));
        assertThat(summary).contains("scanned=2", "abnormal=1", "lcic_init_failed=1");
    }

    /** 未知 meeting.status 兜底 → abnormal/meeting_unknown_status,可观测但不漏数据。 */
    @Test
    void meetingUnknownStatus_marksAbnormalUnknown() {
        CourseOrderDO o = buildOrder(107L);
        MeetingDO m = buildMeeting(107L, "weird-future-status", "lcic-x");
        when(courseOrderMapper.selectOverdueUpcoming(any(), anyInt())).thenReturn(List.of(o));
        when(meetingMapper.selectById(107L)).thenReturn(m);

        String summary = bookingService.sweepOverdueUpcoming();

        ArgumentCaptor<CourseOrderDO> cap = ArgumentCaptor.forClass(CourseOrderDO.class);
        verify(courseOrderMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("abnormal");
        assertThat(cap.getValue().getAbnormalReason()).isEqualTo("meeting_unknown_status");
        assertThat(summary).contains("meeting_unknown_status=1");
    }

    /** cutoff 计算:Service 内传给 mapper 的 cutoff 应当是 now - 15min(允许 ±2s 漂移)。 */
    @Test
    void cutoffPassedToMapper_equalsNowMinus15min() {
        when(courseOrderMapper.selectOverdueUpcoming(any(LocalDateTime.class), eq(1000)))
                .thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now().minusMinutes(15);
        bookingService.sweepOverdueUpcoming();
        LocalDateTime after = LocalDateTime.now().minusMinutes(15);

        ArgumentCaptor<LocalDateTime> cap = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(courseOrderMapper).selectOverdueUpcoming(cap.capture(), eq(1000));
        assertThat(cap.getValue()).isBetween(before.minusSeconds(2), after.plusSeconds(2));
    }
}
