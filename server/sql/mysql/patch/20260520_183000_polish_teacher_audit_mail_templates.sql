-- 20260520_183000_polish_teacher_audit_mail_templates.sql
-- 目的:
--   1) 将教师资质审核通过/驳回邮件从 fragment-html 升级为完整 Mandarly 品牌 HTML 卡片。
--   2) 移除审核通过邮件标题开头 emoji,避免部分移动邮箱客户端显示"空标题"。
--   3) 邮件页脚补公司主体 / 注册地址,口径来自 docs/compliance/company-entity.md。
-- 回滚:
--   - 可重跑 20260518_180000_seed_teacher_audit_mail_templates.sql 恢复 D19 初始片段模板。

SET NAMES utf8mb4;

START TRANSACTION;

UPDATE system_mail_template
SET title = 'Mandarly - 教师资质审核通过',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">您在 Mandarly 提交的教师资质材料已审核通过。</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1v1 中文口语训练</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">教师资质审核已通过</h1>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">{nickname},您好,</p>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">您在 Mandarly 提交的教师资质材料已审核通过,即日起可以正式接收学生预约,开始你的教学之旅。</p>',
      '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">请登录教师中心查看排课、订单、收入等功能。</p>',
      '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">进入教师中心</a></p>',
      '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">祝教学愉快,<br>Mandarly 团队</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© 2026 曼德勵科技有限公司 保留所有权利。<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      '</div></div></body></html>'
    ),
    params = '["nickname"]',
    updater = '20260520_183000_teacher_audit_mail',
    update_time = NOW()
WHERE code = 'mandarly_teacher_audit_approved_zh_cn' AND deleted = b'0';

UPDATE system_mail_template
SET title = 'Mandarly - 教師資質審核通過',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">您在 Mandarly 提交的教師資質材料已審核通過。</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1v1 中文口語訓練</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">教師資質審核已通過</h1>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">{nickname},您好,</p>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">您在 Mandarly 提交的教師資質材料已審核通過,即日起可以正式接收學生預約,開始您的教學之旅。</p>',
      '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">請登入教師中心查看排課、訂單、收入等功能。</p>',
      '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">進入教師中心</a></p>',
      '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">祝教學愉快,<br>Mandarly 團隊</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© 2026 曼德勵科技有限公司 保留所有權利。<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      '</div></div></body></html>'
    ),
    params = '["nickname"]',
    updater = '20260520_183000_teacher_audit_mail',
    update_time = NOW()
WHERE code = 'mandarly_teacher_audit_approved_zh_tw' AND deleted = b'0';

UPDATE system_mail_template
SET title = 'Mandarly - Teacher qualification approved',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">Your Mandarly teacher qualification has been approved.</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">Your qualification has been approved</h1>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Hi {nickname},</p>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Your teacher qualification submitted on Mandarly has been approved. You can now start receiving student bookings and begin your teaching journey.</p>',
      '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">Log in to the Teacher Center to manage your schedule, orders, and earnings.</p>',
      '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">Open Teacher Center</a></p>',
      '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">Happy teaching,<br>The Mandarly Team</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© 2026 MANDARLY TECHNOLOGY LIMITED. All rights reserved.<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      '</div></div></body></html>'
    ),
    params = '["nickname"]',
    updater = '20260520_183000_teacher_audit_mail',
    update_time = NOW()
WHERE code = 'mandarly_teacher_audit_approved_en' AND deleted = b'0';

UPDATE system_mail_template
SET title = 'Mandarly - Teacher qualification approved',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">Your Mandarly teacher qualification has been approved.</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">Your qualification has been approved</h1>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Hi {nickname},</p>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Your teacher qualification submitted on Mandarly has been approved. You can now start receiving student bookings and begin your teaching journey.</p>',
      '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">Log in to the Teacher Center to manage your schedule, orders, and earnings.</p>',
      '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">Open Teacher Center</a></p>',
      '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">Happy teaching,<br>The Mandarly Team</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© 2026 MANDARLY TECHNOLOGY LIMITED. All rights reserved.<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      '</div></div></body></html>'
    ),
    params = '["nickname"]',
    updater = '20260520_183000_teacher_audit_mail',
    update_time = NOW()
WHERE code = 'mandarly_teacher_audit_approved_ar' AND deleted = b'0';

