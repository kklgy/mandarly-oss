-- =============================================================================
-- Patch: D4 客服 admin 菜单 + 按钮权限
-- =============================================================================
-- 影响:system_menu / system_role_menu
-- id 段:3026 ~ 3035(接续 M6 withdrawal 3025)
-- parent_id:3001(教育业务顶级目录)
-- 可重入:全部使用 ON DUPLICATE KEY UPDATE / NOT EXISTS
-- 回滚:DELETE FROM system_role_menu WHERE menu_id BETWEEN 3026 AND 3035;
--      DELETE FROM system_menu WHERE id BETWEEN 3026 AND 3035;
--
-- 权限点:
--   edu:support-faq:query/create/update/delete
--   edu:support-contact:query/create/update/delete
--   edu:support-inquiry:query
-- =============================================================================

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3026, '客服管理', '', 2, 18, 3001, 'support', 'ep:service', 'edu/support/index', 'EduSupport', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='客服管理', type=2, sort=18, parent_id=3001, path='support', icon='ep:service',
  component='edu/support/index', component_name='EduSupport', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3027, '查询 FAQ', 'edu:support-faq:query', 3, 1, 3026, 0, 'system', 'system'),
  (3028, '新建 FAQ', 'edu:support-faq:create', 3, 2, 3026, 0, 'system', 'system'),
  (3029, '更新 FAQ', 'edu:support-faq:update', 3, 3, 3026, 0, 'system', 'system'),
  (3030, '删除 FAQ', 'edu:support-faq:delete', 3, 4, 3026, 0, 'system', 'system'),
  (3031, '查询联系方式', 'edu:support-contact:query', 3, 5, 3026, 0, 'system', 'system'),
  (3032, '新建联系方式', 'edu:support-contact:create', 3, 6, 3026, 0, 'system', 'system'),
  (3033, '更新联系方式', 'edu:support-contact:update', 3, 7, 3026, 0, 'system', 'system'),
  (3034, '删除联系方式', 'edu:support-contact:delete', 3, 8, 3026, 0, 'system', 'system'),
  (3035, '查询咨询日志', 'edu:support-inquiry:query', 3, 9, 3026, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), permission=VALUES(permission), type=3, sort=VALUES(sort), parent_id=3026,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_role_menu (role_id, menu_id, creator, updater)
SELECT 1, m.id, 'system', 'system'
FROM system_menu m
WHERE m.id BETWEEN 3026 AND 3035
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu srm
    WHERE srm.role_id = 1 AND srm.menu_id = m.id AND srm.deleted = 0
  );
