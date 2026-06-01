package com.mandarly.boot.module.system.service.auth;

import com.mandarly.boot.module.system.controller.app.auth.AppAuthController;
import com.mandarly.boot.module.system.enums.sms.SmsSceneEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M2.5-β / C2:locale + phone 路由 SmsSceneEnum 单元测试
 *
 * 关键不变量:send/use 两端必须命中同一 scene,
 * 否则 SmsCodeMapper.selectLastByMobile(mobile, code, scene) 验证拿不到记录。
 *
 * 路由优先级:+86 → 国内短信模板;其它号码按 locale zh-* → ZH(海外 ISMS 中文)> EN(海外 ISMS 英文)
 */
class AppAuthLocaleRoutingTest {

    // ==================== pickSmsScene(register, locale, phone) ====================

    @Test
    void pickScene_zhCN_register_returnsZh() {
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "zh-CN", "+85298765432")).isEqualTo(SmsSceneEnum.MEMBER_REGISTER);
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "zh-HK", "+85298765432")).isEqualTo(SmsSceneEnum.MEMBER_REGISTER);
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "zh-TW", "+886912345678")).isEqualTo(SmsSceneEnum.MEMBER_REGISTER);
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "ZH", null)).isEqualTo(SmsSceneEnum.MEMBER_REGISTER);
    }

    @Test
    void pickScene_zhCN_login_returnsZh() {
        assertThat(AppAuthServiceImpl.pickSmsScene(false, "zh-CN", "+85298765432")).isEqualTo(SmsSceneEnum.MEMBER_LOGIN);
        assertThat(AppAuthServiceImpl.pickSmsScene(false, "zh-HK", "+85298765432")).isEqualTo(SmsSceneEnum.MEMBER_LOGIN);
    }

    @Test
    void pickScene_en_register_returnsEn() {
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "en", "+13022682332")).isEqualTo(SmsSceneEnum.MEMBER_REGISTER_EN);
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "en-US", "+13022682332")).isEqualTo(SmsSceneEnum.MEMBER_REGISTER_EN);
    }

    @Test
    void pickScene_en_login_returnsEn() {
        assertThat(AppAuthServiceImpl.pickSmsScene(false, "en", "+13022682332")).isEqualTo(SmsSceneEnum.MEMBER_LOGIN_EN);
        assertThat(AppAuthServiceImpl.pickSmsScene(false, "en-US", "+447700900123")).isEqualTo(SmsSceneEnum.MEMBER_LOGIN_EN);
    }

    @Test
    void pickScene_arOrUnknown_login_returnsEn() {
        assertThat(AppAuthServiceImpl.pickSmsScene(false, "ar", "+971501234567")).isEqualTo(SmsSceneEnum.MEMBER_LOGIN_EN);
        assertThat(AppAuthServiceImpl.pickSmsScene(false, "fr", "+33612345678")).isEqualTo(SmsSceneEnum.MEMBER_LOGIN_EN);
        assertThat(AppAuthServiceImpl.pickSmsScene(false, "", null)).isEqualTo(SmsSceneEnum.MEMBER_LOGIN_EN);
    }

    @Test
    void pickScene_nullLocale_falsbacksToEn() {
        assertThat(AppAuthServiceImpl.pickSmsScene(true, null, "+13022682332")).isEqualTo(SmsSceneEnum.MEMBER_REGISTER_EN);
        assertThat(AppAuthServiceImpl.pickSmsScene(false, null, null)).isEqualTo(SmsSceneEnum.MEMBER_LOGIN_EN);
    }

    // ==================== M2.5-C2 phone +86 路由优先级 ====================

    @Test
    void checkSmsPhoneSupported_cn_phone_allowed() {
        AppAuthServiceImpl.checkSmsPhoneSupported("+8613912345678");
    }

    @Test
    void pickScene_cn_phone_overridesLocaleToDomesticTemplate() {
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "zh-CN", "+8613912345678"))
                .isEqualTo(SmsSceneEnum.MEMBER_REGISTER_CN);
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "en", "+8613912345678"))
                .isEqualTo(SmsSceneEnum.MEMBER_REGISTER_CN);
        assertThat(AppAuthServiceImpl.pickSmsScene(false, "zh-HK", "+8616657104800"))
                .isEqualTo(SmsSceneEnum.MEMBER_LOGIN_CN);
        assertThat(AppAuthServiceImpl.pickSmsScene(false, null, "+8616657104800"))
                .isEqualTo(SmsSceneEnum.MEMBER_LOGIN_CN);
    }

    @Test
    void pickScene_nonCn_phone_isIgnored() {
        // +852/+1/其它都不算大陆,按 locale 走 ZH/EN
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "zh-HK", "+85298765432")).isEqualTo(SmsSceneEnum.MEMBER_REGISTER);
        assertThat(AppAuthServiceImpl.pickSmsScene(true, "en", "+85298765432")).isEqualTo(SmsSceneEnum.MEMBER_REGISTER_EN);
    }

    // ==================== mapSmsScene(scene, locale, phone) ====================

    @Test
    void mapSmsScene_register_zh_returnsZh() {
        assertThat(AppAuthController.mapSmsScene("register", "zh-CN", "+85298765432"))
                .isEqualTo(SmsSceneEnum.MEMBER_REGISTER.getScene());
    }

    @Test
    void mapSmsScene_register_en_returnsEn() {
        assertThat(AppAuthController.mapSmsScene("register", "en", "+13022682332"))
                .isEqualTo(SmsSceneEnum.MEMBER_REGISTER_EN.getScene());
    }

    @Test
    void mapSmsScene_register_cn_phone_usesDomesticTemplate() {
        assertThat(AppAuthController.mapSmsScene("register", "en", "+8613912345678"))
                .isEqualTo(SmsSceneEnum.MEMBER_REGISTER_CN.getScene());
        assertThat(AppAuthController.mapSmsScene("register", "zh-CN", "+8616657104800"))
                .isEqualTo(SmsSceneEnum.MEMBER_REGISTER_CN.getScene());
    }

    @Test
    void mapSmsScene_login_default_byLocaleAndPhone() {
        assertThat(AppAuthController.mapSmsScene("login", "zh-HK", "+85298765432"))
                .isEqualTo(SmsSceneEnum.MEMBER_LOGIN.getScene());
        assertThat(AppAuthController.mapSmsScene(null, "en", "+13022682332"))
                .isEqualTo(SmsSceneEnum.MEMBER_LOGIN_EN.getScene());
        assertThat(AppAuthController.mapSmsScene("login", "en", "+8613912345678"))
                .isEqualTo(SmsSceneEnum.MEMBER_LOGIN_CN.getScene());
        assertThat(AppAuthController.mapSmsScene(null, null, "+8616657104800"))
                .isEqualTo(SmsSceneEnum.MEMBER_LOGIN_CN.getScene());
    }

    @Test
    void mapSmsScene_reset_routesCnPhoneToDomesticTemplate() {
        assertThat(AppAuthController.mapSmsScene("reset", "en", "+13022682332"))
                .isEqualTo(SmsSceneEnum.MEMBER_RESET_PASSWORD.getScene());
        assertThat(AppAuthController.mapSmsScene("reset", "zh-CN", "+8613912345678"))
                .isEqualTo(SmsSceneEnum.MEMBER_RESET_PASSWORD_CN.getScene());
    }

    @Test
    void mapSmsScene_bind_usesUpdateMobileScene() {
        assertThat(AppAuthController.mapSmsScene("bind", "en", "+13022682332"))
                .isEqualTo(SmsSceneEnum.MEMBER_UPDATE_MOBILE.getScene());
        assertThat(AppAuthController.mapSmsScene("bind", "zh-CN", "+8613912345678"))
                .isEqualTo(SmsSceneEnum.MEMBER_UPDATE_MOBILE_CN.getScene());
    }
}
