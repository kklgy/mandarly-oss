-- 修正生产系统邮件账号 SMTP 主机。
-- 背景:2026-05-12 P0 注册邮件未送达排查发现, system_mail_account.host 仍是旧
-- smtp.mxhichina.com, 而 no-reply@mandarly.com 实际应走企业邮箱 smtp.qiye.aliyun.com。
-- 注意:本 patch 不写入 SMTP 密码;密码继续从 secrets/.env 手工同步到 system_mail_account.password。

UPDATE system_mail_account
SET host = 'smtp.qiye.aliyun.com',
    port = 465,
    ssl_enable = b'1',
    starttls_enable = b'0',
    update_time = NOW()
WHERE id = 1
  AND mail = 'no-reply@mandarly.com'
  AND host <> 'smtp.qiye.aliyun.com';
