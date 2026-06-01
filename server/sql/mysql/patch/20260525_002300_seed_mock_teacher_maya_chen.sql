-- 20260525_002300_seed_mock_teacher_maya_chen.sql
-- Purpose:
--   Seed one approved mock teacher for admin/backend review and marketplace smoke checks.
--   The account is intentionally non-login by default (password_hash is NULL).
-- Rollback:
--   UPDATE teacher_schedule ts
--   JOIN `user` u ON u.id = ts.teacher_id
--   SET ts.deleted = 1, ts.updater = 'rollback', ts.update_time = NOW()
--   WHERE u.email = 'teacher.mock.maya.chen@mandarly.test';
--   UPDATE teacher_qualification tq
--   JOIN `user` u ON u.id = tq.user_id
--   SET tq.deleted = 1, tq.updater = 'rollback', tq.update_time = NOW()
--   WHERE u.email = 'teacher.mock.maya.chen@mandarly.test';
--   UPDATE teacher_profile tp
--   JOIN `user` u ON u.id = tp.user_id
--   SET tp.deleted = 1, tp.updater = 'rollback', tp.update_time = NOW()
--   WHERE u.email = 'teacher.mock.maya.chen@mandarly.test';
--   UPDATE `user`
--   SET deleted = 1, updater = 'rollback', update_time = NOW()
--   WHERE email = 'teacher.mock.maya.chen@mandarly.test';
-- Safety:
--   Data-only, idempotent seed. Uses reserved .test email and does not create a password.

SET NAMES utf8mb4;

SET @patch_name := '20260525_002300_seed_mock_teacher_maya_chen' COLLATE utf8mb4_general_ci;
SET @mock_teacher_email := 'teacher.mock.maya.chen@mandarly.test' COLLATE utf8mb4_general_ci;
SET @mock_teacher_referral := 'TMOCKMC26' COLLATE utf8mb4_general_ci;

INSERT INTO `user` (
    `role`, `email`, `email_verified_at`, `phone`, `phone_verified_at`, `password_hash`,
    `nickname`, `avatar_url`, `locale`, `timezone`, `status`, `referral_code`,
    `referred_by`, `learning_goal`, `last_login_at`, `last_login_ip`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT
    'teacher', @mock_teacher_email, NOW(), NULL, NULL, NULL,
    'Maya Chen (Mock)', NULL, 'en', 'Asia/Hong_Kong', 'active', @mock_teacher_referral,
    NULL, NULL, NULL, NULL,
    @patch_name, NOW(), @patch_name, NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `user`
    WHERE `email` = @mock_teacher_email
      AND `deleted` = 0
);

SET @mock_teacher_id := (
    SELECT `id`
    FROM `user`
    WHERE `email` = @mock_teacher_email
      AND `deleted` = 0
    ORDER BY `id` DESC
    LIMIT 1
);

UPDATE `user`
SET `role` = 'teacher',
    `nickname` = 'Maya Chen (Mock)',
    `locale` = 'en',
    `timezone` = 'Asia/Hong_Kong',
    `status` = 'active',
    `updater` = @patch_name,
    `update_time` = NOW()
WHERE `id` = @mock_teacher_id;

