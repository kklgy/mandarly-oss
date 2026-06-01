package com.mandarly.boot.module.system.service.auth;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mandarly.boot.framework.common.enums.UserTypeEnum;
import com.mandarly.boot.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.mandarly.boot.module.system.controller.app.auth.vo.AppAuthChannelsRespVO;
import com.mandarly.boot.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.mandarly.boot.module.system.dal.dataobject.sms.SmsChannelDO;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import com.mandarly.boot.module.system.dal.mysql.sms.SmsChannelMapper;
import com.mandarly.boot.module.system.enums.sms.SmsSceneEnum;
import com.mandarly.boot.module.system.framework.auth.config.AppAuthProperties;
import com.mandarly.boot.module.system.framework.auth.config.SmtpAvailabilityCheck;
import com.mandarly.boot.module.system.service.oauth2.OAuth2TokenService;
import com.mandarly.boot.module.system.service.sms.SmsCodeService;
import com.mandarly.boot.module.system.service.user.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.system.enums.ErrorCodeConstants.*;

@Service
@Slf4j
public class AppAuthServiceImpl implements AppAuthService {

    private static final String CLIENT_ID = "app-client";
    private static final List<String> SCOPES = List.of("user.read", "user.write");

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource private UserService userService;
    @Resource private MailCodeService mailCodeService;
    @Resource private SmsCodeService smsCodeService;
    @Resource private SocialClient socialClient;
    @Resource private OAuth2TokenService oauth2TokenService;
    @Resource private StringRedisTemplate redis;
    @Resource private AppAuthProperties props;
    @Resource private ApplicationContext applicationContext;
    @Resource private SmtpAvailabilityCheck smtp;
    @Resource private SmsChannelMapper smsChannelMapper;

    @Override
    public OAuth2AccessTokenDO loginByEmail(String email, String password, String ip) {
        AppUserDO user = userService.getByEmail(email);
        if (user == null) {
            throw exception(AUTH_USER_NOT_FOUND);
        }
        checkLoginNotLocked(user.getId());
        if (StrUtil.isBlank(user.getPasswordHash()) || !encoder.matches(password, user.getPasswordHash())) {
            recordLoginFailure(user.getId());
            throw exception(AUTH_INVALID_CREDENTIALS);
        }
        if ("frozen".equals(user.getStatus())) {
            throw exception(AUTH_USER_FROZEN);
        }
        userService.updateLastLogin(user.getId(), ip);
        return oauth2TokenService.createAccessToken(
                user.getId(), UserTypeEnum.MEMBER.getValue(), CLIENT_ID, SCOPES);
    }

    @Override
    public OAuth2AccessTokenDO loginByPhone(String phone, String code, String locale, String ip) {
        useSmsCode(phone, code, ip, pickSmsScene(false, locale, phone));
        AppUserDO user = userService.getByPhone(phone);
        if (user == null) {
            throw exception(AUTH_USER_NOT_FOUND);
        }
        if ("frozen".equals(user.getStatus())) {
            throw exception(AUTH_USER_FROZEN);
        }
        userService.updateLastLogin(user.getId(), ip);
        return oauth2TokenService.createAccessToken(
                user.getId(), UserTypeEnum.MEMBER.getValue(), CLIENT_ID, SCOPES);
    }

