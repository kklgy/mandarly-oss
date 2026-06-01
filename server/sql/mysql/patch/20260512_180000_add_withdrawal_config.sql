-- =============================================================================
-- M6 A7:教师提现最低金额改走 infra_config 热更新(spec §7.5)
--
-- 背景:
--   A4 时 TeacherWithdrawalServiceImpl 通过 @Value 读 application.yaml 的
--   mandarly.withdrawal.min-amount。A7 收口改为 ConfigApi.getConfigValueByKey
--   读 infra_config 表,支持运维在 admin 端 → 参数配置 中热更新,无需重启。
--
--   Service 内 fallback 默认 100 USD(配置缺失 / 解析失败均走默认),所以本补丁
--   只是把配置项显式落到数据库,方便后台可见可改。
--
-- ⚠️ infra_config.idx_config_key 是非 unique KEY,不能 ON DUPLICATE KEY UPDATE,
--    走 INSERT ... WHERE NOT EXISTS 模式可重入(参考 20260509_103000_add_payment_tables.sql)。
-- =============================================================================

INSERT INTO `infra_config` (`category`, `type`, `name`, `config_key`, `value`, `visible`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'mandarly', 2, '最低提现金额(USD)', 'mandarly.withdrawal.min_amount', '100', 1, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_config` WHERE `config_key` = 'mandarly.withdrawal.min_amount');
