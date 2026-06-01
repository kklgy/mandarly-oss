-- =============================================================================
-- Patch:M6 教师提现邮件模板(2026-05-11)
-- =============================================================================
-- 来源:docs/product/M6-plan.md Phase D / Task D2,spec §7.4
-- 5 模板 × 4 语言(zh-CN / zh-TW / en / ar) = 20 行 system_mail_template
-- ar 为初版占位(沿用 M4 / M5 策略),二期由阿语校对补全
-- 模板 ID 范围:301-320(接续 M4 201-216)
--
-- 模板说明:
--   #1 mandarly_withdrawal_applied_{locale}:   已提交申请
--   #2 mandarly_withdrawal_approved_{locale}:  审核通过
--   #3 mandarly_withdrawal_paid_{locale}:      已打款
--   #4 mandarly_withdrawal_rejected_{locale}:  已驳回(含 rejectReason)
--   #5 mandarly_withdrawal_failed_{locale}:    打款失败(含 failReason)
--
-- 占位变量说明:
--   #1: {amount} {currency}
--   #2: {amount} {currency}
--   #3: {amount} {currency} {paidRemark}
--   #4: {amount} {currency} {rejectReason}
--   #5: {amount} {currency} {failReason}
-- =============================================================================

INSERT INTO system_mail_template
  (id, name, code, account_id, nickname, title, content, params, status, remark, creator, updater)
VALUES

-- ============ #1 已提交申请 ============
(301, '提现-已申请-简中', 'mandarly_withdrawal_applied_zh_cn', 1, 'Mandarly',
 'Mandarly · 提现申请已提交',
 '您好！\n\n您已成功提交 Mandarly 提现申请。\n\n提现金额：{currency} {amount}\n当前状态：已申请,等待审核\n\n我们将在 3 个工作日内完成审核,审核结果将通过短信和邮件通知您。\n\n查看提现记录:\nhttp://localhost:3001/teacher/withdrawal\n\n如有疑问请联系客服：support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency"]', 0, 'M6 提现已申请 ZH-CN', 'system', 'system'),

(302, '提现-已申请-繁中', 'mandarly_withdrawal_applied_zh_tw', 1, 'Mandarly',
 'Mandarly · 提現申請已提交',
 '您好！\n\n您已成功提交 Mandarly 提現申請。\n\n提現金額：{currency} {amount}\n當前狀態:已申請,等待審核\n\n我們將在 3 個工作日內完成審核,審核結果將通過簡訊和郵件通知您。\n\n查看提現記錄:\nhttp://localhost:3001/teacher/withdrawal\n\n如有疑問請聯絡客服:support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency"]', 0, 'M6 提现已申请 ZH-TW', 'system', 'system'),

(303, '提现-已申请-英文', 'mandarly_withdrawal_applied_en', 1, 'Mandarly',
 'Mandarly · Withdrawal request submitted',
 'Hi there!\n\nYour Mandarly withdrawal request has been submitted.\n\nAmount: {currency} {amount}\nStatus: Submitted, pending review\n\nWe will complete the review within 3 business days, and notify you via SMS and email of the result.\n\nView your withdrawal records:\nhttp://localhost:3001/teacher/withdrawal\n\nIf you have any questions, contact: support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency"]', 0, 'M6 提现已申请 EN', 'system', 'system'),

(304, '提现-已申请-阿语', 'mandarly_withdrawal_applied_ar', 1, 'Mandarly',
 '[TODO ar] Mandarly · تم تقديم طلب السحب',
 '[TODO ar] مرحباً!\n\nتم تقديم طلب السحب الخاص بك على Mandarly.\n\nالمبلغ: {currency} {amount}\nالحالة: مُقدّم، في انتظار المراجعة\n\nسنُكمل المراجعة خلال 3 أيام عمل.\n\nhttp://localhost:3001/teacher/withdrawal\n\n— Mandarly Team',
 '["amount","currency"]', 0, 'M6 提现已申请 AR (TODO)', 'system', 'system'),

-- ============ #2 审核通过 ============
(305, '提现-已通过-简中', 'mandarly_withdrawal_approved_zh_cn', 1, 'Mandarly',
 'Mandarly · 提现申请已通过',
 '您好！\n\n好消息！您的 Mandarly 提现申请已通过审核。\n\n提现金额：{currency} {amount}\n当前状态：已通过,等待打款\n\n我们将尽快为您打款,款项到账后会再次通知您。\n\n查看提现记录:\nhttp://localhost:3001/teacher/withdrawal\n\n— Mandarly Team',
 '["amount","currency"]', 0, 'M6 提现已通过 ZH-CN', 'system', 'system'),

