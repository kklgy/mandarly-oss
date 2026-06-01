package com.mandarly.boot.module.system.service.auth;

import com.mandarly.boot.module.system.controller.app.auth.vo.AppAuthChannelsRespVO;
import com.mandarly.boot.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;

public interface AppAuthService {

    OAuth2AccessTokenDO loginByEmail(String email, String password, String ip);

    OAuth2AccessTokenDO loginByPhone(String phone, String code, String locale, String ip);

    OAuth2AccessTokenDO loginBySocial(String provider, String code, String state, String ip, String role);

    OAuth2AccessTokenDO registerByEmail(String role, String email, String password, String code,
                                         String nickname, String locale, String tz, String refCode, String ip);

    OAuth2AccessTokenDO registerByPhone(String role, String phone, String code,
                                         String nickname, String locale, String tz, String refCode, String ip);

    void logout(String token);

    /**
     * 探测 4 个登录渠道(邮箱 / 手机 / Google / Apple)的凭证就位状态。
     * 前端 LoginView/RegisterView 据此 disable 不可用的 tab/按钮。
     */
    AppAuthChannelsRespVO getAvailableChannels();
}
