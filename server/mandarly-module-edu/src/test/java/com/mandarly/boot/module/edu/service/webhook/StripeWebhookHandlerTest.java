package com.mandarly.boot.module.edu.service.webhook;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.framework.stripe.client.StripeClient;
import com.mandarly.boot.module.edu.framework.stripe.config.StripeProperties;
import com.mandarly.boot.module.edu.service.payment.PaymentService;
import com.mandarly.boot.module.edu.service.refund.RefundService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.STRIPE_WEBHOOK_SIG_INVALID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StripeWebhookHandler 单元测试(4 case)
 *
 * <p>覆盖:
 * <ol>
 *   <li>签名无效 → 抛 STRIPE_WEBHOOK_SIG_INVALID</li>
 *   <li>重复事件(INSERT IGNORE 0 affected) → skip business,不调 paymentService</li>
 *   <li>checkout.session.completed → 调 paymentService.handleCheckoutCompleted(Session)</li>
 *   <li>未知事件类型 → markEventIgnored,不调 paymentService/refundService</li>
 * </ol>
 *
 * <p>A-pre-4 修法后:REQUIRES_NEW 方法搬到 {@link StripeEventTxHelper} 独立 Bean,
 * 测试用 @Mock 注入 helper 即可,不需要 @Spy + doReturn 兜底。
 */
class StripeWebhookHandlerTest extends BaseMockitoUnitTest {

    @Mock
    private StripeClient stripeClient;

    @Mock
    private StripeProperties props;

    @Mock
    private StripeEventTxHelper txHelper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RefundService refundService;

    @InjectMocks
    private StripeWebhookHandler handler;

    private static final String FAKE_PAYLOAD = "{\"id\":\"evt_test\"}";
    private static final String FAKE_SIG = "t=123,v1=abc";
    private static final String FAKE_SECRET = "stripe_webhook_secret_test";

    @BeforeEach
    void setUp() {
        when(props.getWebhookSecret()).thenReturn(FAKE_SECRET);
    }

    // ======================== case 1: 签名无效 → 抛异常 ========================

    @Test
    void process_invalidSignature_throwsServiceException() {
        when(stripeClient.verifyWebhookSignature(anyString(), anyString(), anyString()))
                .thenThrow(new ServiceException(STRIPE_WEBHOOK_SIG_INVALID.getCode(), "invalid sig"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> handler.process(FAKE_PAYLOAD, FAKE_SIG))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
                        ((ServiceException) ex).getCode()).isEqualTo(STRIPE_WEBHOOK_SIG_INVALID.getCode()));

        verify(paymentService, never()).handleCheckoutCompleted(any(Session.class));
        verify(refundService, never()).handleChargeRefunded(anyString());
    }

    // ======================== case 2: 重复事件 → skip business ========================

    @Test
    void process_duplicateEvent_skipsBusiness() {
        Event event = buildFakeEvent("evt_dup", "checkout.session.completed");
        when(stripeClient.verifyWebhookSignature(anyString(), anyString(), anyString())).thenReturn(event);
        when(txHelper.persistEventIgnoreDuplicate(event, FAKE_PAYLOAD)).thenReturn(false);

        handler.process(FAKE_PAYLOAD, FAKE_SIG);

        verify(paymentService, never()).handleCheckoutCompleted(any(Session.class));
        verify(paymentService, never()).handleSessionExpired(anyString());
        verify(refundService, never()).handleChargeRefunded(anyString());
        verify(paymentService, never()).handlePaymentFailed(anyString());
    }

    // ======================== case 3: checkout.session.completed → dispatch ========================

    @Test
    void process_checkoutCompleted_dispatchesToPaymentService() {
        String sessionId = "cs_test_abc";
        Event event = buildFakeEvent("evt_checkout", "checkout.session.completed");

        Session session = new Session();
        session.setId(sessionId);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        Event spyEvent = spy(event);
        doReturn(deserializer).when(spyEvent).getDataObjectDeserializer();

        when(stripeClient.verifyWebhookSignature(anyString(), anyString(), anyString())).thenReturn(spyEvent);
        when(txHelper.persistEventIgnoreDuplicate(eq(spyEvent), anyString())).thenReturn(true);

        handler.process(FAKE_PAYLOAD, FAKE_SIG);

        verify(paymentService).handleCheckoutCompleted(session);
    }

    // ======================== case 4: 未知事件类型 → markEventIgnored ========================

    @Test
    void process_unknownEventType_marksIgnoredAndReturnsNormally() {
        Event event = buildFakeEvent("evt_unknown", "some.unknown.event");
        when(stripeClient.verifyWebhookSignature(anyString(), anyString(), anyString())).thenReturn(event);
        when(txHelper.persistEventIgnoreDuplicate(event, FAKE_PAYLOAD)).thenReturn(true);

        handler.process(FAKE_PAYLOAD, FAKE_SIG);

        verify(txHelper).markEventIgnored(event.getId());
        verify(paymentService, never()).handleCheckoutCompleted(any(Session.class));
        verify(paymentService, never()).handleSessionExpired(anyString());
        verify(refundService, never()).handleChargeRefunded(anyString());
        verify(paymentService, never()).handlePaymentFailed(anyString());
    }

    // ======================== helper ========================

    private Event buildFakeEvent(String id, String type) {
        Event event = new Event();
        event.setId(id);
        event.setType(type);
        return event;
    }
}
