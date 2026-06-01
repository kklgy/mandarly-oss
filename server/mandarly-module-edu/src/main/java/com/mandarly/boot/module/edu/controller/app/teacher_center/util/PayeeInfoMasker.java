package com.mandarly.boot.module.edu.controller.app.teacher_center.util;

/**
 * 提现 payee_info 脱敏工具(spec §6.5 / §5.4)。
 *
 * <p>规则:
 * <ul>
 *   <li>输入 null → 输出 null</li>
 *   <li>取 payeeInfo 后 4 个 <strong>UTF-8 字符</strong>(非 byte),前缀 method:
 *       {@code "paypal·****.com"} / {@code "wechat·****朋友"}</li>
 *   <li>不足 4 字符时全部脱敏:{@code "paypal·****"}</li>
 *   <li>method 为空时仅返回脱敏部分(无前缀)</li>
 * </ul>
 *
 * <p>placement:teacher_center.util,A6 仅 Withdrawal Controller 内部使用;
 * 若 A7 admin 列表也需脱敏,可移到 module-edu 公共 util 包(留待 A7 视复用程度再迁)。
 */
public final class PayeeInfoMasker {

    private static final String SEPARATOR = "·"; // 中点 ·
    private static final String MASK = "****";

    private PayeeInfoMasker() {
        // utility class
    }

    /**
     * 脱敏 payeeInfo,返回 "{payeeMethod}·****{last4}"。
     *
     * @param payeeInfo   明文(可能为 null)
     * @param payeeMethod 方式(wechat/alipay/paypal/bank_card/other,可空)
     * @return 脱敏字符串,或 null(当 payeeInfo 为 null 时)
     */
    public static String mask(String payeeInfo, String payeeMethod) {
        if (payeeInfo == null) {
            return null;
        }

        // UTF-8 char count via codePoint stream(正确处理中文 / emoji)
        int[] codePoints = payeeInfo.codePoints().toArray();
        String last4;
        if (codePoints.length <= 4) {
            last4 = ""; // 太短全部脱敏
        } else {
            int start = codePoints.length - 4;
            last4 = new String(codePoints, start, 4);
        }

        String prefix = (payeeMethod == null || payeeMethod.isBlank())
                ? ""
                : payeeMethod + SEPARATOR;

        return prefix + MASK + last4;
    }
}
