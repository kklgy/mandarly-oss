-- =============================================================================
-- Patch: 推荐记录页配置卡片权限补齐
-- =============================================================================
-- 背景:
--   admin /edu/referral 页面除了推荐记录查询,还读取/更新 infra_config 中的推荐奖励规则。
--   原菜单只授予 edu:referral:query,业务角色进入页面后会在配置卡片调用
--   /infra/config/page 与 /infra/config/update 时被权限拦截。
-- 影响:
--   在推荐记录菜单下补两个按钮权限,让角色可在该页面范围内配置推荐规则。
-- 可重入:ON DUPLICATE KEY UPDATE
-- 回滚:DELETE FROM system_menu WHERE id IN (3036,3037)
-- =============================================================================

SET NAMES utf8mb4;

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3036, '查询推荐规则配置', 'infra:config:query', 3, 2, 3019, 0, 'system', 'system'),
  (3037, '更新推荐规则配置', 'infra:config:update', 3, 3, 3019, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), permission=VALUES(permission), type=3, sort=VALUES(sort), parent_id=3019,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_role_menu (role_id, menu_id, creator, updater)
SELECT 1, m.id, 'system', 'system'
FROM system_menu m
WHERE m.id IN (3036, 3037)
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu srm
    WHERE srm.role_id = 1 AND srm.menu_id = m.id AND srm.deleted = 0
  );
