package com.mandarly.boot.module.edu.service.mail;

/**
 * M4 payment mail template code helper.
 *
 * <p>Payment mails now use one template code per business event. The user's locale
 * should be passed as a template param if the template body needs locale-aware text.
 */
public final class PaymentMailTemplateHelper {

    public static final String PARAM_LOCALE = "locale";

    private PaymentMailTemplateHelper() {
    }

    public static String pickTemplateCode(String baseCode, String locale) {
        return baseCode;
    }

    public static String normalizeLocale(String locale) {
        return locale == null || locale.isBlank() ? "en" : locale;
    }
}
