package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherProfileUpdateReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherQualificationMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.ErrorCodeConstants;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import com.mandarly.boot.module.edu.enums.teacher.TeacherQualificationDocTypeEnum;
import com.mandarly.boot.module.edu.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TeacherProfileServiceImpl#updateOwnProfile + #submitForAudit TDD 单元测试(D19 B3)
 *
 * <p>覆盖:
 * <ol>
 *   <li>updateOwnProfile:approved 状态下改 intro,字段更新但 audit_status 不变</li>
 *   <li>updateOwnProfile:partial update —— null 字段保留旧值</li>
 *   <li>updateOwnProfile:profile 不存在 → 抛 ServiceException(404)</li>
 *   <li>submitForAudit:draft → pending</li>
 *   <li>submitForAudit:rejected → pending,清空 rejectReason / auditedAt / auditedBy</li>
 *   <li>submitForAudit:pending → 抛 TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT</li>
 *   <li>submitForAudit:approved → 抛 TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT</li>
 *   <li>submitForAudit:profile 不存在 → 抛 ServiceException(404)</li>
 * </ol>
 */
class TeacherProfileServiceImplSubmitAuditTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherProfileServiceImpl teacherProfileService;

    @Mock
    private TeacherProfileMapper teacherProfileMapper;

    @Mock
    private TeacherQualificationMapper teacherQualificationMapper;

    @Mock
    private EduUserMapper eduUserMapper;

    @Mock
    private NotificationService notificationService;

    // ========== updateOwnProfile ==========

    /** case 1: approved 状态下改 intro,intro 更新,audit_status 仍为 approved */
    @Test
    void updateOwnProfile_setsFieldsButPreservesAuditStatus() {
        Long userId = 77001L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setIntro("旧介绍");
        existing.setAuditStatus(TeacherAuditStatusEnum.APPROVED.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);

        AppTeacherProfileUpdateReqVO req = new AppTeacherProfileUpdateReqVO();
        req.setIntro("新介绍");

        teacherProfileService.updateOwnProfile(userId, req);

        ArgumentCaptor<TeacherProfileDO> cap = ArgumentCaptor.forClass(TeacherProfileDO.class);
        verify(teacherProfileMapper).updateById(cap.capture());
        TeacherProfileDO updated = cap.getValue();
        assertThat(updated.getIntro()).isEqualTo("新介绍");
        assertThat(updated.getAuditStatus()).isEqualTo(TeacherAuditStatusEnum.APPROVED.getCode());
    }

    /** case 2: partial update — null 字段不动,只更新 expertise */
    @Test
    void updateOwnProfile_partialUpdate_nullFieldsUntouched() {
        Long userId = 77002L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setIntro("保留的介绍");
        existing.setAccent("hk");
        existing.setYearsExperience(3);
        existing.setAuditStatus(TeacherAuditStatusEnum.APPROVED.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);

        AppTeacherProfileUpdateReqVO req = new AppTeacherProfileUpdateReqVO();
        // intro / accent / yearsExperience 都 null
        List<String> expertise = Arrays.asList("business", "kids");
        req.setExpertise(expertise);

        teacherProfileService.updateOwnProfile(userId, req);

        ArgumentCaptor<TeacherProfileDO> cap = ArgumentCaptor.forClass(TeacherProfileDO.class);
        verify(teacherProfileMapper).updateById(cap.capture());
        TeacherProfileDO updated = cap.getValue();
        assertThat(updated.getIntro()).isEqualTo("保留的介绍");
        assertThat(updated.getAccent()).isEqualTo("hk");
        assertThat(updated.getYearsExperience()).isEqualTo(3);
        assertThat(updated.getExpertise()).containsExactly("business", "kids");
        assertThat(updated.getAuditStatus()).isEqualTo(TeacherAuditStatusEnum.APPROVED.getCode());
    }

    /** case 3: profile 不存在 → 抛异常 */
    @Test
    void updateOwnProfile_profileNotFound_throws() {
        Long userId = 77003L;
        when(teacherProfileMapper.selectById(userId)).thenReturn(null);

        AppTeacherProfileUpdateReqVO req = new AppTeacherProfileUpdateReqVO();
        req.setIntro("x");

        assertThatThrownBy(() -> teacherProfileService.updateOwnProfile(userId, req))
                .isInstanceOf(ServiceException.class);
        verify(teacherProfileMapper, never()).updateById(any(TeacherProfileDO.class));
    }

    // ========== submitForAudit ==========

    /** case 4: draft → pending */
    @Test
    void submitForAudit_draftToPending() {
        Long userId = 77100L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setAuditStatus(TeacherAuditStatusEnum.DRAFT.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);
        when(teacherQualificationMapper.selectListByUserId(userId)).thenReturn(requiredQualifications());

        teacherProfileService.submitForAudit(userId);

        ArgumentCaptor<TeacherProfileDO> cap = ArgumentCaptor.forClass(TeacherProfileDO.class);
        verify(teacherProfileMapper).updateById(cap.capture());
        TeacherProfileDO updated = cap.getValue();
        assertThat(updated.getAuditStatus()).isEqualTo(TeacherAuditStatusEnum.PENDING.getCode());
        assertThat(updated.getRejectReason()).isNull();
        assertThat(updated.getAuditedAt()).isNull();
        assertThat(updated.getAuditedBy()).isNull();
    }

    /** case 5: rejected → pending,清空 rejectReason / auditedAt / auditedBy */
    @Test
    void submitForAudit_rejectedToPending_clearsRejectReason() {
        Long userId = 77101L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setAuditStatus(TeacherAuditStatusEnum.REJECTED.getCode());
        existing.setRejectReason("证件模糊");
        existing.setAuditedAt(LocalDateTime.now().minusDays(1));
        existing.setAuditedBy(1L);
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);
        when(teacherQualificationMapper.selectListByUserId(userId)).thenReturn(requiredQualifications());

        teacherProfileService.submitForAudit(userId);

        ArgumentCaptor<TeacherProfileDO> cap = ArgumentCaptor.forClass(TeacherProfileDO.class);
        verify(teacherProfileMapper).updateById(cap.capture());
        TeacherProfileDO updated = cap.getValue();
        assertThat(updated.getAuditStatus()).isEqualTo(TeacherAuditStatusEnum.PENDING.getCode());
        assertThat(updated.getRejectReason()).isNull();
        assertThat(updated.getAuditedAt()).isNull();
        assertThat(updated.getAuditedBy()).isNull();
    }

    /** case 6: pending → 抛 TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT */
    @Test
    void submitForAudit_pending_throws() {
        Long userId = 77102L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setAuditStatus(TeacherAuditStatusEnum.PENDING.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);

        assertThatThrownBy(() -> teacherProfileService.submitForAudit(userId))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code",
                        ErrorCodeConstants.TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT.getCode());
        verify(teacherProfileMapper, never()).updateById(any(TeacherProfileDO.class));
    }

    /** case 7: draft 缺必填资质 → 抛 TEACHER_REQUIRED_QUALIFICATION_MISSING */
    @Test
    void submitForAudit_draftMissingRequiredQualifications_throws() {
        Long userId = 77105L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setAuditStatus(TeacherAuditStatusEnum.DRAFT.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);
        when(teacherQualificationMapper.selectListByUserId(userId)).thenReturn(List.of(qualification("id_card")));

        assertThatThrownBy(() -> teacherProfileService.submitForAudit(userId))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code",
                        ErrorCodeConstants.TEACHER_REQUIRED_QUALIFICATION_MISSING.getCode());
        verify(teacherProfileMapper, never()).updateById(any(TeacherProfileDO.class));
    }

    /** case 8: approved → 抛 TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT */
    @Test
    void submitForAudit_approved_throws() {
        Long userId = 77103L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setAuditStatus(TeacherAuditStatusEnum.APPROVED.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);

        assertThatThrownBy(() -> teacherProfileService.submitForAudit(userId))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code",
                        ErrorCodeConstants.TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT.getCode());
        verify(teacherProfileMapper, never()).updateById(any(TeacherProfileDO.class));
    }

    /** case 9: profile 不存在 → 抛 ServiceException */
    @Test
    void submitForAudit_profileNotFound_throws() {
        Long userId = 77104L;
        when(teacherProfileMapper.selectById(userId)).thenReturn(null);

        assertThatThrownBy(() -> teacherProfileService.submitForAudit(userId))
                .isInstanceOf(ServiceException.class);
        verify(teacherProfileMapper, never()).updateById(any(TeacherProfileDO.class));
    }

    private static List<TeacherQualificationDO> requiredQualifications() {
        return List.of(
                qualification(TeacherQualificationDocTypeEnum.ID_CARD.getCode()),
                qualification(TeacherQualificationDocTypeEnum.DEGREE_CERT.getCode()));
    }

    private static TeacherQualificationDO qualification(String docType) {
        TeacherQualificationDO row = new TeacherQualificationDO();
        row.setDocType(docType);
        return row;
    }
}
