package com.mandarly.boot.module.edu.dal.dataobject.review;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生评价(与 course_order 1:1)
 *
 * <p>对应 mandarly.sql §子域 02 review 表 / PRD §S7。
 * <p>tag 白名单见 {@code platform_config.review.tag_dict};一期硬编码在
 *    {@link com.mandarly.boot.module.edu.service.review.ReviewServiceImpl#TAG_WHITELIST}。
 */
@TableName(value = "review", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewDO extends TenantBaseDO {

    /**
     * 主键 = course_order.id(1:1)
     */
    @TableId
    private Long orderId;

    private Long studentId;

    private Long teacherId;

    /**
     * 1-5 星
     */
    private Integer rating;

    private String content;

    /**
     * 受 platform_config.review.tag_dict 白名单约束;一期硬编码 6 个 tag
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 最后编辑时间;每次 update 更新;首次提交 = create_time
     */
    private LocalDateTime lastEditedAt;

    /**
     * 1=显示 / 0=隐藏(管理员可后台改,一期默认 1)
     */
    private Boolean isVisible;

    /**
     * 匿名评价(Wave 5 第 11 轮),true 时公开列表展示"匿名学员"
     */
    private Boolean isAnonymous;

    /**
     * 编辑窗口截止时间(create_time + 72h);前端 EditWindowProgress 读
     */
    private LocalDateTime editableUntilAt;

    /**
     * 用户自定义标签,最多 3 条 × 8 字符;不进 platform_config 字典
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> customTags;

}
