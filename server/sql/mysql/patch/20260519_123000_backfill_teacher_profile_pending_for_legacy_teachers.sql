-- D19 续 cutover Hot-Patch - 2026-05-19
--
-- 背景:
--   D19 Phase B 上线前(commit c34a6e0 之前)注册的 role=teacher 用户,
--   反射调用 TeacherProfileServiceImpl.createPendingProfile 时因 creator NOT NULL
--   约束 INSERT 失败被 AppAuthServiceImpl.log.warn 吞掉,导致 user 行已建好但
--   teacher_profile 行没建上。c34a6e0 fix 显式 setCreator/setUpdater 之后注册的
--   教师不再有此问题,但历史漏行需要回填,否则:
--     - PUT /app-api/edu/teacher-center/profile/me → 404 教师档案不存在
--     - POST /submit-audit → 404
--     - admin /edu/teacher-profile/page 看不到该教师
--
-- 回填策略:
--   - audit_status = 'pending'(等待教师补充资质 + 提交审核)
--   - creator/updater = user.id 字符串(与 c34a6e0 fix 对齐,标记"自注册"来源)
--   - tenant_id 跟随 user.tenant_id
--   - 其它业务字段(intro / expertise / languages / accent / yearsExperience /
--     introVideoUrl)留 NULL,由教师自己在教师中心补
--
-- 幂等:
--   INSERT ... SELECT ... WHERE NOT EXISTS,重跑只补真正缺行,不覆盖已有数据
--   patch_log 用 INSERT IGNORE 防重跑唯一键冲突
--
-- 不可回滚:回填的 pending 行如要撤销,需手工 DELETE FROM teacher_profile
--          WHERE user_id IN (...) — 但仅在确认无后续业务数据(资质上传 / 排课 /
--          下单)依赖时安全。

SET NAMES utf8mb4;

-- 1) 回填漏行教师的 pending profile
INSERT INTO teacher_profile (
  user_id, audit_status, creator, updater,
  create_time, update_time, deleted, tenant_id, recommend_weight
)
SELECT
  u.id,
  'pending',
  CAST(u.id AS CHAR),
  CAST(u.id AS CHAR),
  NOW(),
  NOW(),
  0,
  u.tenant_id,
  0
FROM `user` u
WHERE u.role = 'teacher'
  AND u.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM teacher_profile p WHERE p.user_id = u.id
  );

-- 2) 登记 mandarly_patch_log(INSERT IGNORE 兼容重跑)
INSERT IGNORE INTO mandarly_patch_log (patch_file, executed_at, executor, note)
VALUES (
  '20260519_123000_backfill_teacher_profile_pending.sql',
  NOW(),
  'd19-hotfix',
  'D19 续: 回填 c34a6e0 fix 之前注册的历史教师 pending profile,修 PUT /me 404'
);
