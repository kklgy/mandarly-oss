-- Patch: 配置默认真人客服微信号
-- Date: 2026-05-14
-- Scope:
--   1) 将 DEFAULT market 的 WeChat 联系方式配置为用户提供的微信号 I-KYC_ZJF-LOVE。
--   2) link_url 使用 app 端约定的 wechat:<id>,点击后复制微信号而不是打开外链。
-- Safe: 可重复执行;已有 DEFAULT/wechat 记录则更新,否则插入。

SET @wechat_id := 'I-KYC_ZJF-LOVE';
SET @wechat_link := CONCAT('wechat:', @wechat_id);
SET @wechat_display := CONCAT('微信客服:', @wechat_id);

UPDATE `support_contact`
SET `display_text` = @wechat_display,
    `link_url` = @wechat_link,
    `image_url` = NULL,
    `sort` = 10,
    `is_active` = 1,
    `updater` = 'system',
    `update_time` = NOW()
WHERE `market` = 'DEFAULT'
  AND `channel_type` = 'wechat'
  AND `deleted` = 0;

INSERT INTO `support_contact` (`market`, `channel_type`, `display_text`, `link_url`, `image_url`, `sort`, `is_active`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'DEFAULT', 'wechat', @wechat_display, @wechat_link, NULL, 10, 1, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM `support_contact`
  WHERE `market` = 'DEFAULT' AND `channel_type` = 'wechat' AND `deleted` = 0
);
