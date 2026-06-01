-- D19 Phase B 续 - 2026-05-18
-- 目的: seed 教师资质审核通过/驳回 邮件模板 4 locale × 2 event = 8 行
-- 对应 NotificationServiceImpl.sendForTeacherAuditEvent 调 mailSendApi 时查表 code:
--   approve: mandarly_teacher_audit_approved_{zh_cn|zh_tw|en|ar}
--   reject : mandarly_teacher_audit_rejected_{zh_cn|zh_tw|en|ar}
-- 沿用 withdrawal 同款 locale 后缀 + system_mail_template schema
-- 不可回滚: 重复 import 会被 ON DUPLICATE KEY 兜住(code UNIQUE)

SET NAMES utf8mb4;

-- =========================================================================
-- 邮件模板 schema 参考(ruoyi system_mail_template 表):
--   name / code / account_id / nickname / title / content / params / status
-- =========================================================================

-- ----------- approved × 4 locale -----------

INSERT INTO system_mail_template
  (name, code, account_id, nickname, title, content, params, status, remark, creator, updater)
VALUES
  ('教师资质审核通过(简中)', 'mandarly_teacher_audit_approved_zh_cn', 1, 'Mandarly',
   '🎉 你的教师资质已通过审核',
   '<p>{nickname},您好,</p><p>您在 Mandarly 提交的教师资质材料已审核通过,即日起可以正式接收学生预约,开始你的教学之旅。</p><p>请登录教师中心查看排课、订单、收入等功能:<a href="http://localhost:3001/teacher-center">http://localhost:3001/teacher-center</a></p><p>祝教学愉快,<br/>Mandarly 团队</p>',
   '["nickname"]', 0, 'D19 seed', 'd19-migration', 'd19-migration'),

  ('教师资质审核通过(繁中)', 'mandarly_teacher_audit_approved_zh_tw', 1, 'Mandarly',
   '🎉 您的教師資質已通過審核',
   '<p>{nickname},您好,</p><p>您在 Mandarly 提交的教師資質材料已審核通過,即日起可以正式接收學生預約,開始您的教學之旅。</p><p>請登入教師中心查看排課、訂單、收入等功能:<a href="http://localhost:3001/teacher-center">http://localhost:3001/teacher-center</a></p><p>祝教學愉快,<br/>Mandarly 團隊</p>',
   '["nickname"]', 0, 'D19 seed', 'd19-migration', 'd19-migration'),

  ('教师资质审核通过(英)', 'mandarly_teacher_audit_approved_en', 1, 'Mandarly',
   '🎉 Your teacher qualification has been approved',
   '<p>Hi {nickname},</p><p>Your teacher qualification submitted on Mandarly has been approved. You can now start receiving student bookings and begin your teaching journey.</p><p>Log in to the Teacher Center to manage your schedule, orders, and earnings: <a href="http://localhost:3001/teacher-center">http://localhost:3001/teacher-center</a></p><p>Happy teaching,<br/>The Mandarly Team</p>',
   '["nickname"]', 0, 'D19 seed', 'd19-migration', 'd19-migration'),

  ('教师资质审核通过(阿)', 'mandarly_teacher_audit_approved_ar', 1, 'Mandarly',
   '🎉 Your teacher qualification has been approved',
   '<p>Hi {nickname},</p><p>Your teacher qualification submitted on Mandarly has been approved. You can now start receiving student bookings and begin your teaching journey.</p><p>Log in to the Teacher Center to manage your schedule, orders, and earnings: <a href="http://localhost:3001/teacher-center">http://localhost:3001/teacher-center</a></p><p>Happy teaching,<br/>The Mandarly Team</p>',
   '["nickname"]', 0, 'D19 seed - ar placeholder pending native review', 'd19-migration', 'd19-migration'),

-- ----------- rejected × 4 locale -----------

  ('教师资质审核驳回(简中)', 'mandarly_teacher_audit_rejected_zh_cn', 1, 'Mandarly',
   '关于您在 Mandarly 教师资质审核的结果',
   '<p>{nickname},您好,</p><p>您在 Mandarly 提交的教师资质材料经审核暂未通过,原因如下:</p><blockquote>{rejectReason}</blockquote><p>您可以在<a href="http://localhost:3001/teacher-center/profile-edit">教师中心 → 我的档案</a>修改资料并重新提交审核。如有疑问可联系平台客服。</p><p>Mandarly 团队</p>',
   '["nickname","rejectReason"]', 0, 'D19 seed', 'd19-migration', 'd19-migration'),

  ('教师资质审核驳回(繁中)', 'mandarly_teacher_audit_rejected_zh_tw', 1, 'Mandarly',
   '關於您在 Mandarly 教師資質審核的結果',
   '<p>{nickname},您好,</p><p>您在 Mandarly 提交的教師資質材料經審核暫未通過,原因如下:</p><blockquote>{rejectReason}</blockquote><p>您可以在<a href="http://localhost:3001/teacher-center/profile-edit">教師中心 → 我的檔案</a>修改資料並重新提交審核。如有疑問可聯絡平台客服。</p><p>Mandarly 團隊</p>',
   '["nickname","rejectReason"]', 0, 'D19 seed', 'd19-migration', 'd19-migration'),

  ('教师资质审核驳回(英)', 'mandarly_teacher_audit_rejected_en', 1, 'Mandarly',
   'Update on your Mandarly teacher qualification review',
   '<p>Hi {nickname},</p><p>Your teacher qualification submitted on Mandarly was not approved at this time. Reason:</p><blockquote>{rejectReason}</blockquote><p>You can update your information in <a href="http://localhost:3001/teacher-center/profile-edit">Teacher Center → My Profile</a> and resubmit for review. Contact support if you have questions.</p><p>The Mandarly Team</p>',
   '["nickname","rejectReason"]', 0, 'D19 seed', 'd19-migration', 'd19-migration'),

  ('教师资质审核驳回(阿)', 'mandarly_teacher_audit_rejected_ar', 1, 'Mandarly',
   'Update on your Mandarly teacher qualification review',
   '<p>Hi {nickname},</p><p>Your teacher qualification submitted on Mandarly was not approved at this time. Reason:</p><blockquote>{rejectReason}</blockquote><p>You can update your information in <a href="http://localhost:3001/teacher-center/profile-edit">Teacher Center → My Profile</a> and resubmit for review. Contact support if you have questions.</p><p>The Mandarly Team</p>',
   '["nickname","rejectReason"]', 0, 'D19 seed - ar placeholder pending native review', 'd19-migration', 'd19-migration')

ON DUPLICATE KEY UPDATE
  title  = VALUES(title),
  content = VALUES(content),
  params = VALUES(params),
  updater = 'd19-migration';

-- mandarly_patch_log 登记
INSERT INTO mandarly_patch_log (patch_file, executed_at, executor, note)
VALUES (
  '20260518_180000_seed_teacher_audit_mail_templates.sql',
  NOW(),
  'd19-migration',
  'D19 Phase B 续 - seed 教师审核通过/驳回 mail_template × 4 locale × 2 event = 8 行'
);
