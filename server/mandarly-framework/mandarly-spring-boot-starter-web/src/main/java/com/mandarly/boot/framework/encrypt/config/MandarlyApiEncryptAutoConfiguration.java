package com.mandarly.boot.framework.encrypt.config;

import com.mandarly.boot.framework.common.enums.WebFilterOrderEnum;
import com.mandarly.boot.framework.encrypt.core.filter.ApiEncryptFilter;
import com.mandarly.boot.framework.web.config.WebProperties;
import com.mandarly.boot.framework.web.core.handler.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static com.mandarly.boot.framework.web.config.MandarlyWebAutoConfiguration.createFilterBean;

@AutoConfiguration
@Slf4j
@EnableConfigurationProperties(ApiEncryptProperties.class)
@ConditionalOnProperty(prefix = "mandarly.api-encrypt", name = "enable", havingValue = "true")
public class MandarlyApiEncryptAutoConfiguration {

    @Bean
    public FilterRegistrationBean<ApiEncryptFilter> apiEncryptFilter(WebProperties webProperties,
                                                                     ApiEncryptProperties apiEncryptProperties,
                                                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                                                     GlobalExceptionHandler globalExceptionHandler) {
        ApiEncryptFilter filter = new ApiEncryptFilter(webProperties, apiEncryptProperties,
                requestMappingHandlerMapping, globalExceptionHandler);
        return createFilterBean(filter, WebFilterOrderEnum.API_ENCRYPT_FILTER);

    }

}
