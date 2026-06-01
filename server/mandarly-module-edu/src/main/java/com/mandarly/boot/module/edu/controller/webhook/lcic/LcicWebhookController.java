package com.mandarly.boot.module.edu.controller.webhook.lcic;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.module.edu.framework.classroom.config.MandarlyLcicProperties;
import com.mandarly.boot.module.edu.service.classroom.ClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * 腾讯云 LCIC 回调 webhook(server-to-server,不在 /admin-api 或 /app-api 下,无前缀)。
 *
 * <p>对应 PRD-v4 §4.6 + LCIC 文档 <a href="https://www.tencentcloud.com/document/product/1168/53700">服务回调</a>。
 *
 * <p>回调 body 结构(LCIC 实际格式):
 * <pre>
 * {
 *   "Sign":      "md5(CallbackKey + ExpireTime) 十六进制",
 *   "Timestamp": 1735689600,
 *   "ExpireTime":1735693200,
 *   "SdkAppId":  123456789,
 *   "EventType": "RoomStart" | "RoomEnd" | "MemberJoin" | "MemberQuit" | "RoomExpire" | "RecordFinish" | ...,
 *   "EventData": { "RoomId": 123456, ... }
 * }
 * </pre>
 *
 * <p>支持事件:room_start / room_end → 落 meeting + course_order;其他事件先留 INFO log。
 */
@Tag(name = "LCIC 回调")
@RestController
@RequestMapping("/webhook/lcic")
@Slf4j
public class LcicWebhookController {

    @Resource
    private ClassroomService classroomService;

    @Resource
    private MandarlyLcicProperties lcicProperties;

    @PostMapping
    @PermitAll
    @Operation(summary = "LCIC 服务器回调入口")
    public CommonResult<Boolean> handle(@RequestBody Map<String, Object> body) {
        log.info("[lcic-webhook] received body={}", body);

        // 签名校验:real 模式严格,mock 模式跳过(本地 curl 仿真)
        if ("real".equalsIgnoreCase(lcicProperties.getMode())) {
            if (!verifySignature(body)) {
                log.warn("[lcic-webhook] 签名校验失败,拒绝 body={}", body);
                return success(false);
            }
        } else {
            log.warn("[lcic-webhook] mock 模式,跳过签名校验(仅本地测试)");
        }

        String eventType = pickString(body, "EventType", "event_type", "type");
        if (eventType == null) {
            log.warn("[lcic-webhook] 缺失 EventType,忽略");
            return success(false);
        }
        String classId = extractRoomId(body);
        if (classId == null) {
            log.warn("[lcic-webhook] 无法定位 RoomId,忽略 eventType={}", eventType);
            return success(false);
        }

        switch (eventType.toLowerCase(Locale.ROOT)) {
            case "room_start":
            case "roomstart":
                classroomService.handleRoomStart(classId);
                break;
            case "room_end":
            case "roomend":
                classroomService.handleRoomEnd(classId);
                break;
            default:
                log.info("[lcic-webhook] 暂不处理事件类型 {} classId={}(B2 异常分支阶段补)",
                        eventType, classId);
        }
        return success(true);
    }

    /**
     * RoomId 优先从 {@code EventData.RoomId / RoomId / room_id / ClassId / class_id} 取。
     * LCIC 实际是 {@code EventData.RoomId},兼容 mock 模式 curl 直接放顶层。
     */
    @SuppressWarnings("unchecked")
    private String extractRoomId(Map<String, Object> body) {
        Object eventData = body.get("EventData");
        if (eventData instanceof Map) {
            String fromNested = pickString((Map<String, Object>) eventData,
                    "RoomId", "room_id", "ClassId", "class_id");
            if (fromNested != null) {
                return fromNested;
            }
        }
        return pickString(body, "RoomId", "room_id", "ClassId", "class_id");
    }

    /**
     * LCIC 签名:Sign = MD5(CallbackKey + ExpireTime) 十六进制小写。
     * 校验流程:
     * 1) 取 body.Sign + body.ExpireTime
     * 2) 算 expected = md5(callbackKey + expireTime)
     * 3) Sign 与 expected 一致(忽略大小写)
     * 4) ExpireTime &gt; now (UNIX 秒,容忍 60s 时钟偏差)
     */
    private boolean verifySignature(Map<String, Object> body) {
        String sign = pickString(body, "Sign", "sign");
        String expireTime = pickString(body, "ExpireTime", "expire_time");
        if (sign == null || expireTime == null) {
            log.warn("[lcic-webhook] 缺 Sign 或 ExpireTime: sign={} expireTime={}", sign, expireTime);
            return false;
        }
        // 过期(允许 60s 时钟偏差)
        long expireSec;
        try {
            expireSec = Long.parseLong(expireTime);
        } catch (NumberFormatException e) {
            log.warn("[lcic-webhook] ExpireTime 解析失败: {}", expireTime);
            return false;
        }
        long nowSec = System.currentTimeMillis() / 1000;
        if (expireSec + 60 < nowSec) {
            log.warn("[lcic-webhook] 回调已过期 expireSec={} nowSec={}", expireSec, nowSec);
            return false;
        }
        String expected = md5Hex(lcicProperties.getCallbackKey() + expireTime);
        if (!sign.equalsIgnoreCase(expected)) {
            log.warn("[lcic-webhook] 签名不匹配 expected={} got={}", expected, sign);
            return false;
        }
        return true;
    }

    private static String md5Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }

    private static String pickString(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) {
                return v.toString();
            }
        }
        return null;
    }

}
