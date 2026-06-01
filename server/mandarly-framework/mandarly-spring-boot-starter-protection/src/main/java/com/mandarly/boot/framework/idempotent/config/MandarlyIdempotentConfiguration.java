package com.mandarly.boot.framework.idempotent.config;

import com.mandarly.boot.framework.idempotent.core.aop.IdempotentAspect;
import com.mandarly.boot.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import com.mandarly.boot.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import com.mandarly.boot.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.mandarly.boot.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;
import com.mandarly.boot.framework.idempotent.core.redis.IdempotentRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import com.mandarly.boot.framework.redis.config.MandarlyRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = MandarlyRedisAutoConfiguration.class)
public class MandarlyIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public UserIdempotentKeyResolver userIdempotentKeyResolver() {
        return new UserIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
