package com.mandarly.boot.module.edu.dal.dataobject.pkg;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐定义表(平台层套餐,后台 §A4 维护)
 *
 * <p>对应 docs/database/02-packages-orders.md §1.2;DDL 见 server/sql/mysql/mandarly.sql §02
 *
 * <p>注意:Java 关键字限制 → 子包名用 pkg,类名 PackageDO,@TableName 显式指向 `package` 表。
 */
@TableName(value = "package", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class PackageDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐名称多语言 key,关联 i18n_message.code
     */
    private String nameI18nCode;

    /**
     * 每周课次,免费体验课为 NULL
     */
    private Integer weeklyCount;

    /**
     * 总课次
     */
    private Integer totalCount;

    /**
     * 有效期(天)
     */
    private Integer validityDays;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 币种,默认 HKD
     */
    private String currency;

    /**
     * 是否免费体验课;一期约束:全表最多 1 条 is_free_trial=1 AND is_active=1
     */
    private Boolean isFreeTrial;

    /**
     * 是否启用(下架后老套餐持有人不影响)
     */
    private Boolean isActive;

    /**
     * 列表排序,正序小在前
     */
    private Integer sort;

    /**
     * 描述多语言 key
     */
    private String descriptionI18nCode;

    // ============= Wave 4 P1 字段(2026-05-10, M5 Wave 4 PackageListView 改造) =============

    /**
     * Wave 4 P1: PackageList 主推卡排序;> 0 表示进主推区,按数值升序;0 仅出现在对比表。
     * 默认 0。
     */
    private Integer showOnListPriority;

    /**
     * Wave 4 P1: 推荐 pill i18n_message.code(如 packages.recommendationLabel.popular);
     * null 表示不显示推荐 pill。前端按用户 locale 通过 I18nMessageService 翻译后渲染。
     */
    private String recommendationLabel;

    /**
     * Wave 4 P1: 折扣徽章 i18n_message.code(如 packages.discountLabel.year10off);
     * null 表示不显示折扣徽章。前端按用户 locale 翻译后渲染。
     */
    private String discountLabel;

    /**
     * Wave 4 P1: 卖点 i18n_message.code 列表 JSON,例:
     *   ["packages.sellingPoint.freeBooking", "packages.sellingPoint.refund24h"]
     * 后端按 locale 批量翻译后通过 AppPackageRespVO.sellingPoints (List<String>) 返。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> sellingPoints;

}
