-- =============================================================================
-- Patch:Demo 套餐 price 数据修(2026-05-06)
-- =============================================================================
-- 来源:S0 浏览器联调发现 mandarly.sql §685 套餐 seed 价格全 0.00 是占位,
--       但 isFreeTrial=0 + price=0 + 26 节套餐组合让前端 free-trial 误判 +
--       与"半年/年套餐"业务语义矛盾。
-- 影响:6 条 package 记录,price 字段(其余字段不动);free_trial 仍保持 0
-- 价格策略(project owner未给最终价,先按行业参考价填占位,运营/project owner确认后再发 patch):
--   - half_year_1pw(26 节·180 天)  HKD 1280  ~50/节
--   - half_year_2pw(52 节·180 天)  HKD 2380  ~46/节
--   - year_1pw(52 节·365 天)       HKD 2480  ~48/节
--   - year_2pw(104 节·365 天)      HKD 4680  ~45/节
--   - single(1 节·7 天)            HKD  60   单课
--   - free_trial 保持 0(免费体验)
-- =============================================================================

UPDATE `package` SET `price` = 1280.00 WHERE `name_i18n_code` = 'package.name.half_year_1pw';
UPDATE `package` SET `price` = 2380.00 WHERE `name_i18n_code` = 'package.name.half_year_2pw';
UPDATE `package` SET `price` = 2480.00 WHERE `name_i18n_code` = 'package.name.year_1pw';
UPDATE `package` SET `price` = 4680.00 WHERE `name_i18n_code` = 'package.name.year_2pw';
UPDATE `package` SET `price` =   60.00 WHERE `name_i18n_code` = 'package.name.single';
-- free_trial 保持 0
