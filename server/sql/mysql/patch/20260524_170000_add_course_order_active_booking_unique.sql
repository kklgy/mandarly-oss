-- 20260524_170000_add_course_order_active_booking_unique.sql
-- Purpose:
--   Add a database-level fallback for concurrent booking of the same teacher slot.
--   Service layer now uses Redis lock `booking:teacher-slot:{teacherId}:{scheduledAt}`;
--   this generated-column unique key protects against bypasses and race windows.
-- Rollback:
--   ALTER TABLE `course_order` DROP INDEX `uk_teacher_scheduled_active`, DROP COLUMN `active_upcoming_key`;
-- Safety:
--   The generated column is 1 only for status='upcoming' and deleted=0.
--   MySQL UNIQUE allows multiple NULL values, so cancelled/finished/abnormal history is unaffected.

SET NAMES utf8mb4;

ALTER TABLE `course_order`
    ADD COLUMN `active_upcoming_key` TINYINT
        GENERATED ALWAYS AS (
            CASE WHEN `status` = 'upcoming' AND `deleted` = 0 THEN 1 ELSE NULL END
        ) STORED
        COMMENT 'generated: active upcoming booking uniqueness key' AFTER `is_free_trial`,
    ADD UNIQUE KEY `uk_teacher_scheduled_active` (`teacher_id`, `scheduled_at`, `active_upcoming_key`);
