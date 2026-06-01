package com.mandarly.boot.module.edu.controller.app.teacher;

import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.teacher.vo.AppTeacherSimpleRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.teacher.TeacherProfileService;
import com.mandarly.boot.module.system.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppTeacherControllerTest {

    @Mock
    private TeacherProfileService teacherProfileService;

    @Mock
    private EduUserMapper eduUserMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private AppTeacherController controller;

    private MockedStatic<SecurityFrameworkUtils> securityStatic;

    @AfterEach
    void teardown() {
        if (securityStatic != null) {
            securityStatic.close();
        }
    }

    @Test
    void listVisibleTeachers_allowsAnonymousVisitors() {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(null);
        when(teacherProfileService.listVisibleTeachers(any())).thenReturn(List.of());

        List<AppTeacherSimpleRespVO> list = controller.listVisibleTeachers(null).getData();

        assertTrue(list.isEmpty());
        verify(teacherProfileService).listVisibleTeachers(any());
    }

    @Test
    void getTeacher_allowsAnonymousVisitors() {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(null);
        when(teacherProfileService.getTeacherProfile(1001L)).thenReturn(null);

        AppTeacherSimpleRespVO vo = controller.getTeacher(1001L).getData();

        assertNull(vo);
        verify(teacherProfileService).getTeacherProfile(1001L);
    }

    @Test
    void listVisibleTeachers_returnsProfileFieldsForLoggedInVisitors() {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(2001L);
        TeacherProfileDO profile = profileRow();
        when(teacherProfileService.listVisibleTeachers(any())).thenReturn(List.of(profile));
        when(teacherProfileService.presignIntroVideoUrl("cos://intro-video.mp4"))
                .thenReturn("https://signed.example/intro-video.mp4");
        when(eduUserMapper.selectBatchIds(any())).thenReturn(List.of(userRow()));
        when(userService.presignAvatarUrl("https://cdn.example/avatar.jpg"))
                .thenReturn("https://cdn.example/avatar.jpg");

        AppTeacherSimpleRespVO vo = controller.listVisibleTeachers(null).getData().get(0);

        assertEquals(1001L, vo.getUserId());
        assertEquals("Teacher Lin", vo.getNickname());
        assertEquals("https://cdn.example/avatar.jpg", vo.getAvatar());
        assertEquals("Asia/Hong_Kong", vo.getTeacherTimezone());
        assertEquals("I teach spoken Mandarin.", vo.getIntro());
        assertEquals("https://signed.example/intro-video.mp4", vo.getIntroVideoUrl());
        assertTrue(vo.getHasIntroVideo());
        assertTrue(vo.getProfileVisible());
    }

    private TeacherProfileDO profileRow() {
        TeacherProfileDO profile = new TeacherProfileDO();
        profile.setUserId(1001L);
        profile.setIntro("I teach spoken Mandarin.");
        profile.setIntroVideoUrl("cos://intro-video.mp4");
        profile.setExpertise(List.of("speaking"));
        profile.setAccent("mandarin_cn");
        return profile;
    }

    private UserDO userRow() {
        UserDO user = new UserDO();
        user.setId(1001L);
        user.setNickname("Teacher Lin");
        user.setAvatarUrl("https://cdn.example/avatar.jpg");
        user.setTimezone("Asia/Hong_Kong");
        return user;
    }
}
