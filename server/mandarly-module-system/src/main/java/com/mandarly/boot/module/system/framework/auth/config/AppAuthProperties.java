package com.mandarly.boot.module.system.framework.auth.config;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "mandarly.app-auth")
@Validated
@Data
public class AppAuthProperties {
    @NotNull
    private CodeConfig emailCode = new CodeConfig();
    @NotNull
    private CodeConfig smsCode = new CodeConfig();
    @NotNull
    private SocialConfig social = new SocialConfig();
    @NotNull
    private ReferralConfig referral = new ReferralConfig();
    @NotNull
    private RateLimitConfig rateLimit = new RateLimitConfig();

    @Data public static class CodeConfig {
        @Min(4) @Max(8) private int length = 6;
        @Min(60) private int ttlSeconds = 300;
        @Min(0) private int cooldownSeconds = 60;
        @Min(1) private int dailyLimit = 10;
        private String templateIdRegister;
        private String templateIdLogin;
    }

    @Data public static class SocialConfig {
        private ProviderConfig google = new ProviderConfig();
        private ProviderConfig apple = new ProviderConfig();
    }

    @Data public static class ProviderConfig {
        private String clientId;
        private String clientSecret;
        private String teamId;          // apple
        private String serviceId;       // apple
        private String keyId;           // apple
        private String privateKeyPath;  // apple
        private String redirectUri;
    }

    @Data public static class ReferralConfig {
        @Min(1) private long freeTrialPackageId = 1L;
        @Min(0) private int signupBonusDiscount = 30;
    }

    @Data public static class RateLimitConfig {
        @Min(60) private int ipRegisterWindowSeconds = 600;
        @Min(1) private int ipRegisterMax = 3;
        @Min(1) private int loginFailLockThreshold = 5;
        @Min(60) private int loginFailLockWindowSeconds = 900;
    }
}
