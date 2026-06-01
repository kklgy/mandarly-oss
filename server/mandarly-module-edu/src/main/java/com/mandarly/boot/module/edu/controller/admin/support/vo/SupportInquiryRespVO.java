package com.mandarly.boot.module.edu.controller.admin.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客服咨询日志 Response VO")
@Data
public class SupportInquiryRespVO {

    private Long id;
    private Long userId;
    private String sessionId;
    private String locale;
    private String market;
    private String questionText;
    private Long matchedFaqId;
    private BigDecimal score;
    private Boolean clickedToHuman;
    private Long clickedContactId;
    private String ip;
    private String userAgent;
    private LocalDateTime createTime;
}
