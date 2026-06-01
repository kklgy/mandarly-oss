package com.mandarly.boot.module.edu.controller.admin.levelcheck.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 答卷分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class LevelCheckSubmissionPageReqVO extends PageParam {

    @Schema(description = "语言")
    private String locale;

    @Schema(description = "推断等级")
    private String inferredLevel;

    @Schema(description = "是否已转化下单")
    private Boolean isConverted;

    @Schema(description = "答卷时间起点")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createTimeFrom;

    @Schema(description = "答卷时间终点")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createTimeTo;
}
