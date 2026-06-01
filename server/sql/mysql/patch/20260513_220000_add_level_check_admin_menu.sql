-- Patch:D4.2 admin §A13 水平测试题库管理菜单
-- 时间:2026-05-13 22:00
-- 影响:新增 admin 菜单「水平测试」及题库/答卷权限;幂等,可重复执行

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3036, '水平测试', '', 2, 19, 3001, 'level-check', 'ep:data-analysis', 'edu/level-check/index', 'EduLevelCheck', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='水平测试', type=2, sort=19, parent_id=3001, path='level-check', icon='ep:data-analysis',
  component='edu/level-check/index', component_name='EduLevelCheck', status=0, visible=b'1',
  updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3037, '查询水平测试', 'edu:level-check:query', 3, 1, 3036, 0, 'system', 'system'),
  (3038, '新建水平测试题目', 'edu:level-check:create', 3, 2, 3036, 0, 'system', 'system'),
  (3039, '更新水平测试题目', 'edu:level-check:update', 3, 3, 3036, 0, 'system', 'system'),
  (3040, '删除水平测试题目', 'edu:level-check:delete', 3, 4, 3036, 0, 'system', 'system'),
  (3041, '导出水平测试答卷', 'edu:level-check:export', 3, 5, 3036, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), permission=VALUES(permission), type=3, sort=VALUES(sort), parent_id=3036,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_role_menu (role_id, menu_id, creator, updater)
SELECT 1, m.id, 'system', 'system'
FROM system_menu m
WHERE m.id BETWEEN 3036 AND 3041
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu srm
    WHERE srm.role_id = 1 AND srm.menu_id = m.id AND srm.deleted = 0
  );