(306, '提现-已通过-繁中', 'mandarly_withdrawal_approved_zh_tw', 1, 'Mandarly',
 'Mandarly · 提現申請已通過',
 '您好！\n\n好消息!您的 Mandarly 提現申請已通過審核。\n\n提現金額:{currency} {amount}\n當前狀態:已通過,等待打款\n\n我們將盡快為您打款,款項到帳後會再次通知您。\n\n查看提現記錄:\nhttp://localhost:3001/teacher/withdrawal\n\n— Mandarly Team',
 '["amount","currency"]', 0, 'M6 提现已通过 ZH-TW', 'system', 'system'),

(307, '提现-已通过-英文', 'mandarly_withdrawal_approved_en', 1, 'Mandarly',
 'Mandarly · Withdrawal approved',
 'Hi there!\n\nGood news! Your Mandarly withdrawal request has been approved.\n\nAmount: {currency} {amount}\nStatus: Approved, awaiting payout\n\nWe will process the payout shortly and notify you once the funds are sent.\n\nView your withdrawal records:\nhttp://localhost:3001/teacher/withdrawal\n\n— Mandarly Team',
 '["amount","currency"]', 0, 'M6 提现已通过 EN', 'system', 'system'),

(308, '提现-已通过-阿语', 'mandarly_withdrawal_approved_ar', 1, 'Mandarly',
 '[TODO ar] Mandarly · تمت الموافقة على السحب',
 '[TODO ar] مرحباً!\n\nأخبار سارّة! تمت الموافقة على طلب السحب الخاص بك.\n\nالمبلغ: {currency} {amount}\nالحالة: مُعتمد، في انتظار الدفع\n\n— Mandarly Team',
 '["amount","currency"]', 0, 'M6 提现已通过 AR (TODO)', 'system', 'system'),

-- ============ #3 已打款 ============
(309, '提现-已打款-简中', 'mandarly_withdrawal_paid_zh_cn', 1, 'Mandarly',
 'Mandarly · 提现已到账',
 '您好！\n\n您的 Mandarly 提现已打款完成,请注意查收。\n\n提现金额：{currency} {amount}\n当前状态：已打款\n打款备注：{paidRemark}\n\n如未在 1 个工作日内收到款项,请联系客服。\n\n查看提现记录:\nhttp://localhost:3001/teacher/withdrawal\n\n如有疑问请联系客服：support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency","paidRemark"]', 0, 'M6 提现已打款 ZH-CN', 'system', 'system'),

(310, '提现-已打款-繁中', 'mandarly_withdrawal_paid_zh_tw', 1, 'Mandarly',
 'Mandarly · 提現已到帳',
 '您好！\n\n您的 Mandarly 提現已打款完成,請注意查收。\n\n提現金額:{currency} {amount}\n當前狀態:已打款\n打款備註:{paidRemark}\n\n如未在 1 個工作日內收到款項,請聯絡客服。\n\nhttp://localhost:3001/teacher/withdrawal\n\n— Mandarly Team',
 '["amount","currency","paidRemark"]', 0, 'M6 提现已打款 ZH-TW', 'system', 'system'),

(311, '提现-已打款-英文', 'mandarly_withdrawal_paid_en', 1, 'Mandarly',
 'Mandarly · Withdrawal paid',
 'Hi there!\n\nYour Mandarly withdrawal has been paid out, please check your account.\n\nAmount: {currency} {amount}\nStatus: Paid\nNote: {paidRemark}\n\nIf you do not receive the funds within 1 business day, please contact customer support.\n\nView your withdrawal records:\nhttp://localhost:3001/teacher/withdrawal\n\nContact: support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency","paidRemark"]', 0, 'M6 提现已打款 EN', 'system', 'system'),

(312, '提现-已打款-阿语', 'mandarly_withdrawal_paid_ar', 1, 'Mandarly',
 '[TODO ar] Mandarly · تم صرف السحب',
 '[TODO ar] مرحباً!\n\nتم دفع طلب السحب الخاص بك.\n\nالمبلغ: {currency} {amount}\nالحالة: مدفوع\nملاحظة: {paidRemark}\n\n— Mandarly Team',
 '["amount","currency","paidRemark"]', 0, 'M6 提现已打款 AR (TODO)', 'system', 'system'),

-- ============ #4 已驳回 ============
(313, '提现-已驳回-简中', 'mandarly_withdrawal_rejected_zh_cn', 1, 'Mandarly',
 'Mandarly · 提现申请被驳回',
 '您好：\n\n很抱歉,您的 Mandarly 提现申请未通过审核。\n\n申请金额：{currency} {amount}\n当前状态：已驳回\n驳回原因：{rejectReason}\n\n您冻结的余额已退回,可重新申请提现。\n\n查看提现记录:\nhttp://localhost:3001/teacher/withdrawal\n\n如有疑问请联系客服：support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency","rejectReason"]', 0, 'M6 提现已驳回 ZH-CN', 'system', 'system'),

