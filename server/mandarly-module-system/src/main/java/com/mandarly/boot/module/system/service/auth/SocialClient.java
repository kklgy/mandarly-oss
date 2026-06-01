package com.mandarly.boot.module.system.service.auth;

public interface SocialClient {

    /**
     * 取三方登录跳转 URL(含 state)
     *
     * @param provider google / apple
     */
    String getRedirectUrl(String provider);

    /**
     * 用三方回跳的 code/state 拉用户信息
     */
    SocialUserInfo getUserInfo(String provider, String code, String state);

    /**
     * 检测三方 provider 是否凭证就位(供 channels 探测用)
     *
     * @param provider google / apple
     * @return true 凭证齐备可走,false 凭证未就位(前端应 disable 按钮)
     */
    boolean isProviderConfigured(String provider);
}
