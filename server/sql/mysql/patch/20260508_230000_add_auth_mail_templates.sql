-- M2.5-γ:邮箱验证码模板接入(注册 / 登录 / 重置密码 × ZH/EN 共 6 个模板)
-- 凭证(SMTP_USERNAME / SMTP_PASSWORD)就位前 status=1 disabled,但 MailCodeServiceImpl 仍走 stdout 兜底,不影响 dev e2e
-- 凭证就位后:.env.local 配 SMTP 凭证 + 切 system_mail_account.password / username + 把模板 status=0 即真发
-- 详见 docs/progress/M2.5-登录改造.md §6.1 M2.5-γ

-- 1. 邮箱账号占位(id=1):阿里云企业邮箱(no-reply@mandarly.com),凭证占位待project owner提供授权码
INSERT INTO system_mail_account
  (id, mail, username, password, host, port, ssl_enable, starttls_enable, creator, updater)
VALUES
  (1, 'no-reply@mandarly.com', 'no-reply@mandarly.com', 'PENDING_SMTP_PASSWORD',
   'smtp.mxhichina.com', 465, b'1', b'0', 'system', 'system')
ON DUPLICATE KEY UPDATE
  mail     = VALUES(mail),
  username = VALUES(username),
  host     = VALUES(host),
  port     = VALUES(port);

-- 2. 6 个邮件模板 — auth-mail-{register,login,reset} × {zh, en}
--    title / content 都用 {code} 占位符,MailCodeServiceImpl 调用时传 templateParams={code: 真验证码}
--    nickname 为 None(走账号默认 from);params 字段填入 ["code"] 让 admin UI 也能编辑
INSERT INTO system_mail_template
  (id, name, code, account_id, nickname, title, content, params, status, remark, creator, updater)
VALUES
  (101, '认证-注册验证码-中文', 'auth-mail-register',     1, 'Mandarly',
   'Mandarly · 注册验证码',
   '欢迎注册 Mandarly 中文培训!\n\n您的注册验证码是:{code}\n\n5 分钟内有效,请勿告知他人。\n\n— Mandarly Team',
   '["code"]', 0, 'M2.5-γ 邮箱注册 ZH', 'system', 'system'),
  (102, '认证-注册验证码-英文', 'auth-mail-register-en',  1, 'Mandarly',
   'Mandarly · Registration Code',
   'Welcome to Mandarly!\n\nYour registration code is: {code}\n\nValid for 5 minutes. Do not share with anyone.\n\n— Mandarly Team',
   '["code"]', 0, 'M2.5-γ 邮箱注册 EN', 'system', 'system'),
  (103, '认证-登录验证码-中文', 'auth-mail-login',        1, 'Mandarly',
   'Mandarly · 登录验证码',
   '您的 Mandarly 登录验证码是:{code}\n\n5 分钟内有效。如非本人操作请忽略。\n\n— Mandarly Team',
   '["code"]', 0, 'M2.5-γ 邮箱登录 ZH', 'system', 'system'),
  (104, '认证-登录验证码-英文', 'auth-mail-login-en',     1, 'Mandarly',
   'Mandarly · Login Code',
   'Your Mandarly login code is: {code}\n\nValid for 5 minutes. Ignore this email if you did not request it.\n\n— Mandarly Team',
   '["code"]', 0, 'M2.5-γ 邮箱登录 EN', 'system', 'system'),
  (105, '认证-密码重置-中文',  'auth-mail-reset',        1, 'Mandarly',
   'Mandarly · 密码重置',
   '您正在重置 Mandarly 账户密码。\n\n验证码:{code}\n\n5 分钟内有效。如非本人操作请立即修改密码或联系客服。\n\n— Mandarly Team',
   '["code"]', 0, 'M2.5-γ 密码重置 ZH', 'system', 'system'),
  (106, '认证-密码重置-英文',  'auth-mail-reset-en',     1, 'Mandarly',
   'Mandarly · Password Reset',
   'You requested a Mandarly password reset.\n\nCode: {code}\n\nValid for 5 minutes. If this was not you, change your password immediately or contact support.\n\n— Mandarly Team',
   '["code"]', 0, 'M2.5-γ 密码重置 EN', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name      = VALUES(name),
  title     = VALUES(title),
  content   = VALUES(content),
  params    = VALUES(params),
  status    = VALUES(status),
  account_id = VALUES(account_id),
  remark    = VALUES(remark),
  updater   = VALUES(updater);
