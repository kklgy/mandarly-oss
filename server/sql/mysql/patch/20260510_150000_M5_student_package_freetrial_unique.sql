-- =============================================================================
-- M5 self-review P0-2 加固 — student_package 免费体验 / 注册赠送防双发
-- 来源: 2026-05-10 M5 self-review,Subagent 3 报 free_trial 竞态(c 方案 DB 兜底)
-- 触发场景: 多 tab 同时注册回调 / 多次点 ?ref= 领取,绕过应用层 selectCount 重查
-- =============================================================================
--
-- ⚠️ 可选 patch(OPTIONAL,not blocking M5 上线):
--   - 应用层 StudentPackageServiceImpl.grantFreeTrialPackage 已有 selectCount 快路径
--     (2026-05-10 加 catch DataIntegrityViolationException 慢路径兜底)
--   - 本 patch 加 DB functional unique index 闭环防护;未启用时实际损失也可控
--     (并发窗口小 + 白嫖一笔免费课 ≈ 1-2 节 30min 体验,平台成本可承受)
--
-- ⚠️ 上线前验证(必做):
--   1. MySQL 版本 ≥ 8.0.13(functional index 支持)
--   2. 跑下面 SELECT 确认无重复 — 有重复需先清理或归并
--      SELECT student_id, source, COUNT(*) AS cnt
--        FROM student_package
--        WHERE source IN ('register_grant', 'free_trial') AND deleted = 0
--        GROUP BY student_id, source
--        HAVING COUNT(*) > 1;
--   3. 锁表评估:student_package 表数据量 < 10万 时 INPLACE 重建 < 30s,
--      生产 > 10万 跑 staging 验证锁写时间,或用 pt-online-schema-change
--
-- ⚠️ 索引语义:
--   - functional expression: CASE WHEN source IN ('register_grant','free_trial')
--                                 THEN student_id END
--   - source NOT IN 这两种时,表达式返 NULL,unique 不约束(MySQL NULL 视为不等)
--   - 即:purchase / admin_grant 不受影响,可一个学生多笔
--   - register_grant 同 student_id 唯一 ✓
--   - free_trial 同 student_id 唯一 ✓
--   - 跨 source(register_grant + free_trial)不互斥 → 应用层 hasClaimedFreeTrial
--     已合并判定,不会真的并发不同 source 同时发(不同入口、串行调用)
-- =============================================================================

ALTER TABLE student_package
  ADD UNIQUE INDEX uk_student_freetrial_grant (
    (CASE WHEN source IN ('register_grant', 'free_trial') THEN student_id END),
    source
  );

-- 验证(手工执行):
--   SHOW INDEX FROM student_package WHERE Key_name = 'uk_student_freetrial_grant';
--
-- 回滚:
--   ALTER TABLE student_package DROP INDEX uk_student_freetrial_grant;
--
-- 标记 patch 已执行(M0 阶段 mandarly_patch_log 若已建):
-- INSERT INTO mandarly_patch_log (patch_file, executed_at)
-- VALUES ('20260510_150000_M5_student_package_freetrial_unique.sql', NOW());
