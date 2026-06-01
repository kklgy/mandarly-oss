package com.mandarly.boot.module.edu.controller.admin.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客服联系方式 Response VO")
@Data
public class SupportContactRespVO {

    private Long id;
    private String market;
    private String channelType;
    private String displayText;
    private String linkUrl;
    private String imageUrl;
    private Integer sort;
    private Boolean isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
