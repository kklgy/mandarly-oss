-- D7 admin private cleanup: remove legacy template avatar URLs from admin users.
-- Rollback: set avatar back to a chosen HTTPS asset via system user profile or SQL.

UPDATE system_users
SET avatar = NULL,
    updater = 'migration',
    update_time = NOW()
WHERE deleted = 0
  AND avatar LIKE '%iocoder.cn%';
