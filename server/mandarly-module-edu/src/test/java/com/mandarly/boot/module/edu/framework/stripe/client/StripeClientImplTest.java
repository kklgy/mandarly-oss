package com.mandarly.boot.module.edu.framework.stripe.client;

import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.framework.stripe.config.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * StripeClientImpl 单元测试
 *
 * <p>覆盖:
 * <ul>
 *   <li>createCheckoutSession:idempotency-key 注入 / metadata 字段</li>
 *   <li>createRefund:idempotency-key + 原支付币种金额(分为单位)</li>
 *   <li>verifyWebhookSignature:合法签名通过 / 非法签名抛 STRIPE_WEBHOOK_SIG_INVALID</li>
 * </ul>
 *
 * <p>注:mockito-inline(已在测试框架中)用于 mock Stripe 静态方法,
 * 代替 WireMock(BOM 未引入)。签名测试使用实际 HMAC-SHA256 计算验证真实 Stripe 签名逻辑。
 */
class StripeClientImplTest extends BaseMockitoUnitTest {

    @Mock
    private StripeProperties props;

    @InjectMocks
    private StripeClientImpl stripeClient;

    @BeforeEach
    void setUp() {
        // lenient: verifyWebhookSignature 不用 props,避免 UnnecessaryStubbingException
        lenient().when(props.getApiVersion()).thenReturn("2025-01-27.acacia");
        lenient().when(props.getSessionExpiresMinutes()).thenReturn(30);
    }

    // ======================== createCheckoutSession ========================

