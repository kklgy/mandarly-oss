package com.mandarly.boot.module.system.service.user;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseDbUnitTest;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import com.mandarly.boot.module.system.dal.mysql.user.AppUserMapper;
import jakarta.annotation.Resource;
import com.mandarly.boot.module.system.dal.dataobject.user.UserOAuthDO;
import com.mandarly.boot.module.system.service.user.UserOAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({UserServiceImpl.class, UserOAuthServiceImpl.class})
class UserServiceImplTest extends BaseDbUnitTest {

    @Resource private UserService userService;
    @Resource private UserOAuthService userOAuthService;
    @Resource private AppUserMapper userMapper;

    @Test
    void testGenerateUniqueReferralCode_format() {
        String code = userService.generateUniqueReferralCode();
        assertThat(code).matches("^MAND[A-HJ-NP-Z2-9]{4}$");
    }

    @Test
    void testGenerateUniqueReferralCode_collisionAvoidance() {
        AppUserDO existing = new AppUserDO();
        existing.setRole("student");
        existing.setEmail("a@b.c");
        existing.setReferralCode("MANDXXXX");
        existing.setStatus("active");
        userMapper.insert(existing);
        for (int i = 0; i < 100; i++) {
            String code = userService.generateUniqueReferralCode();
            assertThat(code).isNotEqualTo("MANDXXXX");
        }
    }

    /**
     * M2.5 §6.5 上线 checklist:推荐码生成防撞测过 1k 次 0 冲突。
     *
     * 算法:MAND + 4 位 (32-字母表 = 32^4 ≈ 1M 空间);
     * 1000 次 birthday 概率 ≈ 39% 至少 1 次随机碰撞,5 次重试 + DB 唯一查询兜底 → 最终 0 冲突。
     * 本测试每轮真插库,下一轮 generateUnique* 会查到前面的并 retry,验证完整链路。
     */
    @Test
    void testGenerateUniqueReferralCode_1k_zeroCollisionInDb() {
        Set<String> issued = new HashSet<>(2048);
        for (int i = 0; i < 1000; i++) {
            String code = userService.generateUniqueReferralCode();
            assertThat(code).matches("^MAND[A-HJ-NP-Z2-9]{4}$");
            assertThat(issued.add(code)).as("collision at i=%d code=%s", i, code).isTrue();

            AppUserDO u = new AppUserDO();
            u.setRole("student");
            u.setEmail("u" + i + "@b.c");
            u.setReferralCode(code);
            u.setStatus("active");
            userMapper.insert(u);
        }
        assertThat(issued).hasSize(1000);
    }

    @Test
    void testCreateUserByEmail_duplicate() {
        userService.createUserByEmail("student", "dup@b.c", "Pass1234", null, "en", "UTC", null);
        assertThatThrownBy(() ->
            userService.createUserByEmail("student", "dup@b.c", "Pass1234", null, "en", "UTC", null))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("已注册");
    }

    @Test
    void testCreateUserByPhone_duplicate() {
        userService.createUserByPhone("student", "+85291234567", null, "en", "UTC", null);
        assertThatThrownBy(() ->
            userService.createUserByPhone("student", "+85291234567", null, "en", "UTC", null))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("已注册");
    }

    @Test
    void testCreateUserByEmail_basicFields() {
        Long id = userService.createUserByEmail("student", "alice@b.c", "Pass1234", null, "en", "UTC", null);
        AppUserDO user = userService.getById(id);
        assertThat(user.getEmail()).isEqualTo("alice@b.c");
        assertThat(user.getStatus()).isEqualTo("active");
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        assertThat(user.getReferralCode()).matches("^MAND[A-HJ-NP-Z2-9]{4}$");
        assertThat(user.getNickname()).isEqualTo("alice");
        assertThat(user.getPasswordHash()).isNotBlank();
    }

    @Test
    void testCreateOrLoginBySocial_newUser_setsBindingAndProfile() {
        Long id = userService.createOrLoginBySocial(
                "google", "g-uuid-1", "social@b.c",
                "{\"name\":\"G\"}",
                "student", "Alice", "https://avatar/url", "en", "UTC");
        AppUserDO user = userService.getById(id);
        assertThat(user.getEmail()).isEqualTo("social@b.c");
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        assertThat(user.getNickname()).isEqualTo("Alice");
        assertThat(user.getAvatarUrl()).isEqualTo("https://avatar/url");
        assertThat(user.getStatus()).isEqualTo("active");
        assertThat(user.getReferralCode()).matches("^MAND[A-HJ-NP-Z2-9]{4}$");
    }

    @Test
    void testCreateOrLoginBySocial_existingBinding_returnsSameUserId() {
        Long firstId = userService.createOrLoginBySocial(
                "google", "g-uuid-2", null, null,
                "student", null, null, "en", "UTC");
        Long secondId = userService.createOrLoginBySocial(
                "google", "g-uuid-2", "later@b.c", null,
                "student", "Bob", null, "en", "UTC");
        assertThat(secondId).isEqualTo(firstId);
    }

    @Test
    void testCreateOrLoginBySocial_existingEmail_bindsAndReturnsExistingUserId() {
        Long emailUserId = userService.createUserByEmail(
                "student", "social-existing@b.c", "Pass1234", "Email User", "en", "UTC", null);

        Long socialUserId = userService.createOrLoginBySocial(
                "google", "g-existing-email", "social-existing@b.c",
                "{\"name\":\"Google User\"}",
                "student", "Google User", "https://avatar/url", "en", "UTC");

        assertThat(socialUserId).isEqualTo(emailUserId);
        UserOAuthDO binding = userOAuthService.findActive("google", "g-existing-email");
        assertThat(binding).isNotNull();
        assertThat(binding.getUserId()).isEqualTo(emailUserId);
        assertThat(binding.getOauthEmail()).isEqualTo("social-existing@b.c");
        assertThat(userMapper.selectList()).hasSize(1);
    }
}
