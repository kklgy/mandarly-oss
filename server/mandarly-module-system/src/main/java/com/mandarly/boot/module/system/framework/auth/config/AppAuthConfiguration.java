package com.mandarly.boot.module.system.framework.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppAuthProperties.class)
public class AppAuthConfiguration {}
