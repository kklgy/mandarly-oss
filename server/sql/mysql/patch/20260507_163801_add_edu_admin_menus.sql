-- =============================================================================
-- Patch:教育业务 admin 菜单(C-min:教师审核 + 套餐管理 + 订单看板)
-- =============================================================================
-- 来源:M0 收尾 / docs/progress/M0-启动初始化.md / C-min 范围
-- 影响:system_menu 表;新增 1 顶级目录 + 3 子菜单 + 7 按钮权限点
-- id 段:3001 ~ 3011(MAX(id)=5010 当前段干净;3000+ 段 reserved for edu)
-- 可重入:全部使用 ON DUPLICATE KEY UPDATE,可重复执行
-- 回滚:DELETE FROM system_menu WHERE id BETWEEN 3001 AND 3011
-- =============================================================================

-- ---- 顶级目录:教育业务 ----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3001, '教育业务', '', 1, 30, 0, '/edu', 'ep:reading', NULL, NULL, 0, b'1', b'1', b'1', '1', '1')
ON DUPLICATE KEY UPDATE
  name='教育业务', type=1, sort=30, parent_id=0, path='/edu', icon='ep:reading',
  component=NULL, component_name=NULL, status=0, visible=b'1', updater='1', update_time=NOW();

-- ---- 1) 教师审核(menu + 2 buttons)----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3002, '教师审核', '', 2, 1, 3001, 'teacher', 'ep:user-filled', 'edu/teacher/index', 'EduTeacher', 0, b'1', b'1', b'1', '1', '1')
ON DUPLICATE KEY UPDATE
  name='教师审核', type=2, sort=1, parent_id=3001, path='teacher', icon='ep:user-filled',
  component='edu/teacher/index', component_name='EduTeacher', status=0, visible=b'1', updater='1', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3003, '查询教师档案', 'edu:teacher-profile:query', 3, 1, 3002, 0, '1', '1')
ON DUPLICATE KEY UPDATE
  name='查询教师档案', permission='edu:teacher-profile:query', type=3, sort=1, parent_id=3002,
  status=0, updater='1', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3004, '审核教师档案', 'edu:teacher-profile:audit', 3, 2, 3002, 0, '1', '1')
ON DUPLICATE KEY UPDATE
  name='审核教师档案', permission='edu:teacher-profile:audit', type=3, sort=2, parent_id=3002,
  status=0, updater='1', update_time=NOW();

-- ---- 2) 套餐管理(menu + 4 buttons)----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3005, '套餐管理', '', 2, 2, 3001, 'package', 'ep:goods', 'edu/package/index', 'EduPackage', 0, b'1', b'1', b'1', '1', '1')
ON DUPLICATE KEY UPDATE
  name='套餐管理', type=2, sort=2, parent_id=3001, path='package', icon='ep:goods',
  component='edu/package/index', component_name='EduPackage', status=0, visible=b'1', updater='1', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3006, '查询套餐', 'edu:package:query', 3, 1, 3005, 0, '1', '1')
ON DUPLICATE KEY UPDATE
  name='查询套餐', permission='edu:package:query', type=3, sort=1, parent_id=3005,
  status=0, updater='1', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3007, '新建套餐', 'edu:package:create', 3, 2, 3005, 0, '1', '1')
ON DUPLICATE KEY UPDATE
  name='新建套餐', permission='edu:package:create', type=3, sort=2, parent_id=3005,
  status=0, updater='1', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3008, '更新套餐', 'edu:package:update', 3, 3, 3005, 0, '1', '1')
ON DUPLICATE KEY UPDATE
  name='更新套餐', permission='edu:package:update', type=3, sort=3, parent_id=3005,
  status=0, updater='1', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3009, '删除套餐', 'edu:package:delete', 3, 4, 3005, 0, '1', '1')
ON DUPLICATE KEY UPDATE
  name='删除套餐', permission='edu:package:delete', type=3, sort=4, parent_id=3005,
  status=0, updater='1', update_time=NOW();

-- ---- 3) 订单看板(menu + 1 button,C-min 只读)----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3010, '订单看板', '', 2, 3, 3001, 'course-order', 'ep:tickets', 'edu/course-order/index', 'EduCourseOrder', 0, b'1', b'1', b'1', '1', '1')
ON DUPLICATE KEY UPDATE
  name='订单看板', type=2, sort=3, parent_id=3001, path='course-order', icon='ep:tickets',
  component='edu/course-order/index', component_name='EduCourseOrder', status=0, visible=b'1', updater='1', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3011, '查询订单', 'edu:course-order:query', 3, 1, 3010, 0, '1', '1')
ON DUPLICATE KEY UPDATE
  name='查询订单', permission='edu:course-order:query', type=3, sort=1, parent_id=3010,
  status=0, updater='1', update_time=NOW();

-- 上线后此 patch 在 mandarly_patch_log 表登记(由 apply-patches.sh 自动写入)
