package com.mandarly.boot.module.system.service.auth;

public interface MailCodeService {

    /**
     * 发送邮件验证码
     *
     * @param email  目标邮箱
     * @param scene  场景 register / login / reset / bind(reset 与 reset-password 等价)
     * @param locale 语言(zh* → 中文模板,其它 → 英文模板;null/空也走英文)
     */
    void sendCode(String email, String scene, String locale);

    /**
     * 校验并使用验证码(一次性失效)。失败抛 ServiceException
     *
     * @param email 目标邮箱
     * @param code  用户输入的验证码
     * @param scene 场景
     */
    void useCode(String email, String code, String scene);
}
