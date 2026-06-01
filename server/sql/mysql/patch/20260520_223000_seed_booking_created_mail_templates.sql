-- D22 - 2026-05-20
-- 目的:seed 学生预约成功后通知教师的邮件模板 4 locale。
-- 约束:
--   1) 对齐 20260520_183000_polish_teacher_audit_mail_templates.sql 的完整 Mandarly 品牌 HTML 卡片。
--   2) 标题统一以 "Mandarly - ..." 开头,避免移动邮箱客户端对符号 / emoji 标题兼容异常。
--   3) 页脚补公司主体 / 注册地址,口径来自 docs/compliance/company-entity.md。
-- 对应 NotificationServiceImpl.sendForBookingCreated 调 mailSendApi 时查表 code:
--   mandarly_booking_created_{zh_cn|zh_tw|en|ar}
-- 不可回滚:重复 import 会被 ON DUPLICATE KEY 兜住(code UNIQUE)

SET NAMES utf8mb4;

INSERT INTO system_mail_template
  (name, code, account_id, nickname, title, content, params, status, remark, creator, updater)
VALUES
  ('预约成功通知教师(简中)', 'mandarly_booking_created_zh_cn', 1, 'Mandarly',
   'Mandarly - 你有一节新的学生预约',
   CONCAT(
     '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
     '<div style="display:none;max-height:0;overflow:hidden;">学生 {studentName} 已预约您的 Mandarly 课程。</div>',
     '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
     '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
     '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
     '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1v1 中文口语训练</div>',
     '</div>',
     '<div style="padding:32px;">',
     '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">你有一节新的学生预约</h1>',
     '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">{teacherName},您好,</p>',
     '<p style="margin:0 0 20px;font-size:15px;line-height:1.8;color:#4b5563;">学生 <strong style="color:#1f2933;">{studentName}</strong> 已预约您的课程,请提前查看订单并准备上课。</p>',
     '<div style="margin:22px 0;padding:18px 20px;border-radius:12px;background:#fffaf0;border:1px solid #f0e6d8;">',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">上课时间(UTC):</strong> {scheduledAt}</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">课时:</strong> {duration} 分钟</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">学生邮箱:</strong> {studentEmail}</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">订单编号:</strong> {orderId}</div>',
     '</div>',
     '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">请登录教师中心查看订单详情与课堂入口。</p>',
     '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center/orders" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">查看教师订单</a></p>',
     '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">Mandarly 团队</p>',
     '</div>',
     '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
     'This is an automated email. Please do not reply.<br>© 2026 曼德勵科技有限公司 保留所有权利。<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
     '</div></div></body></html>'
   ),
   '["teacherName","studentName","studentEmail","scheduledAt","duration","orderId"]', 0, 'D22 seed full-html', 'd22-migration', 'd22-migration'),

  ('預約成功通知教師(繁中)', 'mandarly_booking_created_zh_tw', 1, 'Mandarly',
   'Mandarly - 您有一節新的學生預約',
   CONCAT(
     '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
     '<div style="display:none;max-height:0;overflow:hidden;">學生 {studentName} 已預約您的 Mandarly 課程。</div>',
     '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
     '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
     '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
     '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1v1 中文口語訓練</div>',
     '</div>',
     '<div style="padding:32px;">',
     '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">您有一節新的學生預約</h1>',
     '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">{teacherName},您好,</p>',
     '<p style="margin:0 0 20px;font-size:15px;line-height:1.8;color:#4b5563;">學生 <strong style="color:#1f2933;">{studentName}</strong> 已預約您的課程,請提前查看訂單並準備上課。</p>',
     '<div style="margin:22px 0;padding:18px 20px;border-radius:12px;background:#fffaf0;border:1px solid #f0e6d8;">',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">上課時間(UTC):</strong> {scheduledAt}</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">課時:</strong> {duration} 分鐘</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">學生電郵:</strong> {studentEmail}</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">訂單編號:</strong> {orderId}</div>',
     '</div>',
     '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">請登入教師中心查看訂單詳情與課堂入口。</p>',
     '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center/orders" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">查看教師訂單</a></p>',
     '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">Mandarly 團隊</p>',
     '</div>',
     '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
     'This is an automated email. Please do not reply.<br>© 2026 曼德勵科技有限公司 保留所有權利。<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
     '</div></div></body></html>'
   ),
   '["teacherName","studentName","studentEmail","scheduledAt","duration","orderId"]', 0, 'D22 seed full-html', 'd22-migration', 'd22-migration'),

  ('Booking created notification to teacher (EN)', 'mandarly_booking_created_en', 1, 'Mandarly',
   'Mandarly - You have a new student booking',
   CONCAT(
     '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
     '<div style="display:none;max-height:0;overflow:hidden;">{studentName} has booked a Mandarly lesson with you.</div>',
     '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
     '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
     '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
     '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
     '</div>',
     '<div style="padding:32px;">',
     '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">You have a new student booking</h1>',
     '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Hi {teacherName},</p>',
     '<p style="margin:0 0 20px;font-size:15px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">{studentName}</strong> has booked a lesson with you. Please review the order and prepare for class.</p>',
     '<div style="margin:22px 0;padding:18px 20px;border-radius:12px;background:#fffaf0;border:1px solid #f0e6d8;">',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">Class time (UTC):</strong> {scheduledAt}</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">Duration:</strong> {duration} minutes</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">Student email:</strong> {studentEmail}</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">Order ID:</strong> {orderId}</div>',
     '</div>',
     '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">Open Teacher Center to view the order details and classroom entry.</p>',
     '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center/orders" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">View teacher orders</a></p>',
     '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">The Mandarly Team</p>',
     '</div>',
     '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
     'This is an automated email. Please do not reply.<br>© 2026 MANDARLY TECHNOLOGY LIMITED. All rights reserved.<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
     '</div></div></body></html>'
   ),
   '["teacherName","studentName","studentEmail","scheduledAt","duration","orderId"]', 0, 'D22 seed full-html', 'd22-migration', 'd22-migration'),

  ('Booking created notification to teacher (AR)', 'mandarly_booking_created_ar', 1, 'Mandarly',
   'Mandarly - You have a new student booking',
   CONCAT(
     '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
     '<div style="display:none;max-height:0;overflow:hidden;">{studentName} has booked a Mandarly lesson with you.</div>',
     '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
     '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
     '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
     '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
     '</div>',
     '<div style="padding:32px;">',
     '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">You have a new student booking</h1>',
     '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Hi {teacherName},</p>',
     '<p style="margin:0 0 20px;font-size:15px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">{studentName}</strong> has booked a lesson with you. Please review the order and prepare for class.</p>',
     '<div style="margin:22px 0;padding:18px 20px;border-radius:12px;background:#fffaf0;border:1px solid #f0e6d8;">',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">Class time (UTC):</strong> {scheduledAt}</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">Duration:</strong> {duration} minutes</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">Student email:</strong> {studentEmail}</div>',
     '<div style="font-size:14px;line-height:1.8;color:#4b5563;"><strong style="color:#1f2933;">Order ID:</strong> {orderId}</div>',
     '</div>',
     '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">Open Teacher Center to view the order details and classroom entry.</p>',
     '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center/orders" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">View teacher orders</a></p>',
     '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">The Mandarly Team</p>',
     '</div>',
     '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
     'This is an automated email. Please do not reply.<br>© 2026 MANDARLY TECHNOLOGY LIMITED. All rights reserved.<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
     '</div></div></body></html>'
   ),
   '["teacherName","studentName","studentEmail","scheduledAt","duration","orderId"]', 0, 'D22 seed full-html - ar placeholder pending native review', 'd22-migration', 'd22-migration')
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content),
  params = VALUES(params),
  remark = VALUES(remark),
  updater = 'd22-migration',
  update_time = NOW();

INSERT INTO mandarly_patch_log (patch_file, executed_at, executor, note)
VALUES (
  '20260520_223000_seed_booking_created_mail_templates.sql',
  NOW(),
  'd22-migration',
  'D22 - seed booking created teacher full-html mail_template × 4 locale'
);
