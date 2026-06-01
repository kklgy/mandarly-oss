package com.mandarly.boot.module.system.service.auth;

import com.mandarly.boot.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.mandarly.boot.module.system.service.oauth2.OAuth2TokenService;
import com.mandarly.boot.module.system.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * D19-A2:注册路径 role=teacher 时反射调 TeacherProfileServiceImpl.createPendingProfile(Long) 单元测试。
 *
 * <p>验证三点:
 * <ul>
 *   <li>role=teacher → ApplicationContext.getBean("teacherProfileServiceImpl") 被命中,
 *       且 createPendingProfile(userId) 反射调用恰好一次。</li>
 *   <li>role=student / 其它 → 不查 ApplicationContext bean,不触发 createPendingProfile。</li>
 *   <li>ApplicationContext.getBean 抛 BeansException 时 graceful degrade,
 *       不向上抛(注册不被阻塞)。</li>
 * </ul>
 *
 * <p>不走 SpringBootTest 避免引入 DB 启动开销;直接 mock ApplicationContext +
 * 反射目标用真 Object(暴露 public createPendingProfile(Long)),拿到真实信号。
 */
@ExtendWith(MockitoExtension.class)
class AppAuthServiceRoleTest {

    @Mock private ApplicationContext applicationContext;
    @Mock private SocialClient socialClient;
    @Mock private UserService userService;
    @Mock private OAuth2TokenService oauth2TokenService;

    @InjectMocks private AppAuthServiceImpl service;

    /** 反射目标 stub:暴露 createPendingProfile(Long) 与生产 bean 同名同签。 */
    static class FakeTeacherProfileServiceBean {
        final AtomicInteger callCount = new AtomicInteger();
        final AtomicReference<Long> lastUserId = new AtomicReference<>();

        public void createPendingProfile(Long userId) {
            callCount.incrementAndGet();
            lastUserId.set(userId);
        }
    }

    @Test
    void teacherRole_invokesCreatePendingProfile_once() {
        FakeTeacherProfileServiceBean fake = new FakeTeacherProfileServiceBean();
        when(applicationContext.getBean("teacherProfileServiceImpl")).thenReturn(fake);

        service.createPendingTeacherProfileIfTeacher(42L, "teacher");

        assertThat(fake.callCount.get()).isEqualTo(1);
        assertThat(fake.lastUserId.get()).isEqualTo(42L);
    }

    @Test
    void studentRole_doesNotInvokeCreatePendingProfile() {
        // 不 stub applicationContext.getBean — 因为应当根本不调用它(role!=teacher 时早返回)
        service.createPendingTeacherProfileIfTeacher(43L, "student");

        // 没抛异常即可;并且 applicationContext.getBean 未被调用(MockitoExtension verifyNoMoreInteractions
        // 默认不强制,但 lenient 关闭场景下未 stub 的 mock 调用会触发 UnnecessaryStubbingException;
        // 这里直接通过 "不抛 + 后续无副作用" 完成基础断言)
        // 显式校验 applicationContext 未被触达更稳:
        org.mockito.Mockito.verifyNoInteractions(applicationContext);
    }

    @Test
    void nullRole_doesNotInvokeCreatePendingProfile() {
        // null 安全:跟 "student" 一样,不触发反射
        service.createPendingTeacherProfileIfTeacher(44L, null);
        org.mockito.Mockito.verifyNoInteractions(applicationContext);
    }

    @Test
    void teacherRole_butBeanMissing_doesNotThrow() {
        // edu 模块未装载或 bean 异常时,优雅降级 — 不阻塞注册流程
        when(applicationContext.getBean("teacherProfileServiceImpl"))
                .thenThrow(new NoSuchBeanDefinitionException("teacherProfileServiceImpl"));

        assertThatCode(() -> service.createPendingTeacherProfileIfTeacher(45L, "teacher"))
                .doesNotThrowAnyException();
    }

    @Test
    void teacherRole_butInvokeThrowsRuntime_doesNotPropagate() {
        // 反射目标内部出错时(比如 DB 写入抛 RuntimeException),InvocationTargetException
        // 应当被 catch (Exception) 兜住,不向上抛
        Object brokenBean = new Object() {
            @SuppressWarnings("unused")
            public void createPendingProfile(Long userId) {
                throw new IllegalStateException("simulated DB write failure");
            }
        };
        when(applicationContext.getBean("teacherProfileServiceImpl")).thenReturn(brokenBean);

        assertThatCode(() -> service.createPendingTeacherProfileIfTeacher(46L, "teacher"))
                .doesNotThrowAnyException();
    }

