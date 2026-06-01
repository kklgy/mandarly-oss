package com.mandarly.boot.module.edu.dal.dataobject.levelcheck;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 水平测试题目
 *
 * <p>对应 DDL `level_check_question` / 设计文档 `docs/product/level-check-recommendation-v1.md`
 */
@TableName("level_check_question")
@Data
@EqualsAndHashCode(callSuper = true)
public class LevelCheckQuestionDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String questionCode;

    private String questionI18nCode;

    /**
     * 一期固定 single_choice
     */
    private String questionType;

    private Integer sort;

    private Boolean isActive;

}
