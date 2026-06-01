package com.mandarly.boot.module.edu.controller.app.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "App - 教师评价聚合统计(T6)")
@Data
public class AppTeacherReviewStatRespVO {

    @Schema(description = "平均评分,2 位小数;无评价时 0")
    private BigDecimal avgRating;

    @Schema(description = "可见评价数(is_visible=1)")
    private Long reviewCount;

    @Schema(description = "完成订单数(course_order.status=finished)")
    private Long finishedOrderCount;

}