    // ============ D19-A3: loginBySocial(provider, code, state, ip, role) 透传 role 验证 ============

    /**
     * role=teacher 走 OAuth 登录注册 → reflection 命中 teacherProfileServiceImpl.createPendingProfile。
     * 同时验证 createOrLoginBySocial 接收到的 role 入参 == "teacher"。
     */
    @Test
    void loginBySocial_teacherRole_pipesRoleAndInvokesCreatePendingProfile() {
        // 1. socialClient 返回任意 SocialUserInfo(走 new-user 路径,email 不存在)
        SocialUserInfo info = SocialUserInfo.builder()
                .provider("google").oauthUid("uid-100").oauthEmail("new-teacher@example.com")
                .nickname("Tee").avatarUrl(null).rawAttributes(null)
                .build();
        when(socialClient.getUserInfo("google", "code-x", "state-y")).thenReturn(info);

        // 2. userService.createOrLoginBySocial 返回 newUserId=100;断言传入的 role == "teacher"
        when(userService.createOrLoginBySocial(
                eq("google"), eq("uid-100"), eq("new-teacher@example.com"),
                any(), eq("teacher"),
                any(), any(), eq("en"), eq("UTC")))
                .thenReturn(100L);

        // 3. oauth2TokenService.createAccessToken 返回 stub token
        OAuth2AccessTokenDO stubToken = new OAuth2AccessTokenDO();
        stubToken.setUserId(100L);
        when(oauth2TokenService.createAccessToken(eq(100L), anyInt(), anyString(), anyList()))
                .thenReturn(stubToken);

        // 4. teacherProfileServiceImpl bean 反射应被命中
        FakeTeacherProfileServiceBean fake = new FakeTeacherProfileServiceBean();
        when(applicationContext.getBean("teacherProfileServiceImpl")).thenReturn(fake);

        OAuth2AccessTokenDO token = service.loginBySocial("google", "code-x", "state-y", "1.2.3.4", "teacher");

        assertThat(token).isSameAs(stubToken);
        assertThat(fake.callCount.get()).isEqualTo(1);
        assertThat(fake.lastUserId.get()).isEqualTo(100L);
    }

    /**
     * role=null(旧前端不传)→ fallback "student" → 不触发 teacher 反射;
     * createOrLoginBySocial 入参 role == "student"。
     */
    @Test
    void loginBySocial_nullRole_fallbackStudent_noTeacherInvocation() {
        SocialUserInfo info = SocialUserInfo.builder()
                .provider("google").oauthUid("uid-200").oauthEmail("new-student@example.com")
                .nickname("Stu").avatarUrl(null).rawAttributes(null)
                .build();
        when(socialClient.getUserInfo("google", "code-a", "state-b")).thenReturn(info);

        when(userService.createOrLoginBySocial(
                eq("google"), eq("uid-200"), eq("new-student@example.com"),
                any(), eq("student"),
                any(), any(), eq("en"), eq("UTC")))
                .thenReturn(200L);

        OAuth2AccessTokenDO stubToken = new OAuth2AccessTokenDO();
        stubToken.setUserId(200L);
        when(oauth2TokenService.createAccessToken(eq(200L), anyInt(), anyString(), anyList()))
                .thenReturn(stubToken);

        // studentPackageServiceImpl bean 缺失时 graceful degrade
        when(applicationContext.getBean("studentPackageServiceImpl"))
                .thenThrow(new NoSuchBeanDefinitionException("studentPackageServiceImpl"));

        OAuth2AccessTokenDO token = service.loginBySocial("google", "code-a", "state-b", "1.2.3.4", null);

        assertThat(token).isSameAs(stubToken);
        // 关键:role=null 走 student 分支,teacherProfileServiceImpl 反射不应被触发
        org.mockito.Mockito.verify(applicationContext, org.mockito.Mockito.never())
                .getBean("teacherProfileServiceImpl");
    }
}
