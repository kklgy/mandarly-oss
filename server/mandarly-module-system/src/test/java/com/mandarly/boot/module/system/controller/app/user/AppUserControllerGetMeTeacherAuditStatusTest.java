package com.mandarly.boot.module.system.controller.app.user;

import com.mandarly.boot.module.system.controller.app.user.vo.AppUserProfileRespVO;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * D19-A7:AppUserController.enrichTeacherAuditStatus 反射读教师档案单元测试。
 *
 * <p>验证四点:
 * <ul>
 *   <li>role=teacher 且反射目标存在 → vo.teacherAuditStatus / teacherRejectReason 被填充。</li>
 *   <li>role=student / null → 不查 ApplicationContext bean,vo 两字段保持 null。</li>
 *   <li>teacherProfileServiceImpl bean 缺失 → graceful degrade,不抛异常,vo 两字段保持 null。</li>
 *   <li>反射目标 getTeacherProfile 返回 null(还没建过 profile)→ vo 两字段保持 null。</li>
 * </ul>
 *
 * <p>system 模块不依赖 edu,故无法 import TeacherProfileDO;FakeBean 暴露反射友好的同签方法。
 */
@ExtendWith(MockitoExtension.class)
class AppUserControllerGetMeTeacherAuditStatusTest {

    @Mock private ApplicationContext applicationContext;

    @InjectMocks private AppUserController controller;

    /** 反射目标 stub:与生产 TeacherProfileServiceImpl 同 bean name 同方法签名。 */
    static class FakeTeacherProfileServiceBean {
        private final Object profileToReturn;

        FakeTeacherProfileServiceBean(Object profile) {
            this.profileToReturn = profile;
        }

        @SuppressWarnings("unused")
        public Object getTeacherProfile(Long userId) {
            return profileToReturn;
        }
    }

    /** 反射目标返回值 stub:暴露 getAuditStatus / getRejectReason getter。 */
    static class FakeProfile {
        private final String auditStatus;
        private final String rejectReason;

        FakeProfile(String auditStatus, String rejectReason) {
            this.auditStatus = auditStatus;
            this.rejectReason = rejectReason;
        }

        @SuppressWarnings("unused")
        public String getAuditStatus() {
            return auditStatus;
        }

        @SuppressWarnings("unused")
        public String getRejectReason() {
            return rejectReason;
        }
    }

    @Test
    void teacherRole_draftProfile_populatesAuditStatus() {
        FakeProfile profile = new FakeProfile("draft", null);
        when(applicationContext.getBean("teacherProfileServiceImpl"))
                .thenReturn(new FakeTeacherProfileServiceBean(profile));

        AppUserDO user = new AppUserDO();
        user.setId(100L);
        user.setRole("teacher");
        AppUserProfileRespVO vo = new AppUserProfileRespVO();

        controller.enrichTeacherAuditStatus(vo, user);

        assertThat(vo.getTeacherAuditStatus()).isEqualTo("draft");
        assertThat(vo.getTeacherRejectReason()).isNull();
    }

    @Test
    void teacherRole_rejectedProfile_populatesBothFields() {
        FakeProfile profile = new FakeProfile("rejected", "资质照片不清晰");
        when(applicationContext.getBean("teacherProfileServiceImpl"))
                .thenReturn(new FakeTeacherProfileServiceBean(profile));

        AppUserDO user = new AppUserDO();
        user.setId(101L);
        user.setRole("teacher");
        AppUserProfileRespVO vo = new AppUserProfileRespVO();

        controller.enrichTeacherAuditStatus(vo, user);

        assertThat(vo.getTeacherAuditStatus()).isEqualTo("rejected");
        assertThat(vo.getTeacherRejectReason()).isEqualTo("资质照片不清晰");
    }

    @Test
    void studentRole_doesNotInvokeReflection_voUntouched() {
        AppUserDO user = new AppUserDO();
        user.setId(200L);
        user.setRole("student");
        AppUserProfileRespVO vo = new AppUserProfileRespVO();

        controller.enrichTeacherAuditStatus(vo, user);

        assertThat(vo.getTeacherAuditStatus()).isNull();
        assertThat(vo.getTeacherRejectReason()).isNull();
        verifyNoInteractions(applicationContext);
    }

    @Test
    void nullRole_doesNotInvokeReflection_voUntouched() {
        AppUserDO user = new AppUserDO();
        user.setId(201L);
        user.setRole(null);
        AppUserProfileRespVO vo = new AppUserProfileRespVO();

        controller.enrichTeacherAuditStatus(vo, user);

        assertThat(vo.getTeacherAuditStatus()).isNull();
        verifyNoInteractions(applicationContext);
    }

    @Test
    void nullUser_doesNotThrow_voUntouched() {
        AppUserProfileRespVO vo = new AppUserProfileRespVO();

        assertThatCode(() -> controller.enrichTeacherAuditStatus(vo, null))
                .doesNotThrowAnyException();
        assertThat(vo.getTeacherAuditStatus()).isNull();
        verifyNoInteractions(applicationContext);
    }

    @Test
    void teacherRole_butBeanMissing_doesNotThrow_voUntouched() {
        // edu 模块未装载时 graceful degrade
        when(applicationContext.getBean("teacherProfileServiceImpl"))
                .thenThrow(new NoSuchBeanDefinitionException("teacherProfileServiceImpl"));

        AppUserDO user = new AppUserDO();
        user.setId(300L);
        user.setRole("teacher");
        AppUserProfileRespVO vo = new AppUserProfileRespVO();

        assertThatCode(() -> controller.enrichTeacherAuditStatus(vo, user))
                .doesNotThrowAnyException();
        assertThat(vo.getTeacherAuditStatus()).isNull();
        assertThat(vo.getTeacherRejectReason()).isNull();
    }

    @Test
    void teacherRole_profileNotYetCreated_voUntouched() {
        // TeacherProfile 还没建(理论上 A2 反射注册会建,但兜底防御)→ 反射返回 null,VO 保持 null
        when(applicationContext.getBean("teacherProfileServiceImpl"))
                .thenReturn(new FakeTeacherProfileServiceBean(null));

        AppUserDO user = new AppUserDO();
        user.setId(400L);
        user.setRole("teacher");
        AppUserProfileRespVO vo = new AppUserProfileRespVO();

        controller.enrichTeacherAuditStatus(vo, user);

        assertThat(vo.getTeacherAuditStatus()).isNull();
        assertThat(vo.getTeacherRejectReason()).isNull();
    }

    @Test
    void teacherRole_reflectionInternalError_doesNotPropagate() {
        // 反射目标内部抛 RuntimeException(比如 DB 读失败)时,InvocationTargetException 被 catch(Exception) 兜住
        Object brokenBean = new Object() {
            @SuppressWarnings("unused")
            public Object getTeacherProfile(Long userId) {
                throw new IllegalStateException("simulated DB read failure");
            }
        };
        when(applicationContext.getBean("teacherProfileServiceImpl")).thenReturn(brokenBean);

        AppUserDO user = new AppUserDO();
        user.setId(500L);
        user.setRole("teacher");
        AppUserProfileRespVO vo = new AppUserProfileRespVO();

        assertThatCode(() -> controller.enrichTeacherAuditStatus(vo, user))
                .doesNotThrowAnyException();
        assertThat(vo.getTeacherAuditStatus()).isNull();
    }
}
