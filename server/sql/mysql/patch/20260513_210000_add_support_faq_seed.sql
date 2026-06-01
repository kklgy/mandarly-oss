-- Patch: D3 客服体系简化版 — FAQ 初始数据 + 默认联系方式 + FAQ 阈值配置
-- Date: 2026-05-13
-- Scope:
--   1) faq/support_contact/support_inquiry_log 三表已在 mandarly.sql 设计中存在。
--   2) 本 patch 只补生产增量 seed 与 infra_config 阈值,不改 schema。
-- Safe: 可重复执行;所有 INSERT 均用 NOT EXISTS 守护。

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, 'FAQ 命中阈值', 'support.faq_match_threshold', '0.3', 1, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_config` WHERE `config_key` = 'support.faq_match_threshold' AND `deleted` = 0);

INSERT INTO `support_contact` (`market`, `channel_type`, `display_text`, `link_url`, `image_url`, `sort`, `is_active`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'DEFAULT', 'other', 'Email support', 'mailto:hello.com', NULL, 100, 1, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM `support_contact`
  WHERE `market` = 'DEFAULT' AND `channel_type` = 'other' AND `link_url` = 'mailto:hello.com' AND `deleted` = 0
);

INSERT INTO `faq` (`category`, `locale`, `question`, `answer`, `keywords`, `sort`, `status`, `view_count`, `match_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'package', 'zh-CN', '可以免费试听一节吗?', '可以。注册后可领取 1 节免费体验课,自由选择老师和时间。', JSON_ARRAY('免费试听','体验课','免费课','trial'), 10, 'active', 0, 0, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `faq` WHERE `locale` = 'zh-CN' AND `question` = '可以免费试听一节吗?' AND `deleted` = 0);

INSERT INTO `faq` (`category`, `locale`, `question`, `answer`, `keywords`, `sort`, `status`, `view_count`, `match_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'class', 'zh-CN', '怎么预约课程?', '进入老师列表或老师详情页,选择可预约时间后提交预约。开课前 30 分钟可进入课堂调试设备。', JSON_ARRAY('预约','上课','时间','课堂'), 20, 'active', 0, 0, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `faq` WHERE `locale` = 'zh-CN' AND `question` = '怎么预约课程?' AND `deleted` = 0);

INSERT INTO `faq` (`category`, `locale`, `question`, `answer`, `keywords`, `sort`, `status`, `view_count`, `match_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'refund', 'zh-CN', '不满意可以退款吗?', '课前 24 小时取消可退回课次。开课后的退款申请按服务条款处理。', JSON_ARRAY('退款','取消','不满意','refund'), 30, 'active', 0, 0, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `faq` WHERE `locale` = 'zh-CN' AND `question` = '不满意可以退款吗?' AND `deleted` = 0);

INSERT INTO `faq` (`category`, `locale`, `question`, `answer`, `keywords`, `sort`, `status`, `view_count`, `match_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'package', 'en', 'Can I take a free trial lesson?', 'Yes. After signing up, you can claim one free trial lesson and choose a tutor and time.', JSON_ARRAY('free trial','trial','free lesson'), 10, 'active', 0, 0, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `faq` WHERE `locale` = 'en' AND `question` = 'Can I take a free trial lesson?' AND `deleted` = 0);

INSERT INTO `faq` (`category`, `locale`, `question`, `answer`, `keywords`, `sort`, `status`, `view_count`, `match_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'class', 'en', 'How do I book a lesson?', 'Open the tutor list or tutor profile, choose an available time, and submit the booking. You can enter the classroom 30 minutes before class.', JSON_ARRAY('book','booking','lesson','classroom'), 20, 'active', 0, 0, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `faq` WHERE `locale` = 'en' AND `question` = 'How do I book a lesson?' AND `deleted` = 0);

INSERT INTO `faq` (`category`, `locale`, `question`, `answer`, `keywords`, `sort`, `status`, `view_count`, `match_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'package', 'zh-TW', '可以免費試聽一節嗎?', '可以。註冊後可領取 1 節免費體驗課,自由選擇老師和時間。', JSON_ARRAY('免費試聽','體驗課','免費課','trial'), 10, 'active', 0, 0, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `faq` WHERE `locale` = 'zh-TW' AND `question` = '可以免費試聽一節嗎?' AND `deleted` = 0);

INSERT INTO `faq` (`category`, `locale`, `question`, `answer`, `keywords`, `sort`, `status`, `view_count`, `match_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'package', 'ar', 'هل يمكنني تجربة درس مجاني؟', 'نعم. بعد إنشاء الحساب، يمكنك الحصول على درس تجريبي مجاني واختيار المعلم والوقت.', JSON_ARRAY('تجريبي','مجاني','درس'), 10, 'active', 0, 0, 'system', NOW(), 'system', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `faq` WHERE `locale` = 'ar' AND `question` = 'هل يمكنني تجربة درس مجاني؟' AND `deleted` = 0);
