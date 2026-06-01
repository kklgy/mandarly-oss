package com.mandarly.boot.module.edu.service.schedule;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.ScheduleExceptionDO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.mysql.schedule.ScheduleExceptionMapper;
import com.mandarly.boot.module.edu.dal.mysql.schedule.TeacherScheduleMapper;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.Collections;

import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.TEACHER_NOT_APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ScheduleServiceImpl#toggleWeeklySlot + #toggleException audit_status 闭合 backdoor 单元测试(D19 A4)
 *
 * <p>覆盖:pending/rejected 教师不能配排课;approved 教师正常通过审核闸口。
 */
class ScheduleServiceAuditGateTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Mock
    private TeacherScheduleMapper teacherScheduleMapper;

    @Mock
    private ScheduleExceptionMapper scheduleExceptionMapper;

    @Mock
    private TeacherProfileMapper teacherProfileMapper;

    private static final Long TEACHER_ID = 2002L;
    private static final String TZ = "Asia/Hong_Kong";

    private TeacherProfileDO profile(String auditStatus) {
        TeacherProfileDO p = new TeacherProfileDO();
        p.setUserId(TEACHER_ID);
        p.setAuditStatus(auditStatus);
        return p;
    }

    // ========== toggleWeeklySlot ==========

    /** case 1: teacher_profile 缺失 → toggleWeeklySlot 抛 TEACHER_NOT_APPROVED */
    @Test
    void toggleWeeklySlot_teacherProfileMissing_throwsNotApproved() {
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(null);

        assertThatThrownBy(() ->
                scheduleService.toggleWeeklySlot(TEACHER_ID, 1, 10, 0, true, TZ))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        verify(teacherScheduleMapper, never()).insert(org.mockito.ArgumentMatchers.any(TeacherScheduleDO.class));
        verify(teacherScheduleMapper, never()).deleteById(org.mockito.ArgumentMatchers.anyLong());
    }

    /** case 2: pending → toggleWeeklySlot 抛 TEACHER_NOT_APPROVED */
    @Test
    void toggleWeeklySlot_teacherProfilePending_throwsNotApproved() {
        when(teacherProfileMapper.selectById(TEACHER_ID))
                .thenReturn(profile(TeacherAuditStatusEnum.PENDING.getCode()));

        assertThatThrownBy(() ->
                scheduleService.toggleWeeklySlot(TEACHER_ID, 1, 10, 0, true, TZ))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        verify(teacherScheduleMapper, never()).insert(org.mockito.ArgumentMatchers.any(TeacherScheduleDO.class));
    }

    /** case 3: rejected → toggleWeeklySlot 抛 TEACHER_NOT_APPROVED */
    @Test
    void toggleWeeklySlot_teacherProfileRejected_throwsNotApproved() {
        when(teacherProfileMapper.selectById(TEACHER_ID))
                .thenReturn(profile(TeacherAuditStatusEnum.REJECTED.getCode()));

        assertThatThrownBy(() ->
                scheduleService.toggleWeeklySlot(TEACHER_ID, 1, 10, 0, true, TZ))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        verify(teacherScheduleMapper, never()).insert(org.mockito.ArgumentMatchers.any(TeacherScheduleDO.class));
    }

    /** case 4: approved → toggleWeeklySlot 顺利落库,不抛审核错 */
    @Test
    void toggleWeeklySlot_teacherProfileApproved_passesAuditAndInserts() {
        when(teacherProfileMapper.selectById(TEACHER_ID))
                .thenReturn(profile(TeacherAuditStatusEnum.APPROVED.getCode()));
        // 无现存排课 → 新增分支
        when(teacherScheduleMapper.selectListByTeacherId(TEACHER_ID)).thenReturn(Collections.emptyList());

        scheduleService.toggleWeeklySlot(TEACHER_ID, 1, 10, 0, true, TZ);

        verify(teacherScheduleMapper).insert(org.mockito.ArgumentMatchers.any(TeacherScheduleDO.class));
    }

    // ========== toggleException ==========

    /** case 5: teacher_profile 缺失 → toggleException 抛 TEACHER_NOT_APPROVED */
    @Test
    void toggleException_teacherProfileMissing_throwsNotApproved() {
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(null);

        assertThatThrownBy(() ->
                scheduleService.toggleException(TEACHER_ID, LocalDate.now(), 10, 0,
                        "closed", TZ, null))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        verify(scheduleExceptionMapper, never()).insert(org.mockito.ArgumentMatchers.any(ScheduleExceptionDO.class));
    }

    /** case 6: pending → toggleException 抛 TEACHER_NOT_APPROVED */
    @Test
    void toggleException_teacherProfilePending_throwsNotApproved() {
        when(teacherProfileMapper.selectById(TEACHER_ID))
                .thenReturn(profile(TeacherAuditStatusEnum.PENDING.getCode()));

        assertThatThrownBy(() ->
                scheduleService.toggleException(TEACHER_ID, LocalDate.now(), 10, 0,
                        "closed", TZ, null))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        verify(scheduleExceptionMapper, never()).insert(org.mockito.ArgumentMatchers.any(ScheduleExceptionDO.class));
    }

    /** case 7: rejected → toggleException 抛 TEACHER_NOT_APPROVED */
    @Test
    void toggleException_teacherProfileRejected_throwsNotApproved() {
        when(teacherProfileMapper.selectById(TEACHER_ID))
                .thenReturn(profile(TeacherAuditStatusEnum.REJECTED.getCode()));

        assertThatThrownBy(() ->
                scheduleService.toggleException(TEACHER_ID, LocalDate.now(), 10, 0,
                        "closed", TZ, null))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        verify(scheduleExceptionMapper, never()).insert(org.mockito.ArgumentMatchers.any(ScheduleExceptionDO.class));
    }

    /** case 8: approved → toggleException 顺利落库,不抛审核错 */
    @Test
    void toggleException_teacherProfileApproved_passesAuditAndInserts() {
        when(teacherProfileMapper.selectById(TEACHER_ID))
                .thenReturn(profile(TeacherAuditStatusEnum.APPROVED.getCode()));
        LocalDate date = LocalDate.now().plusDays(1);
        when(scheduleExceptionMapper.selectListByTeacherIdAndDateRange(TEACHER_ID, date, date))
                .thenReturn(Collections.emptyList());

        scheduleService.toggleException(TEACHER_ID, date, 10, 0, "closed", TZ, null);

        verify(scheduleExceptionMapper).insert(org.mockito.ArgumentMatchers.any(ScheduleExceptionDO.class));
    }

    /** 边界:approved 但 toggleException 收到非法 action,应抛 400,不应抛 TEACHER_NOT_APPROVED */
    @Test
    void toggleException_teacherProfileApproved_invalidActionStillThrowsButNotAuditError() {
        when(teacherProfileMapper.selectById(TEACHER_ID))
                .thenReturn(profile(TeacherAuditStatusEnum.APPROVED.getCode()));
        LocalDate date = LocalDate.now().plusDays(1);
        when(scheduleExceptionMapper.selectListByTeacherIdAndDateRange(TEACHER_ID, date, date))
                .thenReturn(Collections.emptyList());

        try {
            scheduleService.toggleException(TEACHER_ID, date, 10, 0, "bogus", TZ, null);
        } catch (ServiceException e) {
            assertThat(e.getCode()).isNotEqualTo(TEACHER_NOT_APPROVED.getCode());
        }
    }
}
