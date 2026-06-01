-- =============================================================================
-- Patch: M6 教师提现审核 admin 菜单 + 4 按钮权限
-- =============================================================================
-- 来源:docs/product/M6-plan.md Phase D / Task D6,spec §6.4 / §7.1
-- 影响:system_menu 表
-- id 段:3021 ~ 3025(接续 M4 §6 patch 的 3020)
-- parent_id:3001(教育业务顶级目录,见 20260507_163801_add_edu_admin_menus.sql)
-- 可重入:全部使用 ON DUPLICATE KEY UPDATE,可重复执行
-- 回滚:DELETE FROM system_menu WHERE id BETWEEN 3021 AND 3025
--
-- 权限点(与后端 @PreAuthorize 对齐,见 TeacherWithdrawalController.java):
--   edu:withdrawal:query         — 查询提现列表 / 详情
--   edu:withdrawal:audit         — 审核提现(approve / reject)
--   edu:withdrawal:pay           — 标记打款成功 / 失败(共用一个权限)
--   edu:withdrawal:reveal-payee  — 查看完整收款信息明文(独立权限,二次确认)
--
-- 默认不预设角色绑定。上线后由超管在后台手动给「财务」「客服」分配按钮权限。
-- =============================================================================

-- ---- 提现审核(menu type=2 + 5 buttons type=3)----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3021, '提现审核', '', 2, 16, 3001, 'withdrawal', 'ep:money', 'edu/withdrawal/index', 'EduWithdrawal', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='提现审核', type=2, sort=16, parent_id=3001, path='withdrawal', icon='ep:money',
  component='edu/withdrawal/index', component_name='EduWithdrawal', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3022, '查询提现', 'edu:withdrawal:query', 3, 1, 3021, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询提现', permission='edu:withdrawal:query', type=3, sort=1, parent_id=3021,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3023, '审核提现', 'edu:withdrawal:audit', 3, 2, 3021, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='审核提现', permission='edu:withdrawal:audit', type=3, sort=2, parent_id=3021,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3024, '标记打款', 'edu:withdrawal:pay', 3, 3, 3021, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='标记打款', permission='edu:withdrawal:pay', type=3, sort=3, parent_id=3021,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3025, '查看完整收款信息', 'edu:withdrawal:reveal-payee', 3, 4, 3021, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查看完整收款信息', permission='edu:withdrawal:reveal-payee', type=3, sort=4, parent_id=3021,
  status=0, updater='system', update_time=NOW();

-- 历史驼峰命名兜底:若 patch 在 perm key 重命名之前执行过,清理失败的 3026 行
DELETE FROM system_menu WHERE id = 3026 AND permission = 'edu:withdrawal:revealPayee';

-- 上线后此 patch 在 mandarly_patch_log 表登记(由 apply-patches.sh 自动写入)
