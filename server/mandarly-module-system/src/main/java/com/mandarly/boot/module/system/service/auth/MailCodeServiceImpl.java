package com.mandarly.boot.module.system.service.auth;

import cn.hutool.core.util.RandomUtil;
import com.mandarly.boot.module.system.framework.auth.config.AppAuthProperties;
import com.mandarly.boot.module.system.framework.auth.config.SmtpAvailabilityCheck;
import com.mandarly.boot.module.system.service.mail.MailSendService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.system.enums.ErrorCodeConstants.*;

@Service
@Slf4j
public class MailCodeServiceImpl implements MailCodeService {

    private static final String KEY_VAL_PREFIX = "auth:mail-code:val:";
    private static final String KEY_CD_PREFIX = "auth:mail-code:cd:";
    private static final String KEY_DAILY_PREFIX = "auth:mail-code:daily:";

    @Resource
    private AppAuthProperties props;

    @Resource
    private StringRedisTemplate redis;

    @Resource
    private SmtpAvailabilityCheck smtp;

    @Resource
    private MailSendService mailSendService;

    @Override
    public void sendCode(String email, String scene, String locale) {
        String cooldownKey = KEY_CD_PREFIX + email;
        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey))) {
            throw exception(AUTH_CODE_COOLDOWN);
        }
        if (!smtp.isAvailable()) {
            throw exception(AUTH_SMTP_NOT_CONFIGURED);
        }

        String dailyKey = KEY_DAILY_PREFIX + email + ":" + LocalDate.now();
        Long cnt = redis.opsForValue().increment(dailyKey);
        if (cnt != null && cnt == 1L) {
            redis.expire(dailyKey, Duration.ofDays(1));
        }
        if (cnt != null && cnt > props.getEmailCode().getDailyLimit()) {
            throw exception(AUTH_RATE_LIMIT_DAILY);
        }

        String code = RandomUtil.randomNumbers(props.getEmailCode().getLength());
        String valKey = KEY_VAL_PREFIX + scene + ":" + email;
        redis.opsForValue().set(valKey, code, Duration.ofSeconds(props.getEmailCode().getTtlSeconds()));
        redis.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(props.getEmailCode().getCooldownSeconds()));

        String templateCode = pickMailTemplateCode(scene, locale);
        try {
            Long mailLogId = mailSendService.sendSingleMailToMemberSync(null, List.of(email), null, null,
                    templateCode, Map.of("code", code), (java.io.File[]) null);
            if (mailLogId == null) {
                throw new IllegalStateException("邮件发送日志未生成");
            }
            log.info("[M2.5][MAIL] sent email={} scene={} locale={} template={} logId={}",
                    email, scene, locale, templateCode, mailLogId);
        } catch (Exception e) {
            redis.delete(valKey);
            redis.delete(cooldownKey);
            log.warn("[M2.5][MAIL] queue failed email={} scene={} template={} err={}",
                    email, scene, templateCode, e.getMessage());
            throw exception(AUTH_MAIL_SEND_FAILED);
        }
    }

    @Override
    public void useCode(String email, String code, String scene) {
        String valKey = KEY_VAL_PREFIX + scene + ":" + email;
        String saved = redis.opsForValue().get(valKey);
        if (saved == null) {
            throw exception(AUTH_CODE_EXPIRED);
        }
        if (!saved.equals(code)) {
            throw exception(AUTH_CODE_INVALID);
        }
        redis.delete(valKey); // 一次性
    }

    /**
     * 路由到 system_mail_template.code:scene + locale → auth-mail-{base}[-en]
     *   register → auth-mail-register / auth-mail-register-en
     *   reset / reset-password → auth-mail-reset / auth-mail-reset-en
     *   bind → 复用 auth-mail-register*(一期)
     *   default(login)→ auth-mail-login / auth-mail-login-en
     */
    static String pickMailTemplateCode(String scene, String locale) {
        String base = switch (scene == null ? "" : scene) {
            case "register", "bind" -> "auth-mail-register";
            case "reset", "reset-password" -> "auth-mail-reset";
            default -> "auth-mail-login";
        };
        boolean zh = locale != null && locale.toLowerCase().startsWith("zh");
        return zh ? base : base + "-en";
    }
}
