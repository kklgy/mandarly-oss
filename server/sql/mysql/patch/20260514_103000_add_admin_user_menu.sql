-- Patch:D12 admin §A2 C 端用户管理菜单
-- 时间:2026-05-14 10:30
-- 影响:新增 admin 菜单「用户管理」及查询 / 冻结权限;幂等,可重复执行
-- 回滚:DELETE FROM system_role_menu WHERE menu_id BETWEEN 3042 AND 3044;
--      DELETE FROM system_menu WHERE id BETWEEN 3042 AND 3044;

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3042, '用户管理', '', 2, 0, 3001, 'user', 'ep:user', 'edu/user/index', 'EduUser', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='用户管理', type=2, sort=0, parent_id=3001, path='user', icon='ep:user',
  component='edu/user/index', component_name='EduUser', status=0, visible=b'1',
  updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3043, '查询 C 端用户', 'edu:user:query', 3, 1, 3042, 0, 'system', 'system'),
  (3044, '冻结 C 端用户', 'edu:user:freeze', 3, 2, 3042, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), permission=VALUES(permission), type=3, sort=VALUES(sort), parent_id=3042,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_role_menu (role_id, menu_id, creator, updater)
SELECT 1, m.id, 'system', 'system'
FROM system_menu m
WHERE m.id BETWEEN 3042 AND 3044
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu srm
    WHERE srm.role_id = 1 AND srm.menu_id = m.id AND srm.deleted = 0
  );
