package com.mandarly.boot.module.system.service.auth;

import cn.hutool.core.util.StrUtil;
import com.mandarly.boot.module.system.framework.auth.config.AppAuthProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthAppleRequest;
import me.zhyd.oauth.request.AuthGoogleRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.system.enums.ErrorCodeConstants.AUTH_SOCIAL_FETCH_FAILED;
import static com.mandarly.boot.module.system.enums.ErrorCodeConstants.AUTH_SOCIAL_NOT_CONFIGURED;

@Service
@Slf4j
public class SocialClientImpl implements SocialClient {

    @Resource
    private AppAuthProperties props;

    @Override
    public String getRedirectUrl(String provider) {
        AuthRequest req = buildAuthRequest(provider);
        return req.authorize(UUID.randomUUID().toString());
    }

    @Override
    public SocialUserInfo getUserInfo(String provider, String code, String state) {
        AuthRequest req = buildAuthRequest(provider);
        AuthCallback callback = AuthCallback.builder().code(code).state(state).build();
        AuthResponse<AuthUser> resp;
        try {
            resp = req.login(callback);
        } catch (Exception e) {
            log.warn("[M2.5][SOCIAL] {} JustAuth login 抛错", provider, e);
            throw exception(AUTH_SOCIAL_FETCH_FAILED);
        }
        if (resp == null || !resp.ok() || resp.getData() == null) {
            log.warn("[M2.5][SOCIAL] {} JustAuth login 失败 resp={}", provider, resp);
            throw exception(AUTH_SOCIAL_FETCH_FAILED);
        }
        AuthUser u = resp.getData();
        Map<String, Object> raw = new HashMap<>();
        raw.put("uuid", u.getUuid());
        raw.put("username", u.getUsername());
        raw.put("nickname", u.getNickname());
        raw.put("avatar", u.getAvatar());
        raw.put("email", u.getEmail());
        raw.put("source", u.getSource());
        return SocialUserInfo.builder()
                .provider(provider)
                .oauthUid(u.getUuid())
                .oauthEmail(u.getEmail())
                .nickname(u.getNickname())
                .avatarUrl(u.getAvatar())
                .rawAttributes(raw)
                .build();
    }

    @Override
    public boolean isProviderConfigured(String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            AppAuthProperties.ProviderConfig g = props.getSocial().getGoogle();
            return StrUtil.isNotBlank(g.getClientId()) && StrUtil.isNotBlank(g.getClientSecret());
        }
        if ("apple".equalsIgnoreCase(provider)) {
            AppAuthProperties.ProviderConfig a = props.getSocial().getApple();
            return StrUtil.isNotBlank(a.getTeamId()) && StrUtil.isNotBlank(a.getServiceId())
                    && StrUtil.isNotBlank(a.getKeyId()) && StrUtil.isNotBlank(a.getPrivateKeyPath());
        }
        return false;
    }

    private AuthRequest buildAuthRequest(String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            AppAuthProperties.ProviderConfig g = props.getSocial().getGoogle();
            if (StrUtil.isBlank(g.getClientId()) || StrUtil.isBlank(g.getClientSecret())) {
                throw exception(AUTH_SOCIAL_NOT_CONFIGURED);
            }
            return new AuthGoogleRequest(AuthConfig.builder()
                    .clientId(g.getClientId())
                    .clientSecret(g.getClientSecret())
                    .redirectUri(g.getRedirectUri())
                    .build());
        }
        if ("apple".equalsIgnoreCase(provider)) {
            AppAuthProperties.ProviderConfig a = props.getSocial().getApple();
            if (StrUtil.isBlank(a.getTeamId()) || StrUtil.isBlank(a.getServiceId())
                    || StrUtil.isBlank(a.getKeyId()) || StrUtil.isBlank(a.getPrivateKeyPath())) {
                throw exception(AUTH_SOCIAL_NOT_CONFIGURED);
            }
            // Apple 真链路在 M2.5-ε 阶段接;此处只到 fail-fast 与构造 AuthRequest
            // TODO(M2.5-ε): 补全 teamId / keyId / privateKey 到 AuthConfig
            return new AuthAppleRequest(AuthConfig.builder()
                    .clientId(a.getServiceId())
                    .redirectUri(a.getRedirectUri())
                    // 真完整 Apple 配置(teamId / keyId / privateKey)在 ε 阶段补,这里先让 builder 接受空字段
                    .build());
        }
        throw exception(AUTH_SOCIAL_NOT_CONFIGURED);
    }
}
