-- =============================================================================
-- Patch: M4 Stripe 支付主线 admin 菜单(支付 + 退款 + 教师收入 + 推荐记录)
-- =============================================================================
-- 来源:docs/progress/m4-stripe-payment-plan.md Phase 6 收尾
-- 影响:system_menu 表 + system_role_menu 表
-- id 段:3012 ~ 3020(接续 M0 §9 patch 的 3011)
-- parent_id:3001(教育业务顶级目录,见 20260507_163801_add_edu_admin_menus.sql)
-- 可重入:全部使用 ON DUPLICATE KEY UPDATE,可重复执行
-- 回滚:DELETE FROM system_menu WHERE id BETWEEN 3012 AND 3020
-- =============================================================================

-- ---- 1) 支付管理(menu type=2 + 1 button type=3) ----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3012, '支付管理', '', 2, 12, 3001, 'payment', 'ep:credit-card', 'edu/payment/index', 'EduPayment', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='支付管理', type=2, sort=12, parent_id=3001, path='payment', icon='ep:credit-card',
  component='edu/payment/index', component_name='EduPayment', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3013, '查询支付', 'edu:payment:query', 3, 1, 3012, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询支付', permission='edu:payment:query', type=3, sort=1, parent_id=3012,
  status=0, updater='system', update_time=NOW();

-- ---- 2) 退款管理(menu + 2 buttons:query + approve) ----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3014, '退款管理', '', 2, 13, 3001, 'refund', 'ep:tickets', 'edu/refund/index', 'EduRefund', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='退款管理', type=2, sort=13, parent_id=3001, path='refund', icon='ep:tickets',
  component='edu/refund/index', component_name='EduRefund', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3015, '查询退款', 'edu:refund:query', 3, 1, 3014, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询退款', permission='edu:refund:query', type=3, sort=1, parent_id=3014,
  status=0, updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3016, '审核退款', 'edu:refund:approve', 3, 2, 3014, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='审核退款', permission='edu:refund:approve', type=3, sort=2, parent_id=3014,
  status=0, updater='system', update_time=NOW();

-- ---- 3) 教师收入(menu + 1 button) ----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3017, '教师收入', '', 2, 14, 3001, 'teacher-income', 'ep:money', 'edu/teacher-income/index', 'EduTeacherIncome', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='教师收入', type=2, sort=14, parent_id=3001, path='teacher-income', icon='ep:money',
  component='edu/teacher-income/index', component_name='EduTeacherIncome', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3018, '查询收入', 'edu:teacher-income:query', 3, 1, 3017, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询收入', permission='edu:teacher-income:query', type=3, sort=1, parent_id=3017,
  status=0, updater='system', update_time=NOW();

-- ---- 4) 推荐记录(menu + 1 button) ----
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater)
VALUES
  (3019, '推荐记录', '', 2, 15, 3001, 'referral', 'ep:share', 'edu/referral/index', 'EduReferral', 0, b'1', b'1', b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='推荐记录', type=2, sort=15, parent_id=3001, path='referral', icon='ep:share',
  component='edu/referral/index', component_name='EduReferral', status=0, visible=b'1', updater='system', update_time=NOW();

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, status, creator, updater)
VALUES
  (3020, '查询推荐', 'edu:referral:query', 3, 1, 3019, 0, 'system', 'system')
ON DUPLICATE KEY UPDATE
  name='查询推荐', permission='edu:referral:query', type=3, sort=1, parent_id=3019,
  status=0, updater='system', update_time=NOW();

-- ---- 给 admin 角色(role_id=1)绑权限(id 3012~3020) ----
INSERT INTO system_role_menu (role_id, menu_id, creator, updater)
SELECT 1, m.id, 'system', 'system'
FROM system_menu m
WHERE m.id BETWEEN 3012 AND 3020
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu srm
    WHERE srm.role_id = 1 AND srm.menu_id = m.id AND srm.deleted = 0
  );

-- 上线后此 patch 在 mandarly_patch_log 表登记(由 apply-patches.sh 自动写入)
