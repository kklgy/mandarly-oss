-- =============================================================================
-- Patch: D7.1 admin private content cleanup
-- =============================================================================
-- Scope:
--   1) Hide organization/post menus that are not part of the current Mandarly
--      operations scope.
--   2) Soft-delete default template dictionaries from MES/Mall/CRM/ERP/AI/IoT/BPM
--      families so the dictionary page no longer exposes private scaffold data.
-- Idempotent: UPDATE-only and safe to rerun.
-- Rollback:
--   UPDATE system_menu SET visible=b'1', status=0 WHERE id IN (...);
--   UPDATE system_dict_type SET deleted=b'0', deleted_time=NULL, status=1 WHERE ...;
--   UPDATE system_dict_data SET deleted=b'0' WHERE ...;
-- =============================================================================

SET NAMES utf8mb4;

-- Department/post management are framework administration features, not needed
-- for current Mandarly operations.
UPDATE system_menu
SET visible = b'0',
    status = 1,
    updater = 'system',
    update_time = NOW()
WHERE id IN (
  103, 1017, 1018, 1019, 1020,
  104, 1021, 1022, 1023, 1024, 1025
)
  AND deleted = b'0';

-- Hide non-Mandarly dictionary data from the operator-facing dictionary page.
UPDATE system_dict_data
SET deleted = b'1',
    updater = 'system',
    update_time = NOW()
WHERE deleted = b'0'
  AND (
    dict_type REGEXP '^(bpm_|crm_|product_|promotion_|trade_|brokerage_|ai_|iot_|mes_|erp_)'
    OR dict_type REGEXP '^infra_codegen_'
    OR dict_type IN (
      'bpm_oa_leave_type',
      'member_point_biz_type',
      'member_experience_biz_type'
    )
  );

UPDATE system_dict_type
SET status = 1,
    deleted = b'1',
    deleted_time = NOW(),
    updater = 'system',
    update_time = NOW()
WHERE deleted = b'0'
  AND (
    type REGEXP '^(bpm_|crm_|product_|promotion_|trade_|brokerage_|ai_|iot_|mes_|erp_)'
    OR type REGEXP '^infra_codegen_'
    OR type IN (
      'bpm_oa_leave_type',
      'member_point_biz_type',
      'member_experience_biz_type'
    )
  );

