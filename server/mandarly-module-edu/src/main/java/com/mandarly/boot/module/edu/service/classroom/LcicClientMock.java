package com.mandarly.boot.module.edu.service.classroom;

import com.mandarly.boot.module.edu.framework.classroom.config.MandarlyLcicProperties;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicCreateRoomRequest;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicJoinInfo;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicRoom;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * LCIC 客户端 mock 实现:不调腾讯云 API,返回固定 stub URL。
 *
 * <p>用于本地 / 开发环境凭证未就位时,前端 e2e 跑通主路径(下单 → meeting 落库 → join-info → iframe stub 页 → webhook 模拟)。
 *
 * <p>stub join URL:`/classroom-stub?orderId=...&role=...&class=...` 由 app 端 SPA 渲染一个占位页。
 */
@Service
@ConditionalOnProperty(name = "mandarly.lcic.mode", havingValue = "mock")
@Slf4j
public class LcicClientMock implements LcicClient {

    @Resource
    private MandarlyLcicProperties properties;

    @Override
    public LcicRoom createRoom(LcicCreateRoomRequest request) {
        String classId = "mock-" + request.getOrderId();
        String roomUrl = "/classroom-stub?class=" + classId;
        log.info("[LcicClientMock.createRoom] orderId={} → classId={}", request.getOrderId(), classId);
        return LcicRoom.builder()
                .lcicClassId(classId)
                .lcicRoomUrl(roomUrl)
                .build();
    }

    @Override
    public LcicJoinInfo generateJoinInfo(String lcicClassId, Long userId, String role) {
        String token = "mock-" + UUID.randomUUID();
        String joinUrl = "/classroom-stub?class=" + lcicClassId
                + "&role=" + role
                + "&userId=" + userId
                + "&token=" + token;
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(properties.getTokenTtlSeconds());
        log.info("[LcicClientMock.generateJoinInfo] classId={} userId={} role={} ttlSec={}",
                lcicClassId, userId, role, properties.getTokenTtlSeconds());
        return LcicJoinInfo.builder()
                .joinUrl(joinUrl)
                .token(token)
                .expiresAt(expiresAt)
                .role(role)
                .build();
    }

}
