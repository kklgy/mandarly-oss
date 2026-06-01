-- D28 过期 upcoming 兜底 Job 注册
-- 每 15 min 跑一次,扫 status='upcoming' AND scheduledAt+duration+15min < now() 的孤儿订单
-- ⚠️ infra_job 既无 unique(name) 也无 unique(handler_name),走 WHERE NOT EXISTS 兜底,可重入
-- ⚠️ Cron 按 JVM TZ 解析;生产 JVM TZ=UTC
-- handler_name = Spring Bean 名(类名首字母小写)= overdueUpcomingSweepJob
INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`,
                        `cron_expression`, `retry_count`, `retry_interval`,
                        `monitor_timeout`,
                        `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'D28 过期 upcoming 订单兜底', 1, 'overdueUpcomingSweepJob', '',
       '0 */15 * * * ?', 3, 1000, 300000,
       'system', NOW(), 'system', NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `infra_job`
  WHERE `handler_name` = 'overdueUpcomingSweepJob' AND `deleted` = 0
);
