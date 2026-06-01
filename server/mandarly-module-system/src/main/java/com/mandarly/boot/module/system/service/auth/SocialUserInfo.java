package com.mandarly.boot.module.system.service.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {
    private String provider;
    private String oauthUid;
    private String oauthEmail;
    private String nickname;
    private String avatarUrl;
    private Map<String, Object> rawAttributes;
}
