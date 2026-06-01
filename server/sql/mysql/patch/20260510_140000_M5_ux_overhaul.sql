-- =============================================================================
-- M5 用户端 UX 大改造 — SQL patch(单文件,Wave 2/3/4 全字段一次性合并)
-- 来源: docs/frontend/visual-reference/DESIGN-mandarly-v1.md 12 轮设计稿
-- 执行时机: M5 上线前 staging 验证 → prod 一次性执行
--
-- ⚠️ ALTER TABLE 锁表注意(2026-05-10 plan review 修订):
--   - VARCHAR / INT / BOOLEAN / DATETIME 列加 INSTANT 不锁表(MySQL 8.0.29+)
--   - JSON 列 不走 INSTANT(行内长度不固定),会回退 INPLACE 重建表 → 锁写
--   - 生产前先在 staging 跑,确认锁写时间 < 30s 再上 prod;高峰期不上 patch
--   - JSON 列拆单独 ALTER 执行,与非 JSON 列分开
-- =============================================================================

-- ─── teacher_profile 表(Wave 2 第 3 轮 + Wave 4 第 6 轮)───
-- 非 JSON 列(走 INSTANT 不锁表)
ALTER TABLE teacher_profile
  ADD COLUMN intro_video_url VARCHAR(500) NULL COMMENT '试讲视频 URL,可空',
  ADD COLUMN recommend_weight INT NOT NULL DEFAULT 0 COMMENT '运营手动置顶推荐排序',
  ALGORITHM=INSTANT;

-- JSON 列(锁写,staging 验证 < 30s)
ALTER TABLE teacher_profile ADD COLUMN accent JSON COMMENT '口音 ["mandarin_cn","mandarin_tw","cantonese"];过渡期与 String 单值兼容,前后端 normalize';
ALTER TABLE teacher_profile ADD COLUMN tags JSON COMMENT '教师标签 ["beginner","kids","hasVideo"]';
-- today_available_count: 用 view 派生(TeacherStat),不冗余字段

-- 数据迁移(若 accent 既有 String 单值,升级到 JSON List):
-- UPDATE teacher_profile SET accent = JSON_ARRAY(accent_legacy_string) WHERE accent IS NULL AND accent_legacy_string IS NOT NULL;
-- (如 DO 字段名仍是 accent,需要先备份再 ALTER;具体迁移由后端 agent 在 staging 验证)

-- ─── package 表(Wave 4 第 7 轮)───
ALTER TABLE package
  ADD COLUMN show_on_list_priority INT NOT NULL DEFAULT 0 COMMENT 'PackageList 主推卡排序; >0 上主推区, 0 仅在对比表',
  ADD COLUMN recommendation_label VARCHAR(50) NULL COMMENT '推荐 pill i18n_message.code, null 不显示',
  ADD COLUMN discount_label VARCHAR(50) NULL COMMENT '折扣徽章 i18n_message.code, null 不显示',
  ALGORITHM=INSTANT;

-- selling_points 是 JSON,与上面 INSTANT 不能同 ALTER
ALTER TABLE package ADD COLUMN selling_points JSON NULL COMMENT '卖点 i18n_message.code 列表';

-- 默认数据回填(运营按需调整 priority)
-- UPDATE package SET show_on_list_priority = 1 WHERE name_i18n_code = 'package.halfYear.weekly1';
-- UPDATE package SET show_on_list_priority = 2, recommendation_label = 'packages.recommendationLabel.popular' WHERE name_i18n_code = 'package.halfYear.weekly2';
-- UPDATE package SET show_on_list_priority = 3, discount_label = 'packages.discountLabel.year10off' WHERE name_i18n_code = 'package.fullYear.weekly1';
-- UPDATE package SET show_on_list_priority = 4, discount_label = 'packages.discountLabel.year10off' WHERE name_i18n_code = 'package.fullYear.weekly2';

-- ─── i18n_message 配套种子(packages 推荐 / 折扣 / 卖点 4 语言)───
-- 由独立 patch 维护(若 i18n_message 表存在;不存在则前端 i18n locale 已处理)
-- INSERT INTO i18n_message (code, locale, message) VALUES
--   ('packages.recommendationLabel.popular', 'zh-CN', '推荐'),
--   ('packages.recommendationLabel.popular', 'en',    'Popular'),
--   ('packages.recommendationLabel.popular', 'zh-TW', '推薦'),
--   ('packages.recommendationLabel.popular', 'ar',    'الأكثر شعبية'),
--   ('packages.discountLabel.year10off',     'zh-CN', '9 折省 HK$ 200'),
--   ('packages.discountLabel.year10off',     'en',    'Save HK$200 (10% off)'),
--   ('packages.discountLabel.year10off',     'zh-TW', '9 折省 HK$ 200'),
--   ('packages.discountLabel.year10off',     'ar',    'وفّر 200 HK$ (خصم 10%)'),
--   ('packages.sellingPoint.freeBooking',    'zh-CN', '自由预约任选老师'),
--   ('packages.sellingPoint.freeBooking',    'en',    'Book any tutor freely'),
--   ('packages.sellingPoint.refund24h',      'zh-CN', '24h 前可全额退'),
--   ('packages.sellingPoint.refund24h',      'en',    'Full refund 24h+ before lesson'),
--   ('packages.sellingPoint.reminder',       'zh-CN', '课前提醒'),
--   ('packages.sellingPoint.reminder',       'en',    'Class reminders');

