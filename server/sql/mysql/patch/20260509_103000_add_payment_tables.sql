-- M4 Stripe 支付:6 张新表 + infra_config 6 配置 key
-- 上线后变更走本 patch,新库初始化走 mandarly.sql(同步维护)
-- 可重入:CREATE TABLE IF NOT EXISTS / INSERT ... WHERE NOT EXISTS

-- ============================================================
-- 1. payment 支付订单(套餐购买)
-- ============================================================
CREATE TABLE IF NOT EXISTS `payment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '学生 id',
  `type` VARCHAR(32) NOT NULL DEFAULT 'package_purchase',
  `package_id` BIGINT UNSIGNED NOT NULL,
  `student_package_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'webhook 成功后回填',
  `channel` VARCHAR(16) NOT NULL DEFAULT 'stripe',
  `channel_session_id` VARCHAR(128) DEFAULT NULL COMMENT 'cs_xxx',
  `channel_payment_intent_id` VARCHAR(128) DEFAULT NULL COMMENT 'pi_xxx',
  `channel_charge_id` VARCHAR(128) DEFAULT NULL COMMENT 'ch_xxx',
  `amount_request` DECIMAL(12, 2) NOT NULL,
  `currency_request` VARCHAR(8) NOT NULL DEFAULT 'USD',
  `amount_paid` DECIMAL(12, 2) DEFAULT NULL,
  `currency_paid` VARCHAR(8) DEFAULT NULL,
  `amount_settled_usd` DECIMAL(12, 2) DEFAULT NULL COMMENT 'Stripe balance_transaction 入账 USD',
  `discount_amount_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `referrer_user_id` BIGINT UNSIGNED DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT 'pending/paid/failed/expired/refunded/partial_refunded',
  `paid_at` DATETIME DEFAULT NULL,
  `expired_at` DATETIME DEFAULT NULL,
  `success_url` VARCHAR(512) NOT NULL,
  `cancel_url` VARCHAR(512) NOT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_session` (`channel_session_id`),
  KEY `idx_charge` (`channel_charge_id`),
  KEY `idx_referrer` (`referrer_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付订单(套餐购买)';

-- ============================================================
-- 2. refund 退款工单
-- ============================================================
CREATE TABLE IF NOT EXISTS `refund` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `payment_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `student_package_id` BIGINT UNSIGNED NOT NULL,
  `apply_reason` TEXT NOT NULL,
  `suggested_amount_usd` DECIMAL(12, 2) NOT NULL,
  `final_amount_usd` DECIMAL(12, 2) DEFAULT NULL,
  `adjust_reason` VARCHAR(512) DEFAULT NULL,
  `channel_refund_id` VARCHAR(128) DEFAULT NULL COMMENT 're_xxx',
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/refunded/rejected/failed',
  `audit_by` BIGINT DEFAULT NULL,
  `audit_at` DATETIME DEFAULT NULL,
  `audit_note` TEXT DEFAULT NULL,
  `refunded_at` DATETIME DEFAULT NULL,
  `teacher_income_deducted` TINYINT(1) NOT NULL DEFAULT 0,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_payment` (`payment_id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_created` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款工单';

-- ============================================================
-- 3. stripe_event Webhook 去重
-- ============================================================
CREATE TABLE IF NOT EXISTS `stripe_event` (
  `id` VARCHAR(64) NOT NULL COMMENT 'Stripe evt_xxx',
  `type` VARCHAR(64) NOT NULL,
  `payload` JSON NOT NULL,
  `processed_at` DATETIME NOT NULL,
  `result` VARCHAR(16) NOT NULL DEFAULT 'success' COMMENT 'success/ignored/failed',
  `error_msg` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_created` (`type`, `created_at`),
  KEY `idx_result_created` (`result`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Stripe webhook 事件去重 + 审计';

-- ============================================================
-- 4. teacher_income 教师收入流水(append-only)
-- ============================================================
CREATE TABLE IF NOT EXISTS `teacher_income` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `course_order_id` BIGINT UNSIGNED DEFAULT NULL,
  `refund_id` BIGINT UNSIGNED DEFAULT NULL,
  `amount_usd` DECIMAL(12, 2) NOT NULL COMMENT '扣回为负数',
  `type` VARCHAR(16) NOT NULL COMMENT 'normal/free_trial/refund_deduct',
  `settled_at` DATETIME NOT NULL COMMENT 'T+7 起算时点',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_type` (`course_order_id`, `type`),
  UNIQUE KEY `uk_refund_teacher` (`refund_id`, `teacher_id`),
  KEY `idx_teacher_settled` (`teacher_id`, `settled_at`),
  KEY `idx_settled` (`settled_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师收入流水(append-only)';

-- ============================================================
-- 5. teacher_balance 教师余额缓存
-- ============================================================
CREATE TABLE IF NOT EXISTS `teacher_balance` (
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `frozen_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `available_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `total_earned_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `total_withdrawn_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `last_rebuild_at` DATETIME NOT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师余额缓存(daily Job 重建)';

-- ============================================================
-- 6. referral_record 推荐关系 + 奖励
-- ============================================================
CREATE TABLE IF NOT EXISTS `referral_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `referrer_user_id` BIGINT UNSIGNED NOT NULL,
  `referee_user_id` BIGINT UNSIGNED NOT NULL,
  `referral_code` VARCHAR(16) NOT NULL,
  `payment_id` BIGINT UNSIGNED DEFAULT NULL,
  `referee_discount_amount_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `referrer_reward_package_id` BIGINT UNSIGNED DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL COMMENT 'bound/rewarded/voided',
  `bound_at` DATETIME NOT NULL,
  `rewarded_at` DATETIME DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_referee` (`referee_user_id`),
  KEY `idx_referrer_status` (`referrer_user_id`, `status`),
  KEY `idx_payment` (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='推荐关系 + 奖励记录';

-- ============================================================
-- infra_config 6 配置 key (M4 业务参数)
-- ⚠️ infra_config 表只有 `KEY idx_config_key`(非 unique),不能 ON DUPLICATE KEY UPDATE
-- 走 INSERT ... WHERE NOT EXISTS 模式,可重入
-- ============================================================
INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, '支付 Session 过期分钟', 'mandarly.payment.session_expires_minutes', '30', 1, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_config` WHERE `config_key` = 'mandarly.payment.session_expires_minutes');

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, 'Stripe 测试模式', 'mandarly.payment.test_mode', 'true', 1, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_config` WHERE `config_key` = 'mandarly.payment.test_mode');

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, '推荐码被推荐人减免 USD', 'mandarly.referral.referee_discount_usd', '4', 1, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_config` WHERE `config_key` = 'mandarly.referral.referee_discount_usd');

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, '推荐码推荐人奖励套餐 id', 'mandarly.referral.referrer_reward_package_id', '1', 1, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_config` WHERE `config_key` = 'mandarly.referral.referrer_reward_package_id');

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, '普通课课时费 USD', 'mandarly.income.teacher_lesson_fee_usd', '5', 1, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_config` WHERE `config_key` = 'mandarly.income.teacher_lesson_fee_usd');

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, '教师收入冻结天数', 'mandarly.income.frozen_days', '7', 1, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_config` WHERE `config_key` = 'mandarly.income.frozen_days');
