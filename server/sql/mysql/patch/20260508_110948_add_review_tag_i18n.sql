-- =============================================================================
-- Patch:M3 评价 review.tag.* i18n 文案(2026-05-08)
-- =============================================================================
-- 来源:`docs/product/prd-v4.md` §S7 评价 + `platform_config.review.tag_dict`(已配 6 个 tag i18n key)
-- 影响:i18n_message 表,共 6 个 tag × 4 语言(zh-CN / zh-TW / en / ar)= 24 条
-- 依赖:platform_config.review.tag_dict 已就位(mandarly.sql:707)
-- 对齐:levelcheck/package patch 已支持 4 语(ar 为初版,二期校对)
-- =============================================================================

INSERT INTO `i18n_message` (`code`, `locale`, `text`, `category`) VALUES
-- 耐心
('review.tag.patient',        'zh-CN', '耐心',           'review'),
('review.tag.patient',        'zh-TW', '耐心',           'review'),
('review.tag.patient',        'en',    'Patient',        'review'),
('review.tag.patient',        'ar',    'صبور',           'review'),

-- 口音地道
('review.tag.native_accent',  'zh-CN', '口音地道',       'review'),
('review.tag.native_accent',  'zh-TW', '口音道地',       'review'),
('review.tag.native_accent',  'en',    'Native accent',  'review'),
('review.tag.native_accent',  'ar',    'لكنة أصلية',     'review'),

-- 节奏好
('review.tag.good_pace',      'zh-CN', '节奏好',         'review'),
('review.tag.good_pace',      'zh-TW', '節奏好',         'review'),
('review.tag.good_pace',      'en',    'Good pace',      'review'),
('review.tag.good_pace',      'ar',    'إيقاع جيد',      'review'),

-- 备课充分
('review.tag.well_prepared',  'zh-CN', '备课充分',       'review'),
('review.tag.well_prepared',  'zh-TW', '備課充分',       'review'),
('review.tag.well_prepared',  'en',    'Well prepared',  'review'),
('review.tag.well_prepared',  'ar',    'محضر جيداً',     'review'),

-- 声音问题
('review.tag.audio_issue',    'zh-CN', '声音问题',       'review'),
('review.tag.audio_issue',    'zh-TW', '聲音問題',       'review'),
('review.tag.audio_issue',    'en',    'Audio issue',    'review'),
('review.tag.audio_issue',    'ar',    'مشكلة في الصوت', 'review'),

-- 迟到
('review.tag.late',           'zh-CN', '迟到',           'review'),
('review.tag.late',           'zh-TW', '遲到',           'review'),
('review.tag.late',           'en',    'Late',           'review'),
('review.tag.late',           'ar',    'متأخر',          'review');
