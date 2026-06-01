package com.mandarly.boot.module.edu.service.mail;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMailTemplateHelperTest {

    @Test
    void pickTemplateCode_collapsesLocaleVariantsToBaseCode() {
        assertThat(PaymentMailTemplateHelper.pickTemplateCode("mandarly_payment_success", "zh-CN"))
                .isEqualTo("mandarly_payment_success");
        assertThat(PaymentMailTemplateHelper.pickTemplateCode("mandarly_payment_success", "ar"))
                .isEqualTo("mandarly_payment_success");
    }

    @Test
    void normalizeLocale_defaultsBlankToEnglish() {
        assertThat(PaymentMailTemplateHelper.normalizeLocale(null)).isEqualTo("en");
        assertThat(PaymentMailTemplateHelper.normalizeLocale("")).isEqualTo("en");
        assertThat(PaymentMailTemplateHelper.normalizeLocale("zh-TW")).isEqualTo("zh-TW");
    }
}
