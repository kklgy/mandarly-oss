-- =============================================================================
-- Patch:套餐名 i18n_message 文案(2026-05-06)
-- =============================================================================
-- 来源:`docs/product/level-check-recommendation-v1.md` / mandarly.sql §685 套餐 seed
-- 影响:i18n_message 表,共 6 套餐 × 4 语言(en/zh-CN/zh-TW/ar)= 24 条
-- 依赖:mandarly.sql 全量初始化(i18n_message 表已存在)
-- 阿拉伯语为初版,精度有限,二期请人校对
-- =============================================================================

INSERT INTO `i18n_message` (`code`, `locale`, `text`, `category`) VALUES
('package.name.free_trial',     'en',    'Free trial',                                    'package'),
('package.name.free_trial',     'zh-CN', '免费体验',                                       'package'),
('package.name.free_trial',     'zh-TW', '免費體驗',                                       'package'),
('package.name.free_trial',     'ar',    'تجربة مجانية',                                  'package'),

('package.name.half_year_1pw',  'en',    'Half-year Plan · 1 class/week',                 'package'),
('package.name.half_year_1pw',  'zh-CN', '半年套餐 · 每周 1 节',                           'package'),
('package.name.half_year_1pw',  'zh-TW', '半年方案 · 每週 1 節',                           'package'),
('package.name.half_year_1pw',  'ar',    'خطة نصف سنوية · 1 درس/أسبوع',                  'package'),

('package.name.half_year_2pw',  'en',    'Half-year Plan · 2 classes/week',               'package'),
('package.name.half_year_2pw',  'zh-CN', '半年套餐 · 每周 2 节',                           'package'),
('package.name.half_year_2pw',  'zh-TW', '半年方案 · 每週 2 節',                           'package'),
('package.name.half_year_2pw',  'ar',    'خطة نصف سنوية · 2 دروس/أسبوع',                 'package'),

('package.name.year_1pw',       'en',    'Annual Plan · 1 class/week',                    'package'),
('package.name.year_1pw',       'zh-CN', '年度套餐 · 每周 1 节',                           'package'),
('package.name.year_1pw',       'zh-TW', '年度方案 · 每週 1 節',                           'package'),
('package.name.year_1pw',       'ar',    'خطة سنوية · 1 درس/أسبوع',                      'package'),

('package.name.year_2pw',       'en',    'Annual Plan · 2 classes/week',                  'package'),
('package.name.year_2pw',       'zh-CN', '年度套餐 · 每周 2 节',                           'package'),
('package.name.year_2pw',       'zh-TW', '年度方案 · 每週 2 節',                           'package'),
('package.name.year_2pw',       'ar',    'خطة سنوية · 2 دروس/أسبوع',                     'package'),

('package.name.single',         'en',    'Single Class',                                  'package'),
('package.name.single',         'zh-CN', '单课',                                           'package'),
('package.name.single',         'zh-TW', '單課',                                           'package'),
('package.name.single',         'ar',    'درس فردي',                                      'package');
