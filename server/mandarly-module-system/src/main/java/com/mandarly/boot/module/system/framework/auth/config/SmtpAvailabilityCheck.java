package com.mandarly.boot.module.system.framework.auth.config;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmtpAvailabilityCheck {

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    private boolean available;

    @PostConstruct
    void check() {
        available = StrUtil.isNotBlank(smtpUsername) && StrUtil.isNotBlank(smtpPassword);
        if (!available) {
            log.warn("[M2.5] SMTP 未就位(SMTP_USERNAME / PASSWORD 空),邮件验证码不可用");
        } else {
            log.info("[M2.5] SMTP 已就位 username={}", smtpUsername);
        }
    }

    public boolean isAvailable() {
        return available;
    }
}
