-- =============================================================================
-- Mandarly 业务表全量初始化脚本
-- =============================================================================
-- 项目:Mandarly 在线中文培训平台
-- 模块:mandarly-module-edu(业务模块)
-- 版本:v1.x 定稿 2026-05-05
-- 来源:docs/database/(7 份子域设计文档定稿汇编)
-- 用途:仅用于「新库初始化」;上线后变更全部走 server/sql/mysql/patch/YYYYMMDD_HHmmss_*.sql
-- 范围:仅业务表 24 张;若依基础表(system_* / infra_* / quartz_*)走 ruoyi-vue-pro.sql
-- =============================================================================
-- 通用字段(每张业务表必带,DDL 中显式列出避免 BaseDO 反射假设):
--   id            BIGINT UNSIGNED  AUTO_INCREMENT  PRIMARY KEY
--   creator       VARCHAR(64)      DEFAULT ''
--   create_time   DATETIME         DEFAULT CURRENT_TIMESTAMP
--   updater       VARCHAR(64)      DEFAULT ''
--   update_time   DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
--   deleted       TINYINT(1)       DEFAULT 0
--   tenant_id     BIGINT           DEFAULT 0  (一期单租户固定 0,拦截器关闭)
-- =============================================================================
-- 表选项:
--   ENGINE = InnoDB
--   DEFAULT CHARSET = utf8mb4
--   COLLATE = utf8mb4_general_ci
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET sql_mode = 'NO_ENGINE_SUBSTITUTION';

