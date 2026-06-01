package com.mandarly.boot.module.edu.controller.app.teacher_center.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PayeeInfoMasker} 单元测试(A6 Step 4 — 单 case 快测)
 *
 * <p>spec §6.5 / §5.4 教师视角:payee_info 后 4 UTF-8 字符 + method 前缀。
 */
class PayeeInfoMaskerTest {

    @Test
    void mask_handles_paypal_email_and_chinese_and_null() {
        // 英文 email
        assertThat(PayeeInfoMasker.mask("user@example.com", "paypal"))
                .isEqualTo("paypal·****.com");

        // 中文 — UTF-8 字符计数,非 byte;后 4 个 char point = "张三李四"
        assertThat(PayeeInfoMasker.mask("微信号张三李四", "wechat"))
                .isEqualTo("wechat·****张三李四");

        // null 输入 → null 输出
        assertThat(PayeeInfoMasker.mask(null, "paypal")).isNull();

        // 太短(≤4 chars)全部脱敏
        assertThat(PayeeInfoMasker.mask("abc", "bank_card"))
                .isEqualTo("bank_card·****");

        // method 空 → 仅返回脱敏部分
        assertThat(PayeeInfoMasker.mask("user@example.com", null))
                .isEqualTo("****.com");
    }
}
