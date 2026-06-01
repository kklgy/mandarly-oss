package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 水平测试等级分布 Response VO")
@Data
public class LevelCheckLevelStatsRespVO {

    @Schema(description = "推断等级")
    private String inferredLevel;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "占比")
    private BigDecimal rate;

}
