package com.mandarly.boot.module.edu.service.webhook;

import com.mandarly.boot.module.edu.dal.dataobject.payment.StripeEventDO;
import com.mandarly.boot.module.edu.dal.mysql.payment.StripeEventMapper;
import com.stripe.model.Event;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stripe webhook 事务边界 helper — 解决 Spring AOP self-invocation 经典坑。
 *
 * <p><b>背景</b>:Spring 的 @Transactional 通过动态代理实现,同类内部直接调用方法
 * 不会经过代理,导致 {@link Propagation#REQUIRES_NEW} 失效退化为同事务。
 * StripeWebhookHandler.process() 直接调本类内部的 persist/mark 方法时,两段式事务
 * 边界实际未生效 — 段三 markEventFailed 会被段二业务异常回滚。
 *
 * <p><b>修法</b>:把 REQUIRES_NEW 方法抽到独立 Spring Bean(本类),
 * StripeWebhookHandler 通过 @Resource 注入再调用,代理生效,事务真独立。
 *
 * <p>本类只放事务边界方法,业务逻辑仍在 Handler / 各 Service。
 */
@Component
@Slf4j
public class StripeEventTxHelper {

    @Resource
    private StripeEventMapper eventMapper;

    /**
     * 段一独立事务:INSERT IGNORE 去重,即时 commit。
     *
     * @return true = 新事件(需继续 dispatch);false = 重复事件(已处理过,跳过)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean persistEventIgnoreDuplicate(Event event, String payload) {
        int affected = eventMapper.insertIgnore(event.getId(), event.getType(), payload);
        log.info("[persistEventIgnoreDuplicate] eventId={} type={} affected={}",
                event.getId(), event.getType(), affected);
        return affected == 1;
    }

    /**
     * 段三独立事务:标记 result=failed + error_msg。
     * REQUIRES_NEW 保证即便段二事务已回滚也能写入。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markEventFailed(String eventId, Exception e) {
        StripeEventDO update = new StripeEventDO();
        update.setId(eventId);
        update.setResult("failed");
        update.setErrorMsg(truncate(cn.hutool.core.exceptions.ExceptionUtil.stacktraceToString(e), 4000));
        eventMapper.updateById(update);
        log.info("[markEventFailed] eventId={} marked failed", eventId);
    }

    /**
     * 段三独立事务:标记 result=ignored(未知事件类型)。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markEventIgnored(String eventId) {
        StripeEventDO update = new StripeEventDO();
        update.setId(eventId);
        update.setResult("ignored");
        eventMapper.updateById(update);
        log.info("[markEventIgnored] eventId={} marked ignored", eventId);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }
}
