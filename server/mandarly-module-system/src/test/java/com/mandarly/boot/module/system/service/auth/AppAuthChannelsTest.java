package com.mandarly.boot.module.system.service.auth;

import com.mandarly.boot.module.system.controller.app.auth.vo.AppAuthChannelsRespVO;
import com.mandarly.boot.module.system.dal.dataobject.sms.SmsChannelDO;
import com.mandarly.boot.module.system.dal.mysql.sms.SmsChannelMapper;
import com.mandarly.boot.module.system.framework.auth.config.SmtpAvailabilityCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * M2.5 §6.5:4 渠道功能 toggle 由 .env 控制(凭证不全 disabled)
 *
 * 测试 AppAuthServiceImpl.getAvailableChannels 与 smsChannelAvailable 的探测逻辑。
 */
@ExtendWith(MockitoExtension.class)
class AppAuthChannelsTest {

    @Mock private SmtpAvailabilityCheck smtp;
    @Mock private SocialClient socialClient;
    @Mock private SmsChannelMapper smsChannelMapper;

    @InjectMocks private AppAuthServiceImpl service;

    private static SmsChannelDO chan(int status, String apiKey) {
        SmsChannelDO c = new SmsChannelDO();
        c.setStatus(status);
        c.setApiKey(apiKey);
        return c;
    }

    @Test
    void getChannels_allEnabled() {
        when(smtp.isAvailable()).thenReturn(true);
        when(socialClient.isProviderConfigured("google")).thenReturn(true);
        when(socialClient.isProviderConfigured("apple")).thenReturn(true);
        when(smsChannelMapper.selectList(any())).thenReturn(List.of(
                chan(0, "OSS_TENCENT_SECRET_ID_A 1400000000")));

        AppAuthChannelsRespVO vo = service.getAvailableChannels();
        assertThat(vo.getEmail()).isTrue();
        assertThat(vo.getSms()).isTrue();
        assertThat(vo.getGoogle()).isTrue();
        assertThat(vo.getApple()).isTrue();
    }

    @Test
    void getChannels_allDisabled_credentialsAllPending() {
        when(smtp.isAvailable()).thenReturn(false);
        when(socialClient.isProviderConfigured("google")).thenReturn(false);
        when(socialClient.isProviderConfigured("apple")).thenReturn(false);
        when(smsChannelMapper.selectList(any())).thenReturn(List.of());

        AppAuthChannelsRespVO vo = service.getAvailableChannels();
        assertThat(vo.getEmail()).isFalse();
        assertThat(vo.getSms()).isFalse();
        assertThat(vo.getGoogle()).isFalse();
        assertThat(vo.getApple()).isFalse();
    }

    @Test
    void getChannels_smsOnly_phoneRealCredentialsLanded() {
        // β 真链路状态:SMTP / Google / Apple 仍空,SMS 已就位
        when(smtp.isAvailable()).thenReturn(false);
        when(socialClient.isProviderConfigured("google")).thenReturn(false);
        when(socialClient.isProviderConfigured("apple")).thenReturn(false);
        when(smsChannelMapper.selectList(any())).thenReturn(List.of(
                chan(0, "OSS_TENCENT_SECRET_ID_B 1400000000")));

        AppAuthChannelsRespVO vo = service.getAvailableChannels();
        assertThat(vo.getEmail()).isFalse();
        assertThat(vo.getSms()).isTrue();
        assertThat(vo.getGoogle()).isFalse();
        assertThat(vo.getApple()).isFalse();
    }

    @Test
    void smsChannelAvailable_pendingApiKey_isFalse() {
        when(smsChannelMapper.selectList(any())).thenReturn(List.of(
                chan(0, "PENDING_SECRETID PENDING_SDKAPPID")));
        assertThat(service.smsChannelAvailable()).isFalse();
    }

    @Test
    void smsChannelAvailable_disabledStatus_isFiltered() {
        // mapper 已按 status=0 过滤,无返回
        when(smsChannelMapper.selectList(any())).thenReturn(List.of());
        assertThat(service.smsChannelAvailable()).isFalse();
    }

    @Test
    void smsChannelAvailable_blankApiKey_isFalse() {
        when(smsChannelMapper.selectList(any())).thenReturn(List.of(
                chan(0, "")));
        assertThat(service.smsChannelAvailable()).isFalse();
    }

    @Test
    void smsChannelAvailable_mixedChannels_anyValidWins() {
        // 多 channel 时任一就绪即返 true(国际通道占位 + 国内通道真凭证 → true)
        when(smsChannelMapper.selectList(any())).thenReturn(List.of(
                chan(0, "PENDING_SECRETID PENDING_SDKAPPID"),
                chan(0, "OSS_TENCENT_SECRET_ID_B 1400000000")));
        assertThat(service.smsChannelAvailable()).isTrue();
    }

    @Test
    void getChannels_emailOnly() {
        // γ 真链路前提:SMTP 凭证就位但 SMS / Google / Apple 全无
        when(smtp.isAvailable()).thenReturn(true);
        when(socialClient.isProviderConfigured("google")).thenReturn(false);
        when(socialClient.isProviderConfigured("apple")).thenReturn(false);
        when(smsChannelMapper.selectList(any())).thenReturn(List.of(
                chan(0, "PENDING_SECRETID PENDING_SDKAPPID")));

        AppAuthChannelsRespVO vo = service.getAvailableChannels();
        assertThat(vo.getEmail()).isTrue();
        assertThat(vo.getSms()).isFalse();
        assertThat(vo.getGoogle()).isFalse();
        assertThat(vo.getApple()).isFalse();
    }
}
