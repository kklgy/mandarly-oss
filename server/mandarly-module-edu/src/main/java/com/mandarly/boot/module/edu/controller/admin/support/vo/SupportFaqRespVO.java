package com.mandarly.boot.module.edu.controller.admin.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 客服 FAQ Response VO")
@Data
public class SupportFaqRespVO {

    private Long id;
    private String category;
    private String locale;
    private String question;
    private String answer;
    private List<String> keywords;
    private Integer sort;
    private String status;
    private Long viewCount;
    private Long matchCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
