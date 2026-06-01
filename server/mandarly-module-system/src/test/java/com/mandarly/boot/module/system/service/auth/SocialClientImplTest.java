package com.mandarly.boot.module.system.service.auth;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.module.system.framework.auth.config.AppAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialClientImplTest {

    @Mock private AppAuthProperties props;
    @Mock private AppAuthProperties.SocialConfig socialConfig;
    @Mock private AppAuthProperties.ProviderConfig googleConfig;
    @Mock private AppAuthProperties.ProviderConfig appleConfig;

    @InjectMocks private SocialClientImpl client;

    @BeforeEach
    void setUp() {
        lenient().when(props.getSocial()).thenReturn(socialConfig);
        lenient().when(socialConfig.getGoogle()).thenReturn(googleConfig);
        lenient().when(socialConfig.getApple()).thenReturn(appleConfig);
    }

    @Test
    void googleNotConfigured_throws1_002_109() {
        when(googleConfig.getClientId()).thenReturn("");
        lenient().when(googleConfig.getClientSecret()).thenReturn("");
        assertThatThrownBy(() -> client.getRedirectUrl("google"))
                .isInstanceOf(ServiceException.class)
                .extracting("code").isEqualTo(1_002_109);
    }

    @Test
    void appleNotConfigured_throws1_002_109() {
        when(appleConfig.getTeamId()).thenReturn("");
        assertThatThrownBy(() -> client.getRedirectUrl("apple"))
                .isInstanceOf(ServiceException.class)
                .extracting("code").isEqualTo(1_002_109);
    }

    @Test
    void unknownProvider_throws1_002_109() {
        assertThatThrownBy(() -> client.getRedirectUrl("wechat"))
                .isInstanceOf(ServiceException.class)
                .extracting("code").isEqualTo(1_002_109);
    }
}
