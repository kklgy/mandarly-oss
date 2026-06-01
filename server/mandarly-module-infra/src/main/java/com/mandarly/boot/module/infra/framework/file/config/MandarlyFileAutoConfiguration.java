package com.mandarly.boot.module.infra.framework.file.config;

import com.mandarly.boot.module.infra.framework.file.core.client.FileClientFactory;
import com.mandarly.boot.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class MandarlyFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