    @Test
    void createCheckoutSession_idempotencyKey_injected() {
        // given
        CreateSessionRequest req = CreateSessionRequest.builder()
                .paymentId(123L)
                .userId(1L)
                .packageId(10L)
                .packageNameI18n("中文口语 4 次课")
                .unitAmount(new BigDecimal("199.00"))
                .currency("USD")
                .successUrl("http://localhost:3001/payment/success")
                .cancelUrl("http://localhost:3001/payment/cancel")
                .expiresInMinutes(30)
                .build();

        Session stubSession = new Session();
        stubSession.setId("cs_test_123");
        stubSession.setUrl("https://checkout.stripe.com/test");

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class), any(RequestOptions.class)))
                    .thenReturn(stubSession);

            // when
            Session result = stripeClient.createCheckoutSession(req);

            // then: session 返回正确
            assertThat(result.getId()).isEqualTo("cs_test_123");

            // verify idempotency-key = "payment-123"
            sessionMock.verify(() -> Session.create(
                    any(SessionCreateParams.class),
                    argThat(opts -> "payment-123".equals(opts.getIdempotencyKey()))
            ));
        }
    }

    @Test
    void createCheckoutSession_metadata_containsPaymentId() {
        // given
        CreateSessionRequest req = CreateSessionRequest.builder()
                .paymentId(456L)
                .userId(2L)
                .packageId(20L)
                .packageNameI18n("Test Package")
                .unitAmount(new BigDecimal("99.00"))
                .currency("USD")
                .successUrl("https://example.com/ok")
                .cancelUrl("https://example.com/cancel")
                .expiresInMinutes(30)
                .build();

        Session stubSession = new Session();
        stubSession.setId("cs_test_456");

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            // capture the params to inspect metadata
            final SessionCreateParams[] captured = new SessionCreateParams[1];
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class), any(RequestOptions.class)))
                    .thenAnswer(inv -> {
                        captured[0] = inv.getArgument(0);
                        return stubSession;
                    });

            // when
            stripeClient.createCheckoutSession(req);

            // then: metadata 包含 payment_id / user_id / package_id
            assertThat(captured[0]).isNotNull();
            var metadata = captured[0].getMetadata();
            assertThat(metadata).containsEntry("payment_id", "456");
            assertThat(metadata).containsEntry("user_id", "2");
            assertThat(metadata).containsEntry("package_id", "20");
        }
    }

    @Test
    void createCheckoutSession_usesRequestCurrencyAndAmount() {
        // given
        CreateSessionRequest req = CreateSessionRequest.builder()
                .paymentId(789L)
                .userId(3L)
                .packageId(30L)
                .packageNameI18n("单节课")
                .unitAmount(new BigDecimal("60.00"))
                .currency("HKD")
                .successUrl("https://example.com/ok")
                .cancelUrl("https://example.com/cancel")
                .expiresInMinutes(30)
                .build();

        Session stubSession = new Session();
        stubSession.setId("cs_test_hkd");

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            final SessionCreateParams[] captured = new SessionCreateParams[1];
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class), any(RequestOptions.class)))
                    .thenAnswer(inv -> {
                        captured[0] = inv.getArgument(0);
                        return stubSession;
                    });

            // when
            stripeClient.createCheckoutSession(req);

            // then
            var priceData = captured[0].getLineItems().get(0).getPriceData();
            assertThat(priceData.getCurrency()).isEqualTo("hkd");
            assertThat(priceData.getUnitAmount()).isEqualTo(6000L);
        }
    }

    // ======================== createRefund ========================

    @Test
    void createRefund_idempotencyKey_andAmountInCents() {
        // given: HKD 50.00 → 5000 分
        CreateRefundRequest req = CreateRefundRequest.builder()
                .refundId(789L)
                .paymentIntentId("pi_test_abc")
                .amount(new BigDecimal("50.00"))
                .currency("hkd")
                .build();

        Refund stubRefund = new Refund();
        stubRefund.setId("re_test_789");
        stubRefund.setAmount(5000L);
        stubRefund.setCurrency("hkd");

        try (MockedStatic<Refund> refundMock = mockStatic(Refund.class)) {
            final RefundCreateParams[] captured = new RefundCreateParams[1];
            refundMock.when(() -> Refund.create(any(RefundCreateParams.class), any(RequestOptions.class)))
                    .thenAnswer(inv -> {
                        captured[0] = inv.getArgument(0);
                        return stubRefund;
                    });

            // when
            Refund result = stripeClient.createRefund(req);

            // then: 返回正确
            assertThat(result.getId()).isEqualTo("re_test_789");

            // verify: amount = 5000 (分), idempotency-key = "refund-789"
            assertThat(captured[0].getAmount()).isEqualTo(5000L);
            assertThat(captured[0].getPaymentIntent()).isEqualTo("pi_test_abc");

            refundMock.verify(() -> Refund.create(
                    any(RefundCreateParams.class),
                    argThat(opts -> "refund-789".equals(opts.getIdempotencyKey()))
            ));
        }
    }

    // ======================== verifyWebhookSignature ========================

    @Test
    void verifyWebhookSignature_validSig_returnsEvent() throws Exception {
        // given: mock Webhook.constructEvent 返回 stub event
        String payload = "{\"id\":\"evt_test\",\"object\":\"event\"}";
        String secret = "stripe_webhook_secret_test";
        long timestamp = Instant.now().getEpochSecond();
        String sig = computeStripeSignature(timestamp, payload, secret);
        String sigHeader = "t=" + timestamp + ",v1=" + sig;

        Event stubEvent = new Event();
        stubEvent.setId("evt_test");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(payload, sigHeader, secret))
                    .thenReturn(stubEvent);

            // when
            Event result = stripeClient.verifyWebhookSignature(payload, sigHeader, secret);

            // then
            assertThat(result.getId()).isEqualTo("evt_test");
        }
    }

    @Test
    void verifyWebhookSignature_invalidSig_throwsStripeWebhookSigInvalid() throws Exception {
        // given
        String payload = "{\"id\":\"evt_invalid\"}";
        String badSigHeader = "t=1234,v1=bad_signature_value";
        String secret = "stripe_webhook_secret_real";

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(payload, badSigHeader, secret))
                    .thenThrow(new SignatureVerificationException("sig mismatch", badSigHeader));

            // when & then: 应抛出 ServiceException(code=1_004_401 STRIPE_WEBHOOK_SIG_INVALID)
            assertThatThrownBy(() -> stripeClient.verifyWebhookSignature(payload, badSigHeader, secret))
                    .isInstanceOf(com.mandarly.boot.framework.common.exception.ServiceException.class)
                    .satisfies(ex -> {
                        var se = (com.mandarly.boot.framework.common.exception.ServiceException) ex;
                        assertThat(se.getCode()).isEqualTo(1_004_401);
                    });
        }
    }

    // ======================== helper ========================

    /**
     * 手工计算 Stripe webhook 签名(用于测试)
     * Stripe 签名算法:HMAC-SHA256("${timestamp}.${payload}", secret)
     */
    private static String computeStripeSignature(long timestamp, String payload, String secret)
            throws Exception {
        String signedPayload = timestamp + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
