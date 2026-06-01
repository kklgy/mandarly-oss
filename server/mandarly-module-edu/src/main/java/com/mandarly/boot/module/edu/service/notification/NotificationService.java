package com.mandarly.boot.module.edu.service.notification;

import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;

/**
 * 教育业务通知 Service(facade)。
 *
 * <p>M6 Phase D · D3 新建,集中承载提现状态变更的 SMS + 邮件双通道触发。
 *
 * <p>设计要点:
 * <ul>
 *   <li>所有方法 {@code @Async},通知失败不回滚主业务事务</li>
 *   <li>SMS:海外 / 港澳台号(非 +86 开头)走腾讯云 ISMS;当前提现模板尚未申请国内版,+86 暂跳过</li>
 *   <li>邮件:无地域限制,所有教师都发</li>
 *   <li>状态文案 i18n 通过本 service 内的 hard-coded Map 翻译(zh / en),不动 i18n_message DB 表(spec §7.2)</li>
 * </ul>
 */
public interface NotificationService {

    /**
     * 提现状态事件通知。
     *
     * <p>触发时机(由 TeacherWithdrawalServiceImpl 在主事务 commit 后调用):
     * <pre>
     *   applyWithdrawal() 末尾  → sendForWithdrawalEvent("applied", w)
     *   audit(approved=true)    → sendForWithdrawalEvent("approved", w)
     *   audit(approved=false)   → sendForWithdrawalEvent("rejected", w)
     *   markPaid()              → sendForWithdrawalEvent("paid", w)
     *   markFailed()            → sendForWithdrawalEvent("failed", w)
     * </pre>
     *
     * @param event 状态事件:applied / approved / paid / rejected / failed
     * @param w     提现记录 DO(必填字段:teacherId / amount / currency / status / rejectReason / paidRemark)
     */
    void sendForWithdrawalEvent(String event, TeacherWithdrawalDO w);

    /**
     * 教师资质审核结果通知(D19 一期只发 mail)。
     *
     * <p>触发时机(由 TeacherProfileServiceImpl.auditTeacherProfile 在主事务 commit 后调用):
     * <pre>
     *   audit(approved=true)  → sendForTeacherAuditEvent("approved", teacherUserId, null)
     *   audit(approved=false) → sendForTeacherAuditEvent("rejected", teacherUserId, rejectReason)
     * </pre>
     *
     * <p>一期实装范围:仅 mail 通道。
     * <ul>
     *   <li>SMS:腾讯云 ISMS 教师审核结果专属模板尚未申请过审,memory sms_template_ids.md
     *       已过审 10 个模板里没有审核结果类目,塞退款模板兜底反而误导教师,故先跳过。
     *       专属模板过审后再启用(参考 withdrawal 海外号判断逻辑)。</li>
     *   <li>inbox:notify_log 表 + NotifyLogDO + NotifyLogMapper 尚不存在,
     *       按 MVP "no over-engineering" 原则不在 D19 一期范围内新建基础设施,
     *       需要 inbox 时另起 task 建表 + 实装。</li>
     * </ul>
     *
     * @param event         "approved" / "rejected"
     * @param teacherUserId 教师 user.id
     * @param rejectReason  rejected 时传文案,approved 时传 null
     */
    void sendForTeacherAuditEvent(String event, Long teacherUserId, String rejectReason);

    /**
     * 学生预约成功后通知教师(D22 一期只发 mail)。
     *
     * <p>触发时机:BookingServiceImpl#createOrder 成功创建 course_order + LCIC 房间后调用。
     * 通知失败只记录日志,不反噬预约主链路。
     *
     * @param orderId course_order.id
     */
    void sendForBookingCreated(Long orderId);
}
