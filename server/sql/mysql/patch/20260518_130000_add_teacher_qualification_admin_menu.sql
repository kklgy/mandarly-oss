-- D19 Phase B Task B5 - 2026-05-18
-- 目的: 给 admin 端「教师审核」menu(id=3002)挂一颗按钮权限点
--       'edu:teacher-qualification:query',对应 GET /admin-api/edu/teacher-qualification/list
--       admin 在教师详情页查看资质材料 + 在线预签名 URL 预览证件
-- id 段: 3045(接续 D14 admin_user_menu 3042-3044;menu 3005-3041 在历次 patch 已被占)
-- 影响: system_menu(新增 1 条按钮) + system_role_menu(超管 role_id=1 自动授权)
-- 可重入: ON DUPLICATE KEY UPDATE + NOT EXISTS 双保险
-- 回滚:
--   DELETE FROM system_role_menu WHERE menu_id = 3045;
--   DELETE FROM system_menu WHERE id = 3045;

SET NAMES utf8mb4;

-- 1) 按钮权限点(挂在「教师审核」menu 3002 下,sort=3 接续 3003/3004)
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3045, '查询教师资质', 'edu:teacher-qualification:query', 3, 3, 3002, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询教师资质', permission='edu:teacher-qualification:query',
  type=3, sort=3, parent_id=3002, status=0,
  updater='system', update_time=NOW();

-- 2) 默认给超管 role_id=1 授权(其他 admin 角色按需在 UI 勾选)
INSERT INTO system_role_menu (role_id, menu_id, creator, updater)
SELECT 1, 3045, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM system_role_menu srm
    WHERE srm.role_id = 1 AND srm.menu_id = 3045 AND srm.deleted = 0
  );

-- 3) 登记到 mandarly_patch_log(若依约定;patch_file UNIQUE,重复执行会报错提示)
INSERT INTO mandarly_patch_log (patch_file, executed_at, executor, note)
VALUES (
  '20260518_130000_add_teacher_qualification_admin_menu.sql',
  NOW(),
  'd19-migration',
  'D19 Phase B Task B5 - admin 教师资质查询权限按钮 edu:teacher-qualification:query'
);
