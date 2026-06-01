-- M2.5-β:腾讯云 ISMS 单通道 + 注册/登录 ZH/EN 4 模板
-- 签名 Mandarly 已过审 2026-05-08;模板 ID 见 docs/changelog/2026-05.md
-- 注:api_key (SDKAppID) 与 api_secret (APP_KEY) 是真凭证,不入 git;
--     本地需另行执行 server/sql/mysql/local-creds-bootstrap.sql.example
--     (复制为 .sql 后填入 .env.local 的真值,文件已 .gitignore)

-- 1. channel:把 mock 占位行 id=7 升级为生产 ISMS(签名 Mandarly,凭证仍占位)
UPDATE system_sms_channel
SET signature = 'Mandarly',
    code      = 'TENCENT',
    status    = 0,
    remark    = '腾讯云 ISMS 单通道(国际/港澳台 + 国内统一)SDKAppID 1400000000;真凭证由本地 bootstrap SQL 注入',
    updater   = 'system'
WHERE id = 7;

-- 2. templates:ZH/EN × register/login,channel_id=7,api_template_id 与腾讯云控制台一一对应
INSERT INTO system_sms_template
  (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, updater)
VALUES
  (2026050801, 1, 0, 'user-sms-register',    '会员-手机注册-中文', '您的 Mandarly 注册验证码:{code},5 分钟内有效,请勿告知他人。',          '["code"]', 'M2.5 β 腾讯云 ISMS ZH', '2638298', 7, 'TENCENT', 'system', 'system'),
  (2026050802, 1, 0, 'user-sms-register-en', '会员-手机注册-英文', 'Your Mandarly registration code is {code}, valid for 5 minutes. Do not share.', '["code"]', 'M2.5 β 腾讯云 ISMS EN', '2638297', 7, 'TENCENT', 'system', 'system'),
  (2026050803, 1, 0, 'user-sms-login',       '会员-手机登录-中文', '您的 Mandarly 登录验证码:{code},5 分钟内有效。',                       '["code"]', 'M2.5 β 腾讯云 ISMS ZH', '2638300', 7, 'TENCENT', 'system', 'system'),
  (2026050804, 1, 0, 'user-sms-login-en',    '会员-手机登录-英文', 'Your Mandarly login code is {code}, valid for 5 minutes. Do not share.',       '["code"]', 'M2.5 β 腾讯云 ISMS EN', '2638299', 7, 'TENCENT', 'system', 'system')
ON DUPLICATE KEY UPDATE
  status          = VALUES(status),
  name            = VALUES(name),
  content         = VALUES(content),
  api_template_id = VALUES(api_template_id),
  channel_id      = VALUES(channel_id),
  channel_code    = VALUES(channel_code),
  remark          = VALUES(remark),
  updater         = VALUES(updater);
