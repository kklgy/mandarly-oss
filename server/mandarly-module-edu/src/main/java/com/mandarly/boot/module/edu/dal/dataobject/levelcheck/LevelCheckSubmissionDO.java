package com.mandarly.boot.module.edu.dal.dataobject.levelcheck;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 水平测试答卷记录
 *
 * <p>未登录用户用 session_id 兜底(沿用 PRD §C 客服 session 同源)
 * <p>is_converted 在订单成功后回写,用于运营看转化漏斗
 */
@TableName(value = "level_check_submission", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class LevelCheckSubmissionDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String sessionId;

    private String email;

    private String locale;

    private String market;

    /**
     * 答案数组,如 [{"questionCode":"q1_level","optionCode":"beginner"}]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> answers;

    private String inferredLevel;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> recommendedTeacherIds;

    private Long recommendedPackageId;

    private Boolean isConverted;

    private Long convertedOrderId;

}
