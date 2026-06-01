package com.mandarly.boot.module.edu.framework.stripe.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Stripe 配置属性类
 * 前缀: mandarly.stripe
 */
@ConfigurationProperties(prefix = "mandarly.stripe")
@Validated
@Data
public class StripeProperties {

    /** Stripe Secret Key, test secret key (local/dev) / live secret key (prod);空时所有 Stripe API 调用失败 */
    private String secretKey;

    /**
     * Webhook signing secret, stripe_webhook_secret
     * <p>不允许为空 — 空 secret 时 Stripe SDK 仍会算 HMAC 但永不匹配,所有 webhook 请求被拒;
     * StripeClientImpl.verifyWebhookSignature 入口同时显式 fail-fast,避免运维误判为"签名有效但失败"
     */
    private String webhookSecret;

    /** Publishable Key, test publishable key / live publishable key;前端用 */
    private String publishableKey;

    /** API version (锁定版本防 Stripe upgrade 影响) */
    @NotEmpty
    private String apiVersion = "2025-01-27.acacia";

    /** test mode 标识(影响 success/cancel URL host) */
    private boolean testMode = true;

    /** Session 过期分钟 */
    @Min(30)
    private int sessionExpiresMinutes = 30;

    /** 应用域名(success/cancel URL 根) */
    @NotEmpty
    private String appBaseUrl = "http://localhost:3001";

    /** 同 user+package pending payment 复用窗口秒数(>Stripe API timeout 80s) */
    @Min(60)
    private int pendingReuseWindowSeconds = 90;

    @PostConstruct
    public void validateProdAppBaseUrl() {
        if (!testMode && isLocalhost(appBaseUrl)) {
            throw new IllegalStateException("mandarly.stripe.app-base-url must not be localhost when test-mode=false");
        }
    }

    private boolean isLocalhost(String url) {
        if (url == null) {
            return false;
        }
        String normalized = url.toLowerCase();
        return normalized.contains("localhost")
                || normalized.contains("127.0.0.1")
                || normalized.contains("[::1]");
    }
}
