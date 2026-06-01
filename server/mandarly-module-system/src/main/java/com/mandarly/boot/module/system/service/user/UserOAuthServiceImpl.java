package com.mandarly.boot.module.system.service.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.module.system.dal.dataobject.user.UserOAuthDO;
import com.mandarly.boot.module.system.dal.mysql.user.UserOAuthMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class UserOAuthServiceImpl implements UserOAuthService {

    @Resource
    private UserOAuthMapper userOAuthMapper;

    @Override
    public void bind(Long userId, String provider, String oauthUid, String oauthEmail,
                     Map<String, Object> oauthRaw) {
        // 复用历史 row 语义:(provider, oauth_uid) 在 deleted=0 内唯一(uk_provider_uid_deleted),
        // 解绑只设 unbound_at != NULL 不动 deleted,所以历史 row 仍占唯一键。
        // 重新绑定时复用旧 row:清掉 unbound_at + 更新 userId/email/raw/bound_at,避免 insert 撞唯一键。
        UserOAuthDO existing = userOAuthMapper.selectAnyByProviderAndUid(provider, oauthUid);
        if (existing != null) {
            // entity 提供 SET 子句(走 @TableField JacksonTypeHandler 序列化 oauthRaw → JSON);
            // wrapper.set(unbound_at, null) 显式清 null(MP 默认 update 跳 null,不能用 entity.setUnboundAt(null))
            // wrapper.eq(id) 提供 WHERE;两者 SET 子句合并
            UserOAuthDO patch = UserOAuthDO.builder()
                    .userId(userId)
                    .oauthEmail(oauthEmail)
                    .oauthRaw(oauthRaw)
                    .boundAt(LocalDateTime.now())
                    .build();
            patch.setUpdater(userId == null ? "system" : userId.toString());
            userOAuthMapper.update(patch, Wrappers.<UserOAuthDO>lambdaUpdate()
                    .eq(UserOAuthDO::getId, existing.getId())
                    .set(UserOAuthDO::getUnboundAt, null));
            return;
        }
        UserOAuthDO entity = UserOAuthDO.builder()
                .userId(userId)
                .provider(provider)
                .oauthUid(oauthUid)
                .oauthEmail(oauthEmail)
                .oauthRaw(oauthRaw)
                .boundAt(LocalDateTime.now())
                .build();
        // 三方登录场景常无登录上下文(callback 阶段),手动兜底 creator/updater
        entity.setCreator(userId == null ? "system" : userId.toString());
        entity.setUpdater(entity.getCreator());
        userOAuthMapper.insert(entity);
    }

    @Override
    public void unbind(Long userId, String provider, String oauthUid) {
        UserOAuthDO active = userOAuthMapper.selectActiveByProviderAndUid(provider, oauthUid);
        if (active == null || !Objects.equals(active.getUserId(), userId)) {
            return;
        }
        UserOAuthDO update = new UserOAuthDO();
        update.setId(active.getId());
        update.setUnboundAt(LocalDateTime.now());
        userOAuthMapper.updateById(update);
    }

    @Override
    public void unbindByUserAndProvider(Long userId, String provider) {
        UserOAuthDO active = userOAuthMapper.selectActiveByUserAndProvider(userId, provider);
        if (active == null) {
            return;
        }
        UserOAuthDO update = new UserOAuthDO();
        update.setId(active.getId());
        update.setUnboundAt(LocalDateTime.now());
        userOAuthMapper.updateById(update);
    }

    @Override
    public UserOAuthDO findActive(String provider, String oauthUid) {
        return userOAuthMapper.selectActiveByProviderAndUid(provider, oauthUid);
    }

    @Override
    public List<UserOAuthDO> listActiveByUser(Long userId) {
        return userOAuthMapper.selectActiveByUserId(userId);
    }
}
