-- =============================================================================
-- Patch:M2.5-C2 国内短信模板审核通过记录
-- =============================================================================
-- 背景:
--   2026-05-28 腾讯云国内短信实名资质 mandarly_1293083 已通过,签名
--   "四川医凌科技有限公司" 已报备成功。5 个国内正文模板均已审核通过。
--
-- 决策:
--   1) 国内模板与国际/港澳台 ISMS 模板隔离。
--   2) 国内模板与签名使用腾讯云国内短信,不复用国际/港澳台签名。
--   3) 本 patch 启用已审核通过模板,但不注入任何 Secret;生产通道凭证需
--      在服务器运行态配置。
--
-- 回滚:
--   UPDATE system_sms_template SET api_template_id = 'PENDING', status = 1
--   WHERE code IN ('user-sms-register-cn','user-sms-login-cn',
--                  'user-reset-password-cn','user-update-mobile-cn','edu-class-reminder-cn');
--   DELETE FROM system_sms_template
--   WHERE code IN ('user-reset-password-cn','user-update-mobile-cn','edu-class-reminder-cn');
-- =============================================================================

-- 1. 国内 channel id=8 更新为真实签名,凭证仍保持占位且禁用。
INSERT INTO system_sms_channel
  (id, signature, code, status, remark, api_key, api_secret, creator, updater)
VALUES
  (8, '四川医凌科技有限公司', 'TENCENT', 1,
   '腾讯云国内短信(中国大陆);资质/签名/正文模板已通过;生产凭证由服务器运行态配置后启用',
   'PENDING_SECRETID PENDING_SDKAPPID', 'PENDING_SECRETKEY',
   'system', 'system')
ON DUPLICATE KEY UPDATE
  signature = VALUES(signature),
  code      = VALUES(code),
  status    = 1,
  remark    = VALUES(remark),
  updater   = VALUES(updater);

-- 2. 注册 / 登录国内验证码模板:审核通过,启用模板映射。
INSERT INTO system_sms_template
  (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, updater)
VALUES
  (2026050811, 1, 0, 'user-sms-register-cn',
   '会员-手机注册-国内',
   '您的 Mandarly 注册验证码为{code},5分钟内有效,请勿泄露。',
   '["code"]',
   '腾讯云国内短信已审核通过模板 2652656',
   '2652656', 8, 'TENCENT', 'system', 'system'),
  (2026050812, 1, 0, 'user-sms-login-cn',
   '会员-手机登录-国内',
   '您的 Mandarly 登录验证码为{code},5分钟内有效。如非本人操作,请忽略。',
   '["code"]',
   '腾讯云国内短信已审核通过模板 2652654',
   '2652654', 8, 'TENCENT', 'system', 'system')
ON DUPLICATE KEY UPDATE
  status          = VALUES(status),
  name            = VALUES(name),
  content         = VALUES(content),
  params          = VALUES(params),
  api_template_id = VALUES(api_template_id),
  channel_id      = VALUES(channel_id),
  channel_code    = VALUES(channel_code),
  remark          = VALUES(remark),
  updater         = VALUES(updater);

-- 3. 重置密码 / 绑定换号 / 课前提醒国内模板:审核通过,启用模板映射。
INSERT INTO system_sms_template
  (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, updater)
VALUES
  (2026052801, 1, 0, 'user-reset-password-cn',
   '会员-重置密码-国内',
   '您正在重置 Mandarly 账户密码,验证码为{code},5分钟内有效。如非本人操作,请忽略。',
   '["code"]',
   '腾讯云国内短信已审核通过模板 2652657',
   '2652657', 8, 'TENCENT', 'system', 'system'),
  (2026052802, 1, 0, 'user-update-mobile-cn',
   '会员-绑定更换手机号-国内',
   '您正在绑定或更换 Mandarly 账户手机号,验证码为{code},5分钟内有效,请勿泄露。',
   '["code"]',
   '腾讯云国内短信已审核通过模板 2652658',
   '2652658', 8, 'TENCENT', 'system', 'system'),
  (2026052803, 2, 0, 'edu-class-reminder-cn',
   '课前提醒-国内',
   '您预约的 Mandarly 中文课即将开始,请提前进入 Mandarly 查看课堂信息。',
   '[]',
   '腾讯云国内通知短信已审核通过模板 2652664;通知类无变量',
   '2652664', 8, 'TENCENT', 'system', 'system')
ON DUPLICATE KEY UPDATE
  status          = VALUES(status),
  name            = VALUES(name),
  content         = VALUES(content),
  params          = VALUES(params),
  api_template_id = VALUES(api_template_id),
  channel_id      = VALUES(channel_id),
  channel_code    = VALUES(channel_code),
  remark          = VALUES(remark),
  updater         = VALUES(updater);
