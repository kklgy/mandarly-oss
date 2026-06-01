package com.mandarly.boot.module.edu.controller.app.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户端 - 客服初始化 Response VO")
@Data
public class AppSupportBootstrapRespVO {

    @Schema(description = "开场白")
    private String greeting;

    @Schema(description = "真人客服联系方式")
    private List<AppSupportContactRespVO> contacts;
}