(314, '提现-已驳回-繁中', 'mandarly_withdrawal_rejected_zh_tw', 1, 'Mandarly',
 'Mandarly · 提現申請被駁回',
 '您好:\n\n很抱歉,您的 Mandarly 提現申請未通過審核。\n\n申請金額:{currency} {amount}\n當前狀態:已駁回\n駁回原因:{rejectReason}\n\n您凍結的餘額已退回,可重新申請提現。\n\nhttp://localhost:3001/teacher/withdrawal\n\n— Mandarly Team',
 '["amount","currency","rejectReason"]', 0, 'M6 提现已驳回 ZH-TW', 'system', 'system'),

(315, '提现-已驳回-英文', 'mandarly_withdrawal_rejected_en', 1, 'Mandarly',
 'Mandarly · Withdrawal rejected',
 'Hi there,\n\nUnfortunately, your Mandarly withdrawal request was not approved.\n\nAmount: {currency} {amount}\nStatus: Rejected\nReason: {rejectReason}\n\nThe held balance has been released back to your available balance, and you may submit a new withdrawal request.\n\nView your withdrawal records:\nhttp://localhost:3001/teacher/withdrawal\n\nContact: support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency","rejectReason"]', 0, 'M6 提现已驳回 EN', 'system', 'system'),

(316, '提现-已驳回-阿语', 'mandarly_withdrawal_rejected_ar', 1, 'Mandarly',
 '[TODO ar] Mandarly · تم رفض طلب السحب',
 '[TODO ar] مرحباً،\n\nللأسف، لم تتم الموافقة على طلب السحب الخاص بك.\n\nالمبلغ: {currency} {amount}\nالحالة: مرفوض\nالسبب: {rejectReason}\n\n— Mandarly Team',
 '["amount","currency","rejectReason"]', 0, 'M6 提现已驳回 AR (TODO)', 'system', 'system'),

-- ============ #5 打款失败 ============
(317, '提现-打款失败-简中', 'mandarly_withdrawal_failed_zh_cn', 1, 'Mandarly',
 'Mandarly · 提现打款失败',
 '您好：\n\n抱歉,您的 Mandarly 提现打款失败。\n\n申请金额：{currency} {amount}\n当前状态：打款失败\n失败原因：{failReason}\n\n您冻结的余额已退回,请检查收款信息后重新申请提现。\n\n查看提现记录:\nhttp://localhost:3001/teacher/withdrawal\n\n如有疑问请联系客服：support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency","failReason"]', 0, 'M6 提现失败 ZH-CN', 'system', 'system'),

(318, '提现-打款失败-繁中', 'mandarly_withdrawal_failed_zh_tw', 1, 'Mandarly',
 'Mandarly · 提現打款失敗',
 '您好:\n\n抱歉,您的 Mandarly 提現打款失敗。\n\n申請金額:{currency} {amount}\n當前狀態:打款失敗\n失敗原因:{failReason}\n\n您凍結的餘額已退回,請檢查收款資訊後重新申請提現。\n\nhttp://localhost:3001/teacher/withdrawal\n\n— Mandarly Team',
 '["amount","currency","failReason"]', 0, 'M6 提现失败 ZH-TW', 'system', 'system'),

(319, '提现-打款失败-英文', 'mandarly_withdrawal_failed_en', 1, 'Mandarly',
 'Mandarly · Withdrawal payout failed',
 'Hi there,\n\nWe are sorry, your Mandarly withdrawal payout failed.\n\nAmount: {currency} {amount}\nStatus: Payout failed\nReason: {failReason}\n\nThe held balance has been released back to your available balance. Please verify your payee details and submit a new request.\n\nView your withdrawal records:\nhttp://localhost:3001/teacher/withdrawal\n\nContact: support@mandarly.com\n\n— Mandarly Team',
 '["amount","currency","failReason"]', 0, 'M6 提现失败 EN', 'system', 'system'),

(320, '提现-打款失败-阿语', 'mandarly_withdrawal_failed_ar', 1, 'Mandarly',
 '[TODO ar] Mandarly · فشل صرف السحب',
 '[TODO ar] مرحباً،\n\nنأسف، فشل دفع طلب السحب الخاص بك.\n\nالمبلغ: {currency} {amount}\nالحالة: فشل الدفع\nالسبب: {failReason}\n\n— Mandarly Team',
 '["amount","currency","failReason"]', 0, 'M6 提现失败 AR (TODO)', 'system', 'system')

ON DUPLICATE KEY UPDATE
  name       = VALUES(name),
  title      = VALUES(title),
  content    = VALUES(content),
  params     = VALUES(params),
  status     = VALUES(status),
  account_id = VALUES(account_id),
  remark     = VALUES(remark),
  updater    = VALUES(updater);
