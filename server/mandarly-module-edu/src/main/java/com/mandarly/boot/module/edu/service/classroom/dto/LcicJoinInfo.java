package com.mandarly.boot.module.edu.service.classroom.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生 / 教师进入课堂的 join 信息(实时生成,有效期由 mandarly.lcic.token-ttl-seconds 控制)
 */
@Data
@Builder
public class LcicJoinInfo {

    /** 完整 join URL,前端 iframe 直接嵌入 */
    private String joinUrl;

    /** join token(已拼到 joinUrl 的 query,这里冗余返回方便日志 / 排错) */
    private String token;

    /** token 过期时间 */
    private LocalDateTime expiresAt;

    /** 角色:student | teacher */
    private String role;

}
