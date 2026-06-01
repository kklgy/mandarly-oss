package com.mandarly.boot.framework.dict.config;

import com.mandarly.boot.framework.common.biz.system.dict.DictDataCommonApi;
import com.mandarly.boot.framework.dict.core.DictFrameworkUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MandarlyDictAutoConfiguration {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictUtils(DictDataCommonApi dictDataApi) {
        DictFrameworkUtils.init(dictDataApi);
        return new DictFrameworkUtils();
    }

}
