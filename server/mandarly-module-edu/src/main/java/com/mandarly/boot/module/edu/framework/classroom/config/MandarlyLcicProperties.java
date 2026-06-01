package com.mandarly.boot.module.edu.framework.classroom.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 腾讯云 LCIC(视频课堂)配置
 *
 * <p>对应 PRD-v4 §4.6。mode=mock 时不调腾讯云 API,前端 iframe 走 stub 页;
 *    mode=real 时走 LcicClientReal,需要 secretId / secretKey / callbackKey 全部就位。
 */
@ConfigurationProperties(prefix = "mandarly.lcic")
@Validated
@Data
public class MandarlyLcicProperties {

    /** 工作模式:mock | real */
    @NotBlank(message = "lcic.mode 不能为空(mock | real)")
    private String mode;

    /** LCIC 应用 ID;mock 模式可为 0 */
    private Integer sdkAppId;

    /** 腾讯云子账号 SecretId(real 模式必填) */
    private String secretId;

    /** 腾讯云子账号 SecretKey(real 模式必填) */
    private String secretKey;

    /** LCIC 回调签名 key(real 模式必填,在 LCIC 控制台「回调配置」生成) */
    private String callbackKey;

    /** 学生 / 教师 join token 有效期(秒);PRD §4.6 课前 5 分钟生成,默认 300 */
    @NotNull(message = "lcic.token-ttl-seconds 不能为空")
    private Integer tokenTtlSeconds;

}