-- =============================================================================
-- 子域 01 · 用户与认证
-- =============================================================================

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role` VARCHAR(16) NOT NULL COMMENT '角色:student/teacher 互斥',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `email_verified_at` DATETIME DEFAULT NULL COMMENT '邮箱验证时间',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT 'E.164 格式手机号',
  `phone_verified_at` DATETIME DEFAULT NULL COMMENT '手机号验证时间',
  `password_hash` VARCHAR(128) DEFAULT NULL COMMENT 'BCrypt 哈希',
  `nickname` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像 URL',
  `locale` VARCHAR(16) NOT NULL DEFAULT 'en' COMMENT '语言偏好:en/zh-CN/zh-TW/ar',
  `timezone` VARCHAR(64) NOT NULL DEFAULT 'UTC' COMMENT 'IANA 时区名',
  `status` VARCHAR(24) NOT NULL DEFAULT 'pending' COMMENT '状态:pending/active/frozen',
  `referral_code` VARCHAR(16) NOT NULL COMMENT '自己的推荐码',
  `referred_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '被谁推荐 user.id',
  `learning_goal` VARCHAR(256) DEFAULT NULL COMMENT '学习目标(自填)',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最近登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最近登录 IP',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email_deleted` (`email`, `deleted`),
  UNIQUE KEY `uk_phone_deleted` (`phone`, `deleted`),
  UNIQUE KEY `uk_referral_code` (`referral_code`),
  KEY `idx_role_status` (`role`, `status`),
  KEY `idx_referred_by` (`referred_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='C 端用户主表(学生+教师)';

DROP TABLE IF EXISTS `user_oauth`;
CREATE TABLE `user_oauth` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联 user.id',
  `provider` VARCHAR(16) NOT NULL COMMENT 'google/apple',
  `oauth_uid` VARCHAR(128) NOT NULL COMMENT '第三方平台唯一 ID',
  `oauth_email` VARCHAR(128) DEFAULT NULL COMMENT '第三方返回邮箱',
  `oauth_raw` JSON DEFAULT NULL COMMENT '完整原始 payload',
  `bound_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `unbound_at` DATETIME DEFAULT NULL COMMENT '解绑时间;NULL 即生效',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_uid_deleted` (`provider`, `oauth_uid`, `deleted`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='第三方登录绑定';

DROP TABLE IF EXISTS `teacher_profile`;
CREATE TABLE `teacher_profile` (
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '主键 = user.id(1:1)',
  `intro` VARCHAR(1024) DEFAULT NULL COMMENT '文字自我介绍',
  `audit_status` VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/approved/rejected',
  `reject_reason` VARCHAR(512) DEFAULT NULL,
  `audited_at` DATETIME DEFAULT NULL,
  `audited_by` BIGINT DEFAULT NULL COMMENT '审核管理员 system_users.id',
  `level` VARCHAR(16) DEFAULT NULL COMMENT '一期 NULL,二期预留 Level1/2/3',
  `expertise` JSON DEFAULT NULL COMMENT '教学方向数组',
  `accent` VARCHAR(32) DEFAULT NULL COMMENT '口音:mainland/taiwan/hk/mixed',
  `languages` JSON DEFAULT NULL COMMENT '会的语言列表',
  `years_experience` INT DEFAULT NULL,
  `intro_video_url` VARCHAR(512) DEFAULT NULL,
  `intro_video_size` BIGINT DEFAULT NULL COMMENT 'byte',
  `intro_video_uploaded_at` DATETIME DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师档案(与 user 1:1)';

DROP TABLE IF EXISTS `teacher_qualification`;
CREATE TABLE `teacher_qualification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联 user.id',
  `doc_type` VARCHAR(32) NOT NULL COMMENT 'id_card/passport/degree_cert/teaching_cert/english_cert/experience_proof',
  `doc_url` VARCHAR(512) NOT NULL COMMENT '文件 URL(COS)',
  `doc_filename` VARCHAR(128) DEFAULT NULL COMMENT '原始文件名',
  `audit_status` VARCHAR(16) NOT NULL DEFAULT 'pending',
  `reject_reason` VARCHAR(512) DEFAULT NULL,
  `audited_at` DATETIME DEFAULT NULL,
  `audited_by` BIGINT DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_doc` (`user_id`, `doc_type`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师资质材料(多份独立审核)';

DROP TABLE IF EXISTS `teacher_schedule`;
CREATE TABLE `teacher_schedule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `weekday` TINYINT NOT NULL COMMENT '0=周日 6=周六',
  `start_time` TIME NOT NULL COMMENT '教师本地时区',
  `end_time` TIME NOT NULL COMMENT '同日内,end > start',
  `timezone` VARCHAR(64) NOT NULL COMMENT '该条记录设置时的时区',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_teacher_weekday` (`teacher_id`, `weekday`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师每周可约时段';

DROP TABLE IF EXISTS `schedule_exception`;
CREATE TABLE `schedule_exception` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `exception_date` DATE NOT NULL COMMENT '教师时区下的日期',
  `type` VARCHAR(16) NOT NULL COMMENT 'closed/extra',
  `start_time` TIME DEFAULT NULL COMMENT 'closed 时可空(整天)/ extra 时必填',
  `end_time` TIME DEFAULT NULL,
  `timezone` VARCHAR(64) NOT NULL,
  `reason` VARCHAR(256) DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_teacher_date` (`teacher_id`, `exception_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师时段例外(临时不可约/新增可约)';

-- =============================================================================
-- 子域 02 · 套餐与订单
-- =============================================================================

DROP TABLE IF EXISTS `package`;
CREATE TABLE `package` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name_i18n_code` VARCHAR(64) NOT NULL COMMENT '多语言 key,关联 i18n_message.code',
  `weekly_count` INT DEFAULT NULL COMMENT '每周课次,免费体验课 NULL',
  `total_count` INT NOT NULL,
  `validity_days` INT NOT NULL,
  `price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(8) NOT NULL DEFAULT 'HKD',
  `is_free_trial` TINYINT(1) NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `sort` INT NOT NULL DEFAULT 0,
  `description_i18n_code` VARCHAR(64) DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_active_sort` (`is_active`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='套餐定义';

DROP TABLE IF EXISTS `student_package`;
CREATE TABLE `student_package` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `package_id` BIGINT UNSIGNED NOT NULL,
  `remaining` INT NOT NULL,
  `expire_at` DATETIME NOT NULL,
  `source` VARCHAR(16) NOT NULL COMMENT 'purchase/free_trial/admin_grant',
  `payment_id` BIGINT UNSIGNED DEFAULT NULL,
  `granted_by` BIGINT DEFAULT NULL COMMENT 'admin_grant 时管理员 id',
  `granted_reason` VARCHAR(256) DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_student_remaining_expire` (`student_id`, `remaining`, `expire_at`),
  KEY `idx_payment_id` (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生持有的套餐实例';

DROP TABLE IF EXISTS `course_order`;
CREATE TABLE `course_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `student_package_id` BIGINT UNSIGNED NOT NULL,
  `scheduled_at` DATETIME(3) NOT NULL COMMENT 'UTC',
  `duration` INT NOT NULL DEFAULT 30 COMMENT '分钟',
  `price_display` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(8) NOT NULL DEFAULT 'HKD',
  `status` VARCHAR(24) NOT NULL DEFAULT 'upcoming' COMMENT 'upcoming/cancelled/finished/no_show_student/no_show_teacher/refunding/refunded/abnormal',
  `cancel_reason` VARCHAR(256) DEFAULT NULL,
  `abnormal_reason` VARCHAR(64) DEFAULT NULL COMMENT 'status=abnormal 时的异常子类(自动写入):meeting_missing/meeting_ongoing_overdue/lcic_init_failed/lcic_no_attendance/meeting_cancelled_orphan/meeting_unknown_status',
  `cancelled_by` VARCHAR(16) DEFAULT NULL COMMENT 'student/teacher/admin/system',
  `cancelled_at` DATETIME DEFAULT NULL,
  `is_refunded_class` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '取消是否返还课次:1=已返还(24h+)/0=未返还(24h 内/缺席)',
  `abnormal_resolution` VARCHAR(256) DEFAULT NULL COMMENT 'abnormal 处置结论备注',
  `abnormal_processed_by` BIGINT DEFAULT NULL COMMENT '处置 abnormal 的管理员 id',
  `abnormal_processed_at` DATETIME DEFAULT NULL,
  `finished_at` DATETIME DEFAULT NULL COMMENT 'LCIC Webhook ended 时间',
  `teacher_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `teacher_amount_currency` VARCHAR(8) NOT NULL DEFAULT 'USD',
  `teacher_settle_status` VARCHAR(24) NOT NULL DEFAULT 'pending' COMMENT 'pending/settled/refund_deducted',
  `is_free_trial` TINYINT(1) NOT NULL DEFAULT 0,
  `active_upcoming_key` TINYINT GENERATED ALWAYS AS (CASE WHEN `status` = 'upcoming' AND `deleted` = 0 THEN 1 ELSE NULL END) STORED COMMENT 'generated: active upcoming booking uniqueness key',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_scheduled_active` (`teacher_id`, `scheduled_at`, `active_upcoming_key`),
  KEY `idx_student_status_scheduled` (`student_id`, `status`, `scheduled_at`),
  KEY `idx_teacher_status_scheduled` (`teacher_id`, `status`, `scheduled_at`),
  KEY `idx_teacher_settle_status` (`teacher_id`, `teacher_settle_status`),
  KEY `idx_scheduled_at` (`scheduled_at`),
  KEY `idx_student_package_id` (`student_package_id`),
  KEY `idx_status_refunded` (`status`, `is_refunded_class`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程订单';

DROP TABLE IF EXISTS `meeting`;
CREATE TABLE `meeting` (
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '主键 = course_order.id(1:1)',
  `lcic_class_id` VARCHAR(64) NOT NULL,
  `lcic_room_url` VARCHAR(512) DEFAULT NULL,
  `student_join_url` VARCHAR(512) DEFAULT NULL,
  `teacher_join_url` VARCHAR(512) DEFAULT NULL,
  `student_token_expires_at` DATETIME DEFAULT NULL,
  `teacher_token_expires_at` DATETIME DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'created' COMMENT 'created/ongoing/ended/cancelled',
  `started_at` DATETIME(3) DEFAULT NULL,
  `ended_at` DATETIME(3) DEFAULT NULL,
  `student_attended` TINYINT(1) DEFAULT NULL,
  `teacher_attended` TINYINT(1) DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_lcic_class_id` (`lcic_class_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='LCIC 课堂(与订单 1:1)';

DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '主键 = course_order.id(1:1)',
  `student_id` BIGINT UNSIGNED NOT NULL,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `rating` TINYINT NOT NULL COMMENT '1-5',
  `content` VARCHAR(1024) DEFAULT NULL,
  `tags` JSON DEFAULT NULL COMMENT '受 platform_config.review.tag_dict 白名单约束',
  `last_edited_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_visible` TINYINT(1) NOT NULL DEFAULT 1,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`order_id`),
  KEY `idx_teacher_rating` (`teacher_id`, `rating`, `is_visible`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价(与订单 1:1)';

-- =============================================================================
-- 子域 06 · 配置与通知
-- =============================================================================

DROP TABLE IF EXISTS `platform_config`;
CREATE TABLE `platform_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(128) NOT NULL COMMENT '点号 namespace,如 pricing.class_unit_price.HKD',
  `config_value` JSON NOT NULL,
  `value_type` VARCHAR(16) NOT NULL DEFAULT 'string' COMMENT 'number/string/boolean/array/object/decimal',
  `category` VARCHAR(32) NOT NULL DEFAULT 'general',
  `description` VARCHAR(512) DEFAULT NULL,
  `is_visible_to_admin` TINYINT(1) NOT NULL DEFAULT 1,
  `last_modified_by` BIGINT DEFAULT NULL COMMENT '审计:最后修改的管理员 system_users.id',
  `last_modified_reason` VARCHAR(256) DEFAULT NULL COMMENT '审计:高敏分类强制必填',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key_deleted` (`config_key`, `deleted`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='平台动态配置(后台 §A8 可改)';

DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `channel` VARCHAR(16) NOT NULL COMMENT 'inbox/email/sms',
  `event_type` VARCHAR(64) NOT NULL,
  `i18n_code` VARCHAR(128) NOT NULL COMMENT '模板 key,溯源 + 重发用',
  `i18n_vars` JSON DEFAULT NULL COMMENT '渲染变量',
  `rendered_locale` VARCHAR(16) NOT NULL,
  `rendered_subject` VARCHAR(256) DEFAULT NULL COMMENT '邮件主题(写入时定型)',
  `rendered_content` VARCHAR(2048) NOT NULL COMMENT '写入时按 user.locale 立刻渲染好的最终文本',
  `to_address` VARCHAR(256) DEFAULT NULL COMMENT '邮件 = email / 短信 = E.164 / inbox NULL',
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT 'pending/sent/failed/read',
  `sent_at` DATETIME DEFAULT NULL,
  `read_at` DATETIME DEFAULT NULL,
  `error_message` VARCHAR(1024) DEFAULT NULL,
  `retry_count` INT NOT NULL DEFAULT 0,
  `provider_message_id` VARCHAR(128) DEFAULT NULL,
  `tpl_ref` VARCHAR(64) DEFAULT NULL COMMENT '腾讯云 ISMS 模板 ID',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_channel_status` (`user_id`, `channel`, `status`, `create_time`),
  KEY `idx_status_pending` (`status`, `retry_count`, `create_time`),
  KEY `idx_event_type_time` (`event_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知投递记录(三通道统一,立刻渲染)';

DROP TABLE IF EXISTS `i18n_message`;
CREATE TABLE `i18n_message` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(128) NOT NULL COMMENT '文案 key',
  `locale` VARCHAR(16) NOT NULL COMMENT 'en/zh-CN/zh-TW/ar',
  `text` TEXT NOT NULL COMMENT '支持命名占位 {varName}',
  `category` VARCHAR(32) DEFAULT NULL COMMENT 'email/sms/error/notification',
  `description` VARCHAR(256) DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_locale_deleted` (`code`, `locale`, `deleted`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='多语言文案(后端 MessageSource 用)';

-- =============================================================================
-- 子域 07 · 客服
-- =============================================================================

DROP TABLE IF EXISTS `faq`;
CREATE TABLE `faq` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(32) NOT NULL COMMENT 'account/package/class/refund/teacher/other',
  `locale` VARCHAR(16) NOT NULL,
  `question` VARCHAR(512) NOT NULL,
  `answer` TEXT NOT NULL,
  `keywords` JSON DEFAULT NULL COMMENT '关键字数组',
  `sort` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT 'active/disabled',
  `view_count` BIGINT NOT NULL DEFAULT 0,
  `match_count` BIGINT NOT NULL DEFAULT 0,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_locale_category_status_sort` (`locale`, `category`, `status`, `sort`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='FAQ 库';

DROP TABLE IF EXISTS `support_contact`;
CREATE TABLE `support_contact` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `market` VARCHAR(16) NOT NULL DEFAULT 'DEFAULT' COMMENT 'CN/AE/HK/SG/US/DEFAULT',
  `channel_type` VARCHAR(16) NOT NULL COMMENT 'wechat/whatsapp/other',
  `display_text` VARCHAR(128) NOT NULL,
  `link_url` VARCHAR(512) DEFAULT NULL,
  `image_url` VARCHAR(512) DEFAULT NULL COMMENT '二维码图 URL(COS)',
  `sort` INT NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_market_active_sort` (`market`, `is_active`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='私域联系方式(按 market 区分)';

DROP TABLE IF EXISTS `support_inquiry_log`;
CREATE TABLE `support_inquiry_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '未登录可空',
  `session_id` VARCHAR(64) NOT NULL COMMENT '浏览器 localStorage session ID',
  `locale` VARCHAR(16) NOT NULL,
  `market` VARCHAR(16) DEFAULT NULL,
  `question_text` VARCHAR(1024) DEFAULT NULL COMMENT '提问原文,纯渠道点击时为 NULL',
  `matched_faq_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'NULL = 未命中',
  `score` DECIMAL(5,3) DEFAULT NULL COMMENT '匹配分 0.000-1.000',
  `clicked_to_human` TINYINT(1) NOT NULL DEFAULT 0,
  `clicked_contact_id` BIGINT UNSIGNED DEFAULT NULL,
  `ip` VARCHAR(64) DEFAULT NULL,
  `user_agent` VARCHAR(512) DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session_created` (`session_id`, `create_time`),
  KEY `idx_matched_faq` (`matched_faq_id`),
  KEY `idx_unmatched_created` (`matched_faq_id`, `create_time`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客服咨询日志';

-- =============================================================================
-- 系统辅助表 · mandarly_patch_log(上线增量补丁追踪,沿用 ckkj 模式)
-- =============================================================================

DROP TABLE IF EXISTS `mandarly_patch_log`;
CREATE TABLE `mandarly_patch_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `patch_file` VARCHAR(256) NOT NULL COMMENT '补丁文件名,如 20260520_120000_add_xxx.sql',
  `executed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `executor` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '执行者',
  `checksum` VARCHAR(64) DEFAULT NULL COMMENT '文件 SHA-256(防止已执行补丁被改)',
  `note` VARCHAR(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patch_file` (`patch_file`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='上线补丁执行追踪';

-- =============================================================================
-- 初始化 seed 数据
-- =============================================================================

-- ----- 套餐 seed(M2 阶段;HKD 主套餐 + D10 USD/CNY 初始可售套餐,后台可继续调整)-----
INSERT INTO `package` (`name_i18n_code`, `weekly_count`, `total_count`, `validity_days`, `price`, `currency`, `is_free_trial`, `is_active`, `sort`) VALUES
('package.name.free_trial',     NULL, 1,   30,  0.00, 'HKD', 1, 1, 0),
('package.name.half_year_1pw',  1,    26,  180, 1280.00, 'HKD', 0, 1, 10),
('package.name.half_year_2pw',  2,    52,  180, 2380.00, 'HKD', 0, 1, 20),
('package.name.year_1pw',       1,    52,  365, 2480.00, 'HKD', 0, 1, 30),
('package.name.year_2pw',       2,    104, 365, 4680.00, 'HKD', 0, 1, 40),
('package.name.single',         NULL, 1,   7,     60.00, 'HKD', 0, 1, 50),
('package.name.half_year_1pw',  1,    26,  180,  164.00, 'USD', 0, 1, 110),
('package.name.half_year_2pw',  2,    52,  180,  305.00, 'USD', 0, 1, 120),
('package.name.year_1pw',       1,    52,  365,  318.00, 'USD', 0, 1, 130),
('package.name.year_2pw',       2,    104, 365,  600.00, 'USD', 0, 1, 140),
('package.name.single',         NULL, 1,   7,      8.00, 'USD', 0, 1, 150),
('package.name.half_year_1pw',  1,    26,  180, 1185.00, 'CNY', 0, 1, 210),
('package.name.half_year_2pw',  2,    52,  180, 2204.00, 'CNY', 0, 1, 220),
('package.name.year_1pw',       1,    52,  365, 2296.00, 'CNY', 0, 1, 230),
('package.name.year_2pw',       2,    104, 365, 4333.00, 'CNY', 0, 1, 240),
('package.name.single',         NULL, 1,   7,     56.00, 'CNY', 0, 1, 250);

-- ----- platform_config seed(关键业务配置)-----
INSERT INTO `platform_config` (`config_key`, `config_value`, `value_type`, `category`, `description`) VALUES
('class.duration_minutes',                            '30',                                 'number',  'pricing',      '课时长度(分钟,固定 30)'),
('pricing.class_unit_price.HKD',                      '0',                                  'decimal', 'pricing',      '普通课课时费(HKD,待project owner提供)'),
('pricing.free_trial_class_value',                    '0',                                  'decimal', 'pricing',      '免费课课时费(默认 0)'),
('withdrawal.min_amount.USD',                         '100',                                'decimal', 'withdrawal',   '最低提现额度(美元等值)'),
('withdrawal.frozen_days',                            '7',                                  'number',  'withdrawal',   'T+7 冻结期天数'),
('referral.reward_referrer_free_classes',             '1',                                  'number',  'referral',     '推荐人奖励免费课次数'),
('referral.reward_referee_discount_amount.HKD',       '30',                                 'decimal', 'referral',     '被推荐人立减(HKD)'),
('cancel.full_refund_hours_before',                   '24',                                 'number',  'order',        '取消全额退款时限(小时)'),
('support.faq_match_threshold',                       '0.3',                                'decimal', 'support',      'FAQ 关键字命中阈值'),
('support.inquiry_log_retention_days',                '180',                                'number',  'support',      '咨询日志保留天数(6 个月)'),
('package.expire_reminder_days',                      '[7,3,1]',                            'array',   'notification', '套餐到期提醒提前天数'),
('currency.exchange_rates',                           '{"USD_HKD":7.8,"CNY_HKD":1.08}',     'object',  'pricing',      '汇率表(后续接 API 自动)'),
('review.tag_dict',                                   '{"patient":{"i18n":"review.tag.patient","category":"positive"},"native_accent":{"i18n":"review.tag.native_accent","category":"positive"},"good_pace":{"i18n":"review.tag.good_pace","category":"positive"},"well_prepared":{"i18n":"review.tag.well_prepared","category":"positive"},"audio_issue":{"i18n":"review.tag.audio_issue","category":"negative"},"late":{"i18n":"review.tag.late","category":"negative"}}', 'object', 'general', '评价标签白名单字典');

-- ----- 客服 seed(D3 简化版;FAQ 后续由 admin §A10 管理)-----
INSERT INTO `support_contact` (`market`, `channel_type`, `display_text`, `link_url`, `image_url`, `sort`, `is_active`) VALUES
('DEFAULT', 'wechat', '微信客服:I-KYC_ZJF-LOVE', 'wechat:I-KYC_ZJF-LOVE', NULL, 10, 1),
('DEFAULT', 'other', 'Email support', 'mailto:hello.com', NULL, 100, 1);

INSERT INTO `faq` (`category`, `locale`, `question`, `answer`, `keywords`, `sort`, `status`) VALUES
('package', 'zh-CN', '可以免费试听一节吗?', '可以。注册后可领取 1 节免费体验课,自由选择老师和时间。', JSON_ARRAY('免费试听','体验课','免费课','trial'), 10, 'active'),
('class',   'zh-CN', '怎么预约课程?', '进入老师列表或老师详情页,选择可预约时间后提交预约。开课前 30 分钟可进入课堂调试设备。', JSON_ARRAY('预约','上课','时间','课堂'), 20, 'active'),
('refund',  'zh-CN', '不满意可以退款吗?', '课前 24 小时取消可退回课次。开课后的退款申请按服务条款处理。', JSON_ARRAY('退款','取消','不满意','refund'), 30, 'active'),
('package', 'en',    'Can I take a free trial lesson?', 'Yes. After signing up, you can claim one free trial lesson and choose a tutor and time.', JSON_ARRAY('free trial','trial','free lesson'), 10, 'active'),
('class',   'en',    'How do I book a lesson?', 'Open the tutor list or tutor profile, choose an available time, and submit the booking. You can enter the classroom 30 minutes before class.', JSON_ARRAY('book','booking','lesson','classroom'), 20, 'active'),
('package', 'zh-TW', '可以免費試聽一節嗎?', '可以。註冊後可領取 1 節免費體驗課,自由選擇老師和時間。', JSON_ARRAY('免費試聽','體驗課','免費課','trial'), 10, 'active'),
('package', 'ar',    'هل يمكنني تجربة درس مجاني؟', 'نعم. بعد إنشاء الحساب، يمكنك الحصول على درس تجريبي مجاني واختيار المعلم والوقت.', JSON_ARRAY('تجريبي','مجاني','درس'), 10, 'active');

-- =============================================================================
-- 子域 08 · 水平测试与推荐(PRD-v4.3 / 2026-05-06)
-- =============================================================================

DROP TABLE IF EXISTS `level_check_question`;
CREATE TABLE `level_check_question` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `question_code` VARCHAR(32) NOT NULL,
  `question_i18n_code` VARCHAR(64) NOT NULL,
  `question_type` VARCHAR(16) NOT NULL DEFAULT 'single_choice',
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

DROP TABLE IF EXISTS `level_check_option`;
CREATE TABLE `level_check_option` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT UNSIGNED NOT NULL,
  `option_code` VARCHAR(32) NOT NULL,
  `option_i18n_code` VARCHAR(64) NOT NULL,
  `match_expertise` JSON DEFAULT NULL,
  `score_rules` JSON DEFAULT NULL,
  `inferred_level` VARCHAR(16) DEFAULT NULL,
  `recommended_weekly_count` INT DEFAULT NULL,
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

DROP TABLE IF EXISTS `level_check_submission`;
CREATE TABLE `level_check_submission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED DEFAULT NULL,
  `session_id` VARCHAR(64) DEFAULT NULL,
  `email` VARCHAR(128) DEFAULT NULL,
  `locale` VARCHAR(16) NOT NULL,
  `market` VARCHAR(8) DEFAULT NULL,
  `answers` JSON NOT NULL,
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

-- 题库初始化数据(默认 4 题 + 18 选项;运营可后台改 / 加)
INSERT INTO `level_check_question` (`question_code`, `question_i18n_code`, `sort`) VALUES
('q1_level',   'level_check.q1.text', 10),
('q2_goal',    'level_check.q2.text', 20),
('q3_pace',    'level_check.q3.text', 30),
('q4_learner', 'level_check.q4.text', 40);

INSERT INTO `level_check_option` (`question_id`, `option_code`, `option_i18n_code`, `inferred_level`, `sort`) VALUES
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q1_level') AS t), 'complete_beginner',   'level_check.q1.opt.complete_beginner',   'beginner',     10),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q1_level') AS t), 'some_basics',         'level_check.q1.opt.some_basics',         'beginner',     20),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q1_level') AS t), 'simple_conversation', 'level_check.q1.opt.simple_conversation', 'intermediate', 30),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q1_level') AS t), 'fairly_fluent',       'level_check.q1.opt.fairly_fluent',       'advanced',     40);

INSERT INTO `level_check_option` (`question_id`, `option_code`, `option_i18n_code`, `match_expertise`, `score_rules`, `sort`) VALUES
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'career',     'level_check.q2.opt.career',     NULL,           '[{"expertise":"business","score":10}]', 10),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'business',   'level_check.q2.opt.business',   NULL,           '[{"expertise":"business","score":10}]', 20),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'travel',     'level_check.q2.opt.travel',     NULL,           '[{"expertise":"daily","score":10}]',    30),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'hsk_exam',   'level_check.q2.opt.hsk_exam',   '["HSK"]',      '[{"expertise":"HSK","score":10}]',      40),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'for_kids',   'level_check.q2.opt.for_kids',   NULL,           '[{"expertise":"kids","score":10}]',     50),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q2_goal') AS t), 'just_for_fun','level_check.q2.opt.just_for_fun',NULL,         NULL,                                    60);

