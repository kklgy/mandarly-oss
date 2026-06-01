-- M2.5-β / C2 本地腾讯云短信真凭证注入(模板,凭证占位,文件可入 git)
--
-- 双通道结构(channel 行已由 patch 预占,本文件只写 UPDATE 切活):
--   channel id=7 ISMS 国际(海外 + 港澳台,签名 Mandarly,主账号 SecretId/Key 共用)
--   channel id=8 国内大陆(中国大陆 +86,签名 【Mandarly】,独立 SDKAppID,资质另审)
--
-- 用法:
--   1. cp m2.5-isms-bootstrap.example.sql m2.5-isms-bootstrap.local.sql
--      (.local.sql 后缀已 .gitignore,真凭证不会被提交)
--   2. 替换占位符为 .env.local 的:
--        TENCENT_SECRET_ID         (主账号 SecretId,腾讯云 API V3 签名,两通道共用)
--        TENCENT_SECRET_KEY        (主账号 SecretKey,两通道共用)
--        TENCENT_SMS_SDK_APP_ID    (ISMS 国际版应用 SDKAppID,如 1400000000 → channel 7)
--        TENCENT_SMS_CN_SDK_APP_ID (国内大陆版应用 SDKAppID,资质审批后产生 → channel 8)
--      (TENCENT_SMS_APP_KEY 是 ISMS 回调签名 key,不参与 API V3 调用,本表不存)
--   3. 在本机执行:
--      mysql -h 127.0.0.1 -P 3307 -u root -p123456 mandarly < server/sql/mysql/m2.5-isms-bootstrap.local.sql
--
-- 生效条件:
--   - patch 20260508_193000_add_isms_channel_template.sql 已先执行(channel id=7 ISMS 已存在)
--   - patch 20260508_220000_add_domestic_sms_channel.sql 已先执行(channel id=8 CN 占位 + -cn 模板已存在)
-- 改完 channel 后需重启后端或调 admin API 刷新 SmsClientFactory 缓存,才能生效。
--
-- 注:yudao TencentSmsClient 把 SecretId 与 SmsSdkAppId 拼到 api_key 一个字段(空格分隔),两通道同样格式。

-- ====================== ISMS 国际通道(channel id=7) ======================
UPDATE system_sms_channel
SET api_key    = '<SecretId> <SmsSdkAppId>',
    api_secret = '<SecretKey>',
    updater    = 'local-bootstrap'
WHERE id = 7;

-- ====================== 国内大陆通道(channel id=8,M2.5-C2) ======================
-- 资质 / 签名 / 模板报备见 docs/progress/M2.5-C2-国内短信通道.md;凭证就位前 status=1 维持 disabled。
-- 切活步骤:① 改 api_key/api_secret 为真值;② 把 status 改 0(启用);③ 把两条 -cn 模板的 api_template_id 改为腾讯返回的真模板 ID + status=0。
UPDATE system_sms_channel
SET api_key    = '<SecretId> <SmsCnSdkAppId>',
    api_secret = '<SecretKey>',
    -- status   = 0,             -- 凭证 + 模板报备完成后再放开
    updater    = 'local-bootstrap'
WHERE id = 8;

-- 模板 id 切活(凭证就位、运营商报备通过后,把 PENDING 换成腾讯返回的真模板 ID,并放开 status):
-- UPDATE system_sms_template SET api_template_id = '<真模板ID-注册>', status = 0, updater = 'local-bootstrap' WHERE code = 'user-sms-register-cn';
-- UPDATE system_sms_template SET api_template_id = '<真模板ID-登录>', status = 0, updater = 'local-bootstrap' WHERE code = 'user-sms-login-cn';
