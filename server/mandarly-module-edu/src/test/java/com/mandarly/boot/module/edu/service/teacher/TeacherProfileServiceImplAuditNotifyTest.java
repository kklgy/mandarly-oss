package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfileAuditReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import com.mandarly.boot.module.edu.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TeacherProfileServiceImpl#auditTeacherProfile 通知触发 TDD 单元测试(D19 A6)
 *
 * <p>覆盖:
 * <ol>
 *   <li>approve → notificationService.sendForTeacherAuditEvent("approved", userId, null)</li>
 *   <li>reject  → notificationService.sendForTeacherAuditEvent("rejected", userId, rejectReason)</li>
 *   <li>profile 不存在抛 404 → 不应触发通知(放在 updateById 之后才走通知)</li>
 * </ol>
 *
 * <p>注:仅校验通知触发,不重复 A1/审核基础逻辑(由现有 controller / e2e 兜底)。
 */
class TeacherProfileServiceImplAuditNotifyTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherProfileServiceImpl teacherProfileService;

    @Mock
    private TeacherProfileMapper teacherProfileMapper;

    @Mock
    private EduUserMapper eduUserMapper;

    @Mock
    private NotificationService notificationService;

    /** case 1: approve — 通知事件 = approved,rejectReason = null */
    @Test
    void auditTeacherProfile_approved_invokesNotificationWithApprovedEvent() {
        Long userId = 88001L;
        Long operatorId = 1L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setAuditStatus(TeacherAuditStatusEnum.PENDING.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);

        TeacherProfileAuditReqVO req = new TeacherProfileAuditReqVO();
        req.setUserId(userId);
        req.setAction("approve");

        teacherProfileService.auditTeacherProfile(req, operatorId);

        verify(notificationService).sendForTeacherAuditEvent("approved", userId, null);
    }

    /** case 2: reject — 通知事件 = rejected,带 rejectReason */
    @Test
    void auditTeacherProfile_rejected_invokesNotificationWithReason() {
        Long userId = 88002L;
        Long operatorId = 1L;
        String reason = "证件模糊";
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setAuditStatus(TeacherAuditStatusEnum.PENDING.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);

        TeacherProfileAuditReqVO req = new TeacherProfileAuditReqVO();
        req.setUserId(userId);
        req.setAction("reject");
        req.setRejectReason(reason);

        teacherProfileService.auditTeacherProfile(req, operatorId);

        verify(notificationService).sendForTeacherAuditEvent("rejected", userId, reason);
    }

    /** case 3: profile 不存在 → 早抛异常,通知不触发 */
    @Test
    void auditTeacherProfile_profileMissing_doesNotInvokeNotification() {
        Long userId = 88003L;
        when(teacherProfileMapper.selectById(userId)).thenReturn(null);

        TeacherProfileAuditReqVO req = new TeacherProfileAuditReqVO();
        req.setUserId(userId);
        req.setAction("approve");

        assertThatThrownBy(() -> teacherProfileService.auditTeacherProfile(req, 1L))
                .isInstanceOf(ServiceException.class);

        verify(notificationService, never())
                .sendForTeacherAuditEvent(anyString(), anyLong(), any());
        // 同时 updateById 也不应被调用
        verify(teacherProfileMapper, never()).updateById(any(TeacherProfileDO.class));
    }
}
