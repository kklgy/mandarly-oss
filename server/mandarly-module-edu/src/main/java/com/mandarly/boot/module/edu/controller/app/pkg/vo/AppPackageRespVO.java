package com.mandarly.boot.module.edu.controller.app.pkg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "用户端 - 公开套餐 Resp VO")
@Data
public class AppPackageRespVO {

    @Schema(description = "套餐 id")
    private Long id;

    @Schema(description = "套餐名 i18n code")
    private String nameI18nCode;

    @Schema(description = "套餐名(已本地化)")
    private String name;

    @Schema(description = "套餐描述 i18n code")
    private String descriptionI18nCode;

    @Schema(description = "每周课次")
    private Integer weeklyCount;

    @Schema(description = "总课次")
    private Integer totalCount;

    @Schema(description = "有效天数")
    private Integer validityDays;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "价格是否对当前访问者可见")
    private Boolean priceVisible;

    /**
     * Wave 4 P1 字段(2026-05-10):是否免费体验套餐(前端用于隐藏 / 直跳 free-trial)。
     */
    @Schema(description = "是否免费体验套餐")
    private Boolean isFreeTrial;

    /**
     * Wave 4 P1 字段(2026-05-10):是否启用(前端兜底过滤,后端默认已过滤)。
     */
    @Schema(description = "是否启用")
    private Boolean isActive;

    /**
     * Wave 4 P1 字段(2026-05-10):PackageList 主推卡排序;> 0 上主推区,0 仅在对比表。
     */
    @Schema(description = "主推卡排序(>0 表示主推卡)")
    private Integer showOnListPriority;

    /**
     * Wave 4 P1 字段(2026-05-10):推荐 pill 文案(已按用户 locale 翻译);null 不显示。
     */
    @Schema(description = "推荐 pill 文案(已本地化)")
    private String recommendationLabel;

    /**
     * Wave 4 P1 字段(2026-05-10):折扣徽章文案(已按用户 locale 翻译);null 不显示。
     */
    @Schema(description = "折扣徽章文案(已本地化)")
    private String discountLabel;

    /**
     * Wave 4 P1 字段(2026-05-10):卖点列表(已按用户 locale 翻译);
     * 实际卖点 i18n_message code 在 DB selling_points JSON, 后端按 locale 翻译。
     */
    @Schema(description = "卖点列表(已本地化)")
    private List<String> sellingPoints;

    /**
     * Wave 4 P1 字段(2026-05-10):单节均价(price / totalCount,后端预算好,前端零计算)。
     */
    @Schema(description = "单节均价")
    private BigDecimal pricePerSession;
}
