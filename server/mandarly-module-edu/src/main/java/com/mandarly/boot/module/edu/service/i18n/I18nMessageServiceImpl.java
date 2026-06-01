package com.mandarly.boot.module.edu.service.i18n;

import com.mandarly.boot.module.edu.dal.dataobject.i18n.I18nMessageDO;
import com.mandarly.boot.module.edu.dal.mysql.i18n.I18nMessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class I18nMessageServiceImpl implements I18nMessageService {

    private static final String DEFAULT_LOCALE = "en";

    @Resource
    private I18nMessageMapper i18nMessageMapper;

    @Override
    @Cacheable(cacheNames = "i18nMessage", key = "#code + '::' + #locale", unless = "#result == null")
    public String translate(String code, String locale) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String effectiveLocale = (locale == null || locale.isBlank()) ? DEFAULT_LOCALE : locale;
        I18nMessageDO hit = i18nMessageMapper.selectByCodeAndLocale(code, effectiveLocale);
        if (hit != null) {
            return hit.getText();
        }
        if (!DEFAULT_LOCALE.equals(effectiveLocale)) {
            I18nMessageDO fallback = i18nMessageMapper.selectByCodeAndLocale(code, DEFAULT_LOCALE);
            if (fallback != null) {
                return fallback.getText();
            }
        }
        return null;
    }

    @Override
    public Map<String, String> translateMap(Collection<String> codes, String locale) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        String effectiveLocale = (locale == null || locale.isBlank()) ? DEFAULT_LOCALE : locale;
        List<I18nMessageDO> all = i18nMessageMapper.selectByCodes(codes);
        if (all.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, I18nMessageDO> primary = new HashMap<>();
        Map<String, I18nMessageDO> fallback = new HashMap<>();
        for (I18nMessageDO row : all) {
            if (effectiveLocale.equals(row.getLocale())) {
                primary.put(row.getCode(), row);
            } else if (DEFAULT_LOCALE.equals(row.getLocale())) {
                fallback.put(row.getCode(), row);
            }
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String code : codes) {
            I18nMessageDO hit = primary.get(code);
            if (hit == null) {
                hit = fallback.get(code);
            }
            if (hit != null) {
                result.put(code, hit.getText());
            }
        }
        return result;
    }

}
