package com.mandarly.boot.module.edu.dal.dataobject.booking;

import com.mandarly.boot.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 学生持有的套餐实例
 *
 * <p>对应 docs/database/02-packages-orders.md §1.2 / DDL `student_package`
 */
@TableName("student_package")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentPackageDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生 user.id(role=student)
     */
    private Long studentId;

    /**
     * 套餐定义 id
     */
    private Long packageId;

    /**
     * 剩余课次
     */
    private Integer remaining;

    /**
     * 有效期截止(UTC)
     */
    private LocalDateTime expireAt;

    /**
     * 来源 purchase / free_trial / admin_grant;
     * 枚举:{@link com.mandarly.boot.module.edu.enums.booking.StudentPackageSourceEnum}
     */
    private String source;

    /**
     * 关联支付 id(可空,非 purchase 时为空)
     */
    private Long paymentId;

    /**
     * admin_grant 时操作管理员 id
     */
    private Long grantedBy;

    /**
     * admin_grant 时备注
     */
    private String grantedReason;

}
