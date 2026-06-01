package com.mandarly.boot.module.system.service.user;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mandarly.boot.module.infra.api.file.FileApi;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import com.mandarly.boot.module.system.dal.dataobject.user.UserOAuthDO;
import com.mandarly.boot.module.system.dal.mysql.user.AppUserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.system.enums.ErrorCodeConstants.*;

@Service
public class UserServiceImpl implements UserService {

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * avatarUrl 预签名 TTL(秒)— 15 分钟,与 edu.TeacherProfileService.presignIntroVideoUrl 对齐
     */
    private static final int AVATAR_PRESIGN_TTL_SECONDS = 900;

    @Resource
    private AppUserMapper userMapper;

    @Resource
    private UserOAuthService userOAuthService;

    /**
     * required=false:UserServiceImplTest 通过 @Import 直接实例化 Service,
     * 测试容器无 FileApi bean;生产环境 system 模块依赖 infra 模块,bean 一定存在。
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private FileApi fileApi;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUserByEmail(String role, String email, String password,
                                   String nickname, String locale, String tz, String refCode) {
        if (userMapper.selectByEmail(email) != null) {
            throw exception(AUTH_EMAIL_EXISTS);
        }
        Long referredBy = resolveReferralCode(refCode);
        AppUserDO user = AppUserDO.builder()
                .role(role)
                .email(email)
                .emailVerifiedAt(LocalDateTime.now())
                .passwordHash(encoder.encode(password))
                .nickname(StrUtil.isBlank(nickname) ? StrUtil.subBefore(email, '@', false) : nickname)
                .locale(locale)
                .timezone(tz)
                .status("active")
                .referralCode(generateUniqueReferralCode())
                .referredBy(referredBy)
                .build();
        fillSystemAudit(user);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUserByPhone(String role, String phone,
                                   String nickname, String locale, String tz, String refCode) {
        if (userMapper.selectByPhone(phone) != null) {
            throw exception(AUTH_PHONE_EXISTS);
        }
        Long referredBy = resolveReferralCode(refCode);
        String defaultNickname = StrUtil.isBlank(nickname)
                ? "user_" + phone.substring(Math.max(0, phone.length() - 4))
                : nickname;
        AppUserDO user = AppUserDO.builder()
                .role(role)
                .phone(phone)
                .phoneVerifiedAt(LocalDateTime.now())
                .nickname(defaultNickname)
                .locale(locale)
                .timezone(tz)
                .status("active")
                .referralCode(generateUniqueReferralCode())
                .referredBy(referredBy)
                .build();
        fillSystemAudit(user);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrLoginBySocial(String provider, String oauthUid, String oauthEmail,
                                       String oauthRaw, String role,
                                       String nickname, String avatarUrl, String locale, String tz) {
        UserOAuthDO existing = userOAuthService.findActive(provider, oauthUid);
        if (existing != null) {
            return existing.getUserId();
        }

        boolean hasEmail = StrUtil.isNotBlank(oauthEmail);
        Map<String, Object> rawMap = parseRaw(oauthRaw);
        if (hasEmail) {
            AppUserDO emailUser = userMapper.selectByEmail(oauthEmail);
            if (emailUser != null) {
                userOAuthService.bind(emailUser.getId(), provider, oauthUid, oauthEmail, rawMap);
                return emailUser.getId();
            }
        }

        AppUserDO user = AppUserDO.builder()
                .role(role)
                .email(hasEmail ? oauthEmail : null)
                .emailVerifiedAt(hasEmail ? LocalDateTime.now() : null)
                .nickname(StrUtil.isBlank(nickname) ? null : nickname)
                .avatarUrl(avatarUrl)
                .locale(locale)
                .timezone(tz)
                .status("active")
                .referralCode(generateUniqueReferralCode())
                .build();
        fillSystemAudit(user);
        userMapper.insert(user);

        if (StrUtil.isBlank(user.getNickname())) {
            AppUserDO update = new AppUserDO();
            update.setId(user.getId());
            update.setNickname("user_" + user.getId());
            userMapper.updateById(update);
        }

        userOAuthService.bind(user.getId(), provider, oauthUid, oauthEmail, rawMap);

        return user.getId();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRaw(String oauthRawJson) {
        if (StrUtil.isBlank(oauthRawJson)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(oauthRawJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 注册路径无登录上下文,DefaultDBFieldHandler 拿不到 userId 兜底,这里手动填 creator/updater=system
     * 让 NOT NULL 约束通过。
     */
    private static void fillSystemAudit(AppUserDO user) {
        user.setCreator("system");
        user.setUpdater("system");
    }

    @Override
    public AppUserDO getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public AppUserDO getByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public AppUserDO getByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public void verifyEmail(Long userId) {
        AppUserDO update = new AppUserDO();
        update.setId(userId);
        update.setEmailVerifiedAt(LocalDateTime.now());
        userMapper.updateById(update);
    }

