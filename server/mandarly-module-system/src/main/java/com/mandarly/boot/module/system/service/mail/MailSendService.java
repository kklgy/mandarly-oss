package com.mandarly.boot.module.system.service.mail;

import com.mandarly.boot.framework.common.enums.UserTypeEnum;
import com.mandarly.boot.module.system.mq.message.mail.MailSendMessage;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * 邮件发送 Service 接口
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailSendService {

    /**
     * 发送单条邮件给管理后台的用户
     *
     * @param userId 用户编码
     * @param toMails 收件邮箱
     * @param ccMails 抄送邮箱
     * @param bccMails 密送邮箱
     * @param templateCode 邮件模版编码
     * @param templateParams 邮件模版参数
     * @param attachments 附件
     * @return 发送日志编号
     */
    default Long sendSingleMailToAdmin(Long userId,
                                       Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                                       String templateCode, Map<String, Object> templateParams,
                                       File... attachments) {
        return sendSingleMail(toMails, ccMails, bccMails, userId, UserTypeEnum.ADMIN.getValue(),
                templateCode, templateParams, attachments);
    }

    /**
     * 发送单条邮件给用户 APP 的用户
     *
     * @param userId 用户编码
     * @param toMails 收件邮箱
     * @param ccMails 抄送邮箱
     * @param bccMails 密送邮箱
     * @param templateCode 邮件模版编码
     * @param templateParams 邮件模版参数
     * @param attachments 附件
     * @return 发送日志编号
     */
    default Long sendSingleMailToMember(Long userId,
                                        Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                                        String templateCode, Map<String, Object> templateParams,
                                        File... attachments) {
        return sendSingleMail(toMails, ccMails, bccMails, userId, UserTypeEnum.MEMBER.getValue(),
                templateCode, templateParams, attachments);
    }

    /**
     * 同步发送单条邮件给用户 APP 的用户。
     *
     * 该方法会在 SMTP 返回并更新发送日志后才返回,用于验证码这类不能把"入队成功"当成"送达链路成功"的场景。
     *
     * @return 发送日志编号
     */
    default Long sendSingleMailToMemberSync(Long userId,
                                            Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                                            String templateCode, Map<String, Object> templateParams,
                                            File... attachments) {
        return sendSingleMailSync(toMails, ccMails, bccMails, userId, UserTypeEnum.MEMBER.getValue(),
                templateCode, templateParams, attachments);
    }

    /**
     * 发送单条邮件
     *
     * @param toMails 收件邮箱
     * @param ccMails 抄送邮箱
     * @param bccMails 密送邮箱
     * @param userId 用户编号
     * @param userType 用户类型
     * @param templateCode 邮件模版编码
     * @param templateParams 邮件模版参数
     * @param attachments 附件
     * @return 发送日志编号
     */
    Long sendSingleMail(Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                        Long userId, Integer userType,
                        String templateCode, Map<String, Object> templateParams,
                        File... attachments);

    /**
     * 同步发送单条邮件。
     *
     * @return 发送日志编号
     */
    Long sendSingleMailSync(Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                            Long userId, Integer userType,
                            String templateCode, Map<String, Object> templateParams,
                            File... attachments);

    /**
     * 执行真正的邮件发送
     * 注意，该方法仅仅提供给 MQ Consumer 使用
     *
     * @param message 邮件
     */
    void doSendMail(MailSendMessage message);

}
