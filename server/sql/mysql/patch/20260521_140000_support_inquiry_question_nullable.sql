-- 20260521_140000_support_inquiry_question_nullable.sql
-- 目的:
--   允许 support_inquiry_log.question_text 为空,支撑"用户直接点客服联系方式"的纯点击日志。
--   Phase 1 D15-A 决策 SupportChatWidget 不做 FAQ 聊天 UI,但仍需要记录"转人工点击率"。
--   纯点击日志:question_text IS NULL AND clicked_to_human = 1 AND clicked_contact_id IS NOT NULL
-- 适用范围:
--   修改列约束,无数据迁移;现有 ask 写入路径不受影响(reqVO.question @NotBlank 已保证非空)。
-- 回滚:
--   ALTER TABLE support_inquiry_log MODIFY question_text VARCHAR(1024) NOT NULL;
--   (回滚前需先清理 question_text IS NULL 的行,否则无法 NOT NULL)

SET NAMES utf8mb4;

ALTER TABLE `support_inquiry_log`
  MODIFY COLUMN `question_text` VARCHAR(1024) DEFAULT NULL COMMENT '提问原文,纯渠道点击时为 NULL';
