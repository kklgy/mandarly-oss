-- =============================================================================
-- Patch:M6 教师提现短信模板映射(复用现有过审 ISMS 海外通用模板)
-- =============================================================================
-- 来源:docs/product/M6-plan.md Phase D / Task D1
-- 现状:腾讯云 ISMS 通用「提现状态」模板已过审 2026-05-08
--   ZH api_template_id = 2638304
--   EN api_template_id = 2638303
-- 决策:不再申请新模板,5 状态共用 1 对通用模板,status 文案由 Java 层 i18n
--   (NotificationService)翻译后填入 {statusText} 占位变量(腾讯 {3})
-- =============================================================================
-- 影响:system_sms_template 表;新增 2 行
-- 关联:复用 patch 20260508_193000_add_isms_channel_template.sql 建的 TENCENT channel
-- 可重入:ON DUPLICATE KEY UPDATE 可重复执行
-- 回滚:DELETE FROM system_sms_template WHERE code IN
--      ('edu-withdrawal-status-zh','edu-withdrawal-status-en')
-- =============================================================================

-- 查找现有海外 ISMS channel id(签名 Mandarly,code=TENCENT)
SET @overseas_channel_id := (
    SELECT id FROM system_sms_channel
    WHERE code = 'TENCENT' AND status = 0 AND signature LIKE '%Mandarly%'
    ORDER BY id LIMIT 1
);

INSERT INTO system_sms_template
  (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, updater)
VALUES
  (2026051101, 1, 0, 'edu-withdrawal-status-zh',
   '教师提现-状态通知-中文',
   '您的 Mandarly 提现申请已更新,提现金额:{amount},币种:{currency},当前状态:{statusText}。',
   '["amount","currency","statusText"]',
   'M6 复用 ISMS 海外通道现有过审通用模板 2638304;status 文案由 Java NotificationService 翻译',
   '2638304', @overseas_channel_id, 'TENCENT', 'system', 'system'),

  (2026051102, 1, 0, 'edu-withdrawal-status-en',
   '教师提现-状态通知-英文',
   'Your Mandarly withdrawal updated. Amount: {amount}. Currency: {currency}. Status: {statusText}.',
   '["amount","currency","statusText"]',
   'M6 复用 ISMS 海外通道现有过审通用模板 2638303',
   '2638303', @overseas_channel_id, 'TENCENT', 'system', 'system')
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

-- =============================================================================
-- statusText 文案对照(供 NotificationService 实现参考)
--
-- | status enum | zh                       | en                                 |
-- |-------------|--------------------------|------------------------------------|
-- | applied     | 已申请,等待审核          | Submitted, pending review          |
-- | approved    | 已通过审核,等待打款       | Approved, awaiting payout          |
-- | paid        | 已打款完成                | Paid                               |
-- | rejected    | 已驳回                    | Rejected                           |
-- | failed      | 打款失败,余额已退回       | Payout failed, balance restored    |
-- =============================================================================
