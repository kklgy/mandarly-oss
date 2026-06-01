package com.mandarly.boot.module.edu.service.notification;

import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import com.mandarly.boot.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import com.mandarly.boot.module.system.api.sms.SmsSendApi;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceImpl 学生预约成功通知教师单元测试(D22)。
 *
 * <p>D22 一期仅实装 mail 通道,通知失败不反噬预约主链路。
 */
class NotificationServiceImplBookingCreatedTest extends BaseMockitoUnitTest {

    @Spy
    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private CourseOrderMapper courseOrderMapper;

    @Mock
    private MailSendApi mailSendApi;

    @Mock
    private SmsSendApi smsSendApi;

    @Test
    void bookingCreated_sendsMailToTeacher() {
        CourseOrderDO order = order();
        when(courseOrderMapper.selectById(5005L)).thenReturn(order);
        doReturn(user(2002L, "teacher@example.com", "zh-CN", "Teacher Wang"))
                .when(notificationService).reflectGetUser(2002L);
        doReturn(user(1001L, "student@example.com", "en", "Student Lee"))
                .when(notificationService).reflectGetUser(1001L);

        notificationService.sendForBookingCreated(5005L);

        ArgumentCaptor<MailSendSingleToUserReqDTO> captor =
                ArgumentCaptor.forClass(MailSendSingleToUserReqDTO.class);
        verify(mailSendApi).sendSingleMailToMember(captor.capture());
        MailSendSingleToUserReqDTO req = captor.getValue();
        assertThat(req.getTemplateCode()).isEqualTo("mandarly_booking_created_zh_cn");
        assertThat(req.getToMails()).containsExactly("teacher@example.com");
        assertThat(req.getTemplateParams())
                .containsEntry("teacherName", "Teacher Wang")
                .containsEntry("studentName", "Student Lee")
                .containsEntry("studentEmail", "student@example.com")
                .containsEntry("orderId", "5005")
                .containsEntry("duration", "30");
        assertThat(req.getTemplateParams()).containsKey("scheduledAt");
        verify(smsSendApi, never()).sendSingleSmsToMember(any());
    }

    @Test
    void bookingCreated_teacherEmailMissing_skipsMail() {
        CourseOrderDO order = order();
        when(courseOrderMapper.selectById(5005L)).thenReturn(order);
        doReturn(user(2002L, "", "zh-CN", "Teacher Wang"))
                .when(notificationService).reflectGetUser(2002L);

        notificationService.sendForBookingCreated(5005L);

        verify(mailSendApi, never()).sendSingleMailToMember(any());
    }

    @Test
    void bookingCreated_mailSendThrows_doesNotPropagate() {
        CourseOrderDO order = order();
        when(courseOrderMapper.selectById(5005L)).thenReturn(order);
        doReturn(user(2002L, "teacher@example.com", "en", "Teacher Wang"))
                .when(notificationService).reflectGetUser(2002L);
        doReturn(user(1001L, "student@example.com", "en", "Student Lee"))
                .when(notificationService).reflectGetUser(1001L);
        doThrow(new RuntimeException("smtp down"))
                .when(mailSendApi).sendSingleMailToMember(any());

        notificationService.sendForBookingCreated(5005L);

        verify(mailSendApi).sendSingleMailToMember(any());
    }

    private CourseOrderDO order() {
        CourseOrderDO order = new CourseOrderDO();
        order.setId(5005L);
        order.setTeacherId(2002L);
        order.setStudentId(1001L);
        order.setScheduledAt(LocalDateTime.of(2026, 5, 21, 10, 0));
        order.setDuration(30);
        return order;
    }

    private AppUserDO user(Long id, String email, String locale, String nickname) {
        AppUserDO user = new AppUserDO();
        user.setId(id);
        user.setEmail(email);
        user.setLocale(locale);
        user.setNickname(nickname);
        return user;
    }
}
