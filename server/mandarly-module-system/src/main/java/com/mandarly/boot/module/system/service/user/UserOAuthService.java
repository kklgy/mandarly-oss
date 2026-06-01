package com.mandarly.boot.module.system.service.user;

import com.mandarly.boot.module.system.dal.dataobject.user.UserOAuthDO;

import java.util.List;
import java.util.Map;

public interface UserOAuthService {

    void bind(Long userId, String provider, String oauthUid, String oauthEmail, Map<String, Object> oauthRaw);

    void unbind(Long userId, String provider, String oauthUid);

    void unbindByUserAndProvider(Long userId, String provider);

    UserOAuthDO findActive(String provider, String oauthUid);

    List<UserOAuthDO> listActiveByUser(Long userId);
}