INSERT INTO `level_check_option` (`question_id`, `option_code`, `option_i18n_code`, `recommended_weekly_count`, `sort`) VALUES
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q3_pace') AS t), 'one_per_week', 'level_check.q3.opt.one_per_week', 1, 10),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q3_pace') AS t), 'two_per_week', 'level_check.q3.opt.two_per_week', 2, 20),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q3_pace') AS t), 'intensive',    'level_check.q3.opt.intensive',    2, 30),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q3_pace') AS t), 'not_sure',     'level_check.q3.opt.not_sure',     1, 40);

INSERT INTO `level_check_option` (`question_id`, `option_code`, `option_i18n_code`, `match_expertise`, `sort`) VALUES
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q4_learner') AS t), 'myself',  'level_check.q4.opt.myself',  NULL,        10),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q4_learner') AS t), 'child',   'level_check.q4.opt.child',   '["kids"]',  20),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q4_learner') AS t), 'teen',    'level_check.q4.opt.teen',    NULL,        30),
((SELECT id FROM (SELECT id FROM level_check_question WHERE question_code='q4_learner') AS t), 'multiple','level_check.q4.opt.multiple',NULL,        40);

-- =============================================================================
SET FOREIGN_KEY_CHECKS = 1;
-- =============================================================================

-- ===== M4 Patch (20260509) =====
-- ============================================================
-- 1. payment 支付订单(套餐购买)
-- ============================================================
CREATE TABLE `payment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '学生 id',
  `type` VARCHAR(32) NOT NULL DEFAULT 'package_purchase',
  `package_id` BIGINT UNSIGNED NOT NULL,
  `student_package_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'webhook 成功后回填',
  `channel` VARCHAR(16) NOT NULL DEFAULT 'stripe',
  `channel_session_id` VARCHAR(128) DEFAULT NULL COMMENT 'cs_xxx',
  `channel_payment_intent_id` VARCHAR(128) DEFAULT NULL COMMENT 'pi_xxx',
  `channel_charge_id` VARCHAR(128) DEFAULT NULL COMMENT 'ch_xxx',
  `payment_method_type` VARCHAR(32) DEFAULT NULL COMMENT 'card/wechat_pay/alipay/link/...,webhook payment_intent.succeeded 时回填',
  `amount_request` DECIMAL(12, 2) NOT NULL,
  `currency_request` VARCHAR(3) NOT NULL DEFAULT 'USD',
  `amount_paid` DECIMAL(12, 2) DEFAULT NULL,
  `currency_paid` VARCHAR(3) DEFAULT NULL,
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
CREATE TABLE `refund` (
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
  `is_active` TINYINT(1) GENERATED ALWAYS AS (CASE WHEN `status` IN ('pending','approved') THEN 1 ELSE NULL END) STORED COMMENT '派生:active(pending/approved)=1,其他 NULL,配合 UNIQUE 防重审',
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_active` (`payment_id`, `is_active`),
  KEY `idx_payment` (`payment_id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_created` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款工单';

-- ============================================================
-- 3. stripe_event Webhook 去重
-- ============================================================
CREATE TABLE `stripe_event` (
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
--    v1.2 (M6) — 加 status / currency / frozen_until / related_income_id / description
-- ============================================================
CREATE TABLE `teacher_income` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `course_order_id` BIGINT UNSIGNED DEFAULT NULL,
  `refund_id` BIGINT UNSIGNED DEFAULT NULL,
  `amount_usd` DECIMAL(12, 2) NOT NULL COMMENT '扣回为负数',
  `currency` VARCHAR(8) NOT NULL DEFAULT 'USD',
  `type` VARCHAR(16) NOT NULL COMMENT 'normal/free_trial/refund_deduct',
  `status` VARCHAR(16) NOT NULL DEFAULT 'frozen' COMMENT 'frozen/available/reverted',
  `settled_at` DATETIME NOT NULL COMMENT 'T+7 起算时点',
  `frozen_until` DATETIME(3) NOT NULL COMMENT '解冻时刻 = settled_at + 7d',
  `related_income_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'refund_deduct 时关联原 normal 行',
  `description` VARCHAR(256) DEFAULT NULL,
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
  KEY `idx_settled` (`settled_at`),
  KEY `idx_teacher_status_frozen` (`teacher_id`, `status`, `frozen_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师收入流水(append-only)';

-- ============================================================
-- 5. teacher_balance 教师余额缓存
--    v1.2 (M6) — frozen_usd 拆为 frozen_t7_usd + pending;加 currency + version 乐观锁
-- ============================================================
CREATE TABLE `teacher_balance` (
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `frozen_t7_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT 'T+7 未解冻',
  `available_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `pending` DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '提现 in-flight',
  `total_earned_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `total_withdrawn_usd` DECIMAL(12, 2) NOT NULL DEFAULT 0,
  `currency` VARCHAR(8) NOT NULL DEFAULT 'USD',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  `last_rebuild_at` DATETIME NOT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师余额缓存(Service 强一致 + 对账 Job 告警)';

-- ============================================================
-- 5b. teacher_withdrawal 教师提现申请(M6 新建)
--     payee_info: AES-256-GCM 密文(Base64),格式 v1:{iv}:{ct_with_tag}
--     状态机: pending → approved → paid / rejected / failed
-- ============================================================
CREATE TABLE `teacher_withdrawal` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `teacher_id` BIGINT UNSIGNED NOT NULL,
  `amount` DECIMAL(12, 2) NOT NULL,
  `currency` VARCHAR(8) NOT NULL DEFAULT 'USD',
  `payee_info` VARCHAR(2048) NOT NULL COMMENT 'AES-256-GCM 密文(Base64),格式 v1:{iv}:{ct_with_tag}',
  `payee_method` VARCHAR(32) DEFAULT NULL COMMENT 'wechat/alipay/paypal/bank_card/other',
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/paid/rejected/failed',
  `applied_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `audited_by` BIGINT DEFAULT NULL,
  `audited_at` DATETIME DEFAULT NULL,
  `reject_reason` VARCHAR(512) DEFAULT NULL,
  `paid_by` BIGINT DEFAULT NULL,
  `paid_at` DATETIME DEFAULT NULL,
  `paid_proof` VARCHAR(512) DEFAULT NULL COMMENT '打款凭证截图 URL(COS)',
  `paid_remark` VARCHAR(256) DEFAULT NULL,
  `creator` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_teacher_status_applied` (`teacher_id`, `status`, `applied_at`),
  KEY `idx_status_applied` (`status`, `applied_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师提现申请';

-- ============================================================
-- 6. referral_record 推荐关系 + 奖励
-- ============================================================
CREATE TABLE `referral_record` (
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
-- 7. user_payment_profile (Stripe Customer 映射,复核 #4)
--    支持 lazy create:首次支付时建 stripe_customer 并缓存
--    二期订阅 / 保存卡片 / dispute 聚合预留
-- ============================================================
CREATE TABLE `user_payment_profile` (
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
-- infra_config 6 配置 key (M4 业务参数)
-- ============================================================
INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('mandarly', 2, '支付 Session 过期分钟', 'mandarly.payment.session_expires_minutes', '30', 1, 'system', NOW(), 'system', NOW(), 0);

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('mandarly', 2, 'Stripe 测试模式', 'mandarly.payment.test_mode', 'true', 1, 'system', NOW(), 'system', NOW(), 0);

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('mandarly', 2, '推荐码被推荐人减免 USD', 'mandarly.referral.referee_discount_usd', '4', 1, 'system', NOW(), 'system', NOW(), 0);

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('mandarly', 2, '推荐码推荐人奖励套餐 id', 'mandarly.referral.referrer_reward_package_id', '1', 1, 'system', NOW(), 'system', NOW(), 0);

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('mandarly', 2, '普通课课时费 USD', 'mandarly.income.teacher_lesson_fee_usd', '3', 1, 'system', NOW(), 'system', NOW(), 0);

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('mandarly', 2, '教师收入冻结天数', 'mandarly.income.frozen_days', '7', 1, 'system', NOW(), 'system', NOW(), 0);

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('mandarly', 2, 'FAQ 命中阈值', 'support.faq_match_threshold', '0.3', 1, 'system', NOW(), 'system', NOW(), 0);

-- =============================================================================
-- M4 Stripe 3 个定时 Job 注册(20260509_113000)
-- handler_name = Spring Bean 名(类名首字母小写),Quartz JobHandler 框架
-- Cron 按 JVM TZ 解析;生产 JVM TZ=UTC,Phase 10 Task 10.2 上线 checklist 确认
-- =============================================================================
INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'M4 教师余额冻结转可用', 1, 'teacherBalanceFrozenToAvailableJob', '', '0 0 3 * * ?', 3, 1000, 300000, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_job` WHERE `handler_name` = 'teacherBalanceFrozenToAvailableJob' AND `deleted` = 0);

INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'M4 教师收入兜底建账', 1, 'teacherIncomeBackfillJob', '', '0 0 4 * * ?', 3, 1000, 300000, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_job` WHERE `handler_name` = 'teacherIncomeBackfillJob' AND `deleted` = 0);

INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'M4 退款孤儿对账', 1, 'refundOrphanCheckJob', '', '0 0 5 ? * MON', 3, 1000, 600000, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_job` WHERE `handler_name` = 'refundOrphanCheckJob' AND `deleted` = 0);

-- =============================================================================
-- M4 admin 菜单:支付 + 退款 + 教师收入 + 推荐记录(id 3012~3020)
-- patch: 20260509_140000_add_edu_payment_menus.sql
-- =============================================================================

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3012, '支付管理', '', 2, 12, 3001, 'payment', 'ep:credit-card', 'edu/payment/index', 'EduPayment', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='支付管理', type=2, sort=12, parent_id=3001, path='payment', icon='ep:credit-card',
  component='edu/payment/index', component_name='EduPayment', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3013, '查询支付', 'edu:payment:query', 3, 1, 3012, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询支付', permission='edu:payment:query', type=3, sort=1, parent_id=3012,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3014, '退款管理', '', 2, 13, 3001, 'refund', 'ep:tickets', 'edu/refund/index', 'EduRefund', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='退款管理', type=2, sort=13, parent_id=3001, path='refund', icon='ep:tickets',
  component='edu/refund/index', component_name='EduRefund', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3015, '查询退款', 'edu:refund:query', 3, 1, 3014, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询退款', permission='edu:refund:query', type=3, sort=1, parent_id=3014,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3016, '审核退款', 'edu:refund:approve', 3, 2, 3014, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='审核退款', permission='edu:refund:approve', type=3, sort=2, parent_id=3014,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3017, '教师收入', '', 2, 14, 3001, 'teacher-income', 'ep:money', 'edu/teacher-income/index', 'EduTeacherIncome', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='教师收入', type=2, sort=14, parent_id=3001, path='teacher-income', icon='ep:money',
  component='edu/teacher-income/index', component_name='EduTeacherIncome', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3018, '查询收入', 'edu:teacher-income:query', 3, 1, 3017, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询收入', permission='edu:teacher-income:query', type=3, sort=1, parent_id=3017,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3019, '推荐记录', '', 2, 15, 3001, 'referral', 'ep:share', 'edu/referral/index', 'EduReferral', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='推荐记录', type=2, sort=15, parent_id=3001, path='referral', icon='ep:share',
  component='edu/referral/index', component_name='EduReferral', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3020, '查询推荐', 'edu:referral:query', 3, 1, 3019, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询推荐', permission='edu:referral:query', type=3, sort=1, parent_id=3019,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3036, '查询推荐规则配置', 'infra:config:query', 3, 2, 3019, 0, 'system', 'system'),
  (3037, '更新推荐规则配置', 'infra:config:update', 3, 3, 3019, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), permission=VALUES(permission), type=3, sort=VALUES(sort), parent_id=3019,
  status=0, updater='system', update_time=NOW();

-- M6 admin 菜单:教师提现审核(id 3021~3025,parent 3001 教育业务顶级)
-- 来源:server/sql/mysql/patch/20260511_233800_add_withdrawal_menus.sql
-- 权限点:edu:withdrawal:query / audit / pay / reveal-payee(连字符,与 spec §6.4 + 后端 @PreAuthorize + 前端 v-access 对齐)
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3021, '提现审核', '', 2, 16, 3001, 'withdrawal', 'ep:money', 'edu/withdrawal/index', 'EduWithdrawal', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='提现审核', type=2, sort=16, parent_id=3001, path='withdrawal', icon='ep:money',
  component='edu/withdrawal/index', component_name='EduWithdrawal', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3022, '查询提现', 'edu:withdrawal:query', 3, 1, 3021, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询提现', permission='edu:withdrawal:query', type=3, sort=1, parent_id=3021,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3023, '审核提现', 'edu:withdrawal:audit', 3, 2, 3021, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='审核提现', permission='edu:withdrawal:audit', type=3, sort=2, parent_id=3021,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3024, '标记打款', 'edu:withdrawal:pay', 3, 3, 3021, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='标记打款', permission='edu:withdrawal:pay', type=3, sort=3, parent_id=3021,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3025, '查看完整收款信息', 'edu:withdrawal:reveal-payee', 3, 4, 3021, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查看完整收款信息', permission='edu:withdrawal:reveal-payee', type=3, sort=4, parent_id=3021,
  status=0, updater='system', update_time=NOW();

-- D4 admin 菜单:客服管理(id 3026~3035,parent 3001 教育业务顶级)
-- 来源:server/sql/mysql/patch/20260513_214000_add_support_admin_menus.sql
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3026, '客服管理', '', 2, 18, 3001, 'support', 'ep:service', 'edu/support/index', 'EduSupport', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='客服管理', type=2, sort=18, parent_id=3001, path='support', icon='ep:service',
  component='edu/support/index', component_name='EduSupport', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3027, '查询 FAQ', 'edu:support-faq:query', 3, 1, 3026, 0, 'system', 'system'),
  (3028, '新建 FAQ', 'edu:support-faq:create', 3, 2, 3026, 0, 'system', 'system'),
  (3029, '更新 FAQ', 'edu:support-faq:update', 3, 3, 3026, 0, 'system', 'system'),
  (3030, '删除 FAQ', 'edu:support-faq:delete', 3, 4, 3026, 0, 'system', 'system'),
  (3031, '查询联系方式', 'edu:support-contact:query', 3, 5, 3026, 0, 'system', 'system'),
  (3032, '新建联系方式', 'edu:support-contact:create', 3, 6, 3026, 0, 'system', 'system'),
  (3033, '更新联系方式', 'edu:support-contact:update', 3, 7, 3026, 0, 'system', 'system'),
  (3034, '删除联系方式', 'edu:support-contact:delete', 3, 8, 3026, 0, 'system', 'system'),
  (3035, '查询咨询日志', 'edu:support-inquiry:query', 3, 9, 3026, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), permission=VALUES(permission), type=3, sort=VALUES(sort), parent_id=3026,
  status=0, updater='system', update_time=NOW();

