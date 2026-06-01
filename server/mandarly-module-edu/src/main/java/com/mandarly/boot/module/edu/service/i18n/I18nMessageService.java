package com.mandarly.boot.module.edu.service.i18n;

import java.util.Collection;
import java.util.Map;

/**
 * 多语言文案查询 Service
 *
 * <p>所有 i18n_code → text 翻译统一走这里;找不到 locale 时 fallback 'en',再找不到返回 null
 */
public interface I18nMessageService {

    /**
     * 单条翻译,locale 未命中时 fallback 'en'
     */
    String translate(String code, String locale);

    /**
     * 批量翻译,推荐套餐列表等场景批量查 i18n_message,降低 N+1
     */
    Map<String, String> translateMap(Collection<String> codes, String locale);

}
