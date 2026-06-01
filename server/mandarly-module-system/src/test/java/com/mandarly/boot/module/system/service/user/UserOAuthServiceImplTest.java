package com.mandarly.boot.module.system.service.user;

import com.mandarly.boot.framework.test.core.ut.BaseDbUnitTest;
import com.mandarly.boot.module.system.dal.dataobject.user.UserOAuthDO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import(UserOAuthServiceImpl.class)
class UserOAuthServiceImplTest extends BaseDbUnitTest {

    @Resource
    private UserOAuthService userOAuthService;

    @Test
    void testBindThenFindActive() {
        userOAuthService.bind(100L, "google", "g-uid-1", "g@b.c", Map.of("name", "G"));
        UserOAuthDO active = userOAuthService.findActive("google", "g-uid-1");
        assertThat(active).isNotNull();
        assertThat(active.getUserId()).isEqualTo(100L);
        assertThat(active.getOauthEmail()).isEqualTo("g@b.c");
        assertThat(active.getOauthRaw()).containsEntry("name", "G");
        assertThat(active.getBoundAt()).isNotNull();
        assertThat(active.getUnboundAt()).isNull();
    }

    @Test
    void testUnbindMakesFindActiveReturnNull() {
        userOAuthService.bind(100L, "google", "g-uid-2", "g@b.c", null);
        userOAuthService.unbind(100L, "google", "g-uid-2");
        assertThat(userOAuthService.findActive("google", "g-uid-2")).isNull();
    }

    @Test
    void testRebindAfterUnbindCreatesNewActiveRecord() {
        userOAuthService.bind(100L, "google", "g-uid-3", "g1@b.c", null);
        userOAuthService.unbind(100L, "google", "g-uid-3");
        userOAuthService.bind(100L, "google", "g-uid-3", "g2@b.c", null);
        assertThat(userOAuthService.listActiveByUser(100L)).hasSize(1);
        UserOAuthDO active = userOAuthService.findActive("google", "g-uid-3");
        assertThat(active.getOauthEmail()).isEqualTo("g2@b.c");
    }

    @Test
    void testListActiveByUser_multiProvider() {
        userOAuthService.bind(101L, "google", "g-uid-4", null, null);
        userOAuthService.bind(101L, "apple", "a-uid-1", null, null);
        assertThat(userOAuthService.listActiveByUser(101L)).hasSize(2);
    }

    @Test
    void testUnbind_noopIfWrongUser() {
        userOAuthService.bind(100L, "google", "g-uid-5", null, null);
        // 不属于 200L 的记录,unbind 应该 noop
        userOAuthService.unbind(200L, "google", "g-uid-5");
        assertThat(userOAuthService.findActive("google", "g-uid-5")).isNotNull();
    }

    @Test
    void testUnbindByUserAndProvider_resolvesActiveRowWithoutOauthUid() {
        userOAuthService.bind(100L, "google", "g-uid-6", "g@b.c", null);
        // 不带 oauthUid 直接按 (userId, provider) 解绑
        userOAuthService.unbindByUserAndProvider(100L, "google");
        assertThat(userOAuthService.findActive("google", "g-uid-6")).isNull();
    }

    @Test
    void testUnbindByUserAndProvider_noopIfWrongUser() {
        userOAuthService.bind(100L, "google", "g-uid-7", null, null);
        userOAuthService.unbindByUserAndProvider(200L, "google");
        assertThat(userOAuthService.findActive("google", "g-uid-7")).isNotNull();
    }

    /**
     * Regression:解绑后再用同一 Google 账号登录,bind 直接 insert 会撞
     * uk_provider_uid_deleted 唯一键(deleted=0 仍占 key)。bind 必须复用历史 row。
     */
    @Test
    void testBindReusesHistoricalRowAfterUnbind() {
        userOAuthService.bind(100L, "google", "g-uid-8", "g1@b.c", null);
        Long firstId = userOAuthService.findActive("google", "g-uid-8").getId();
        userOAuthService.unbindByUserAndProvider(100L, "google");
        // 再次 bind 同一 oauth_uid,即使经过解绑,也应该 update 旧 row 而非 insert
        userOAuthService.bind(100L, "google", "g-uid-8", "g2@b.c", null);
        UserOAuthDO active = userOAuthService.findActive("google", "g-uid-8");
        assertThat(active).isNotNull();
        assertThat(active.getId()).isEqualTo(firstId);
        assertThat(active.getOauthEmail()).isEqualTo("g2@b.c");
        assertThat(active.getUnboundAt()).isNull();
    }

    /**
     * Regression:rebind 时 oauth_raw(JSON column with JacksonTypeHandler)
     * 必须正确序列化。UpdateWrapper.set 不走 TypeHandler 会撞
     * "Data truncation: Cannot create a JSON value from a string with CHARACTER SET 'binary'"。
     * 用 update(entity, wrapper) 让 entity 走 TypeHandler 提供 SET 子句。
     */
    @Test
    void testBindRebindPreservesJsonOauthRawSerialization() {
        userOAuthService.bind(100L, "google", "g-uid-9", "g1@b.c", Map.of("k", "v1"));
        userOAuthService.unbindByUserAndProvider(100L, "google");
        userOAuthService.bind(100L, "google", "g-uid-9", "g2@b.c", Map.of("k", "v2", "n", 7));
        UserOAuthDO active = userOAuthService.findActive("google", "g-uid-9");
        assertThat(active.getOauthRaw()).containsEntry("k", "v2");
        assertThat(active.getOauthRaw()).containsKey("n");
    }
}