UPDATE system_mail_template
SET title = 'Mandarly - 教师资质审核结果',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">您在 Mandarly 提交的教师资质材料暂未通过审核。</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1v1 中文口语训练</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">教师资质审核结果</h1>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">{nickname},您好,</p>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">您在 Mandarly 提交的教师资质材料经审核暂未通过,原因如下:</p>',
      '<div style="margin:20px 0;padding:16px;border-left:4px solid #ef4444;background:#fff5f5;color:#7f1d1d;font-size:15px;line-height:1.8;">{rejectReason}</div>',
      '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">您可以在教师中心修改资料并重新提交审核。如有疑问可联系平台客服。</p>',
      '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center/profile-edit" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">修改教师资料</a></p>',
      '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">Mandarly 团队</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© 2026 曼德勵科技有限公司 保留所有权利。<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      '</div></div></body></html>'
    ),
    params = '["nickname","rejectReason"]',
    updater = '20260520_183000_teacher_audit_mail',
    update_time = NOW()
WHERE code = 'mandarly_teacher_audit_rejected_zh_cn' AND deleted = b'0';

UPDATE system_mail_template
SET title = 'Mandarly - 教師資質審核結果',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">您在 Mandarly 提交的教師資質材料暫未通過審核。</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1v1 中文口語訓練</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">教師資質審核結果</h1>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">{nickname},您好,</p>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">您在 Mandarly 提交的教師資質材料經審核暫未通過,原因如下:</p>',
      '<div style="margin:20px 0;padding:16px;border-left:4px solid #ef4444;background:#fff5f5;color:#7f1d1d;font-size:15px;line-height:1.8;">{rejectReason}</div>',
      '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">您可以在教師中心修改資料並重新提交審核。如有疑問可聯絡平台客服。</p>',
      '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center/profile-edit" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">修改教師資料</a></p>',
      '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">Mandarly 團隊</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© 2026 曼德勵科技有限公司 保留所有權利。<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      '</div></div></body></html>'
    ),
    params = '["nickname","rejectReason"]',
    updater = '20260520_183000_teacher_audit_mail',
    update_time = NOW()
WHERE code = 'mandarly_teacher_audit_rejected_zh_tw' AND deleted = b'0';

UPDATE system_mail_template
SET title = 'Mandarly - Teacher qualification review update',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">Your Mandarly teacher qualification was not approved at this time.</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">Teacher qualification review update</h1>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Hi {nickname},</p>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Your teacher qualification submitted on Mandarly was not approved at this time. Reason:</p>',
      '<div style="margin:20px 0;padding:16px;border-left:4px solid #ef4444;background:#fff5f5;color:#7f1d1d;font-size:15px;line-height:1.8;">{rejectReason}</div>',
      '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">You can update your information in the Teacher Center and resubmit for review. Contact support if you have questions.</p>',
      '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center/profile-edit" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">Update profile</a></p>',
      '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">The Mandarly Team</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© 2026 MANDARLY TECHNOLOGY LIMITED. All rights reserved.<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      '</div></div></body></html>'
    ),
    params = '["nickname","rejectReason"]',
    updater = '20260520_183000_teacher_audit_mail',
    update_time = NOW()
WHERE code = 'mandarly_teacher_audit_rejected_en' AND deleted = b'0';

UPDATE system_mail_template
SET title = 'Mandarly - Teacher qualification review update',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">Your Mandarly teacher qualification was not approved at this time.</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">Teacher qualification review update</h1>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Hi {nickname},</p>',
      '<p style="margin:0 0 18px;font-size:15px;line-height:1.8;color:#4b5563;">Your teacher qualification submitted on Mandarly was not approved at this time. Reason:</p>',
      '<div style="margin:20px 0;padding:16px;border-left:4px solid #ef4444;background:#fff5f5;color:#7f1d1d;font-size:15px;line-height:1.8;">{rejectReason}</div>',
      '<p style="margin:0 0 24px;font-size:15px;line-height:1.8;color:#4b5563;">You can update your information in the Teacher Center and resubmit for review. Contact support if you have questions.</p>',
      '<p style="margin:0 0 28px;"><a href="http://localhost:3001/teacher-center/profile-edit" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#ffb627;color:#1f2933;text-decoration:none;font-weight:700;">Update profile</a></p>',
      '<p style="margin:0;font-size:15px;line-height:1.8;color:#4b5563;">The Mandarly Team</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© 2026 MANDARLY TECHNOLOGY LIMITED. All rights reserved.<br>RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      '</div></div></body></html>'
    ),
    params = '["nickname","rejectReason"]',
    updater = '20260520_183000_teacher_audit_mail',
    update_time = NOW()
WHERE code = 'mandarly_teacher_audit_rejected_ar' AND deleted = b'0';

COMMIT;