-- D4.2 admin 菜单:水平测试(id 3036~3041,parent 3001 教育业务顶级)
-- 来源:server/sql/mysql/patch/20260513_220000_add_level_check_admin_menu.sql
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3036, '水平测试', '', 2, 19, 3001, 'level-check', 'ep:data-analysis', 'edu/level-check/index', 'EduLevelCheck', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='水平测试', type=2, sort=19, parent_id=3001, path='level-check', icon='ep:data-analysis',
  component='edu/level-check/index', component_name='EduLevelCheck', status=0, visible=b'1',
  updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3037, '查询水平测试', 'edu:level-check:query', 3, 1, 3036, 0, 'system', 'system'),
  (3038, '新建水平测试题目', 'edu:level-check:create', 3, 2, 3036, 0, 'system', 'system'),
  (3039, '更新水平测试题目', 'edu:level-check:update', 3, 3, 3036, 0, 'system', 'system'),
  (3040, '删除水平测试题目', 'edu:level-check:delete', 3, 4, 3036, 0, 'system', 'system'),
  (3041, '导出水平测试答卷', 'edu:level-check:export', 3, 5, 3036, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), permission=VALUES(permission), type=3, sort=VALUES(sort), parent_id=3036,
  status=0, updater='system', update_time=NOW();

