package com.mandarly.boot.module.system.service.mail;

import cn.hutool.core.collection.ListUtil;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import com.mandarly.boot.module.system.dal.dataobject.mail.MailAccountDO;
import com.mandarly.boot.module.system.dal.dataobject.mail.MailLogDO;
import com.mandarly.boot.module.system.dal.dataobject.mail.MailTemplateDO;
import com.mandarly.boot.module.system.dal.mysql.mail.MailLogMapper;
import com.mandarly.boot.module.system.enums.mail.MailSendStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.hutool.core.exceptions.ExceptionUtil.getRootCauseMessage;

/**
 * 邮件日志 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
public class MailLogServiceImpl implements MailLogService {

    @Resource
    private MailLogMapper mailLogMapper;

    @Override
    public PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO) {
        return mailLogMapper.selectPage(pageVO);
    }

    @Override
    public MailLogDO getMailLog(Long id) {
        return mailLogMapper.selectById(id);
    }

    @Override
    public Long createMailLog(Long userId, Integer userType,
                              Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                              MailAccountDO account, MailTemplateDO template,
                              String templateContent, Map<String, Object> templateParams, Boolean isSend) {
        MailLogDO.MailLogDOBuilder logDOBuilder = MailLogDO.builder();
        // 根据是否要发送，设置状态
        logDOBuilder.sendStatus(Objects.equals(isSend, true) ? MailSendStatusEnum.INIT.getStatus()
                : MailSendStatusEnum.IGNORE.getStatus())
                // 用户信息
                .userId(userId).userType(userType)
                .toMails(ListUtil.toList(toMails)).ccMails(ListUtil.toList(ccMails)).bccMails(ListUtil.toList(bccMails))
                .accountId(account.getId()).fromMail(account.getMail())
                // 模板相关字段
                .templateId(template.getId()).templateCode(template.getCode()).templateNickname(template.getNickname())
                .templateTitle(template.getTitle()).templateContent(templateContent).templateParams(templateParams);

        // 插入数据库
        MailLogDO logDO = logDOBuilder.build();
        int inserted = mailLogMapper.insert(logDO);
        if (inserted != 1 || logDO.getId() == null) {
            throw new IllegalStateException("邮件发送日志创建失败");
        }
        return logDO.getId();
    }

    @Override
    public void updateMailSendResult(Long logId, String messageId, Exception exception) {
        if (logId == null) {
            throw new IllegalArgumentException("邮件发送日志编号不能为空");
        }
        // 1. 成功
        int updated;
        if (exception == null) {
            updated = mailLogMapper.updateById(new MailLogDO().setId(logId).setSendTime(LocalDateTime.now())
                    .setSendStatus(MailSendStatusEnum.SUCCESS.getStatus()).setSendMessageId(messageId));
        } else {
            // 2. 失败
            updated = mailLogMapper.updateById(new MailLogDO().setId(logId).setSendTime(LocalDateTime.now())
                    .setSendStatus(MailSendStatusEnum.FAILURE.getStatus()).setSendException(getRootCauseMessage(exception)));
        }
        if (updated != 1) {
            throw new IllegalStateException("邮件发送日志更新失败, logId=" + logId);
        }

    }

}