    @Override
    public OAuth2AccessTokenDO loginBySocial(String provider, String code, String state, String ip, String role) {
        // D19-A3:role 由前端注册入口透传(student / teacher);旧前端不带时 fallback student
        // 走 existing-user-by-(provider,uid) 或 existing-user-by-email 绑定路径时,
        // UserServiceImpl.createOrLoginBySocial 会忽略此 role 保留旧 user 原 role(D15-D)
        String effectiveRole = (role == null || role.isBlank()) ? "student" : role;
        SocialUserInfo info = socialClient.getUserInfo(provider, code, state);
        Long userId = userService.createOrLoginBySocial(
                provider, info.getOauthUid(), info.getOauthEmail(),
                toJson(info.getRawAttributes()),
                effectiveRole,
                info.getNickname(), info.getAvatarUrl(),
                "en", "UTC");
        // 仅当此次三方调用真的新建了 user 时(createOrLoginBySocial 内部 already 处理 idempotent),
        // grantFreeTrial 也是幂等的(Task 1.6),重复调用安全
        grantFreeTrialIfStudent(userId, effectiveRole);
        createPendingTeacherProfileIfTeacher(userId, effectiveRole);
        userService.updateLastLogin(userId, ip);
        return oauth2TokenService.createAccessToken(
                userId, UserTypeEnum.MEMBER.getValue(), CLIENT_ID, SCOPES);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuth2AccessTokenDO registerByEmail(String role, String email, String password, String code,
                                                String nickname, String locale, String tz, String refCode, String ip) {
        validatePasswordStrength(password);
        mailCodeService.useCode(email, code, "register");
        checkIpRegisterLimit(ip);
        Long userId = userService.createUserByEmail(role, email, password, nickname, locale, tz, refCode);
        grantFreeTrialIfStudent(userId, role);
        createPendingTeacherProfileIfTeacher(userId, role);
        userService.updateLastLogin(userId, ip);
        return oauth2TokenService.createAccessToken(
                userId, UserTypeEnum.MEMBER.getValue(), CLIENT_ID, SCOPES);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuth2AccessTokenDO registerByPhone(String role, String phone, String code,
                                                String nickname, String locale, String tz, String refCode, String ip) {
        useSmsCode(phone, code, ip, pickSmsScene(true, locale, phone));
        checkIpRegisterLimit(ip);
        Long userId = userService.createUserByPhone(role, phone, nickname, locale, tz, refCode);
        grantFreeTrialIfStudent(userId, role);
        createPendingTeacherProfileIfTeacher(userId, role);
        userService.updateLastLogin(userId, ip);
        return oauth2TokenService.createAccessToken(
                userId, UserTypeEnum.MEMBER.getValue(), CLIENT_ID, SCOPES);
    }

    @Override
    public void logout(String token) {
        if (StrUtil.isBlank(token)) {
            return;
        }
        oauth2TokenService.removeAccessToken(token);
    }

    @Override
    public AppAuthChannelsRespVO getAvailableChannels() {
        return AppAuthChannelsRespVO.builder()
                .email(smtp.isAvailable())
                .sms(smsChannelAvailable())
                .google(socialClient.isProviderConfigured("google"))
                .apple(socialClient.isProviderConfigured("apple"))
                .build();
    }

    /**
     * SMS 渠道可用性:任一 system_sms_channel 状态 = 0(开启)且 api_key 不含 "PENDING" 占位串。
     * api_key 是腾讯云 SecretId+SmsSdkAppId 拼串,凭证占位时形如 "PENDING_SECRETID PENDING_SDKAPPID"。
     */
    boolean smsChannelAvailable() {
        List<SmsChannelDO> channels = smsChannelMapper.selectList(
                Wrappers.<SmsChannelDO>lambdaQuery().eq(SmsChannelDO::getStatus, 0));
        return channels.stream().anyMatch(c ->
                StrUtil.isNotBlank(c.getApiKey()) && !c.getApiKey().contains("PENDING"));
    }

    /**
     * 按 mobile + locale 选 SmsSceneEnum:
     *   +86 → CN scene(中国大陆国内通道);
     *   zh-* → ZH scene(海外 ISMS 中文模板);
     *   en/* → EN scene(海外 ISMS 英文模板)。
     * 必须与发码端口一致(发 ZH 模板的码,验证也得用 ZH scene)。
     */
    static SmsSceneEnum pickSmsScene(boolean register, String locale, String phone) {
        if (isMainlandMobile(phone)) {
            return register ? SmsSceneEnum.MEMBER_REGISTER_CN : SmsSceneEnum.MEMBER_LOGIN_CN;
        }
        boolean zh = locale != null && locale.toLowerCase().startsWith("zh");
        if (register) {
            return zh ? SmsSceneEnum.MEMBER_REGISTER : SmsSceneEnum.MEMBER_REGISTER_EN;
        }
        return zh ? SmsSceneEnum.MEMBER_LOGIN : SmsSceneEnum.MEMBER_LOGIN_EN;
    }

    public static void checkSmsPhoneSupported(String phone) {
        // +86 is enabled through the domestic Tencent SMS channel.
    }

    public static boolean isMainlandMobile(String phone) {
        return phone != null && phone.startsWith("+86");
    }

    private void useSmsCode(String phone, String code, String ip, SmsSceneEnum scene) {
        SmsCodeUseReqDTO useReq = new SmsCodeUseReqDTO();
        useReq.setMobile(phone);
        useReq.setCode(code);
        useReq.setScene(scene.getScene());
        useReq.setUsedIp(StrUtil.isBlank(ip) ? "0.0.0.0" : ip);
        smsCodeService.useSmsCode(useReq);
    }

    private void grantFreeTrialIfStudent(Long userId, String role) {
        if (!"student".equals(role)) {
            return;
        }
        // 反射调用 edu 模块的 StudentPackageService.grantFreeTrialPackage(Long)
        // 避免 system → edu 直接 import(模块依赖方向相反)
        try {
            Object bean = applicationContext.getBean("studentPackageServiceImpl");
            Method method = bean.getClass().getMethod("grantFreeTrialPackage", Long.class);
            method.invoke(bean, userId);
        } catch (BeansException e) {
            log.warn("[M2.5] StudentPackageService bean 不存在,跳过 grant 免费体验 userId={}", userId);
        } catch (Exception e) {
            log.warn("[M2.5] grant 免费体验失败 userId={}, 不阻塞登录注册", userId, e);
        }
    }

    /**
     * 注册路径 role=teacher 时反射调用 edu 模块的 TeacherProfileServiceImpl.createPendingProfile(Long)。
     *
     * <p>避免 system → edu 直接 import(模块依赖方向反过来);失败不阻塞注册。
     *
     * <p>设计上与 {@link #grantFreeTrialIfStudent(Long, String)} 对称。
     */
    void createPendingTeacherProfileIfTeacher(Long userId, String role) {
        if (!"teacher".equals(role)) {
            return;
        }
        try {
            Object bean = applicationContext.getBean("teacherProfileServiceImpl");
            Method method = bean.getClass().getMethod("createPendingProfile", Long.class);
            method.invoke(bean, userId);
        } catch (BeansException e) {
            log.warn("[D19-A2] TeacherProfileService bean 不存在,跳过建 draft profile userId={}", userId);
        } catch (Exception e) {
            log.warn("[D19-A2] 建 draft profile 失败 userId={}, 不阻塞注册", userId, e);
        }
    }

    private void validatePasswordStrength(String pwd) {
        if (StrUtil.length(pwd) < 8) {
            throw exception(AUTH_PASSWORD_WEAK);
        }
        boolean hasLetter = pwd.chars().anyMatch(Character::isLetter);
        boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw exception(AUTH_PASSWORD_WEAK);
        }
    }

    private void checkIpRegisterLimit(String ip) {
        if (StrUtil.isBlank(ip)) {
            return;
        }
        String key = "auth:reg:ip:" + ip;
        Long cnt = redis.opsForValue().increment(key);
        if (cnt != null && cnt == 1L) {
            redis.expire(key, Duration.ofSeconds(props.getRateLimit().getIpRegisterWindowSeconds()));
        }
        if (cnt != null && cnt > props.getRateLimit().getIpRegisterMax()) {
            throw exception(AUTH_IP_REGISTER_LIMIT);
        }
    }

    private void checkLoginNotLocked(Long userId) {
        String key = "auth:login-fail:" + userId;
        String val = redis.opsForValue().get(key);
        if (val != null && Long.parseLong(val) >= props.getRateLimit().getLoginFailLockThreshold()) {
            throw exception(AUTH_LOGIN_FAILED_LOCKED);
        }
    }

    private void recordLoginFailure(Long userId) {
        String key = "auth:login-fail:" + userId;
        Long cnt = redis.opsForValue().increment(key);
        if (cnt != null && cnt == 1L) {
            redis.expire(key, Duration.ofSeconds(props.getRateLimit().getLoginFailLockWindowSeconds()));
        }
    }

    private String toJson(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            log.debug("toJson failed", e);
            return null;
        }
    }
}