    @Override
    public void verifyPhone(Long userId) {
        AppUserDO update = new AppUserDO();
        update.setId(userId);
        update.setPhoneVerifiedAt(LocalDateTime.now());
        userMapper.updateById(update);
    }

    @Override
    public String generateUniqueReferralCode() {
        for (int i = 0; i < 5; i++) {
            String code = "MAND" + randomSuffix(4);
            if (userMapper.selectByReferralCode(code) == null) {
                return code;
            }
        }
        throw exception(AUTH_REFERRAL_INVALID); // 用 referral 通用错误码兜底,空间不足异常
    }

    @Override
    public void updateLastLogin(Long userId, String ip) {
        AppUserDO update = new AppUserDO();
        update.setId(userId);
        update.setLastLoginAt(LocalDateTime.now());
        update.setLastLoginIp(ip);
        // 注册路径下尚无 SecurityContext,手动填 updater 让 NOT NULL 通过
        update.setUpdater(userId.toString());
        userMapper.updateById(update);
    }

    @Override
    public void updateProfile(Long userId, String nickname, String avatarUrl, String locale,
                               String timezone, String learningGoal) {
        AppUserDO update = new AppUserDO();
        update.setId(userId);
        if (nickname != null) update.setNickname(nickname);
        // D20 头像上线:前端 /infra/file/upload 返完整 signed URL,这里 normalize 成 object key 入库
        // 对齐 edu.TeacherProfileServiceImpl.updateOwnProfile 的 normalize 模式
        if (avatarUrl != null) update.setAvatarUrl(normalizeToObjectKey(avatarUrl));
        if (locale != null) update.setLocale(locale);
        if (timezone != null) update.setTimezone(timezone);
        if (learningGoal != null) update.setLearningGoal(learningGoal);
        userMapper.updateById(update);
    }

    @Override
    public String presignAvatarUrl(String url) {
        if (url == null || url.isBlank() || fileApi == null) {
            return url; // fileApi null 仅在单测环境,生产由 Spring 容器注入
        }
        // 兼容历史脏数据 + 重复签名安全
        return fileApi.presignGetUrl(normalizeToObjectKey(url), AVATAR_PRESIGN_TTL_SECONDS);
    }

    /**
     * 把 /infra/file/upload 返的 signed URL 规范化成 COS object key:剥 scheme://host[:port] + query string。
     *
     * <p>对齐 edu.service.teacher.TeacherQualificationServiceImpl#normalizeToObjectKey
     * (后续可抽到 infra-api 共享层去重)。
     */
    static String normalizeToObjectKey(String url) {
        if (url == null || url.isBlank()) return url;
        if (!url.contains("://")) return url;
        int schemeEnd = url.indexOf("://");
        int pathStart = url.indexOf('/', schemeEnd + 3);
        if (pathStart < 0) return url;
        String afterDomain = url.substring(pathStart + 1);
        int q = afterDomain.indexOf('?');
        return q >= 0 ? afterDomain.substring(0, q) : afterDomain;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindEmail(Long userId, String email) {
        AppUserDO existing = userMapper.selectByEmail(email);
        if (existing != null && !existing.getId().equals(userId)) {
            throw exception(AUTH_EMAIL_EXISTS);
        }
        AppUserDO update = new AppUserDO();
        update.setId(userId);
        update.setEmail(email);
        update.setEmailVerifiedAt(LocalDateTime.now());
        userMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(Long userId, String phone) {
        AppUserDO existing = userMapper.selectByPhone(phone);
        if (existing != null && !existing.getId().equals(userId)) {
            throw exception(AUTH_PHONE_EXISTS);
        }
        AppUserDO update = new AppUserDO();
        update.setId(userId);
        update.setPhone(phone);
        update.setPhoneVerifiedAt(LocalDateTime.now());
        userMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePasswordByEmail(String email, String newPassword) {
        AppUserDO user = userMapper.selectByEmail(email);
        if (user == null) {
            throw exception(AUTH_USER_NOT_FOUND);
        }
        validatePasswordStrength(newPassword);
        AppUserDO update = new AppUserDO();
        update.setId(user.getId());
        update.setPasswordHash(encoder.encode(newPassword));
        // @PermitAll 路径无登录上下文,DefaultDBFieldHandler 不填 updater → NOT NULL 约束失败
        update.setUpdater(user.getId().toString());
        userMapper.updateById(update);
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

    private String randomSuffix(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    @Override
    public AppUserDO getUserByReferralCode(String referralCode) {
        if (referralCode == null || referralCode.isBlank()) return null;
        return userMapper.selectByReferralCode(referralCode);
    }

    private Long resolveReferralCode(String refCode) {
        if (StrUtil.isBlank(refCode)) {
            return null;
        }
        AppUserDO inviter = userMapper.selectByReferralCode(refCode);
        if (inviter == null) {
            throw exception(AUTH_REFERRAL_INVALID);
        }
        return inviter.getId();
    }
}
