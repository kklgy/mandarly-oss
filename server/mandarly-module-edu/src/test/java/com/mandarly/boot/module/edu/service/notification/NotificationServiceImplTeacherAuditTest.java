package com.mandarly.boot.module.edu.service.notification;

import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import com.mandarly.boot.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import com.mandarly.boot.module.system.api.sms.SmsSendApi;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceImpl 教师资质审核结果通知 TDD 单元测试(D19-A5)
 *
 * <p>D19 一期仅实装 mail 通道,故测试只覆盖 mail 路径 + 容错:
 * <ol>
 *   <li>approved → 调用 mailSendApi,templateCode 含 "approved"</li>
 *   <li>rejected → templateParams 含 rejectReason</li>
 *   <li>user 不存在 → 早返回,mailSendApi 不调用</li>
 *   <li>email 空 → 跳过 mail,mailSendApi 不调用</li>
 *   <li>mailSendApi throws → 不外抛(try/catch 容错)</li>
 * </ol>
 *
 * <p>注:反射方法 reflectGetUser 在单元测试中通过 spy 覆盖,避免 SpringUtil 依赖
 * (沿用 ReferralServiceImpl#findUserByReferralCode 模式)。SMS / inbox 通道 D19 一期未实装,不测。
 */
class NotificationServiceImplTeacherAuditTest extends BaseMockitoUnitTest {

    @Spy
    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private MailSendApi mailSendApi;

    @Mock
    private SmsSendApi smsSendApi;

    // ======================== helpers ========================

    private AppUserDO teacherWith(String email, String locale) {
        return teacherWith(email, locale, "Test Teacher");
    }

    private AppUserDO teacherWith(String email, String locale, String nickname) {
        AppUserDO u = new AppUserDO();
        u.setId(101L);
        u.setEmail(email);
        u.setLocale(locale);
        u.setNickname(nickname);
        u.setRole("teacher");
        return u;
    }

    // ======================== tests ========================

    /** case 1: approved → mailSendApi 收到 templateCode 含 "approved" */
    @Test
    void approved_sendsMailWithApprovedTemplate() {
        Long teacherUserId = 101L;
        AppUserDO teacher = teacherWith("teacher@example.com", "zh-CN");
        doReturn(teacher).when(notificationService).reflectGetUser(teacherUserId);

        notificationService.sendForTeacherAuditEvent("approved", teacherUserId, null);

        ArgumentCaptor<MailSendSingleToUserReqDTO> captor =
                ArgumentCaptor.forClass(MailSendSingleToUserReqDTO.class);
        verify(mailSendApi, times(1)).sendSingleMailToMember(captor.capture());

        MailSendSingleToUserReqDTO req = captor.getValue();
        assertThat(req.getTemplateCode()).contains("approved");
        assertThat(req.getTemplateCode()).isEqualTo("mandarly_teacher_audit_approved_zh_cn");
        assertThat(req.getToMails()).containsExactly("teacher@example.com");
        assertThat(req.getTemplateParams()).containsKey("statusText");
        // 模板正文用 {nickname} 起手,必须有该 key 否则会渲染字面值
        assertThat(req.getTemplateParams()).containsEntry("nickname", "Test Teacher");
        // approved 不带 rejectReason
        assertThat(req.getTemplateParams()).doesNotContainKey("rejectReason");
    }

    /** case 6: nickname 缺失时 fallback 用 email 占位,避免邮件正文留 "{nickname}" 字面 */
    @Test
    void missingNickname_fallsBackToEmail() {
        Long teacherUserId = 101L;
        AppUserDO teacher = teacherWith("teacher@example.com", "zh-CN", null);
        doReturn(teacher).when(notificationService).reflectGetUser(teacherUserId);

        notificationService.sendForTeacherAuditEvent("approved", teacherUserId, null);

        ArgumentCaptor<MailSendSingleToUserReqDTO> captor =
                ArgumentCaptor.forClass(MailSendSingleToUserReqDTO.class);
        verify(mailSendApi, times(1)).sendSingleMailToMember(captor.capture());
        assertThat(captor.getValue().getTemplateParams())
                .containsEntry("nickname", "teacher@example.com");
    }

    /** case 2: rejected → templateParams 含 rejectReason */
    @Test
    void rejected_includesReasonInTemplateParams() {
        Long teacherUserId = 101L;
        AppUserDO teacher = teacherWith("teacher@example.com", "en");
        doReturn(teacher).when(notificationService).reflectGetUser(teacherUserId);

        notificationService.sendForTeacherAuditEvent("rejected", teacherUserId, "Certificate unclear");

        ArgumentCaptor<MailSendSingleToUserReqDTO> captor =
                ArgumentCaptor.forClass(MailSendSingleToUserReqDTO.class);
        verify(mailSendApi, times(1)).sendSingleMailToMember(captor.capture());

        MailSendSingleToUserReqDTO req = captor.getValue();
        assertThat(req.getTemplateCode()).isEqualTo("mandarly_teacher_audit_rejected_en");
        assertThat(req.getTemplateParams())
                .containsEntry("rejectReason", "Certificate unclear");
        assertThat(req.getTemplateParams()).containsKey("statusText");
    }

    /** case 3: user 不存在 → 早返回,mailSendApi 不调用 */
    @Test
    void userNotFound_returnsEarly() {
        Long teacherUserId = 999L;
        doReturn(null).when(notificationService).reflectGetUser(teacherUserId);

        notificationService.sendForTeacherAuditEvent("approved", teacherUserId, null);

        verify(mailSendApi, never()).sendSingleMailToMember(any());
        verify(smsSendApi, never()).sendSingleSmsToMember(any());
    }

    /** case 4: user 邮箱为空 → 跳过 mail,mailSendApi 不调用 */
    @Test
    void emptyEmail_skipsMail() {
        Long teacherUserId = 101L;
        AppUserDO teacher = teacherWith("", "zh-CN");
        doReturn(teacher).when(notificationService).reflectGetUser(teacherUserId);

        notificationService.sendForTeacherAuditEvent("approved", teacherUserId, null);

        verify(mailSendApi, never()).sendSingleMailToMember(any());
    }

    /** case 5: mailSendApi throws → 不外抛(try/catch 容错) */
    @Test
    void mailSendApiThrows_doesNotPropagate() {
        Long teacherUserId = 101L;
        AppUserDO teacher = teacherWith("teacher@example.com", "zh-CN");
        doReturn(teacher).when(notificationService).reflectGetUser(teacherUserId);
        doThrow(new RuntimeException("smtp down"))
                .when(mailSendApi).sendSingleMailToMember(any());

        // 不外抛即通过
        notificationService.sendForTeacherAuditEvent("approved", teacherUserId, null);

        verify(mailSendApi, times(1)).sendSingleMailToMember(any());
    }
}
