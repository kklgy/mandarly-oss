-- =============================================================================
-- 🚨 LOCAL DEV ONLY — 文件名后缀 `_local-only`,自动化部署脚本必须 glob 排除
-- 🚨 staging / prod 全空 DB **不要跑本补丁**,走主 patch + unique patch 标准流程
-- =============================================================================
--
-- M5 主 patch 部分应用修补 — 补主 patch 漏掉的字段 + 数据 + log
--
-- 背景(2026-05-10 hot patch staging 准备):
--   主 patch `20260510_140000_M5_ux_overhaul.sql` 因 `ALGORITHM=INSTANT, LOCK=NONE`
--   并用(MySQL 1221 错)在本地 dev DB 部分应用,patch_log 无记录,字段 / 数据
--   落地不全。本补丁仅补**本地 dev DB**这种"部分应用 + 无 log"状态。
--
--   主 patch 已同时修复(去掉 LOCK=NONE),staging / prod 全空 DB 直接跑修订后
--   的主 patch 即可全字段落地,无需本补丁。
--
-- 本地 dev DB 实测落地状态(2026-05-10 18:39):
--   teacher_profile: 已有 intro_video_url + accent;缺 recommend_weight + tags
--   package:        所有 4 字段全缺(show_on_list_priority / recommendation_label
--                   / discount_label / selling_points)
--   review:         所有 3 字段全缺(is_anonymous / editable_until_at / custom_tags)
--   platform_config review.tag_dict: 6 条 M3 原版,缺 Wave 5 新增 6 条(共应 12)
--   patch_log:      只有 20260510_150000(OPTIONAL unique),缺主 patch log
--
-- 幂等性:
--   - ALTER TABLE ADD COLUMN 不幂等,本补丁仅添加确认缺失的字段;若 staging /
--     prod DB 状态不同(例如主 patch 全应用),需先核对 §1.2-1.5,**不可盲跑**
--   - platform_config JSON_MERGE_PATCH 幂等(主 patch 设计原文,重跑安全)
--   - INSERT 主 patch log 用 INSERT IGNORE(避免与可能已存在的记录冲突)
--
-- ⚠️ 上 staging/prod 前必跑 staging-checklist § 1.1-1.5 核对实际落地状态,
--    本补丁仅适用"本地 dev DB 实测状态"。其它状态需重写 patch。
-- =============================================================================

-- ─── teacher_profile 补 2 字段 ───
-- 非 JSON(INSTANT)
ALTER TABLE teacher_profile
  ADD COLUMN recommend_weight INT NOT NULL DEFAULT 0 COMMENT '运营手动置顶推荐排序',
  ALGORITHM=INSTANT;

-- JSON(锁写,< 30s 验证后上 prod)
ALTER TABLE teacher_profile
  ADD COLUMN tags JSON COMMENT '教师标签 ["beginner","kids","hasVideo"]';

-- ─── package 补全部 4 字段 ───
ALTER TABLE package
  ADD COLUMN show_on_list_priority INT NOT NULL DEFAULT 0 COMMENT 'PackageList 主推卡排序; >0 上主推区, 0 仅在对比表',
  ADD COLUMN recommendation_label VARCHAR(50) NULL COMMENT '推荐 pill i18n_message.code, null 不显示',
  ADD COLUMN discount_label VARCHAR(50) NULL COMMENT '折扣徽章 i18n_message.code, null 不显示',
  ALGORITHM=INSTANT;

ALTER TABLE package
  ADD COLUMN selling_points JSON NULL COMMENT '卖点 i18n_message.code 列表';

-- ─── review 补 3 字段 + backfill ───
ALTER TABLE review
  ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE COMMENT '匿名评价(true 时公开列表展示为"匿名学员")',
  ADD COLUMN editable_until_at DATETIME NULL COMMENT '编辑窗口截止时间(create + 72h),前端进度条用',
  ALGORITHM=INSTANT;

ALTER TABLE review
  ADD COLUMN custom_tags JSON NULL COMMENT '用户自定义标签,最多 3 条 × 8 字符;不进 platform_config 字典';

-- 历史数据 backfill editable_until_at(create_time + 72h)
UPDATE review SET editable_until_at = DATE_ADD(create_time, INTERVAL 72 HOUR)
  WHERE editable_until_at IS NULL;

-- ─── platform_config review.tag_dict 加 Wave 5 新增 6 条 ───
-- (主 patch 原文,JSON_MERGE_PATCH 幂等;现 6 条 → 12 条)
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

-- ─── INSERT patch log(主 patch + 本补丁)───
-- 主 patch:虽然字段是分批落地,但语义上等价于"主 patch 已落",一并 log
INSERT IGNORE INTO mandarly_patch_log (patch_file, executed_at)
  VALUES ('20260510_140000_M5_ux_overhaul.sql', NOW());

INSERT IGNORE INTO mandarly_patch_log (patch_file, executed_at)
  VALUES ('20260510_141000_M5_ux_overhaul_partial_apply_fix_local-only.sql', NOW());
