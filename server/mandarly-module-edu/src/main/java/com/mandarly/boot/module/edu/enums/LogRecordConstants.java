package com.mandarly.boot.module.edu.enums;

/**
 * Edu 模块操作日志枚举
 *
 * <p>使用 mzt-biz-log 的 {@code @LogRecord} 注解,日志走 {@code system_operate_log} 表,
 * 可在「系统管理 → 操作日志」追溯。
 */
public interface LogRecordConstants {

    // ======================= EDU_WITHDRAWAL 教师提现(M6)=======================

    String EDU_WITHDRAWAL_TYPE = "EDU 提现";

    /** reveal-payee 一次性审计:管理员查看收款信息明文(spec §6.5 ⭐) */
    String EDU_WITHDRAWAL_REVEAL_PAYEE_SUB_TYPE = "查看收款信息明文";
    String EDU_WITHDRAWAL_REVEAL_PAYEE_SUCCESS =
            "管理员【{{#adminId}}】查看了提现申请【{{#id}}】的收款信息明文(教师 ID 【{{#teacherId}}】)";

}
