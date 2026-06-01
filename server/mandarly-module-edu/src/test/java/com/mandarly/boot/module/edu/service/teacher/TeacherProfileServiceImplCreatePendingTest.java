package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TeacherProfileServiceImpl#createPendingProfile TDD 单元测试(D19 A1)
 *
 * <p>覆盖:
 * <ol>
 *   <li>新用户:首次调用插入一条 audit_status=draft 的档案</li>
 *   <li>幂等:已存在档案时第二次调用不重复 insert</li>
 * </ol>
 */
class TeacherProfileServiceImplCreatePendingTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherProfileServiceImpl teacherProfileService;

    @Mock
    private TeacherProfileMapper teacherProfileMapper;

    @Mock
    private EduUserMapper eduUserMapper;

    /** case 1: 新教师 — 落库一条 draft 档案 */
    @Test
    void createPendingProfile_persistsRowWithAuditStatusDraft() {
        Long userId = 99001L;
        when(teacherProfileMapper.selectById(userId)).thenReturn(null);

        teacherProfileService.createPendingProfile(userId);

        ArgumentCaptor<TeacherProfileDO> cap = ArgumentCaptor.forClass(TeacherProfileDO.class);
        verify(teacherProfileMapper).insert(cap.capture());
        TeacherProfileDO inserted = cap.getValue();
        assertThat(inserted.getUserId()).isEqualTo(userId);
        assertThat(inserted.getAuditStatus()).isEqualTo(TeacherAuditStatusEnum.DRAFT.getCode());
        // prod 2026-05-18 实战:注册场景没 login user,MP auto-fill 拿不到 creator,
        // 必须显式填(本回归测试覆盖 fix)
        assertThat(inserted.getCreator()).isEqualTo(String.valueOf(userId));
        assertThat(inserted.getUpdater()).isEqualTo(String.valueOf(userId));
    }

    /** case 2: 已存在 — 幂等,第二次不再 insert */
    @Test
    void createPendingProfile_idempotentOnSecondCall() {
        Long userId = 99002L;
        TeacherProfileDO existing = new TeacherProfileDO();
        existing.setUserId(userId);
        existing.setAuditStatus(TeacherAuditStatusEnum.DRAFT.getCode());
        when(teacherProfileMapper.selectById(userId)).thenReturn(existing);

        teacherProfileService.createPendingProfile(userId);

        verify(teacherProfileMapper, never()).insert(org.mockito.ArgumentMatchers.any(TeacherProfileDO.class));
    }
}
