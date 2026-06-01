package com.mandarly.boot.module.edu.controller.admin.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 未命中问题 Top N Response VO")
@Data
public class SupportTopUnmatchedRespVO {

    private String questionText;
    private Long count;
    private LocalDateTime lastAskedAt;
}
