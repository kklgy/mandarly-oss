package com.mandarly.boot.module.edu.service.levelcheck;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckResultRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckSubmissionDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.levelcheck.LevelCheckSubmissionMapper;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.teacher.TeacherProfileService;
import com.mandarly.boot.module.system.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LevelCheckServiceImplTest {

    @Mock
    private LevelCheckSubmissionMapper submissionMapper;
    @Mock
    private TeacherProfileMapper teacherProfileMapper;
    @Mock
    private EduUserMapper eduUserMapper;
    @Mock
    private PackageMapper packageMapper;
    @Mock
    private TeacherProfileService teacherProfileService;
    @Mock
    private UserService userService;

    @InjectMocks
    private LevelCheckServiceImpl service;

    @Test
    void getResult_presignsRecommendedTeacherAvatar() {
        LevelCheckSubmissionDO submission = new LevelCheckSubmissionDO();
        submission.setId(16L);
        submission.setInferredLevel("beginner");
        submission.setRecommendedTeacherIds(List.of(1001L));
        when(submissionMapper.selectById(16L)).thenReturn(submission);

        TeacherProfileDO profile = new TeacherProfileDO();
        profile.setUserId(1001L);
        profile.setIntro("Warm coaching");
        when(teacherProfileMapper.selectList(any(Wrapper.class))).thenReturn(List.of(profile));

        UserDO user = new UserDO();
        user.setId(1001L);
        user.setNickname("林子轩");
        user.setAvatarUrl("avatar/1001.jpg");
        when(eduUserMapper.selectBatchIds(List.of(1001L))).thenReturn(List.of(user));
        when(userService.presignAvatarUrl("avatar/1001.jpg")).thenReturn("https://cos.example/avatar/1001.jpg?sign=1");

        AppLevelCheckResultRespVO result = service.getResult(16L);

        assertEquals("林子轩", result.getRecommendedTeachers().get(0).getNickname());
        assertEquals("https://cos.example/avatar/1001.jpg?sign=1",
                result.getRecommendedTeachers().get(0).getAvatar());
    }
}
