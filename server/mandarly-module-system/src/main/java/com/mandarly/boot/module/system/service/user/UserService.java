package com.mandarly.boot.module.system.service.user;

import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;

public interface UserService {

    Long createUserByEmail(String role, String email, String password,
                           String nickname, String locale, String tz, String refCode);

    Long createUserByPhone(String role, String phone,
                           String nickname, String locale, String tz, String refCode);

    Long createOrLoginBySocial(String provider, String oauthUid, String oauthEmail,
                                String oauthRaw, String role,
                                String nickname, String avatarUrl, String locale, String tz);

    AppUserDO getById(Long id);
    AppUserDO getByEmail(String email);
    AppUserDO getByPhone(String phone);

    void verifyEmail(Long userId);
    void verifyPhone(Long userId);

    String generateUniqueReferralCode();

    void updateLastLogin(Long userId, String ip);

    void updateProfile(Long userId, String nickname, String avatarUrl, String locale,
                       String timezone, String learningGoal);

    void bindEmail(Long userId, String email);

    void bindPhone(Long userId, String phone);

    /**
     * 重置密码:按 email 找用户 → 校验密码强度 → BCrypt 加密 → 更新。
     * 验证码校验由 controller 层先调 MailCodeService.useCode 完成。
     */
    void updatePasswordByEmail(String email, String newPassword);

    /**
     * 把 user.avatarUrl(DB 存的 object key)签成 COS 临时下载 URL 供前端渲染头像。
     *
     * <p>D20 头像上线:对齐 edu.TeacherProfileService#presignIntroVideoUrl(String) 的语义,
     * normalize 兜底历史脏数据 + 15 分钟 TTL。
     *
     * @param url DB 字段值(预期 object key,兼容历史完整 signed URL)
     * @return 签名后的 COS https URL(15 分钟 TTL);null / blank 返 null
     */
    String presignAvatarUrl(String url);

    /**
     * 按推荐码查用户(M4 推荐链路反查用)
     *
     * @param referralCode 推荐码,8 位大写字母 + 数字
     * @return 对应用户,不存在时返回 null
     */
    AppUserDO getUserByReferralCode(String referralCode);
}