INSERT INTO `teacher_profile` (
    `user_id`, `intro`, `audit_status`, `reject_reason`, `audited_at`, `audited_by`,
    `level`, `expertise`, `accent`, `languages`, `years_experience`,
    `intro_video_url`, `intro_video_size`, `intro_video_uploaded_at`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`,
    `tags`, `recommend_weight`
)
SELECT
    @mock_teacher_id,
    'Warm, structured Mandarin coaching for beginners and young learners. I focus on clear pronunciation, daily speaking confidence, and practical conversation.',
    'approved', NULL, NOW(), NULL,
    NULL, JSON_ARRAY('daily', 'kids', 'beginner', 'HSK'),
    'mandarin_cn', JSON_ARRAY('zh', 'en'), 6,
    NULL, NULL, NULL,
    @patch_name, NOW(), @patch_name, NOW(), 0, 0,
    JSON_ARRAY('beginner', 'kids', 'structured'), 90
FROM DUAL
WHERE @mock_teacher_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `teacher_profile`
      WHERE `user_id` = @mock_teacher_id
        AND `deleted` = 0
  );

UPDATE `teacher_profile`
SET `intro` = 'Warm, structured Mandarin coaching for beginners and young learners. I focus on clear pronunciation, daily speaking confidence, and practical conversation.',
    `audit_status` = 'approved',
    `reject_reason` = NULL,
    `audited_at` = COALESCE(`audited_at`, NOW()),
    `audited_by` = NULL,
    `level` = NULL,
    `expertise` = JSON_ARRAY('daily', 'kids', 'beginner', 'HSK'),
    `accent` = 'mandarin_cn',
    `languages` = JSON_ARRAY('zh', 'en'),
    `years_experience` = 6,
    `tags` = JSON_ARRAY('beginner', 'kids', 'structured'),
    `recommend_weight` = 90,
    `deleted` = 0,
    `updater` = @patch_name,
    `update_time` = NOW()
WHERE `user_id` = @mock_teacher_id;

INSERT INTO `teacher_qualification` (
    `user_id`, `doc_type`, `doc_url`, `doc_filename`, `audit_status`,
    `reject_reason`, `audited_at`, `audited_by`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT
    @mock_teacher_id, 'id_card', 'mock/teacher/maya-chen/id-card.pdf',
    'maya-chen-id-card-mock.pdf', 'approved',
    NULL, NOW(), NULL,
    @patch_name, NOW(), @patch_name, NOW(), 0, 0
FROM DUAL
WHERE @mock_teacher_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `teacher_qualification`
      WHERE `user_id` = @mock_teacher_id
        AND `doc_type` = 'id_card'
        AND `deleted` = 0
  );

INSERT INTO `teacher_qualification` (
    `user_id`, `doc_type`, `doc_url`, `doc_filename`, `audit_status`,
    `reject_reason`, `audited_at`, `audited_by`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT
    @mock_teacher_id, 'degree_cert', 'mock/teacher/maya-chen/degree-cert.pdf',
    'maya-chen-degree-cert-mock.pdf', 'approved',
    NULL, NOW(), NULL,
    @patch_name, NOW(), @patch_name, NOW(), 0, 0
FROM DUAL
WHERE @mock_teacher_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `teacher_qualification`
      WHERE `user_id` = @mock_teacher_id
        AND `doc_type` = 'degree_cert'
        AND `deleted` = 0
  );

INSERT INTO `teacher_schedule` (
    `teacher_id`, `weekday`, `start_time`, `end_time`, `timezone`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT @mock_teacher_id, 1, '19:00:00', '19:30:00', 'Asia/Hong_Kong',
       @patch_name, NOW(), @patch_name, NOW(), 0, 0
FROM DUAL
WHERE @mock_teacher_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `teacher_schedule`
      WHERE `teacher_id` = @mock_teacher_id
        AND `weekday` = 1
        AND `start_time` = '19:00:00'
        AND `end_time` = '19:30:00'
        AND `deleted` = 0
  );

INSERT INTO `teacher_schedule` (
    `teacher_id`, `weekday`, `start_time`, `end_time`, `timezone`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT @mock_teacher_id, 3, '19:00:00', '19:30:00', 'Asia/Hong_Kong',
       @patch_name, NOW(), @patch_name, NOW(), 0, 0
FROM DUAL
WHERE @mock_teacher_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `teacher_schedule`
      WHERE `teacher_id` = @mock_teacher_id
        AND `weekday` = 3
        AND `start_time` = '19:00:00'
        AND `end_time` = '19:30:00'
        AND `deleted` = 0
  );

INSERT INTO `teacher_schedule` (
    `teacher_id`, `weekday`, `start_time`, `end_time`, `timezone`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT @mock_teacher_id, 5, '19:00:00', '19:30:00', 'Asia/Hong_Kong',
       @patch_name, NOW(), @patch_name, NOW(), 0, 0
FROM DUAL
WHERE @mock_teacher_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `teacher_schedule`
      WHERE `teacher_id` = @mock_teacher_id
        AND `weekday` = 5
        AND `start_time` = '19:00:00'
        AND `end_time` = '19:30:00'
        AND `deleted` = 0
  );
