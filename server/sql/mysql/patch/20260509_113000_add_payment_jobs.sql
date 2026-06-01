-- M4 Stripe 3 个 Job 注册
-- ⚠️ infra_job 既无 unique(name) 也无 unique(handler_name),走 WHERE NOT EXISTS 兜底,可重入
-- ⚠️ Cron 按 JVM TZ 解析;生产 JVM TZ=UTC,Phase 10 Task 10.2 上线 checklist 确认
-- handler_name = Spring Bean 名(类名首字母小写)

-- 1. 教师余额冻结转可用 Job(daily UTC 03:00 = HKT 11:00)
INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'M4 教师余额冻结转可用', 1, 'teacherBalanceFrozenToAvailableJob', '', '0 0 3 * * ?', 3, 1000, 300000, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_job` WHERE `handler_name` = 'teacherBalanceFrozenToAvailableJob' AND `deleted` = 0);

-- 2. 教师收入兜底建账 Job(daily UTC 04:00 = HKT 12:00)
INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'M4 教师收入兜底建账', 1, 'teacherIncomeBackfillJob', '', '0 0 4 * * ?', 3, 1000, 300000, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_job` WHERE `handler_name` = 'teacherIncomeBackfillJob' AND `deleted` = 0);

-- 3. 退款孤儿对账 Job(weekly Mon UTC 05:00 = HKT 13:00)
INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'M4 退款孤儿对账', 1, 'refundOrphanCheckJob', '', '0 0 5 ? * MON', 3, 1000, 600000, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_job` WHERE `handler_name` = 'refundOrphanCheckJob' AND `deleted` = 0);
