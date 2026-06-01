-- 20260520_180500_fix_teacher_amount_currency_usd.sql
-- 目的:
--   修正 course_order.teacher_amount_currency 误跟随学生套餐币种的问题。
--   一期教师结算课时费来自 infra_config.mandarly.income.teacher_lesson_fee_usd,
--   teacher_income / teacher_balance 均按 USD 入账,订单快照币种也应为 USD。
-- 适用范围:
--   已有正向教师课时费订单,例如 HKD 套餐单课完成后 teacher_amount=5.00
--   但 teacher_amount_currency='HKD' 的历史记录。
-- 回滚:
--   不建议自动回滚;旧值来自学生套餐币种,与教师结算币种语义不一致。

SET NAMES utf8mb4;

ALTER TABLE course_order
  MODIFY COLUMN teacher_amount_currency VARCHAR(8) NOT NULL DEFAULT 'USD' COMMENT '教师收入币种';

UPDATE course_order
SET teacher_amount_currency = 'USD',
    updater = '20260520_180500_fix_teacher_amount_currency_usd',
    update_time = NOW()
WHERE deleted = 0
  AND teacher_amount > 0
  AND teacher_amount_currency <> 'USD';
