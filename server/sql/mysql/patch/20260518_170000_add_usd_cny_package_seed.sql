-- =============================================================================
-- Patch: D10 补 USD/CNY 初始可售套餐 seed (2026-05-18)
-- =============================================================================
-- 背景:
--   - 生产套餐当前只有 HKD 行;前端切 USD/CNY 时公开套餐列表为空。
--   - package 表设计为“一行一个币种价格”,后端按 package.currency 过滤,
--     Stripe Checkout 也按该行套餐自身 currency 收款。
--
-- 策略:
--   - 基于现有 HKD SKU 补 USD/CNY 初始可售行,复用 name_i18n_code。
--   - 价格按当前平台配置汇率 USD_HKD=7.8 / CNY_HKD=1.08 折算后做业务取整,
--     作为上线初始值;后台套餐管理可继续增删改/下架。
--   - 本 patch 幂等:同一 name_i18n_code + currency 存在未删除行时不重复插入。
--
-- 价格快照:
--   HKD 60 / 1280 / 2380 / 2480 / 4680
--   USD 8  / 164  / 305  / 318  / 600
--   CNY 56 / 1185 / 2204 / 2296 / 4333
-- =============================================================================

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.single', NULL, 1, 7, 8.00, 'USD', 0, 1, 150, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.single' AND `currency` = 'USD' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.half_year_1pw', 1, 26, 180, 164.00, 'USD', 0, 1, 110, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.half_year_1pw' AND `currency` = 'USD' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.half_year_2pw', 2, 52, 180, 305.00, 'USD', 0, 1, 120, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.half_year_2pw' AND `currency` = 'USD' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.year_1pw', 1, 52, 365, 318.00, 'USD', 0, 1, 130, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.year_1pw' AND `currency` = 'USD' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.year_2pw', 2, 104, 365, 600.00, 'USD', 0, 1, 140, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.year_2pw' AND `currency` = 'USD' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.single', NULL, 1, 7, 56.00, 'CNY', 0, 1, 250, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.single' AND `currency` = 'CNY' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.half_year_1pw', 1, 26, 180, 1185.00, 'CNY', 0, 1, 210, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.half_year_1pw' AND `currency` = 'CNY' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.half_year_2pw', 2, 52, 180, 2204.00, 'CNY', 0, 1, 220, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.half_year_2pw' AND `currency` = 'CNY' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.year_1pw', 1, 52, 365, 2296.00, 'CNY', 0, 1, 230, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.year_1pw' AND `currency` = 'CNY' AND `deleted` = 0
);

INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`, `creator`, `updater`, `tenant_id`)
SELECT 'package.name.year_2pw', 2, 104, 365, 4333.00, 'CNY', 0, 1, 240, 'system', 'system', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `package` WHERE `name_i18n_code` = 'package.name.year_2pw' AND `currency` = 'CNY' AND `deleted` = 0
);
