-- 20260524_231500_set_teacher_lesson_fee_usd_3.sql
-- Purpose:
--   Set phase-1 ordinary lesson teacher fee from USD 5 to USD 3 per lesson.
--   teacher_profile.level remains a reserved field for future Level1/2/3 tiers
--   and is not used by phase-1 settlement calculation.
-- Rollback:
--   UPDATE `infra_config`
--   SET `value` = '5', `updater` = 'rollback', `update_time` = NOW()
--   WHERE `config_key` = 'mandarly.income.teacher_lesson_fee_usd' AND `deleted` = b'0';
-- Safety:
--   Affects future bookings only because BookingService snapshots teacher_amount
--   into course_order at booking creation.

SET NAMES utf8mb4;

ALTER TABLE `teacher_profile`
    MODIFY COLUMN `level` VARCHAR(16) DEFAULT NULL COMMENT '一期 NULL,二期预留 Level1/2/3';

UPDATE `infra_config`
SET `value` = '3',
    `name` = '普通课课时费 USD',
    `remark` = '一期固定普通课教师课时费:3 USD/节; teacher_profile.level 预留二期 Level1/2/3 阶梯,一期不参与计算',
    `updater` = 'system',
    `update_time` = NOW()
WHERE `config_key` = 'mandarly.income.teacher_lesson_fee_usd'
  AND `deleted` = b'0';

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, '普通课课时费 USD', 'mandarly.income.teacher_lesson_fee_usd', '3', b'1',
       '一期固定普通课教师课时费:3 USD/节; teacher_profile.level 预留二期 Level1/2/3 阶梯,一期不参与计算',
       'system', NOW(), 'system', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `infra_config`
    WHERE `config_key` = 'mandarly.income.teacher_lesson_fee_usd'
      AND `deleted` = b'0'
);
