package com.mandarly.boot.module.edu.dal.dataobject.levelcheck;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 水平测试选项 + 推荐规则
 *
 * <p>match_expertise 是硬约束(数组),score_rules 是软约束打分([{expertise, score}])
 */
@TableName(value = "level_check_option", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class LevelCheckOptionDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private String optionCode;

    private String optionI18nCode;

    /**
     * 硬约束:必须命中的教师 expertise 数组,如 ["kids"] / ["HSK"]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> matchExpertise;

    /**
     * 软约束打分规则,如 [{"expertise":"business","score":10}]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> scoreRules;

    /**
     * Q1 用,beginner / intermediate / advanced
     */
    private String inferredLevel;

    /**
     * Q3 用,推荐套餐时取此值匹配 package.weekly_count
     */
    private Integer recommendedWeeklyCount;

    private Integer sort;

}
