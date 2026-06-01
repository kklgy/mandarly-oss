-- =============================================================================
-- Patch:子域 08 · 水平测试与推荐(PRD-v4.3 / 2026-05-06)
-- =============================================================================
-- 来源:`docs/product/level-check-recommendation-v1.md` §五
-- 影响:新增 3 张表(level_check_question / level_check_option / level_check_submission)
-- 不可回滚:执行前请先备份;部署前用 mandarly_patch_log 记录(2026-05-06)
-- =============================================================================

-- 题库
DROP TABLE IF EXISTS `level_check_question`;
CREATE TABLE `level_check_question` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `question_code` VARCHAR(32) NOT NULL COMMENT '题目编码,如 q1_level / q2_goal',
  `question_i18n_code` VARCHAR(64) NOT NULL COMMENT '题干多语言 key,关联 i18n_message.code',
  `question_type` VARCHAR(16) NOT NULL DEFAULT 'single_choice' COMMENT '一期仅 single_choice',
  `sort` INT NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_deleted` (`question_code`, `deleted`),
  KEY `idx_active_sort` (`is_active`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='水平测试题库';

-- 选项库
DROP TABLE IF EXISTS `level_check_option`;
CREATE TABLE `level_check_option` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT UNSIGNED NOT NULL,
  `option_code` VARCHAR(32) NOT NULL COMMENT '选项编码,如 beginner/business/kids',
  `option_i18n_code` VARCHAR(64) NOT NULL COMMENT '选项文案多语言 key',
  `match_expertise` JSON DEFAULT NULL COMMENT '硬约束:必须命中的教师 expertise 数组',
  `score_rules` JSON DEFAULT NULL COMMENT '软约束 [{"expertise":"business","score":10}]',
  `inferred_level` VARCHAR(16) DEFAULT NULL COMMENT 'Q1 用,beginner/intermediate/advanced',
  `recommended_weekly_count` INT DEFAULT NULL COMMENT 'Q3 用,推套餐时取此值',
  `sort` INT NOT NULL DEFAULT 0,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='水平测试选项库';

-- 答卷记录
DROP TABLE IF EXISTS `level_check_submission`;
CREATE TABLE `level_check_submission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '可空,未登录用 session_id 兜底',
  `session_id` VARCHAR(64) DEFAULT NULL COMMENT '浏览器 session,跟 §C 客服同源',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '用户选填,后续召回用',
  `locale` VARCHAR(16) NOT NULL,
  `market` VARCHAR(8) DEFAULT NULL COMMENT '一期 NULL,二期接 §4.10',
  `answers` JSON NOT NULL COMMENT '[{"questionCode":"q1_level","optionCode":"beginner"}]',
  `inferred_level` VARCHAR(16) NOT NULL,
  `recommended_teacher_ids` JSON NOT NULL,
  `recommended_package_id` BIGINT UNSIGNED DEFAULT NULL,
  `is_converted` TINYINT(1) NOT NULL DEFAULT 0,
  `converted_order_id` BIGINT UNSIGNED DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_converted` (`is_converted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='水平测试答卷记录';

-- =============================================================================
-- 题库初始化数据(默认 4 题,运营可后台改 / 加)
-- 多语言文案需另插 i18n_message 表(en/zh-CN/zh-TW/ar 各一份),本 patch 不含
-- =============================================================================

INSERT INTO `level_check_question` (`question_code`, `question_i18n_code`, `sort`) VALUES
('q1_level',   'level_check.q1.text', 10),
('q2_goal',    'level_check.q2.text', 20),
('q3_pace',    'level_check.q3.text', 30),
('q4_learner', 'level_check.q4.text', 40);

-- Q1 选项
INSERT INTO `level_check_option` (`question_id`, `option_code`, `option_i18n_code`, `inferred_level`, `sort`) VALUES
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q1_level') AS t), 'complete_beginner',   'level_check.q1.opt.complete_beginner',   'beginner',     10),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q1_level') AS t), 'some_basics',         'level_check.q1.opt.some_basics',         'beginner',     20),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q1_level') AS t), 'simple_conversation', 'level_check.q1.opt.simple_conversation', 'intermediate', 30),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q1_level') AS t), 'fairly_fluent',       'level_check.q1.opt.fairly_fluent',       'advanced',     40);

-- Q2 选项(score_rules 软约束打分,匹配教师 expertise)
INSERT INTO `level_check_option` (`question_id`, `option_code`, `option_i18n_code`, `match_expertise`, `score_rules`, `sort`) VALUES
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'career',     'level_check.q2.opt.career',     NULL,           '[{"expertise":"business","score":10}]',         10),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'business',   'level_check.q2.opt.business',   NULL,           '[{"expertise":"business","score":10}]',         20),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'travel',     'level_check.q2.opt.travel',     NULL,           '[{"expertise":"daily","score":10}]',            30),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'hsk_exam',   'level_check.q2.opt.hsk_exam',   '["HSK"]',      '[{"expertise":"HSK","score":10}]',              40),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'for_kids',   'level_check.q2.opt.for_kids',   NULL,           '[{"expertise":"kids","score":10}]',             50),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'just_for_fun', 'level_check.q2.opt.just_for_fun', NULL,         NULL,                                            60);

-- Q3 选项
INSERT INTO `level_check_option` (`question_id`, `option_code`, `option_i18n_code`, `recommended_weekly_count`, `sort`) VALUES
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q3_pace') AS t), 'one_per_week', 'level_check.q3.opt.one_per_week', 1,    10),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q3_pace') AS t), 'two_per_week', 'level_check.q3.opt.two_per_week', 2,    20),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q3_pace') AS t), 'intensive',    'level_check.q3.opt.intensive',    2,    30),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q3_pace') AS t), 'not_sure',     'level_check.q3.opt.not_sure',     1,    40);

-- Q4 选项(强约束 child / teen / multi)
INSERT INTO `level_check_option` (`question_id`, `option_code`, `option_i18n_code`, `match_expertise`, `sort`) VALUES
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q4_learner') AS t), 'myself',  'level_check.q4.opt.myself',  NULL,         10),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q4_learner') AS t), 'child',   'level_check.q4.opt.child',   '["kids"]',   20),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q4_learner') AS t), 'teen',    'level_check.q4.opt.teen',    NULL,         30),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q4_learner') AS t), 'multiple','level_check.q4.opt.multiple',NULL,         40);

-- 上线后此 patch 在 mandarly_patch_log 表登记
-- =============================================================================
-- patch end
-- =============================================================================
