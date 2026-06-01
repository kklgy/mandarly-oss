-- =============================================================================
-- Patch: Polish public teacher names and pricing display data (2026-05-25)
-- =============================================================================
-- Background:
--   - The public homepage still exposes test-like teacher display names:
--     "Maya Chen (Mock)", "10tec", and "kk_lgy".
--   - The pricing page should present the four main USD plans using the
--     reference per-lesson prices:
--       Starter 6.99 / Standard 5.99 / Annual 5.69 / Premium Annual 4.99.
--
-- Scope:
--   - Update only user.nickname for the existing public teacher accounts.
--   - Update package.price for the four main paid packages across USD/HKD/CNY.
--   - Keep free-trial and single-lesson packages unchanged.
--
-- Pricing notes:
--   - AppPackageController displays price_per_session as price / total_count,
--     rounded to 2 decimals with HALF_UP.
--   - USD year_1pw uses the reference billed total 295.68, which displays as
--     5.69 per lesson after rounding.
--   - HKD/CNY keep the existing seed conversion model: USD_HKD=7.8,
--     CNY_HKD=1.08.
--
-- Rollback:
--   - Restore the previous nicknames and package.price values if business
--     decides to revert this display/pricing snapshot.
-- =============================================================================

SET NAMES utf8mb4;

UPDATE `user`
SET `nickname` = 'Emily Chen',
    `updater` = 'patch:20260525_235500',
    `update_time` = NOW()
WHERE `email` = 'teacher.mock.maya.chen@mandarly.test'
  AND `role` = 'teacher'
  AND `deleted` = 0;

UPDATE `user`
SET `nickname` = '林子轩',
    `updater` = 'patch:20260525_235500',
    `update_time` = NOW()
WHERE `email` = 'kk_lgy@163.com'
  AND `role` = 'teacher'
  AND `deleted` = 0;

UPDATE `user`
SET `nickname` = '王嘉怡',
    `updater` = 'patch:20260525_235500',
    `update_time` = NOW()
WHERE `email` = '10tec@sina.com'
  AND `role` = 'teacher'
  AND `deleted` = 0;

UPDATE `package`
SET `price` = CASE `name_i18n_code`
        WHEN 'package.name.half_year_1pw' THEN 181.74
        WHEN 'package.name.half_year_2pw' THEN 311.48
        WHEN 'package.name.year_1pw' THEN 295.68
        WHEN 'package.name.year_2pw' THEN 518.96
        ELSE `price`
    END,
    `updater` = 'patch:20260525_235500',
    `update_time` = NOW()
WHERE `currency` = 'USD'
  AND `deleted` = 0
  AND `name_i18n_code` IN (
      'package.name.half_year_1pw',
      'package.name.half_year_2pw',
      'package.name.year_1pw',
      'package.name.year_2pw'
  );

UPDATE `package`
SET `price` = CASE `name_i18n_code`
        WHEN 'package.name.half_year_1pw' THEN 1417.57
        WHEN 'package.name.half_year_2pw' THEN 2429.54
        WHEN 'package.name.year_1pw' THEN 2306.30
        WHEN 'package.name.year_2pw' THEN 4047.89
        ELSE `price`
    END,
    `updater` = 'patch:20260525_235500',
    `update_time` = NOW()
WHERE `currency` = 'HKD'
  AND `deleted` = 0
  AND `name_i18n_code` IN (
      'package.name.half_year_1pw',
      'package.name.half_year_2pw',
      'package.name.year_1pw',
      'package.name.year_2pw'
  );

UPDATE `package`
SET `price` = CASE `name_i18n_code`
        WHEN 'package.name.half_year_1pw' THEN 1312.57
        WHEN 'package.name.half_year_2pw' THEN 2249.58
        WHEN 'package.name.year_1pw' THEN 2135.47
        WHEN 'package.name.year_2pw' THEN 3748.04
        ELSE `price`
    END,
    `updater` = 'patch:20260525_235500',
    `update_time` = NOW()
WHERE `currency` = 'CNY'
  AND `deleted` = 0
  AND `name_i18n_code` IN (
      'package.name.half_year_1pw',
      'package.name.half_year_2pw',
      'package.name.year_1pw',
      'package.name.year_2pw'
  );
