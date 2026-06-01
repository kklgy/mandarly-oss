-- D28 过期 upcoming 兜底 Job:course_order 加 abnormal_reason
-- 与既有 abnormal_resolution(admin 人工录入处置备注)语义并存:
--   abnormal_reason       — OverdueUpcomingSweepJob 自动写入的诊断子类
--   abnormal_resolution   — admin 客服查实后人工录入的最终处置结论
ALTER TABLE `course_order`
  ADD COLUMN `abnormal_reason` VARCHAR(64) NULL
    COMMENT 'status=abnormal 时的异常子类(自动写入):meeting_missing/meeting_ongoing_overdue/lcic_init_failed/lcic_no_attendance/meeting_cancelled_orphan/meeting_unknown_status'
  AFTER `cancel_reason`;
