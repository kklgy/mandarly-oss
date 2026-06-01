package com.mandarly.boot.module.edu.controller.app.levelcheck;

import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckResultRespVO;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckSubmitReqVO;
import com.mandarly.boot.module.edu.service.levelcheck.LevelCheckService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppLevelCheckControllerTest {

    @Mock
    private LevelCheckService levelCheckService;

    @InjectMocks
    private AppLevelCheckController controller;

    private MockedStatic<SecurityFrameworkUtils> securityStatic;

    @AfterEach
    void teardown() {
        if (securityStatic != null) {
            securityStatic.close();
        }
    }

    @Test
    void submit_masksTeacherAndPackageRecommendationsForAnonymousVisitors() {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(null);
        AppLevelCheckSubmitReqVO reqVO = new AppLevelCheckSubmitReqVO();
        AppLevelCheckResultRespVO result = resultWithRecommendations();
        when(levelCheckService.submitAnswers(reqVO, null)).thenReturn(result);

        AppLevelCheckResultRespVO data = controller.submit(reqVO).getData();

        assertEquals(List.of(), data.getRecommendedTeachers());
        assertNull(data.getRecommendedPackage());
    }

    @Test
    void submit_preservesRecommendationsForLoggedInVisitors() {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(1001L);
        AppLevelCheckSubmitReqVO reqVO = new AppLevelCheckSubmitReqVO();
        AppLevelCheckResultRespVO result = resultWithRecommendations();
        when(levelCheckService.submitAnswers(reqVO, 1001L)).thenReturn(result);

        AppLevelCheckResultRespVO data = controller.submit(reqVO).getData();

        assertEquals(1, data.getRecommendedTeachers().size());
        assertEquals(1L, data.getRecommendedPackage().getId());
    }

    @Test
    void getResult_masksTeacherAndPackageRecommendationsForAnonymousVisitors() {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(null);
        when(levelCheckService.getResult(13L)).thenReturn(resultWithRecommendations());

        AppLevelCheckResultRespVO data = controller.getResult(13L).getData();

        assertEquals(List.of(), data.getRecommendedTeachers());
        assertNull(data.getRecommendedPackage());
    }

    private AppLevelCheckResultRespVO resultWithRecommendations() {
        AppLevelCheckResultRespVO result = new AppLevelCheckResultRespVO();
        result.setSubmissionId(13L);
        result.setInferredLevel("intermediate");

        AppLevelCheckResultRespVO.RecommendedTeacher teacher = new AppLevelCheckResultRespVO.RecommendedTeacher();
        teacher.setUserId(10001L);
        teacher.setNickname("Tutor");
        result.setRecommendedTeachers(List.of(teacher));

        AppLevelCheckResultRespVO.RecommendedPackage pkg = new AppLevelCheckResultRespVO.RecommendedPackage();
        pkg.setId(1L);
        result.setRecommendedPackage(pkg);
        return result;
    }
}
