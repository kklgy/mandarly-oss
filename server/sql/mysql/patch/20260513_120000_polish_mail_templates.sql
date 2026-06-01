-- =============================================================================
-- Patch: D7.1 polish Mandarly mail templates
-- =============================================================================
-- Scope:
--   1) Auth verification templates use a branded HTML card with a clear code block.
--   2) Payment/referral/withdrawal templates keep their existing business text and
--      placeholders, wrapped in a restrained Mandarly HTML layout.
-- Idempotent:
--   Auth templates are overwritten by code. Business templates are wrapped only
--   when content is not already HTML.
-- Rollback:
--   Restore template content from database backup or rerun the previous plain-text
--   template patches for ids 101-106, 201-216, 301-320.
-- =============================================================================

SET NAMES utf8mb4;

UPDATE system_mail_template
SET name = '认证-注册验证码-中文',
    title = 'Mandarly - 注册验证码',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">您的 Mandarly 注册验证码是 {code}。</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1v1 中文口语训练</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">欢迎注册 Mandarly</h1>',
      '<p style="margin:0 0 20px;font-size:15px;line-height:1.8;color:#4b5563;">您的注册验证码是：</p>',
      '<div style="margin:24px 0;padding:24px;border-radius:14px;background:#202124;text-align:center;">',
      '<span style="font-size:36px;line-height:1;letter-spacing:12px;font-weight:700;color:#ffb627;">{code}</span>',
      '</div>',
      '<p style="margin:0 0 12px;font-size:15px;line-height:1.8;color:#4b5563;">验证码 5 分钟内有效，请勿告知他人。</p>',
      '<p style="margin:0;font-size:14px;line-height:1.8;color:#6b7280;">如果不是您本人操作，请忽略本邮件。</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© Mandarly',
      '</div></div></body></html>'
    ),
    params = '["code"]',
    updater = 'system',
    update_time = NOW()
WHERE code = 'auth-mail-register' AND deleted = b'0';

UPDATE system_mail_template
SET name = '认证-注册验证码-英文',
    title = 'Mandarly - Registration Code',
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="display:none;max-height:0;overflow:hidden;">Your Mandarly registration code is {code}.</div>',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
      '</div>',
      '<div style="padding:32px;">',
      '<h1 style="margin:0 0 16px;font-size:22px;line-height:1.4;color:#1f2933;">Welcome to Mandarly</h1>',
      '<p style="margin:0 0 20px;font-size:15px;line-height:1.8;color:#4b5563;">Your registration code is:</p>',
      '<div style="margin:24px 0;padding:24px;border-radius:14px;background:#202124;text-align:center;">',
      '<span style="font-size:36px;line-height:1;letter-spacing:12px;font-weight:700;color:#ffb627;">{code}</span>',
      '</div>',
      '<p style="margin:0 0 12px;font-size:15px;line-height:1.8;color:#4b5563;">This code is valid for 5 minutes. Please do not share it with anyone.</p>',
      '<p style="margin:0;font-size:14px;line-height:1.8;color:#6b7280;">If you did not request this code, please ignore this email.</p>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© Mandarly',
      '</div></div></body></html>'
    ),
    params = '["code"]',
    updater = 'system',
    update_time = NOW()
WHERE code = 'auth-mail-register-en' AND deleted = b'0';

UPDATE system_mail_template
SET name = '认证-登录验证码-中文',
    title = 'Mandarly - 登录验证码',
    content = REPLACE(
      REPLACE((SELECT content FROM (SELECT content FROM system_mail_template WHERE code = 'auth-mail-register' AND deleted = b'0') t), '欢迎注册 Mandarly', '登录 Mandarly'),
      '注册验证码',
      '登录验证码'
    ),
    params = '["code"]',
    updater = 'system',
    update_time = NOW()
WHERE code = 'auth-mail-login' AND deleted = b'0';

UPDATE system_mail_template
SET name = '认证-登录验证码-英文',
    title = 'Mandarly - Login Code',
    content = REPLACE(
      REPLACE((SELECT content FROM (SELECT content FROM system_mail_template WHERE code = 'auth-mail-register-en' AND deleted = b'0') t), 'Welcome to Mandarly', 'Log in to Mandarly'),
      'registration code',
      'login code'
    ),
    params = '["code"]',
    updater = 'system',
    update_time = NOW()
WHERE code = 'auth-mail-login-en' AND deleted = b'0';

UPDATE system_mail_template
SET name = '认证-密码重置-中文',
    title = 'Mandarly - 密码重置验证码',
    content = REPLACE(
      REPLACE((SELECT content FROM (SELECT content FROM system_mail_template WHERE code = 'auth-mail-register' AND deleted = b'0') t), '欢迎注册 Mandarly', '重置 Mandarly 密码'),
      '注册验证码',
      '密码重置验证码'
    ),
    params = '["code"]',
    updater = 'system',
    update_time = NOW()
WHERE code = 'auth-mail-reset' AND deleted = b'0';

UPDATE system_mail_template
SET name = '认证-密码重置-英文',
    title = 'Mandarly - Password Reset Code',
    content = REPLACE(
      REPLACE((SELECT content FROM (SELECT content FROM system_mail_template WHERE code = 'auth-mail-register-en' AND deleted = b'0') t), 'Welcome to Mandarly', 'Reset your Mandarly password'),
      'registration code',
      'password reset code'
    ),
    params = '["code"]',
    updater = 'system',
    update_time = NOW()
WHERE code = 'auth-mail-reset-en' AND deleted = b'0';

UPDATE system_mail_template
SET title = REPLACE(title, '[TODO ar] ', ''),
    content = CONCAT(
      '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
      '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
      '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
      '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
      '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
      '</div>',
      '<div style="padding:32px;font-size:15px;line-height:1.8;color:#4b5563;">',
      '<div style="white-space:pre-line;">',
      REPLACE(REPLACE(content, '[TODO ar] ', ''), '\n', '<br>'),
      '</div>',
      '</div>',
      '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
      'This is an automated email. Please do not reply.<br>© Mandarly',
      '</div></div></body></html>'
    ),
    updater = 'system',
    update_time = NOW()
WHERE deleted = b'0'
  AND content NOT LIKE '<!doctype html>%'
  AND (code LIKE 'mandarly_payment_%' OR code LIKE 'mandarly_withdrawal_%');
