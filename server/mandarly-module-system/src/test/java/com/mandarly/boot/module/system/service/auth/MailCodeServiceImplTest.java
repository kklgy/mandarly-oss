package com.mandarly.boot.module.system.service.auth;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.module.system.framework.auth.config.AppAuthProperties;
import com.mandarly.boot.module.system.framework.auth.config.SmtpAvailabilityCheck;
import com.mandarly.boot.module.system.service.mail.MailSendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailCodeServiceImplTest {

    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> ops;
    @Mock private AppAuthProperties props;
    @Mock private SmtpAvailabilityCheck smtp;
    @Mock private AppAuthProperties.CodeConfig codeConfig;
    @Mock private MailSendService mailSendService;

    @InjectMocks private MailCodeServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(redis.opsForValue()).thenReturn(ops);
        lenient().when(props.getEmailCode()).thenReturn(codeConfig);
        lenient().when(codeConfig.getLength()).thenReturn(6);
        lenient().when(codeConfig.getTtlSeconds()).thenReturn(300);
        lenient().when(codeConfig.getCooldownSeconds()).thenReturn(60);
        lenient().when(codeConfig.getDailyLimit()).thenReturn(10);
    }

    @Test
    void sendCode_cooldown_throws() {
        when(redis.hasKey("auth:mail-code:cd:a@b.c")).thenReturn(true);
        assertThatThrownBy(() -> service.sendCode("a@b.c", "register", "zh-CN"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("过于频繁");
    }

    @Test
    void sendCode_dailyLimit_throws() {
        when(redis.hasKey(anyString())).thenReturn(false);
        when(smtp.isAvailable()).thenReturn(true);
        when(ops.increment(anyString())).thenReturn(11L);
        assertThatThrownBy(() -> service.sendCode("a@b.c", "register", "zh-CN"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("上限");
    }

    @Test
    void sendCode_ok_setsValAndCooldown() {
        when(redis.hasKey(anyString())).thenReturn(false);
        when(smtp.isAvailable()).thenReturn(true);
        when(ops.increment(anyString())).thenReturn(1L);
        when(mailSendService.sendSingleMailToMemberSync(isNull(), any(), isNull(), isNull(),
                anyString(), anyMap(), isNull())).thenReturn(1L);
        service.sendCode("a@b.c", "register", "zh-CN");
        verify(ops).set(eq("auth:mail-code:val:register:a@b.c"), anyString(), any(Duration.class));
        verify(ops).set(eq("auth:mail-code:cd:a@b.c"), eq("1"), any(Duration.class));
    }

    @Test
    void useCode_expired_throws() {
        when(ops.get("auth:mail-code:val:register:a@b.c")).thenReturn(null);
        assertThatThrownBy(() -> service.useCode("a@b.c", "123456", "register"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已过期");
    }

    @Test
    void useCode_invalid_throws() {
        when(ops.get("auth:mail-code:val:register:a@b.c")).thenReturn("999999");
        assertThatThrownBy(() -> service.useCode("a@b.c", "123456", "register"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不正确");
    }

    @Test
    void useCode_correct_deletesKey() {
        when(ops.get("auth:mail-code:val:register:a@b.c")).thenReturn("123456");
        service.useCode("a@b.c", "123456", "register");
        verify(redis).delete("auth:mail-code:val:register:a@b.c");
    }

    // ==================== M2.5-γ pickMailTemplateCode ====================

    @Test
    void pickTemplate_register_zhAndOther() {
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("register", "zh-CN")).isEqualTo("auth-mail-register");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("register", "zh-TW")).isEqualTo("auth-mail-register");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("register", "en")).isEqualTo("auth-mail-register-en");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("register", "ar")).isEqualTo("auth-mail-register-en");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("register", null)).isEqualTo("auth-mail-register-en");
    }

    @Test
    void pickTemplate_login_default() {
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("login", "zh-CN")).isEqualTo("auth-mail-login");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("login", "en")).isEqualTo("auth-mail-login-en");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode(null, "zh-CN")).isEqualTo("auth-mail-login");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("", "en")).isEqualTo("auth-mail-login-en");
    }

    @Test
    void pickTemplate_reset_acceptsBothNames() {
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("reset", "zh-CN")).isEqualTo("auth-mail-reset");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("reset", "en")).isEqualTo("auth-mail-reset-en");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("reset-password", "zh-CN")).isEqualTo("auth-mail-reset");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("reset-password", "en")).isEqualTo("auth-mail-reset-en");
    }

    @Test
    void pickTemplate_bind_reusesRegister() {
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("bind", "zh-CN")).isEqualTo("auth-mail-register");
        assertThat(MailCodeServiceImpl.pickMailTemplateCode("bind", "en")).isEqualTo("auth-mail-register-en");
    }

    // ==================== M2.5-γ smtp-available 时同步发送邮件 ====================

    @Test
    void sendCode_smtpAvailable_callsMailSendServiceSync() {
        when(redis.hasKey(anyString())).thenReturn(false);
        when(ops.increment(anyString())).thenReturn(1L);
        when(smtp.isAvailable()).thenReturn(true);
        when(mailSendService.sendSingleMailToMemberSync(isNull(), any(), isNull(), isNull(),
                anyString(), anyMap(), isNull())).thenReturn(1L);

        service.sendCode("a@b.c", "register", "zh-CN");

        verify(mailSendService).sendSingleMailToMemberSync(
                isNull(),
                argThat((Collection<String> toMails) -> toMails.size() == 1 && toMails.contains("a@b.c")),
                isNull(),
                isNull(),
                eq("auth-mail-register"),
                argThat((Map<String, Object> params) -> params.containsKey("code")),
                (File[]) isNull());
    }

    @Test
    void sendCode_smtpAvailable_enLocale_picksEnTemplate() {
        when(redis.hasKey(anyString())).thenReturn(false);
        when(ops.increment(anyString())).thenReturn(1L);
        when(smtp.isAvailable()).thenReturn(true);
        when(mailSendService.sendSingleMailToMemberSync(isNull(), any(), isNull(), isNull(),
                anyString(), anyMap(), isNull())).thenReturn(1L);

        service.sendCode("a@b.c", "reset-password", "en");

        verify(mailSendService).sendSingleMailToMemberSync(isNull(), any(), isNull(), isNull(),
                eq("auth-mail-reset-en"), anyMap(), (File[]) isNull());
    }

    @Test
    void sendCode_smtpUnavailable_skipsMailSendApi() {
        when(redis.hasKey(anyString())).thenReturn(false);
        when(smtp.isAvailable()).thenReturn(false);

        assertThatThrownBy(() -> service.sendCode("a@b.c", "register", "zh-CN"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("邮件服务未就位");

        verify(mailSendService, never()).sendSingleMailToMemberSync(any(), any(), any(), any(),
                anyString(), anyMap(), any());
        verify(ops, never()).increment(anyString());
        verify(ops, never()).set(eq("auth:mail-code:val:register:a@b.c"), anyString(), any(Duration.class));
    }

    @Test
    void sendCode_mailSendServiceThrows_propagatesAndRollsBackCode() {
        when(redis.hasKey(anyString())).thenReturn(false);
        when(ops.increment(anyString())).thenReturn(1L);
        when(smtp.isAvailable()).thenReturn(true);
        doThrow(new RuntimeException("SMTP down")).when(mailSendService)
                .sendSingleMailToMemberSync(isNull(), any(), isNull(), isNull(),
                        anyString(), anyMap(), isNull());

        assertThatThrownBy(() -> service.sendCode("a@b.c", "register", "zh-CN"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("邮件发送失败");

        verify(mailSendService).sendSingleMailToMemberSync(isNull(), any(), isNull(), isNull(),
                eq("auth-mail-register"), anyMap(), (File[]) isNull());
        verify(redis).delete("auth:mail-code:val:register:a@b.c");
        verify(redis).delete("auth:mail-code:cd:a@b.c");
    }

    @Test
    void sendCode_mailSendServiceReturnsNull_propagatesAndRollsBackCode() {
        when(redis.hasKey(anyString())).thenReturn(false);
        when(ops.increment(anyString())).thenReturn(1L);
        when(smtp.isAvailable()).thenReturn(true);
        when(mailSendService.sendSingleMailToMemberSync(isNull(), any(), isNull(), isNull(),
                anyString(), anyMap(), isNull())).thenReturn(null);

        assertThatThrownBy(() -> service.sendCode("a@b.c", "register", "zh-CN"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("邮件发送失败");

        verify(redis).delete("auth:mail-code:val:register:a@b.c");
        verify(redis).delete("auth:mail-code:cd:a@b.c");
    }
}
