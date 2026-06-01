package com.mandarly.boot.module.edu.controller.admin.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "学生持有套餐 Response VO")
@Data
public class StudentPackageRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "学生 user.id")
    private Long studentId;

    @Schema(description = "套餐定义 id")
    private Long packageId;

    @Schema(description = "剩余课次")
    private Integer remaining;

    @Schema(description = "有效期截止(UTC)")
    private LocalDateTime expireAt;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "支付 id(可空)")
    private Long paymentId;

    @Schema(description = "管理员发放时操作人")
    private Long grantedBy;

    @Schema(description = "管理员发放备注")
    private String grantedReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
