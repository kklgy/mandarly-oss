package com.mandarly.boot.module.edu.framework.classroom.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LCIC 视频课堂模块配置入口
 *
 * <p>LcicClient bean 选择(mock vs real)在各 Impl 类上用 @ConditionalOnProperty 控制,
 *    不在此处装配。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MandarlyLcicProperties.class)
public class LcicConfiguration {
}
