package com.mandarly.boot.module.edu.framework.stripe.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe 框架配置
 * 初始化 Stripe API key，绑定 StripeProperties
 */
@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfiguration {

    private final StripeProperties props;

    public StripeConfiguration(StripeProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        if (props.getSecretKey() != null && !props.getSecretKey().isBlank()) {
            Stripe.apiKey = props.getSecretKey();
        }
        // apiVersion 在每次 RequestOptions 时单独传，不用全局静态
    }
}
