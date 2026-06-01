package com.mandarly.boot.module.edu.service.notification;

import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import com.mandarly.boot.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import com.mandarly.boot.module.system.api.sms.SmsSendApi;
import com.mandarly.boot.module.system.api.sms.dto.send.SmsSendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationService 实现(M6 D3)。
 *
 * <p>SMS 模板 code:
 * <ul>
 *   <li>{@code edu-withdrawal-status-zh} → ISMS 通用过审 api_template_id 2638304</li>
 *   <li>{@code edu-withdrawal-status-en} → ISMS 通用过审 api_template_id 2638303</li>
 * </ul>
 *
 * <p>邮件模板 code(按 event + locale 拼接):
 * {@code mandarly_withdrawal_{event}_{locale_suffix}},5 事件 × 4 语言 = 20 行,
 * 见 patch {@code 20260511_233700_add_withdrawal_mail_templates.sql}。
 *
 * <p>状态文案 i18n:zh / en 硬编码 Map(spec §7.2 i18n_message DB 表不动)。
 * 当 locale = zh-TW 时按 zh 处理(简繁同义),locale = ar 时回落 en(M5 占位策略)。
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private CourseOrderMapper courseOrderMapper;

    @Resource
    private SmsSendApi smsSendApi;

    @Resource
    private MailSendApi mailSendApi;

    /** event → locale("zh" / "en") → statusText 翻译表。 */
    private static final Map<String, Map<String, String>> STATUS_TEXT;

    /** D19 教师审核 event → locale("zh" / "en") → statusText 翻译表。 */
    private static final Map<String, Map<String, String>> TEACHER_AUDIT_STATUS_TEXT;

    static {
        STATUS_TEXT = new HashMap<>();
        STATUS_TEXT.put("applied", Map.of(
                "zh", "已申请,等待审核",
                "en", "Submitted, pending review"));
        STATUS_TEXT.put("approved", Map.of(
                "zh", "已通过审核,等待打款",
                "en", "Approved, awaiting payout"));
        STATUS_TEXT.put("paid", Map.of(
                "zh", "已打款完成",
                "en", "Paid"));
        STATUS_TEXT.put("rejected", Map.of(
                "zh", "已驳回",
                "en", "Rejected"));
        STATUS_TEXT.put("failed", Map.of(
                "zh", "打款失败,余额已退回",
                "en", "Payout failed, balance restored"));

        TEACHER_AUDIT_STATUS_TEXT = new HashMap<>();
        TEACHER_AUDIT_STATUS_TEXT.put("approved", Map.of(
                "zh", "审核通过,可以开始接单",
                "en", "Approved, you can now accept orders"));
        TEACHER_AUDIT_STATUS_TEXT.put("rejected", Map.of(
                "zh", "审核未通过",
                "en", "Application rejected"));
    }

    @Override
    @Async
    public void sendForWithdrawalEvent(String event, TeacherWithdrawalDO w) {
        if (event == null || w == null) {
            log.warn("[withdrawal-notify] skip null event or withdrawal");
            return;
        }
        if (!STATUS_TEXT.containsKey(event)) {
            log.warn("[withdrawal-notify] unknown event={}, skip", event);
            return;
        }

        Long teacherId = w.getTeacherId();
        Object teacher = reflectGetUser(teacherId);
        if (teacher == null) {
            log.warn("[withdrawal-notify] teacher not found, skip notify: teacherId={} event={}", teacherId, event);
            return;
        }

        String locale = normalizeLocale(reflectGet(teacher, "getLocale"));
        String phone = reflectGet(teacher, "getPhone");
        String email = reflectGet(teacher, "getEmail");

        Map<String, Object> params = buildParams(event, w, locale);

        // ---------- SMS:提现状态仅有国际/港澳台模板,+86 等国内提现模板报备后再启用 ----------
        try {
            if (isOverseasMobile(phone)) {
                String smsCode = locale.startsWith("zh") ? "edu-withdrawal-status-zh" : "edu-withdrawal-status-en";
                SmsSendSingleToUserReqDTO smsReq = new SmsSendSingleToUserReqDTO();
                smsReq.setUserId(teacherId);
                smsReq.setMobile(phone);
                smsReq.setTemplateCode(smsCode);
                // SMS 参数只用 amount/currency/statusText 3 个(模板有 3 占位)
                Map<String, Object> smsParams = Map.of(
                        "amount", params.get("amount"),
                        "currency", params.get("currency"),
                        "statusText", params.get("statusText"));
                smsReq.setTemplateParams(smsParams);
                smsSendApi.sendSingleSmsToMember(smsReq);
                log.info("[withdrawal-sms] sent teacherId={} event={} code={}", teacherId, event, smsCode);
            } else {
                log.info("[withdrawal-sms] skip mobile without withdrawal domestic template: teacherId={} event={} phone={}",
                        teacherId, event, maskPhone(phone));
            }
        } catch (Exception e) {
            log.error("[withdrawal-sms] send failed teacherId={} event={}", teacherId, event, e);
        }

        // ---------- 邮件:所有教师都发 ----------
        if (email == null || email.isBlank()) {
            log.warn("[withdrawal-mail] teacher email missing, skip: teacherId={} event={}", teacherId, event);
            return;
        }
        try {
            String mailCode = "mandarly_withdrawal_" + event + "_" + mailLocaleSuffix(locale);
            MailSendSingleToUserReqDTO mailReq = new MailSendSingleToUserReqDTO();
            // 不传 userId,改直接 setToMails 绕过 member 反查(沿用 M4 PaymentServiceImpl 模式 —
            // Mandarly 删除了 ruoyi 默认 member 模块,默认 MailSendApi 反查 email 会 ClassNotFound)
            mailReq.setToMails(List.of(email));
            mailReq.setTemplateCode(mailCode);
            mailReq.setTemplateParams(params);
            mailSendApi.sendSingleMailToMember(mailReq);
            log.info("[withdrawal-mail] sent teacherId={} event={} code={}", teacherId, event, mailCode);
        } catch (Exception e) {
            log.error("[withdrawal-mail] send failed teacherId={} event={}", teacherId, event, e);
        }
    }

    @Override
    @Async
    public void sendForTeacherAuditEvent(String event, Long teacherUserId, String rejectReason) {
        if (event == null || teacherUserId == null) {
            log.warn("[teacher-audit-notify] skip null event or teacherUserId");
            return;
        }
        if (!TEACHER_AUDIT_STATUS_TEXT.containsKey(event)) {
            log.warn("[teacher-audit-notify] unknown event={}, skip", event);
            return;
        }

        Object teacher = reflectGetUser(teacherUserId);
        if (teacher == null) {
            log.warn("[teacher-audit-notify] teacher not found, skip notify: teacherUserId={} event={}",
                    teacherUserId, event);
            return;
        }

        String locale = normalizeLocale(reflectGet(teacher, "getLocale"));
        String email = reflectGet(teacher, "getEmail");
        String nickname = reflectGet(teacher, "getNickname");
        String i18nLocale = locale.startsWith("zh") ? "zh" : "en";
        String statusText = TEACHER_AUDIT_STATUS_TEXT.get(event).get(i18nLocale);

        Map<String, Object> params = new HashMap<>();
        // 模板正文以 "{nickname},您好" / "Hi {nickname}" 起手;nickname 缺失时 fallback email
        // (避免 StrUtil.format 留下字面 "{nickname}" 文本污染用户邮箱)
        params.put("nickname", (nickname != null && !nickname.isBlank()) ? nickname : email);
        params.put("statusText", statusText);
        if ("rejected".equals(event)) {
            params.put("rejectReason", nullToDash(rejectReason));
        }

        // === inbox 通道(D19 一期未实装)===
        // TODO(D19 后续):notify_log 表 + NotifyLogDO + NotifyLogMapper 建好后,
        //   在此插入 channel='inbox' 一行;教师端从 inbox 拉取审核状态消息.

        // === SMS 通道(D19 一期未实装)===
        // TODO(D19 后续):腾讯云 ISMS 教师审核结果专属模板申请过审后启用.
        //   仅发海外号(非 +86 开头);复用 withdrawal 海外号判断逻辑.

        // ---------- 邮件:所有教师都发(沿用 withdrawal 模式) ----------
        if (email == null || email.isBlank()) {
            log.warn("[teacher-audit-mail] teacher email missing, skip: teacherUserId={} event={}",
                    teacherUserId, event);
            return;
        }
        try {
            String mailCode = "mandarly_teacher_audit_" + event + "_" + mailLocaleSuffix(locale);
            MailSendSingleToUserReqDTO mailReq = new MailSendSingleToUserReqDTO();
            // 不传 userId,直接 setToMails(沿用 withdrawal-mail 模式,绕过 member 反查)
            mailReq.setToMails(List.of(email));
            mailReq.setTemplateCode(mailCode);
            mailReq.setTemplateParams(params);
            mailSendApi.sendSingleMailToMember(mailReq);
            log.info("[teacher-audit-mail] sent teacherUserId={} event={} code={}",
                    teacherUserId, event, mailCode);
        } catch (Exception e) {
            log.error("[teacher-audit-mail] send failed teacherUserId={} event={}",
                    teacherUserId, event, e);
        }
    }

    @Override
    @Async
    public void sendForBookingCreated(Long orderId) {
        if (orderId == null) {
            log.warn("[booking-created-notify] skip null orderId");
            return;
        }
        CourseOrderDO order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            log.warn("[booking-created-notify] order not found, skip notify: orderId={}", orderId);
            return;
        }

        Object teacher = reflectGetUser(order.getTeacherId());
        if (teacher == null) {
            log.warn("[booking-created-notify] teacher not found, skip notify: orderId={} teacherId={}",
                    orderId, order.getTeacherId());
            return;
        }
        String teacherEmail = reflectGet(teacher, "getEmail");
        if (teacherEmail == null || teacherEmail.isBlank()) {
            log.warn("[booking-created-mail] teacher email missing, skip: orderId={} teacherId={}",
                    orderId, order.getTeacherId());
            return;
        }

        Object student = reflectGetUser(order.getStudentId());
        String studentEmail = reflectGet(student, "getEmail");
        String studentNickname = reflectGet(student, "getNickname");
        String teacherLocale = normalizeLocale(reflectGet(teacher, "getLocale"));
        String teacherNickname = reflectGet(teacher, "getNickname");

        Map<String, Object> params = new HashMap<>();
        params.put("teacherName", firstNonBlank(teacherNickname, teacherEmail));
        params.put("studentName", firstNonBlank(studentNickname, studentEmail, "Student"));
        params.put("studentEmail", nullToDash(studentEmail));
        params.put("scheduledAt", order.getScheduledAt() != null ? order.getScheduledAt().toString() : "-");
        params.put("duration", order.getDuration() != null ? order.getDuration().toString() : "30");
        params.put("orderId", order.getId() != null ? order.getId().toString() : orderId.toString());

        try {
            String mailCode = "mandarly_booking_created_" + mailLocaleSuffix(teacherLocale);
            MailSendSingleToUserReqDTO mailReq = new MailSendSingleToUserReqDTO();
            mailReq.setToMails(List.of(teacherEmail));
            mailReq.setTemplateCode(mailCode);
            mailReq.setTemplateParams(params);
            mailSendApi.sendSingleMailToMember(mailReq);
            log.info("[booking-created-mail] sent orderId={} teacherId={} code={}",
                    orderId, order.getTeacherId(), mailCode);
        } catch (Exception e) {
            log.error("[booking-created-mail] send failed orderId={} teacherId={}",
                    orderId, order.getTeacherId(), e);
        }
    }

    // ======================== helpers ========================

    /**
     * 构建模板参数集。
     *
     * <p>邮件模板按 event 不同会引用以下额外占位变量(SQL patch §D2):
     * <ul>
     *   <li>paid → paidRemark</li>
     *   <li>rejected → rejectReason</li>
     *   <li>failed → failReason(实现上复用 paidRemark 字段,见 spec §4.2)</li>
     * </ul>
     */
    private Map<String, Object> buildParams(String event, TeacherWithdrawalDO w, String locale) {
        BigDecimal amount = w.getAmount();
        String amountStr = amount != null ? amount.toPlainString() : "0.00";
        String currency = w.getCurrency() != null ? w.getCurrency() : "USD";
        String i18nLocale = locale.startsWith("zh") ? "zh" : "en";
        String statusText = STATUS_TEXT.get(event).get(i18nLocale);

        Map<String, Object> p = new HashMap<>();
        p.put("amount", amountStr);
        p.put("currency", currency);
        p.put("statusText", statusText);

        switch (event) {
            case "paid":
                p.put("paidRemark", nullToDash(w.getPaidRemark()));
                break;
            case "rejected":
                p.put("rejectReason", nullToDash(w.getRejectReason()));
                break;
            case "failed":
                // markFailed 把 failReason 写到 paidRemark 字段(spec §4.2)
                p.put("failReason", nullToDash(w.getPaidRemark()));
                break;
            default:
                break;
        }
        return p;
    }

    private String mailLocaleSuffix(String locale) {
        return switch (locale != null ? locale : "en") {
            case "zh-CN" -> "zh_cn";
            case "zh-TW" -> "zh_tw";
            case "ar" -> "ar";
            default -> "en";
        };
    }

    private String normalizeLocale(String raw) {
        if (raw == null || raw.isBlank()) return "en";
        return raw;
    }

    /**
     * 判断是否海外 / 港澳台号(非 +86 开头)。
     *
     * <p>规则:E.164 格式以 + 开头;非 +86 开头视为海外。
     * 无前缀号或 +86 开头 → 当前提现状态 SMS 跳过(国内提现模板尚未报备)。
     */
    private boolean isOverseasMobile(String phone) {
        if (phone == null || phone.isBlank()) return false;
        String trimmed = phone.trim();
        if (!trimmed.startsWith("+")) {
            // 无国际前缀:保守起见当作大陆号,跳过 SMS
            return false;
        }
        return !trimmed.startsWith("+86");
    }

    private String maskPhone(String phone) {
        if (phone == null) return "(null)";
        if (phone.length() <= 4) return "***";
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 2);
    }

    private String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    /**
     * 反射取 user 对象(沿用 M4 PaymentServiceImpl#reflectGetUser 模式,避免模块间循环依赖)。
     *
     * <p>独立为 package-private,单元测试通过 spy 覆盖,避免 Spring context 依赖
     * (沿用 ReferralServiceImpl#findUserByReferralCode 模式)。
     */
    Object reflectGetUser(Long userId) {
        if (userId == null) return null;
        try {
            Class<?> svc = Class.forName("com.mandarly.boot.module.system.service.user.UserService");
            Object bean = cn.hutool.extra.spring.SpringUtil.getBean(svc);
            return svc.getMethod("getById", Long.class).invoke(bean, userId);
        } catch (Exception e) {
            log.warn("[reflect-get-user] reflectGetUser fail userId={}", userId, e);
            return null;
        }
    }

    private String reflectGet(Object obj, String getter) {
        if (obj == null) return null;
        try {
            Object v = obj.getClass().getMethod(getter).invoke(obj);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            log.warn("[withdrawal-notify] reflectGet {} fail", getter, e);
            return null;
        }
    }
}
