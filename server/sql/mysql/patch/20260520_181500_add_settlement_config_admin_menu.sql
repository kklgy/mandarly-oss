-- 20260520_181500_add_settlement_config_admin_menu.sql
-- 目的:
--   在 admin「教育业务」下新增「结算配置」入口,避免运营去 RuoYi 原生
--   「基础设施 -> 配置管理」里查找教师课时费等业务配置。
-- 影响:
--   system_menu 新增 1 个页面菜单 + 2 个按钮权限。
--   默认给 role_id=1(super_admin) / role_id=2(common) 分配权限。
-- 回滚:
--   DELETE FROM system_role_menu WHERE menu_id BETWEEN 3046 AND 3048;
--   DELETE FROM system_menu WHERE id BETWEEN 3046 AND 3048;

SET NAMES utf8mb4;

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3046, '结算配置', '', 2, 17, 3001, 'settlement-config', 'ep:setting', 'edu/settlement-config/index', 'EduSettlementConfig', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='结算配置',
  permission='',
  type=2,
  sort=17,
  parent_id=3001,
  path='settlement-config',
  icon='ep:setting',
  component='edu/settlement-config/index',
  component_name='EduSettlementConfig',
  status=0,
  visible=b'1',
  updater='system',
  update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3047, '查询结算配置', 'infra:config:query', 3, 1, 3046, 0, 'system', 'system'),
  (3048, '修改结算配置', 'infra:config:update', 3, 2, 3046, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name),
  permission=VALUES(permission),
  type=3,
  sort=VALUES(sort),
  parent_id=3046,
  status=0,
  updater='system',
  update_time=NOW();

INSERT INTO system_role_menu (role_id, menu_id, creator, updater)
SELECT r.id, m.id, 'system', 'system'
FROM system_role r
JOIN system_menu m ON m.id BETWEEN 3046 AND 3048
WHERE r.id IN (1, 2)
  AND r.deleted = b'0'
  AND m.deleted = b'0'
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu srm
    WHERE srm.role_id = r.id
      AND srm.menu_id = m.id
      AND srm.deleted = b'0'
  );
