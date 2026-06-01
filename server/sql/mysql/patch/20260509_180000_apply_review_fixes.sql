-- M4 Stripe 接入复核报告(docs/database/03-payment-review.md)修订增量 patch
-- 应用于已跑过 20260509_103000_add_payment_tables.sql 的库
-- 可重入(IF NOT EXISTS / DROP+ADD 兜底)
--
-- 修订 5 项必改:
-- 1. payment.currency_* VARCHAR(8) → VARCHAR(3)(ISO 4217)
-- 2. payment.payment_method_type 加列(card/wechat_pay/alipay/link/...)
-- 3. refund.is_active GENERATED + UNIQUE(payment_id, is_active) 防重审
-- 4. user_payment_profile 加表(Stripe Customer 映射,二期订阅/保存卡片预留)
-- 5. teacher_income.amount_usd 不变(M4 只 USD 单币种,无需改宽)

-- ============================================================
-- 1. payment.currency_* 改宽 + 加 payment_method_type
-- ============================================================
ALTER TABLE `payment`
  MODIFY COLUMN `currency_request` VARCHAR(3) NOT NULL DEFAULT 'USD',
  MODIFY COLUMN `currency_paid` VARCHAR(3) DEFAULT NULL;

-- payment_method_type 字段(MySQL 不支持 IF NOT EXISTS 加列,用 procedure 兜底)
DROP PROCEDURE IF EXISTS `add_payment_method_type_column`;
DELIMITER $$
CREATE PROCEDURE `add_payment_method_type_column`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment' AND COLUMN_NAME = 'payment_method_type'
  ) THEN
    ALTER TABLE `payment` ADD COLUMN `payment_method_type` VARCHAR(32) DEFAULT NULL
      COMMENT 'card/wechat_pay/alipay/link/...,webhook payment_intent.succeeded 时回填'
      AFTER `channel_charge_id`;
  END IF;
END$$
DELIMITER ;
CALL `add_payment_method_type_column`();
DROP PROCEDURE `add_payment_method_type_column`;

-- ============================================================
-- 2. refund 加 is_active GENERATED + UNIQUE(payment_id, is_active)
--    防并发审核同一 payment 创建多条 active refund
-- ============================================================
DROP PROCEDURE IF EXISTS `add_refund_is_active_column`;
DELIMITER $$
CREATE PROCEDURE `add_refund_is_active_column`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund' AND COLUMN_NAME = 'is_active'
  ) THEN
    ALTER TABLE `refund` ADD COLUMN `is_active` TINYINT(1) GENERATED ALWAYS AS
      (CASE WHEN status IN ('pending', 'approved') THEN 1 ELSE NULL END) STORED
      COMMENT '派生:active(pending/approved)=1,其他 NULL,配合 UNIQUE 防重审';
  END IF;
END$$
DELIMITER ;
CALL `add_refund_is_active_column`();
DROP PROCEDURE `add_refund_is_active_column`;

-- UNIQUE 利用 MySQL "唯一索引允许多个 NULL" 特性:
-- 同一 payment_id 在 active 状态(pending/approved)下只能有 1 条
-- non-active(refunded/rejected/failed)状态 is_active=NULL,不参与唯一性
DROP PROCEDURE IF EXISTS `add_refund_active_uk`;
DELIMITER $$
CREATE PROCEDURE `add_refund_active_uk`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund' AND INDEX_NAME = 'uk_payment_active'
  ) THEN
    ALTER TABLE `refund` ADD UNIQUE KEY `uk_payment_active` (`payment_id`, `is_active`);
  END IF;
END$$
DELIMITER ;
CALL `add_refund_active_uk`();
DROP PROCEDURE `add_refund_active_uk`;

-- ============================================================
-- 3. user_payment_profile (Stripe Customer 映射,M4 lazy create)
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_payment_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `channel` VARCHAR(16) NOT NULL DEFAULT 'stripe' COMMENT 'stripe/paypal/airwallex 二期扩展',
  `channel_customer_id` VARCHAR(128) NOT NULL COMMENT 'Stripe cus_xxx',
  `default_payment_method_id` VARCHAR(128) DEFAULT NULL COMMENT '保存卡片二期用,M4 不填',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_channel` (`user_id`, `channel`, `deleted`),
  KEY `idx_channel_customer` (`channel_customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户支付通道 customer 映射(Stripe Customer)';

-- ============================================================
-- 完成。Java 侧需配合改:
-- - PaymentDO 加 paymentMethodType 字段
-- - RefundDO 加 isActive 字段(GENERATED 列只读)
-- - 新建 UserPaymentProfileDO + Mapper(Phase 2 新增 Track 2E)
-- - StripeClient 加 createOrGetCustomer 方法(Phase 3 Track 3A 增补)
-- - PaymentService.createCheckout 在创建 Session 前 lazy create customer
-- - StripeWebhookHandler dispatch 加 6 个事件(charge.dispute.created/closed
--   + radar.early_fraud_warning.created + payment_intent.canceled/processing
--   + charge.refund.updated)
-- - PaymentService.handleCheckoutCompleted 末尾回填 paymentMethodType
-- ============================================================
