package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 水平测试答卷指标 Response VO")
@Data
public class LevelCheckSubmissionStatsRespVO {

    @Schema(description = "答卷总数")
    private Long totalCount;

    @Schema(description = "已转化下单数")
    private Long convertedCount;

    @Schema(description = "未转化下单数")
    private Long unconvertedCount;

    @Schema(description = "下单转化率")
    private BigDecimal conversionRate;

    @Schema(description = "等级分布")
    private List<LevelCheckLevelStatsRespVO> levelStats;

}
