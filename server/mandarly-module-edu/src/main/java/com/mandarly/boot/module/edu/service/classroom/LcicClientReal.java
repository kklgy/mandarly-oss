package com.mandarly.boot.module.edu.service.classroom;

import com.mandarly.boot.module.edu.framework.classroom.config.MandarlyLcicProperties;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicCreateRoomRequest;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicJoinInfo;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicRoom;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.lcic.v20220817.models.CreateRoomRequest;
import com.tencentcloudapi.lcic.v20220817.models.CreateRoomResponse;
import com.tencentcloudapi.lcic.v20220817.models.LoginOriginIdRequest;
import com.tencentcloudapi.lcic.v20220817.models.LoginOriginIdResponse;
import com.tencentcloudapi.lcic.v20220817.models.RegisterUserRequest;
import com.tencentcloudapi.lcic.v20220817.models.RegisterUserResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 腾讯云 LCIC 真模式客户端。
 *
 * <p>对应 PRD-v4 §4.6。SDK = tencentcloud-sdk-java v3.1.964 lcic v20220817。
 *
 * <p>流程:
 * <ol>
 *   <li>{@link #createRoom} → SDK CreateRoom,落 RoomId(数值)字符串化为 lcicClassId</li>
 *   <li>{@link #generateJoinInfo} → 优先 LoginOriginId(originId="mandarly-{userId}"),
 *       账号未注册时降级 RegisterUser;拿到的 token 7 天有效(LCIC 文档),
 *       但我们仍按 properties.tokenTtlSeconds 短期暴露给前端,避免 token 长期外露</li>
 *   <li>join URL pattern:{@code https://class.qcloudclass.com/?sdkAppId=X&classId=Y&userId=Z&token=T}</li>
 * </ol>
 *
 * <p>endpoint 默认 mainland(`lcic.tencentcloudapi.com`)— 我们 liu002 子账号在腾讯云国内主账号下;
 *    如未来改用 intl 账号,setEndpoint("lcic.intl.tencentcloudapi.com")。
 */
@Service
@ConditionalOnProperty(name = "mandarly.lcic.mode", havingValue = "real")
@Slf4j
public class LcicClientReal implements LcicClient {

    /** 1v1 课堂分辨率:2 = HD */
    private static final long DEFAULT_RESOLUTION = 2L;

    /** 1v1 课堂麦位数(教师自动有,学生 1 个)*/
    private static final long DEFAULT_MAX_MIC_NUMBER = 1L;

    /** 房间类型:videodoc 支持白板 + 视频,适合语言课;另一选项 video 纯音视频 */
    private static final String DEFAULT_SUB_TYPE = "videodoc";

    /** Mandarly 用户在 LCIC 侧的 originId 前缀,与本地 user.id 一一映射 */
    private static final String ORIGIN_ID_PREFIX = "mandarly-";

    /** LCIC 课堂入场页域名(腾讯云固定)*/
    private static final String CLASSROOM_HOST = "https://class.qcloudclass.com";

    @Resource
    private MandarlyLcicProperties properties;

    private com.tencentcloudapi.lcic.v20220817.LcicClient client;

    @PostConstruct
    public void init() {
        if (properties.getSdkAppId() == null || properties.getSdkAppId() <= 0) {
            throw new IllegalStateException("mandarly.lcic.mode=real 但 sdk-app-id 未配置");
        }
        if (StringUtils.isBlank(properties.getSecretId())) {
            throw new IllegalStateException("mandarly.lcic.mode=real 但 secret-id 未配置(TENCENT_SECRET_ID 环境变量)");
        }
        if (StringUtils.isBlank(properties.getSecretKey())) {
            throw new IllegalStateException("mandarly.lcic.mode=real 但 secret-key 未配置(TENCENT_SECRET_KEY 环境变量)");
        }
        if (StringUtils.isBlank(properties.getCallbackKey())) {
            throw new IllegalStateException("mandarly.lcic.mode=real 但 callback-key 未配置(LCIC_CALLBACK_KEY 环境变量)");
        }
        Credential cred = new Credential(properties.getSecretId(), properties.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("lcic.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        this.client = new com.tencentcloudapi.lcic.v20220817.LcicClient(cred, "", clientProfile);
        log.info("[LcicClientReal] initialized, sdkAppId={}", properties.getSdkAppId());
    }

    @Override
    public LcicRoom createRoom(LcicCreateRoomRequest request) {
        try {
            CreateRoomRequest req = new CreateRoomRequest();
            req.setSdkAppId(properties.getSdkAppId().longValue());
            req.setName("mandarly-order-" + request.getOrderId());
            // LCIC 要求 UNIX 秒
            long startSec = request.getScheduledAt().toEpochSecond(ZoneOffset.UTC);
            int durationMinutes = request.getDurationMinutes() != null ? request.getDurationMinutes() : 30;
            long endSec = startSec + durationMinutes * 60L;
            req.setStartTime(startSec);
            req.setEndTime(endSec);
            req.setResolution(DEFAULT_RESOLUTION);
            req.setMaxMicNumber(DEFAULT_MAX_MIC_NUMBER);
            req.setSubType(DEFAULT_SUB_TYPE);
            // teacherId 必须先在 LCIC 注册过,否则 LCIC 会报错;
            // 这里先确保教师已注册(LoginOriginId 失败时 RegisterUser),拿到 LCIC 内部 UserId 作为 teacherId
            String lcicTeacherUserId = ensureLcicUser(request.getTeacherId(), request.getTeacherName());
            req.setTeacherId(lcicTeacherUserId);

            CreateRoomResponse resp = client.CreateRoom(req);
            String classId = String.valueOf(resp.getRoomId());
            String roomUrl = buildJoinUrlBase(classId);
            log.info("[LcicClientReal.createRoom] orderId={} → classId={} requestId={}",
                    request.getOrderId(), classId, resp.getRequestId());
            return LcicRoom.builder()
                    .lcicClassId(classId)
                    .lcicRoomUrl(roomUrl)
                    .build();
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("LCIC CreateRoom 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public LcicJoinInfo generateJoinInfo(String lcicClassId, Long userId, String role) {
        try {
            // LoginOriginId / RegisterUser 返回 LCIC 内部 UserId + Token,URL 里 userid 用前者(非我们的 originId)
            LcicUserCred cred = ensureLcicUserCred(userId, null);
            // LCIC join URL 文档形态:
            // https://class.qcloudclass.com/latest/class.html?userid=X&token=Y&classid=Z
            String joinUrl = CLASSROOM_HOST + "/latest/class.html?userid=" + cred.userId
                    + "&token=" + cred.token
                    + "&classid=" + lcicClassId;
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(properties.getTokenTtlSeconds());
            log.info("[LcicClientReal.generateJoinInfo] classId={} mandarlyUserId={} lcicUserId={} role={}",
                    lcicClassId, userId, cred.userId, role);
            return LcicJoinInfo.builder()
                    .joinUrl(joinUrl)
                    .token(cred.token)
                    .expiresAt(expiresAt)
                    .role(role)
                    .build();
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("LCIC 生成 join token 失败: " + e.getMessage(), e);
        }
    }

    /** LCIC 用户登录 / 注册返回的内部 UserId + Token 配对 */
    private record LcicUserCred(String userId, String token) {}

    /**
     * 确保 LCIC 侧已存在该 user(originId = "mandarly-{userId}"),返回 LCIC 内部 UserId(供 CreateRoom 的 teacherId 使用)。
     *
     * <p>策略:LoginOriginId 优先;失败(用户不存在)则 RegisterUser。
     */
    private String ensureLcicUser(Long userId, String displayName) throws TencentCloudSDKException {
        String originId = originIdFor(userId);
        try {
            LoginOriginIdRequest req = new LoginOriginIdRequest();
            req.setSdkAppId(properties.getSdkAppId().longValue());
            req.setOriginId(originId);
            LoginOriginIdResponse resp = client.LoginOriginId(req);
            return resp.getUserId();
        } catch (TencentCloudSDKException e) {
            log.info("[ensureLcicUser] LoginOriginId 失败,降级 RegisterUser originId={} err={}", originId, e.getMessage());
            RegisterUserRequest req = new RegisterUserRequest();
            req.setSdkAppId(properties.getSdkAppId().longValue());
            req.setOriginId(originId);
            if (StringUtils.isNotBlank(displayName)) {
                req.setName(displayName);
            }
            RegisterUserResponse resp = client.RegisterUser(req);
            return resp.getUserId();
        }
    }

    /**
     * 同 {@link #ensureLcicUser},但同时返回 LCIC UserId + Token,供 join URL 拼接用。
     */
    private LcicUserCred ensureLcicUserCred(Long userId, String displayName) throws TencentCloudSDKException {
        String originId = originIdFor(userId);
        try {
            LoginOriginIdRequest req = new LoginOriginIdRequest();
            req.setSdkAppId(properties.getSdkAppId().longValue());
            req.setOriginId(originId);
            LoginOriginIdResponse resp = client.LoginOriginId(req);
            return new LcicUserCred(resp.getUserId(), resp.getToken());
        } catch (TencentCloudSDKException e) {
            log.info("[ensureLcicUserCred] LoginOriginId 失败,降级 RegisterUser originId={} err={}",
                    originId, e.getMessage());
            RegisterUserRequest req = new RegisterUserRequest();
            req.setSdkAppId(properties.getSdkAppId().longValue());
            req.setOriginId(originId);
            if (StringUtils.isNotBlank(displayName)) {
                req.setName(displayName);
            }
            RegisterUserResponse resp = client.RegisterUser(req);
            return new LcicUserCred(resp.getUserId(), resp.getToken());
        }
    }

    private String originIdFor(Long userId) {
        return ORIGIN_ID_PREFIX + userId;
    }

    /** 通用房间 URL(不含 token / userid),供 meeting.lcic_room_url 引用 */
    private String buildJoinUrlBase(String classId) {
        return CLASSROOM_HOST + "/latest/class.html?classid=" + classId;
    }

}