-- ─── booking 表 free trial 标识(Wave 4 PackageList 用)───
-- 当前实现走 student_package.source IN ('free_trial','register_grant'),无需 booking 表加字段
-- 若后续按 booking.is_trial 重构,在此处 ALTER:
-- ALTER TABLE booking
--   ADD COLUMN is_trial BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否免费体验课',
--   ALGORITHM=INSTANT;

-- ─── review 表(Wave 5 第 11 轮 ReviewFormView 步骤化)───
-- 设计稿: docs/frontend/visual-reference/DESIGN-mandarly-v1.md § ReviewFormView 步骤化(第 11 轮)
-- 字段:
--   - is_anonymous     匿名评价开关(student step3 勾选)
--   - editable_until_at 编辑窗口截止时间(create + 72h, 后端写入,前端 EditWindowProgress 读)
--   - custom_tags      用户自定义标签 JSON(最多 3 条 × 8 字符,与 review_tag_lib 路线 A 配套)
-- 注:旧版 EDIT_WINDOW = 24h 已废弃,改 72h 与设计稿对齐;ReviewServiceImpl 上线时同步把 Duration.ofHours(24) 改 72
ALTER TABLE review
  ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE COMMENT '匿名评价(true 时公开列表展示为"匿名学员")',
  ADD COLUMN editable_until_at DATETIME NULL COMMENT '编辑窗口截止时间(create + 72h),前端进度条用',
  ALGORITHM=INSTANT;

-- JSON 列单独 ALTER(不走 INSTANT)
ALTER TABLE review ADD COLUMN custom_tags JSON NULL COMMENT '用户自定义标签,最多 3 条 × 8 字符;不进 platform_config 字典';

-- 历史数据 backfill editable_until_at(create_time + 72h)
UPDATE review SET editable_until_at = DATE_ADD(create_time, INTERVAL 72 HOUR)
  WHERE editable_until_at IS NULL;

-- ─── review_tag_lib 路线 A:扩 platform_config['review.tag_dict']───
-- M3 mandarly.sql:499 已落 6 条:patient / native_accent / good_pace / well_prepared / audio_issue / late
-- Wave 5 第 11 轮设计稿追加 6 条正向 tag,共 12 条预设 tag(运营在 admin 端动态调整 — 二期)
-- 新增 codes:
--   interactive          互动好
--   good_material        选材合适
--   std_pronunciation    发音标准
--   humorous             幽默
--   encouraging          鼓励学生
--   proper_difficulty    课程难度合适
-- 注:platform_config 表 config_value 是 JSON 字符串,这里用 JSON_MERGE_PATCH 安全合并(MySQL 8.0+),
--     不覆盖 M3 已有 6 条,新追加 6 条;若已运行过本 patch 直接幂等(再运行不增字典内容)
UPDATE platform_config
SET config_value = JSON_MERGE_PATCH(
  CAST(config_value AS JSON),
  CAST('{
    "interactive":       {"i18n":"review.tag.interactive",       "category":"positive"},
    "good_material":     {"i18n":"review.tag.good_material",     "category":"positive"},
    "std_pronunciation": {"i18n":"review.tag.std_pronunciation", "category":"positive"},
    "humorous":          {"i18n":"review.tag.humorous",          "category":"positive"},
    "encouraging":       {"i18n":"review.tag.encouraging",       "category":"positive"},
    "proper_difficulty": {"i18n":"review.tag.proper_difficulty", "category":"positive"}
  }' AS JSON)
)
WHERE config_key = 'review.tag_dict';

-- 配套 i18n_message 4 语言种子(zh-CN/en/zh-TW/ar)在独立 patch:
-- 20260510_140100_M5_review_tag_i18n_append.sql(运营改文案不影响代码)
-- 注:Java 侧 ReviewServiceImpl#TAG_WHITELIST 同步加 6 个 code,否则 save 时 400

-- =============================================================================
-- 上线后验证(plan §11.12 JSON_CONTAINS 索引压力):
-- 数据量 < 5000 不加索引;> 5000 跑 EXPLAIN,> 200ms 加 generated column STORED + 索引:
-- ALTER TABLE teacher_profile
--   ADD COLUMN accent_mandarin_cn BOOLEAN GENERATED ALWAYS AS (JSON_CONTAINS(accent, '"mandarin_cn"')) STORED,
--   ADD INDEX idx_accent_mandarin_cn (accent_mandarin_cn);
-- (其它 accent 值同理)
-- =============================================================================

-- 标记 patch 已执行(M0 阶段表名 mandarly_patch_log 若已建)
-- INSERT INTO mandarly_patch_log (patch_file, executed_at)
-- VALUES ('20260510_140000_M5_ux_overhaul.sql', NOW());
