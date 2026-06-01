-- M2.5-C2:腾讯云国内短信通道占位(凭证审批中,status=1 disabled)
-- 资质 / 签名 / 模板审批见 docs/progress/M2.5-C2-国内短信通道.md
-- 凭证就位后切活:UPDATE channel id=8 + UPDATE 2 行模板的 api_template_id 与 status
--
-- 2026-05-11 N1 留尾 #3 备注:本 patch 依赖 system_sms_channel.signature ≥ VARCHAR(12)。
--   ruoyi-vue-pro.sql 全量 init 已同步扩到 VARCHAR(32);已存在库走 20260511_111358_fix_sms_signature_length.sql。
--   本 patch 内容不动(本机已成功 INSERT id=8;无 schema 操作)。

-- 1. channel id=8 国内通道(占位)
INSERT INTO system_sms_channel
  (id, signature, code, status, remark, api_key, api_secret, creator, updater)
VALUES
  (8,  '【Mandarly】', 'TENCENT', 1,
   '腾讯云国内短信(中国大陆)— 资质审批中,凭证 + 模板报备完成后 status=0 切活;参见 docs/progress/M2.5-C2-国内短信通道.md',
   'PENDING_SECRETID PENDING_SDKAPPID', 'PENDING_SECRETKEY',
   'system', 'system')
ON DUPLICATE KEY UPDATE
  signature = VALUES(signature),
  code      = VALUES(code),
  status    = VALUES(status),
  remark    = VALUES(remark);

-- 2. 国内 ZH 模板占位 — 注册 + 登录
INSERT INTO system_sms_template
  (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, updater)
VALUES
  (2026050811, 1, 1, 'user-sms-register-cn', '会员-手机注册-国内', '【Mandarly】您的注册验证码:{code},5 分钟内有效,请勿告知他人。', '["code"]', 'M2.5-C2 国内大陆通道占位,凭证就位后改 api_template_id', 'PENDING', 8, 'TENCENT', 'system', 'system'),
  (2026050812, 1, 1, 'user-sms-login-cn',    '会员-手机登录-国内', '【Mandarly】您的登录验证码:{code},5 分钟内有效。',                  '["code"]', 'M2.5-C2 国内大陆通道占位,凭证就位后改 api_template_id', 'PENDING', 8, 'TENCENT', 'system', 'system')
ON DUPLICATE KEY UPDATE
  status          = VALUES(status),
  name            = VALUES(name),
  content         = VALUES(content),
  api_template_id = VALUES(api_template_id),
  channel_id      = VALUES(channel_id),
  channel_code    = VALUES(channel_code),
  remark          = VALUES(remark);
