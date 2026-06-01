package com.mandarly.boot.module.edu.framework.stripe.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripePropertiesTest {

    @Test
    void validateProdAppBaseUrl_testModeAllowsLocalhost() {
        StripeProperties props = new StripeProperties();
        props.setTestMode(true);
        props.setAppBaseUrl("http://localhost:3001");

        assertThatCode(props::validateProdAppBaseUrl).doesNotThrowAnyException();
    }

    @Test
    void validateProdAppBaseUrl_prodRejectsLocalhost() {
        StripeProperties props = new StripeProperties();
        props.setTestMode(false);
        props.setAppBaseUrl("http://localhost:3001");

        assertThatThrownBy(props::validateProdAppBaseUrl)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app-base-url");
    }
}
