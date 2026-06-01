package com.mandarly.boot.module.edu.controller.webhook.stripe;

import com.mandarly.boot.module.edu.service.webhook.StripeWebhookHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.STRIPE_WEBHOOK_SIG_INVALID;

/**
 * Stripe Webhook 回调入口(server-to-server,无用户 session)
 *
 * <p>签名失败 → 401(唯一非 200 情况)
 * <p>业务异常 → 200 + stripe_event.result=failed 监控
 * <p>对应 PRD-v4 §4.2 webhook 两段式事务规范
 */
@Tag(name = "Stripe Webhook 回调")
@RestController
@RequestMapping("/edu/webhook/stripe")
@Slf4j
public class StripeWebhookController {

    private final StripeWebhookHandler handler;

    public StripeWebhookController(StripeWebhookHandler handler) {
        this.handler = handler;
    }

    /**
     * Stripe webhook 主入口。
     *
     * <ul>
     *   <li>签名失败(STRIPE_WEBHOOK_SIG_INVALID) → 401,其余始终 200</li>
     *   <li>业务失败已在 Handler 段三标 result=failed,不影响 Stripe 重发判断</li>
     * </ul>
     */
    @PostMapping
    @PermitAll
    @Operation(summary = "Stripe webhook 回调入口")
    public ResponseEntity<String> handle(HttpServletRequest request,
                                         @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        String payload;
        try {
            payload = readBody(request);
        } catch (IOException e) {
            log.error("[handle] failed to read request body", e);
            return ResponseEntity.ok("ok");
        }

        try {
            handler.process(payload, sigHeader);
            return ResponseEntity.ok("ok");
        } catch (com.mandarly.boot.framework.common.exception.ServiceException e) {
            if (e.getCode() == STRIPE_WEBHOOK_SIG_INVALID.getCode()) {
                log.warn("[handle] invalid Stripe signature, returning 401");
                return ResponseEntity.status(401).body("invalid signature");
            }
            // 其他业务异常:已在 Handler markEventFailed,返回 200 防 Stripe 重发
            log.error("[handle] business error, stripe_event.result=failed, returning 200", e);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            log.error("[handle] unexpected error, returning 200", e);
            return ResponseEntity.ok("ok");
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        try (BufferedReader r = req.getReader()) {
            return r.lines().collect(Collectors.joining("\n"));
        }
    }
}