-- D12 admin 菜单:C 端用户管理(id 3042~3044,parent 3001 教育业务顶级)
-- 来源:server/sql/mysql/patch/20260514_103000_add_admin_user_menu.sql
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3042, '用户管理', '', 2, 0, 3001, 'user', 'ep:user', 'edu/user/index', 'EduUser', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='用户管理', type=2, sort=0, parent_id=3001, path='user', icon='ep:user',
  component='edu/user/index', component_name='EduUser', status=0, visible=b'1',
  updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3043, '查询 C 端用户', 'edu:user:query', 3, 1, 3042, 0, 'system', 'system'),
  (3044, '冻结 C 端用户', 'edu:user:freeze', 3, 2, 3042, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), permission=VALUES(permission), type=3, sort=VALUES(sort), parent_id=3042,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_role_menu (role_id, menu_id, creator, updater)
SELECT 1, m.id, 'system', 'system'
FROM system_menu m
WHERE m.id BETWEEN 3012 AND 3044
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu srm
    WHERE srm.role_id = 1 AND srm.menu_id = m.id AND srm.deleted = 0
  );

-- =============================================================================
-- 脚本结束
-- 共建表 30 张(M6 后):
--   01 用户与认证(6) + 02 套餐与订单(5) + M4 支付域(7) + M6 提现(1) +
--   06 配置与通知(3) + 07 客服(3) + 08 水平测试(3) + 系统辅助(1)
-- M4 支付域 7 张表:payment / refund / stripe_event / teacher_income /
--   teacher_balance / referral_record / user_payment_profile
-- M6 升级:teacher_income v1.2(+5 字段)/ teacher_balance v1.2(拆 frozen + 乐观锁)/
--   teacher_withdrawal 新建(AES-GCM 加密 payee_info)
-- (老 03 支付 / 04 教师收入提现 / 05 推荐码 schema 已在 2026-05-09 废弃,
--  当前 M4+M6 schema 由 docs/product/M6-spec.md §2.1 定义)
-- =============================================================================
