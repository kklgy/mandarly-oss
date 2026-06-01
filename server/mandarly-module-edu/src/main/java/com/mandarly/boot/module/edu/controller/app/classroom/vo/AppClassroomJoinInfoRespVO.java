package com.mandarly.boot.module.edu.controller.app.classroom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "App 端 - 进入课堂 join 信息")
@Data
public class AppClassroomJoinInfoRespVO {

    @Schema(description = "完整 join URL,前端 iframe 直接嵌入")
    private String joinUrl;

    @Schema(description = "join token(已拼到 joinUrl,冗余返回方便日志)")
    private String token;

    @Schema(description = "token 过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "角色 student | teacher")
    private String role;

}
